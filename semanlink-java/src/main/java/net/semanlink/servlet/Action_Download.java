package net.semanlink.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.struts.action.*;

import net.semanlink.arxiv.Arxiv;
import net.semanlink.semanlink.*;
import net.semanlink.util.CopyFiles;
import net.semanlink.util.Util;
import net.semanlink.util.html.HTMLPageDownload;

// TODO !!
/*
 // YA 2 TRUCS NULS : 
  // 1) on lit le html une fois ici, et aune autre ds le addedDoc pour extraire metadata
   // 2) si problème lecture du html ici (générant une RuntimeException), on plante ds 
    // le HTMLPageDoowload, et on n'est même pas capable de suaver le fichier tel quel
     // (simple save) Ex :
      WARN [http-8080-Processor23] (RequestProcessor.java:538) - Unhandled Exception thrown: class net.semanlink.semanlink.SLRuntimeException
      java.lang.RuntimeException: Unexpected top level block close
      at javax.swing.text.html.CSSParser.getNextStatement(CSSParser.java:175)
      at javax.swing.text.html.CSSParser.parse(CSSParser.java:136)
      at javax.swing.text.html.StyleSheet$CssParser.parse(StyleSheet.java:3031)
      at javax.swing.text.html.StyleSheet.addRule(StyleSheet.java:272)
      at javax.swing.text.html.HTMLDocument$HTMLReader.addCSSRules(HTMLDocument.java:3345)
      at javax.swing.text.html.HTMLDocument$HTMLReader$HeadAction.end(HTMLDocument.java:2495)
      at javax.swing.text.html.HTMLDocument$HTMLReader.handleEndTag(HTMLDocument.java:2233)
      at net.semanlink.util.html.HTMLDocumentLoaderNew$1.handleEndTag(HTMLDocumentLoaderNew.java:143)
      at javax.swing.text.html.parser.DocumentParser.handleEndTag(DocumentParser.java:217)
      at javax.swing.text.html.parser.Parser.parse(Parser.java:2072)
      at javax.swing.text.html.parser.DocumentParser.parse(DocumentParser.java:106)
      at javax.swing.text.html.parser.ParserDelegator.parse(ParserDelegator.java:78)
      at net.semanlink.util.html.HTMLPageDownload.loadDocument(HTMLPageDownload.java:119)
      at net.semanlink.util.html.HTMLDocumentLoader.loadDocument(HTMLDocumentLoader.java:22)
      at net.semanlink.util.html.HTMLDocumentLoader.loadDocument(HTMLDocumentLoader.java:18)
      at net.semanlink.util.html.HTMLPageDownload.<init>(HTMLPageDownload.java:31)
      at net.semanlink.servlet.Action_Bookmark.execute(Action_Bookmark.java:52)
      */

/**
 * On arrive ici après Action_BookmarkForm (qui, autrefois, créait le HTMLDownload (et le mettait
 * ds la session via le Form_Bookmark)
 * 
 * Question des caratères spéciaux : résolu pour le short name, pas pour le path complet // TODO still true?
 */
public class Action_Download extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		SLModel mod = SLServlet.getSLModel();
		String docUri = request.getParameter("docuri");
		SLDocument doc = mod.getDocument(docUri);
		String contextURL = Util.getContextURL(request);
		
		// URIs for bookmarks: d'où est-ce qu'on downloade ?
		SLDocumentStuff stuff = new SLDocumentStuff(doc, mod, contextURL);
		String downloadFromUri = stuff.getHref();
		
		// 2020-03
		if (stuff.getLocalCopy() != null) {
			return error(mapping, request, "This doc already has a local copy. Delete this copy first if you want to download it again.");
		}

		
		// 2020-03 arxiv
		String arxivPdf = Arxiv.url2pdfUrl(downloadFromUri);
		if (arxivPdf != null) {
			downloadFromUri = arxivPdf;
		}		
		
		
		
		
		//
		boolean overwrite = false;
		File file = downloadFile(downloadFromUri, doc.getLabel(), overwrite, mod);
		setSource(docUri, file, mod);
		
		//
		// POST REDIRECT
		// getJsp_Document(doc, request);
		// return mapping.findForward("continue");
		// String redirectURL = contextURL + HTML_Link.docLink(doc.getURI());
		String redirectURL = null;
		if (docUri.startsWith(SLServlet.getServletUrl())) {
			redirectURL = docUri; // hum pb de session, non ?
		} else {
			// pre uris for bookmarks
			redirectURL = contextURL + HTML_Link.docLink(doc.getURI());
		}
		
  	response.sendRedirect(response.encodeRedirectURL(redirectURL));
  	return null;
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute



