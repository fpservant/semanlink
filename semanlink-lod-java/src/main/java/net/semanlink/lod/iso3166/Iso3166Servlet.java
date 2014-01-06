/* Created on Oct 15, 2007 */
package net.semanlink.lod.iso3166;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.semanlink.lod.Jsp_RDF2HTMLPage;
import net.semanlink.lod.LODServlet;
import net.semanlink.lod.LODDataset;
import net.semanlink.lod.SimpleLODDataset;
import net.semanlink.util.jena.JenaUtils;
import net.semanlink.util.jena.RDFServletWriterUtil;
import net.semanlink.util.servlet.BasicServlet;
import net.semanlink.util.servlet.Jsp_Page;

// ce commentaire vient de RDCServlet, je pense, et je ne crois pasinitdalable ici
// *** ATTENTION ***, DOIT ETRE DEFINIE POUR ETRE LOADEE AVANT EXECUTION (web.xml, <load-on-startup>1</load-on-startup>)

/**
 * Servlet publishing the content of one rdf file containing the iso 3166 norm about country codes.
 * 
 * This is here about publishing # URIs, which is quiet different from the / URIs of LODServlet.
 * 
 * Assumes that <ul>
 * <li>the namespace for the countries is NS (see init) (the content of the rdf file
 * can be written as relative to NS)</li>
 * <li>the namespace for the schema is SCHEMA_NS</li>
 * <li>the file is written in n3</li>
 * <li>the file is [RDFFolder]iso3166/countries.n3 ([RDFFolder] being defined in the deployment file)</li>
 * <li>the schema is [RDFFolder]iso3166/schema/schema.rdf</li>
 * <li>BEWARE, the namespace is also in rdf_parsing.js, find ISO_SH</li>
 * </ul>
 * 
 */
public class Iso3166Servlet extends LODServlet {
public static String NS; //  = "http://sicg.tpz.renault.fr/sw/2008/09/iso3166#";
public static String SCHEMA_NS; // = "http://sicg.tpz.renault.fr/sw/2008/09/iso3166-schema#";
public static String PREFIX; // = "pays";
public static String SCHEMA_PREFIX; // = "iso";

// private static String COUNTRIES_SHORT_FILENAME = "countries.rdf";
private static String COUNTRIES_SHORT_FILENAME = "countries.n3";
private static String SCHEMA_SHORT_FILENAME = "schema/schema.rdf";

private File countriesRDFFile;
private File schemaRDFFile;

/** The folder inside the web app folder that contains the JSP. */ // TODO a mettre dans BasicServlet. Accès d'ailleurs ? ds contexte ?
protected String JSPFolder = "/jsp/iso3166/";

public void init() throws ServletException {
	// setTemplateJSP("/jsp/iso3166/template.jsp");
	String s = this.getInitParameter("WebAppURL");
	if (!s.endsWith("/")) s += "/";
	xmlBase = s + "iso3166"; // webAppURL + iso3166
	NS = s + "iso3166#"; // webAppURL + iso3166#
	SCHEMA_NS = s + "iso3166-schema#";
	PREFIX = "pays";
	SCHEMA_PREFIX = "iso";
	super.init();
}

/*
// this is not OK because the loaded rdf in ".rdf" -> the res inside are iso3166.rdf#FR
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	String servletPath =  req.getServletPath();
	printRequestInfo(req);
	if ("/iso3166".equals(servletPath)) {
  	AcceptHeader acceptHeader = new AcceptHeader(req);
  	String pathInfo = req.getPathInfo();
  	if (acceptHeader.prefersRDF()) {
  		// we just have to return the rdf file
  		CopyFiles.writeFile2ServletResponse(countriesRDFFile, res);
  	} else { // redirect to html
  		// sends to the html containing the js to build it from rdf
  		String uri = req.getRequestURL().toString();
  		
  		Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri + ".rdf");
  		req.setAttribute("jsp", jsp);
  		RequestDispatcher requestDispatcher = req.getRequestDispatcher(jsp.getJSP());
  		requestDispatcher.forward(req, res);
  	}
  	return;
  } else if ("/iso3166.rdf".equals(servletPath)) {
		// we just have to return the rdf file
		CopyFiles.writeFile2ServletResponse(countriesRDFFile, res);
		return;
  } else if ("/iso3166.html".equals(servletPath)) {
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, uri.length()-5);
		
		Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri + ".rdf");
		req.setAttribute("jsp", jsp);
		RequestDispatcher requestDispatcher = req.getRequestDispatcher(jsp.getJSP());
		requestDispatcher.forward(req, res);
		return;
	}
	super.doGet(req, res);
}
*/

/**
 * Not possible to have content negociation for servletPath /iso3166 redirecting to /iso3166.rdf,
 * because that would imply URIs such as ../iso3166.rdf#FR in the returned file
 */
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	String servletPath =  req.getServletPath();
	// printRequestInfo(req);
	if ("/iso3166".equals(servletPath)) {
		String pathInfo = req.getPathInfo();
		if ("/sparql/".equals(pathInfo)) { // sparql query
			if (null == req.getParameter("query")) { // TO DO A VIRER cas accès à la page de saisie
				// go to sparql GUI page
			  forward2Jsp(req, res, sparqlGUI(req, res));
			  return;
			} else {
				doGetSparql(req, res);
				return;
			}
	
		} else if ("/".equals(pathInfo)) {
			// go to home page
		  forward2Jsp(req, res, homePage(req, res));
		  return;
			
		} else { // pathInfo null or any other
			// System.out.println("pathinfo : " + pathInfo);
			
  		// we just have to return the rdf file
			// -- in RDF/XML
  		// CopyFiles.writeFile2ServletResponse(countriesRDFFile, res); // this only works when the file is RDF/XML
  		// To take care of the case when the file is in n3:
			// we have to convert it to n3, because that's what expect the javascript parser
			// TODO : the parser doesn't supprot n3 ???
			// It would be better to serve a static file !!!
			// String xmlBase = net.semanlink.util.Util.getContextURL(req) + servletPath;
			// String xmlBase = "http://sicg.tpz.renault.fr/sw/2008/09/iso3166";
			RDFServletWriterUtil.writeRDF(((Iso3166LODDataset) dataset).getModel(),res, xmlBase , null, null); // "RDF/XML-ABBREV"
			
			
			// ESSAYER LE xmlns blanc // to search ce terme
			
  		return; 
		}

	} else if ("/iso3166.n3".equals(servletPath)) {
		// we just have to return the n3 file
		res.setContentType("text/rdf+n3; charset=UTF-8");
		BasicServlet.writeFile2ServletResponse(countriesRDFFile, res); // this only works when the file is RDF/XML
		return;

	} else if ("/iso3166.html".equals(servletPath)) {
		// forward to html page that builds itself loading rdf in javascript
  	// It would be better to serve a static file!
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, uri.length()-5);
		
