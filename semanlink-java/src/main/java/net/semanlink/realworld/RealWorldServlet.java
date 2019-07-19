/* Created on 8 mars 08 */
package net.semanlink.realworld;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.util.AcceptHeader;
import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.servlet.SLServlet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class RealWorldServlet extends HttpServlet {
public static String NS;
public static String SCHEMA_NS = "http://www.semanlink.net/2008/02/realworld#";

public void init() {
	NS = SLServlet.getServletUrl();
	if (!(NS.endsWith("/"))) NS += "/";
	NS += "sl/realworld/";
}

public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	// printRequestInfo(req);
	String uri = req.getParameter("uri");
	// if (uri == null) throw new RuntimeException("Asked to dereference uri, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
	
	String pathInfo = req.getPathInfo();
	if ("/get".equals(pathInfo)) { // /get?uri=...
		// main access: dereferencing the URI of the NIR corresponding to an HTML page, given in the uri param
		// 303 needed 
		String redirectURL = null;
		AcceptHeader acceptHeader = new AcceptHeader(req);
  	// parameter "as=rdf" is used by the onload script (loading of rdf from javascript). This is needed, because
  	// we dereference the uri from a browser: HTML is prefered, not rdf
  	if (acceptHeader.prefersRDF()) {
  		StringBuffer sb = req.getRequestURL(); // everything up to ? excluded
  		// not OK to redirect to:
  		// sb.append("?as=rdf&amp;uri=");
  		// sb.append(encodedURI); // ??? TODO CHECK
  		// redirectURL = sb.toString();
  		// because gives 
  		// org.apache.commons.httpclient.CircularRedirectException: Circular redirect to 'http://127.0.0.1:9080/semanlink/sl/realworld/get'
  		// in HttpClient
  		String s = sb.toString();
  		s = s.substring(0, s.length()-3); // http://.../realworld/
  		String encodedURI = URLUTF8Encoder.encode(uri);
  		redirectURL = s + "rdf?uri=" + encodedURI;
  	} else { // redirect to html
  		redirectURL = uri;
  	}
  	res.setStatus(303);
  	res.setHeader("Location", res.encodeRedirectURL(redirectURL));
  	return;

	} else 	if ("/rdf".equals(pathInfo)) { // /rdf?uri=...
		StringBuffer sb = req.getRequestURL(); // everything up to ? excluded
		String deb = sb.toString();
		deb = deb.substring(0, deb.length()-3); // http://.../realworld/ // même chose que NS !!!
		String encodedURI = URLUTF8Encoder.encode(uri);
		String nirURI = deb + "get?uri=" + encodedURI; // TODO check encode
		String rdfURI = deb + "rdf?uri=" + encodedURI; // TODO check encode
		toRDF(req, res, uri, nirURI, rdfURI);
		return;

	} else {
		// ...realworld/xxx/en.wikipedia.org/wiki/Marco_Polo
		if (pathInfo.startsWith("/rdf/")) { // /rdf/en.wikipedia.org/wiki/Marco_Polo
			String var = pathInfo.substring(5);
			uri = "http://" + var;
			String nirURI = NS + "get/" + var;
			String rdfURI = NS + "rdf/" + var;
			toRDF(req, res, uri, nirURI, rdfURI);
			return;
	    
		} else if (pathInfo.startsWith("/get/")) {
			String var = pathInfo.substring(5);
			uri = "http://" + var;
			String rdfURI = NS + "rdf/" + var;

			// 303 needed 
			String redirectURL = null;
			AcceptHeader acceptHeader = new AcceptHeader(req);
	  	// parameter "as=rdf" is used by the onload script (loading of rdf from javascript). This is needed, because
	  	// we dereference the uri from a browser: HTML is prefered, not rdf
	  	if (acceptHeader.prefersRDF()) {
	  		redirectURL = rdfURI;
	  	} else { // redirect to html
	  		redirectURL = uri;
	  	}
	  	res.setStatus(303);
  		System.out.println("RealWorldServlet REDIRECT TO "  + redirectURL);
	  	res.setHeader("Location", res.encodeRedirectURL(redirectURL));
	  	return;

		} else {
			throw new RuntimeException("unexepcted request");
		}
	}

}