public static String getShortFilename(String title, String docuri, boolean isHTML) {
	String dotExtension = null;
	if (docuri != null) {
		if (!docuri.endsWith(".")) {
			String sfn = Util.getLastItem(docuri, '/');
			dotExtension = Util.getDotExtension(sfn);
		}
	}
	
	/*
	if ((dotExtension == null) || ("".equals(dotExtension))) {
		if (contentType.indexOf("html") > -1) {
			dotExtension = ".html";
		} else {
			dotExtension = "";
		}
	}
	*/
	if (isHTML) {
		dotExtension = ".html";
	} else {
		if (dotExtension == null) dotExtension = "";
	}
	
	if (title != null) {
		String sfn = title.trim();
		if (sfn.length() > 32) sfn = sfn.substring(0,31);
		if (!("".equals(sfn))) return SLUtils.shortFilenameFromString(sfn) +  dotExtension;
	}
	String x = null;
	if ((docuri != null) && (!("".equals(docuri)))) {
		x = SLUtils.shortFilenameFromString(Util.getLastItem(docuri,'/'));
	}
	if (!("".equals(x))) return x;
	return "untitled.html"; // TODO
}

/**
 * Download et sauve un fichier.
 * Pour en plus dire que le fichier a pour source downloadFromUri, faire :
 * setSource(downloadFromUri, [the returned file], mod);
 * @return the new file.
 * @throws RuntimeException if overwrite is false and file already exists
 */ // pre 2019-03 uris for bookmarks
public static File downloadFile(String downloadFromUri, String title, boolean overwrite, SLModel mod) throws IOException {
	// if (!downloadFromUri.startsWith("http:")) throw new RuntimeException("Not an http uri"); // TODO : on pourrait vouloir faire une copie d'un file
	
	Response res = getResponse(downloadFromUri);
	boolean isHTML = isHTML(downloadFromUri, res);

	File dir = mod.goodDirToSaveAFile(); ////// !!!
	String sfn = getShortFilename(title, downloadFromUri, isHTML); // TODO IMPROVE SEE ActionBookmark
	File saveAs = new File(dir, sfn);
	
	download(downloadFromUri, saveAs, overwrite, res, isHTML);

	return saveAs;
}

public static void download(String downloadFromUri, File saveAs, boolean overwrite, Response res, boolean isHTML) throws IOException {
	if (saveAs.exists()) {
		if (!overwrite) throw new Error400Exception ("A file " + saveAs.toString() + " already exists.");
	}

	if (isHTML) {
		HTMLPageDownload download = new HTMLPageDownload(new URL(downloadFromUri), res);
//		if (HTMLPageDownload.isLeMondePrintPage(downloadFromUri)) {
//			// remplacer le titre ds le html
//			if (title != null) {
//				download.replaceTitle(title);
//			}
//		}
		download.save(saveAs);						
	} else {
		save(res, saveAs);
	}
}

public static Response getResponse(String downloadFromUri) {
	Client simpleHttpClient = SLServlet.getSimpleHttpClient();
	// String contentType = simpleHttpClient.getContentType(downloadFromUri, false);
  WebTarget webTarget = simpleHttpClient.target(downloadFromUri);
  return webTarget.request(MediaType.WILDCARD_TYPE).get(); 
}

public static boolean isHTML(String downloadFromUri, Response res) {
	boolean isHTML = false;
	MediaType m = res.getMediaType();
  if ((m != null) && (m.isCompatible(MediaType.TEXT_HTML_TYPE))) {
  	isHTML = true;
  } else {
		if ((downloadFromUri.endsWith(".html"))||(downloadFromUri.endsWith(".htm"))) isHTML = true;
	}
  return isHTML;
}

static private void save(Response res, File saveAsFile) throws IOException {	
  	File dir = new File(saveAsFile.getParent());
  	if (!dir.exists()) dir.mkdirs();
    OutputStream out = new FileOutputStream(saveAsFile);
    Object o = res.getEntity();
    InputStream in;
	  if (o instanceof InputStream) {
	  	in = (InputStream) o;
	  } else {
	  	out.close();
	  	throw new RuntimeException("No content");
	  }
  	CopyFiles.writeIn2Out(in, out, new byte[1024]);
  	in.close();
  	out.close();
}


/**
 * to state that file is a local copy of docUri
 * @param file
 * @param docUri
 * @param mod
 * @throws URISyntaxException 
 * @throws MalformedURLException 
 */
public static void setSource(String docUri, File file, SLModel mod) throws MalformedURLException, URISyntaxException {
	 String localUri = mod.fileToUri(file);
	 SLDocument localDoc = mod.getDocument(localUri);
	 mod.addDocProperty(localDoc, SLVocab.SOURCE_PROPERTY, docUri);
}
	
} // end Action
