/* Created on 2012-1-3
 * Trying to solve ClassNotFoundException httpclient with tomcat 7 */
package net.semanlink.lod;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//// import org.apache.commons.httpclient.HttpException;

import com.hp.hpl.jena.rdf.model.Model;

import net.semanlink.util.AcceptHeader;
import net.semanlink.util.CopyFiles;
///// import net.semanlink.util.SimpleHttpClient;
import net.semanlink.util.jena.RDFServletWriterUtil;
import net.semanlink.util.servlet.Jsp_Page;

abstract public class LODServletNoHttpClient extends SparqlServlet {

//
// ATTRIBUTES
//
	
protected LODDataset dataset;
///** use getter! */
//private URIDereferencer uriDereferencer; // @find generic uri dereferencing


//
// INIT
//

// abstract public LODDataset initDataset(LODServlet lodServlet);
abstract public LODDataset initDataset();
public LODDataset getLODDataset() {
	if (this.dataset == null) this.dataset = initDataset();
	return this.dataset;
}

public void init() throws ServletException {
	super.init();
	dataset = initDataset();
	//getServletContext().setAttribute("LODServletPaths", dataset)
}

//
//
//

// UNUSED -- WHY? SEEMED A GOOD IDEA!

public class LODServletPaths {
	/** javascript method called by a link, in the generated HTML, to RDF (outside of dataset) */
	protected String linkToRdfJavascriptMethod() {
		return "function lod_linkToRdf(uri) { return getContextURL() + \"/getrdf/?uri=\" + encodeURIComponent(uri); }\n";
	}

	/** javascript method called by a link, in the generated HTML, to RDF (outside of dataset) */
	protected String linkToHtmlJavascriptMethod() {
		return "function lod_linkToRdf(uri) { return getContextURL() + \"/htmlget/?uri=\" + encodeURIComponent(uri); }\n";
	}

	public String getRDFServletPath() { return "/getrdf"; }
	public String getHTMLServletPath() { return "/htmlget"; }
}

//
//
//

/* 2010-07 : pour faire focntionner les liens ds le download dans semanlink (pour lequel je dois, sur kattare, utiliser sl/htmlget (par ex))
 * je dois faire un truc pour que ds rdfparsing.js, on appelle une méthode donnée (plus compliquée que getContextURL() pour créer ces liens.
 * C'est bien un truc lié à la servlet (cf d'ailleurs le doget) : et en fct de ce truc, il faut mettre une méthode js qui va bien ds la page
 */
/*
String htmlGet_jsScript() {
	StringBuilder sb = new StringBuilder();
	// function lod_linkToRdf(uri) { return getContextURL() + "/getrdf/?uri=" + encodeURIComponent(uri); }

	return "function lod_linkToRdf(uri) { return getContextURL() + " "/getrdf/?uri=" + encodeURIComponent(uri); }

}
*/
//
//
//

@Override
protected SPARQLEndPoint initSparqlEndPoint() { return getLODDataset().getSPARQLEndPoint() ; }
protected SPARQLUpdateEndPoint initSparqUpdatelEndPoint() { return null ; }

//
//
//

protected Jsp_Page releaseNotesPage(HttpServletRequest req, HttpServletResponse res) {
	Jsp_Page x = new Jsp_Page(req, res);
	x.setCenterBoxJsp(releaseNotesPage());
	return x;
}

protected String releaseNotesPage() { return "/jsp/releaseNotes.jsp"; }

protected Jsp_Page apiPage(HttpServletRequest req, HttpServletResponse res) {
	Jsp_Page x = new Jsp_Page(req, res);
	x.setCenterBoxJsp(apiPage());
	return x;
}

// actually, the jsp doesn't exist yet
protected String apiPage() { return "/jsp/api.jsp"; }


//
// DOGET
//

// TODO : changer les ../x/uri=[encoded uri] par ../x/uri (vérifier si ":" est autorisé dans une uri http) (au moins pour les uri http)

// TODO : gestion des exceptions

public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	// We must first sort between URIs belonging to the dataset, and those who don't (such as "/home" ...)
	// In case we are here serving several datasets, we could have
	// something such as getDataset(uri) (returning null in case uri doesn't belong to any of the datasets)
	// OR we could deal first with URIs that don't belong to the dataset. Pb: need to call
	// some form of super.xxx for paths that are handled by BasicServlet
	// OR deal with that directly in the web.xml, using several servlets (including one BasicSerlet
	// for paths such as "/home").
	// That's what we were doing here BUT WE STOPPED, we now uses dataset.owns
	// The method dataset.owns is indeed necessary, because we have to handle the case where the uri to be dereferenced
	// is passed as an "uri" argument to a "/get?uri=..."-like request: in this case,
	// we must be able to decide whether uri belongs to this dataset or not

