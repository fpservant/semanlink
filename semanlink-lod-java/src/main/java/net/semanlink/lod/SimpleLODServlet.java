/* Created on 15 janv. 2009 */
package net.semanlink.lod;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import net.semanlink.util.jena.JenaUtils;
import net.semanlink.util.servlet.Jsp_Page;

/** A simple implementation of LODServlet that reads its data from files. */
public class SimpleLODServlet extends LODServlet {
private static final String LINKED_DATA = "linkeddata";
/** The folder inside the web app folder that contains the JSP. */ // TODO a mettre dans BasicServlet. Accès d'ailleurs ? ds contexte ?
protected String JSPFolder = "/jsp/simplelodservlet/";

/*
 * BEWARE to parameter base: used to read the rdf files (cf jena) iff (?) there is no xml:base inside the file
 */
	
/** A simple servlet with a sparql endpoint (path /sparql) and a linked data set (path /LINKED_DATA) */
@Override public LODDataset initDataset(HttpServletRequest req) {
	String rdfFolder = getInitParameter("RDFFolder");
	if (rdfFolder == null) throw new RuntimeException("Init parameter RDFFolder undefined.");
	String base = getInitParameter("base");
	if ((base != null) && ("".equals(base))) base = null;
	if (base == null) {
		ServletContext servletContext = getServletContext();
		String webAppURL = servletContext.getInitParameter("WebAppURL");
		if ((webAppURL == null) || ("".equals(webAppURL))) throw new RuntimeException("InitParameter WebAppURL not defined");
			String s = webAppURL;
			if (!s.endsWith("/")) s += "/";
			base = s + LINKED_DATA + "/";
	}
	try {
		Model model = ModelFactory.createDefaultModel();
		JenaUtils.loadModel(new File(rdfFolder), model, base, false);
		SimpleLODDataset x = new SimpleLODDataset(model, base);
		return x;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

protected boolean isRequestForDataSet(HttpServletRequest req) {
	return (("/" + LINKED_DATA).equals(req.getServletPath()));
}

//
//HOME
//

protected Jsp_Page homePage (HttpServletRequest req, HttpServletResponse res) {
	Jsp_Page x = new Jsp_Page(req, res);
	x.setLeftBoxJsp(null);
	x.setTitle("Simple Linked Data Servlet");
	x.setCenterBoxJsp(JSPFolder + "sparql.jsp");
	x.setMoreHeadersJsp("/jsp/jsRDFParserHeaders.jsp");
	return x;
}

//
//Look of pages related
//To have a template for iso3166 different from LOD template
//

protected void forward2Jsp(HttpServletRequest req, HttpServletResponse res, Jsp_Page jsp) throws ServletException, IOException {
	// to customize the look of the page (to have a template for iso3166 different from LOD template).
	jsp.setTopBoxJsp(JSPFolder + "topBox.jsp");
	jsp.setRightBoxJsp(JSPFolder + "rightBox.jsp");	
	super.forward2Jsp(req, res, jsp);
}

//
//
//

protected String sparqlGUIPageName() { return JSPFolder + "sparql.jsp"; }

/** req supposed to contain a sparql query to be returned as html, return the uri of the (rdf) sparql query. 
 * THIS IS EXACTLY THE SAME THING AS IN LODServlet,
 * but when it is executed by LODservlet, (both being in the same web app)
 * req.getRequestURL() is different. STRANGE!!!!
 * @throws MalformedURLException 
 * @throws UnsupportedEncodingException */
protected String getSparqlQueryUri(HttpServletRequest req) throws MalformedURLException, UnsupportedEncodingException {
	// String uri = net.semanlink.util.Util.getContextURL(req) + "/sparql/?query=";
	String uri = req.getRequestURL().toString() + "?query=";
	String q = req.getParameter("query"); 
	// @find Tomcat "feature" wrt uri encoding
	// q has been decoded as if it is ISO-8859-1
	q = java.net.URLEncoder.encode(q, "ISO-8859-1");
	return uri + q;
	// return uri + java.net.URLEncoder.encode(q, "UTF-8"); // getParameter unencode: we have to encode again
}

}
