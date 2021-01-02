/* Created on 16 sept. 03 */
package net.semanlink.sljena;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// 2012-08 jena at apache
// import org.apache.jena.iri.*;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.Violation;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import net.semanlink.semanlink.LabelLN;
import net.semanlink.semanlink.LabelLNImpl;
import net.semanlink.semanlink.SLVocab;

/**
 * @author fps
 */
public class JenaUtils {
private JenaUtils() {}

/** Retourne une HashMap de cle les String uri de property, et de data une JPropertyValues */
public static HashMap getProperties(Resource res) {
	 HashMap propH = new HashMap();
	 StmtIterator ite = res.listProperties ();
	 for (;ite.hasNext();) {
		 Statement sta = ite.nextStatement();
		 Property prop = sta.getPredicate();
		 String propUri = prop.getURI();
		 JPropertyValues objs = (JPropertyValues) propH.get(propUri);
		 if (objs == null) {
			 objs = new JPropertyValues();
			 propH.put(propUri, objs);
		 }
		 objs.add(sta.getObject());
	 }
	 ite.close();
	 return propH;
 }
/** Retourne une HashMap de cle les property (uri), et de data 
 * une ArrayList des valeurs de property (uri ou String - ce qui laisse posee la question du lang.)
 */
public static HashMap getPropertiesAsStrings(Resource res) {
	HashMap propH = new HashMap();
	StmtIterator ite = res.listProperties ();
	for (;ite.hasNext();) {
		Statement sta = ite.nextStatement();
		Property prop = sta.getPredicate();
		String propUri = prop.getURI();
		ArrayList objs = (ArrayList) propH.get(propUri);
		if (objs == null) {
			objs = new ArrayList();
			propH.put(propUri, objs);
		}
		RDFNode object = sta.getObject();
		if (object instanceof Literal) {
			objs.add(((Literal)object).getString());
		} else {
			objs.add(((Resource)object).getURI());
		}
	}
	ite.close();
	return propH;
}

//
//
//

public static void print(Model mod) {
	Iterator ite = mod.listStatements();
	for (;ite.hasNext();) {
		System.out.println(ite.next());
	}
}

//
// MODIFICATIONS D'UN MODEL
//

/** Add a statement to model. */
static void add(Model model, String docUri, String propertyUri, String propertyValueUri) {
	JThing trip = new JThing(model, docUri, propertyUri, propertyValueUri); // trip comme triple
	model.add(trip.getStatement());
}

/** Add a statement to model. */
static void add(Model model, String docUri, String propertyUri, String propertyValue, String lang) {	//model.add(SLJenaUtils.newStatement(model, docUri, propertyUri, propertyValue, lang));
	JThing trip = new JThing(model, docUri, propertyUri, propertyValue, lang); // trip comme triple
	model.add(trip.getStatement());
}

/** Add a statement to model. */
static void add(Model model, String docUri, String propertyUri, RDFNode object) {
	JThing trip = new JThing(model, docUri, propertyUri, object); // trip comme triple
	model.add(trip.getStatement());
}


static void setPropertyValue(Model model, String docUri, String propertyUri, String propertyValueUri) { 
	JThing trip = new JThing(model, docUri, propertyUri, propertyValueUri); // trip comme triple
	trip.getSubject().removeAll(trip.getProperty());
	model.add(trip.getStatement());
}

static void setPropertyValue(Model model, String docUri, String propertyUri, String propertyValue, String lang) { // private si on fait celles pour value/uri
	JThing trip = new JThing(model, docUri, propertyUri, propertyValue, lang); // trip comme triple
	trip.getSubject().removeAll(trip.getProperty());
	model.add(trip.getStatement());
}

static void setPropertyValue(Model model, String docUri, String propertyUri, RDFNode object) { // private si on fait celles pour value/uri
	JThing trip = new JThing(model, docUri, propertyUri, object); // trip comme triple
	trip.getSubject().removeAll(trip.getProperty());
	model.add(trip.getStatement());
}

public static void removeProperty(Model model, String docUri, String propertyUri) {
	JThing trip = new JThing(model, docUri, propertyUri, null, null); // trip comme triple
	trip.getSubject().removeAll(trip.getProperty());
}

static void remove(Model model, String subjectUri, String propertyUri, String objectUri) {
	JThing trip = new JThing(model, subjectUri, propertyUri, objectUri); // trip comme triple
	model.remove(trip.getStatement());
}

static void remove(Model model, String subjectUri, String propertyUri, String propertyValue, String lang) { // 2020-04
	JThing trip = new JThing(model, subjectUri, propertyUri, propertyValue, lang); // trip comme triple
	model.remove(trip.getStatement());
}


static void remove(Model model, String subjectUri, String propertyUri, RDFNode node) {
	JThing trip = new JThing(model, subjectUri, propertyUri, node); // trip comme triple
	model.remove(trip.getStatement());
}


/** Creation d'un keyword -- censé ne pas exister prealablement. 
 *  Attention, ne traite pas la date de création, juste le RDF.type et le SLVocab.PREF_LABEL_PROPERTY. */
static Resource newKeyword(Model model, String kwUri, String label, String lang) {
	JThing trip = new JThing(model, kwUri, RDF.type.getURI(), JModel.KEYWORD_CLASS); // trip comme triple
	model.add(trip.getStatement());
	// trip = new JThing(model, kwUri, SLVocab.PREF_LABEL_PROPERTY, label, lang); // trip comme triple
	trip.sets(null, SLVocab.PREF_LABEL_PROPERTY, label, lang);
	model.add(trip.getStatement());
	return trip.getSubject();
}
//
// UTILITAIRES POUR FAIRE UN ENSEMBLE DE MODIFS SUR UN MODEL
//

/**
 * change toutes les occurences de oldUri en tant que subject ou object en newUri
 *  Retourne s'il y a eu des changements.
 */
public static boolean changeResURI(Model mod, String oldUri, String newUri) {
	Resource oldRes = mod.createResource(oldUri);
	Resource newRes = mod.createResource(newUri);
	boolean x = changeSubjects(mod, oldRes, newRes);
	x = changeObjects(mod, oldRes, newRes) || x;
	return x;
}

public static boolean changeSubjects(Model mod, String oldUri, String newUri) {
	Resource oldRes = mod.createResource(oldUri);
	Resource newRes = mod.createResource(newUri);
	return changeSubjects(mod, oldRes, newRes);
}

/** Change l'objet de tous les statements dont l'objet est oldUri par newUri.
 *  Retourne s'il y a eu des changements.
 */
public static boolean changeObjects(Model mod, String oldUri, String newUri) {
	Resource oldRes = mod.createResource(oldUri);
	Resource newRes = mod.createResource(newUri);
	return changeObjects(mod, oldRes, newRes);
}

/** Change le subject de tous les statements dont le subject est oldSubject par newSubject.
 *  Retourne s'il y a eu des changements.
 */
public static boolean changeSubjects(Model mod, Resource oldSubject, Resource newSubject) {
	StmtIterator staIte = mod.listStatements (oldSubject, (Property) null, (Resource) null);
	// Peut-on, au fur et a mesure de l'enumeration,
	// creer les nouveaux statements, et supprimer les anciens ?
	// I don't know, et je vais la jouer prudente :
	ArrayList oldStas = new ArrayList();
	for (;staIte.hasNext();) {
		oldStas.add(staIte.next());
	}
	Iterator ite = oldStas.iterator();
	boolean x = ite.hasNext();
	for (;ite.hasNext();) {
		Statement oldSta = (Statement) ite.next();
		mod.add(newSubject, oldSta.getPredicate(), oldSta.getObject());
		mod.remove(oldSta);
	}
	return x;
}

/** Change l'objet de tous les statements dont l'objet est oldObject par newObject.
 *  Retourne s'il y a eu des changements.
 */
public static boolean changeObjects(Model mod, Resource oldObject, Resource newObject) {
	StmtIterator staIte = mod.listStatements ((Resource) null, (Property) null, oldObject);
	// Peut-on, au fur et a mesure de l'enumeration,
	// creer les nouveaux statements, et supprimer les anciens ?
	// I don't know, et je vais la jouer prudente :
	ArrayList<Statement> oldStas = new ArrayList<>();
	for (;staIte.hasNext();) {
		oldStas.add(staIte.next());
	}
	Iterator<Statement> ite = oldStas.iterator();
	boolean x = ite.hasNext();
	for (;ite.hasNext();) {
		Statement oldSta = ite.next();
		mod.add(oldSta.getSubject(), oldSta.getPredicate(), newObject);
		mod.remove(oldSta);
	}
	return x;
}

// 2021-01: hum, this handles the possibility to have several comments for one res
static public String getComment(Resource res) {
	LabelLN ln = getCommentLN(res);
	if (ln == null) return null;
	return ln.getLabel();
}

static public LabelLN getCommentLN(Resource res) {
	Model model = res.getModel();
	NodeIterator ite = model.listObjectsOfProperty(res, model.getProperty(SLVocab.COMMENT_PROPERTY));
	int k = 0;
	LabelLN x = null;
	if (ite.hasNext()) {
		String lang = null;
		StringBuffer sb = new StringBuffer();
		String curLang = null;
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (k > 0) {
				sb.append("\r\n");
			}
			if (node instanceof Literal) {
				Literal lit = (Literal) node;
				sb.append(lit.getString()); // toString de RDFNode met @lang
				curLang = lit.getLanguage();
				if (k == 0) {
					lang = curLang;
				} else {
					if (lang != null) {
						if (!lang.equals(curLang)) {
							lang = null;
						}
					}
				}
			} else {
				sb.append(node.toString());
			}
			k++;
		}
		
		x = new LabelLNImpl(sb.toString(), lang);
	}
	ite.close();
	return x;
}


