package net.semanlink.util;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

// import com.hp.hpl.jena.iri.impl.XercesURI;

/** Used to define the way we get an uri string from a filename.
 * 
 *  Méthode fileToUri(File)
 * 
 *  La classe contient de nombreuses variantes d'implémentation et tests.
 *  Celles numerotées (V1, ...) ont été à un moment donné "le best choice".
 *
 *  Deux questions sont traitées (avec plus ou moins de bonheur) à partir de la version V1,
 *  et une ne se pose pas.
 * 
 *  1) D'abord celle de l'encoding des noms de fichiers. Une URI par exemple ne doit
 *  pas contenir d'espace (mais des %20 à la place)
 *  Pour ce qui est des caractères UTF-8, java 1.4 les laisse dans les uri (uri.toString())
 *  mais on peut vouloir les encoder (utiliser uri.toASCIIString())
 * 
 *  2) Problème avec Jena 1.2 quand on ecrit des fichiers contenant des url
 *  de protocole file : elles doivent commencer par "file:///".
 *  Si les urls (utilisée pour l'argument base et les file urls au sein du modele)
 *  commencent par "file:/", le fichier ne contient que des urls absolues, pas relatives.
 *  Comme il se trouve que java sur MacOSX retourne les url sous cette forme, on ne peut
 *  utiliser ce qui serait la méthode normale pour proceder - file.toURI().toASCIIString() -
 * 
 *  3) il n'y a pas à se preoccuper de la question des noms de fichiers comportant des e accents aigus,
 *  (lors du retour de File.list sur MacOS, il faut leur faire subir un traitement pour les afficher)
 *  parce que la transformation faite ici donne un résultat correct, (le même, je crois) quelque soit 
 *  la forme de l'entrée.
 * 
 * 	Reste le problème de la dé-relativisation dans Jena d'une uri
 *  d'un nom de fichier contenant un ":" (comme en retourne macosx quand le nom de fichier contient un "/"
 *  sous Finder). Pb qui a plusieurs aspects :
 *  - une fois relativisée, une url que l'on voudrait relative contenant un ":" peut s'interpréter comme
 *  une url avec un scheme. Dépend de la facon de lire le fichier. Mais est-il "ds la norme" d'avoir des url
 *  relatives au fichier contenant dans un fichier rdf ? Si non, on peut toujours utiliser une url absolue
 *  par rapport à un namespace convenu, signifiant : "convertir en uri relative au fichier".
 *  rdf contenant une "base"
 */
