/* Created on Apr 27, 2019 
 * 
 * To regroup methods related to the access to document's page, the bookmark, the local copy
 * (things related to "uris for bookmarks"
 */
package net.semanlink.semanlink;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import net.semanlink.servlet.CoolUriServlet;
import net.semanlink.servlet.SLServlet;
import net.semanlink.servlet.StaticFileServlet;

public class SLDocumentStuff {
private SLDocument doc;
private SLModel mod;
private String contextURL;

private String bookmarkOf; // use getter
private boolean bookmarkOfComputed = false;
private File file; // use getter
private boolean fileComputed;
private String aboutHref; // use getter
private boolean aboutHrefComputed= false;
private String href; // use getter
private boolean hrefComputed = false;
private SLDocument source; // use getter
private boolean sourceComputed = false;
private SLDocument localCopy; // use getter
private boolean localCopyComputed = false;
private HrefPossiblyOpeningInDestop localCopyLink; // use getter
private boolean localCopyLinkComputed = false;
private String localCopyPage; // use getter
private boolean localCopyPageComputed = false;
private SLDocumentStuff localCopyStuff; // use getter
private boolean localCopyStuffComputed = false;
private boolean isNoteComputed = false;
private boolean isNote; // use getter
private String rawMarkdownUrl; // use getter
private boolean rawMarkdownUrlComputed = false;


/**
 * @param doc
 * @param mod
 * @param contextURL eg. http://127.0.0.1:8080/semanlink or http://www.semanlink.net cf. Util.getContextURL(HttpServletRequest)
 */
public SLDocumentStuff(SLDocument doc, SLModel mod, String contextURL) {
	this.doc = doc;
	this.mod = mod;
	this.contextURL = contextURL;
}

//
//
//

// TODO CHANGER CES NOMS DE METHODE

/**
 * From uri of document as used in RDF to URL of page to display
 * BEWARE: ONLY FOR MODERN THINGS (post uris for bookmarks) -- and intended this way
 * (THAT IS: uri ASSUMED TO START WITH /doc/)
 * Basically transforms .../doc/... to .../document/... - except in the .md file case
 * @param contextURL eg. http://127.0.0.1:8080/semanlink or http://www.semanlink.net cf. Util.getContextURL(HttpServletRequest)
 * @return null if uri not of a "modern" document
 * @throws URISyntaxException
 * @throws IOException 
 */
private String docUri2Page() throws IOException, URISyntaxException {
	String uri = doc.getURI();
	String deb = contextURL + CoolUriServlet.DOC_SERVLET_PATH + "/";
	if (!uri.startsWith(deb)) return null;
	String x = null;
	if (isMarkdown(uri)) {
		x = uri;
	} else if (isDir()) {
		x = uri;
	} else {
		x = contextURL + StaticFileServlet.PATH + "/" + uri.substring(deb.length());
	}
	return x;
}

private static String docUri2DownloadUrl(String uri, String contextURL) {
	String deb = contextURL + CoolUriServlet.DOC_SERVLET_PATH + "/";
	if (!uri.startsWith(deb)) return null;
	return contextURL + StaticFileServlet.PATH + "/" + uri.substring(deb.length());
}


/**
 * MAL NOMMEE TODO
 * BEWARE: ONLY FOR MODERN THINGS (post uris for bookmarks) -- and intended this way
 * @param contextURL eg. http://127.0.0.1:8080/semanlink or http://www.semanlink.net cf. Util.getContextURL(HttpServletRequest)
 * @return null if uri not served by StaticFileServlet
 */
public static String page2DocUri(String uri, String contextURL) {
	String deb = contextURL + StaticFileServlet.PATH + "/";
	if (uri.startsWith(deb)) {
		return contextURL + CoolUriServlet.DOC_SERVLET_PATH + "/" + uri.substring(deb.length());
	}
	return null;
}

//
//
//

/**
 * Return doc.bookmarkOf() -- that is, normally (if not overrriden), the value of the bookmarkOf property
 * @see SLDocument.bookmarkOf
 */
public String getBookmarkOf() {
	if (!bookmarkOfComputed) {
		bookmarkOfComputed = true;
		bookmarkOf = doc.bookmarkOf();
	}
	return bookmarkOf;
}

// doesn't check that it exists -- ET ATTENTION, SUPPOSE QU'ON N'A PAS AFFAIRE A UNE NOTE
// 2020-01 localFilesOutOfDatafolders : now handles the case of bookmarks on files outside datafolder
public File getFile() throws IOException, URISyntaxException {
	if (fileComputed) return file;
	fileComputed = true;
	String bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		
		// 2020-01 localFilesOutOfDatafolders
//    // TODO ceci n'est OK que si les bookmarks sont seulement utilisés pour les pages externes
//  	file = null;
//	  return null;
		if (bookmarkOf.startsWith("file:")) {
			file = mod.getFile(bookmarkOf);
			return file;			
		}	
	}
	
