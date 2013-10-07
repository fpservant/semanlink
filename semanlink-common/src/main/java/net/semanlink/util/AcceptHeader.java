/* Created on 24 oct. 07 */
package net.semanlink.util;
import javax.servlet.http.HttpServletRequest;

/** Used to deal with MIME types. 
 *  Should be improved : RDF will be returned iff it is in the acceptHeader.
 *  (where could I find the code to do that correctly? see Richard Cyganiak)
 *  Nothing about N3 there. // TODO
 */
public class AcceptHeader {
public static String RDF_XML = "application/rdf+xml";
public static String RDF_N3 = "text/rdf+n3";
public static String JSON_LD = "application/ld+json"; // 2012-08 JSON-LD // 2012-08 RDF/JSON TALIS
public static String RDF_JSON_TALIS = "application/rdf+json"; // 2012-08 RDF/JSON TALIS
private String acceptHeader;

public AcceptHeader(HttpServletRequest req) {
	this.acceptHeader = req.getHeader("accept");
}

/** Intended to return true if return as RDF-XML is prefered, but in fact only checks that it is accepted. */
public boolean prefersRDF() {
	return acceptsRDF();
}

public boolean acceptsRDF() { 
	if (this.acceptHeader == null) return false;
	// TODO !!! THIS IS NOT GOOD !!!
	// if (this.acceptHeader.startsWith(RDF)) return true;
	if (this.acceptHeader.contains(RDF_XML)) return true;
	if (this.acceptHeader.contains(RDF_N3)) return true;
	return false;
}

public boolean accepts(String what) {
	if (this.acceptHeader == null) return false;
	if (this.acceptHeader.indexOf(what) > -1) return true;
	return false;
}
}

