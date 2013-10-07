/* Created on 7 oct. 03 */
package net.semanlink.semanlink;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

import com.hp.hpl.jena.rdf.model.Property;

import net.semanlink.servlet.SLServlet;
import net.semanlink.util.FileUriFormat;
/**
 * @author fps
 */
public class SLUtils {
//
//CREATING NEW FILES
//
/**
 * Creates a new empty SL file. 
 */
public static void newEmptySLFile(String filename) throws IOException {
	File dir = (new File(filename)).getParentFile();
	if (!dir.exists()) {
		dir.mkdirs();
	}
	PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
	out.println("<?xml version=\"1.0\" ?>");
	out.println("<!DOCTYPE rdf:RDF [");
	out.println("\t<!ENTITY sl  'http://www.semanlink.net/2001/00/semanlink-schema#'>");
	// out.println("\t<!ENTITY kw  'http://www.hypersolutions.fr/2001/00/vocab#'>");
	out.println("]>");
	out.println("<rdf:RDF");
	out.println("\txmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
	out.println("\txmlns:sl='http://www.semanlink.net/2001/00/semanlink-schema#'");
	out.println(">");
	out.println("</rdf:RDF>");
	out.flush();
	out.close();
}
 
//
//SORT
//

public static void sortDocsByKws(List docList) {
	Collections.sort(docList, new CompKwsList());
}

public static void sortDocsByKws(List docList, SLKeyword[] dontUseToSort) {
	Collections.sort(docList, new CompKwsList(dontUseToSort));
}


public static void sortByProperty(List slResList, String propUri) {
	if  (propUri.equals(SLVocab.SL_CREATION_TIME_PROPERTY)) {
		Collections.sort(slResList, COMP_CREATION_TIME);
	} else if  (propUri.equals(SLVocab.SL_CREATION_DATE_PROPERTY)) {
			Collections.sort(slResList, COMP_CREATION_TIME);
	} else if ( (propUri.indexOf("date") > -1) || (propUri.indexOf("Date") > -1) ) {
		Collections.sort(slResList, new CompPropertyReverseOrder(propUri));
	} else {
		Collections.sort(slResList, new CompProperty(propUri));
	}
}

public static void reverseSortByProperty(List slResList, String propUri) {
	Collections.sort(slResList, new CompPropertyReverseOrder(propUri));
}

/** Permet de trier des SLResources selon une propriété.
 * (note : la 1ere valeur de la prop est utilisée)
 */
static class CompProperty implements Comparator {
	private String propUri;
	CompProperty(String propUri) { this.propUri = propUri; }
	public int compare(Object arg0, Object arg1) {
		return getProp((SLResource) arg0).compareTo( getProp((SLResource) arg1) );
	}
	public String getProp(SLResource res) {
		String x = null;
		PropertyValues pv = res.getProperty(propUri);
		if (pv != null) x = pv.getFirstAsString();
		if (x == null) x = "";
		return x;
	}
}

/** Permet de trier des SLResources selon une propriété.
 * (note : la 1ere valeur de la prop est utilisée)
 */
static class CompPropertyReverseOrder extends CompProperty {
	CompPropertyReverseOrder(String propUri) { super(propUri); }
	public int compare(Object arg0, Object arg1) {
		return super.compare(arg1, arg0);
	}
}

/** 
 * To take care of old versions of sl where creation time property was not set
 * It's a reverse order. */
static CompCreationTime COMP_CREATION_TIME = new CompCreationTime();
static class CompCreationTime implements Comparator {
	public int compare(Object arg0, Object arg1) {
		return getProp((SLDocument) arg1).compareTo( getProp((SLDocument) arg0) );
	}
	public String getProp(SLResource res) {
		String x = null;
		PropertyValues pv = res.getProperty(SLVocab.SL_CREATION_TIME_PROPERTY);
		if (pv != null) {
			x = pv.getFirstAsString();
		} else {
			pv = res.getProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
			if (pv != null) {
				x = pv.getFirstAsString();
			}
		}
		if (x == null) x = "";
		return x;
	}	
}

/** Permet de trier des docs par kws. 
 *  Rq : ça doit être un peu lent. Peut s'optimiser, mais pas très facilement
 *  (pas besoin de calculer le toString (d'ailleurs complexe) complet à chaque fois :
 *  il suffit souvent du début pour pouvoir déterminer le + petit) TODO
 */
static class CompKwsList implements Comparator {
	private SLKeyword[] dontUseToSort;
	CompKwsList() {};
	CompKwsList(SLKeyword[] dontUseToSort) { this.dontUseToSort = dontUseToSort;};
	
	/*public int compare(Object arg0, Object arg1) {
		return toString((SLDocument) arg0).compareTo( toString((SLDocument) arg1) );
	}*/
	/** returns a negative integer, zero, or a positive integer 
	 *  as the first argument is less than, equal to, or greater than the second.*/
	public int compare(Object arg0, Object arg1) {
		SLDocument doc0 = (SLDocument) arg0;
		SLDocument doc1 = (SLDocument) arg1;
		List kws0 = doc0.getKeywords(); // ils sont triés
		List kws1 = doc1.getKeywords(); // ils sont triés
		int n0 = kws0.size();
		int n1 = kws1.size();
		int i0=0, i1=0;
		for(;;) {
			if (i0 >= n0) {
				if (i1 >= n1) {
					return 0;
				} else {
					return -1;
				}
			} else {
				if (i1 >= n1) return 1;
			}
			Object kw0 = kws0.get(i0);
			if (dontUse(kw0)) {
				i0++;
				continue;
			}
			Object kw1 = kws1.get(i1);
			if (dontUse(kw1)) {
				i1++;
				continue;
			}
			int x = ((SLKeyword) kw0).compareTo(kw1);
			if (x != 0) return x;
			i0++;
			i1++;
		}
	}
	
	private boolean dontUse(Object kw) {
		if (dontUseToSort != null) {
			for (int j = 0; j < dontUseToSort.length; j++) {
				if (dontUseToSort[j].equals(kw)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String toString(SLDocument doc) {
		List kws = doc.getKeywords(); // ils sont triés
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < kws.size(); i++) {
			Object kw = kws.get(i);
			if (dontUseToSort != null) {
				boolean dontUse = false;
				for (int j = 0; j < dontUseToSort.length; j++) {
					if (dontUseToSort[j].equals(kw)) {
						dontUse = true;
						break;
					}
				}
				if (dontUse) continue;
			}
			sb.append(kw.toString());
		}
		return sb.toString();
	}
}

/** Calcul des kws liés à un kw. 
 *  Un kw est lié à kw ssi il ont un document en commun.
 * // il y a une version avec les nb dans Jsp_Keyword
 */
public static SLKeyword[] getLinkedKeywords(SLKeyword kw) {
	HashSet hs = getKeywords(kw.getDocuments());
	// retirer kw
	hs.remove(kw);
	// et ses enfants
	/*List children = kw.getChildren();
	for (int i = 0; i < children.size(); i++) {
		hs.remove(children.get(i));
	}*/
	SLKeyword[] x = (SLKeyword[]) hs.toArray(new SLKeyword[0]);
	Arrays.sort(x);
	return x;
}



public static HashSet getKeywords(List docs) {
	HashSet hs = new HashSet();
	for (int i = 0; i < docs.size(); i++) {
		SLDocument doc = (SLDocument) docs.get(i);
		List kws = doc.getKeywords();
		for (int j = 0; j < kws.size(); j++) {
			hs.add(kws.get(j));
		}
	}
	return hs;
}

// TAG CLOUD
/** retourne un HashMap de clé des SLKeyword et de data un Integer représentant le nb d'occurrences du kw. */
public static HashMap getLinkedKeywords2NbHashMap(List docs) {
	HashMap hm = new HashMap();
	Integer un = new Integer(1);
	for (int i = 0; i < docs.size(); i++) {
		SLDocument doc = (SLDocument) docs.get(i);
		List kws = doc.getKeywords();
		for (int j = 0; j < kws.size(); j++) {
			SLKeyword kw = (SLKeyword) kws.get(j);
			Integer nb = (Integer) hm.get(kw);
			if (nb == null) {
				hm.put(kw, un);
			} else {
				hm.put(kw, new Integer(nb.intValue()+1));
			}
		}
	}
	return hm;
}


//
//
//

/** 
 * Pour retourner une uri correctement encodée, même avec quelque chose comme :
 * http://.../a b/test ééé.html (ou file:/a é.html)
 * Si l'uri n'est pas quotée, elle est retournée quotée. 
 * si elle est une uri correctement quotée, elle est retournée telle qu'elle.
 * Attention, il suffit qu'un car ne soit pas quoté pour que tout le soit (et dans
 * ce cas, les % préexistants sont quotés en %25)
 * Traite aussi le cas des uri file:/xxx, transformées en file:///xxx
 */
public static String laxistUri2Uri(String laxistUri) throws URISyntaxException {
	try {
		// On recherche l'exception émise si l'uri est laxiste 
		// ceci qui parait bien a priori (pour traiter le file:/ en particulier)
		// accepte "test ééé" dans broncher !!!
		// new com.hp.hpl.jena.rdf.arp.URI(laxistUri);

		// Everything was allright with the following code
		// new URI(laxistUri);
		// return FileUriFormat.fileSlashSlashSlashProblem(laxistUri);
		// a laxist uri like the one obtained when bookmarking a page such as the one pointed by:
		// <A HREF="test%20e%cc%81e%cc%81e%cc%81.htm">
		// (generated by apache for a file called "test ééé.htm")
		// giving here (see Action_BookmarkForm):
		// ..../test%20ééé
		// generates an exception in new URI, and the handling of the exception takes care of the problem.
		// All was good until I tried to bookmark the foolowing url:
		// http://dowhatimean.net/2006/05/juc-francois-paul-servant-?-semanlink
		// (where the "-" before semanlink are not what they seem to be
		// here a href linking to that page:
		// <a href="http://dowhatimean.net/2006/05/juc-francois-paul-servant-%e2%80%93-semanlink">
		// (there is 
		URI uri = new URI(laxistUri);
		return FileUriFormat.fileSlashSlashSlashProblem(uri.toASCIIString());
	} catch (Exception e) {
		// on prend le tout début de l'url que l'on considère correct,
		// et la suite que l'on considère laxiste -- cad sans car quoté
		String s = laxistUri;
		int k = s.indexOf("//");
		String debut = s.substring(0,k+2);
		String fin = s.substring(k+2);
		if (fin.startsWith("/")) {
			debut = debut + "/";
			fin = fin.substring(1);
		}
		k = fin.indexOf('/');
		debut = debut + fin.substring(0,k+1);
		fin = fin.substring(k+1);
		return laxistRelativPath2Uri(debut,fin);
	}
}

/** Pour retourner une uri correctment encodée, même avec quelque chose comme :
 *  http://.../test ééé.html (ou file:/a é.html)
 *  ATTENTION, DS CETTE VERSION, SEULE LA DERNIERE PARTIE DE L'URL PEUT ETRE LAXIST
 */
/*public static String laxistUri2UriV1(String laxistUri) throws URISyntaxException {
	try {
		// On recherche l'exception émise si l'uri est laxiste 
		// ceci qui parait bien a priori (pour traiter le file:/ en particulier)
		// accepte "test ééé" dans broncher !!!
		// new com.hp.hpl.jena.rdf.arp.URI(laxistUri);
		new URI(laxistUri);
		return FileUriFormat.fileSlashSlashSlashProblem(laxistUri);
	} catch (Exception e) {
		String shortName = Util.getLastItem(laxistUri, '/');
		String dir = Util.getWithoutLastItem(laxistUri,'/');
		// rq : ceci marche aussi pour http, ne suppose pas file:
		return laxistRelativPath2Uri(dir,shortName);  		
	}
}*/


/**
 * @param dirUri peut être une uri http locale MAIS SUPPOSEE CORRECTE (sinon URISyntaxException)
 * ET SANS CAR QUOTES (sinon les % sont réencodés en %25 par new URI -- à mon avis à tort : bug java ?)
 * Elle peut commencer par file:/, parce qu'on y force à retourner file:///
 * (sans un code dédié, retournerait systématiquement file:/, même si 
 * dirUri était en file:///)
 * ATTENTION : si le laxistRelativPath est une dir, on aura probablement envie d'avoir
 * une uri "/" terminated, auquel cas il faudra veiller à ce que shortFilename le soit
 * (Il y a une raison forte pour vouloir une uri de dir "/" terminated :
 * on a besoin d'une telle uri pour retrouver une ppté de ce doc ds le modèle
 * enfin, on a besoin que ça soit traité partout de la même façon :	or, on
 * a des "/" ds le modèle ? - probablement d'ailleurs suite à cette méthode, 
 * 
 * Attention, pas de fragment, de query,...
 */
static public String laxistRelativPath2Uri(String dirUri, String laxistRelativPath) throws URISyntaxException {
	if (!(dirUri.endsWith("/"))) {
		dirUri = dirUri + "/";
	}
	// il est bien possible qu'on puisse faire mieux que ça :
	URI dirURI = new URI(dirUri);
	// et merde ! la ligne suivante fait passer de file:/// à file:/ !!!
	// URI uri = new URI(dirURI.getScheme(), dirURI.getHost(), dirURI.getRawPath(), dirURI.getFragment());
	// ceci, parce que le host retourné est null, alors que si on y met "", ca ira
	String scheme = dirURI.getScheme();
	String host = dirURI.getHost();
	if ("file".equals(scheme)) {
		if (host == null) host = ""; // question du file:/// : ceinture
	}
	//	 ce code pose problème car certains car, comme la VIRGULE, sont codés en %2C par
	//	 URLUTF8Encoder (toute version) et URLEncoder.encode
	//	 OR new URI(,,,,) (utilisé ds la version à un seul arg de fileToUri) ne code pas la virgule.
	// URI uri = new URI(scheme, host, dirURI.getRawPath(), dirURI.getFragment());
	// uri = uri.resolve(URLUTF8Encoder.encodeFilename(laxistRelativPath)); // faut-il URLUTF8Encoder.encode ?
	// URI uri = new URI(scheme, host, dirURI.getRawPath() + laxistRelativPath, null); // pas bon : pas de port
	URI uri = new URI(scheme, null, host, dirURI.getPort(), dirURI.getRawPath() + laxistRelativPath, null, null);
	String x = uri.toASCIIString();
	return FileUriFormat.fileSlashSlashSlashProblem(x); // question du file:/// : et bretelle
}

/** Quote à la mode uri un chemin relatif (met des % là où il en faut).
 *  (attention, si la path donné est déjà quoté, ne le quote pas une seconde fois)
 */
static public String notQuotedToQuotedUriRelativPath(String notQuotedRelativPath) throws URISyntaxException {
	// Cette implémentation est un hack, fondé sur new URI(à plein de params)
	// il y a peut-être plus simple
	String path = null;
	boolean slashAdded;
	if (notQuotedRelativPath.startsWith("/")) {
		path = notQuotedRelativPath;
		slashAdded = false;
	} else {
		path = "/" + notQuotedRelativPath;
		slashAdded = true;
	}
	String fragment = null;
	int k = path.indexOf("#");
	if (k > 0) {
		// not tested
		path = path.substring(0,k-1);
		fragment = path.substring(k+1);
	}
	URI uri = new URI("http", "www.semanlink.net", path, fragment); // www.semanlink.net : c'est un hack. ca va être viré plus bas
	
	// pas de uri.toASCIIString(); ???
	/*// peut-être bon
	String x = uri.getRawPath();
	if (slashAdded) {
		// not tested
		if (x.startsWith("/")) x = x.substring(1);
	}
	if (fragment != null) {
		// not tested
		x = x + "#" + fragment;
	}
	*/
	String x = uri.toASCIIString();
	if (slashAdded) {
		x = x.substring("http://www.semanlink.net/".length());
	} else {
		// not tested
		x = x.substring("http://www.semanlink.net".length());
	}
	return x;
}

/**
 * @see SLDocument.getLabel()
 */
static public String getLabel(SLDocument doc) throws IOException, URISyntaxException {
	String x = doc.getLabel();
	if (x != null) return x;
	// par defaut, on prend la fin de l'uri
	x = doc.getURI();
	WebServer webServer = SLServlet.getWebServer();
	boolean useLastItemOnly = !(x.endsWith("/"));
	// boolean useLastItemOnly = true;
	useLastItemOnly = useLastItemOnly && (((webServer != null) && (webServer.owns(x))) || (x.startsWith("file:")));
	if (useLastItemOnly) {
		// if (x.endsWith("/")) x = x.substring(0, x.length()-1);
		int n = x.lastIndexOf('/');
		x = x.substring(n+1);
	} 
	// comme uri est encodee, il faut la decoder pour l'afficher sous une forme "human compliant"
	try {
		x = URLDecoder.decode(x, "UTF-8"); // hum ; decode ou toString bien choisi de uri ?
	} catch (UnsupportedEncodingException e) { throw new SLRuntimeException(e); }
	return x;
}
}

