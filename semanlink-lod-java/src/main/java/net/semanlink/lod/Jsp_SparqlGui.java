/* Created on 24 nov. 2008 */
package net.semanlink.lod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.util.servlet.Jsp_Page;

/** "SandBox" SPARQL page */
public class Jsp_SparqlGui extends Jsp_Page {
private SPARQLEndPoint endPoint;

public Jsp_SparqlGui(HttpServletRequest req, HttpServletResponse res, SPARQLEndPoint endPoint) {
	super(req, res);
	//
	this.endPoint = endPoint;
	//
	setTitle("SPARQL GUI");
	setMoreHeadersJsp("/jsp/jsRDFParserHeaders.jsp");
}

public SPARQLEndPoint getSPARQLEndPoint() { return this.endPoint; }
}


