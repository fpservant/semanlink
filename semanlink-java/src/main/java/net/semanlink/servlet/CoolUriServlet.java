/* Created on 27 avr. 2005 */
package net.semanlink.servlet;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;

import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLDocumentStuff;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLLabeledResource;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLRuntimeException;
import net.semanlink.semanlink.SLThesaurus;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.semanlink.WebServer;
import net.semanlink.skos.SKOS;
import net.semanlink.util.AcceptHeader;
import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.util.Util;
import net.semanlink.util.servlet.BasicServlet;

public class CoolUriServlet extends HttpServlet {

// 2025-01 : pff, ne gère pas correctement les retours en cas d'erreur
// (cf. pb d'appels prenant des uri invalides et qui ont généré des masses de logs)

// on a le même pour les 2 pour cause de kattare. Ce qui distingue entre les 2, ce sont les arguments 
// 2019-03 HUM, j'espère qu'en fait, on n'a pas besoin des 2
	
public static final String DOC_SERVLET_PATH = "/doc"; // celui pour des requêtes genre doc/?docuri=xxx
/** @since 2017-09 */
public static final String DOC_SERVLET_PATH2017 = "/sl/doc"; // celui pour des requêtes genre sl/doc/2017/01/toto

public static final String NOTE_SERVLET_PATH_KATTARE = "/doc";
public static final String NOTE_SERVLET_PATH = "/note";
// public static final String MD_SERVLET_PATH = "/md";
// public static final String ABOUT_SERVLET_PATH = "/about";
public static final String ABOUT_SERVLET_PATH = "/sl/about";
public static final String TAG_SERVLET_PATH = "/tag";

public static final String AND_QUERY_PARAM = "and"; // 2020-02 TagAndTag


public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	// res.setContentType("text/html; charset=UTF-8  "); // ceci ne semble pas nécessaire // commented 2007-01 VOIRE NUISIBLE: si c'est autre chose qu'on veut retourner
	doGetOrPost(req, res);
	//SLServlet.debugWeakHashMap();
}
public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	// res.setContentType("text/html; charset=UTF-8  "); // ceci ne semble pas nécessaire // commented 2007-01 VOIRE NUISIBLE: si c'est autre chose qu'on veut retourner
	SLServlet.handlePostParams(req);
	doGetOrPost(req, res);
	//SLServlet.debugWeakHashMap();
}


/** 
 * With URIs containing encoded characters, req.getPathInfo() is not correct (at least, it's not
 * what we want) (tested with Tomcat 5.5). Hence we cannot use it to get, almost directly, the tag from the URL.
 * 
 * clic on http://127.0.0.1:8080/semanlink/tag/afrique?resolvealias=true
 * req.getContextPath(): /semanlink
 * req.getPathInfo(): /afrique
 * req.getPathTranslated(): /Users/fps/Semanlink/webapp/afrique
 * req.getQueryString(): resolvealias=true
 * req.getRequestURI(): /semanlink/tag/afrique
 * req.getResource(/): jndi:/localhost/semanlink/
 * req.getRequestURL(): http://127.0.0.1:8080/semanlink/tag/afrique
 * req.getServletPath(): /tag
 * net.semanlink.util.Util.getContextURL(req): http://127.0.0.1:8080/semanlink
 * req.getParameterNames() : 
 * 
 * clic on tag Λεωνίδας from the search box
 * URL of tag is: http://127.0.0.1:9080/semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82
 * (and that's what we have in a href)
 * We see in safari's nav box: http://127.0.0.1:9080/semanlink/tag/Λεωνίδας.html?resolvealias=true
 * req.getContextPath(): /semanlink
 * req.getPathInfo(): /Î?ÎµÏ?Î?Î¯Î´Î±Ï?.html
 * req.getPathTranslated(): /Users/fps/_fps/-JavaDev/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/webapps/semanlink/Î?ÎµÏ?Î?Î¯Î´Î±Ï?.html
 * req.getQueryString: resolvealias=true
 * req.getRequestURI(): /semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * req.getRequestURL(): http://127.0.0.1:9080/semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * req.getServletPath(): /tag
 * net.semanlink.util.Util.getContextURL(req): http://127.0.0.1:9080/semanlink
 * Parameters:
 * 	resolvealias : true
 * 
 * 
 */
static void printRequestInfo(HttpServletRequest req) throws MalformedURLException {
	BasicServlet.printRequestInfo(req);
}

private static void logInvalidUri(String uri, URISyntaxException e, HttpServletRequest req) {
	String referer = BasicServlet.getReferer(req);
	/*
	if (uri.length() > 100) {
		uri = uri.substring(0, 99) + "...";
	}
	System.err.println("Invalid URI in " + req.getRequestURL() + " ; referer: "+referer+ " ; uri: " + uri + " ; " + e.toString() + "\n");
	*/
	System.err.println("URISyntaxException in " + req.getRequestURL() + " ; referer: "+referer+ " ; " + e.toString() + "\n");
}

private void doGetOrPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { // 2025-01
	try {
		doGetOrPost_impl(req,res);
	} catch (SLRuntimeException e) {
		Throwable cause = e.getOrigine();
		boolean done = false;
		if (cause != null) {
			if (cause instanceof URISyntaxException) {
				res.setStatus(400);
				// res.getWriter().write("go f*ck yourself with your invalid uri");
				res.getWriter().write("There something wrong with this uri! " + cause.toString());
				done = true;
			}
		}
		if (!done) {
			res.setStatus(e.toHttpErrorCode());
			res.getWriter().write("something went wrong");
		}
	} catch(Exception e) {
		res.setStatus(500);
		res.getWriter().write("something unexpected happened");		
	}
	
}