	String servletPath =  req.getServletPath();
	
	String uri = null;
	
	// There is a difference between a get for a resource of this dataset, and a request /get?uri=...
	// For the first kind: we must conform to http-range14
	// while the second is a request for an information resource, with a representation as either rdf or html
	// (or at least, could be considered as such. But we can also prefer to have the redirection,
	// in order to display smarter uri, in the case of uri in this dataset) 
	// SO, what ?
	
	// /get and /getAsItReallyIs ? (le 1re permettant par ex, de displayer du rdf en html de la façon que nous on veut)
	
	// BTW: what must return a HTTP HEAD in the case of a NIR? hmm, same return code, i presume
	
	
	//
	// SPARQL Servlet
	//
	
	// v1.0.5
	if (servletPath.endsWith("sparql")) {
		super.doGet(req, res);
		return;
	}

	//
	//
	//
	
	if ("/home".equals(servletPath)) { // @find welcome-file-list (in web.xml)
		super.doGet(req, res);
		return;
	}
	
	if ("/releasenotes".equals(servletPath)) {
  	forward2Jsp(req, res, releaseNotesPage(req, res));
  	return;
	}
	
	if ("/api".equals(servletPath)) {
  	forward2Jsp(req, res, apiPage(req, res));		
  	return;
	}
	
	//
	// LOD STUFF
	//
	
	// I only test the end of the servletPath, in order to be able to use, for instance "/sl/rdf2html/*" in the web.xml servlet mappings
	
	// if ("/getrdf".equals(servletPath)) {
	if (servletPath.endsWith("/getrdf")) {
    // dereferencing the uri of some rdf NO : OUTSIDE RDF // TODO fixme
    httpGetRDF(req, res);
    return;
	}
		
