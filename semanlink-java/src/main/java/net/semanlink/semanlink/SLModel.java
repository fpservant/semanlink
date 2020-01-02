// pb avec getBase sur un fichier servi par http (rechercher mais non ! au moins pas si on est en http !)
// voir getThesaurus (cf bug avere) / getLoaderDocsFile

// getBase cas docs file qui n'est pas http (voir JModel.getJFileModel4Docs) :
// reprendre tout ça en voyant bien que c'est un DocsFile (un couplus fichier - base)
// qu'il est tjrs question de définir. Remplacer getBase, etc... par une méthode
// retournant un DocsFile - c'est en gros ce qu'on a fait ici un peu.
// attention aux meth getDocsFile, qui parciurent la liste ouverte : de nature un peu différente

// btw : ne faut-il pas se méfier de accentcomposer ici aussi ???

/*
 * Gestion des copies locales d'uri http.
 * On enregistre la copie locale dont on documente la source.
 * Sur la page de l'uri http, on veut avoir accés à la copie locale, cad
 * le doc qui a sa ppté "source" valant cette uri http. Appeler la méthode getDocumentsList(String, String)
 * avec propertyUri valant "source" et objectUri valant l'uri http
 * (Pourquoi ne pas avoir affecté à l'uri http une ppté "hasLocalCopy" ?
 * parce qu'on serait géné pour lui donner sa valeur : uri file, on ne pourrait plus bouger le fichier.
 * Ceci dit, si le fichier a été mis sur cédé et n'est pas accessible, on ne saura même pas qu'il existe)
 * @param propertyUri peut être null
 * @param objectUri
 */

package net.semanlink.semanlink;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
/*
 * Le "modele" Semanlink.
 * <P>
 * <B>Création de doc</B>
 * Un doc n'existe (au sens semanlink), que par les statements dans lesqules il intervient.
 * <B>Au sujet des Label de Keywords</B>
 * J'utilise en ce moment RDFS.Label, mais ce n'est peut-etre pas une bonne idee.
 * </P><P>
 * <B>Au sujet des fonctions getResources() et createResources de Model</B>
 * Il y a un probleme : getResource retourne toujours (? du moins avec ModelMem) une Resource,
 * alors qu'on s'attendrait à ce qu'elle retourne null si la Resource n'est pas presente dans le Model.
 * Et c'est tres genant du coup de ne pas pouvoir le savoir
 * (d'autant que la javadoc dit de n'utiliser getResource que si la Resource existe dans le Model.
 * A mon avis, ils ont merde sur ce coup là.
 * Du coup, je suis oblige, là où j'esperais pouvoir utiliser getResource puis tester son retour
 * (vis à vis de null) de systématiquement faire un CreateResource.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.jena.shared.JenaException;

import net.semanlink.metadataextraction.MetadataExtractorManager;
import net.semanlink.servlet.Action_Download;
import net.semanlink.servlet.HTML_Link;
import net.semanlink.sljena.modelcorrections.ModelCorrector;
import net.semanlink.util.CopyFiles;
import net.semanlink.util.FileUriFormat;
import net.semanlink.util.YearMonthDay;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * The semanlink "model". 
 * 
 * Includes in fact two distinct RDF models: one for documents, the other for tags. 
 */
public abstract class SLModel implements SLVocab {
//
// ATTRIBUTS
//

private String modelUrl;

/** la liste des SLThesaurus ouverts. */
protected Vector thesauri = new Vector();
/** la liste des fichiers kws. Les elts sont des KwsFile
* Sert pour les corrections */
protected Vector openKWsFiles = new Vector();
/** la liste des fichiers de docs. Les elts sont de classe DocsFile 
 * Sert pour les corrections
 * Pbs : 
 * 1) les fichiers créés après le load ne sont pas dedans
 * 2) les fichiers mentionnés ds les fichiers à charger ne sont pas dedans s'ils n'existent pas
 */
protected Vector openDocsFiles = new Vector();
/** List of loaded SLDataFolder */
protected Vector dataFolderList = new Vector();
private boolean dataFolderListSorted = false;
/** Fichier kws utilisé par défaut. */
/// private String defaultKwsFile;
/** Thesaurus utilisé par défaut. */
private SLThesaurus defaultThesaurus;
private SLDataFolder defaultFolder;
/** if null, use defaultFolder. */
private SLDataFolder bookmarkFolder;
/** To store notes.  */  
private SLDataFolder notesFolder;

private WebServer webServer;
private MetadataExtractorManager metadataExtractorManager;
/** use getter! */
protected ThesaurusIndex thesaurusIndex;
// this is not very good: imagine you're french and you install on a server in the US // TODO
static public CharConverter converter = new CharConverter(Locale.getDefault());
static private WordsInString wordsInString = new WordsInString(false, true);
/** use getter! */
private SLKeyword favori;
private boolean isFavoriComputed = false;

public SLModel() {}
/** can be called once everything is loaded, to initialize every attributes that are else computed only on request. */
public void endInit() {
	this.getThesaurusIndex();
}

public String toString() { return "SLModel " + getModelUrl(); }
public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }
public String getModelUrl() { return this.modelUrl; }

public SLThesaurus getDefaultThesaurus() { return this.defaultThesaurus; }
public void setDefaultThesaurus(SLThesaurus th) { this.defaultThesaurus = th; }

public SLDataFolder getDefaultFolder() { return this.defaultFolder; }
public void setDefaultDataFolder(SLDataFolder defaultFolder) { this.defaultFolder = defaultFolder; }

public SLDataFolder getBookmarkFolder() { 
	if (this.bookmarkFolder != null) return this.bookmarkFolder;
	return getDefaultFolder();
}
public void setBookmarkFolder(SLDataFolder f) {
	/*if (f != null) {
		if (!(f.exists())) throw new IllegalArgumentException("Value given to bookmarkFolder, \"" + f + "\", doesn't exist.");
		if (!(f.isDirectory())) throw new IllegalArgumentException("Value given to bookmarkFolder, \"" + f + "\", is not a directory.");
	}*/
	this.bookmarkFolder = f;
}

public SLDataFolder getNotesFolder() { return this.notesFolder; }
public void setNotesFolder(SLDataFolder f) {
	/*if (!(f.exists())) throw new IllegalArgumentException("Value given to notesFolder, \"" + f + "\", doesn't exist.");
	if (!(f.isDirectory())) throw new IllegalArgumentException("Value given to notesFolder, \"" + f + "\", is not a directory.");*/
	this.notesFolder = f;
}

public void setWebServer(WebServer webServer) { this.webServer = webServer; }
private WebServer getWebServer() { return this.webServer; }

//
// URI TO DOC OU KW
//

/** Retourne le SLDocument (la ressource au sens RDF) correspondant à l'uri passée en argument.
 *  Ne retourne jamais null, même si le document "n'existe pas" dans le model (c'est à dire
 *  si aucun statement ne lui corrrespond). Ne modifie pas le model.
 *  (Creer une fonction isNewDocument (ou isEmptyDocument) pour discerner ces cas la ? (a posteriori))
 * 
 * 	Pour savoir si un doc existe, on peut regarder existsAsSubject // Attention, pre 2019-03 uris for bookmarks
 *  @see existsAsSubject(SLDocument)
 *  
 *  Il faut mieux smarterGetDocument si l'on souhaite retomber sur ses pieds sur des pbs genre uri http vs https
 *  @see smarterGetDocument(String)
 */
abstract public SLDocument getDocument(String uri);
/** Retourne true ssi doc intervient dans au moins un statement en tant que sujet. */
abstract public boolean existsAsSubject(SLDocument doc); // pertinent en pre 2019-03 uris for bookmarks

/**
 * the SLDocument corresponding to the url of a bookmark (url on the web)
 * or null if it doesn't exist yet
 * @since 0.6
 */ 
public SLDocument bookmarkUrl2Doc(String bookmarkUrl) throws Exception { // 2019-03 uris for bookmarks
	List al = getDocumentsList(SLVocab.SL_BOOKMARK_OF_PROPERTY, bookmarkUrl);
	if ((al == null) || (al.size() == 0)) {
		// peut-être https alors qu'on avait stocké http. Cool URIs don't change, they say
		if (bookmarkUrl.startsWith("https://")) {
			bookmarkUrl = "http://" + bookmarkUrl.substring(8);
			al = getDocumentsList(SLVocab.SL_BOOKMARK_OF_PROPERTY, bookmarkUrl);
		}
	}
	if ((al == null) || (al.size() == 0)) return null;
	return (SLDocument) al.get(0);
}






/** Returns the doc corresponding to uri. 
 *  if uri's protocol is file:<OL>
 *  	<LI>if this file is served by this.webServer, returns the http document.</LI>
 *  	<LI>else, returns the file document.</LI>
 *  </OL>
 *  
 *  if https uri and doesn't exist as subjetcs, checks whether the http one exist,
 *  switch to it if it is the case. (URIs suck)
 *  (and vice versa)
 */
public SLDocument smarterGetDocument(String uri) throws URISyntaxException {
	SLDocument x = getDocument(uri);
	if (uri.startsWith("file:")) {
		if (!existsAsSubject(x)) {
			WebServer ws = getWebServer();
			if (ws != null) {
				File f = fileUri2File(uri);
				String httpUri = ws.getURI(f);
				if (httpUri != null) {
					x = getDocument(httpUri);
				}
			}
		}
	} 
	
	if (!existsAsSubject(x)) {
		if (uri.startsWith("https://")) {
			// peut-être https alors qu'on avait stocké http. Cool URIs don't change, they say
			uri = "http://" + uri.substring(8);
			SLDocument x2 = getDocument(uri);
			if (existsAsSubject(x2)) {
				x = x2;
			}
		} else if (uri.startsWith("http://")) {
			// ou l'inverse
			uri = "https://" + uri.substring(7);
			SLDocument x2 = getDocument(uri);
			if (existsAsSubject(x2)) {
				x = x2;
			}			
		}
	}

	return x;
}

