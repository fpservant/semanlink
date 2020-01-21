package net.semanlink.servlet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.ModelFileIOManager;

import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.DC;



/** To load and save a RDF file containing the list of properties to be used in forms */
public class SemanlinkConfigProps {
// private static final String SEMANLINK_CONFIG_SCHEMA = SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA;
/** slash terminated */
private String servletUrl;
private File configFile;

private Model model;
private Property usePropertyProp;


private String base;
public SemanlinkConfigProps(File configFile, String servletUrl) throws JenaException, IOException, URISyntaxException {
	this.configFile = configFile;
	this.servletUrl = SemanlinkConfig.slashEndedServletUrl(servletUrl);
	this.base = this.servletUrl;
	//
	this.model = ModelFactory.createDefaultModel();
	this.usePropertyProp = this.model.createProperty(SemanlinkConfig.SLC_USE_PROPERTY_PROP);
	
	if (! (configFile.exists()) ) {
		// 2020-01: use props.rdf in WEB-INF as default
//		Logger.getLogger(getClass()).info("Creation of props config file: " + configFile);
//		init();	
	}

	// corrects bug found by Jeriel Perlman:
	// exception when changing doc's title on a fresh install
	// java.lang.Exception: Invalid property URI:dc:title
	// at net.semanlink.servlet.Action_SetOrAddProperty.setOrAddProp(Action_SetOrAddProperty.java:80)
	// (le setTitle utilise dc:title pour définir la prop qu'il modifie dans le mess envoyé au serveur)
	this.model.setNsPrefix("dc",DC.NS);
	for (SLVocab.EasyProperty easyProp : getEasyProps()) {
		String shortProp = easyProp.getName();
		Resource prop = model.createProperty(easyProp.getUri());
		int k = shortProp.indexOf(":");
		if ( k > 0) {
			this.model.setNsPrefix(shortProp.substring(0,k),prop.getNameSpace());			
		}
	}

	/* on ne peut tolérer aucune erreur sur la lecture
	(for instance, we cannot tolerate a warning such as:
	WARN [Thread-1] (RDFDefaultErrorHandler.java:36) - unknown-source: {W107} Bad U
	RI: <?127.0.0.1:8080/semanlink/?> Error: 10/SCHEME_MUST_START_WITH_LETTER in slo
	t 2
	ERROR [Thread-1] (RDFDefaultErrorHandler.java:40) - 127.0.0.1:8080/semanlink/(li
	ne 14 column 27): {E214} Resolving against bad URI <127.0.0.1:8080/semanlink/>:
	
	Donc, ceci ne suffit pas :
	InputStream in = new BufferedInputStream(new FileInputStream(configFile));
	model.read(in,this.servletUrl); */
	RDFReader rdfReader = this.model.getReader();
	rdfReader.setErrorHandler(new SemanlinkConfig.ConfigErrorHandler());
	InputStream in = new BufferedInputStream(new FileInputStream(configFile));
	rdfReader.read(model, in, base);
	
}

String prefix2NameSpace(String prefix) {
	Map prefixMap = this.model.getNsPrefixMap();
	return (String) prefixMap.get(prefix);
}

public void save() throws JenaException, IOException, URISyntaxException {
	File file = this.configFile;
	if (file.exists()) {
	} else {
		// make sure the directory exists, else writing will fail
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	try {
		this.model.setNsPrefix("sl","http://www.semanlink.net/2001/00/semanlink-schema#");
		this.model.setNsPrefix("slc",SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA);
		this.model.setNsPrefix("dc",DC.NS);
		ModelFileIOManager.getInstance().writeModel(this.model, file.getPath(), base);
	} finally {		}
}

// 2020-01 use props.rdf in WEB-INF as default
//private void init() throws JenaException, IOException, URISyntaxException {
//	SLVocab.EasyProperty[] easyProps = SLVocab.COMMON_PROPERTIES;
//	Resource top = this.model.createResource(this.servletUrl);
//	for (int i = 0; i < easyProps.length; i++) {
//		Resource obj = this.model.createProperty(easyProps[i].getUri());
//		Statement sta = this.model.createStatement(top, this.usePropertyProp, obj);
//		this.model.add(sta);
//	}
//	save();
//}

public SLVocab.EasyProperty[] getEasyProps() {
	SLVocab.EasyProperty[] x = null;
	ArrayList al = new ArrayList();
	Resource top = this.model.createResource(this.servletUrl);
	NodeIterator it = model.listObjectsOfProperty(top, this.usePropertyProp);
	for (;it.hasNext();) {
		Resource res = (Resource) it.next();
		String uri = res.getURI();
		
		al.add(easyProp(uri));
	}
	x = new SLVocab.EasyProperty[al.size()];
	al.toArray(x);
	Arrays.sort(x);
	return x;
}

private SLVocab.EasyProperty easyProp(String propUri) {
	Map prefixMap = this.model.getNsPrefixMap();
	Set prefixes = prefixMap.keySet();
	//System.out.println("SemanlinkConfigProps " + propUri);
	for(Iterator it = prefixes.iterator(); it.hasNext();) {
		String prefix = (String) it.next();
		String uri = (String) prefixMap.get(prefix);
		// System.out.println(prefix + " : " + uri);
		if (propUri.startsWith(uri)) {
			String end = propUri.substring(uri.length());
			// System.out.println("\t" + prefix + ":" + end);
			return new SLVocab.EasyProperty(prefix + ":" + end, propUri);
		}
	}
	return new SLVocab.EasyProperty(propUri, propUri);
}

/**
 * 
 * @param nsPrefixedString for instance ex:foo
 * @return the uri or null
 */
String getUriString(String nsPrefixedString) {
	Map prefixMap = this.model.getNsPrefixMap();
	Set prefixes = prefixMap.keySet();
	for(Iterator it = prefixes.iterator(); it.hasNext();) {
		String prefix = (String) it.next();
		if (nsPrefixedString.startsWith(prefix + ":")) {
			String ns = (String) prefixMap.get(prefix);
			return ns + nsPrefixedString.substring(prefix.length() + 1);
		}
	}
	return null;
}


}