package net.semanlink.metadataextraction;

import java.util.Locale;

class BBC extends Extractor {
	public boolean dealWith(ExtractorData data) {
		String uri = data.getUri();
		if (uri.startsWith("https://www.bbc.com")) return true;
		return false;
	}
	
	public String getSource(ExtractorData data) {
		return "BBC";
	}
	
//	// elle est en aaaa/mm/jj ds le texte
//	public String getDateParution(ExtractorData data) {
//		String t = data.getText();
//		String marc = "Published: 20";
//		int n = t.indexOf(marc);
//		if (n < 0) return null;
//		String x = t.substring(n+11, n+21);
//		return x.substring(0,4) + DATE_DELIM + x.substring(5,7) + DATE_DELIM + x.substring(8);
//	}

	public Locale getLocale(ExtractorData data) throws Exception {
		return Locale.UK;
	}
} // class BBC
