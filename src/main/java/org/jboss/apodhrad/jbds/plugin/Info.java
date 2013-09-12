package org.jboss.apodhrad.jbds.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 
 * Info Goal
 * 
 * @author apodhrad
 * 
 */
@Mojo(name = "info", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class Info extends AbstractMojo {

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private BuildPluginManager manager;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	public void execute() throws MojoExecutionException {
		getLog().info("Info");
		getLog().info("Project = " + project.getArtifactId());
		DropinPlugin dropinPlugin = new DropinPlugin(project, session, manager, target + "/dropins");
		dropinPlugin.addDropin("org.eclipse.tycho", "org.eclipse.tycho.surefire.osgibooter",
				"0.18.0");
		dropinPlugin.createDropinFolder();
		getLog().info("Finished");
	}

}