// ne faut-il pas se préoccuper d'encoding (cf SLServlet.doPost) ?
private void doGetOrPost_impl(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	// BasicServlet.printRequestInfo(req);
	SLServlet.handleParams(req);
	String servletPath = req.getServletPath();
	boolean isJsDoubleEncodeURIComponent = (req.getParameter("js") != null);

	if ((DOC_SERVLET_PATH.equals(servletPath))
			|| (DOC_SERVLET_PATH2017.equals(servletPath))) {

		// 2 cases: 
		// - doc/x/y/z
		// - doc?uri=xxx (mostly for "old fashioned bookmarks" -- possibly still used as links in comments)
		
		// 2021-01: (f*ck) we cannot just test whether there is a param uri,
		// because it uri param used when adding a tag with drag'ndrop for the added tag.
				
		// String relativUrl = req.getPathInfo(); // avec ça, a%20b.html est transformé en a b.html
		String relativUrl = BasicServlet.getPathInfo_Patched(req);
		
		if (relativUrl.length() > 1) { // first case
			relativUrl = relativUrl.substring(1); // virer le / initial
			String docuri = null;
			
			// THE WS QUESTION
			
			// prendre en compte le webserver pour
			// passer à l'uri réellement utilisée dans le rdf (qui elle tient compte du webserver)
			// (hack)
			// (même genre de chose plus bas)
			// TODO : devrait être inutile : le RDF devrait contenir des infos directement au sujet de cette url !
			// (moui, sauf que ya le webserver qui sert vraiment des trucs à l'intérieur du dossier, et l'historique)
			WebServer ws = SLServlet.getWebServer();
			// 2019-03 uris for bookmarks
			// Hum, ws n'est jamais null !!!
			
			String url = req.getRequestURL().toString(); //  http://127.0.0.1:9080/semanlink/doc/bla
			if (ws != null) {
				try {
					if (!ws.owns(url)) { // 2019-03 uris for bookmarks
						// (hack utilisé par vieil accès)
						String base = ws.getURI(ws.getDefaultDocFolder());
						docuri = base + relativUrl;
					} else {
						docuri = url; // 2019-03 uris for bookmarks
					}
				} catch (URISyntaxException e) {
					// throw new RuntimeException(e);
					logInvalidUri(url, e, req); // 2025-01 - a priori pas là où on avait les exceptions
					throw new SLRuntimeException(e);
				}
				
			} else {
				// possible ? il semble que non, voir SemanlinkConfig
				docuri = net.semanlink.util.Util.getContextURL(req) + servletPath + "/" + relativUrl;
			}

			// 2019-10 bordel des response.encode machin pour le jsessionid
			// (et gaffe si on a des query params)
			// Ca arrive ici - je vais patcher ds goDoc
	
			// can happen for .md files
			boolean openInApp = (req.getParameter("openInDesktop") != null);
			if (openInApp) {
				try {
					// 2020-01 localFilesOutOfDatafolders
					// this was not taking into account the subtilties regarding what "the file" is
				  // when we have a bookmark which is a bookmark on a local file out of a datafolder
					// File f = SLServlet.getSLModel().getFile(docuri);
					SLModel mod = SLServlet.getSLModel();
					SLDocumentStuff stuff = new SLDocumentStuff(mod.getDocument(docuri), mod, Util.getContextURL(req));
					File f = stuff.getFile();
					if (f != null) {
						Desktop.getDesktop().open(f);
					}
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			
			// goDoc(docuri, req, res); // 2025-01
			try {
				goDoc(docuri, req, res);
			} catch (SLRuntimeException e) { // 2025-01 added (too many exceptions in logs, called from exterior (?) même si pas là a priori
				// logInvalidUri(docuri, e, req);
				throw e;
			}
			return;
			
		} else { // 2nd case
			String docuri = req.getParameter("uri");
			if (docuri != null) {
				if (isJsDoubleEncodeURIComponent) docuri = URLDecoder.decode(docuri,"UTF-8");
				
				
				
				// 2017-09 links to files in markdown
				// cf les liens dans du markdown: pas forcément les uris utilisées dans le rdf,
				// (parce que le markdown-sl.js replaceLinkFct ne prend pas en compte le webserver)
				// voire des paths qui ne sont pas des uri genre /semanlink/document/2002/01/toto
				// (parce que ça suffit pour les liens dans le html généré à partir du md)
				
				try {
					// let's support a path such as "/semanlink/document/2002/01/toto"
					// and also such as "/document/2002/01/toto" - which is not really absolute, 
					// but to have it working
					// both locally and on semanlink.net
					
					if (docuri.startsWith("/")) {
						if (docuri.startsWith("/semanlink")) {
							docuri = (new URI(SLServlet.getServletUrl())).resolve(docuri).toString();
							// same thing, I think:
							// docuri = (new URI(BasicServlet.getContextURL(req))).resolve(docuri).toString(); 
						} else {
							docuri = BasicServlet.getContextURL(req) + docuri; // http://127.0.0.1:8080/semanlink + "/document/2002/01/toto"
						}					
					}
					
					// THE WS QUESTION
					
					// si on a une uri qui ne tient pas compte du webserver (genre http://127.0.0.1:8080/semanlink/document/2012/06/toto.md)
					// passer à l'uri réellement utilisée dans le rdf (qui elle tient compte du webserver)
					// (ça arrive -- en paticulier ? - avec des urls venant de .md)
					// (hack)
					String documentPath = SLServlet.getServletUrl() + StaticFileServlet.PATH + "/"; // http://127.0.0.1:8080/semanlink/document/
					if (docuri.startsWith(documentPath)) {
						// System.out.println("CoolUriServlet docuri an /document/ " + docuri);
						WebServer ws = SLServlet.getWebServer();
						if (ws != null) {
							String base = ws.getURI(ws.getDefaultDocFolder()); // http://127.0.0.1/~fps/fps/
							docuri = base + docuri.substring(documentPath.length());
						}
					}

				} catch (URISyntaxException e) { 
					logInvalidUri(docuri, e, req);
					throw new RuntimeException(e) ;
				}
				
				// 2019-07 : uris for bookmarks
				// to have old form of links to (old) docs (/doc?uri=xxx) still work
				// if used with a new doc (using bookmarkOf)
				// (this in order to have old links in comments still working if
				// changing the doc form from old to new)
				SLDocument bookmark2019;
				SLModel mod = SLServlet.getSLModel();
				try {
					bookmark2019 = mod.bookmarkUrl2Doc(docuri);
				} catch (Exception e) { throw new RuntimeException(e); }
				if (bookmark2019 != null) {
					// this does work:
//					docuri = bookmark2019.getURI();
//					goDoc(docuri, req, res);
//					return;
					// but let's redirect to the new url instead
					SLDocumentStuff stuff = new SLDocumentStuff(bookmark2019, mod, Util.getContextURL(req));
					String redirectURL = stuff.getAboutHref();
			  	res.setStatus(303);
			  	res.setHeader("Location", res.encodeRedirectURL(redirectURL));
			  	return;
			  	
				}
				try {
					goDoc(docuri, req, res);
				} catch (SLRuntimeException e) { // 2025-01 added (too many exceptions in logs, called from exterior (?)
					// logInvalidUri(docuri, e, req); // already done in goDoc
					throw e;
				}
				return;
			}			
		} // if 1st or 2nd case
	}
	
	if ( (NOTE_SERVLET_PATH.equals(servletPath)) || (NOTE_SERVLET_PATH_KATTARE.equals(servletPath)) ) {
			// cas des notes. ATTENTION : suppose l'absence de caractères à la con
			String relativUrl = req.getPathInfo();
			relativUrl = relativUrl.substring(1); // virer le / initial
			goDoc(net.semanlink.util.Util.getContextURL(req) + servletPath + "/" + URLUTF8Encoder.encodeFilename(relativUrl), req, res);
			return;
	}
	
	// je n'ai pas réussi à faire faire ça par SLServlet (quand
	// on passe "/about/filename.html", ce con de struts se plaint "invalid path")
	// ce qui m'oblige à traiter le path "/sl/about" dans coolUriServlet
	if (ABOUT_SERVLET_PATH.equals(servletPath))  {
		// cas de la doc sur sl. ATTENTION : suppose l'absence de caractères à la con
		/*String relativUrl = req.getPathInfo();
		relativUrl = relativUrl.substring(1); // virer le / initial
		goABout(net.semanlink.util.Util.getContextURL(req) + servletPath + "/" + URLUTF8Encoder.encodeFilename(relativUrl), req, res);*/
		goAbout(req, res);
		return;
	}
		
	CoolUriRequest coolRequest = new CoolUriRequest(req, isJsDoubleEncodeURIComponent);
	handleCoolUriRequest(servletPath, coolRequest, req, res);
}

/**
 * 
 */
protected void handleCoolUriRequest(String servletPath, CoolUriRequest coolRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	// le "/doc/url du doc" n'est plus utilisé (2005/12) à cause d'un
	// pb avec : 11 avril 1241 - les Mongols écrasent les Hongrois; l'Europe tremble.html ds tagservlet
	// Le pb est au niveau du ; : tout ce qui le suit n'est pas ds req.getServletPath() (alors que c'est bien
	// ds req.getRequestURL()) // voir https://stackoverflow.com/questions/2163803/what-is-the-semicolon-reserved-for-in-urls
	// La modif est dans HTML_Link (chercher les mongols)
	// (rq ; ds le cas du double encoding en js, ça marchait quand même)
	/* if ("/doc".equals(servletPath)) {
		goDoc(tagUri, req, res);
	} else if ("/docjs".equals(servletPath)) {
		try {
			tagUri = SLUtils.laxistUri2Uri(tagUri);
		} catch (URISyntaxException e) { throw new RuntimeException(e); }
		goDoc(tagUri, req, res);
	} else */
	
	if ("/page".equals(servletPath)) {
		goPage(req.getRequestURL().toString() , req.getPathInfo(), req, res);

	} else if ("/rss".equals(servletPath)) { // old way for a tag as rss MAIS AUSSI, ATTENTION, sur news
		rss(coolRequest.getUri(),req,res);
		
	/*} else if ("/delicious".equals(servletPath)) {
		delicious(coolRequest.getUri(),req,res);*/
		
	// kattare
	} else if ("/semanlink/rss".equals(servletPath)) { // old way for a tag as rss
		rss(coolRequest.getUri(),req,res);

	// } else if ("/documents".equals(servletPath)) { // handled by StaticFileServlet

	} else if (TAG_SERVLET_PATH.equals(servletPath)) {
		goTag(coolRequest.getUri(),req,res);

	} else {
		printRequestInfo(req);
		throw new RuntimeException("Unexpected servletPath: " + servletPath);
	}
}

protected class CoolUriRequest {
	private String tag;
	private String tagUri;
	CoolUriRequest(HttpServletRequest req, boolean isJsDoubleEncodeURIComponent) throws UnsupportedEncodingException {
		// with tomcat5.5 this doesn't work for Greek tags
		// for http://127.0.0.1:8080/semanlink/tag/afrique
		// returns /afrique
		// but  when we requested for http://127.0.0.1:8080/semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
		// (that is to say http://127.0.0.1:8080/semanlink/tag/Λεωνίδας.html)
		// returns /Î?ÎµÏ?Î?Î¯Î´Î±Ï?.html 
		// instead of /%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
		// this.tag = req.getPathInfo(); // /afrique
		this.tag = getPathInfo_Patched(req); // /afrique
		
		// 2012-04 with tomcat 7, we get here for instance /afrique.html;jsessionid=0B2B7F2EF14BBF9C8124DCB31E25914E
		// PATCH / HACK / hmm ???
		int k = this.tag.indexOf(";jsessionid=");
		if (k > -1) this.tag = this.tag.substring(0, k);

		this.tagUri = null;
		if (tag != null) {
			// cas du docjs (double encodeURIComponent en javascript) n'est pas terrible : utilisr /doc et un argument ?js
			// if ("/docjs".equals(servletPath)) { // cas où on a un double encodeURIComponent en javascript
			if (isJsDoubleEncodeURIComponent) {
				tag = URLDecoder.decode(tag,"UTF-8"); // de la forme http://127.0.0.1/a b.html, d'où plus bas l'appel à laxistUri2Uri
				tag = tag.substring(1); // virer le / initial
				try {
					tagUri = SLUtils.laxistUri2Uri(tag);
				} catch (URISyntaxException e) { throw new RuntimeException(e); }
			} else {
				tag = tag.substring(1); // virer le / initial -> afrique
				
				if ("".equals(tag)) {
					tagUri = req.getParameter("uri");
				} else {
					// cas court vs long : uri complète ou bien juste le tag
					tagUri = getRequestArgument(tag); // cas long plus possible ? (voir getKwRelativHREF ds HTML_Link)
				}
				// System.out.println("TAG tagUri: " + tagUri);
			}
		}	
	}
	/** for instance a tag, such as "africa" or "africa.html" */
	public String getArg() { return this.tag; }
	/** for instance url of a tag, such as http://www.semanlink.net/tag/africa or http://www.semanlink.net/tag/africa.html */
	public String getUri() { return this.tagUri; }
}

/** 
 * To correct a problem with HttpServletRequest.getPathInfo. 
 * for http://127.0.0.1:8080/semanlink/tag/afrique
 * getPathInfo(req) returns /afrique
 * but  when we requested for http://127.0.0.1:8080/semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * (that is to say http://127.0.0.1:8080/semanlink/tag/Λεωνίδας.html)
 * returns /Î?ÎµÏ?Î?Î¯Î´Î±Ï?.html 
 * instead of /%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * Hence this patch.
 */
static String getPathInfo_Patched(HttpServletRequest req) {
	return BasicServlet.getPathInfo_Patched(req);
}

/** 
 * @param tag l'argument de la requête
 * par ex "afrique", ou l'uri complète, ou une uri complète mal formée http:/xxx...
 * MAIS AUSSI l'uri d'un DOC
 * Patch le problème mystérieux pour http, file (attention si on rajoute des types d'url)
 * (en effet, mystérieusement, alors que ce qui est passé à TagServlet est la forme encodée de http://xxx,
 * on se retrouve dans doGetOrPost avec http:/xxx)
 * (Attention au cas "/doc2" où on a une uri qui a eu un double encodeUriComponent : il faut au préalable
 * avoir fait un URLDecoder.decode et dans ce cas, on arrive ici avec http:// - mais attention, une uri laxiste (des spaces, par ex)
 * Par défaut, suppose avoir affaire à un tag
 */
public static String getRequestArgument(String tag) {
	if (tag.startsWith("http:/")) {
		if (!(tag.startsWith("http://"))) {
			tag = "http://" + tag.substring(6);
		}
	} else if (tag.startsWith("file://")) { // pas testé
			if (!(tag.startsWith("file:///"))) {
				tag = "file:///" + tag.substring(7);
			}
	} else if  (tag.startsWith("file:/")) {
			if (!(tag.startsWith("file:///"))) {
				tag = "file:///" + tag.substring(6);
			}
	} else {
		// tag = SLServlet.getSLModel().getDefaultThesaurus().getURI()+"#" + tag; // #thing
		tag = SLServlet.getSLModel().getDefaultThesaurus().getURI()+"/" + tag; // #thing
	}	
	return tag;
}




/** 
 * @param tag par ex "afrique", ou l'uri complète, ou une uri complète mal formée http:/xxx...
 * (en effet, mystérieusement, alors que ce qui est passé à TagServlet est la forme encodée de http://xxx,
 * on se retrouve dans doGetOrPost avec http:/xxx)
 * Attention, contient l'éventuelle extension (".html")
 */
public static String getTagUri(String tag) {
	return getRequestArgument(tag);
}





static protected SLKeyword getKeyword(SLModel mod, String kwuri) {
	return mod.getKeyword(kwuri);	
}

static public SLKeyword getSLKeyword(String kwuri, HttpServletRequest request) {
	SLModel mod = SLServlet.getSLModel();
	if ("true".equals(request.getParameter("resolvealias"))) {
		return mod.resolveAlias(kwuri);
	} else {
		return getKeyword(mod, kwuri);
	}
}

/**
 * return the decoded kwuris included in an andOfTag query (or null)
 */
// 2020-03: tagAndTags tag?and=kw1&and=kw2&
private static String[] decodeAndOfTagsQuery(HttpServletRequest request) throws UnsupportedEncodingException {
  String[] andKws = request.getParameterValues(AND_QUERY_PARAM);
  if ((andKws != null) && (andKws.length > 0)) {
    	// cf. Action_AndKws2
      for (int i = 0; i < andKws.length; i++) {
      	andKws[i] = java.net.URLDecoder.decode(andKws[i],"UTF-8");
      	if ((!andKws[i].startsWith("http:")&&(!andKws[i].startsWith("https:")))) {
      		andKws[i] = HTML_Link.DEFAULT_THESAURUS_URI_SLASH + andKws[i];
      	}
      }
      return andKws;
  } else {
  	return null;
  }
}

// tag request, but not a kwuri: either thesaurus, or and of tags
private static void tagRequestWoTag (HttpServletRequest request, HttpServletResponse response) {
  try {
		RequestDispatcher requestDispatcher = null;
		// 2020-03: tagAndTags tag?and=kw1&and=kw2&
	  String[] kwuris = decodeAndOfTagsQuery(request);
	  if (kwuris != null) {
			  Jsp_Page jsp = null;
		    Jsp_AndKws jspAndKws = new Jsp_AndKws(kwuris, request);
		    
//		    if (request.getParameter("newkw") != null) {
//		    		SLKeyword newKw = jspAndKws.toNewKeyword();
//		    		jsp = new Jsp_Keyword(newKw, request);
//		    } else {
//		    		jsp = jspAndKws;
//		    }
		    
		    if (request.getParameter("newkw") != null) {
	    		SLKeyword newKw = jspAndKws.toNewKeyword();
	    		jsp = new Jsp_Keyword(newKw, request);
		    } else {
		    		// 2021-07 if no docs in intersection, display docs of last of the kws instead
		    	  // (Not sure this is a good idea. Purpose is not to frighten people
		    	  // searching a tag on the web site when already displaying one
		    	  boolean REDIRECT_IF_EMPTY_INTERSECTION = true;
		    	  if (REDIRECT_IF_EMPTY_INTERSECTION) {
			    		List<SLDocument> docs = jspAndKws.getDocs();
			    		if (docs.size() == 0) {
			    			// OK, sauf qu'on a l'url en and
			    			// SLKeyword kw = SLServlet.getSLModel().getKeyword(kwuris[kwuris.length-1]);
			    			// jsp = new Jsp_Keyword(kw, request);
			    			// So, we have to do a redirect
			        	response.setStatus(303);
			        	String url = HTML_Link.getTagURL(Util.getContextURL(request), kwuris[kwuris.length-1], false, null);
			        	// String qs = request.getQueryString(); if (qs != null) url += "?" + qs;
			        	response.setHeader("Location", url); // resolve ?
			        	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08
			        	return;
			    		}
		    	  }

		    	  jsp = jspAndKws;
		    }

		    request.setAttribute("net.semanlink.servlet.jsp", jsp);
	  		requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());
	
		} else {
			
	  	// case http://.../tag: display page for thesaurus
	  	// @TODO : rdf versus html
		  SLThesaurus th = SLServlet.getSLModel().getDefaultThesaurus();
		  Jsp_Thesaurus jsp = new Jsp_Thesaurus(th, request);
		  request.setAttribute("net.semanlink.servlet.jsp", jsp);
			requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());
	  }
	  
		response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2019-05
	  requestDispatcher.forward(request, response);
  } catch (Exception e) {
    throw new SLRuntimeException(e);
  }
}