/** Retourne la local copy du doc sourceUri, ou null s'il n'en a pas. 
 *  (ou - ce qui revient au même - le doc local dont sourceUri est la source) 
 *  ATTENTION : c'est le "doc" qui est retourné, pas forcément le fichier lui-même. */
public SLDocument source2LocalCopy(String sourceUri) throws Exception {
	// 2019-03 uris for bookmarks
	// this, OK before 2019-03, sourceUri étant l'url internet du bookmark
	List al = getDocumentsList(SLVocab.SOURCE_PROPERTY, sourceUri);
	if ((al == null) || (al.size() == 0)) {
		// peut-être https alors qu'on avait stocké http. Cool URIs don't change, they say
		if (sourceUri.startsWith("https://")) {
			sourceUri = "http://" + sourceUri.substring(8);
			al = getDocumentsList(SLVocab.SOURCE_PROPERTY, sourceUri);
		}
	}
	if ((al == null) || (al.size() == 0)) return null;
	return (SLDocument) al.get(0);
}


/** @since 2019-03 uris for bookmark */ // was in Jsp_Document
public SLDocument getLocalCopy(SLDocument slDoc) throws Exception {

	// 2019-03 uris for bookmarks
	
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
	
	
  // But now: the local copy may be linked to the internet url
	SLDocument x = null;
	String url = slDoc.bookmarkOf();
	if (url != null) {
		x = source2LocalCopy(url);
		// System.out.println("getLocalCopy 1 " + url); // TODO REMOVE
		if (x != null) {
			return x;
		}
	}
	
	// not a post 2019-03 bkm, or local copy linked to 2019-03 bkm
	
//	x = SLServlet.getSLModel().doc2Source(this.slDoc.getURI());
//	System.out.println("Jsp_Document getLocalCopy 2 " + this.slDoc.getURI()); // TODO REMOVE
//	if (x != null) {
//		return x;
//	}
	
	// pre 2019-03
	
	// 2019-03 en fait, faut virer ce test
	// if (getFile() != null) return null; // si doc local, pas de local copy // ATTENTION, ce test retourne qlq chose si servi par webserver - donc pas à mettre plu shat

	x = source2LocalCopy(slDoc.getURI());
	// System.out.println("getLocalCopy 3 " + slDoc.getURI()); // TODO REMOVE
	if (x != null) {
		return x;
	}
	// System.out.println("getLocalCopy NOT FOUND"); // TODO REMOVE
	return null;
}
















public SLDocument doc2Source(String docUri) throws Exception { // pas optimisé du tout @TODO
	List al = getDocumentsList(SLVocab.SOURCE_PROPERTY, docUri, true);
	if (al.size() == 0) return null;
	return (SLDocument) al.get(0);
}


/** null if docUri not the uri of a md file 
 * @param contextUrl @see Util.getContextURL(HttpServletRequest) ou Jsp_Page.getContextURL()
 */
public String doc2markdownHref(String contextUrl, String docUri) throws IOException, URISyntaxException {
	if (!docUri.endsWith(".md")) return null;
	if (getFile(docUri) == null) return null;
	// ATTENTION adherence markdown-sl.js replaceLinkFct
	// return contextUrl + HTML_Link.docLink(docUri); // http://127.0.0.1:8080/semanlink + /doc/?uri=...
	
	return contextUrl + HTML_Link.docLink(docUri, getWebServer()); // 2017-09-20
}


// attention, ajout rapide en 2003-07-01 pour servlet
/** Attention, ne retourne jamais null. 
 *  Ne tente pas de résoudre les alias.
 *  @see kwExists(String)
 *  @see resolveAlias(String) */
abstract public SLKeyword getKeyword(String uri);

//
//
//

/**Retourne les docs affectés d'une liste de keywords. 
 * Attention, ne se préoccupe pas de descendants.`
 * Si on veut avoir les docs des descendants, utiliser la méthode de SLTree
 */
public Collection findDocs(List kws) {
	Collection x = new ArrayList();
	int nKws = kws.size();
	if (nKws == 0) throw new IllegalArgumentException("Empty list of keywords.");
	SLKeyword kw = (SLKeyword) kws.get(0);
	x = kw.getDocuments();
	if (nKws == 1) return x;
	
	x = new HashSet(x);
	for (int i = 1; i < nKws; i++) {
		List docs = ((SLKeyword) kws.get(i)).getDocuments();
		x.addAll(docs);
	}
	return x;
}

//
// LOADING SL FILES
//





/**La lecture/ecriture de fichiers depend de la facon de convertir une uri relative en absolue :
 * celle-ci peut etre locale (de type file) ou de type http, selon la facon dont le client va
 * pouvoir y acceder.
 * @param base utilisée pour la lecture dans le cas d'un fichier de documents. Must not be null
 * @param thesaurusURI Si le thésaurus correspondant n'est pas chargé, le charge
 * Si null, getDefaultThesaurus sera utilisé
 * @see load(File, String, SLThesaurus)
 */
public SLDataFolder loadSLDataFolder(File file, String base, String thesaurusURI, LoadingMode loadingMode) throws IOException {
	if (!file.exists()) throw new RuntimeException(file + " doesn't exist.");
	if (!file.isDirectory()) throw new RuntimeException(file + " is not a directory.");
	SLThesaurus th;
	if (thesaurusURI == null) {
		th = this.getDefaultThesaurus();
	} else {
		th = this.getThesaurus(thesaurusURI);
		if (th == null) {
			throw new RuntimeException("Unknown thesaurus: " + thesaurusURI);
		}
	}
	return load(file, base, th, loadingMode);
}

/**Only loads sl.rdf or slkws.rdf files.
 * If file is a dir, loads all sl.rdf and slkws.rdf in dir and its subdirectories.
 * @param file supposed existing
 * @param base used when reading the file. Must not be null
 * (could it be stored in the file? at this time, seems that you cannot do this with jena)
 * (in fact you can, but seems difficult then to change it if you want to)
 * @param thesaurus used to define the thesaurus to be used by default when transforming a tag's label into an uri
 * must not be null (you can use getDefaultThesaurus() if you want).
 */
private SLDataFolder load(File file, String base, SLThesaurus thesaurus, LoadingMode loadingMode) throws IOException {
	// doc de dataFolderList
	if (file == null) throw new IllegalArgumentException("file must not be null");
	SLDataFolder x = new SLDataFolder(file, base, thesaurus, loadingMode);
	this.dataFolderList.add(x);
	this.dataFolderListSorted = false;
	load(file, x);
	return x;
}

private void load(File file, SLDataFolder dataFolder) throws IOException {
	SLThesaurus thesaurus = dataFolder.getDefaultThesaurus();
	LoadingMode loadingMode = dataFolder.getLoadingMode();
	
	if (file.isDirectory()) {
		if (loadingMode.isAllSubDirsMode()) {
			String[] list = file.list();
			for (int i = 0; i < list.length; i++) {
				load(new File(file, list[i]), dataFolder);
			}
		} else if (loadingMode.isSimple()) {
			load(new File(file, "sl.rdf"), dataFolder);			
		} else {
			boolean isDepth2 = loadingMode.isDepth2();
			boolean isYearMonthMode = loadingMode.isYearMonthMode();
			boolean isYearSubMode = loadingMode.isYearSubMode();
			if (isDepth2) {
				String[] list = file.list();
				for (int i = 0; i < list.length; i++) {
					String fn = list[i];
					// s'il y a un sl.rdf, on le charge
					if (fn.endsWith("sl.rdf")) {
						load(new File(file, fn), dataFolder);
						continue;
					}
					if ((isYearMonthMode) || (isYearSubMode)) {
						try {
							Integer.parseInt(fn);
						} catch (NumberFormatException e) {
							continue;
						}
					}
					File ydir = new File(file, fn);
					if (!(ydir.isDirectory())) continue;
					File yslDotRdf = new File(ydir,"sl.rdf");
					if (yslDotRdf.exists()) load(yslDotRdf, dataFolder);
					
					String[] mlist = ydir.list();
					for (int m = 0; m < mlist.length; m++) {
						String mfn = mlist[m];
						File mdir = new File(ydir, mfn);
						if (!(mdir.isDirectory())) continue;
						if (isYearMonthMode) {
							try {
								Integer.parseInt(mfn);
							} catch (NumberFormatException e) {
								continue;
							}
						}
						File mslDotRdf = new File(mdir,"sl.rdf");
						if (mslDotRdf.exists()) load(mslDotRdf, dataFolder);
					}			
				} // for
			} else {
				throw new RuntimeException("Unexpected loadingMode " + loadingMode + " dataFolder:" + dataFolder);
			} // if (isDepth2) or not
		} // if loadingmode
	} else { // not a dir
		
		String filename = file.getAbsolutePath();
		if (filename.endsWith("sl.rdf")) {
			// System.out.println("load file " + file + " base " + rootBase + " rootFileCorrespondingToBase " + rootFile);
			try {
				// 2006-11
				String base = dataFolder.getBase(file);
				loadDocsModelFromFile(filename, base, thesaurus);
			} catch (Throwable t) {
				// Pour ne pas arreter le chargement pour un fichier qui ne marche pas
				System.err.println("Unable to load file: " + filename);
				t.printStackTrace();
			}
		} else if (filename.endsWith("slkws.rdf")) {
			try {
				loadKWsModelFromFile(filename, thesaurus);
			} catch (Throwable t) {
				// Pour ne pas arreter le chargement pour un fichier qui ne marche pas
				System.err.println("Unable to load file: " + filename);
				t.printStackTrace();
			}
		}
	} // dir or nor
}