private void toRDF(HttpServletRequest req, HttpServletResponse res, String uri, String nirURI, String rdfURI) throws ServletException, IOException {
	Model model = computeModel( uri, nirURI, rdfURI);
	
	model.setNsPrefix("realw",NS);
	model.setNsPrefix("realws",SCHEMA_NS);
	model.setNsPrefix("rdfs",RDFS.getURI());
	
	req.setAttribute("net.semanlink.servlet.rdf", model);
	RequestDispatcher requestDispatcher = req.getRequestDispatcher("/rdf"); // forward to the RDFServlet
	requestDispatcher.forward(req, res);
}

/**
 * 
 * @param uri url of page
 * @param nirURI
 * @param rdfURI
 * @return
 */
private Model computeModel(String uri, String nirURI, String rdfURI) {
	Model model = ModelFactory.createDefaultModel();

	Resource nirRes = model.createResource(nirURI);
	Resource htmlRes = model.createResource(uri);
	Statement sta;
	Property htmlProp = model.createProperty(SCHEMA_NS + "html");
	sta = model.createStatement(nirRes, htmlProp, htmlRes);
	model.add(sta);
	
	Property rdfProp = model.createProperty(SCHEMA_NS + "rdf");
	Resource rdfRes = model.createResource(rdfURI);
	sta = model.createStatement(nirRes, rdfProp, rdfRes);
	model.add(sta);
	
	// this is the same as rdfProp. We should put in the schema that rdfProp is subProperty of isDefinedByProp
	Property isDefinedByProp = model.createProperty(RDFS.isDefinedBy.getURI());
	sta = model.createStatement(nirRes, isDefinedByProp, rdfRes);
	model.add(sta);
	
	if (uri.startsWith("http://en.wikipedia.org/wiki/")) {
		String dbPediaURI = sparqlDBPedia(uri);
		if (dbPediaURI != null) {
			Property owlSameAsProp = model.createProperty(OWL.sameAs.getURI());
			Resource dbPediaNIR = model.createResource(dbPediaURI);
			sta = model.createStatement(nirRes, owlSameAsProp, dbPediaNIR);
			model.add(sta);
		}
	}

	return model;
}

String sparqlDBPedia(String wikipediaUri) {
	String service = "http://dbpedia.org/sparql";
	// String queryString = "SELECT ?s ?p ?p2 WHERE {{ ?s ?p <http://en.wikipedia.org/wiki/Marco_Polo> } { ?s ?p2 <http://fr.wikipedia.org/wiki/Marco_Polo> }}" ;
	// String queryString = "SELECT ?s ?p WHERE { ?s ?p <http://en.wikipedia.org/wiki/Marco_Polo> }" ;
	String queryString = "SELECT ?s ?p WHERE { ?s ?p <" + wikipediaUri + "> }" ;
	Query query = QueryFactory.create(queryString) ;
	String defaultGraph = "";
	QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query, defaultGraph);
	try {
	  ResultSet results = qexec.execSelect() ;
	  for ( ; results.hasNext() ; ) {
	    QuerySolution soln = results.nextSolution() ;
	    /*
	    RDFNode x = soln.get("varName") ;       // Get a result variable by name.
	    Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
	    Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
	    */
	    RDFNode x = soln.get("s") ;
	    return ((Resource) x).getURI();
	  }
	} catch (Exception e) {
		e.printStackTrace();
	}
	finally { qexec.close() ; }
	return null;
}

	
/*class RealWorldDataset  {
		public Model getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String html2nirURI(String htmlURI) {
		// TODO Auto-generated method stub
		return NS + "get?uri=" + URLUTF8Encoder.encode(htmlURI);
	}
	


	public boolean isNonInformationResource(String uri) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String nir2htmlURI(String nirURI) {
		String x = xx(nirURI);
		if (x == null) return null;
		return URLUTF8Encoder.decode(x);
	}
	
	private String xx(String nirURI) {
		String deb = NS + "get?uri=";
		if (!nirURI.startsWith(deb)) return null;
		return nirURI.substring(deb.length());
	}
	
	public String nir2rdfURI(String nirURI) {
		String x = xx(nirURI);
		if (x == null) return null;
		return NS + "rdf?uri=" + x;
	}
	
	public boolean owns(String uri) {
		if (!uri.startsWith(NS)) return true;
		return false;
	}
	
	public String rdf2nirURI(String rdfURI) {
		throw new RuntimeException("wait 4 nect release");
	}
}*/
}
