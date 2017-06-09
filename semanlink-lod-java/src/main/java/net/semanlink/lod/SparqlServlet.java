/* Created on 26 oct. 07 */
package net.semanlink.lod;
import java.io.*;
import java.net.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;

import net.semanlink.util.AcceptHeader;
import net.semanlink.util.jena.RDFServletWriterUtil;
import net.semanlink.util.servlet.BasicServlet;

abstract public class SparqlServlet extends BasicServlet {

//
// ATTRIBUTES
//

/** use getter! */
protected SPARQLEndPoint sparqlEndPoint;
/** use getter! */
protected SPARQLUpdateEndPoint sparqlUpdateEndPoint;
/** xmlBase to be used by default when writing models to the response.
 *  Can be null.
 *  use getter!
 */
protected String xmlBase;
protected String getXMLBase() { return xmlBase; }

//
// INIT
//

abstract protected SPARQLEndPoint initSparqlEndPoint(HttpServletRequest req);
/** The SPARQLEndPoint used by this servlet. */
protected SPARQLEndPoint getSparqlEndPoint(HttpServletRequest req) {
	if (this.sparqlEndPoint == null) this.sparqlEndPoint = initSparqlEndPoint(req);
	return this.sparqlEndPoint;
}

//
//SPARQL Update
//

abstract protected SPARQLUpdateEndPoint initSparqUpdatelEndPoint();
protected SPARQLUpdateEndPoint getSparqlUpdateEndPoint() {
	if (this.sparqlUpdateEndPoint == null) initSparqUpdatelEndPoint();
	return this.sparqlUpdateEndPoint;
}

//
// POST CF SPARQL UPDATE
//

//ne faut-il pas se préoccuper d'encoding (cf SLServlet.doPost) ?
public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	String servletPath =  req.getServletPath();
	if ("/update".equals(servletPath)) { // /update/?query=...
		getSparqlUpdateEndPoint().exec(req, res);
		// REDIRECT AFTER POST
		String referer = req.getHeader("referer");
		res.sendRedirect(res.encodeRedirectURL(referer));
		return;
	}	
	super.doPost(req, res);
}

//
// DOGET
//

// TODO : changer les ../x/uri=[encoded uri] par ../x/uri (vérifier si ":" est autorisé dans une uri http) (au moins pour les uri http)

// TODO : gestion des exceptions

public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	// this.printRequestInfo(req); // a virer 2012-08
	
	req.setCharacterEncoding("UTF-8");

	String servletPath =  req.getServletPath();
	
	//
	// SPARQL Servlet
	//
	
	// if ("/sparql".equals(servletPath)) { // /sparql/?query=...
	if (servletPath.endsWith("/sparql")) { // /sparql/?query=...
		if (null == req.getParameter("query")) { // cas accès à la page de saisie
			// go to sparql GUI page
		  forward2Jsp(req, res, sparqlGUI(req, res));

		} else {
			doGetSparql(req, res);
		}
	}
}

//
// SPARQL
//


//@TODO branler mieux le rapport avec SPARQLEndpoint
/**
 * Handles a SPARQL request. 
 * req therefore supposed to contain a "query" parameter (else, RuntimeException). 
 * Return the raw result of the query (XML or RDF/XML), except
 * if the query is DESCRIBE or CONSTRUCT, AND there is a "as" parameter with value "html" or "n3".
 */
protected void doGetSparql(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	SPARQLEndPoint endpoint = getSparqlEndPoint(req);
	Query query = endpoint.createQuery(req);
	boolean isRDFQuery = (query.isDescribeType() || (query.isConstructType()));
	
	String as = req.getParameter("as");
	if (as != null) as = as.toLowerCase();
	
	if ("html".equals(as)) { // return html page for a sparql query
		// only RDF answers are handled (no select, no ask at this time)
		if (isRDFQuery) {
			doGetSparqlAsHtml(req, res);
			return;
		}
	}
	
	res.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08

	if (isRDFQuery) {
		Model resultModel = endpoint.getResultModel(query);
		if ("n3".equals(as)) { 
			RDFServletWriterUtil.writeRDF(resultModel,res, getXMLBase() , "N3", null);
		} else if ("jsonld".equals(as)) { // 2012-08 JSON-LD
			res.setContentType(AcceptHeader.JSON_LD);
			RDFServletWriterUtil.writeRDF(resultModel,res, getXMLBase() , "JSON-LD", null); // 2014-10
//		} else if ("rj".equals(as)) { // 2012-08 RDF/JSON TALIS // cf http://dvcs.w3.org/hg/rdf/raw-file/default/rdf-json/index.html
//			res.setContentType(AcceptHeader.RDF_JSON_TALIS);
		} else {
			RDFServletWriterUtil.writeRDF(resultModel,res, getXMLBase() , "RDF/XML", null); // "RDF/XML-ABBREV"
		}
	} else { // !isRDFQuery
		endpoint.exec(query, res);
	}
}

/** id of div the rdf is downloaded into */
public String div4rdf() { return "centercontent" ; }

/**
 * Forwards to the HTML page for a URI that returns rdf
 * 
 * Just prepare the "onload" of the RDF. 
 * @throws IOException 
 * @throws ServletException */ // 2010-12 out
public void forward2HTMLBuiltFromRDF(HttpServletRequest req, HttpServletResponse res, String divId, String rdfUrl, String mainResUri, boolean displayAllResInList) throws ServletException, IOException {
	// Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri);
	Jsp_RDF2HTMLPage jsp = new Jsp_RDF2HTMLPage(req, res, divId, rdfUrl, mainResUri, displayAllResInList);
	forward2Jsp(req, res, jsp);
}
/** 
 * Forwards to the HTML page for a describe or construct SPARQL query. 
 * 
 * Just prepares the "onload" of the RDF. */
protected void doGetSparqlAsHtml(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	// sends to the html containing the js to build it from rdf
	// (we suppose it is a describe or construct query)
	String uri = getSparqlQueryUri(req);
	forward2HTMLBuiltFromRDF(req, res, div4rdf(), uri, null, true);
}

// overriden in Iso3166Servlet with a strange story. Have a look!
// idem SimpleLODServlet (?)
/** req supposed to contain a sparql query to be returned as html, return the uri of the (rdf) sparql query. 
 * @throws MalformedURLException 
 * @throws UnsupportedEncodingException */
protected String getSparqlQueryUri(HttpServletRequest req) throws MalformedURLException, UnsupportedEncodingException {
	// String uri = net.semanlink.util.Util.getContextURL(req) + "/sparql/?query=";
	String uri = req.getRequestURL().toString() + "?query=";
	String q = req.getParameter("query"); 
	return uri + java.net.URLEncoder.encode(q, "UTF-8"); // ??? @todo CHECK THAT: I don't think we need to encode: already encoded // see also in Euro5ServiceServlet
}

/**
 * A GUI to enter sparql queries
 */

protected Jsp_SparqlGui sparqlGUI(HttpServletRequest req, HttpServletResponse res) {
	Jsp_SparqlGui x = new Jsp_SparqlGui(req, res, this.getSparqlEndPoint(req));
	x.setCenterBoxJsp(sparqlGUIPageName());
	return x;
}

protected String sparqlGUIPageName() { return "/jsp/sparql.jsp"; }



}