/** Très importante fonction qui détermine comment on passe du nom de fichier à l'uri qui sera
 *  écrite dans les fichiers pour y faire référence.
 * 
 * ATTENTION jusqu'à 2005/12, ceci faisait en fait filenameToUri2FileUri -- et il y a des cas où ce n'est pas
 * ça qu'on veut : si fichier qui est servi par webServer, on voudrait l'url retournée par webServer
 * (Au moins ds certains cas. Toujours ? je suppose maintenant que oui A VOIR)
 */
public String filenameToUri(String filename) throws MalformedURLException, URISyntaxException {
	return fileToUri(new File(filename));
}

public String fileToUri(File f) throws MalformedURLException, URISyntaxException {  // THE WS QUESTION
	WebServer ws = this.getWebServer();
	if (ws != null) {
		String uri = ws.getURI(f);
		if (uri != null) return uri;
	}
	return FileUriFormat.filenameToUri(f.getPath());
}


public String filenameToFileUri(String filename) throws MalformedURLException, URISyntaxException {
	return FileUriFormat.filenameToUri(filename);
}


class YYYYMM {
	String yyyy,mm;
	YYYYMM(String yyyy, String mm) {
		this.yyyy = yyyy;
		this.mm = mm;
	}
}
/** doc supposé existant. Retourne null sinon ou si pas de date de création connue. */
YYYYMM doc2YYYYMM(SLDocument doc) {
	String yyyy = null, mm = null;
	PropertyValues vals = doc.getProperty(SL_CREATION_DATE_PROPERTY);
	if (vals != null) {
		String prop = vals.getFirstAsString();
		if ((prop != null) && (prop.length() >= 7)) {
			yyyy = prop.substring(0, 4);
			mm = prop.substring(5,7);
			return new YYYYMM(yyyy,mm);
		}
	}
	return null;
}


// THE WS QUESTION
/** Returns the file corresponding to a URI (or null in case of failure to do so). 
 *  @param uri either a file-protocol uri, or the uri of a file served by this.getWebServer. */
// 2020-01 localFilesOutOfDatafolders NOTE:
// This is a brute, crude result (uri -> file)
// that doesn't take into account the case of bookmarks pointing to a file out of the datafolder
// for this, see SLDOcumentStuff.getFile(), and
// find "2020-01 localFilesOutOfDatafolders" in CoolUriServlet for an example
public File getFile(String uri) throws IOException, URISyntaxException { // HUM TODO identique à getFileIfLocal(String docUri)
	// System.out.println("SLModel.getFile: " + uri);
	if (uri.startsWith("file:")) {
		return fileUri2File(uri);
	} else {
		return getFile(uri, this.getWebServer()); // no need to remove fragment component ?  
	}
}
// THE WS QUESTION
/** Returns the file corresponding to an URI (or null in case of failure to do so). 
 *  @param uri either a file-protocol uri, or the uri of a file served by ws. */
static public File getFile(String uri, WebServer ws) throws IOException, URISyntaxException {
	if (uri.startsWith("file:")) {
		return fileUri2File(uri);
	} else {
		// served by web server ?
		if (ws != null) return ws.getFile(uri); // no need to remove fragment component ?
		return null;
	}
}

/** Return the file corresponding to a file-protocol URI. */
static public File fileUri2File(String uri) throws URISyntaxException {
	// if this.uri has a fragment component (...#...), remove it //needed ?
	String fileUri = uri;
	int n = fileUri.indexOf("#");
	if (n > -1) fileUri = fileUri.substring(0,n);
	return new File(new URI(fileUri));	
}

/** Methode appelee lorsqu'on charge un fichier. Le bon endroit pour placer
 *  un eventuel traitement special.
 *  param base : la base à utiliser pour la lecture. Must not be null
 *  @param thesaurus : pas utile en soi au chargement. Sert à maintenir la liste 
 *  des openDocsFiles -- et donc doit être non null.
 * TODO : attention à la question du "/" final de base (faut-il toujours ?)
 */
private void loadDocsModelFromFile(String filename, String base, SLThesaurus thesaurus) throws MalformedURLException, IOException, URISyntaxException {
	if (base == null) throw new IllegalArgumentException("base must not be null");
	readDocsModelFromFile(filename, base);
	DocsFile docsFile = new DocsFile(filename, base, thesaurus);
	if (!(this.openDocsFiles.contains(docsFile))) this.openDocsFiles.addElement(docsFile);
} 

/** Methode appelee lorsqu'on charge un fichier. Le bon endroit pour placer
 *  un eventuel traitement special.
 *  @param thesaurus supposé non null
 */
private void loadKWsModelFromFile(String filename, SLThesaurus thesaurus) throws Exception {
	String base = thesaurus.getBase();
	readKWsModelFromFile(filename, base);
	KwsFile kwsFile = new KwsFile(filename,base);
	if (!(this.openKWsFiles.contains(kwsFile))) this.openKWsFiles.addElement(kwsFile);
}
abstract protected void readDocsModelFromFile(String filename, String base) throws IOException ;
abstract protected void readKWsModelFromFile(String filename, String base) throws Exception ;

//
// GESTION D'UN THESAURUS A UTILISER PAR DEFAUT AVEC UN FICHIER DE DOCS
//

public class KwsFile extends SLFile {
	public KwsFile(String filename, String base) {
		super(filename, base);
	}
}


public static class LoadingMode {
	private boolean isAllSubDirsMode = false;
	private boolean isDepth2Mode = false;
	private boolean isYearMonthMode = false;
	private boolean isYearSubMode = false;
	private boolean isSimple = false;
	/** Normally, the base used to load a file inside a dataFolder is the file itself. */
	private boolean isBaseRelativeToFile = true;
	public LoadingMode(String loadingMode) {
		if ((loadingMode == null) || ("".equals(loadingMode))) {
			this.isAllSubDirsMode = true;			
		} else {
			loadingMode = loadingMode.toLowerCase();
			if (loadingMode.indexOf("all") > -1){
				this.isAllSubDirsMode = true;
			} else if (loadingMode.indexOf("depth2") > -1){
				this.isDepth2Mode = true;
			} else if (loadingMode.indexOf("yearmonth") > -1){
				this.isYearMonthMode = true;
			} else if (loadingMode.indexOf("yearsub") > -1){
				this.isYearSubMode = true;
			} else if (loadingMode.indexOf("simple") > -1){
				this.isSimple = true;
			}
			if (loadingMode.indexOf("absolutebase") > -1) {
				isBaseRelativeToFile = false;
			}
		}
	}
	boolean isAllSubDirsMode() { return this.isAllSubDirsMode; }
	boolean isDepth2Mode() { return this.isDepth2Mode; }
	boolean isYearMonthMode() { return this.isYearMonthMode; }
	boolean isYearSubMode() { return this.isYearSubMode; }
	boolean isSimple() { return this.isSimple; }
	boolean isDepth2() { return (isDepth2Mode() || isYearMonthMode() || isYearSubMode()); } // TODO
	boolean isBaseRelativeToFile() { return this.isBaseRelativeToFile; }
	/** Return true iff supports use of yyyy/mm folder. */
	boolean supportsYYYYMM() { return !isSimple(); }
	public void setBaseRelativeToFile(boolean isBaseRelativeToFile) {
		this.isBaseRelativeToFile = isBaseRelativeToFile;
	}
}

//
// EDIT MODEL
//

// EDITING DOCUMENTS

/**Voir la discussion sur mise en cache de liste des kws ds doc :
 * ne faut-il pas ici faire une simple "convenience method :
public void addDocProperty(SLDocument doc, String propertyUri, String propertyValue, String lang) {
	doc.addProperty(String propertyUri, String propertyValue, String lang);
}
ou bien mettre ici la version avec uri au lieu de doc ? 
 */
abstract public void addDocProperty(SLDocument doc, String propertyUri, String propertyValue, String lang);
abstract public void addDocProperty(SLDocument doc, String propertyUri, String objectUri);
abstract public void addDocProperty(SLDocument doc, String propertyUri, String[] objectUris);
abstract public void setDocProperty(SLDocument doc, String propertyUri, String propertyValue, String lang);
abstract public void setDocProperty(SLDocument doc, String propertyUri, String objectUri);
/** kw censé exister */
public void addKwProperty(SLKeyword kw, String propertyUri, String objectUri) {
	addKwProperty(kw.getURI(), propertyUri, objectUri);
}
/** kw censé exister */
public void setKwProperty(SLKeyword kw, String propertyUri, String propertyValue, String lang) {
	setKwProperty(kw.getURI(), propertyUri, propertyValue, lang);
}
/** kwuri uri d'un kw sensé exister */
abstract public void addKwProperty(String kwUri, String propertyUri, String objectUri);
/** kwuri uri d'un kw sensé exister */
abstract public void addKwProperty(String kwUri, String propertyUri, String[] objectUris);
/** kwuri uri d'un kw sensé exister */
abstract public void addKwProperty(String kwUri, String propertyUri, String propertyValue, String lang);
/** kwuri uri d'un kw sensé exister */
abstract public void setKwProperty(String kwUri, String propertyUri, String objectUri);
/** kwuri uri d'un kw sensé exister */
abstract public void setKwProperty(String kwUri, String propertyUri, String propertyValue, String lang);


/** true iff values must be indexed by ThesaurusIndex */
public boolean isLabelProperty(String propertyUri) {
	return SLVocab.PREF_LABEL_PROPERTY.equals(propertyUri);
}

