package net.semanlink.servlet;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathLib;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import net.semanlink.lod.LODDataset;
import net.semanlink.lod.LODServlet;
import net.semanlink.lod.RDFIntoDiv;
import net.semanlink.lod.SLSparqlEndPoint;
import net.semanlink.lod.SPARQLEndPoint;
import net.semanlink.lod.SPARQLUpdateEndPoint;
import net.semanlink.skos.SKOS;
import net.semanlink.sljena.*;
import net.semanlink.sparql.TagDescribeHandlerFactory;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.ThesaurusLabels;
import net.semanlink.servlet.Jsp_Page;
import net.semanlink.util.index.WordIndexInterface;
import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.jena.TextMatchMagicProp;

public class SLSparqlServlet extends LODServlet {
	
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");

	String pathInfo = req.getPathInfo();
	// BasicServlet.printRequestInfo(req);
	// if ("/sparql".equals(pathInfo)) {
	if (null == pathInfo) {
			// go to sparql page
			Jsp_Page jsp = sparqlPage(req, res);
			req.setAttribute("net.semanlink.servlet.jsp", jsp);
			RequestDispatcher requestDispatcher;
			try {
				requestDispatcher = req.getRequestDispatcher(jsp.getTemplate());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		  requestDispatcher.forward(req, res);
		  return;
	} /*else if ("/sparql/".equals(req.getPathInfo())) {
		getSparqlEndPoint().exec(req, res);
		return;
	}*/

	super.doGet(req, res);
}

protected Jsp_Page sparqlPage(HttpServletRequest req, HttpServletResponse res) {
	Jsp_Page x = new Jsp_Page(req);
	x.setTitle("SPARQL");
	x.setContent("/jsp/sparql.jsp");
	return x;
}

@Override
protected SPARQLUpdateEndPoint initSparqUpdatelEndPoint() {
	return null ;
}

@Override
protected SPARQLEndPoint initSparqlEndPoint(HttpServletRequest req) {
	return SLServlet.getSLSparqlEndPoint();
}
@Override
public void forward2HTMLBuiltFromRDF(HttpServletRequest req, HttpServletResponse res, String divId, String rdfUrl, String mainResUri, boolean displayAllResInList) throws ServletException, IOException {
	Jsp_Page jsp = new Jsp_Page(req, res);
	@SuppressWarnings("unused")
	RDFIntoDiv rdfIntoDiv = new SLRDFIntoDiv(jsp, divId, rdfUrl, mainResUri, displayAllResInList); // don't remove (this gives the capacity to the page
	jsp.setTitle("SPARQL");
	jsp.setContent("/jsp/sparql.jsp");
  req.setAttribute("net.semanlink.servlet.jsp", jsp);
	forward2Jsp(req, res, jsp);}

@Override
public String div4rdf() { return "sparqlresults" ; }

// 2010-12
protected Jsp_Page newJsp_Page(HttpServletRequest req, HttpServletResponse res) {
	return new Jsp_Page(req,res);
}


/**
 * pour ajouter le "/sl" (nécessaire pour le focntionnement sur kattare)
 */
class SLRDFIntoDiv extends RDFIntoDiv {
	public SLRDFIntoDiv(Jsp_Page jsp, String divId, String rdfUrl,String mainResUri, boolean displayAllResInList) {
		super(jsp, divId, rdfUrl, mainResUri, displayAllResInList);
	}
	
	protected String linkToRdfJavascriptMethod() {
		return "function lod_linkToRdf(uri) { return getContextURL() + \"/sl/getrdf/?uri=\" + encodeURIComponent(uri); }\n";
	}

	protected String linkToHtmlJavascriptMethod() {
		// this is OK, but wanting to have directly smarted url in links, we could test if this is the uri of a tag
		// (what would avoid to have to override clickedLinkToResourceInHTMLDisplayingRDF to go to the tag in sl)
		return "function lod_linkToHtml(uri) { return getContextURL() + \"/sl/htmlget/?uri=\" + encodeURIComponent(uri); }\n";
	}
	
