package net.semanlink.lod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.util.servlet.Jsp_Page;

// 2010-06 see RDFIntoDiv
/**
 * A Jsp_Page that displays HTML built from some RDF into a given div
 * 
 * @see RDFIntoDiv - the class that actually does the job (this Jsp_Page could well be deprecated, as with RDFIntoDiv we now have the possibility to
 * load RDF from any Jsp_Page)
 */
public class Jsp_RDF2HTMLPage extends Jsp_Page {

/**
 * @param rdfUrl: the URL the RDF can be downloaded from. Supposed to be on this server
 */
public Jsp_RDF2HTMLPage(HttpServletRequest request, HttpServletResponse response, String rdfUrl) {
	this(request, response, "centercontent", rdfUrl, null, false);
}

/**
 * @deprecated use Jsp_RDF2HTMLPage(request, response, "centercontent", rdfUrl, mainResUri, displayAllResInList) instead
 */
public Jsp_RDF2HTMLPage(HttpServletRequest request, HttpServletResponse response, String rdfUrl, String mainResUri, boolean displayAllResInList) {
	this(request, response, "centercontent", rdfUrl, mainResUri, displayAllResInList);
}

/**
 * @param divId id of the div into which the rdf will be displayed
 * @param rdfUrl the URL the RDF can be downloaded from. Supposed to be on this server.
 * @param mainResUri Main resource to be displayed, if one res in the RDF located at rdfUrl is the focus of the display. 
 * If mainResUri is null, if there are in the RDF resources ?x such as:
 * ?x rdfs:isDefinedBy rdfUrl
 * these ?x will be displayed, and only them,
 * unless the attribute displayAllResInList is set to true.
 * @param displayAllResInList true to display all res in the RDF
 */
public Jsp_RDF2HTMLPage(HttpServletRequest request, HttpServletResponse response, String divId, String rdfUrl, String mainResUri, boolean displayAllResInList) {
	super(request, response);
	RDFIntoDiv rdfIntoDiv = new RDFIntoDiv(this, divId, rdfUrl, mainResUri, displayAllResInList); // cela ajoute à this page la capacité de télécharger le rdf

	// if (mainResUri != null) this.title = "HTML Page for: " + mainResUri;
	this.title = "HTML Page generated from RDF at: " + rdfUrl;
}

@Override
public String getCenterBoxJsp() { return null; } // v1.1 // Edouard constate une exception "jsp/centerBox.jsp" ds template.jsp. fps ne la constate pas. Anyway...

}