/**
 *  Affecter un kw a un doc
 *  @param kwLabel was : @param kw : sous forme courte. N'a pas besoin d'exister.
 *  2003-08-10 : ecriture du fichier docs qui va bien
 *  @todo : bug - en cas de creation des fichiers, ne sont pas ajoutes aux openbase
 * 
 * il y avait (?) un bug
 *  si il n'y a pas de thesaurus défini pour le fichier -- ce qui survient
 *  en particulier si docsFile n'existe pas encore (? j'ai en tous cas eu le cas lors de ma 1ere tentative cinema)
 * BUG AVERE le 2004-08-08 : ds ce cas, le thesaurus pris est celui par défaut - ce qui ne va pas,
 * ex cas ajout d'un kw à un fichier manifestement si cg
 * palier le bug en créant le docsFile (par ex en créant une ppté via extract metadata) ET en relancant
 * (becoze je pense le 1er bug ci dessus : pas ajoutés aux open files ?
 * paliatif qui pourrait être fait : forcer la création de fichiers de de loadlist.txt (en haut de hierarchie)
 * CORRIGE (?) le 2004-08-16 grace à une recher ds dataFolderList : see getLoaderDocsFile
 */
public SLKeyword addKeyword(SLDocument doc, String kwLabel, Locale locale) throws Exception {
	if ((kwLabel == null) || (kwLabel.equals(""))) throw new IllegalArgumentException("Label of kw null or empty");
	SLThesaurus th = getThesaurus(doc); // thesaurus à utiliser
	SLKeyword kw = kwLabel2KwCreatingItIfNecessary(kwLabel, th.getURI(), locale);
	addKeyword(doc, kw);
	return kw;
}




//
//
//

public void addKeyword(SLDocument doc, SLKeyword kw)  throws Exception {
	addDocProperty(doc, HAS_KEYWORD_PROPERTY, kw.getURI());
}
public void addKeyword(SLDocument doc, SLKeyword[] kw)  throws Exception {
	String[] uris = new String[kw.length];
	for (int i = 0; i < uris.length; i++) {
		uris[i] = kw[i].getURI();
	}
	addDocProperty(doc, HAS_KEYWORD_PROPERTY, uris);
}

/** Supprimer l'affectation d'un kw a un doc. */
abstract public void removeKeywords(SLDocument doc, SLKeyword[] kw);

// EDITING KEYWORDS

abstract public void removeParents(SLKeyword kw, SLKeyword[] parents);
abstract public void removeChildren(SLKeyword kw, SLKeyword[] children);
abstract public void removeFriends(SLKeyword kw, SLKeyword[] removed);


public void addParentChildLink(String parentUri, String childUri) {
	addKwProperty(childUri, HAS_PARENT_PROPERTY, parentUri);
}
public void addParentChildLink(String[] parentUris, String childUri) {
	addKwProperty(childUri, HAS_PARENT_PROPERTY, parentUris);
}
public void addParentChildLink(String parentUri, String[] childrenUri) {
	for (int i = 0; i < childrenUri.length; i++) {
		addKwProperty(childrenUri[i], HAS_PARENT_PROPERTY, parentUri);
	}
}

public void addParent(SLKeyword child, SLKeyword parent) {
	addParentChildLink(parent.getURI(), child.getURI());
}
public void addChild(SLKeyword parent, SLKeyword child) {
	addParentChildLink(parent.getURI(), child.getURI());
}
/** symetry is handled when searching @see JModel.getFriendsList(Resource) */
public void addFriend(SLKeyword kw, SLKeyword friend) {
	addKwProperty(kw.getURI(), HAS_FRIEND_PROPERTY, friend.getURI());
}

public abstract boolean kwExists(String kwUri);
// public abstract boolean docExists(String docUri);

//
// PASSER D'UN LABEL A UN KEYWORD
//

/**
 * A utiliser pour rechercher un kw qu'on espère existant. 
 * Le Keyword retourné n'existe pas forcément.
 * Si thesaurusUri est null, recherche ds tous ceux ouverts, en commençant par le default.
 */ 
// TODO au sujet des formes : ajouter une version ds l'uri pour être capble de fonctionner avec des évolutions
public SLKeyword kwLabel2SLKw(String kwLabel, String thesaurusUri, Locale locale) {
	String uri = kwLabel2Uri(kwLabel, thesaurusUri, locale);
	// attention, ce peut-être un alias !
	// return getKeyword(uri);
	return resolveAlias(uri);
}

/**
 * Retourne l'uri du kw existant, ou à défaut la forme à utiliser pour l'uri d'un keyword 
 * qu'on souhaite créer à partir de son libellé.
 * @param kwLabel
 * @param thesaurusUri si null, recherche ds tous les SLThesaurus ouverts. Si ne trouve dans
 * aucun, retourne la forme ds le default thesaurus
 * @see kwLabel2KwCreatingItIfNecessary
 * @since (sous cet algo) mars 2004
 */
public String kwLabel2Uri(String kwLabel, String thesaurusUri, Locale locale) {
	//	 TODO : faire en sorte que le default soit en premier ds thesauri
	if (thesaurusUri != null) {
		return kwLabel2UriQuick(kwLabel, thesaurusUri, locale);
	} else {
		String uri = kwLabel2ExistingKwUri(kwLabel, locale);
		if (uri != null) return uri;
		return kwLabel2UriQuick(kwLabel, this.defaultThesaurus.getURI(), locale);
	}
}

/**
 * Retourne l'uri d'un kw existant, ou null
 * Recherche ds tous les SLThesaurus ouverts.
 * @see kwLabel2KwCreatingItIfNecessary, kwLabel2Uri
 * @since oct 2004
 */
public String kwLabel2ExistingKwUri(String kwLabel, Locale locale) {
	String uri;
	// on essaye d'abord avec le default
	if (this.defaultThesaurus != null) {
		uri = kwLabel2UriQuick(kwLabel, this.defaultThesaurus.getURI(), locale);
		if (kwExists(uri)) return uri;
	}
	if (this.thesauri != null) {
		for (int i = 0; i < thesauri.size(); i++) {
			uri = kwLabel2UriQuick(kwLabel, ((SLThesaurus) thesauri.get(i)).getURI(), locale);
			if (kwExists(uri)) return uri;
		}
	}
	return null;
}

/**
 * Retourne l'uri d'un kw existant, ou null
 * @param thesaurusUri non null
 * @param resolveAlias si true, résout les alias, sinon, peut retourner l'uri de l'alias
 * @see kwLabel2KwCreatingItIfNecessary, kwLabel2Uri
 * @since oct 2004
 */
public String kwLabel2ExistingKwUri(String kwLabel, String thesaurusUri, Locale locale, boolean resolveAlias) {
	String uri = kwLabel2UriQuick(kwLabel, thesaurusUri, locale);
	if (kwExists(uri)) { // 2008-09 : TODO verify the use of resolvealias here
		if (resolveAlias) uri = resolveAliasAsUri(uri);
		return uri;
	}
	return null;
}


/**
 * Retourne la forme à utiliser pour l'uri d'un keyword qu'on souhaite créer à partir de son libellé.
 * @param thesaurusUri doit être non null.
 * @see kwLabel2KwCreatingItIfNecessary
 */
public String kwLabel2UriQuick(String kwLabel, String thesaurusUri, Locale locale) {
	if (thesaurusUri == null) throw new IllegalArgumentException("Null thesaurusUri prohibited here!");
	// return thesaurusUri + "#" + kwLabel2ShortUri(kwLabel, locale); // #thing
	return thesaurusUri + "/" + kwLabel2ShortUri(kwLabel, locale); // #thing
}
/**
 * Retourne la forme courte à utiliser pour l'uri d'un keyword qu'on souhaite créer à partir de son libellé.
 * Les mots dont les diacritiques ont été remplacées, séparés par "_"
 * If you want to change that after having already created keywords,
 * voir ds SLServelt.initSL ((JModel) mod).correctOldKwUris();
 */
public String kwLabel2ShortUri(String kwLabel, Locale locale) {
	ArrayList al = (wordsInString).words(kwLabel , locale);
	int n = al.size();
	if (n == 0) return "";
	String s = (String) al.get(0);
	s = converter.urlConvert(s);
	if (n == 1) return s;
	StringBuffer sb = new StringBuffer(s);
	for (int i = 1; i < n; i++) {
		sb.append("_");
		sb.append(converter.urlConvert((String) al.get(i)));
	}
	return sb.toString();
}

public String kwLabel2Uri(String kwLabel, String thesaurusUri) {
	return kwLabel2Uri (kwLabel, thesaurusUri, null);
}

/** Modifie le modèle pour y ajouter les statements nécessaires à la création du mot clé, sensé ne pas exister. 
 * Attention, ce qu'il faut utiliser, c'est doCreateKeyword, qui met aussi à jour l'index du thésuarus. 
 */
protected abstract void createKw(String uri, String label, Locale locale) throws Exception;

/**
 * Modifie le modèle pour y ajouter les statements nécessaires à la création du mot clé, sensé ne pas exister.
 * met à jour le thesaurus
 * retourne le kw créé (sensé ne pas exister avant) 
 * @param thesaurusUri not null
 */
public SLKeyword doCreateKeyword(String uri, String kwLabel, Locale locale) throws Exception {
	createKw(uri,kwLabel,locale);
	SLKeyword kw = getKeyword(uri);
	this.getThesaurusIndex().addItem(kw, kwLabel, locale);
	return kw;
}

/** retourne null et ne fait rien si existe déjà.
 * @param thesaurusUri non null
 * @param locale
 * @return
 */
