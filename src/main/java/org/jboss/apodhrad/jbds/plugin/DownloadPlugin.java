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

	public void donwload(String url) throws MojoExecutionException {
		executeMojo(
				plugin(groupId("com.googlecode.maven-download-plugin"),
						artifactId("maven-download-plugin"),
						version("0.2-SNAPSHOT")), goal("wget"),
				configuration(element(name("url"), url)),
				executionEnvironment(mavenProject, mavenSession, pluginManager));
	}
}