public class FileUriFormat {
/**
 * Converts a File to an uri. 
 * 
 * Java 1.4 javadoc about URI states : 
 * The multi-argument constructors quote illegal characters as
 * required by the components in which they appear.  The percent character
 * ( '%' ) is always quoted by these constructors.  Any other characters are preserved.
 * -> every character in the output should be legal. Passing through toASCIIString
 * encodes non ASCII chars -> the result should strictly conform to RFC2396.
 *
 * Returns something beginning with file:/// -- ok to be used with jena.
 * (file:/ returned by file.toURI() is not ok with jena, there is a problem 
 * relativizing uris when writing to file)
 * 
 * Not exactly the same thing as fileToUriV1 with uri pointing to directories
 * 
 * ACHTUNG, modif ici non répercutée dans les autres implementations :
 * ensures that result is / terminated if file is a directory.
 * 
 * ACHTUNG : code à peu près dupliqué ds WebServer.association :
 * si on change ici, changer là-bas (ou factoriser le code)
 */
static public String fileToUri(File file) throws URISyntaxException {
	// "" et non null dans le host, c'est important voir question du file:/// ds fileToUri(String, String)
	// (avec null, on obtiens un seul slash)
	// URI uri = new URI ("file", "", file.getAbsolutePath(), null);
	String s = file.getAbsolutePath();
	// cause windaube, java.net.URISyntaxException: Relative path in absolute URI si on ne force pas un / au début
	// if (!s.startsWith("/")) {
		s = s.replace('\\','/'); 
		if (!s.startsWith("/")) s = "/" + s;
	// }
	URI uri = new URI ("file", "", s, null);
	// ATTENTION ENCORE UN PB QUAND ON COMPARE à URLEncoder - qui est utilisé ds fileToUri(String dirUri, String shortFilename)
	// alors qu'on a bien ici, par ex, des %20 (ce qu'on n'a d'ailleurs pas avec URLEncoder) et des %machins,
	// on garde les VIRGULES, alors que (tous) les url encoder les transforment en %2C
	String x = uri.toASCIIString();
	if (file.isDirectory()) {
		if (!(x.endsWith("/"))) {
			x = x + "/";
		}
	}
	return x;
}

static public URI fileToURI(File file) throws URISyntaxException {
	// "" et non null dans le host, c'est important voir question du file:/// ds fileToUri(String, String)
	// (avec null, on obtiens un seul slash)
	// URI uri = new URI ("file", "", file.getAbsolutePath(), null);
	String s = file.getAbsolutePath();
	// cause windaube, java.net.URISyntaxException: Relative path in absolute URI si on ne force pas un / au début
	// if (!s.startsWith("/")) {
		s = s.replace('\\','/'); 
		if (!s.startsWith("/")) s = "/" + s;
	// }
	return new URI ("file", "", s, null);
}

/**
 * DON'T USE : buggé
 * // ce code pose problème car certains car, comme le VIRGULE, sont codés en %2C par
 * // URLUTF8Encoder (toute version) et URLEncoder.encode
 * // OR new URI(,,,,) (utilisé ds la version à un seul arg de fileToUri) ne code pas la virgule.
 * @param dirUri peut être une uri http locale MAIS SUPPOSEE CORRECTE.
 * Elle peut commencer par file:/, parce qu'on y force à retourner file:///
 * (sans un code dédié, retournerait systématiquement file:/, même si 
 * dirUri était en file:///)
 * ATTENTION : si le fichier est une dir, on aura probablement envie d'avoir
 * une uri "/" terminated, auquel cas il faudra veiller à ce que shortFilename le soit
 * (Il y a une raison forte pour vouloir une uri de dir "/" terminated :
 * on a besoin d'une telle uri pour retrouver une ppté de ce doc ds le modèle
 * enfin, on a besoin que ça soit traité partout de la même façon :	or, on
 * a des "/" ds le modèle ? - probablement d'ailleurs suite à cette méthode, 
 * (cf test du début) et fileToUri(File)
 * 
 * ATTENTION, même genre de code ds WebServer.getURI
 * @deprecated use SLUtils.laxistRelativPath2Uri(String dirUri, String laxistRelativPath)
 */

static public String fileToUriBuggy(String dirUri, String shortFilename) throws URISyntaxException {
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
	URI uri = new URI(scheme, host, dirURI.getRawPath(), dirURI.getFragment());
	uri = uri.resolve(URLUTF8Encoder.encodeFilename(shortFilename)); // faut-il URLUTF8Encoder.encode ?
	String x = uri.toASCIIString();
	return fileSlashSlashSlashProblem(x); // question du file:/// : et bretelle
}

/** Retourne l'uri (sous forme de string) a utiliser pour caracteriser un fichier. 
 *  Deux questions sont traitees ici :
 *  1) D'abord celle de l'encoding des noms de fichiers. Une URI par exemple ne doit
 *  pas contenir d'espace (mais des %20 a la place)
 *  Pour ce qui est des caracteres UTF-8, java 1.4 les laisse dans les uri (uri.toString())
 *  mais on peut vouloir les encoder (utiliser uri.toASCIIString())
 *  2) Cette version patche un probleme avec Jena 1.2 quand on ecrit des fichiers contenant des url
 *  de protocole file : si les urls (utilisee pour l'argument base et les file urls au sein du modele)
 *  commencent par "file:/", le fichier ne contient que des urls absolues, pas relatives.
 *  Comme il se trouve que java sur MacOSX retourne les url sous cette forme, on ne peut
 *  utiliser ce qui serait la methode normale pour proceder - file.toURI().toASCIIString() -
 */
static public String fileToUriV1(File file) {
	return fileSlashSlashSlashProblem(file.toURI().toASCIIString());
}

/** Retourne l'uri (sous forme de string) a utiliser pour caracteriser le fichier filename. 
 *  @see filenameToUri(File)
 */
static public String filenameToUri(String filename) throws URISyntaxException {
	return fileToUri(new File(filename));
}

/** can be used instead of fileToUri to check that the uri returned are "ARP compliant". */
/*static public String fileToARPSafeUri(File file) throws IOException, URISyntaxException {
	String x = fileToUri(file);
	// following line, to check whether this uri is ARP compliant
	// new com.hp.hpl.jena.rdf.arp.URI(x); // a été déprécié, d'où remplacement, non testé, par :
	new XercesURI(x);
	return x;
}*/

/** On Mac OS X, begins with file:/ (only one "/").
 *  UTF-8 chars are encoded and can be decoded with URLUTF8Encoder.decode
 */
static public String encodedFileToURL(String filename) throws MalformedURLException {
	File file = new File(URLUTF8Encoder.encodeFilename(filename));
	return file.toURL().toString();
}

/** On Mac OS X, begins with file:/ (only one "/").
 *  UTF-8 chars are not encoded
 *  Java 1.4 needed
 */
static String fileToURI(String filename) {
	File file = new File(filename);
	return file.toURI().toString();
}

/** On Mac OS X, begins with file:/ (only one "/").
 *  UTF-8 chars are encoded
 *  Java 1.4 needed
 */
static String fileToURIToASCII(File file) {
	return file.toURI().toASCIIString();
}

/** If uri is a file protocol uri, returns it with format file:///, else returns it unchanged. */
public static String fileSlashSlashSlashProblem(String uri) {
	if (uri.startsWith("file:/")) {
		if (uri.charAt(6) != '/') {
			return "file:///" + uri.substring(6);
		} else if (uri.charAt(7) != '/') {
			return "file:///" + uri.substring(7);			
		}
	}
	return uri;
}


}