public SLKeyword kwLabel2NewKeyword(String kwLabel, String thesaurusUri, Locale locale) throws Exception {
	String uri = kwLabel2UriQuick(kwLabel, thesaurusUri, locale);
	if (this.kwExists(uri)) return null;
	return doCreateKeyword(uri,kwLabel,locale);
}

/**
 * @param thesaurusUri non null
 */
public SLKeyword kwLabel2KwCreatingItIfNecessary(String kwLabel, String thesaurusUri, Locale locale) throws Exception {
	// 2007/10
	/*String uri = kwLabel2UriQuick(kwLabel, thesaurusUri, locale);
	if (kwExists(uri)) {
		uri = resolveAliasAsUri(uri);
		return getKeyword(uri);
	} else {
		return doCreateKeyword(uri,kwLabel,locale);
	}*/
	SLKeyword[] kws = getThesaurusIndex().label2Keyword(kwLabel, locale);
	if (kws.length == 0) {
		String uri = kwLabel2UriQuick(kwLabel, thesaurusUri, locale);
		return doCreateKeyword(uri,kwLabel,locale);
	} else {
		// TODO if there are more than one
		return getKeyword(resolveAliasAsUri(kws[0].getURI()));
	}
}

//
// SAVE
//

/** attention, ne marche pas bien pour les folders - si termine par / ou pas, ne fait pas la meme chose. */
public static String file2Folder(String file) {
    int n = file.lastIndexOf("/");
    if (n != file.length() - 1) {
      return file.substring(0, n+1);
    }
    return file;
}

/** Le reotur est "/" terminated (a verifier) @todo */
public static String docUri2Folder(String docUri) throws MalformedURLException {
	URL docUrl = new URL(docUri);
	String docFileName = docUrl.getFile();
	return file2Folder(docFileName);
}


/*
 * Cette version ecrit les kwsModel complet ds le fichier kws le + haut,
 * et pareil pour les docs

 * OLDER COMMENT :
 * Convient donc si on n'en ouvre qu'un.
 * quoique...
 * Situation typique : les kws definis ds d'autres fichiers, et
 * dont on a besoin pour qualifier les docs de ce fichier.
 * C'est OK si on utilise une String caracterisant le kw
 * C'est meme OK qd les kws donnes par uri sont juste mentionnes (apparents comme HAS_KEYWORD_PR0PERTY d'un doc)
 * (argument pour 2 models distincts, dont un seulement KWS ? ala semanlink hc ? rq : oui, mais -> 2 fichiers
 * BTW, 2 fichiers, permet des choses en + pour les uri de KWs ? (rappel, ils doivent avoir une url longue
 * avec un "namespace" fixe, quand on n'a qu'un seul fichier) Je ne crois pas :
 * en effet, on doit pouvoir faire reference ? un kw ? partir de n'importe où, ce qui implique qu'ils aient tous
 * la meme "base" (du moins, qu'on la connaisse sans difficulte)
 * Par contre, avoir des fichiers "purs kws" reste possible et interessant :
 * Ils ne se situent pas forcement "? l'url des kws". Leur base est la base des kws. Donc, inutile
 * d'avoir les urls longues comme ds un model avec des docs.
 * La question est :
 * quand on ouvre un model de docs, comment savoir quels fichiers ouvrir pour avoir les kws
 * (cf import d'ontologies en DAML) Pourrait etre "? cote", ds le meme dossier (genre un fichier sl et slk)
 *
 * DESIGN DECISION :
 * favoriser la def des Keywords dans des fichiers (et Model ?) qui leur sont exclusivement consacres
 * (ressemble bigrement ? un fichier d'ontologie)
 *
 * VOIR :
 * la façon dont on definit la liste des kws ds un model : 2 aspects :
 * - les kws qui sont des HAS_KEYWORD_PR0PERTY d'un doc (pourraient etre une simple String literal), ds un docset
 * - les entites definies avec un rdf:type KEYWORD, ds un "graph de kws" (ala old sl)
 * J'ai bien l'impression qu'on a besoin des 2
 * le 1er : liste des kws utilises ds un docset
 *
 * RQ sur ce qui precede :
 * quand on cree un docset ? la main, le plus simple est souvent d'utiliser des litteraux comme kws
 * Ensuite, lors d'une utilisation, on etablit le lien entre ces kws litteraux et des kws "references"
 * (ds un docset, cad une ontologie. On peut ensuite remplacer ds le fichier initial le litteral
 * par le KW de l'ontologie. (cf le network qui s'autoorganise, qui s'ameliore)
 *
 */
// abstract public void save() throws IOException ;
//

//
// KWS COLLECTION RELATED
//

/** Met les JKeyword dans la collection passee en argument.
 *  Methode d'interface.
 *  Cette implementation ne met que les keywords effectivement utilises pour marquer des documents du modele
 *  (Resource ou litteraux), mais pas ceux qui ne sont que declares (ou qui ne font
 *  qu'intervenir comme parent ou child) 
 * @throws IOException 
 * @throws JenaException */
public void kwsIntoCollection(Collection coll) throws Exception {
  // Cette implementation ne met que les keywords correspondant ? des
  // Resource de rdfs:Class JKeyword. Manque donc les litteraux.
  // kwsResIntoCollection(coll);
  // ou bien :
  // labelledResIntoCollection(coll);
  usedKWsIntoCollection(coll);
}

// 02/01/03
/** Met les JKeyword utilises dans ce modele dans la collection passee en argument.
 *  Contient tous les keywords utilises pour marquer une resource dans ce modele
 *  (litteraux compris) - mais aucun de ceux qui ne sont pas utilises (meme s'ils y sont definis)
 NE CREE PAS DES RES A LA PLACE DES LITTERAUX
 */
abstract public void usedKWsIntoCollection(Collection coll);
/** Met les JKeyword correspondant aux Resource de rdfs:Class JKeyword
 *  dans la collection passée en argument.
 *  (Ne prend donc pas en compte les littéraux.)
 *  ne sert pas si on ne met plus la classe rdfs:Class JKeyword, opération
 *  inutile si tous les kws et slt eux sont ds this.kwsModel
 */
abstract public void kwsResIntoCollection(Collection<SLKeyword> coll);

//
// KWS D'UN DOC RELATED
//

/** retourne une ArrayList des SLKeyword d'un doc */
abstract public ArrayList getKeywordsList(String uri);

///////////////////////// MODIFS POUR SERVLET

/**
 *  calculé à chaque fois : ne va pas pour l'utilisation qui en est faite
 *  @todo change
 */
public ArrayList<SLKeyword> getKWsInConceptsSpaceArrayList() {
  try {
	ArrayList<SLKeyword> x = new ArrayList<SLKeyword>();
	kwsResIntoCollection(x);
	Collections.sort(x);
	return x;
  } catch (Exception ex) { throw new SLRuntimeException(ex);}
}


/** copie de SLApp
 *  calcule à chaque fois : ne va pas pour l'utilisation qui en est faite
 *  @todo change
 * ATTENTION j'ai un doute sur ce qui se passe la dedans. J'ai eu l'impression
 * que les kws retournés ne sont pas vraimeent les vrais : en recherchant les parents
 * d'un elt, pas trouvé
 */
public ArrayList getKWsInRealSpaceArrayList() {
  try {
	ArrayList x = new ArrayList();
	usedKWsIntoCollection(x);
	Collections.sort(x);
	return x;
  } catch (Exception ex) { throw new SLRuntimeException(ex);}
}

// j'ai constaté que ceertaisn kws (par ex fps et cocoon) n'avaient pas le rdf:type kw,
// ce qui fait qu'on ne les trouve pas ds le livesearch
// (ils ne sont pas dans le ThesaurusIndex) // WHY ???
// Pour faire la liste de ces cas
public ArrayList debugRealKWsNotInConceptSpace() {
	ArrayList x = new ArrayList();
	ArrayList list1 = getKWsInRealSpaceArrayList();
	ArrayList list2 = getKWsInConceptsSpaceArrayList();
	HashSet hs = new HashSet();
	for (int i = 0; i < list2.size(); i++) {
		hs.add(list2.get(i));
	}
	for (int i = 0; i < list1.size(); i++) {
		Object o = list1.get(i);
		if (hs.contains(o)) continue;
		SLKeyword kw = (SLKeyword) o;
		SLKeyword resolu = resolveAlias(kw.getURI());
		// System.out.println(kw.getURI() + " hasAlias " + resolu.getURI());
		if (resolu.equals(kw)) {
			x.add(o);
		}
	}
	return x;
}

/** 
 *  calcule à chaque fois : ne va pas pour l'utilisation qui en est faite
 *  @todo change
 */
public List getKWs(SLThesaurus th) {
	if (thesauri.size() < 2) return getKWsInConceptsSpaceArrayList();
	String thUri = th.getURI();
	ArrayList x = new ArrayList();
	kwsResIntoCollection(x);
	for (int i = x.size()-1; i >= 0; i--) {
		SLKeyword kw = (SLKeyword) x.get(i);
		if (!((kw.getURI().startsWith(thUri)))) x.remove(i);
	}
	Collections.sort(x);
	return x;
}

/** Retourne les kws sans parents d'une liste données de kws. */
public ArrayList withoutParents(List kws) {
	int n = kws.size();
	ArrayList x = new ArrayList();
	for (int i = 0; i < n; i++) {
		SLKeyword kw = (SLKeyword) kws.get(i);
		// TODO OPTIMISER : il suffit qu'il y ait un parent.
		// Pas besoin de calculer la liste complete
		List parents = kw.getParents();
		if ((parents == null) || (parents.size() == 0)) {
			x.add(kw);
		}
	}
	return x;
}

