package net.semanlink.servlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLLabeledResource;
import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;
import net.semanlink.util.Util;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.DC;

import javax.servlet.http.*;

/** Affichage d'une SLLabeledResource */
public class Jsp_Resource extends Jsp_Page {
protected SLLabeledResource slRes;
protected String uri;

public Jsp_Resource(SLLabeledResource slRes, HttpServletRequest request) {
	super(request);
	this.slRes = slRes;
	this.uri = slRes.getURI();
	///setSortProperty((String) request.getSession().getAttribute("net.semanlink.servlet.SortProperty"));
	// System.out.println("NEW Jsp_Resource " + uri);
}

public boolean edit() {
    // une nullPointerEx que je ne comprends pas sur si cg sur :
	// return (Boolean.TRUE.equals(this.request.getSession().getAttribute("net.semanlink.servlet.edit")));
    if (this.request == null) {
        System.out.println("Jsp_Resource.edit : request null);" + getClass().getName());
        return false;
    }
    HttpSession session = this.request.getSession();
    if (session == null) {
        System.out.println("Jsp_Resource.edit : session null);" + getClass().getName());
        return false;
    }
    return (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
}

public SLLabeledResource getSLResource() {
	return this.slRes;
}

// <QUESTION> Utiliser java.net.URLEncoder.encode(this.uri, "UTF-8") ? NON!!!
// Et c'est quoi ça, ds la cas d'un tag : lien vers le concept ?
public String getHREF() throws UnsupportedEncodingException, IOException, URISyntaxException {
	//2006/10 file outside dataFolders // should'nt this be in Jsp_Document ? // 2019-03 moved over there
//	if (uri.startsWith("file:")) {
//		URI u;
//		try {
//			u = new URI(this.uri);
//			String path = u.getRawPath();
//			return this.request.getContextPath() + StaticFileServlet.PATH_FOR_FILES_OUTSIDE_DATAFOLDERS + path;
//		} catch (URISyntaxException e) { 
//			throw new RuntimeException(e) ;
//		}
//	}
	return Util.handleAmpersandInHREF(this.uri);
}


/** la valeur pour transmettre cette uri dans un parametre de form */
public String getUriFormValue() {
	return this.uri;
}

public String getUri() { return this.uri; } // 2012-07: Hmm, ATTENTION, retourne uri ds semanlink.net, pas 127.0.0.1:8080/semanlink
public URI getURI() throws URISyntaxException {
	return new URI(this.uri);
}

public String getTitle() {
	return this.slRes.getLabel();
}


//
//UTIL
//

/** key http://... data ns: */
public static HashMap NAMESPACES_HM;
public static String[] NAMESPACES;
public static String[] SHORT_NAMESPACES;

static {
	NAMESPACES_HM = new HashMap();
	NAMESPACES_HM.put(SLSchema.NS,"sl:");
	NAMESPACES_HM.put(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA,"slc:");

	NAMESPACES_HM.put(RDF.getURI(),"rdf:");
	NAMESPACES_HM.put(RDFS.getURI(),"rdfs:");
	NAMESPACES_HM.put(OWL.getURI(),"owl:");

	NAMESPACES_HM.put("http://xmlns.com/foaf/0.1/", "foaf:");
	NAMESPACES_HM.put(DC.NS,"dc:");
	
	NAMESPACES_HM.put(SKOS.NS,"skos:"); //2013-03 @find SKOSIFY
	
	
	NAMESPACES_HM.put("http://purl.org/ontology/mo/", "mo:");
	
	NAMESPACES = new String[NAMESPACES_HM.size()];
	NAMESPACES_HM.keySet().toArray(NAMESPACES);
	SHORT_NAMESPACES = new String[NAMESPACES.length];
	for (int i = 0; i < NAMESPACES.length; i++) {
		SHORT_NAMESPACES[i] = (String) NAMESPACES_HM.get(NAMESPACES[i]);
	}
}

static public String displayUri(String uri) {
	for (int i = 0; i < NAMESPACES.length; i++) {
		String ns = NAMESPACES[i];
		if (uri.startsWith(ns)) {
			// return (String) NAMESPACES_HM.get(ns) + uri.substring(ns.length());
			return SHORT_NAMESPACES[i] + uri.substring(ns.length());
		}
	}
	return uri;
}

/** null si ne commence pas par "shortNS:" */
static public String namespacedUri2Uri(String shortUri) {
	for (int i = 0; i < SHORT_NAMESPACES.length; i++) {
		String ns = SHORT_NAMESPACES[i];
		if (shortUri.startsWith(ns)) {
			return NAMESPACES[i] + shortUri.substring(ns.length());
		}
	}
	return null;	
}

public String getFirstAsString(String pptyUri) {
	PropertyValues pvs = this.slRes.getProperty(pptyUri);
	if (pvs != null) return pvs.getFirstAsString();
	return null;		
}

}