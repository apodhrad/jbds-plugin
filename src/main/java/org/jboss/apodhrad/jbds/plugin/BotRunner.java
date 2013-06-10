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
 * Run bot
 * 
 * @author apodhrad
 * 
 */
@Mojo(name = "runbot", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class BotRunner extends AbstractMojo {

	public static final String DEFAULT_PRODUCT = "com.jboss.jbds.product.product";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private BuildPluginManager manager;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	@Parameter(alias = "eclipse.home", required = true)
	private String eclipseHome;

	@Parameter(alias = "test.plugin", required = true)
	private String pluginName;

	@Parameter(alias = "test.class", required = true)
	private String className;

	@Parameter(defaultValue = DEFAULT_PRODUCT)
	private String product;

	public void execute() throws MojoExecutionException {
		Eclipse eclipse = new Eclipse(eclipseHome);

		eclipse.runBot(product, pluginName, className, target);
		getLog().info("Finished");
	}

}
