package org.jboss.apodhrad.jbds.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

public class DownloadPlugin extends MojoExecutor {

	private MavenProject mavenProject;

	private MavenSession mavenSession;

	private BuildPluginManager pluginManager;

	public DownloadPlugin(MavenProject mavenProject, MavenSession mavenSession,
			BuildPluginManager pluginManager) {
		this.mavenProject = mavenProject;
		this.mavenSession = mavenSession;
		this.pluginManager = pluginManager;
	}

	public void download(String url) throws MojoExecutionException {
		download(url, true);
	}
	
	public void download(String url, boolean unpack) throws MojoExecutionException {
		executeMojo(
				plugin(groupId("com.googlecode.maven-download-plugin"),
						artifactId("maven-download-plugin"),
						version("1.0.0")), goal("wget"),
				configuration(element(name("url"), url), element("unpack", Boolean.toString(unpack))),
				executionEnvironment(mavenProject, mavenSession, pluginManager));
	}
}
