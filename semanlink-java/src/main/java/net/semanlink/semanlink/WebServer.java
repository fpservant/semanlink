package net.semanlink.semanlink;
import java.util.*;
import java.net.*;
import java.io.*;
/*
 * pb / ou pas à la fin des files et url
 * les files : que des dir ?
 * recherche de url of file ou contraire : trier les associations
 * (car gaffe au cas où on a des urls ou file qui commencent par la même chose)
 * on ne se préoccupe donc pas ici du cas où on aurait des associations dont les dir (ou uri ?)
 * commencent pareil
 * @author fps
 */
/**
 * Represents the web server that serves documents referenced in Semanlink.
 */
public class WebServer {
// private ArrayList associations = new ArrayList(10);
/** les associations, en ordre INVERSE de leurs uri. */
private ArrayList associationsSortedByUri = new ArrayList(10);
/** les associations, en ordre INVERSE de leurs dir. */
private ArrayList associationsSortedByDir = new ArrayList(10);
static private ByUriComparator byUriComparator = new ByUriComparator();
static private ByDirComparator byDirComparator = new ByDirComparator();

// can be served at semanlink/document/...
private File defaultDocFolder;

/** ne fait rien si uri est déjà uri d'un Mapping. */
public void addMapping(URI uri, File dir) {
	// this.associations.add(new Mapping(uri, dir));
	Mapping assoc = new Mapping(uri, dir);
	uri = assoc.uri;
	for (int i = 0; i < associationsSortedByUri.size(); i++) {
		if (uri.equals(((Mapping) associationsSortedByUri.get(i)).uri)) return;
	}
	this.associationsSortedByUri.add(assoc);
	Collections.sort(this.associationsSortedByUri, byUriComparator);
	this.associationsSortedByDir.add(assoc);
	Collections.sort(this.associationsSortedByDir, byDirComparator);
}

// 2015-10 @find CORS pb with markdown
public void setDefaultDocFolder(File defaultDocFolder) {
	this.defaultDocFolder = defaultDocFolder;
}

public File getDefaultDocFolder() {
	return this.defaultDocFolder;
}

/** Represents the association between an url and a dir whose content is served by this WebServer */
public class Mapping {
	File dir;
	URI uri;
	/**
	 * @param uri must be "/" terminated
	 * @param dir must be a directory
	 */
	public Mapping(URI uri, File dir) {
		if (!(dir.exists())) throw new IllegalArgumentException("\"" + dir + "\" doesn't exist.");
		if (!(dir.isDirectory())) throw new IllegalArgumentException("Only dirs are allowed.");
		String s = uri.toString();
		if (!s.endsWith("/")) {
			// throw new IllegalArgumentException("uri should be slash terminated.");
				try { // TODO change
					uri = new URI(s + "/");
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
		}
		// System.out.println("WebServer.Mapping.NEW: " + uri + " ; " + dir);
		this.uri = uri;
		this.dir = dir;
	}
}

/** pour trier les associations en ordre INVERSE de leurs uri. */
static class ByUriComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		Mapping assoc0 = (Mapping) arg0;
		Mapping assoc1 = (Mapping) arg1;
		String s0 = assoc0.uri.toASCIIString();
		String s1 = assoc1.uri.toASCIIString();
		return s1.compareTo(s0);
	}
}
/** pour trier les associations en ordre INVERSE de leurs dir. */
static class ByDirComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		Mapping assoc0 = (Mapping) arg0;
		Mapping assoc1 = (Mapping) arg1;
		String s0 = assoc0.dir.getPath();
		String s1 = assoc1.dir.getPath();
		return s1.compareTo(s0);
	}
}

/**
 * Retourne null si ce fichier n'est pas servi par ce serveur.
 * ATTENTION, même genre de code ds FileUriFormat.fileToUriBuggy(String dirUri, String shortFilename) (code copié/chnagé
 * cf URLUTF8Encoder.encode. Lequel a raison ? (if any)
 * @throws URISyntaxException
 * 
 * Rq : pas de AccentComposer.composeAccents ici
 * 
 * CA, c'est du passé :
 * Me retourne du genre :
 * http://127.0.0.1/test/Fran%c3%a7ois-Paul%20Servant.htm (avant changeemnt table hex ds URLUTF8Encoder
 * pour que justement on ait du  %C3%A7
 * OU EST LA DIFF AVEC CE QUI DONNE DU %C3%A7 (par ex ds sl.rdf)
 */
