package org.jboss.apodhrad.jbds.plugin.finder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.hamcrest.Matcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class Finder {

	private Log log;

	public String getLink(String url, Matcher<String> matcher) throws IOException {
		List<String> list = getLinks(url, matcher);
		Collections.sort(list, new VersionComparator());
		if (list.isEmpty()) {
			throw new RuntimeException("No link found at "+url+" with matcher");
		}
		return list.get(0);
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public List<String> getLinks(String url, Matcher<String> matcher) throws IOException {
		List<String> list = new ArrayList<String>();
		Document doc = Jsoup.connect(url).get();
		Elements links = doc.select("a[href]");
		for (Element href : links) {
			String link = href.text();
			if (link.length() == 0) {
				continue;
			}
			char c = link.charAt(link.length() - 1);
			if (c == '/') {
				link = link.substring(0, link.length() - 1);
			}
			if (matcher.matches(link)) {
				debug("Found link " + link);
				list.add(link);
			}
		}
		return list;
	}

	private void debug(String msg) {
		if (log != null) {
			log.debug(msg);
		}
	}

	public abstract String getUrl(String version) throws IOException;
}