	if (isNote()) {
		file = null;
		return null;
	}
	
	file = mod.getFile(doc.getURI());
	return file;
}

public boolean isNote() {
	if (isNoteComputed) return isNote;
	isNoteComputed = true;
	isNote = Note.isNote(doc.getURI());
	return isNote;
}

/**
 * Link to the actual document's page (online or local):
 * the page you want to see when you want to really access the doc (not its surrogate in semanlink)
 * (what we follow when clicking on the title of the sldoc)
 * 
 * WITHOUT withOpenInDesktop
 */
public String getHref() throws IOException, URISyntaxException {
	if (hrefComputed) return href;
	hrefComputed = true;
	href = null;
	
	bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		href = bookmarkOf;
		// 2019-08 to support the display of external .md file
		if (this.isMarkdown(href)) {
			href = doc.getURI();
		}
		return href;
	}
	
	// is it a uri served by us, or a foreign one, such as hypersolutions.fr?
	// Commencer par éliminer les uris qui ne seraient pas servies par nous?
	// Sur les uris post modifs uris for bookmarks, ne devrait pas arriver - donc, est-ce que ça vaut la peine ?
	// (pour pouvoir le faire, il faut avoir les infos request, et l'url de le servlet)
	// Mais si on ne le fait pas, il faut calculer le local file éventuel
	// TODO A VOIR
	
	// HUM : TODO A VOIR : on calcule file, mais on ne s'en sert pas
	// HUM 2 : ON NE CHECKE PAS EXISTENCE DU FILE
	File f = getFile();
	if (f == null) {
		// NOT A LOCAL FILE
		href = doc.getURI();
		return href;
	}
	
	// boolean b = f.isDirectory();
	// System.out.println("File: " + file + " isDir: " + f.isDirectory());
	// LOCAL FILE
	// f not null, but does it exist? Should we check?
	
	// TODO ? vérifier que ca correspond bien à un file ?
	String uri = doc.getURI();
	href = docUri2Page(); // if uri of the form /doc/, replace by /document/ (except if markdown)
	if (href == null) {
		// uri not of the form /doc/, (or markdown)
		href = uri;
	}
			
	return href;
}

/**
 * @deprecated use either getHref() for withOpenInDesktop false, getHrefPossiblyOpeningInDestop(true).href() otherwise 
 */
public String getHref(boolean withOpenInDesktop) throws IOException, URISyntaxException {
	HrefPossiblyOpeningInDestop x = getHrefPossiblyOpeningInDestop(withOpenInDesktop);
	return x.href();
}

public HrefPossiblyOpeningInDestop getHrefPossiblyOpeningInDestop(boolean withOpenInDesktop) throws IOException, URISyntaxException {
	withOpenInDesktop = withOpenInDesktop && SLServlet.canOpenLocalFileWithDesktop();
	String href = getHref();
	if (!withOpenInDesktop) return new HrefPossiblyOpeningInDestop(href, false);
	File f = getFile();
	if (file == null) return new HrefPossiblyOpeningInDestop(href, false);
	withOpenInDesktop = withOpenInDesktop && SLServlet.mayOpenLocalFileWithDesktop(f);
	
	// 2020-01 localFilesOutOfDatafolders
	String bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		if (bookmarkOf.startsWith("file:")) {
			withOpenInDesktop = true;
			href = doc.getURI();
			// return new HrefPossiblyOpeningInDestop(doc.getURI(), true);
		}
	}

	
	// HUM TODO CHECK - doit falloir vérifier qu'on est bien ds cas /doc/ avec md ou ds cas /document/
	if (withOpenInDesktop) {
		if (href.startsWith(contextURL)) {
			href = hrefWithOpenInDesktop(href);
			return new HrefPossiblyOpeningInDestop(href, true);
		}
	}
	return new HrefPossiblyOpeningInDestop(href, false);
}

private static String hrefWithOpenInDesktop(String href) {
	if (href.indexOf("?") > -1) {
		href += "&openInDesktop=true";
	} else {
		href += "?openInDesktop=true";			
	}
	return href;
}

/**
 * the link to the "about" page == the page in semanlink about the doc
 * In "modern" way of doing, the uri of the SLDoc -- but not with the old way of doing
 */