static public void goTag(String kwuri, HttpServletRequest request, HttpServletResponse response) {
  if (kwuri == null) { 
  	tagRequestWoTag(request, response);
    return;
  }
  
  // 2020-05 for dragndrop (add kw to tag list)
  // post to tag url, with new parents/children/etc
  // (something that was done using Action_GoKeyword and/or Action_EditTagList
  // In a more modern way, we would use the tag resource
  
  // if access to the "resource" with content negotiation,
  // redirect to doc.xxx file
  
  try {
    RequestDispatcher requestDispatcher = null;  	
  	// 2007-01 (httprange-14)
    int iContentType = 0;
    boolean redirect = false; // 2020-05
    

    if (kwuri.endsWith(".html")) {
    	kwuri = kwuri.substring(0,kwuri.length() - 5);
    } else if (kwuri.endsWith(".rdf")) {
    	iContentType = 1;
    	kwuri = kwuri.substring(0,kwuri.length() - 4);
    } else if (kwuri.endsWith(".n3")) {
    	iContentType = 3;
    	kwuri = kwuri.substring(0,kwuri.length() - 3);
    } else if (kwuri.endsWith(".json")) { // 2012-08 JSON-LD
    	iContentType = 4;
    	kwuri = kwuri.substring(0,kwuri.length() - 5);
    } else if (kwuri.endsWith(".rj")) { // 2012-08 RDF/JSON TALIS
    	iContentType = 5;
    	kwuri = kwuri.substring(0,kwuri.length() - 3);
    } else if (kwuri.endsWith(".rss")) {
    	// iContentType = 2;
    	kwuri = kwuri.substring(0,kwuri.length() - 4);
  		rss(kwuri,request,response);
  		return;
    } else {
    	redirect = true;
    }
    
    
    // BasicServlet.printRequestInfo(request);
    
    // 2020-05
	  // see tree.js post_usingExistingForm
 	  // see template.jsp form id="tags_form"
    String action = request.getParameter("action2020"); // 2020-05
    if ((action != null) && (!"".equals(action))) { // adding kws to this kw (parent, children, releated
    	SLKeyword thisKw = getSLKeyword(kwuri, request);
    	
  		String addedKwUri = request.getParameter("dragTagUri");
  		if ((addedKwUri == null) || ("".equals(addedKwUri))) throw new RuntimeException();
   		// ce bordel de url en 127 et default thesaurus différent
  		addedKwUri = tagUrl2Uri(addedKwUri);
  		SLKeyword addedKw = getSLKeyword(addedKwUri, request);
  		
  		redirect = true;
  		
  		if ("add2parents".equals(action)) {
    		SLServlet.getSLModel().addChild(addedKw, thisKw);

    	} else if ("add2children".equals(action)) {
    		SLServlet.getSLModel().addChild(thisKw, addedKw);

    	} else if ("add2friends".equals(action)) {
    		SLServlet.getSLModel().addFriend(thisKw, addedKw);

    	} else {
    		throw new IllegalArgumentException("Unexpected action2020: " + action);
    	}
    }

  	if (redirect) {
    	AcceptHeader acceptHeader = new AcceptHeader(request);
    	String dotExtension = null;
    	

    	if (acceptHeader.accepts(AcceptHeader.RDF_N3)) {
    		dotExtension = ".n3";
    	} else if (acceptHeader.accepts(AcceptHeader.RDF_XML)) {
    		dotExtension = ".rdf";
    	} else if (acceptHeader.accepts(AcceptHeader.JSON_LD)) { // 2012-08 JSON-LD
    		dotExtension = ".json";
    	} else if (acceptHeader.accepts(AcceptHeader.RDF_JSON_TALIS)) { // 2012-08 RDF/JSON TALIS
    		dotExtension = ".rj";
    	} else { // redirect to html
     		dotExtension = ".html";
    	}
    	
    	// this is 302, not 303
    	// response.sendRedirect(response.encodeRedirectURL(HTML_Link.getTagURL(Util.getContextURL(request), kwuri, false, dotExtension))); // resolve ? autres params (snip) ?
    	response.setStatus(303);

    	String url = HTML_Link.getTagURL(Util.getContextURL(request), kwuri, false, dotExtension);
    	String qs = request.getQueryString(); if (qs != null) url += "?" + qs;
    	response.setHeader("Location", url); // resolve ?
    	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08
    	return;
  	}

    SLKeyword kw = getSLKeyword(kwuri, request);
    
    
    
    
    
    if (handleAsPptyQuery(kw, request, response)) { // 2020-11
    	return;
    }

    
    
    
  
  	Jsp_Keyword jsp = new Jsp_Keyword(kw, request);
    request.setAttribute("net.semanlink.servlet.jsp", jsp);
    if (iContentType == 0) { // html
      String snip = request.getParameter("snip");
      if (snip != null) {
      	if (snip.indexOf("documents") > -1) {
        	jsp.setDisplaySnipOnly(true);
        	jsp.setDisplayParents(false);
        	jsp.setDisplayChildrenAndDocs(true);
    	    requestDispatcher = request.getRequestDispatcher("/jsp/keyword.jsp");
      	} else {
      		Bean_KwList truc = new Bean_KwList();
      		truc.setUri(kw.getURI());
        	if (snip.indexOf("children") > -1) {
        		truc.setList(kw.getChildren());
        	} else if (snip.indexOf("parents") > -1) {
        		truc.setList(kw.getParents());
        	} else {
        		throw new RuntimeException("Unexpected snip : " + snip);
        	}
      		truc.setContainerAttr(null);
      		request.setAttribute("net.semanlink.servlet.Bean_KwList", truc);
    	    requestDispatcher = request.getRequestDispatcher("/jsp/tagsnip.jsp");
    	    // requestDispatcher = request.getRequestDispatcher("/jsp/tagsnips_" + snip + ".jsp");
      	}
  	    // RequestDispatcher requestDispatcher = request.getRequestDispatcher("/jsp/tagsnips_" + snip + ".jsp");
      } else {
    		requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());
      }

    } else if ((iContentType == 1) || (iContentType == 3) || (iContentType == 4) || (iContentType == 5)) { // rdf, n3, json-ld, rdf/json
    	Model rdfMod = null;
    	if (iContentType == 1) {
    		rdfMod = jsp.getRDF("rdf");
    	} else if (iContentType == 3) {
    		rdfMod = jsp.getRDF("n3");
    		request.setAttribute("net.semanlink.servlet.rdf.lang", "n3");
    	} else if (iContentType == 4) {
    		rdfMod = jsp.getRDF("json");
    		request.setAttribute("net.semanlink.servlet.rdf.lang", "json"); // 2012-08 JSON-LD
    	} else if (iContentType == 5) {
    		rdfMod = jsp.getRDF("rj");
    		request.setAttribute("net.semanlink.servlet.rdf.lang", "rj"); // 2012-08 RDF/JSON TALIS
    	}
    // } else if (iContentType == 2) { // rss: done + haut

			request.setAttribute("net.semanlink.servlet.rdf", rdfMod);
			requestDispatcher = request.getRequestDispatcher("/rdf"); // forward to the RDFServlet

    } else {
    	throw new RuntimeException("Unexpected iContentType: " + iContentType);
    }

    
  	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2019-05
    requestDispatcher.forward(request, response);

  } catch (Exception e) {
    e.printStackTrace();
    throw new SLRuntimeException("SLRuntimeException in goTag " + kwuri, e);
  }
}


