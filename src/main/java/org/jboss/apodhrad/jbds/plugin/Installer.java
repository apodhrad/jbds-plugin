package org.jboss.apodhrad.jbds.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
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
import org.codehaus.plexus.util.FileUtils;
import org.jboss.apodhrad.jbds.plugin.matcher.IsJavaExecutable;
import org.jboss.apodhrad.jbds.plugin.util.FileSearch;

/**
 * 
 * Installs JBDS / Eclipse
 * 
 * @author apodhrad
 * 
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.PACKAGE)
public class Installer extends AbstractMojo {

	public static final String ECLIPSE_DEFAULT_VERSION = "jee-juno-SR2";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Component
	private BuildPluginManager manager;

	@Parameter(defaultValue = "${project.build.directory}")
	private String target;

	@Parameter(alias = "jbds.installer")
	private URL jbdsInstaller;

	@Parameter(alias = "eclipse.version")
	private String eclipseVersion;

	@SuppressWarnings("rawtypes")
	@Parameter(defaultValue = "${project.remoteArtifactRepositories}")
	private java.util.List remoteRepositories;

	@Parameter
	private Set<String> features;

	@Parameter(alias = "jre.location")
	private String jreLocation;
	
	public void execute() throws MojoExecutionException {
		Eclipse eclipse = null;
		if (jbdsInstaller != null && jbdsInstaller.toString().length() > 0) {
			eclipse = installJbds();
		} else {
			eclipse = installEclipse();
		}

		// Install features
		for (Object obj : remoteRepositories) {
			if (obj instanceof MavenArtifactRepository) {
				MavenArtifactRepository repo = (MavenArtifactRepository) obj;
				if (repo.getLayout().getId().equals("update-site")) {
					getLog().info("Added update site " + repo.getId() + " at " + repo.getUrl());
					eclipse.addUpdateSite(repo.getUrl());
				}
			}
		}
		if (features != null && !features.isEmpty()) {
			eclipse.installFeatures(features);
		}
		getLog().info("Finished");
	}

	private Eclipse installJbds() throws MojoExecutionException {
		// Download JBDS
		String url = jbdsInstaller.toString();
		String jarFile = getInstallerJar(jbdsInstaller);
		new DownloadPlugin(project, session, manager).download(url, false);

		// Install JBDS
		String installationFile = null;
		try {
			installationFile = createInstallationFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException("Exception occured during creating installation file");
		}

		String command = "java -jar " + target + "/" + jarFile + " " + installationFile;
		getLog().info("Exec: " + command);
		Process proc;
		try {
			proc = Runtime.getRuntime().exec(command);
			// Then retrieve the process output
			InputStream in = proc.getInputStream();
			BufferedReader br = null;
			String line = null;
			br = new BufferedReader(new InputStreamReader(in));
			while ((line = br.readLine()) != null) {
				getLog().info(line);
			}
			InputStream err = proc.getErrorStream();
			br = new BufferedReader(new InputStreamReader(err));
			while ((line = br.readLine()) != null) {
				getLog().error(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("Exec Exception");
		}
		return new Eclipse(target + "/jbdevstudio/studio");
	}

	private String createInstallationFile() throws IOException, MojoExecutionException {
		String jre = getJreLocation();
		if(jre == null) {
			throw new MojoExecutionException("Cannot find JRE location!");
		}
		
		String dest = target + "/jbdevstudio";

		String tempFile = target + "/install.xml";
		String targetFile = target + "/installation.xml";

		URL url = getClass().getResource("/install.xml");

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
		return targetFile;
	}

	private String getInstallerJar(URL url) {
		String[] parse = url.toString().split("/");
		for (int i = 0; i < parse.length; i++) {
			if (parse[i].endsWith(".jar")) {
				return parse[i];
			}
		}
		throw new RuntimeException("Couldn't found jar file in '" + url + "'");
	}

	private Eclipse installEclipse() throws MojoExecutionException {
		DownloadPlugin downloadPlugin = new DownloadPlugin(project, session, manager);
		downloadPlugin.download(getEclipseUrl(), isWindowsPlatform(), getEclipseInstaller());
		if (!isWindowsPlatform()) {
			String eclipseInstaller = getEclipseInstaller();
			ExecPlugin execPlugin = new ExecPlugin(project, session, manager);
			getLog().info("tar xvf " + eclipseInstaller);
			execPlugin.tar("xvf", eclipseInstaller);
		}
		return new Eclipse(target + "/eclipse");
	}

	private String getEclipseUrl() {
		String url = "http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release";
		String[] version = getEclipseVersion().split("-");
		url += "/" + version[1] + "/" + version[2] + "/" + getEclipseInstaller();
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

		return "eclipse-" + getEclipseVersion() + "-" + platform + "." + archive;
	}

	private boolean isWindowsPlatform() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}
	
	private String getJreLocation() {
		String jreLoc = null;
		
		// find jre location from java home
		String javaHome = System.getProperty("java.home");
		getLog().info("JRE: " + javaHome);
		FileSearch fileSearch = new FileSearch();
		fileSearch.find(javaHome, new IsJavaExecutable());
		List<File> result = fileSearch.getResult();
		if(!result.isEmpty()) {
			jreLoc = result.get(0).getAbsolutePath();
		}
		
		// find jre location from plugin configuration
		if(jreLocation != null && jreLocation.length() > 0) {
			jreLoc = jreLocation;
		}
		
		return jreLoc;
	}

}