/** utilisé juste par welcome.jsp. Ne devrait pas etre public */
public Vector getOpenKWsFiles() { return this.openKWsFiles; }
/** utilisé juste par welcome.jsp. Ne devrait pas etre public */
public Vector getOpenDocsFiles() { return this.openDocsFiles; }
/** utilisé par welcome.jsp */
public Vector getDataFolderList() { return this.dataFolderList; }

public Vector getThesauri() { return this.thesauri; }
//
//
//

abstract public void delete(SLKeyword kw);
abstract public void delete(SLDocument doc);

//
// THESAURI RELATED
//

static public String kwUri2ThesaurusUri(String kwUri) { // #thing
	int n = kwUri.lastIndexOf("/");
	return kwUri.substring(0,n);
}

/** retourne null si le th n'est pas ouvert
 * @see getThesaurus
 */
public SLThesaurus kwUri2Thesaurus(String kwUri) {
	return getThesaurus(kwUri2ThesaurusUri(kwUri));
}

/**
 * Charge un thesaurus. 
 * Charge son fichier par défaut
 * Ne fait rien si le thesaurus est déjà ouvert (attention,
 * ne modifie pas le defaut file même si celui indiqué est différent)
 * @return le thesaurus (attention, y compris si déjà ouvert avant)
 * @param defaultThesaurusDir supposé être une dir
 */
 public SLThesaurus loadThesaurus(String defaultThesaurusURI, File defaultThesaurusDir) throws Exception {
 	// System.out.println("SLModel.loadThesaurus: " + defaultThesaurusURI + " ; " + defaultThesaurusFile);
	if (!defaultThesaurusDir.isDirectory()) throw new IllegalArgumentException(defaultThesaurusDir + " is not a dir");
 	SLThesaurus thesaurus = this.getThesaurus(defaultThesaurusURI);
	if (thesaurus == null) { // pas ouvert
		File defaultThesaurusFile = new File(defaultThesaurusDir,"slkws.rdf");
		String defaultThesaurusFilename = defaultThesaurusFile.getPath();
		thesaurus = new SLThesaurusAdapter(defaultThesaurusURI, defaultThesaurusFilename);
		this.thesauri.add(thesaurus);
		loadKWsModelFromFile(defaultThesaurusFilename,thesaurus);
	}
	return thesaurus;
}

/**
 * Retourne le thesaurus correspondant à uri s'il est ouvert, sinon retourne null.
 * @see kwUri2Thesaurus
 */
public SLThesaurus getThesaurus(String thesaurusURI) {
	for (int i = 0; i < this.thesauri.size(); i++) {
		SLThesaurus th = (SLThesaurus) this.thesauri.get(i);
		if (thesaurusURI.equals(th.getURI())) return th;
	}
	return null;
}

public List getActivFiles(SLThesaurus th) throws IOException, URISyntaxException { // 911 //////////////////
	ArrayList al = new ArrayList();
	List docs = getActivFolder().getDocuments();
	for (int i = 0; i < docs.size(); i++) {
		SLDocument slDoc = (SLDocument) docs.get(i);
		SLThesaurus saurus = this.getThesaurus(slDoc);
		if (th.equals(saurus)) al.add(slDoc);
	}
	return al;
}

public SLKeyword getActivFolder() {
	return getKeyword(getDefaultThesaurus().getBase() + ACTIV_FOLDER); // bof?
}

//
//
//

/** pour faire quelque chose lors de la création de docs.
 *  (par ex affecter une date de création. 
 * @throws URISyntaxException 
 * @throws Exception */
abstract public void onNewDoc(SLDocument doc) throws Exception;

/**

//
// KEYWORD D'UN TEXTE / ThesaurusIndex
//

/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */
public Collection<SLKeyword> getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	return getThesaurusIndex().getKeywordsInText(text, locale, thesaurusUri);
}

public ThesaurusIndex getThesaurusIndex() {
	if (this.thesaurusIndex == null) computeThesaurusIndex();
	return this.thesaurusIndex;
}
void computeThesaurusIndex() {
	// this.thesaurusIndex = new ThesaurusIndex(this); // for ThesaurusIndexOK
	this.thesaurusIndex = new ThesaurusIndex(this, Locale.getDefault());
}

//
//
//

public abstract long size();
public abstract long kwsSize();
public abstract long docsSize();
public abstract long numberOfDocs();
/** Retourne une ArrayList de Documents affectés d'une certaine ppté. 
 *  (rq propertyUri peut-être null)
 *  @see getDocumentsList(Resource)
 */
public abstract ArrayList getDocumentsList(String propertyUri, String objectUri) throws Exception;
public abstract ArrayList getDocumentsList(String propertyUri, String uri, boolean inverse) throws Exception; // bof grosse daube pour trouver le doc qui est source
public abstract ArrayList getDocumentsList(String propertyUri, String propertyValue, String lang) throws Exception;
public abstract ArrayList getKeywordsList(String propertyUri, String objectUri) throws Exception;
public abstract ArrayList getKeywordsList(String propertyUri, String propertyValue, String lang) throws Exception;


//
// ALIAS
//
abstract public SLKeyword resolveAlias(String uri);
abstract public String resolveAliasAsUri(String uri);
/** met ds coll les uris des alias (sous forme de Strings). */ 
abstract public void aliasesIntoCollectionOfUris(Collection coll);

// 2012-12 @find SKOSIFY doens't anymore "add an alias": just transfers the properties
public void addAlias(String aliasLabel, String lang, SLKeyword kw) {
	try {
		String kwUri = kw.getURI();
		String thesaurusUri = SLModel.kwUri2ThesaurusUri(kwUri);
		Locale locale = null;
		if (lang == null) locale = Locale.getDefault();
		else locale = new Locale(lang);
		String aliasUri = kwLabel2UriQuick(aliasLabel, thesaurusUri, locale);
		SLKeyword alias = getKeyword(aliasUri); 
		
		// removing the labels of the alias from the index
		ThesaurusIndex th = getThesaurusIndex();
		th.deleteItem(alias);
		
		// transfering the properties
		aliasIt(alias, getKeyword(kwUri));
		
		// updating the thesaurus index
		th.addItem(kw);
		
//		// addKwProperty(kw, SLVocab.HAS_ALIAS_PROPERTY, aliasUri); // 2012-12 @find SKOSIFY
//		// label de l'alias : seulement si n'existe pas déjà
//		String labelProp = SLVocab.PREF_LABEL_PROPERTY;
//		if (alias.getProperty(labelProp) == null) {
//			addKwProperty(aliasUri, labelProp, aliasLabel, lang);
//			// getThesaurusIndex().add(aliasUri, alias);
//			// getThesaurusIndex().addKw(alias, aliasLabel, locale); // for ThesaurusIndexOK
//			getThesaurusIndex().addItem(alias, aliasLabel, locale);
//		}
	} catch (Exception e) { throw new SLRuntimeException(e); }	
}
public void removeAlias(SLKeyword kw, String[] aliasuris) {
	try {
		String kwUri = kw.getURI();
		for (int i = 0; i < aliasuris.length; i++) {
			removeKWStatement(kwUri, SLVocab.HAS_ALIAS_PROPERTY, aliasuris[i]);	
			setKwProperty(aliasuris[i], SLVocab.PREF_LABEL_PROPERTY, null);
		}
	} catch (Exception e) { throw new SLRuntimeException(e); }		
}
/** fait le transfert des pptés de l'alias (docs, parents, sons) de l'alias vers kw
 *  (appelé par addAlias). Attention, c'est addAlias qu'il faut appeler
 * pour avoir la maj de l'index */
protected abstract void aliasIt(SLKeyword alias, SLKeyword kw);

/** attention, null si no alias */
public List getAliasUriList(SLKeyword kw) {
	return kw.getPropertyAsStrings(SLVocab.HAS_ALIAS_PROPERTY);
}

//
//
//

public abstract void removeKWStatement(String kwUri, String propUri, String objUri);
// reste à ajouter les autres (pour doc, pour obj sous forme de litéral)

public File goodDirToSaveAFile() {
	// ne va pas sur mac : File.pathSeparator met des ":"
	// return new File(getDefaultFolder() + (new YearMonthDay()).getYearMonth(File.pathSeparator) + File.pathSeparator);
	// OK sur windaube ???
	// return new File(getDefaultFolder() + (new YearMonthDay()).getYearMonth("/") + "/");
	return todayYearMonthDir(getDefaultFolder().getFile());
}

/** Retourne la dir parentDir/yyyy/mm avec yyyy/mm d'aujourd'hui. */
File todayYearMonthDir(File parentDir) {
	return (new YearMonthDay()).yearMonthAsFolder(parentDir);	
}

public File dirToSaveANote() {
	return todayYearMonthDir(getNotesFolder().getFile());
}

/** @since v0.6 */ // 2019-03 uris for bookmarks
public File dirToSaveBookmarks() {
	return todayYearMonthDir(getBookmarkFolder().getFile());
}



//
//
//





/** Le thesaurus à utiliser avec un doc donné */
public SLThesaurus getThesaurus(SLDocument doc) throws IOException, URISyntaxException {
	SLDataFolder dataFolder = getSLDataFolder(doc.getURI()); // fichier de modèle pour doc
	if (dataFolder != null) return dataFolder.getDefaultThesaurus();
	return getDefaultThesaurus();
}


//
//
//


public SLKeyword getFavori() {
	if (!isFavoriComputed) {
		String favoriUri = kwLabel2ExistingKwUri("favoris", Locale.FRANCE); // bof
		if (favoriUri != null) this.favori = getKeyword(favoriUri);
		isFavoriComputed = true;
	}
	return this.favori;
}

