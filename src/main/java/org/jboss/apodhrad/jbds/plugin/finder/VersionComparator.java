package org.jboss.apodhrad.jbds.plugin.finder;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

	public int compare(String s1, String s2) {
		return -s1.compareTo(s2);
	}

}
