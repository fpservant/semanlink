package net.semanlink.lod;

import net.semanlink.util.servlet.Jsp_Page;

// BUILT FROM Jsp_RDF2HTMLPage
/**
 * How to include in a JSP a div whose content is made downloading some RDF?
 */
public class RDFIntoDiv {
protected Jsp_Page jsp;
protected String divId;
/** the URL the RDF can be downloaded from. Supposed to be on this server. */
protected String rdfUrl;
/** Main resource to be displayed, if one res is the main subject of the display. 
 *  if it is null, if there are in the RDF resources ?x such as
 *  ?x rdfs:isDefinedBy rdfUrl
 *  these ?x will be displayed, and only them,
 *  unless the attribute displayAllResInList is set to true. */
protected String mainResUri;
protected boolean displayAllResInList = false;

protected String lodHTTPProxyURL;

/**
 * @param jsp the page we want to add this capacity
 * @param rdfUrl the URL the RDF can be downloaded from. Supposed to be on this server.
 * @param mainResUri Main resource to be displayed, if one res in the RDF located at rdfUrl is the focus of the display. 
 * If mainResUri is null, if there are in the RDF resources ?x such as:
 * ?x rdfs:isDefinedBy rdfUrl
 * these ?x will be displayed, and only them,
 * unless the attribute displayAllResInList is set to true.
 * @param displayAllResInList true to display all res in the RDF
 */
public RDFIntoDiv(Jsp_Page jsp, String divId, String rdfUrl, String mainResUri, boolean displayAllResInList) {
	this.jsp = jsp;
	this.divId = divId;
	this.rdfUrl = rdfUrl;
	this.mainResUri = mainResUri;
	this.displayAllResInList = displayAllResInList;
	// More headers than those included by default in template.jsp are needed to call the js rdf parser.
	// And we need to pass this instance to the JSP in order for it to be able to compute things it needs
	this.jsp.getRequest().setAttribute(RDFIntoDiv.class.getName(), this);
	this.jsp.addMoreHeadersJsp("/jsp/jsRDFParserHeaders.jsp");
	this.jsp.addOnLoadEvents(downloadRDFScriptName());
	//
}

/*
public String lodHTTPProxyURL(HttpServletRequest req) throws MalformedURLException {
	String s = BasicServlet.getContextURL(req);
	
}
*/

//
//
//



//
//
//

private String downloadRDFScriptName() {
	return "downloadRDF2" + divId;
}

/*
	function downloadRDF() {
		<%=onLoadScript%>;
	}
 */
/** js methods to be inserted in the page
 * - method to downlaod the RDF and insert it into the div, called by the onload event handler
 * - method called for the links inside the html generated */
public String downloadRDFJavascript() {
	StringBuilder sb = new StringBuilder();
	// function downloadRDF2centercontent() {
	sb.append("function ");
	sb.append(downloadRDFScriptName());
	sb.append("() { ");
	sb.append(loadRDFScript());
	sb.append(" ; }\n");
	
	//
	sb.append(linkToRdfJavascriptMethod());
	sb.append(linkToHtmlJavascriptMethod());

	return sb.toString();
}

/** javascript method called by a link, in the generated HTML, to RDF (outside of dataset) */
protected String linkToRdfJavascriptMethod() {
	return "function lod_linkToRdf(uri) { return getContextURL() + \"/getrdf/?uri=\" + encodeURIComponent(uri); }\n";
}

/** javascript method called by a link, in the generated HTML, to RDF (outside of dataset) */
protected String linkToHtmlJavascriptMethod() {
	return "function lod_linkToHtml(uri) { return getContextURL() + \"/htmlget/?uri=\" + encodeURIComponent(uri); }\n";
}

/**
 * The script that actually downloads the rdf
 * @return
 */
private String loadRDFScript() {
	if (this.mainResUri != null) {
		return "doIt('" + this.rdfUrl  + "' , '" + this.divId + "', '" + this.mainResUri + "', " + displayAllResInList + ")";
	} else {
		return "doIt('" + this.rdfUrl  + "' , '" + this.divId + "', '', " + displayAllResInList + ")";
	}
}

public String getTitle() {
	if (mainResUri != null) return "HTML Page for: " + mainResUri;
	return "HTML Page generated from RDF at: " + rdfUrl;
}

}