/** Returns true iff docUri is a file protocol url or it is served by this.webServer. */
public boolean isLocalDocument(String docUri) throws IOException, URISyntaxException {
	if (docUri.startsWith("file:/")) return true;
	WebServer ws = this.getWebServer();
	if (ws != null) {
		return ws.owns(docUri);
	}
	return false;
}

/** Allows to have Corrections made on a file (either thesaurus or document file) at the time it is loaded. */
public abstract void setCorrector(ModelCorrector corrector);

//
// cf recent docs and kws
//

//peut s'optimiser (par ex en ne recalculant pas tout de YearMonthDay.daysAgo(i)
public List getRecentDocs(int nbOfDays, String dateProp) throws Exception {
	List x = getDocumentsList(dateProp, (new YearMonthDay()).getYearMonthDay("-"),null);
	for (int i = 1; i < nbOfDays+1; i++) {
		List y = getDocumentsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"),null);
		SLUtils.reverseSortByProperty(y, dateProp);
		x.addAll(y);
	}
	return x;
}

//peut s'optimiser (par ex en ne recalculant pas tout de YearMonthDay.daysAgo(i)
public List getRecentKws(int nbOfDays, String dateProp) throws Exception {
	List x = getKeywordsList(dateProp, (new YearMonthDay()).getYearMonthDay("-"),null);
	for (int i = 1; i < nbOfDays+1; i++) {
		List y = getKeywordsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"),null);
		SLUtils.reverseSortByProperty(y, dateProp);
		x.addAll(y);
	}
	// Patch bug googlebot 2008-09
	/*for (int i = x.size()-1; i > -1; i--) {
		SLKeyword kw = (SLKeyword) x.get(i);
		List docs = kw.getDocuments();
		if ((docs == null) || (docs.size() == 0)) {
			x.remove(i);
		}
	}*/
	return x;
}

public List getRecentDocs(int nbOfDays) throws Exception {
	return getRecentDocs(nbOfDays, SLVocab.SL_CREATION_DATE_PROPERTY);
}

public List getRecentKws(int nbOfDays) throws Exception {
	return getRecentKws(nbOfDays, SLVocab.SL_CREATION_DATE_PROPERTY);
}

public List geDocs(Date date) throws Exception {
	return getDocumentsList(SLVocab.SL_CREATION_DATE_PROPERTY, new YearMonthDay(date).getYearMonthDay("-"),null);
}


//
//ECRIRE LES MAJ DU MODEL DS LE FICHIER QUI CONVIENT
//

/** Returns the SLDataFolder corresponding to document docUri.
 * <UL>
 * <LI>If the document is a file (either a file protocol url, or file served by this.webSever)
 * 	<UL>
 * 		<LI>returns the corresponding loaded SLDataFolder, if any </LI>
 *		<LI>else (should send an exception?) or return the defaultDataFolder or bookmarkFolder </LI>
 * 	</UL>
 * </LI>
 * <LI>else (the document is not a file, but a bookmark)<UL>
 * 		<LI>bookmarkFolder if not null</LI>
 * 		<LI>or defaultDataFolder</LI>
 * </UL></LI>
 * </UL>
 * @throws URISyntaxException 
 * @throws IOException 
 */
private SLDataFolder getSLDataFolder(String docUri) throws IOException, URISyntaxException {
	DocMetadataFile x = new DocMetadataFile(docUri, this, false);
	return x.getDataFolder();
}

public DocMetadataFile doc2DocMetadataFile(String docUri) throws IOException, URISyntaxException {
	return new DocMetadataFile(docUri, this, false);
}

/** The link between a docUri and its dataFolder and the sl.rdf file where metadata
 *  about it is written. */
public class DocMetadataFile {
	private SLDataFolder dataFolder;
	private File slDotRdfFile;
	private String base;
	DocMetadataFile(SLDataFolder dataFolder, File slDotRdfFile, String base) {
		this.dataFolder = dataFolder;
		this.slDotRdfFile = slDotRdfFile;
		this.base = base;
	}
	/** 
	 * Compute the SLDataFolder and/or the DocMetadataFile for a document
	 * @param docUri uri of document
	 * @param computecomputeOnlyDataFolder if true, only dataFolder is computed
	 */
	DocMetadataFile(String docUri, SLModel mod, boolean computeOnlyDataFolder) throws IOException, URISyntaxException {
		File docf = getFileIfLocal(docUri);
		if (docf != null) {
			dataFolder = getDataFolderFromLoadingList(docf);
			if (dataFolder == null) {
				// 2019-04 uris for bookmark je (re-?)mets getDefaultFolder plutôt que getBookmarkFolder // TODO CHECK
				dataFolder = getDefaultFolder(); // or throw Exception to forbid file outside an opened SLDataFolder ?
				// dataFolder = getBookmarkFolder(); // or throw Exception to forbid file outside an opened SLDataFolder ?
			} else {
				if (!computeOnlyDataFolder) slDotRdfFile = getSlDotRdfFileFromDataFolder(docf, docUri, dataFolder);
			}
		} else { // ni file protocol url, ni servie par notre webserver
			// Dans le cas d'une url genre www.hypersolutions.fr,
			// et bien on ne sait pas dans quel fichier l'écrire,
			// sauf peut-etre si elle a déjà été écrite !
			
			//
			// 2019-04 uris for bookmarks
			//
			
			// on avait
			// dataFolder = getBookmarkFolder();
			
			// MAIS pb avec mes "anciens" bookmarks :
			// conduit à les sauver dans bookmark folder,
			// alors qu'ils sont dans ds defaultDataFolder
			// (enfin : ds ancienne version, les 2 sont la même chose
			// mais au moins ds une phase transitoire avant de tout convertir,
			// on veut donc que les "anciens bkmrks -- les docs genre hypersolutions.fr restent
			// dans defaultDataFolder (le site web / svg de mes fichiers), mais les nouveaux
			// dans bookmarkFolder)
			
			// ASSUMANT que le cas de "nouveaux bookmarks" sont traités plus haut,
			// cad qu'ils retournent qlq chose ds getFileIfLocal() (== servis par le webserver ?)
			// on va ici (contre ce qui semblerait être logique) retourner defaultDataFolder
			// pour les docuri genre hypersolutions.fr
			
			// [NORMALEMENT, après fin des travaux uris for bookmarks, le cas présent ne se produira plus (???)]
			
			dataFolder = getDefaultFolder();
		}

		if (computeOnlyDataFolder) return;

		if (slDotRdfFile == null) { // on n'a pas trouvé avec dataFolderList, ou bien url genre www.hypersolutions.fr
			slDotRdfFile = getSLDotRdfFileUsingCreationMonth(dataFolder, docUri);
		}

		base = dataFolder.getBase(slDotRdfFile);
	}
	
	public SLDataFolder getDataFolder() { return this.dataFolder; }
	/** The base to be used when reading or writing the file */
	public String getBase() { return this.base; }
	public File getFile() { return this.slDotRdfFile; }
}

/** Si docUri est de protocol File, ou si elle est servie par notre serveur, retourne le fichier correspondant, 
 *  sinon return null */
// THE WS QUESTION
File getFileIfLocal(String docUri) throws IOException, URISyntaxException { // HUM TODO ~identique File getFile(String)
	URI docURI = new URI(docUri);
	if ("file".equals(docURI.getScheme())) {
		String filename = docURI.getPath(); // à ne pas oublier ! (cf les %20)
		return new File(filename);
	} else {
		// Deux types de cas : les url du genre apple.com, et celles servies par notre serveur
		if (this.webServer != null) {
			return this.webServer.getFile(docUri);
		}
	}
	return null;
}


/** 
 * Search in dataFolderList the SLDataFolder corresponding to f, if any. 
 * This search is based on f's filename only.
 * attention, c'est un elet de dataFolderList, pas la version éventuellement complétée d'un sous-dossier. 
 * One (positive) effect of using this method is that, by using the path to the file to retrieve the data folder,
 * (and not the creation date), information about a file such as "20004/01/x.htm" is stored in "2004/01", 
 * even if the file has been created at another date (or has no creation date) */
SLDataFolder getDataFolderFromLoadingList(File f) {
	// un petit doute la dessous pour le cas où f est une dir, égale au dataFolder
	String fn = null;
	if (f.isDirectory()) {
		fn = f.getAbsolutePath();
	} else {
		fn = f.getParentFile().getAbsolutePath();
	}
	// rechercher ds dataFolderList
	SLDataFolder x = null;
	if (this.dataFolderListSorted == false) {
		Collections.sort(dataFolderList);
		this.dataFolderListSorted = true;
	}
	// on les prend en sens inverse de façon à se comporter correctement
	// si on a des éléments de dataFolderList inclus ds un autre
	for (int i = dataFolderList.size() - 1; i > -1; i--) {
		SLDataFolder dataFolder = (SLDataFolder) dataFolderList.get(i);
		String dataFolderName = dataFolder.getFile().getAbsolutePath();
		// if (!dataFolderName.endsWith("/")) dataFolderName += "/"; NON cf windaube
		// D'où bug potentiel ici ?
		if (fn.startsWith(dataFolderName)) {
			x = dataFolder;
			break;
		}
	}
	return x;
}

/**
 * Supposes that dataFolder is the SLDataFolder to be used for document docUri,
 * and that docUri corresponds to file docf
 */
