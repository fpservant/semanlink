package net.semanlink.servlet;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.io.UnsupportedEncodingException;

import net.semanlink.semanlink.*;
import net.semanlink.util.*;

import javax.servlet.http.*;

/** Affichage d'un SLDocument */
public class Jsp_Document extends Jsp_Resource {
private SLDocument slDoc;
/*  TODO : voir si on ne peut pas faire mieux : on compute le file, alors qu'il n'y a peut-etre
 *  pas besoin/
/** Not null if this is a file protocol uri or a uri served by this web server.
 * @see fileHasBeenComputed - must be accessed through its getter */
private File file;
/** indique si this.file a déjà été calculé. */
private boolean fileHasBeenComputed = false;
/** uri of containing folder. (temp var, must be accessed through its getter) */
private String folderHREF;
/** si c'est un dossier, liste des docs. (must be accessed through its getter) */
private List docsInFolder;
/** see CoolUriServlet.goPage */
private String pagePathInfo;

//
// CONSTRUCTION
//

protected Jsp_Document(SLDocument slDoc, HttpServletRequest request) throws IOException, URISyntaxException {
	super(slDoc, request);
	this.slDoc = slDoc;
	// System.out.println("Jsp_Document slDoc \n\t" + slDoc.getURI() + "\n\treq.getRequestURL " + request.getRequestURL());
	// computeFile();
}

private void computeFile() throws IOException, URISyntaxException {
	this.file = SLServlet.getSLModel().getFile(this.uri);
	this.fileHasBeenComputed = true;
}


//
//
//

public SLDocument getSLDocument() { return this.slDoc; }

public Bean_KwList prepareParentsList() {
	this.beanKwList.setList(this.slDoc.getKeywords());
	this.beanKwList.setUri(this.slDoc.getURI()); // voir pourquoi faut le mettre (cf removekw). Devrait pas
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
	if (edit()) {
		this.beanKwList.setField("tags");
		this.beanKwList.setContainerAttr("id=\"parentcontainer\"");
	}
	return this.beanKwList;
}

//
//
//

public String getTitle() {
	// List objects = this.slRes.getProperty();
	try {
		return getLabel(this.slDoc);
	} catch (Exception e) { throw new RuntimeException(e) ; }
}

/** ce qui est affiché dans la div "title" de la page : pour aussi mettre un lien vers le doc */
public String getTitleInTitle() throws Exception {
	return "<a href=\"" + getHREF() + "\">" + getTitle() + "</a>";
}

public String getComment() { return this.slDoc.getComment(); }

/**
 * Retourne true s'il s'agit d'un fichier auquel on accède, soit en tant
 * que uri de protocol file, soit en http servi par le webServer.
 */
public boolean isFile() throws IOException, URISyntaxException {
	return (getFile() != null);
}

public boolean isDirectory() throws IOException, URISyntaxException {
	if (!isFile()) return false;
	return getFile().isDirectory();
}

static boolean isDirectory(SLDocument slDoc) throws IOException, URISyntaxException {
	File file = SLServlet.getSLModel().getFile(slDoc.getURI());
	if (file == null) {
		return false;
	} else {
		return file.isDirectory();
	}
}

public boolean isImage() {
	return Util.isImage(this.uri);
}

/** Link to the parent directory, il applicable. */
public String getFolderHREF() {
	if (this.folderHREF ==  null) {
		this.folderHREF = getParentDir(this.uri);
	}
	return this.folderHREF;
}

/** Attention suppose que si uri représente une dir, est "/" terminated. */
private static String getParentDir(String uri) {
	String s;
	if (uri.endsWith("/")) {
		s = uri.substring(0,  uri.length() - 1);
	} else {
		s = uri;
	}
	return s.substring(0,  s.lastIndexOf("/")+1); // bug si uri se termine par 2 /, I think
}

/** Retourne une uri de type file, si le doc est local ou bien servi par notre WebServer. */
public String getLocalHREF() throws IOException, URISyntaxException {
	if (!isFile()) return null;
	File file = this.getFile();
	return FileUriFormat.fileToUri(file);
}

//2019-04 local use of local files
public String getLocalFolderHREF2() throws IOException, URISyntaxException {
	if (!isFile()) return null;
	File file = this.getFile();
	if (file == null) return null;
	// return FileUriFormat.fileToUri(file.getParentFile());
	return SLServlet.getWebServer().getURI(file.getParentFile());
}




public String getFolderPage(HttpServletResponse response) throws MalformedURLException, UnsupportedEncodingException {
	return response.encodeURL(getContextUrl() + HTML_Link.docLink(getFolderHREF()));
}

/** @deprecated */
public String getFolderPage() throws UnsupportedEncodingException {
	// return "/doc/" + java.net.URLEncoder.encode(getFolderHREF(), "UTF-8");
	// return "/showdocument.do?docuri=" + java.net.URLEncoder.encode(getFolderHREF(), "UTF-8");
	return HTML_Link.docLink(getFolderHREF());
}

/** @deprecated use isFile() */
public boolean isLocal() throws IOException, URISyntaxException {
	return (getFile() != null);
}

public File getFile() throws IOException, URISyntaxException {
	if (!fileHasBeenComputed) computeFile();
	return this.file;
}

public String getEditLinkPage() throws UnsupportedEncodingException {
	return "/editdocument.do?docuri=" + java.net.URLEncoder.encode(this.uri, "UTF-8");
}

public String computelinkToThis() throws UnsupportedEncodingException {
	// return HTML_Link.linkToDocument(this.slDoc).getPage(); // 2006-01
	return HTML_Link.htmlLinkPage(this.slDoc);
}


public String getLinkToThis(String action) throws UnsupportedEncodingException {
	String x = HTML_Link.linkToDocument(this.slDoc, action).getPage();
	return x;
}


/** au cas ou on a affaire a un dossier, liste des documents inclus.
 *  Cette methode devrait probablement etre ds SLDocument.
 * @throws URISyntaxException
 * @throws IOException
 *  @deprecated, utiliser la version avec le boolean sort
 */
public List getDocsInFolder() throws IOException, URISyntaxException {
	return getDocsInFolder(true);
}

public List getDocsInFolder(boolean sort) throws IOException, URISyntaxException {
	if (this.docsInFolder == null) {
		SLFolder fol = new SLFolder(getFile(), this.uri, SLServlet.getSLModel());
		this.docsInFolder = fol.getDocList();
	}
	if (sort) {
		String prop = getSortProperty();
		if (SLVocab.HAS_KEYWORD_PROPERTY.equals(prop)) {
			SLUtils.sortDocsByKws(this.docsInFolder);
		} else if (prop != null) {
			SLUtils.sortByProperty(this.docsInFolder, prop);
		}
	}
	return this.docsInFolder;
}

/** retour non trié 
 * Suppose qu'on a bien affaire à un dossier, mais celui-ci peut être servi en tant que fichier
 * ou par le webServer
 */
/*public List computeDocsInFolder() throws URISyntaxException {
	SLFolder fol = new SLFolder(getFile(), this.uri, SLServlet.getSLModel());
	return fol.getDocList();
}*/

/** au cas ou on a affaire a un dossier, calcule la liste des documents inclus. 
 * @throws IOException*/
protected Bean_DocList computeDocList() throws IOException {
	Bean_DocList x = new Bean_DocList();
	try {
		x.setList(getDocsInFolder());
	} catch (URISyntaxException e) { throw new SLRuntimeException(e); }
	return x;
}

/** pertinent seulement au cas ou on a affaire a un dossier. */
public SLKeyword[] getLinkedKeywords() throws IOException, URISyntaxException {
	if (isDirectory()) {
		List docs = getDocsInFolder(false);
		int n = docs.size();
		if (n == 0) return new SLKeyword[0];
		HashSet hs = new HashSet();
		for (int i = 0; i < n; i++) {
			SLDocument doc = (SLDocument) docs.get(i);
			List kws = doc.getKeywords();
			for (int j = 0; j < kws.size(); j++) {
				hs.add(kws.get(j));
			}
		}
		SLKeyword[] x = (SLKeyword[]) hs.toArray(new SLKeyword[0]);
		Arrays.sort(x);
		return x;
	} else {
		// throw new IllegalArgumentException("Méthode définie seulement ds le cas d'un dossier");
		return null;
	}
}

/** clé un linked SLKeyword, data nb d'occurrences.
 *  pertinent seulement au cas ou on a affaire a un dossier. */
public HashMap getLinkedKeywords2NbHashMap() throws Exception {
	if (isDirectory()) {
		List docs = getDocsInFolder(false);
		return SLUtils.getLinkedKeywords2NbHashMap(docs);
	} else {
		// throw new IllegalArgumentException("Méthode définie seulement ds le cas d'un dossier");
		return null;
	}
}

public String getContent() throws Exception {
	return "/jsp/document.jsp";
}

//
// ce doc peut-etre objet de ppté
//
public SLDocument getLocalCopy() throws Exception {
	if (getFile() != null) return null;
	return SLServlet.getSLModel().source2LocalCopy(this.slDoc.getURI());
}

//
//
//

void setPagePathInfo(String pagePathInfo) { this.pagePathInfo = pagePathInfo; }
public String getPagePathInfo() { return this.pagePathInfo; }

// 2006/11 StaticFileServlet ne peut pas servir un dossier
public String getHREF() throws IOException, URISyntaxException {
	if (isDirectory()) {
		return this.request.getContextPath() + HTML_Link.docLink(this.uri);
	}
	
	// 2019-03 uris for bookmarks
  String bookmarkOf = getSLDocument().bookmarkOf();
  if (bookmarkOf != null) {
  	return Util.handleAmpersandInHREF(bookmarkOf);
  }

	// 2019-03 moved from Jsp_Resource
	if (uri.startsWith("file:")) {
		URI u;
		try {
			u = new URI(this.uri);
			String path = u.getRawPath();
			return this.request.getContextPath() + StaticFileServlet.PATH_FOR_FILES_OUTSIDE_DATAFOLDERS + path;
		} catch (URISyntaxException e) { 
			throw new RuntimeException(e) ;
		}
	}
	
  return super.getHREF();
}


//
//
//

// 2012-02 rss article sicg
public String getGoToDocUri() {
	return getUri();
}


}