/**
 * if it is a query for the ppty of the resource, handle it
 * @param res the resource corresponding to request (doc or tag)
 * @param request
 * @param response
 * @return true if it is a query for the ppty of the resource
 * @throws IOException 
 * @since 2020-11
 */
static boolean handleAsPptyQuery(SLLabeledResource res, HttpServletRequest request, HttpServletResponse response) throws IOException {
	String requestedPpty = request.getParameter("ppty");
	if (requestedPpty != null) {
		
		// TODO CHANGE: on a déjà fait ça qmq part, la conversion des short names
		if (requestedPpty.equals("dc:title")) {
			requestedPpty = SLVocab.TITLE_PROPERTY;
		} else if (requestedPpty.equals("skos:prefLabel")) {
			requestedPpty = SKOS.prefLabel.getURI();
		} else if (!requestedPpty.startsWith("http")) {
			throw new Error400Exception("Not a prop uri " + requestedPpty);
		}
		
		PropertyValues pvals = res.getProperty(requestedPpty);
		String s = null;
		if (pvals != null) {
			s = pvals.getFirstAsString();
		}
		if (s == null) {
			s = res.getURI();
		}
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(s);

		return true;
	}
	return false;
}

// 2020-05
// ce bordel de url en 127 et default thesaurus différent. PUTAIN y faire qlq chose
/**
 * 
 * @param kwUrl eg http://127.0.0.1:8080/tag/foo (take also care to remove final '.html' if present)
 * @return eg http://www.semanlink.net/tag/foo
 */
