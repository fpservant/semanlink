package net.semanlink.servlet;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.io.UnsupportedEncodingException;

import net.semanlink.semanlink.*;
import net.semanlink.semanlink.SLModel.DocMetadataFile;
import net.semanlink.util.*;

import javax.servlet.http.*;

/** Affichage d'un SLDocument */
public class Jsp_Document extends Jsp_Resource {
private SLDocument slDoc;
/** uri of containing folder. (temp var, must be accessed through its getter) */
private String folderHREF;
/** si c'est un dossier, liste des docs. (must be accessed through its getter) */
private List<SLDocument> docsInFolder;
/** see CoolUriServlet.goPage */
private String pagePathInfo;

// 2019-04 uris for bookmraks
private SLDocumentStuff docStuff;

//
// CONSTRUCTION
//

protected Jsp_Document(SLDocument slDoc, HttpServletRequest request) throws IOException, URISyntaxException {
	super(slDoc, request);
	this.slDoc = slDoc;
	this.docStuff = new SLDocumentStuff(slDoc, SLServlet.getSLModel(), this.getContextURL());
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
	try {
		return getLabel(this.slDoc);
	} catch (Exception e) { throw new RuntimeException(e) ; }
}

/** ce qui est affiché dans la div "title" de la page : pour aussi mettre un lien vers le doc */
public String getTitleInTitle() throws Exception {
	return "<a href=\"" + docStuff.getHref(true) + "\">" + getTitle() + "</a>";
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

/** Link to the parent directory, if applicable. */
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


public String getFolderPage(HttpServletResponse response) throws MalformedURLException, UnsupportedEncodingException {
	return response.encodeURL(getContextUrl() + HTML_Link.docLink(getFolderHREF()));
}

/** @deprecated */
public String getFolderPage() throws UnsupportedEncodingException {
	// return "/doc/" + java.net.URLEncoder.encode(getFolderHREF(), "UTF-8");
	// return "/showdocument.do?docuri=" + java.net.URLEncoder.encode(getFolderHREF(), "UTF-8");
	return HTML_Link.docLink(getFolderHREF());
}

public File getFile() throws IOException, URISyntaxException {
	return this.docStuff.getFile();
}

public String getEditLinkPage() throws UnsupportedEncodingException {
	return "/editdocument.do?docuri=" + java.net.URLEncoder.encode(this.uri, "UTF-8");
}

// without the context
public String computelinkToThis() throws UnsupportedEncodingException {
	// return HTML_Link.linkToDocument(this.slDoc).getPage(); // 2006-01
	// return HTML_Link.htmlLinkPage(this.slDoc); // 2019-06-07
	
	// ATTENTION adherence markdown-sl.js replaceLinkFct
	// return CoolUriServlet.DOC_SERVLET_PATH + "/?uri=" + URLEncoder.encode(this.slDoc.getURI(), "UTF-8"); // /doc/?uri=...
	
	// zut, il faut virer le contexte
	return this.docStuff.getAboutHref().substring(getContextURL().length());
}


public String getLinkToThis(String action) throws UnsupportedEncodingException {
	String x = HTML_Link.linkToDocument(this.slDoc, action).getPage();
	return x;
}

//
// FOLDER CASE
//

/** au cas ou on a affaire a un dossier, liste des documents inclus.
 *  Cette methode devrait probablement etre ds SLDocument.
 * @throws URISyntaxException
 * @throws IOException
 *  @deprecated, utiliser la version avec le boolean sort
 */
public List getDocsInFolder() throws IOException, URISyntaxException {
	return getDocsInFolder(true);
}

public List<SLDocument> getDocsInFolder(boolean sort) throws IOException, URISyntaxException {
	if (this.docsInFolder == null) {
		// 2020-08: KO for dirs outside of a data folder (at least when sl:bookmarkO,
		// because uri is not a file:///. We should pass the bookmarkOf, I think)
		SLFolder fol = new SLFolder(getFile(), this.uri, SLServlet.getSLModel());
//		String folderUri = uri;
//		String bof = docStuff.getBookmarkOf();
//		if ((bof != null) && (bof.startsWith("file:///"))) {
//			folderUri = bof;
//		}
//		SLFolder fol = new SLFolder(getFile(), folderUri, SLServlet.getSLModel());
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
		List<SLDocument> docs = getDocsInFolder(false);
		int n = docs.size();
		if (n == 0) return new SLKeyword[0];
		HashSet<SLKeyword> hs = new HashSet<>();
		for (int i = 0; i < n; i++) {
			SLDocument doc = docs.get(i);
			List<SLKeyword> kws = doc.getKeywords();
			for (int j = 0; j < kws.size(); j++) {
				hs.add(kws.get(j));
			}
		}
		SLKeyword[] x = hs.toArray(new SLKeyword[0]);
		Arrays.sort(x);
		return x;
	} else {
		// throw new IllegalArgumentException("Méthode définie seulement ds le cas d'un dossier");
		return null;
	}
}

/** clé un linked SLKeyword, data nb d'occurrences.
 *  pertinent seulement au cas ou on a affaire a un dossier. */
public HashMap<SLKeyword, Integer> getLinkedKeywords2NbHashMap() throws Exception {
	if (isDirectory()) {
		List<SLDocument> docs = getDocsInFolder(false);
		return SLUtils.getLinkedKeywords2NbHashMap(docs);
	} else {
		// throw new IllegalArgumentException("Méthode définie seulement ds le cas d'un dossier");
		return null;
	}
}

//
//
//

public String getContent() throws Exception {
	return "/jsp/document.jsp";
}

//
//
//

// 2020-07 doens't seem to be used
//public SLDocument getLocalCopy() throws Exception {
//	return SLServlet.getSLModel().getLocalCopy(this.slDoc);
//}

//
//
//

//ne sert probablement pas // TODO
void setPagePathInfo(String pagePathInfo) { this.pagePathInfo = pagePathInfo; }
public String getPagePathInfo() { return this.pagePathInfo; }

// 2006/11 StaticFileServlet ne peut pas servir un dossier
// 2019-04 retourne le href de la res online, dans le cas d'un bkmark (pas l'uri du doc ds sl)
public String getHREF() throws IOException, URISyntaxException {
	if (isDirectory()) {
		return this.request.getContextPath() + HTML_Link.docLink(this.uri);
	}
	
  String bookmarkOf = getSLDocument().bookmarkOf(); // 2019-03 uris for bookmarks
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

//
// 2019-04 uris for bookmark
//


// pour tests
public DocMetadataFile getDocMetadataFile() throws IOException, URISyntaxException {
	return SLServlet.getSLModel().doc2DocMetadataFile(this.uri);
}


public SLDocumentStuff getSLDocumentStuff() {
	return docStuff;
}

//
// 2020-07 : add local copy
//

static public SLDocumentStuff localCopyCandidate(String contextUrl) throws Exception { // 2020-07
  SLModel mod = SLServlet.getSLModel();
  File currentDir = mod.goodDirToSaveAFile();	            
  File[] files = currentDir.listFiles();
  // sort by date, newest first
  Arrays.sort(files, new Comparator<File>(){
    public int compare(File f1, File f2) {
        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
    } 
  });
  for (File f : files) { // we only are interested with the most recent, so not really a loop
  	
  	// hum, le plus récent, ce n'est pas forcément génial :
  	// ne marche pas si on a copié-collé un fichier à partir du disque
  	// (le fichier garde son last-modified à sa valeur originale).
  	// Mais ca devient compliqué pour faire mieux : garder un état de l'activ folder
  	// et voir ses changeemnts.
  	
  	// est-ce que c un pdf ou html (pas sl.rdf !)
  	String nam = f.getName();
  	if ("sl.rdf".equals(nam)) continue;
  	if ((nam.startsWith("sl-")) && (nam.endsWith(".rdf"))) continue;
  	// est-ce que ce n'est pas déjà un doc (local)?
  	SLDocument sldoc = mod.smarterGetDocument(mod.filenameToUri(f.getPath()));
  	if (mod.existsAsSubject(sldoc)) { // todo check si c bien le bon test à faire
  		return null;
  	}
    SLDocumentStuff docStuff = new SLDocumentStuff(sldoc, mod, contextUrl);
  	// est-ce qu'il n'est pas déjà source de qlqu'un?
    if (docStuff.getSource() != null) {
    	return null;
    }
    return docStuff;
  }
  return null;
}

//
//
//

public Bean_DocList relatedDocs() {
	Bean_DocList x = new Bean_DocList();
	List<SLDocument> docs = this.slDoc.relatedDocs();
	sort(docs);
	x.setList(docs);
	x.setShowKwsOfDocs(true, null);
	return x;
}
}
