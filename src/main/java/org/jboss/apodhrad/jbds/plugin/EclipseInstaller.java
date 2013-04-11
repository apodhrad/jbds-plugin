package org.jboss.apodhrad.jbds.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jboss.apodhrad.jbds.plugin.eclipse.Eclipse;

/**
 * Installs Eclipse
 */
@Mojo(name = "eclipse", defaultPhase = LifecyclePhase.COMPILE)
public class EclipseInstaller extends AbstractMojo {

	public static final String DEFAULT_VERSION = "jee-juno-SR2";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	@Component
	private BuildPluginManager manager;

	@Parameter
	private File installer;

	@Parameter
	private String version;

	@SuppressWarnings("rawtypes")
	@Parameter(defaultValue = "${project.remoteArtifactRepositories}")
	private java.util.List remoteRepositories;

	@Parameter
	private Set<String> features;

	public void execute() throws MojoExecutionException {
		String url = computeUrl();
		getLog().info("Downloading " + url);
		DownloadPlugin downloadPlugin = new DownloadPlugin(project, session, manager);
		downloadPlugin.download(url, isWindowsPlatform(), getEclipseInstaller());
		if (!isWindowsPlatform()) {
			untar(getEclipseInstaller());
		}

		// Install features
		Eclipse eclipse = new Eclipse(target + "/eclipse");
		for (Object obj : remoteRepositories) {
			if (obj instanceof MavenArtifactRepository) {
				MavenArtifactRepository repo = (MavenArtifactRepository) obj;
				if (repo.getLayout().getId().equals("update-site")) {
					getLog().info("Added update site " + repo.getId() + " at " + repo.getUrl());
					eclipse.addUpdateSite(repo.getUrl());
				}
			}
		}

		System.out.println("Features: " + features);
		if (features != null && !features.isEmpty()) {
			eclipse.installFeatures(features);
		}

		getLog().info("Done");
	}

	private void untar(String tarFile) {
		String command = "tar xvf " + target + "/" + tarFile + " -C " + target;
		getLog().info(command);
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = bri.readLine()) != null) {
				getLog().debug(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				getLog().error(line);
			}
			bre.close();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception during executing '" + command + "'");
		}
		new File(target + "/" + tarFile).delete();
	}

	private String computeUrl() {
		String url = "http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release";
		String[] version = getVersion().split("-");
		url += "/" + version[1] + "/" + version[2] + "/" + getEclipseInstaller();
		return url + "&r=1";
	}

	public String getVersion() {
		if (version == null) {
			return DEFAULT_VERSION;
		}
		return version;
	}

	public String getEclipseInstaller() {
		String os_property = System.getProperty("os.name").toLowerCase();
		String arch_property = System.getProperty("os.arch");

		String platform = null;
		String archive = "zip";

		if (os_property.contains("linux")) {
			platform = "linux-gtk";
			archive = "tar.gz";
		} else if (os_property.contains("win")) {
			platform = "win32";
			archive = "zip";
		} else if (os_property.contains("mac")) {
			platform = "macosx-cocoa";
			archive = "tar.gz";
		}

		if (platform == null) {
			throw new RuntimeException("Unknown platform '" + os_property + "'");
		}

		if (arch_property.contains("64")) {
			platform += "-x86_64";
		}

		return "eclipse-" + getVersion() + "-" + platform + "." + archive;
	}

	public boolean isWindowsPlatform() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}
}