public String getAboutHref() throws UnsupportedEncodingException {
	if (aboutHrefComputed) return aboutHref;
	aboutHrefComputed = true;
	String bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		aboutHref = doc.getURI();		
	} else {
		// not a (new kind of) bookmark
		aboutHref = oldAboutHref(doc.getURI());
	}
	return aboutHref;
}

/**
 * the link to the "about" page == the page in semanlink about the doc 
 * FOR DOCUMENTS BEFORE the "uri for bookmarks" change
 * @param uri : doc.getURI() of such an old doc -- typically, the url of the bookmark
 * (for a new doc, would be the bookmarkOf)
 */
private String oldAboutHref(String uri) throws UnsupportedEncodingException {
	String x = null;
	if (uri.startsWith(contextURL + CoolUriServlet.DOC_SERVLET_PATH + "/")) {
		x = uri;
	
	} else { // (at least in particular) a bookmark to the outside world (eg "www.hypersolutions.fr")
		
		// handle the case of a page such as .../document/... (happens when we click the bookmarklet on a local copy)
		x = page2DocUri(uri, contextURL); // not null if .../document/...
		
		if (x == null) { 
			// (probably) a bookmark to the outside world (eg "www.hypersolutions.fr")
			// cf HTML_Link.docLink
			x = contextURL + CoolUriServlet.DOC_SERVLET_PATH + "/?uri=" + URLEncoder.encode(uri, "UTF-8"); // /doc/?uri=...
		}
	}
	return x;
}

public SLDocument getSource() throws Exception {
	if (sourceComputed) return source;
	sourceComputed = true;
	source = mod.doc2Source(doc.getURI());
	return source;
}

// was in Jsp_Document
// ATTENTION, c'est un doc - pas forcément le fichier lui-même
// Voir getLocalCopyHref pour le lien vers le fichier
public SLDocument getLocalCopy() throws Exception {
	if (localCopyComputed) return localCopy;
	localCopyComputed = true;
	
	// THIS IS VERSION B4 2019-03
	
	// this was OK when docs (bookmarks created for an internet url) had that internet url as uri
	// if (getFile() != null) return null; // si doc local, pas de local copy
	// return SLServlet.getSLModel().source2LocalCopy(this.slDoc.getURI());
	
	
	
	// TODO REVOIR LA SUITE
	//
	// EN DEFINITIVE, CE QUI PASSE C LE 3
	// (comme avant MAIS IL A FALLU VIRER LE TEST getFile() != null)
	//
	// VOIR AUSSI CE QUI SE PASSE DS docline.jsp
	
	
  // But now: the local copy may still be linked to the internet url
	String bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		localCopy = mod.source2LocalCopy(bookmarkOf);
		if (localCopy != null) {
			return localCopy;
		}
	}
	
	// not a post 2019-03 bkm with local copy linked to bookrmaked url
	// -> if there is a local copy, it is linked to doc
	
	// 2019-03 en fait, faut virer ce test
	// if (getFile() != null) return null; // si doc local, pas de local copy // ATTENTION, ce test retourne qlq chose si servi par webserver - donc pas à mettre plu shat

	localCopy = mod.source2LocalCopy(doc.getURI()); // recherche basée sur inv(dc:source)
	// System.out.println("getLocalCopy 3 " + slDoc.getURI());
	if (localCopy != null) {
		return localCopy;
	}
	// System.out.println("getLocalCopy NOT FOUND");
	return null;
}

public HrefPossiblyOpeningInDestop getLocalCopyLink() throws Exception {
	if (localCopyLinkComputed) return localCopyLink;
	localCopyLinkComputed = true;
	SLDocumentStuff stuff = getLocalCopyStuff();
	if (stuff == null) {
		localCopyLink = null;
		return localCopyLink;
	}
	localCopyLink = stuff.getHrefPossiblyOpeningInDestop(true); // true : TODO
	return localCopyLink; 
}

// to store the href plus "is it an opening in desktop or not?"
static public class HrefPossiblyOpeningInDestop {
	private String href;
	private boolean openingInDesktop;
	// you must pass the right href at creation: the simple one when not opening in desktop, the modified one when yes
	HrefPossiblyOpeningInDestop(String href, boolean openingInDesktop) {
		this.href = href;
		this.openingInDesktop = openingInDesktop;
	}
	public boolean openingInDesktop() { return this.openingInDesktop ; }
	public String href() { return this.href ; }
}