/** a ameliorer
 * supprime toutes les occurences d'un kw en tant que subject et object
 * @param mod
 * @param uri
 */
static public void delete(Model mod, String uri) {
	Resource res = mod.createResource(uri);
	removeStats(mod, mod.listStatements ((Resource) null, (Property) null, res));
	removeStats(mod, mod.listStatements (res, (Property) null, (RDFNode) null));
}
private static void removeStats(Model mod, StmtIterator staIte) {
	// Peut-on, au fur et a mesure de l'enumeration,
	// creer les nouveaux statements, et supprimer les anciens ?
	// I don't know, et je vais la jouer prudente :
	/*ArrayList oldStas = new ArrayList();
	for (;staIte.hasNext();) {
		oldStas.add(staIte.next());
	}
	Iterator ite = oldStas.iterator();
	for (;ite.hasNext();) {
		mod.remove((Statement) ite.next());
	}*/
	
	// Mais par ailleurs, pb lors de la suppression d'un doc :
	// si on supprime avant la fin la date de création et que le doc a ses infos stockées ds 
	// un fichier dépendant de la date de création, et bien on se sait plus après
	// ds quel fichier sauver les autres modifs. D'où un hack pour supprimer en dernier
	// la date de création // TOUT CECI ME PARAIT DELIRANT
	Statement creationDateSta = null;
	ArrayList oldStas = new ArrayList();
	for (;staIte.hasNext();) {
		Statement sta = staIte.nextStatement();
		if (sta.getPredicate().getURI().equals(SLVocab.SL_CREATION_DATE_PROPERTY)) {
			creationDateSta = sta;
		} else {
			oldStas.add(sta);
		}
	}
	Iterator ite = oldStas.iterator();
	for (;ite.hasNext();) {
		mod.remove((Statement) ite.next());
	}
	if (creationDateSta != null) {
		mod.remove(creationDateSta);
	}
}

