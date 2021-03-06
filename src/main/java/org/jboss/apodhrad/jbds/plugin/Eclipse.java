package org.jboss.apodhrad.jbds.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jboss.apodhrad.jbds.plugin.matcher.FileNameStartsWith;
import org.jboss.apodhrad.jbds.plugin.util.FileSearch;

/**
 * 
 * @author apodhrad
 * 
 */
public class Eclipse {

	private static final String LAUNCHER_PREFIX = "org.eclipse.equinox.launcher_";
	
	private File jarFile;
	private Set<String> updateSites;

	public Eclipse(String path) {
		this(new File(path));
	}

	public Eclipse(File file) {
		List<File> launchers = null;
		if (file.isDirectory()) {
			launchers = new FileSearch().find(file, new FileNameStartsWith(LAUNCHER_PREFIX));
			if (launchers.isEmpty()) {
				throw new RuntimeException("Cannot find any eclipse structure in '" + file.getAbsolutePath() + "'");
			}
			for (File launcher: launchers) {
				if (isEclipseStructure(launcher)) {
					file = launcher;
				}
			}
		}
		if (!isEclipseStructure(file)) {
			throw new RuntimeException("Cannot find any eclipse structure in '" + file.getAbsolutePath() + "'");
		}
		this.jarFile = file;
		this.updateSites = new HashSet<String>();
	}

	private static boolean isEclipseStructure(File launcher) {
		if (launcher.isDirectory() || !launcher.exists()) {
			return false;
		}
		if (!launcher.getName().startsWith(LAUNCHER_PREFIX)) {
			return false;
		}
		File pluginsFolder = launcher.getParentFile();
		if (pluginsFolder == null || !pluginsFolder.getName().equals("plugins")) {
			return false;
		}
		File featuresFolder = new File(pluginsFolder.getParentFile(), "features");
		if (featuresFolder == null || !featuresFolder.exists()) {
			return false;
		}
		return true;
	}
	
	public void addUpdateSite(String updateSite) {
		updateSites.add(updateSite);
	}

	public void listFeatures() {
		for (String updateSite : updateSites) {
			System.out.println("Update Site: " + updateSite);
			listFeatures(updateSite);
		}
	}

	public void listFeatures(String updateSite) {
		List<String> command = new ArrayList<String>();
		command.add("-application");
		command.add("org.eclipse.equinox.p2.director");
		command.add("-consoleLog");
		command.add("-followReferences");
		command.add("-nosplash");
		command.add("-noExit");
		command.add("-repository");
		command.add(updateSite);
		command.add("-list");

		execute(command);
	}

	public void installFeature(String feature) {
		List<String> command = new ArrayList<String>();
		command.add("-application");
		command.add("org.eclipse.equinox.p2.director");
		command.add("-consoleLog");
		command.add("-followReferences");
		command.add("-nosplash");
		command.add("-noExit");
		command.add("-repository");
		command.add(collectionToString(updateSites));
		command.add("-installIUs");
		command.add(feature);

		execute(command);
	}

	public void installFeatures(String... features) {
		installFeature(arrayToString(features));
	}

	public void installFeatures(Collection<String> features) {
		installFeature(collectionToString(features));
	}