static public String tagUrl2Uri(String kwUrl) {
	String kwUri = kwUrl; 
	String base = SLServlet.getServletUrl();
	if (!base.endsWith("/")) base += "/";
	base += TAG_SERVLET_PATH.substring(1) + "/";
	if (kwUrl.startsWith(base)) {
		kwUri = SLServlet.getSLModel().getDefaultThesaurus().getURI()+"/" + kwUrl.substring(base.length());
	}
	if (kwUri.endsWith(".html")) kwUri = kwUri.substring(0, kwUri.length() - 5);
	return kwUri;
}

//TODO : on vient souvent de la page tag : UTILITE D'UN CACHE !
static public void rss(String kwuri, HttpServletRequest request, HttpServletResponse response) {
	  try {
	  	Jsp_RSS jsp = null;
	  	if (kwuri != null) {
	  		SLKeyword kw = getSLKeyword(kwuri, request);
		    jsp = new Jsp_TagAsRSS(kw, request);
	  	} else {
	  		jsp = new Jsp_NewsAsRSS(request);
	  	}
		request.setAttribute("net.semanlink.servlet.jsp", jsp);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("/jsp/rss.jsp");
		requestDispatcher.forward(request, response);

	  } catch (Exception e) {
	    e.printStackTrace();
	    throw new SLRuntimeException(e);
	  }
}