File getSlDotRdfFileFromDataFolder(File docf, String docUri, SLDataFolder dataFolder) {
	try {
		// on a trouvé un dossier de dataFolderList (dataFolder) qui est 
		// égal au début du nom long du fichier docf
		// selon le loadingMode, il faut éventuellement le compléter (sous-dossiers, voire
		// jusqu'au bout si on écrit dans le sl.rdf situé à côté de f).
		
		// On pourrait dans le même temps, parce qu'on en aura de toute façon besoin,
		// construire la base correpondante
		// String xBase = dataFolder.getBase();
		
		LoadingMode loadingMode = dataFolder.getLoadingMode();
		if (loadingMode.isAllSubDirsMode()) {
			// Remplacer la fin par sl.rdf
			File dir = docf.getParentFile();
			return new File(dir, "sl.rdf");
		} else if (loadingMode.isSimple()) {
			return new File(dataFolder.getFile(), "sl.rdf");
		} else if (loadingMode.isDepth2()) {
			int depth = 2;
			try {
				File dataFolderFile = dataFolder.getFile();
				// calcul du path relatif à dataFolderFile
				// list va contenir ses elements, en ordre inverse
				ArrayList list = new ArrayList();
				File ff = docf.getParentFile();
				for(;((ff != null) && (!(ff.equals(dataFolderFile))));) {
					list.add(ff.getName());
					ff = ff.getParentFile();			
				}
				// on complète dataFolderFile.
				// Dans les cas autres que isYearMonthMode et isYearSubMode,
				// il s'agit juste de compléter avec les éléments de list.
				// Plus délicat si isYearMonthMode : si le path relatif est fait de numeric,
				// on les prend tels quels. Mais sinon, essayer avec la date de création
				int n = list.size();
				int nfin = n - 1 - depth;
				if (nfin < 0) nfin = -1;
				File fx = dataFolderFile;
				// on complète avec le path relativ, sauf s'il ne convient pas dans
				// les cas isYearMonthMode
				if (loadingMode.isYearMonthMode()) {
					// boolean relativPathOK  si true, on prend la path du fichier 
					boolean relativPathOK = true;
					// TEL QUE C'EST, un fichier au même niveau que les années est considéré ok
					// il faut déjà que la path soit assez long
					// boolean relativPathOK = (n > 1);
					// et il faut que les elts soient numériques
					// if (relativPathOK) {
					try {
						for (int i = n-1; i > nfin; i--) {
							String pathItem = (String) list.get(i);
							Integer.parseInt(pathItem);
						}
					} catch (NumberFormatException e) {
						relativPathOK = false;
					}
					// }
					if (!relativPathOK) {
						if (docUri != null) { // cf truc abusif ds class DocsFile.toString
							YYYYMM yyyymm = doc2YYYYMM(getDocument(docUri));
							if (yyyymm != null) {
								fx = new File(fx, yyyymm.yyyy);
								fx = new File(fx, yyyymm.mm);
							} else {
								fx = (new YearMonthDay()).yearMonthAsFolder(fx);
							}
						} else {
							System.err.println("should not happen 1");
						}						
						return new File(fx, "sl.rdf");
					}
				} else if (loadingMode.isYearSubMode()) {
					boolean relativPathOK = true;
					// il faut que "l'année" soit numérique
					// if (relativPathOK) {
					try {
						String pathItem = (String) list.get(n-1);
						Integer.parseInt(pathItem);
					} catch (NumberFormatException e) {
						relativPathOK = false;
					}
					// }
					if (!relativPathOK) {
						if (docUri != null) { // cf truc abusif ds class DocsFile.toString
							YYYYMM yyyymm = doc2YYYYMM(getDocument(docUri));
							// je prends ici le fichier au niveau année, mais pourquoi pas année/mois (à part pour fichiers existants) ?
							if (yyyymm != null) {
								fx = new File(fx, yyyymm.yyyy);
							} else {
								fx = (new YearMonthDay()).yearAsFolder(fx);
							}
							fx = new File(fx, "sl.rdf");
						} else {
							System.err.println("should not happen 2");
						}						
						return new File(fx, "sl.rdf");
					}
				} // si pas de return jusque là, il est ok d'utiliser le relativ path
				
				for (int i = n-1; i > nfin; i--) {
					String pathItem = (String) list.get(i);
					fx = new File(fx, pathItem);
				}
				return new File(fx,"sl.rdf");
			} catch (Exception e) {
				System.err.println(docf + " xxxxx " + dataFolder.getFile());
				e.printStackTrace();
				return new File(dataFolder.getFile(), "sl.rdf");
			}
		} else {
			throw new RuntimeException("Unexpected loadingMode");
		} // case loadingMode
	} catch (Exception e) {
		System.err.println("Exception getDocsFileNameWithLoadingList " + docf);
		e.printStackTrace();
	}
	return null;
}


// A Mettre dans SLDataFolder ?
/** To be used when we don't have other clues about where to write the sl.rdf file.
 *  Supposes that dataFolder is the right dataFolder to store info about docUri. */
File getSLDotRdfFileUsingCreationMonth(SLDataFolder dataFolder, String docUri) {
	File x = dataFolder.getFile();
	if (!dataFolder.getLoadingMode().supportsYYYYMM()) {
		// we'll write the rdf file just inside dataFolder.getFile()
	} else {
		SLDocument doc = this.getDocument(docUri);
		YYYYMM yyyymm = doc2YYYYMM(doc);
		if (yyyymm == null) {
			// we don't know the creation month
			// can be:
			// - a doc whose creation date has not been set (should not happen, except for data produced by very old releases of semanlink),
			// or whose creation date has been deleted
			// - a doc that has not been created yet (new doc)
			
			if (existsAsSubject(doc)) {
				// on pourrait lui affecter une date de création
				// we'll write the rdf file just inside dataFolder.getFile()
			} else {
				// new document
				x = todayYearMonthDir(dataFolder.getFile());
			}
		} else {
			x = new File(x,yyyymm.yyyy);
			x = new File(x,yyyymm.mm);		
		}
	}
	return new File(x,"sl.rdf");
}



public void setMetadataExtractorManager(MetadataExtractorManager metadataExtractorManager) {
	this.metadataExtractorManager = metadataExtractorManager;
}
public MetadataExtractorManager getMetadataExtractorManager() { return this.metadataExtractorManager; }

// 2008/02
// abstract public SLKeyword homePage2Tag(String homePageUri);
/** Search tag such as <tag,propUri,objectUri> */
abstract public SLKeyword object2Tag(String propUri, String objectUri);

/** list of rdf:type used for tags. */
abstract public Iterator rdfTypes4Tags();

// 2017-04 saving modified md files
public void saveDocFile(String docUri, String docContent) throws IOException, URISyntaxException {
	File docf = getFileIfLocal(docUri);
	if (docf == null) throw new RuntimeException("Not a local file");
	// should not happen, so verify it (could probably be removed, )
	if (!docf.exists()) throw new RuntimeException("Unexpected");
	InputStream in = new ByteArrayInputStream(docContent.getBytes(StandardCharsets.UTF_8));
  OutputStream out = new FileOutputStream(docf);
	CopyFiles.writeIn2Out(in, out, new byte[1024]);
	in.close();
	out.close();

}

//
// uris for bookmarks // 2019-04
//

abstract public SLDocument convertOld2NewBookmark(String onlineUri) throws Exception;

// DocMetadataFile doc2DocMetadataFile(String docUri)

/**
 * Pour calculer l'uri à utiliser pour un new bookmark
 * ATTENTION, le bkm est créé ds default datafolder : suppose (en gros) que c'est pour une bkm externe
 * (ne peut pas être utilisé pour un doc ldans une dir locale d'un des SLDataFolders qui ne serait pas ds default folder)
  */
public static class NewBookmarkCreationData {
	private SLDocument bkm;
	/** the dir to save the file if we save a copy */
	private File saveAsDir;
	private String shortFilename; // sans dot extension, par ex "titre_du_bkm_2" 
	
	// ATTENTION, le bkm est créé ds default datafolder : suppose que c'est pour une bkm externe
	public NewBookmarkCreationData(SLModel mod, String title) throws MalformedURLException, URISyntaxException {
		File bkmDir = mod.dirToSaveBookmarks();
		// the dir to save the file if we save a copy
		saveAsDir = mod.goodDirToSaveAFile();
		if (title == null) throw new RuntimeException("No title");	  
		String sfn = SLUtils.title2shortFilename(title); // ATTENTION, ne convertit pas tout, ex : "«"
		String shortFilename = sfn; // sans dot extension, par ex "titre_du_bkm"
		SLDocument bkm = null;
//		File saveAs = null; // the file for the saveAs
		int i = 0;
		String bkmUri = null;
		for(;;) {
			bkmUri = mod.fileToUri(new File(bkmDir, shortFilename));
			bkm = mod.getDocument(bkmUri);
			if (!mod.existsAsSubject(bkm)) {
				
				// vérifier que le nom du fichier où on sauverait le download n'exsite pas déjà
//				saveAs = new File(saveAsDir, ln + dotExtension);
//				if (!saveAs.exists()) {
//					break;
//				}
				// en fait on va faire plus : s'assurer que là où on sauverait le download
				// il n'y a pas aucun fichier downloadshortname "." ext
				String[] names = saveAsDir.list();
				boolean ok = true;
				if (names != null) {
					for (String name : names) {
						if (name.equals(shortFilename)) {
							ok = false;
							break;
						}
						if (name.startsWith(shortFilename + ".")) {
							ok = false;
							break;
						}
					}
				}
				if (ok) {
					break;
				}
			}
			i++;
			shortFilename = sfn + "_" + i;												
		}
		this.bkm = bkm;
		this.shortFilename = shortFilename;
	}
	
	public SLDocument getSLDocument() { return this.bkm ; }
	public File getSaveAsFile(String dotExtension) { 
		return new File(saveAsDir, shortFilename + dotExtension);
	}
}



} // class SLModel
