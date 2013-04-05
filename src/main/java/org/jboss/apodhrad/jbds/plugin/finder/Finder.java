package org.jboss.apodhrad.jbds.plugin.finder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class Finder {

	public static String getLink(String url, Matcher<String> matcher) throws IOException {
		List<String> list = getLinks(url, matcher);
		return list.get(0);
	}

	public static List<String> getLinks(String url, Matcher<String> matcher) throws IOException {
		List<String> list = new ArrayList<String>();
		Document doc = Jsoup.connect(url).get();
		Elements links = doc.select("a[href]");
		for (Element href : links) {
			String link = href.text();
			char c = link.charAt(link.length() - 1);
			if (c == '/') {
				link = link.substring(0, link.length() - 1);
			}
			if (matcher.matches(link)) {
				list.add(link);
			}
		}
		return list;
	}

	public abstract String getUrl(String version) throws IOException ;
}
