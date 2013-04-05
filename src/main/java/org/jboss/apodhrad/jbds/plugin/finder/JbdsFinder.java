package org.jboss.apodhrad.jbds.plugin.finder;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.jboss.apodhrad.jbds.plugin.matcher.EndsWith;
import org.jboss.apodhrad.jbds.plugin.matcher.StartsWith;

import static org.hamcrest.core.AllOf.allOf;

public class JbdsFinder extends Finder {

	private static final String REPO = "http://machydra.brq.redhat.com/www.qa.jboss.com/binaries/RHDS/builds/development";

	public String getUrl(String version) throws IOException {
		Matcher<String> matcher = allOf(new EndsWith(".installer"), new StartsWith(version));
		return getLink(REPO, matcher);
	}

}
