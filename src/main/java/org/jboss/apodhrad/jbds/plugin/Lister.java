package org.jboss.apodhrad.jbds.plugin;

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

/**
 * 
 * List features
 * 
 * @author apodhrad
 * 
 */
@Mojo(name = "list", defaultPhase = LifecyclePhase.PACKAGE)
public class Lister extends AbstractMojo {

	public static final String ECLIPSE_DEFAULT_VERSION = "jee-juno-SR2";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private BuildPluginManager manager;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	@Parameter(alias = "eclipse.version")
	private String eclipseVersion;

	@SuppressWarnings("rawtypes")
	@Parameter(defaultValue = "${project.remoteArtifactRepositories}")
	private java.util.List remoteRepositories;

	@Parameter
	private Set<String> features;

	public void execute() throws MojoExecutionException {
		Eclipse eclipse = installEclipse();

		// Add features
		for (Object obj : remoteRepositories) {
			if (obj instanceof MavenArtifactRepository) {
				MavenArtifactRepository repo = (MavenArtifactRepository) obj;
				if (repo.getLayout().getId().equals("update-site")) {
					getLog().info(
							"Added update site " + repo.getId() + " at "
									+ repo.getUrl());
					eclipse.addUpdateSite(repo.getUrl());
				}
			}
		}
		// List features
		eclipse.listFeatures();
		getLog().info("Finished");
	}

	private Eclipse installEclipse() throws MojoExecutionException {
		DownloadPlugin downloadPlugin = new DownloadPlugin(project, session,
				manager);
		downloadPlugin.download(getEclipseUrl(), isWindowsPlatform(),
				getEclipseInstaller());
		if (!isWindowsPlatform()) {
			untar(getEclipseInstaller());
		}
		return new Eclipse(target + "/eclipse");
	}

	private String getEclipseUrl() {
		String url = "http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release";
		String[] version = getEclipseVersion().split("-");
		url += "/" + version[1] + "/" + version[2] + "/"
				+ getEclipseInstaller();
		return url + "&r=1";
	}

	private String getEclipseVersion() {
		if (eclipseVersion == null) {
			return ECLIPSE_DEFAULT_VERSION;
		}
		return eclipseVersion;
	}

	private String getEclipseInstaller() {
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

		return "eclipse-" + getEclipseVersion() + "-" + platform + "."
				+ archive;
	}

	private boolean isWindowsPlatform() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	private void untar(String tarFile) {
		String command = "tar xvf " + target + "/" + tarFile + " -C " + target;
		getLog().info(command);
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
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
			throw new RuntimeException("Exception during executing '" + command
					+ "'");
		}
		new File(target + "/" + tarFile).delete();
	}
}