public String getURI(File file) throws URISyntaxException {
	String fn = file.getPath();
	// System.out.println("fn: " + fn);
	// cause windaube
	fn = fn.replace('\\','/'); 
	if (!fn.startsWith("/")) fn = "/" + fn;

	// fn = AccentComposer.composeAccents(fn); // faudrait-il ?
	
	// on suppose que associationsSortedByDir est en ordre inverse des noms des dirs des assocsiations :
	// ceci pour retourner l'assoc de path le plus long et qui est égal au début de fn
	for (int i = 0; i < this.associationsSortedByDir.size(); i++) {
		Mapping assoc = (Mapping) associationsSortedByDir.get(i);
		String assocPath = assoc.dir.getAbsolutePath();
		// System.out.println("assocPath: " + assocPath);
		
		// cause windaube, java.net.URISyntaxException: Relative path in absolute URI si on ne force pas un / au début
		assocPath = assocPath.replace('\\','/'); 
		if (!assocPath.startsWith("/")) assocPath = "/" + assocPath;

		if (fn.startsWith(assocPath)) {
			// fn servi ds le cadre de cette assoc
			String endFilename = fn.substring(assocPath.length());
			if (endFilename.startsWith("/")) endFilename = endFilename.substring(1); // peut arriver si manque un / en fin de assocPath
			/* ceci est ok pour des noms de fichiers sans pb.
			 * Mais s'il y a des car à pbs ? Ca ne va pas : resolve plante sur un illegal char
			 * Faut encoder
			 * MAIS JE NE COMPRENDS PAS : ds FileUriFormat.fileToUri(String dirUri, String shortFilename),
			 * il y a URLUTF8Encoder.encode. Il me semble que ça devrait être encodeFilename 
			 * MAIS IL Y A UN PROBLEME, cf FileUriFormat.fileToUriBuggy : les encoder
			 * (URLUTF8 ou URLENcoder) convertissent les virgules, ce que ne fait pas
			 * le constructeur de URI, légitimement utilisé par ailleurs. Il faut donc se baser
			 * sur quelque chose genre SLUtils.laxistRelativPath2Uri*/
			// URI x = assoc.uri.resolve(URLUTF8Encoder.encodeFilename(endFilename));
			// A vérifier : // TODO
			URI x = new URI(SLUtils.laxistRelativPath2Uri(assoc.uri.toString(), endFilename));
			return x.toASCIIString();
		}
	}
	
	return null;
}

/** Cette uri est-elle servie par ce WebServer ? */
public boolean owns(String docUri) throws IOException, URISyntaxException {
	return (assoc(new URI(docUri)) != null);
}

/** null si pas servie par ce server */
private Mapping assoc(URI docURI) throws IOException, URISyntaxException {
	// relativize
	// on suppose que associationsSortedByUri est en ordre inverse des noms des uri des assocsiations :
	// ceci pour retourner l'assoc de uri la plus longue et qui est égale au début de fndocURI
	for (int i = 0; i < this.associationsSortedByUri.size(); i++) {
		Mapping assoc = (Mapping) this.associationsSortedByUri.get(i);
		URI rootURI = assoc.uri;
		URI rel = rootURI.relativize(docURI);
		if (rel.equals(docURI)) continue;
		return assoc;
	}
	return null;
}

/** Retourne le fichier correspondant à une URL servi par ce WebServer, ou null s'il ne la sert pas. */
public File getFile(String docUri) throws IOException, URISyntaxException {
	URI docURI = new URI(docUri);
	Mapping assoc = assoc(docURI);
	// 2015-10 @find CORS pb with markdown 
	// (ex chez moi: les fichiers du main datafolder sont servis par apache
	// et ko pour cors)
	// Adding the possibility to serve files inside the default data folder using 
	if (assoc == null) return null;
	URI rootURI = assoc.uri;
	// je ferais bien qlq chose genre :
	// String rel = docUri.substring(rootUri.length());
	// return assoc.file + rel;
	// mais il y a le pb des file separator
	String docFilename = docURI.getPath(); // à ne pas oublier ! (cf les %20) mais...
	// mais retourne quelque chose genre /~fps/...
	String rootFilename = assoc.dir.getAbsolutePath();
	/*if (!(rootFilename.endsWith("/"))) rootFilename += "/"; // QUOI ??? (2005/12) : et windaube ??? // TODO
	docFilename = rootFilename + docFilename.substring((rootURI).getPath().length());
	// System.out.println("WebServer.getFile de " + docUri + " returns " + docFilename);
	return new File(docFilename);*/
	// 2006/09
	File root = new File(rootFilename);
	return new File(root, docFilename.substring((rootURI).getPath().length()));
}

} // 
