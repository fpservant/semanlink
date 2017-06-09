/* Created on 7 mars 2011 */
package net.semanlink.util;

import java.util.HashSet;
import java.util.Locale;

/** cf ISO 630 2 car langs */
public class IsoLanguages {
private HashSet<String> langs;
public IsoLanguages() {
	String[] s = Locale.getISOLanguages();
	langs = new HashSet<String>(s.length);
	for(String lang : s) {
		langs.add(lang);
	}
}
public boolean exists(String lang) { return langs.contains(lang) ; }
}