	// if ("/get".equals(servletPath)) { // /get?uri=...
	if (servletPath.endsWith("/get")) { // /get?uri=...
		// <!--  @find generic uri dereferencing -->
		// request for uri to be dereferenced
		// Beware, if it is served by this servlet, we must not try to connect to it by http

		// Should we 303?
		// TODO: we are not homogeneous here regarding the 303
		// - if outside uri, no 303,
		// (Here, the SimpleHttpClient has followRedirect true.
		// I think we should have a way to decide whether we followRedirect or not)
		// - if uri in this dataset: we document the uri and handle it in the same way
		// as a dereference of uri

		uri = req.getParameter("uri");
		if (uri == null) throw new RuntimeException("Asked to dereference a URI, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
		
		if (!this.dataset.owns(uri)) {
			throw new RuntimeException("Not supported link"); // @TODO fixme
//			URIDereferencer uriDeref = getURIDereferencer();
//			SimpleHttpClient httpClient = uriDeref.getSimpleHttpClient();
//
//			// This returns exactly what the distant server returns.
//			// TODO: handle exceptions (for instance 404 generates a RuntimeException!)
//			httpClient.output(uri, req, res); // Hmm: rechercher ce même code plus loin. Quid des liens relatifs ds du html ?
//			return;
			
		} else { // this.dataset.owns(uri)
			// uri to be dereferenced. 
		}
	
	// } else 	if ("/htmlget".equals(servletPath)) { // used for links to resource in html displaying rdf
	} else 	if (servletPath.endsWith("/htmlget")) { // used for links to resource in html displaying rdf
		/*// 2010-12
		// NOTE : this is costly: we make a HEAD request to get the content type, (possibly with followredirect) and
		// if it is rdf, the javascript "onload" will make the actual request for the rdf
		
		// This is a request made by html generated from rdf:
		// (the user clicked a link in a web application displaying html generated from rdf)
		
		// In this situation, we must continue, as far as possible,
		// to generate html from rdf.
		// That is: if it is possible to get rdf from the uri to be dereferenced,
		// then we must generate html from it (forwarding to the html page containing the js that downloads rdf)
		
		// request for uri to be dereferenced
		// The "uri" parameter contains the uri to be dereferenced.
		// Beware, if it is served by this servlet, we must not try to connect to it by http
		uri = req.getParameter("uri");
		if (uri == null) throw new RuntimeException("Asked to dereference a URI, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
		
		if (!this.dataset.owns(uri)) {
			clickedLinkToOutsideResourceInHTMLDisplayingRDF(req, res, uri);
			return;

			
		} else {
			// uri belongs to this dataset: continue
			// As of 2008/12, this never happens: the js only uses htmlget with uris outside the context of this webapp
			// (TODO : should be improved)
			// (mouais, c'est pas vrai: cas des url en euro5)
		}*/
		clickedLinkToResourceInHTMLDisplayingRDF(req, res);
	
	} else {
		// we probably can safely assume that this is a request for a URI of the dataset
		// except for path such as "home"
		uri = req.getRequestURL().toString(); // http://127.0.0.1:9080/semanlink/tag/%CE%91%E1%BC%B4%CE%B1%CF%82.html
		if (!dataset.owns(uri)) {
			super.doGet(req, res);
			return;
		}
	}
	
	// uri belongs to this.dataset
	if (uri != null) {
		getLocalURI(req, res, uri);
		return;
	}
	
	super.doGet(req, res);
}

//
//
//

// 2010/12
protected void clickedLinkToResourceInHTMLDisplayingRDF(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	//used for links to resource in html displaying rdf
	
	// NOTE : this is costly: we make a HEAD request to get the content type, (possibly with followredirect) and
	// if it is rdf, the javascript "onload" will make the actual request for the rdf
	
	// This is a request made by html generated from rdf:
	// (the user clicked a link in a web application displaying html generated from rdf)
	
	// In this situation, we must continue, as far as possible,
	// to generate html from rdf.
	// That is: if it is possible to get rdf from the uri to be dereferenced,
	// then we must generate html from it (forwarding to the html page containing the js that downloads rdf)
	
	// request for uri to be dereferenced
	// The "uri" parameter contains the uri to be dereferenced.
	// Beware, if it is served by this servlet, we must not try to connect to it by http
	String uri = req.getParameter("uri");
	if (uri == null) throw new RuntimeException("Asked to dereference a URI, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
	
	if (!this.dataset.owns(uri)) {
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

//
//
//

/**
 * The standard case: deref of an uri of this dataset (either NIR, .rdf or .html)
 * @param uri supposed to belong to this dataset.
 * @throws ServletException 
 * @throws IOException 
 */
protected void getLocalURI(HttpServletRequest req, HttpServletResponse res, String uri) throws IOException, ServletException {

	boolean isNir = dataset.isNonInformationResource(uri);
	if (isNir) {
		// return a 303. Redirect to html or rdf depending on HTTP accept header
		String nirUri = uri;
  	AcceptHeader acceptHeader = new AcceptHeader(req);
  	String redirectURI = null;
  	if (acceptHeader.prefersRDF()) {
  		redirectURI = dataset.nir2rdfURI(nirUri);
  	} else { // redirect to html
  		redirectURI = dataset.nir2htmlURI(nirUri);
  	}
   	// res.sendRedirect is 302, not 303
  	res.setStatus(303);
  	res.setHeader("Location", res.encodeRedirectURL(redirectURI));

  	return;
	} // if isNir
	
	// not a NIR
	
	// .rdf corresponding to a NIR?
	
	String nirUri = dataset.rdf2nirURI(uri);
	if (nirUri != null) { // request for an rdf resource corresponding to a NIR of the dataset.
		// uri is the uri of the rdf about nir
		Model rdfMod = dataset.getRDFAboutNIR(uri, nirUri);
		RDFServletWriterUtil.writeRDF(rdfMod, res);
		
		return;
	}
	
	// .html corresponding to a NIR?
	nirUri = dataset.html2nirURI(uri);
	if (nirUri != null) { // request for an html page corresponding to a NIR of the dataset.
		Jsp_NIRPage jsp = new Jsp_NIRPage(req, res, dataset, nirUri);
	  forward2Jsp(req, res, jsp);
		return;
	}
	
	// remains case of an IR, that does not correspond to a NIR

	// on va supposer que uri retourne du RDF
	// (cas du deref du schema à partir de la boite "deref une uri") // @HACK schema owl
	// forward to html page that builds itself loading rdf in javascript // POURQUOI DIABLE ?
	// ne devrait-on pas retourner ce qu'on nous a demandé ??? !!! Quid du cas schema owl : n'est-ce pas parce qu'en fait,
	// il n'appartient pas au dataset ?
	// BREF, ne sont-ce pas que des artefacts dus au fait qu'on gère mal
	// ce qui est dirigé vers cette servlet ? (trop laxistes en termes de servletmapping,
	// qui devrait sans ambiguité envoyer vers une nir du dataset)
	// et/ou ambiguité sur les IR : ou est-ce qu'on dit qu'on en créait dans notre dataset ?
	// et / ou le fait qu'on arrive ici avec une uri qui en fait a été passée via un getxxx?uri =
	// (il doit quand meêm rester des 404 : deref d'un truc conforme en terme d'uri pour ce dataset, mais qui n'existe pas)
	Jsp_RDF2HTMLPage jsp = new Jsp_RDF2HTMLPage(req, res, div4rdf(), uri, null, true);
  forward2Jsp(req, res, jsp);
}

//
//
//

/**
 * The user clicked on a link to an outside resource from an HTML page displaying RDF. 
 * 
 * If the resource is an outside NIR (that respects LOD principles, that is, dereferences as rdf or html),
 * we could just redirect to it.
 * But here, we want to display the content with our own stuff: if uri can return RDF, we want to get it
 * and display it with our own tool to generate HTML.
 * 
 * (Note also that uri can be a link to a RDF IR -- a RDF file)
 * 
 * @param uri of a resource not belonging to this dataset
 * @throws IOException 
 * @throws HttpException 
 * @throws ServletException 
 */
//// protected void clickedLinkToOutsideResourceInHTMLDisplayingRDF(HttpServletRequest req, HttpServletResponse res, String uri) throws HttpException, IOException, ServletException {
protected void clickedLinkToOutsideResourceInHTMLDisplayingRDF(HttpServletRequest req, HttpServletResponse res, String uri) throws IOException, ServletException {
 throw new RuntimeException("NOT SUPPORTED");
//	// - if uri dereferences (possibly after redirection) as rdf,
//	// we want to display this rdf in html, that is, using the javascript onload of a	Jsp_RDF2HTMLPageNEW
//	// - if uri doesn't return rdf (even after possible redirection), we return the content as it is
//	// (but we should say that, if rdf is not available, we prefer html)
//	// -> as we don't know, from uri, that it dereferences as rdf or not 
//	// we will make a head request (with followredirect true)
//	// (possible optim: if it ends with ".rdf" admit that it dereferences as rdf?)
//	
//	// prepare a head request saying that we prefer rdf, accept html nevertheless
//	// with follow redirect set to true
//	URIDereferencer uriDeref = getURIDereferencer();
//	SimpleHttpClient httpClient = uriDeref.getSimpleHttpClient();
//	// TODO? REMETTRE
//	// String acceptHeader = "text/html;q=0.5, application/rdf+xml";
//	String acceptHeader = "application/rdf+xml";
//	String contentType = null;
//	try {
//		contentType = httpClient.getContentType(uri, acceptHeader, true);
//	} catch (Exception e) {
//		e.printStackTrace();
//		// System.out.println("TRYING AGAIN");
//		// try {
//			// Response r = httpClient.doGet(uri, acceptHeader);
//			// System.out.println("TRIED contentype:"+r.getContentType());
//		/*} catch (Exception e2) {
//			e2.printStackTrace();
//			contentType = "rdf"; // PB AVEC RealWorldServlet, comprends pas. A voir TODO
//		}*/
//	}
//	// System.out.println("htmlget " + uri + " content type: " + contentType);
//
//	if ( ((contentType != null) && (contentType.contains("rdf"))) 
//		|| (uri.endsWith(".rdf")) || (uri.endsWith(".owl")) ){
//		// forward to html page that builds itself loading rdf in javascript
//		// Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri); // // @HACK schema owl A REVOIR !!!
//		// System.out.println("NOT RETESTED !!!!!");
//		
//		// We have to display HTML from the RDF located at uri
//		// -> return the page that displays HTML from RDF, making it load uri.
//		// uri being not in this dataset, the download must go through the proxy 
//		// ICI URI n'est pas ds ce dataset -> faire envoyer directement au proxy avec ordre de retourner du rdf
//		String rdfUrl = rdfUrlThroughProxy(Util.getContextURL(req), uri);
//		/*// was B4 2010-12
//		Jsp_RDF2HTMLPage jsp = new Jsp_RDF2HTMLPage(req, res, div4rdf(), rdfUrl, null, true); // n'y-a-t-il pas une mainres à passer, au moins ds certains cas ?
//		forward2Jsp(req, res, jsp); */
//	  // 2010-12
//	  /*Jsp_Page jsp = newJsp_Page(req,res);
//		RDFIntoDiv rdfIntoDiv = new RDFIntoDiv(jsp, div4rdf(), rdfUrl, null, true); // cela ajoute à this page la capacité de télécharger le rdf
//		// if (mainResUri != null) this.title = "HTML Page for: " + mainResUri;
//		jsp.setTitle("HTML Page generated from RDF at: " + rdfUrl);
//		forward2Jsp(req, res, jsp);*/
//		
//		// 2010-12-30
//		forward2HTMLForOneResource(req, res, uri, rdfUrl); 
//	  
//	} else { // redirect to html
//		// This returns exactly what the distant server returned
//		// BUT it is not OK (cf relative links inside HTML)
//		// httpClient.output(uri, req, res);
//		
//		// we return a redirect to uri
//		res.sendRedirect(uri);
//	}
}

// 2010-12
/** URL of outside RDF through the http proxy 
 * @throws UnsupportedEncodingException */
protected String rdfUrlThroughProxy(String contextURL, String rdfUrl) throws UnsupportedEncodingException {
	return contextURL + "/getrdf/?rdfUrl=" + java.net.URLEncoder.encode(rdfUrl,"UTF-8");
}

// 2010-12
protected Jsp_Page newJsp_Page(HttpServletRequest req, HttpServletResponse res) {
	return new Jsp_Page(req,res);
}

//
//
//

// SEE COMMENT IN rdf_parsing.js function linkToRdf(uri)

/**
 * Dereferencing a URI OUTSIDE THIS NAMESPACE supposed to point to some RDF.
 * Supposes that there is a "uri" parameter which is the address of some RDF served from another server
 * Returns the content. 
 * @throws IOException */ // C'EST DU java.net, POURQUOI PAS HTTPClient ???? (cf USER/MOT DE PASSE)
protected void httpGetRDF(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String uri = req.getParameter("uri");
    if (uri == null) throw new RuntimeException("Ask to dereference an URI, but no uri found in request's parameters"); // @TODO fixme
    // if (!(uri.endsWith(".rdf"))) uri += ".rdf"; // avoids httprange-14 roundtrip 
    URL url = new URL(uri); 
    HttpURLConnection yc = (HttpURLConnection) url.openConnection();
    yc.setRequestProperty("accept","application/rdf+xml");
    yc.setRequestProperty("User-Agent","LODServlet");
    yc.setInstanceFollowRedirects(true);
    yc.setRequestMethod("GET");
    BufferedInputStream in = new BufferedInputStream(yc.getInputStream());
    CopyFiles.writeIn2Out(in, res.getOutputStream(), new byte[res.getBufferSize()]);
    in.close();
}

/*
// TODO: 303. Here, the SimpleHttpClient has followRedirect true.
// I think we should have a way to decide whether we followRedirect or not

// request for uri to be dereferenced
// Beware, if it is served by this servlet, we must not try to connect to it by http
void httpGetRDF(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String uri = req.getParameter("uri");
		if (uri == null) throw new RuntimeException("Asked to dereference uri, but no uri found in parameters (no 'uri' param)"); // @TODO fixme
		if (!this.dataset.owns(uri)) {
			
			URIDereferencer uriDeref = getURIDereferencer();
			SimpleHttpClient httpClient = uriDeref.getSimpleHttpClient();
			
			AcceptHeader acceptHeader = new AcceptHeader(req);
			// parameter "as=rdf" is used by the onload script (loading of rdf from javascript). This is needed, because
			// we dereference the uri from a browser: HTML is prefered, not rdf
			if ( (acceptHeader.prefersRDF()) || ("rdf".equals(req.getParameter("as"))) ) {
				httpClient.output(uri, AcceptHeader.RDF, res);
				return;
			} else { // redirect to html
				// a base de recopié de plus bas
				Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri);
			  req.setAttribute("jsp", jsp);
			  RequestDispatcher requestDispatcher = req.getRequestDispatcher(jsp.getJSP());
			  requestDispatcher.forward(req, res);
				return;
			}
		}
}
*/

//
// @find generic uri dereferencing
//

//private URIDereferencer getURIDereferencer() {
//	if (this.uriDereferencer == null) this.uriDereferencer = createURIDereferencer();
//	return this.uriDereferencer;
//}
//
//private URIDereferencer createURIDereferencer() {
//	String s;
//	boolean useProxy = false;
//	SimpleHttpClient httpc;
//	s = getInitParameter("useProxy");
//	if (s != null) {
//		s = s.trim().toLowerCase();
//		useProxy = Boolean.parseBoolean(s);
//	}
//	if (useProxy) {
//		// We should not have the password in the servlet configuration file // @TODO fixe
//		httpc = new SimpleHttpClient(getInitParameter("proxyHost"), Integer.parseInt(getInitParameter("proxyPort")), getInitParameter("proxyUserName"), getInitParameter("proxyPassword"));
//	} else {
//		httpc = new SimpleHttpClient();
//	}
//
//	s = getInitParameter("userAgent");
//	if (s != null) httpc.setUserAgent(s);
//	return new URIDereferencer(httpc);
//}



/*
 * Moved to SparqlServlet
 * Forwards to the HTML page for a URI that returns rdf
 * 
 * Just prepare the "onload" of the RDF. 
 * @throws IOException 
 * @throws ServletException */
/*
public void forward2HTMLBuiltFromRDF(HttpServletRequest req, HttpServletResponse res, String rdfUri) throws ServletException, IOException {
	// Jsp_RDFPage_JSBased jsp = new Jsp_RDFPage_JSBased(req, res, this, uri, uri);
	Jsp_RDF2HTMLPageNEW jsp = new Jsp_RDF2HTMLPageNEW(req, res, rdfUri, null, true);
	forward2Jsp(req, res, jsp);
}
*/


/**
 * @param resUri uri of Resource
 * @param rdfUrl the url where we get the RDF describing the resource
 * @throws IOException 
 * @throws ServletException 
 * @since 2010-12-30 */
public void forward2HTMLForOneResource(HttpServletRequest req, HttpServletResponse res, String resUri, String rdfUrl) throws ServletException, IOException {
	forward2HTMLBuiltFromRDF(req, res, div4rdf(), rdfUrl, resUri, false);
}


}
