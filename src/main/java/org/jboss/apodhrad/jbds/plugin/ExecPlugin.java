package org.jboss.apodhrad.jbds.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * 
 * @author apodhrad
 * 
 */
public class ExecPlugin extends MojoExecutor {

	private MavenProject mavenProject;

	private MavenSession mavenSession;

	private BuildPluginManager pluginManager;

	public ExecPlugin(MavenProject mavenProject, MavenSession mavenSession,
			BuildPluginManager pluginManager) {
		this.mavenProject = mavenProject;
		this.mavenSession = mavenSession;
		this.pluginManager = pluginManager;
	}

	public void tar(String options, String arg) throws MojoExecutionException {
		executeMojo(
				plugin(groupId("org.codehaus.mojo"), artifactId("exec-maven-plugin"),
						version("1.2.1")),
				goal("exec"),
				configuration(
						element(name("executable"), "tar"),
						element(name("workingDirectory"), "target"),
						element("arguments", element(name("argument"), options),
								element(name("argument"), arg))),
				executionEnvironment(mavenProject, mavenSession, pluginManager));
	}
}
