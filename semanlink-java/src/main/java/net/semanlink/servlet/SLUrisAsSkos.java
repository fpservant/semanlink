/* Created on 27 janv. 07 */
package net.semanlink.servlet;

import net.semanlink.skos.SKOS;

public class SLUrisAsSkos extends SLUris {
	private static SLUrisAsSkos self = new SLUrisAsSkos();

	public static SLUrisAsSkos getInstance() { return self; }
	public String getKeywordClassURI() { return SKOS.Concept.getURI(); }
	public String getHasParentPropertyURI() { return SKOS.broader.getURI(); }
	public String getHasFriendPropertyURI() { return SKOS.related.getURI(); }
	public String getKeywordPrefLabelURI() { return SKOS.prefLabel.getURI(); }
}