	/*
	@Override public String downloadRDFJavascript() {
		String x = super.downloadRDFJavascript();
		x = x + "\n";
		x = x + "if (!TYPE2METHOD) TYPE2METHOD = new Array(); \n";
		x = x + 
	}
	*/

}

@Override
public LODDataset initDataset(HttpServletRequest req) {
	SPARQLEndPoint endpoint = getSparqlEndPoint(req);
	
	return new SLLODDataset(endpoint, SLServlet.getServletUrl());
}

//
//
//

// 2010-12
// cf pb in sparql: we have in html generated from rdf in js www.semanlink.net/tag... instead of 127...
@Override
protected void clickedLinkToResourceInHTMLDisplayingRDF(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	String uri = req.getParameter("uri");
	if (uri == null) throw new RuntimeException("Asked to dereference a URI, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
	
	SLModel mod = SLServlet.getSLModel();
	if (mod.kwExists(uri)) {
		SLKeyword kw = mod.getKeyword(uri);
		CoolUriServlet.goTag(uri, req, res);
		return;
	}
	
	// bof bof pour la suite
	
	LODDataset ds = getLODDataset(req);
	if (!ds.owns(uri)) {
		clickedLinkToOutsideResourceInHTMLDisplayingRDF(req, res, uri);
		return;
		
	} else {
		// As of 2008/12, this never happens: the js only uses htmlget with uris outside the context of this webapp
		// (TODO : should be improved)
		// (mouais, c'est pas vrai: cas des url en euro5)
		getLocalURI(req, res, uri);
		return;
	}
}

// 2010-12
// cf pb in tree generated from rdf in js www.semanlink.net/tag... instead of 127...
@Override
/**
 * Dereferencing a URI OUTSIDE THIS NAMESPACE supposed to point to some RDF.
 * Supposes that there is a "uri" parameter which is the address of some RDF served from another server
 * Returns the content. 
 * @throws IOException */ // C'EST DU java.net, POURQUOI PAS HTTPClient ???? (cf USER/MOT DE PASSE)
protected void httpGetRDF(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String uri = req.getParameter("uri");
    if (uri == null) throw new RuntimeException("Ask to dereference an URI, but no uri found in request's parameters"); // @TODO fixme
  	SLModel mod = SLServlet.getSLModel();
  	if (mod.kwExists(uri)) {
  		// copied from CoolUriServlet
  		SLKeyword kw = mod.getKeyword(uri);
     	// res.setStatus(303);
    	// System.out.println("      303-redirect to " + response.encodeRedirectURL(HTML_Link.getTagURL(Util.getContextURL(request), kwuri, false, dotExtension)));
    	// res.setHeader("Location", res.encodeRedirectURL(HTML_Link.getTagURL(Util.getContextURL(req), uri, false, ".rdf")));
  		
	  	Jsp_Keyword jsp = new Jsp_Keyword(kw, req);
	    req.setAttribute("net.semanlink.servlet.jsp", jsp);
	    // if (iContentType == 0) { // html
	    // } else if (iContentType == 1) { // rdf
	    	/* Rdf_Keyword rdf = new Rdf_Keyword(kw, SLServlet.getSLModel(), Util.getContextURL(request));
				Model rdfMod = rdf.getRDF();*/
	    Model rdfMod = null;
	    // un truc spécial pour mon 127, parce que par défaut on concertit le contenu du rdf pour avoir des tags en 127
	    // mais là, on a besoin que les tags du rdf retourné soit bien en semanlink.net/tag, pas 127/tag
	    try { rdfMod = jsp.getRawRDF("rdf"); } catch (Exception e) { throw new RuntimeException (e); }
			req.setAttribute("net.semanlink.servlet.rdf", rdfMod);
			RequestDispatcher disp = req.getRequestDispatcher("/rdf"); // forward to the RDFServlet
			try { disp.forward(req, res); } catch (Exception e) { throw new RuntimeException (e); }
  		return;
  	}
  	super.httpGetRDF(req, res);
}


//2010-12
/** URL of outside RDF through the http proxy 
 * @throws UnsupportedEncodingException */
protected String rdfUrlThroughProxy(String contextURL, String rdfUrl) throws UnsupportedEncodingException {
	return contextURL + "/sl/getrdf/?uri=" + java.net.URLEncoder.encode(rdfUrl,"UTF-8");
}

}
