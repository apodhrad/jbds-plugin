package org.jboss.apodhrad.jbds.plugin.eclipse;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Eclipse {

	private File jarFile;
	private Set<String> updateSites;

	public Eclipse(String eclipseHome) {
		this(findLauncher(eclipseHome));
	}

	public Eclipse(File jarFile) {
		this.jarFile = jarFile;
		updateSites = new HashSet<String>();
	}

	public void addUpdateSite(String updateSite) {
		updateSites.add(updateSite);
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

	public void execute(List<String> command) {
		execute(command.toArray(new String[command.size()]));
	}

	public void execute(String[] command) {
		try {
			ClassLoader cl = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, null);
			Class<?> clazz = cl.loadClass("org.eclipse.core.launcher.Main");
			Method main = clazz.getMethod("main", String[].class);
			main.invoke(clazz, new Object[] { command });
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occured during command execution");
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
}
