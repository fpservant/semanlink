/* Created on 16 sept. 03 */
package net.semanlink.sljena;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;

import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLThesaurus;
import net.semanlink.servlet.SLServlet;
import net.semanlink.skos.SKOS;

/** Manages the read/write of model files. */
public class ModelFileIOManager {
static private ModelFileIOManager singleton;
/** slash terminated */
private String servletURL;
private ModelFileIOManager() {}
static public void init(String servletURL) {
	if (!servletURL.endsWith("/")) servletURL += "/";
	/*if (singleton != null) { // pose pb quand on a une exception ds init, initSL rappelà aprés et on tombe sur cette exception
		throw new IllegalArgumentException("already inited");
	} else {*/
		singleton = new ModelFileIOManager();
		singleton.servletURL = servletURL;
	//}
}
/** Suppose qu'on a préalablement appelà au moins une fois getInstance(servletUrl) */
static public ModelFileIOManager getInstance() {
	if (singleton != null) return singleton;
	throw new IllegalArgumentException("init(String servletUrl) should have been called before.");
}
//
//synchronization de la lecture/écriture des fichiers rdf
//

// l'idée : synchroniser sur le fichier que l'on écrit -
// ce qui suppose que l'on ait un seul objet correspondant au fichier
// D'où une map assurant qu'il y ait un objet fichier (classe "RDFFile") unique
// pour un fichier donné sur le disque.
// J'ai tenté d'utiliser une WeakHashMap pour stocker ces RDFFile,
// de façon à ce qu'ils puissent être garbage collectés quand ils ne servent plus
// Mais la map ne semble pas se vider comme je m'y attendais. ???

public static WeakHashMap rdfFileMap = new WeakHashMap();
static class RDFFile {
	private String longFilename;
	RDFFile(String longFilename) { this.longFilename = longFilename;}
	public int hashCode() { return this.longFilename.hashCode(); }
	public boolean equals(Object o) {
		return (this.longFilename.equals(((RDFFile) o).longFilename));
	}
}
static synchronized RDFFile getRDFFile(String longFilename) {
	RDFFile x = null;
	WeakReference wr = (WeakReference) rdfFileMap.get(longFilename);
	if (wr != null) {
		x = (RDFFile) wr.get();
		// attention, manifestement, x peut être null
	}
	if (x == null) {
		x = new RDFFile(longFilename);
		rdfFileMap.put(longFilename, new WeakReference(x));
	}
	return x;
}

public static void debugWeakHashMap() {
	System.gc();
	WeakHashMap whm;
	whm = rdfFileMap;
	System.out.println("WEAKHASHMAP size " + whm.size());
	/*	Iterator ite;
	ite = whm.keySet().iterator();
	for (;ite.hasNext();) {
		System.out.println(ite.next());
	}*/	
}

//
//
//

public void writeModel(Model model, String longFilename, String base) throws JenaException, IOException, URISyntaxException {
	/*
	// ceci a, sur une première expérience partielle,
	// un résultat assez joli sous forme d'arbre,
	// avec les documents au 1er niveau, et les kws qui leurs
	// sont affectés :
	PrintWriter pw = new PrintWriter(new FileWriter(longFilename));
	model.write(pw,"RDF/XML-ABBREV",base);
	*/
	/*
	THIS WAS CODE WITH JENA1.6

	BufferedWriter out;
	if (this.encodingToUseToPatchARPBug != null) {
	  out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(longFilename), encodingToUseToPatchARPBug));
	} else {
	  out = new BufferedWriter(new FileWriter(longFilename));
	}

	MyRDFWriter myRDFWriter = new MyRDFWriter();
	// ceci fait que pour les kws, on n'a que des urls relatives, pas l'utilisation de l'entity "&kw;"
	// myRDFWriter.write(model, out, base, xmlEntitiesNames, xmlEntitiesValues);
	if (itsConceptsSpace) {
	  myRDFWriter.write(model, out, "", xmlEntitiesNames, xmlEntitiesValues);
	} else {
	  myRDFWriter.write(model, out, base, xmlEntitiesNames, xmlEntitiesValues);
	}

	*/

	RDFFile rdfFile = getRDFFile(longFilename);
	synchronized(rdfFile) {
		// ceci fait que le fichier utilise l'encoding par defaut, sans le specifier dans le fichier :
		//BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(longFileName)));
		// ceci met du utf-8 :
		OutputStream out = new BufferedOutputStream(new FileOutputStream(longFilename));
		// SLServlet.trace("WRITING " + longFilename + " base : " + base);
		writeModel(model, out, base);
		out.flush();
		out.close();
	}
}

/** not thread safe 
 * @throws URISyntaxException */ 
public void writeModel(Model model, OutputStream out, String base) throws JenaException, IOException, URISyntaxException {
	// RDFWriter rdfWriter = model.getWriter("RDF/XML"); // "RDF/XML" is default
	RDFWriter rdfWriter = model.getWriter("RDF/XML-ABBREV"); // "RDF/XML" is default
	// rdfWriter.setProperty("showXmlDeclaration","true");
	// ceci est important pour la gestion des uris relatives dans les fichiers écrits.
	// Par défaut, on n'a pas grandparent, mais absolute (qui est un absolut sans host).
	// Du coup, pour le cas où on veut mettre
	// les sl.rdf marquants les fichiers d'un dossier "fol" par des sl.rdf dans
	// "fol/yyyy/mm, on a des urls (pseudos) absolutes au niveau des mm.
	// rdfWriter.setProperty("relativeURIs","same-document, absolute, relative, parent, grandparent");
	// Mais attention ! Si l'url du vocabulaire est servletUrl/tag, et si le fichier
	// est servie par la servlet avec une url (cad base) pas trés profonde (telle que serletURL/userdata/documents/file.htm,
	// les tags affectés à ce document vont se trouver "relativisés" en ../../tag/[tagvalue]
	// (au lieu d'être en /semanlink/tag)
	// Je teste donc sur la taille du chemin relatif de base par rapport à this.servletURL pour savoir
	// comment dire au writer Jena de déterminer les "relativ uris")
	rdfWriter.setProperty("relativeURIs",relativeURIsProperty(base));
	rdfWriter.setProperty("showDoctypeDeclaration", "true");
	// rdfWriter.setProperty("xmlbase", base);
	// System.out.println("rdfWriter.getClass().getName(): "+rdfWriter.getClass().getName());
	// BOF BOF
	SLModel slMod = SLServlet.getSLModel();
	if (slMod != null) {
		SLThesaurus th = SLServlet.getSLModel().getDefaultThesaurus();
		if (th != null) { // th can be null during init of servlet
			String thURI = th.getBase(); // slash terminated
			model.setNsPrefix("tag",thURI);
		}
	}
	model.setNsPrefix("skos", SKOS.getURI());
	rdfWriter.write (model, out, base);
}



// MAIS Aïe Aïe Aïe : vouloir mettre des choses telles que
// /semanlink/tag/[tagvalue] dans les sl.rdf pose un problàme :
// en effet, si on décide, par ex, de servir le dossier contenant ce fichier sl.rdf
// non plus avec tomcat sur le port 8080, mais par apache sur le port 80,
// (changement de l'url du dataFolder)
// lors de la lecture du fichier, on va utiliser comme url du vocab,
// pour ce fichier, http://127.0.0.1/semanlink/tag
// alors que la vraie valeur reste celle sur le port 8080
// IL NE FAUDRAIT DONC PAS RELATIVISER, EN AUCUNE FACON QUE CE SOIT,
// LES URLS DES TAGS, cad, pas même utiliser la ppte "absolute" du rdfWriter jena

// HA! mais du coup on ne peut plus changer le host de la servlet sans difficulté !!!
// Hélas non. La procédure pour le permettre serait-elle aisée (cf les model traductions) ?
// Comment ? changeemnt d'url de servlet => modif de web.xml.
// Comment se rendre compte ds la servlet que ça s'est produit ?
// BOF c'est pénible.
// VA-T-ON garder /semanlink/tag/[tagvalue] dans les sl.rdf ?
// OUI.
// C'EST UN MOINDRE MAL
// Quel est le problème que cela induit ?
// Il survient, uniquement, au moins en 1ere analyse, quand on change
// la façon de servir un dataFolder (changement de son host:port), 
// jusqu'alors servi par la servlet, sans changer de la même façon
// le host:port de la servlet.
// (rq : quand on change le host:port de la servlet, on n'a pas le problàme : en effet,
// le dataFolder est soit défini par rapport à la servlet (par ex, au sein du mainDataDir),
// soit déjà de façon absolue)
// Quand on change le host:port de l'uri d'un dataFolder,
// au moment de le charger, on utilise c'est host:port dans la base pour charger les
// sl.rdf. Comme les tags y sont représentés en /semanlink/tag/[tagvalue] (cad,
// implicitement, relatifs à la servlet), il vont se trouver chargés par le nouvel host:port,
// qui ne correspond pas à l'url du thesaurus.
// Ce n'est pas très grave (?) si on change tous les dataFolder :
// le thesaurus se retrouve en pratique avec une nlle uri (sur le nouveau host:port
// Par contre, c'est ennuyeux si tous les dataFolder ne sont pas changés,
// parce que on aura un mélange d'url de l'ancienne forme et de la nlle.
// MORALITE : il faut bien documenter le fait que si on change la façon de charger un dataFolder
// qui était jusque là servi par la servlet, il y a quelque chose à faire
// pour rétablir la situation des tags au sein des fichiers.
// Quoi en pratique ? Convertir tous les /semanlink/tag/[tagvalue] en url de la servlet/tag/[tagvalue]
// CECI POURRAIT-IL ETRE FAIT AUTOMATIQUEMENT ?
private static String NO_PARENT_PROP = "same-document, absolute, relative";
private static String PARENT_ONLY_PROP = NO_PARENT_PROP + ", parent";
private static String PARENT_AND_GRAND_PARENT_PROP = PARENT_ONLY_PROP +", grandparent";
private String relativeURIsProperty(String base) throws URISyntaxException {
	if (!(base.startsWith(this.servletURL))) {
		// ceci n'est pas bien bon si on a base et servletURL qui ont juste le même host:port
		// (urls eb ../ dans le cas du taggage de pages servies par une servlet sur le même host:port) :
		// return PARENT_AND_GRAND_PARENT_PROP;
		URI baseUri = new URI(base);
		URI servletUri = new URI(this.servletURL);
		if ( (servletUri.getHost().equals(baseUri.getHost())) && (servletUri.getPort() == (baseUri.getPort())) ) {
			return NO_PARENT_PROP;
		}
		return PARENT_AND_GRAND_PARENT_PROP;
	}
	String s = base.substring(this.servletURL.length());
	int n = s.length();
	int nbOfSlash = 0;
	for (int i = 0; i < n-1; i++) { // n-1 car si le dernier est un slash, il ne compte pas
		if (s.charAt(i) == '/') nbOfSlash++;
	}
	if (nbOfSlash == 0) {
		return NO_PARENT_PROP;
	} else if (nbOfSlash == 1) {
		return PARENT_ONLY_PROP;
	} else {
		return PARENT_AND_GRAND_PARENT_PROP;
	}
}

void readModel(Model model, String longFilename, String base) throws JenaException, IOException {
	// SLServlet.trace("READING " + longFilename + " base : " + base);
	if (! ((new File(longFilename)).exists()) ) return;
	// Ceci permet de lire un fichier utilisant le default encoding (non specifie dans le fichier) :
	// BufferedReader in = new BufferedReader(new FileReader(longFilename));
	// Ceci permet de lire de l'utf-8 :
	RDFFile rdfFile = getRDFFile(longFilename);
	synchronized(rdfFile) {
		InputStream in = new BufferedInputStream(new FileInputStream(longFilename));
		model.read(in,base);
		// A VIRER 
		model.removeNsPrefix("j.0");
		model.removeNsPrefix("j.1");
	}
} // readModel
}
