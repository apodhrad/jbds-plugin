package org.jboss.apodhrad.jbds.plugin;

import static org.hamcrest.core.AllOf.allOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.jboss.apodhrad.jbds.plugin.matcher.EndsWith;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JBDSSearch {

	private static final String JBDS_REPO = "http://machydra.brq.redhat.com/www.qa.jboss.com/binaries/RHDS/builds/development";

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Matcher<String> matcher = allOf(new EndsWith(".installer"));
		System.out.println(getLinks(JBDS_REPO, matcher));
	}

	public void search() {
		
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

	public void getLink() {

	}
}