/**
 * Allows to check URI validity before adding it to a model.
 * (Without this check, we can add a statement involving an URI to a model,
 * and later get an exception when trying to write it to a file
 * (or, worse, get an exception when reading it later)
 * see http://www.semanlink.net/doc/?uri=http%3A%2F%2Ftech.groups.yahoo.com%2Fgroup%2Fjena-dev%2Fmessage%2F28785
 */
// private static IRIFactory iriFactory = IRIFactory.semanticWebImplementation(); // 2020-02 changed to:
private static IRIFactory iriFactory = IRIFactory.iriImplementation();
//public static boolean uriHasViolation(String iriString) {
//	boolean includeWarnings = false;
//	IRI iri = iriFactory.create(iriString); // always works
//	return iri.hasViolation(includeWarnings);
//}
/**
 * @return null if no violations
 * @since 2010-02 */
public static String getUriViolations(String iriString, boolean includeWarnings) {
	IRI iri = iriFactory.create(iriString); // always works
	boolean x = iri.hasViolation(includeWarnings);
	if (x) {
		StringBuilder sb = new StringBuilder();
		sb.append("Invalid URI: " + iriString);
		Iterator<Violation> it = iri.violations(includeWarnings);
		for(;it.hasNext();) {
			Violation v = it.next();
			sb.append("\n\t" + v.getLongMessage());
		}
		return sb.toString();
	}
	return null;
}

}