		//Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri);
		Jsp_RDF2HTMLPage jsp = new Jsp_RDF2HTMLPage(req, res, "centercontent", uri, null, true);
		forward2Jsp(req, res, jsp);
		return;

	} else if ("/iso3166-schema".equals(servletPath)) {
		// we just have to return the rdf schema file
		res.setContentType("application/rdf+xml; charset=UTF-8");
		BasicServlet.writeFile2ServletResponse(schemaRDFFile, res); // this only works when the file is RDF/XML
		return;
		
	} else if ("/iso3166-schema.html".equals(servletPath)) {
		// forward to html page that builds itself loading rdf in javascript
  	// It would be better to serve a static file!
		String uri = req.getRequestURL().toString();
		uri = uri.substring(0, uri.length()-5);
		
		//Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri);
		Jsp_RDF2HTMLPage jsp = new Jsp_RDF2HTMLPage(req, res, "centercontent", uri, null, true);
		forward2Jsp(req, res, jsp);
		return;

	/*} else if ("/iso3166.n3".equals(servletPath)) {
		Model schemod = */
	}
	super.doGet(req, res);
}


//
// Look of pages related
// To have a template different from default one
//

protected void forward2Jsp(HttpServletRequest req, HttpServletResponse res, Jsp_Page jsp) throws ServletException, IOException {
	// to customize the look of the page (to have a template different from default one).
	jsp.setTopBoxJsp(JSPFolder + "topBox.jsp");
	jsp.setRightBoxJsp(JSPFolder + "rightBox.jsp");	
	jsp.setLeftBoxJsp(null);
	super.forward2Jsp(req, res, jsp);
}

//
// ABSTRACT METHODS OF LODServlet
//

/**
 * BEWARE to parameter base: used to read the rdf files (cf jena) iff (?) there is no xml:base inside the file
 */
public LODDataset initDataset() {
	String rdfFolder = getInitParameter("RDFFolder");
	if (rdfFolder == null) throw new RuntimeException("Init parameters RDFFile and RDFFolder undefined.");
	/* String base = getInitParameter("base");
	if ((base != null) && ("".equals(base))) base = null;
	if (base != null) {
		if (base.endsWith("/")) base = base + "iso3166";
		else base = base + "/iso3166";
	} */
	String base = xmlBase;
	
	try {
		String fol = rdfFolder;
		if (fol.endsWith("/")) fol = fol + "iso3166/";
		else fol = fol + "/iso3166/";
		Model model = ModelFactory.createDefaultModel();
		JenaUtils.loadModel(new File(fol), model, base, false);
		this.dataset = new Iso3166LODDataset(model, base);
		((SimpleLODDataset) this.dataset).getModel().setNsPrefix(PREFIX, NS);
		((SimpleLODDataset) this.dataset).getModel().setNsPrefix(SCHEMA_PREFIX, SCHEMA_NS);
		this.countriesRDFFile = new File(fol + COUNTRIES_SHORT_FILENAME);
		this.schemaRDFFile = new File(fol + SCHEMA_SHORT_FILENAME);

		// Util.printModel(dataset.getModel());
		return this.dataset;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}


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

/**
protected SPARQLEndPoint getIso3166SparqlEndPoint() {
	if (this.iso3166SparqlEndPoint == null) {
		DataSource dataSource = DatasetFactory.create(dataset.getModel());
		// dataSource.addNamedModel(arg0, arg1);
		this.iso3166SparqlEndPoint = new SPARQLEndPoint(dataSource);
	}
	return this.iso3166SparqlEndPoint;
}
*/


protected String sparqlGUIPageName() { return "/jsp/iso3166/sparql.jsp"; }

//
// HOME
//

protected Jsp_Page homePage (HttpServletRequest req, HttpServletResponse res) {
	Jsp_Page x = new Jsp_Page(req, res);
	x.setTitle("ISO3166 intro");
	x.setCenterBoxJsp("/jsp/iso3166/home.jsp");
	x.setMoreHeadersJsp("/jsp/jsRDFParserHeaders.jsp");
	return x;
}


}

