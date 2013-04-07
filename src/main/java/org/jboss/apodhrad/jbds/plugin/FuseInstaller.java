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

import java.io.File;
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jboss.apodhrad.jbds.plugin.finder.Finder;
import org.jboss.apodhrad.jbds.plugin.finder.FuseFinder;

/**
 * Installs JBDS
 */
@Mojo(name = "fuse", defaultPhase = LifecyclePhase.COMPILE)
public class FuseInstaller extends AbstractMojo {

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

	public void execute() throws MojoExecutionException {
		getLog().info("Version: " + version);
		String url = null;
		Finder finder = new FuseFinder();
		finder.setLog(getLog());
		try {
			url = finder.getUrl(version);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("I/O Exception");
		}
		getLog().info("Downloading FueIDE from " + url);
		new DownloadPlugin(project, session, manager).download(url);
	}
}