// the page in sl giving info about the locl copy
public String getLocalCopyPage() throws Exception {
	if (localCopyPageComputed) return localCopyPage;
	localCopyPageComputed = true;
	SLDocumentStuff stuff = getLocalCopyStuff();
	if (stuff == null) {
		localCopyPage = null;
		return localCopyPage;
	}
	localCopyPage = stuff.getAboutHref();
	return localCopyPage;
}

private SLDocumentStuff getLocalCopyStuff() throws Exception {
	if (localCopyStuffComputed) return localCopyStuff;
	localCopyStuffComputed = true;
	SLDocument localCopy = getLocalCopy();
	if (localCopy == null) {
		localCopyStuff = null;
		return null;
	}
	localCopyStuff = new SLDocumentStuff(localCopy, mod, contextURL);
	return localCopyStuff;
}

// TODO CHECK
/**
 * null if not a markdown doc
 * @throws URISyntaxException 
 * @throws IOException 
 */
public String getRawMarkdownUrl() throws IOException, URISyntaxException {
	if (rawMarkdownUrlComputed) return rawMarkdownUrl;
	rawMarkdownUrlComputed = true;
	
  // 2019-08 to support the display of external .md file
	String bookmarkOf = getBookmarkOf();
	if (bookmarkOf != null) {
		if (isMarkdown(bookmarkOf)) { // assuming bookmarks are external
			rawMarkdownUrl = bookmarkOf;
		} else {
			rawMarkdownUrl = null;
		}
		return rawMarkdownUrl;
	}

	// HUM HACK PAS BON // TODO
	
	rawMarkdownUrl = getHref();
	
	if (!isMarkdown(href)) {
		rawMarkdownUrl = null;
		return rawMarkdownUrl;
	}

	File f = getFile();
	if (f == null) {
		// 2019-08 : if it is a md from the outside, we want to be able to display it
		// and this prevent it:
		// rawMarkdownUrl = null;
		return rawMarkdownUrl;		
	}
	
	// a local markdown file
	
	String uri = doc.getURI();
	rawMarkdownUrl = docUri2DownloadUrl(uri, contextURL);
	return rawMarkdownUrl;
}

public static boolean isMarkdown(String uri) {
	return uri.endsWith(".md");
}

// 2019-04 local use of local files
// uniquement s'il s'agit d'un doc local
// voir parentOfRdfStuff
public String uriOfParentFolder() throws IOException, URISyntaxException {
	File f = getFile();
	if (f == null) return null;
	return SLServlet.getWebServer().getURI(file.getParentFile()); // THE WS QUESTION
}

public String uriOfParentFolder(boolean withOpenInDesktop) throws IOException, URISyntaxException {
	String href = uriOfParentFolder();
	if (href == null) return null;
	withOpenInDesktop = withOpenInDesktop && SLServlet.canOpenLocalFileWithDesktop();
	if (withOpenInDesktop) {
		href = hrefWithOpenInDesktop(href);
	}
	return href;
}

public boolean isDir() throws IOException, URISyntaxException {
	File f = getFile();
	if (f == null) return false;
	return f.isDirectory();
}

//
//
//

// to create a link to folder containing sl.rdf file
// Hum voir uriOfParentFolder
public SLDocumentStuff parentOfRdfStuff() throws IOException, URISyntaxException {
  // SLModel.DocMetadataFile metadataFile = jsp.getDocMetadataFile();
  SLModel.DocMetadataFile metadataFile = SLServlet.getSLModel().doc2DocMetadataFile(this.doc.getURI());
  File folder = metadataFile.getFile().getParentFile();
  String uriOfParentOfRdfFile = mod.fileToUri(folder);
  if (!uriOfParentOfRdfFile.endsWith("/")) uriOfParentOfRdfFile += "/";
  return new SLDocumentStuff(mod.getDocument(uriOfParentOfRdfFile), mod, contextURL);
}

//
//
//

/** pour vérifier que des anciens liens (/doc/?uri=...) fonctionnent encore si on
 * a des docs de nlle forme, avec le bookmarkOf documenté)
 * En effet, si je me mets à transformer les anciens docs en nouveaux, va se poser le pb des
 * liens au sein de comments : il faudrait qu'ils marchent encore)
 * @throws UnsupportedEncodingException 
 * 
 * Pas pour être utilisé véritablement
 */
public String oldAboutHref() throws UnsupportedEncodingException {
	String bookmark = getBookmarkOf();
	if (bookmark != null) {
		return oldAboutHref(bookmark);
	} else {
		return oldAboutHref(doc.getURI());
	}
}

}