public void delicious(String kwuri, HttpServletRequest request, HttpServletResponse response) {
  try {
   	// BOF BOF:
  	Jsp_Page jsp = new Jsp_Page(request);
  	String title;
  	if (kwuri != null) {
  		SLKeyword kw = getSLKeyword(kwuri, request);
  		title = "Exporting to delicious docs with tag " + kw.getLabel() + " ; please wait";
	    jsp.setDocList(new Jsp_Keyword(kw, request).getDocList());
  	} else {
   		title = "Exporting new entries to delicious ; please wait";
	    jsp.setDocList(new Jsp_ThisMonth(request).getDocList());
  	}
		request.setAttribute("net.semanlink.servlet.jsp", jsp);
		jsp.setContent("/jsp/deliciousexport.jsp");
		jsp.setTitle(title);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());
		requestDispatcher.forward(request, response);

  } catch (Exception e) {
    e.printStackTrace();
    throw new SLRuntimeException(e);
  }
}

/** ??? même si ca avait l'air interessant */
private void goPage(String docuri, String pathInfo, HttpServletRequest request, HttpServletResponse response) {
  try {
	  	// System.out.println(docuri);
	  	SLModel mod = SLServlet.getSLModel();
	  	SLDocument doc = mod.getDocument(docuri);
		Jsp_Document jsp = Manager_Document.getDocumentFactory().newJsp_Document(doc, request);
		
		// pas prendre page mais autre chose (docs) (si on garde page, ca merde genre morsure de queue
		
		jsp.setPagePathInfo("/docs" + pathInfo);
	
		request.setAttribute("net.semanlink.servlet.jsp", jsp);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());
		requestDispatcher.forward(request, response);
	
	} catch (Exception e) {
	  e.printStackTrace();
	  throw new SLRuntimeException(e);
	}
}


