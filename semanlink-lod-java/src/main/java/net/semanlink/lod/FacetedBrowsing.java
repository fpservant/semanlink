/* Created on 6 mars 2009 */
package net.semanlink.lod;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.*;

public class FacetedBrowsing {
/* Model of reference. */
// private Model baseModel;
// use getter
// private Model restrictedModel;
/* always use getter. */
// private SPARQLEndPoint endpoint;
private Model model;

//
//
//

/** model will be modified. */
public FacetedBrowsing(Model model) {
	this.model = model;
}

/*
public FacetedBrowsing(Model model, Property[] props) {
	this.baseModel = model;
	this.props = props;
	this.keys = new HashMap<Property,RDFNode>(props.length);
}
*/

//
//
//

public Model getModel() {
	// if (this.restrictedModel == null) return this.baseModel;
	// else return this.restrictedModel;
	return this.model;
}

//
//
//

/** A SPARQL endpoint whose datasource is the base model. */
/*
protected SPARQLEndPoint getSPARQLEndPoint() {
	if (this.endpoint == null) {
		this.endpoint = new SPARQLEndPoint(getModel());
	}
	return this.endpoint;
}
*/

//
//
//

/**
 * In order to implement faceted browsing, iterator over the values of a given prop linked to the documents returned by the search. 
 */
public NodeIterator facet(Property prop) {
	return getModel().listObjectsOfProperty(prop);
}

//
//
//

// VOIR SI CE NE SERAIT PAS MIEUX AVEC LES METHODES JENA : Model difference(Model model)
/**
 * Set the value of one of the props. 
 * Remove the "top subjects" that do not have a statement "subject prop object".
 * (We could also only remove the subjects that have statements "subject prop object2" but not "subject prop object"
 * -- maybe not a common way to proceed for documentation, but "open world assumption" compliant,
 * and maybe faster: we only have to iterate over the "subject prop null",
 * look at the objects of these props, and see whether object is in it or not)
 * 
 * @param subjects : what we want to filter
 * @param prop should be in this.props
 * @param object should be an item returned by facet(prop)
 */
public void restrict(Iterator<Resource> subjects, Property prop, RDFNode object) {
	// RDFNode value = keys.get(prop);
	// if (value != null) {
		// if (value.equals(object)) return;
		// throw new RuntimeException("Would empty the stuff"); // NO!!! one item can have several values for one prop !!!
	// }
	/*
	//THIS IS NOT OK, because we lose every statement from the model that is not directly related to docs:
	//- labels of crits
	//- in the euro5 case, unknown objects -- unless we link unknown objects to documents
	SPARQLEndPoint endpoint = getSPARQLEndPoint();
	String q = null;
	if (object instanceof Resource) {
		q = "DESCRIBE ?doc WHERE {?doc <" + prop.getURI() +"> <" + ((Resource) object).getURI() + ">.}";
	} else {
		q = "DESCRIBE ?doc WHERE {?doc <" + prop.getURI() +"> \"" + object.toString() + "\".}"; // probably not good // TODO
	}
	this.restrictedModel = endpoint.getResultModel(endpoint.createQuery(q));
	this.endpoint = null; // it is indeed no more correct since we changed the model. Would it be OK if we were restricting directly this.model? // TODO
	*/
	Model mod = getModel();
	// ArrayList<Resource> subjects = new ArrayList<Resource>();
	// JenaUtils.topSubjects2Collection(mod, subjects); // NOT GOOD: les diagpackage sont aussi objets des SIE
	ArrayList<Resource> toBeRemoved = new ArrayList<Resource>();
	// for (Resource subject : subjects) {
	for (;subjects.hasNext();) {
		Resource subject = subjects.next();
		StmtIterator sit = mod.listStatements(subject, prop, object);
		if (!sit.hasNext()) toBeRemoved.add(subject);
		sit.close();
	}
	for (Resource subject : toBeRemoved) {
		StmtIterator sit = subject.listProperties();
		mod.remove(sit);
	}

}


//
//
//





}
