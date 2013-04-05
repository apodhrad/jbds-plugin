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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Installs JBDS
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.COMPILE)
public class JBDSInstaller extends AbstractMojo {

	public static final String HOST = "http://machydra.brq.redhat.com/www.qa.jboss.com/binaries/RHDS/builds";
	public static final String JBDS = "jbdevstudio-product-universal-6.0.0.GA-v20121206-1855-B186.jar";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private BuildPluginManager manager;
	@Parameter
	private File jbdsInstaller;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	public void execute() throws MojoExecutionException {
		getLog().info("Downloading...");
		String url = HOST + "/stable/6.0.0.GA.installer/" + JBDS;
		new DownloadPlugin(project, session, manager).donwload(url);
		getLog().info("Downloaded");

		getLog().info("Installing " + jbdsInstaller.getAbsolutePath());
		getLog().info("Exists? " + jbdsInstaller.exists());

		getLog().info("" + System.getProperty("os.name"));
		getLog().info("" + System.getProperty("os.arch"));
		getLog().info("" + System.getProperty("os.version"));

		getLog().info("" + System.getProperty("JAVA_HOME"));
		getLog().info("" + System.getenv("JAVA_HOME"));

		getLog().info("project.build.directory = " + target);

		createInstallerFile();
		installJbds(JBDS);

		getLog().info("Installation completed");
	}

	private void createInstallerFile() throws MojoExecutionException {
		String dest = target + "/jbds";
		String jre = System.getenv("JAVA_HOME") + "/jre/bin/java";

		String tempFile = target + "/install.xml";
		String targetFile = target + "/installation.xml";

		URL url = getClass().getResource("/install.xml");

		try {
			FileUtils.copyURLToFile(url, new File(tempFile));
			BufferedReader in = new BufferedReader(new FileReader(tempFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(targetFile));
			String line = null;
			while ((line = in.readLine()) != null) {
				out.write(line.replace("@DEST@", dest).replace("@JRE@", jre));
				out.newLine();
			}
			out.flush();
			out.close();
			in.close();

			new File(tempFile).delete();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("I/O Exception");
		}
	}

	private void installJbds(String jbdsInstaller)
			throws MojoExecutionException {
		String command = "java -jar " + target + "/" + jbdsInstaller + " "
				+ target + "/installation.xml";
		getLog().info("Exec: " + command);

		// Run a java app in a separate system process
		Process proc;
		try {
			proc = Runtime.getRuntime().exec(command);
			// Then retrieve the process output
			InputStream in = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = br.readLine()) != null) {
				getLog().info(line);
			}
			// InputStream err = proc.getErrorStream();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("Exec Exception");
		}
	}

}