	public void runBot(String product, String pluginName, String className, String target) {
		File securityFile = new File(jarFile.getParentFile().getAbsolutePath() + "/password");
		FileWriter out = null;
		try {
			out = new FileWriter(securityFile.getAbsoluteFile());
			out.write("master");
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> command = new ArrayList<String>();
		command.add("-application");
		command.add("org.eclipse.swtbot.eclipse.junit.headless.swtbottestapplication");
		command.add("-product");
		command.add(product);
		command.add("-testApplication");
		command.add(pluginName);
		command.add("-testPluginName");
		command.add(pluginName);
		command.add("-className");
		command.add(className);
		command.add("-data");
		command.add(target + "/workspace");
		command.add("formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter," + target + "/" + className + ".xml");
		command.add("formatter=org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter");
		command.add("-consoleLog");
		command.add("-nosplash");
		command.add("-noExit");
		if(securityFile.exists()) {
			command.add("-eclipse.password");
			command.add(securityFile.getAbsolutePath());
		}
		command.add("-vmargs");
		command.add("-Dusage_reporting_enabled=false");
		
		execute(command);
	}
	
	public void execute(List<String> command) {
		execute(command.toArray(new String[command.size()]));
	}

	public void execute(String[] command) {
		int result = 1;
		try {
			ClassLoader cl = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, null);
			Class<?> clazz = cl.loadClass("org.eclipse.equinox.launcher.Main");
			Method main = clazz.getMethod("run", String[].class);
			Object obj = main.invoke(clazz.newInstance(), new Object[] { command });
			result = (Integer) obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occured during command execution");
		}
		if (result != 0) {
			throw new RuntimeException("Execution failed [result=" + result + "]");
		}
	}

	public static File findLauncher(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			throw new RuntimeException(path + " doesn't exist");
		}
		File pluginDir = null;
		File[] homeDir = dir.listFiles();
		for (int i = 0; i < homeDir.length; i++) {
			if (homeDir[i].getName().equals("plugins") && homeDir[i].isDirectory()) {
				pluginDir = homeDir[i];
				break;
			}
		}
		if (pluginDir == null) {
			throw new RuntimeException("Plugins dir not found");
		}
		File jarFile = null;
		File[] pluginsDir = pluginDir.listFiles();
		for (int i = 0; i < pluginsDir.length; i++) {
			if (pluginsDir[i].getName().startsWith("org.eclipse.equinox.launcher_")) {
				jarFile = pluginsDir[i];
				break;
			}
		}
		return jarFile;
	}

	private static String collectionToString(Collection<String> collection) {
		if (collection.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String item : collection) {
			sb.append(",").append(item);
		}
		return sb.substring(1);
	}

	private static String arrayToString(String[] array) {
		if (array.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(",").append(array[i]);
		}
		return sb.substring(1);
	}
	
	public void addProgramArgument(String... args) {
		File iniFile = getIniFile();
		List<String> originalLines = null;
		try {
			originalLines = FileUtils.readLines(iniFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read from '" + iniFile.getAbsolutePath() + "'", e);
		}
		
		List<String> revisedLines = new ArrayList<String>();
		for (String line: originalLines) {
			if (line.startsWith("-vmargs") || line.startsWith("--launcher.appendVmargs")) {
				for (String arg: args) {
					revisedLines.add(arg);
				}
				args = new String[] {};
			}
			revisedLines.add(line);
		}
		
		try {
			FileUtils.writeLines(iniFile, revisedLines);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write to '" + iniFile.getAbsolutePath() + "'", e);
		}
	}
	
	public void addVMArgument(String... args) {
		File iniFile = getIniFile();
		List<String> originalLines = null;
		try {
			originalLines = FileUtils.readLines(iniFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read from '" + iniFile.getAbsolutePath() + "'", e);
		}
		
		List<String> revisedLines = new ArrayList<String>(originalLines);
		for (String arg: args) {
			revisedLines.add(arg);
		}
		
		try {
			FileUtils.writeLines(iniFile, revisedLines);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write to '" + iniFile.getAbsolutePath() + "'", e);
		}
	}
	
	public File getIniFile() {
		File eclipseDir = jarFile.getParentFile().getParentFile();
		for (File file: eclipseDir.listFiles()) {
			if (file.getName().endsWith(".ini")) {
				return file;
			}
		}
		for (File dir: eclipseDir.listFiles()) {
			if (dir.getName().endsWith(".app") && dir.isDirectory()) {
				for (File file: new File(dir, "Contents/MacOS").listFiles()) {
					if (file.getName().endsWith(".ini")) {
						return file;
					}
				}
			}
		}
		throw new RuntimeException("Cannot find .ini file at '" + eclipseDir.getAbsolutePath() + "'");
	}

}
