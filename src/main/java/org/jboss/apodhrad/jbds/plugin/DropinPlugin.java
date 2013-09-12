package org.jboss.apodhrad.jbds.plugin;

import java.util.ArrayList;
import java.util.List;

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
public class DropinPlugin extends MojoExecutor {

	private MavenProject mavenProject;
	private MavenSession mavenSession;
	private BuildPluginManager pluginManager;
	private List<Dropin> dropins;
	private String dropinFolder;

	public DropinPlugin(MavenProject mavenProject, MavenSession mavenSession,
			BuildPluginManager pluginManager, String dropinFolder) {
		this.mavenProject = mavenProject;
		this.mavenSession = mavenSession;
		this.pluginManager = pluginManager;
		this.dropinFolder = dropinFolder;
		dropins = new ArrayList<DropinPlugin.Dropin>();
	}

	public void addDropin(String group, String artifact) {
		dropins.add(new Dropin(group, artifact, "0.0.0"));
	}

	public void addDropin(String group, String artifact, String version) {
		dropins.add(new Dropin(group, artifact, version));
	}

	public void addDropin(Dropin dropin) {
		dropins.add(dropin);
	}

	public void createDropinFolder() throws MojoExecutionException {
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"),
						version("2.8")), goal("copy"), configuration(getArtifactItems()),
				executionEnvironment(mavenProject, mavenSession, pluginManager));
	}

	private Element getArtifactItems() {
		List<Element> artifactItems = new ArrayList<MojoExecutor.Element>();
		for (Dropin dropin : dropins) {
			artifactItems.add(getArtifactItem(dropin));
		}
		return element("artifactItems", artifactItems.toArray(new Element[artifactItems.size()]));
	}

	private Element getArtifactItem(Dropin dropin) {
		return element("artifactItem", element("groupId", dropin.getGroup()),
				element("artifactId", dropin.getArtifact()),
				element("version", dropin.getVersion()), element("type", "jar"),
				element("outputDirectory", dropinFolder));
	}

	public class Dropin {

		private String group;
		private String artifact;
		private String version;

		public Dropin(String group, String artifact, String version) {
			this.group = group;
			this.artifact = artifact;
			this.version = version;
		}

		public String getGroup() {
			return group;
		}

		public String getArtifact() {
			return artifact;
		}

		public String getVersion() {
			return version;
		}

	}
}
