package org.jboss.apodhrad.jbds.plugin.matcher;

import java.util.Comparator;

public class JBDSComparator implements Comparator<String> {

	public int compare(String s1, String s2) {
		return s1.compareTo(s2);
	}

}