//
// doc
//

/**
 * Display the document using the document.jsp
 * @param docuri document's URI as it is used in the RDF (that is, the URI served by the webserver)
 * @param request
 * @param response
 */
static public void goDoc(String docuri, HttpServletRequest request, HttpServletResponse response) {
	
		// 2019-10 bordel des response.encode machin pour le jsessionid
		// patch horrible
		int k = docuri.indexOf(";jsessionid=");
		if (k > -1) {
			docuri = docuri.substring(0, k);
		}

	  try {
	  	// System.out.println(docuri);
	  	SLModel mod = SLServlet.getSLModel();
	  	
	  	// 2020-01 no access to file uris if not running locally
	  	if (docuri.startsWith("file:")) {
	  		String con = Util.getContextURL(request);
	  		if (!(con.startsWith("http://127.0.0.1"))) {
		  		// forbid access to docuri
		  		// redirect to /doc instead
		  		docuri = con + DOC_SERVLET_PATH + "/";
		  	}
	  	}

	  	SLDocument doc = mod.getDocument(docuri);
	    
	    

	  	
	  	
	  	
	    // 2021-01 copied from goTag 
		  // see tree.js post_usingExistingForm
	 	  // see template.jsp form id="tags_form"
	    String action = request.getParameter("action2020");
	    if ((action != null) && (!"".equals(action))) { // adding kws to this kw (parent, children, releated
	  		String addedKwUri = request.getParameter("dragTagUri");
	  		if ((addedKwUri == null) || ("".equals(addedKwUri))) throw new RuntimeException();
	   		// ce bordel de url en 127 et default thesaurus différent
	  		addedKwUri = tagUrl2Uri(addedKwUri);
	  		SLKeyword addedKw = getSLKeyword(addedKwUri, request);
	    	SLServlet.getSLModel().addKeyword(doc, addedKw);
	    }
	  	
	  	
	  	
	  	
	  	
	  	
	  	
	    
	    
	    if (handleAsPptyQuery(doc, request, response)) { // 2020-11
	    	return;
	    }

	    
	    
	    
	  
	  	Jsp_Document jsp = Manager_Document.getDocumentFactory().newJsp_Document(doc, request);

	  	String imageToBeDisplayed = request.getParameter("imagetobedisplayed");
	  	if (imageToBeDisplayed != null) {
	  		// JE NE COMPRENDS PAS POURQUOI MAIS,
	  		// alors que docuri a ete encode, il ne faut pas ici le decoder.
	  		// VOIR AUSSI ici + haut et ds Action_NextImage et Action_ShowKeyword
	  		jsp.setImageToBeDisplayed(mod.getDocument(imageToBeDisplayed), -1); // en vrai, on n'a besoin que de l'uri - at this time
	  	}
			request.setAttribute("net.semanlink.servlet.jsp", jsp);
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(jsp.getTemplate());

			
			// 2025-01 pb de stacktrace nombreuse dans version en ligne
			
			// BORDEL, quand il y a ici une exception (URISyntaxException,
			// la stacktrace est printée la dedans, sans que je sache comment l'empêcher :-(
			// ex d'apple qui pose pb :
			// http://127.0.0.1:7080/semanlink/doc/?uri=http%3A%2F%2Fwww.wildml.com%2F2016%2F01%2Fattention-and-memory-%20%20in-deep-learning-and-nlp%2F
			
			// System.out.println("AVANT requestDispatcher.forward"); // TODO REMOVE
			// requestDispatcher.forward(request, response);
			// en cas d'exception, on ne passe pas la dedans
			// System.out.println("APRES requestDispatcher.forward"); // TODO REMOVE
			
			// en conséquence de quoi: vérifier d'abord que les choses vont bien se passer
			// -- en tout cas vérifier qu'on n'aura pas de URISyntaxException :
			
			try {
				new URI(docuri);
			} catch (URISyntaxException e) {
		  	logInvalidUri(docuri, e, request);
		  	throw new SLRuntimeException(e);
			}
			
			// on peut maintenant faire ceci sans craindre une stacktrace inopinée 	
			// pour une malheureuse uri mal branlée par un appel externe inapproprié
			requestDispatcher.forward(request, response);
			
	  } catch (SLRuntimeException e) {
	    throw e;
	  
	  } catch (Exception e) {
	    // e.printStackTrace(); // viré 2025-01 cf. logs
	    throw new SLRuntimeException(e);
	  }
}

