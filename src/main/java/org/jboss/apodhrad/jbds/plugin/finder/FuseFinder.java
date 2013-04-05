package org.jboss.apodhrad.jbds.plugin.finder;

import static org.hamcrest.core.AllOf.allOf;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.jboss.apodhrad.jbds.plugin.matcher.IsVersion;
import org.jboss.apodhrad.jbds.plugin.matcher.StartsWith;

public class FuseFinder extends Finder {

	private static final String REPO = "http://repo.fusesource.com/beta/rcp";

	public String getUrl(String version) throws IOException {
		Matcher<String> matcher = allOf(new IsVersion(), new StartsWith(version));
		return getLink(REPO, new StartsWith(version));
	}

}
