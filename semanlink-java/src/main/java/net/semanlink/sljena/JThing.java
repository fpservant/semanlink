/* Created on 13 sept. 03 */
package net.semanlink.sljena;
import org.apache.jena.rdf.model.*;

/**
 * Classe utilitaire pour travailler avec subject, property et object
 * Meme genre que jena Selector.
 * ATTENTION IL ME SEMBLE Y AVOIR UN PB AVEC OBJECT : RDFNode -- et si c'est un node à uri 
 * UTILISER la méthode de RDFNode : RDFNode inModel(Model m)
 */
public class JThing {
private Model model;
private Resource subject;
private Property ppty;
private RDFNode object;
// Les elements suivants ne sont pas calcules systematiquement mais,
// si on les calcule, on les mets en cache
// ce qui, ATTENTION, necessite de les remettre a null si on change this.subject, ppty ou object
// @see setSubject, etc...
private Statement sta;


// AMELIORER : sur object, un test sur null suopreflu ds sets

JThing(Model model) { this.model = model; }

JThing(Model model, String subjectUri, String propertyUri, String propertyValue, String lang) {
	if (propertyValue != null) this.object =  model.createLiteral(propertyValue, lang);
	init (model, subjectUri, propertyUri, this.object);
}

public JThing(Model model, String subjectUri, String propertyUri, String objectUri) {
	if (objectUri != null) this.object =  model.createResource(objectUri);
	init(model, subjectUri, propertyUri, this.object);
}

JThing(Model model, String subjectUri, String propertyUri, RDFNode object) {
	init(model, subjectUri, propertyUri, object);
}

private void init(Model model, String subjectUri, String propertyUri, RDFNode object) {
	this.model = model;
	if (subjectUri != null) this.subject = model.createResource(subjectUri);
	if (propertyUri != null) this.ppty = model.createProperty(propertyUri);
	if (object != null) this.object =  object ;
	//System.out.println("JThing.init " + this);
}

// GET SET

Resource getSubject() { return this.subject; }
Property getProperty() { return this.ppty; }
RDFNode getObject() { return this.object; }

/** Attention, si un elt est null, il est non modifie */
void sets(String subjectUri, String propertyUri, String propertyValue, String lang) {
	RDFNode object = null;
	if (propertyValue != null) {
		object = model.createLiteral(propertyValue, lang); 
	} else if (lang != null) {
		object = model.createLiteral(((Literal) this.object).getString(), lang); 
	}
	init(this.model, subjectUri, propertyUri, object);
	this.sta = null;
}
/** Attention, si un elt est null, il est non modifie */
void sets(String subjectUri, String propertyUri, String objectUri) {
	RDFNode object = null;
	if (objectUri != null) object = model.createResource(objectUri); 
	init(this.model, subjectUri, propertyUri, object);
	this.sta = null;
}

void setSubject(String subjectUri) {
	this.subject = model.createResource(subjectUri);
	this.sta = null;
}

void setProperty(String propertyUri) {
	this.ppty = model.createProperty(propertyUri);
	this.sta = null;
}

void setObject(String propertyValue, String lang) {
	this.object = model.createLiteral(propertyValue, lang);
	this.sta = null;
}

void setObject(String propertyValueUri) {
	this.object = model.createResource(propertyValueUri);
	this.sta = null;
}

public Statement getStatement() {
	// System.out.println("JThing.getStatement " + this);
	if (this.sta == null) this.sta = model.createStatement(getSubject(), getProperty(), getObject());
	return this.sta;
}
//
/*
public static Statement newStatement(Model model, String docUri, String propertyUri, String propertyValueUri) {
	Resource subject = model.createResource(docUri);
	Property ppty = model.createProperty(propertyUri);
	return model.createStatement(subject, ppty, model.createResource(propertyValueUri));
}

public static Statement newStatement(Model model, String docUri, String propertyUri, String propertyValue, String lang) {
	Resource subject = model.createResource(docUri);
	Property ppty = model.createProperty(propertyUri);
	return model.createStatement(subject, ppty, model.createLiteral(propertyValue, lang));
}
*/
public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("s : ");
	if (this.subject != null) {
		sb.append(this.subject.getURI());
	} else {
		sb.append("null");
	}
	sb.append(" p : ");
	if (this.ppty != null) {
		sb.append(this.ppty.getURI());
	} else {
		sb.append("null");
	}
	sb.append(" o : ");
	if (this.ppty != null) {
		sb.append(this.object.toString());
	} else {
		sb.append("null");
	}
	return sb.toString();
}
} // class