//
//about
//

// we could have a redirect to have the lang included in the uri
// (or we could test for every path at the time oif generating the calling page)
void goAbout(HttpServletRequest request, HttpServletResponse response) {
  try {
  	String pathInfo = request.getPathInfo();  // request.getPathInfo() begins with a "/", for instance "/help.htm"
  	if (pathInfo.endsWith(".htm")) {
  		// Including a static HTML page in UTF-8 doesn't work (at least with tomcat 5)
  		// We need to include a jsp.
  		// Static ".htm" were used. Changing here ".htm" to ".jsp" allows to change all references at once.
  		// Also, (and more importantly) we would have a problem using directly a link to ".jsp" files, as they would be forwarded
  		// directly to corresponding jsp.
  		pathInfo = pathInfo.substring(0,pathInfo.length()-3) + "jsp";
  	}
  	
  	String path = I18l.pathToI18lFile(pathInfo, request.getSession(), getServletContext());
  	
  	RequestDispatcher requestDispatcher = null;
  	if (path.endsWith(".jsp")) {
		  Jsp_SimplePage jsp = new Jsp_SimplePage(request);
			jsp.setTitle("About Semanlink");
			jsp.setHtmlFile(path);
	
			request.setAttribute("net.semanlink.servlet.jsp", jsp);
			requestDispatcher = request.getRequestDispatcher("/jsp/template_about.jsp");
  	} else {
			requestDispatcher = request.getRequestDispatcher(path);
  	}
		requestDispatcher.forward(request, response);

  } catch (Exception e) {
    e.printStackTrace();
    throw new SLRuntimeException(e);
  }
}

/*
// doesn't work
// for non english, this version looks whether there is a corresponding file in the config dir
static public String pathToI18lFile(String pathInfo, ServletContext context) {
	String shortFilename = pathInfo;
	String path = null;
	String lang = I18l.getLang();
	
	File outOfDistribDir = new File(SLServlet.getConfigDir(),"i18l/aboutfiles/");
	if (outOfDistribDir.exists()) {
		File f = new File(outOfDistribDir, lang + shortFilename);
		if (f.exists()) return f.getPath();
	}

	if (!("en".equals(lang))) {
		// obligé de choisir un dossier qui ne s'appelle pas "about" (cad, pas comme ds le path de la request)
		path = "/aboutfiles/" + lang + shortFilename;
		String fn = context.getRealPath(path);
		File f = new File(fn);
		if (f.exists()) return path;
	}
	
	return "/aboutfiles/en" + shortFilename;
}
*/
} // class
