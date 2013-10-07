package net.semanlink.util.jena;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Various static methods.
 */
public class JenaUtils {
private JenaUtils() {}

public static void printModel(Model mod) {
	StmtIterator stmtIt = mod.listStatements();
	for (;stmtIt.hasNext();) {
		com.hp.hpl.jena.rdf.model.Statement sta = stmtIt.nextStatement();
		System.out.println(sta);
	}
}

public static void printSubjects(Model mod) {
	ResIterator it = mod.listSubjects();
	ArrayList<String> al = new ArrayList<String>();
	for (;it.hasNext();) {
		al.add(it.nextResource().getURI());
	}
	Collections.sort(al);
	for (String s : al) System.out.println(s);
}

public static void printSta(Resource res) {
	StmtIterator it = res.listProperties();
	for (;it.hasNext();) {
		Statement sta = it.nextStatement();
		System.out.println(sta.getPredicate().getLocalName() + ": " + sta.getObject());
	}
}

public static void printResource(Resource res) {
	System.out.println(res2string(res));
}

public static String res2string(Resource res) {
	StringBuilder sb = new StringBuilder(res.toString());
	sb.append("\n");
	StmtIterator it = res.listProperties();
	for (;it.hasNext();) {
		Statement sta = it.nextStatement();
		// sb.append("\t" + sta.getPredicate().getLocalName() + ": " + sta.getObject() + "\n");
		sb.append("\t" + sta.getPredicate() + ": " + sta.getObject() + "\n");
	}
	return sb.toString();
}


//
// Loading RDF data from files in a folder.
//

/**
 * Loads the content of rdf files into a given model. 
 * 
 * if fileOrFolder is a folder, only the ".rdf", ".ttl" or ".n3" files (whatever the case of the suffix) are loaded.
 * if it is a file, attempts to load it as RDF-XML whatever its suffix, except if the suffix is n3.
 * 
 * @param fileOrFolder file to load RDF from
 * @param model the Jena model to load the statements into
 * @param base optional param allowing to set the base if none is used in the files.
 * @param includeSubfolders true to recursively load files from subfolders of fileOrFolder (used only when fileOrFolder is a folder)
 * @throws IOException */
public static void loadModel(File fileOrFolder, Model model, String base, boolean includeSubfolders) throws IOException {
	loadModel(fileOrFolder, model, base, includeSubfolders, null);
}

public static void loadModel(File fileOrFolder, Model model, String base, boolean includeSubfolders, Logger logger) throws IOException {
	if (fileOrFolder.isDirectory()) {
		loadRDFFolder(fileOrFolder, model, base, includeSubfolders, logger);
	} else {
		if (logger != null) logger.info("Loading " + fileOrFolder.getAbsolutePath());
		loadRDFFile(fileOrFolder, model, base);
	}
}


/**
 * Loads the rdf files in a folder into a given model. 
 * 
 * Only the ".rdf", ".ttl" or ".n3" files (whatever the case of the suffix) are loaded.
 * @param base optional param allowing to set the base if none is used in the files.
 * @param includeSubfolders true to recursively load files from subfolders
 * @throws IOException */
static void loadRDFFolder(File folder, Model model, String base, boolean includeSubfolders, Logger logger) throws IOException {
	File[] files = folder.listFiles();
	for (int i = 0 ; i < files.length ; i++) {
		File f = files[i];
		if (includeSubfolders) {
			if (f.isDirectory()) {
				loadRDFFolder(f, model, base, true, logger);
			} else {
				loadRDFFileUtil(f, model, base, logger);
			}
		} else {
			loadRDFFileUtil(f, model, base, logger);
		}
	}	
}

/**
 * Load the rdf data supposed to be included in the file (not a directory). 
 * 
 * Attempts to read it as "N3" if suffix is "n3", "Turtle" if suffix is "ttl" else as RDF-XML.
 * @param base optional param allowing to set the base if none is used in the files.
 */
public static void loadRDFFile(File f, Model model, String base) throws IOException {
	boolean done = loadRDFFileUtil(f, model, base, null);
	if (!done) { // filename not ended with ".rdf" nor ".n3"
		InputStream in = new BufferedInputStream(new FileInputStream(f));
		model.read(in, base);
		in.close();
	}
}

/** return true if loaded, that is if the filename ends with ".rdf", ".ttl" or ".n3" (insensitive to case) */
private static boolean loadRDFFileUtil(File f, Model model, String base, Logger logger) throws IOException {
	String s = f.getName().toLowerCase();
	String lang = null;
	if (s.endsWith(".rdf")) lang = "RDF/XML";
	else if (s.endsWith(".n3")) lang = "N3";
	else if (s.endsWith(".ttl")) lang = "TURTLE";
	else { } // throw new RuntimeException("Unexpected file " + f);
	boolean x = false;
	if (lang != null) {
		if (logger != null) logger.info("Loading " + f);
		InputStream in = new BufferedInputStream(new FileInputStream(f));
		model.read(in, base, lang);
		in.close();
		x = true;
	}
	/*if (x) System.out.println("\tmodel.size: " + model.size());
	else System.out.println();*/
	return x;
}

//
// PROPERTIES OF A RESOURCE
//

/**
 * Returns the first RDFNode, if any, that is object of a given property of a resource. 
 * Useful for instance when you know that a given prop has one and only one value
 */
static public RDFNode firstObjectOfProperty(Resource res, Property prop) {
	RDFNode x = null;
	NodeIterator it = res.getModel().listObjectsOfProperty(res, prop);
	if (it.hasNext()) x = it.nextNode();
	it.close();
	return x;	
}

/**
 * Returns the first Literal, if any, that is object of a given property of a resource. 
 * Useful for instance when you know that a given prop has one and only one literal value
 */
static public Literal firstLiteralOfProperty(Resource res, Property prop) {
	Literal x = null;
	NodeIterator it = res.getModel().listObjectsOfProperty(res, prop);
	for (;it.hasNext();) {
		Object o = it.next();
		if (o instanceof Literal) {
			x = (Literal) o;
			break;
		}
	}
	it.close();
	return x;	
}

//
// LABEL OF A PROPERTY
//

/**
 * Returns the first RDFS.label with language lang, or null.
 * @param res the resource
 * @param lang the searched language. Null if search for a label without defined language. */
public static String getLabel(Resource res, String lang) {
	return getLabel(res, RDFS.label, lang);
}

/**
 * Returns the first RDFS.label with language lang, or null.
 * @param res the resource
 * @param lang the searched language. Null if search for a label without defined language. */
public static Literal getLabelAsLiteral(Resource res, String lang) {
	return getLabelAsLiteral(res, RDFS.label, lang);
}


/**
 * Returns the first value of labelProp with language lang, or null.
 * @param res the resource
 * @param a property whose values are String Literals
 * @param lang the searched language. Null if search for a label without defined language. */
public static String getLabel(Resource res, Property labelProp, String lang) {
	/*
	NodeIterator it = res.getModel().listObjectsOfProperty(res, labelProp);
	String x = null;
	if (lang == null) {
		for(;it.hasNext();) {
			Literal lit = (Literal) it.next();
			String litLang = lit.getLanguage();
			if ((litLang == null) || ("".equals(litLang))) {
				x = lit.getString();
				break;
			}
		}
	} else {
		for(;it.hasNext();) {
			Literal lit = (Literal) it.next();
			if (lang.equals(lit.getLanguage())) {
				x = lit.getString();
				break;
			}
		}
	}
	it.close();
	return x;
	*/
	Literal x = getLabelAsLiteral(res, labelProp, lang);
	if (x == null) return null;
	return x.getString();	
}

/**
 * Returns the first value of labelProp with language lang, or null.
 * @param res the resource
 * @param a property whose values are String Literals
 * @param lang the searched language. Null if search for a label without defined language. */
public static Literal getLabelAsLiteral(Resource res, Property labelProp, String lang) {
	NodeIterator it = res.getModel().listObjectsOfProperty(res, labelProp);
	Literal x = null;
	if (lang == null) {
		for(;it.hasNext();) {
			Literal lit = (Literal) it.next();
			String litLang = lit.getLanguage();
			if ((litLang == null) || ("".equals(litLang))) {
				x = lit;
				break;
			}
		}
	} else {
		for(;it.hasNext();) {
			Literal lit = (Literal) it.next();
			if (lang.equals(lit.getLanguage())) {
				x = lit;
				break;
			}
		}
	}
	it.close();
	return x;
}


/**
 * Returns a label of a given Resource
 * @param res
 * @param labelProp the property (whose values must all be String Literals) used as label. For instance RDFS.label or DC.title
 * @param langs gives the order of preference for the language
 * @return null if no label in any of the languages listed in langs
 */
public static String getLabel(Resource res, Property labelProp, String... langs) {
	/*if (langs == null) {
		String x = getLabel(res, labelProp, (String) null);
		if (x != null) return x;
	} else {
		for(String lang: langs)	{
			String x = getLabel(res, labelProp, lang);
			if (x != null) return x;
		}
	}
	return null;*/
	Literal x = getLabelAsLiteral(res, labelProp, langs);
	if (x == null) return null;
	return x.getString();	
}

/**
 * Returns a label of a given Resource
 * @param res
 * @param labelProp the property (whose values must all be String Literals) used as label. For instance RDFS.label or DC.title
 * @param langs gives the order of preference for the language
 * @return null if no label in any of the languages listed in langs
 */
public static Literal getLabelAsLiteral(Resource res, Property labelProp, String... langs) {
	if (langs == null) {
		Literal x = getLabelAsLiteral(res, labelProp, (String) null);
		if (x != null) return x;
	} else {
		for(String lang: langs)	{
			Literal x = getLabelAsLiteral(res, labelProp, lang);
			if (x != null) return x;
		}
	}
	return null;
}


/**
 * Returns the label attached to a resource, if any.
 * BEWARE this will throw an exception if labelProp uses non string literal values. // TODO ?
 * @return first label matching the requirements defined by the params, else null.
 * @param res the resource
 * @param labelProp: the property used as label. For instance RDFS.Label, or DC.title
 * @param lang: language. If null, search for a literal without any defined language
 * @param returnAnyLabelIfNoneInLang: in case the label in lang is not found, should we return any language instead.
 * (In that case, if there is a lit without any defined language, this one is returned.)
 */
public static String getLabel(Resource res, Property labelProp, String lang, boolean returnAnyLabelIfNoneInLang) {
	/*
	NodeIterator it = res.getModel().listObjectsOfProperty(res, labelProp);
	try {
		Literal lit = null;
		Literal litWithoutDefinedLang = null;
		for(;it.hasNext();) {
			lit = (Literal) it.next();
			String litLang = lit.getLanguage();
			if (lang == null) {
				if (litLang == null) return lit.getString();
				if ("".equals(litLang)) return lit.getString();
			} else {
				if (lang.equals(litLang)) {
					return lit.getString();
				}
				if (litLang == null) litWithoutDefinedLang = lit;
				if ("".equals(litLang)) litWithoutDefinedLang = lit;
			}
		}
		if (lit == null) return null;
		if (returnAnyLabelIfNoneInLang) {
			if (litWithoutDefinedLang != null) return litWithoutDefinedLang.getString();
			return lit.getString();
		}
	} finally {it.close();}
	return null;
	*/
	Literal x = getLabelAsLiteral(res, labelProp, lang, returnAnyLabelIfNoneInLang);
	if (x == null) return null;
	return x.getString();
}

public static Literal getLabelAsLiteral(Resource res, Property labelProp, String lang, boolean returnAnyLabelIfNoneInLang) {
	NodeIterator it = res.getModel().listObjectsOfProperty(res, labelProp);
	try {
		Literal lit = null;
		Literal litWithoutDefinedLang = null;
		for(;it.hasNext();) {
			lit = (Literal) it.next();
			String litLang = lit.getLanguage();
			if (lang == null) {
				if (litLang == null) return lit;
				if ("".equals(litLang)) return lit;
			} else {
				if (lang.equals(litLang)) {
					return lit;
				}
				if (litLang == null) litWithoutDefinedLang = lit;
				if ("".equals(litLang)) litWithoutDefinedLang = lit;
			}
		}
		if (lit == null) return null;
		if (returnAnyLabelIfNoneInLang) {
			if (litWithoutDefinedLang != null) return litWithoutDefinedLang;
			return lit;
		}
	} finally {it.close();}
	return null;
}


/*
public static String getLabel(Resource res, Property labelProp, boolean returnAnyLabelIfNoneInLangs, String... langs) {
	for(String lang: langs)	{
		String x = getLabel(res, labelProp, lang);
		if (x != null) return x;
	}
	if (returnAnyLabelIfNoneInLangs) return getLabel(res, labelProp, (String) null);
	return null;
}
*/

//
//
//


public static HashSet<Property> predicatesInStatements(StmtIterator it) {
	HashSet<Property> x = new HashSet<Property>();
	for(; it.hasNext(); ) {
		Statement sta = it.nextStatement();
		x.add(sta.getPredicate());
	}
	it.close();
	return x;
}

/**
 * All properties of a resource, with their values.
 */
public static HashMap<Property, ArrayList<RDFNode>> propertyValuesHashMap(Resource res) {
	return propertyValuesHashMap(res.getModel(), res);
}

public static HashMap<Property, ArrayList<RDFNode>> propertyValuesHashMap(Model mod, Resource res) {
	StmtIterator it = mod.listStatements(res, (Property) null, (RDFNode) null);
	HashMap<Property, ArrayList<RDFNode>> x = new HashMap<Property, ArrayList<RDFNode>>();
	for(; it.hasNext(); ) {
		Statement sta = it.nextStatement();
		Property p = sta.getPredicate();
		ArrayList<RDFNode> data = x.get(p);
		if (data == null) {
			data = new ArrayList<RDFNode>(16);
			x.put(p, data);
		}
		data.add(sta.getObject());
	}
	return x;
}

public static HashMap<Property, ArrayList<Resource>> inverseProps(Resource res) {
	return inverseProps(res.getModel(), res);
}

public static HashMap<Property, ArrayList<Resource>> inverseProps(Model mod, RDFNode node) {
	StmtIterator it = mod.listStatements((Resource) null, (Property) null, node);
	HashMap<Property, ArrayList<Resource>> x = new HashMap<Property, ArrayList<Resource>>();
	for(; it.hasNext(); ) {
		Statement sta = it.nextStatement();
		Property p = sta.getPredicate();
		ArrayList<Resource> data = x.get(p);
		if (data == null) {
			data = new ArrayList<Resource>(16);
			x.put(p, data);
		}
		data.add(sta.getSubject());
	}
	return x;
}

//
//
//

/** Add the "top subjects" of a model (resources that are subjects of some statement, but not object of any) to a given collection. */
static public void topSubjects2Collection(Model mod, Collection<Resource> collection) {
	ResIterator it =  mod.listSubjects();
	for (;it.hasNext();) {
		Resource res = it.nextResource();
		StmtIterator it2 = mod.listStatements(null, null, res);
		if (!it2.hasNext()) collection.add(res);
		it2.close();
	}
	it.close();
}

/**
 * Replace oldRes by newRes in all statements involving oldRes in oldRes.getModel() BEWARE, not for properties
 */
static public void replaceResource(Resource oldRes, Resource newRes) {
	Model mod = oldRes.getModel();
	StmtIterator sit;
	List<Statement> al;
	sit = mod.listStatements(oldRes, (Property) null, (RDFNode) null);
	al = sit.toList();
	for (Statement oldSta: al) {
		Statement newSta = mod.createStatement(newRes, oldSta.getPredicate(), oldSta.getObject());
		mod.add(newSta);
	}
	mod.remove(al);
	sit = mod.listStatements((Resource) null, (Property) null, oldRes);
	al = sit.toList();
	for (Statement oldSta: al) {
		Statement newSta = mod.createStatement(oldSta.getSubject(), oldSta.getPredicate(), newRes);
		mod.add(newSta);
	}
	mod.remove(al);
}

//
//
//

/*
 * Was included in jena com.hp.hpl.jena.sparql.util.StringUtils, but not with ARQ 2.8.5
 * I therefore copy/pasted it here
 */
/** Join an array of strings */
public static String join(String sep, String...a)
{
    if ( a.length == 0 )
        return "" ;
    
    if ( a.length == 1)
        return a[0] ;

    StringBuffer sbuff = new StringBuffer() ;
    sbuff.append(a[0]) ;
    
    for ( int i = 1 ; i < a.length ; i++ )
    {
        if ( sep != null )
            sbuff.append(sep) ;
        sbuff.append(a[i]) ;
    }
    return sbuff.toString() ;
}

//
//
//

/** Returns true iff res has rdf:type type */
public static boolean hasRDFType(Resource res, Resource type) {
	NodeIterator it = null;
	try {
		it = res.getModel().listObjectsOfProperty(res, RDF.type);
		for (;it.hasNext();) {
			if (type.equals(it.next())) return true;
		}
		return false;
	} finally { if (it != null) it.close(); }
}

}
