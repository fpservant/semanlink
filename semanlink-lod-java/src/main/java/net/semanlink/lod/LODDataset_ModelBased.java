/* Created on 24 oct. 07 */
package net.semanlink.lod;

import net.semanlink.graph.jena.TreeDefinition;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;

/**
 * A set of "Non Information Resources", using slash URIs, to be published as Linked Data. 
 * 
 * Basically, a Jena Model, and some abstract methods to define<ul>
 * <li>which resources are "Non Information Resources" (NIR)</li>
 * <li>the correspondence between the URI of a NIR and its RDF and HTML representations</li>
 * </ul>
 * Contains methods to<ul>
 * 		<li>return the RDF description of a given resource. (Note: there are 2. TODO change)</li>
 * 		<li>define some properties as building one or more trees of related concepts (using TreeDefinition)</li>
 * </ul>
 * 
 * See the recommendations from the <a href="http://sites.wiwiss.fu-berlin.de/suhl/bizer/pub/LinkedDataTutorial/">
 * How to publish Linked Data on the Web?</a> document.
 */
public abstract class LODDataset_ModelBased implements LODDataset {
private SPARQLEndPoint sparqlEndPoint;

// use getter
private Property labelProperty;
/** If we want to see some parts of the dataset as one ore more trees. */
private TreeDefinition[] treeDefinitions;

//
//
//

// This one is actually used only to construct the SPARQL endpoint (and for some minor things)
// We would not want to have that when publishing data from a LODDataset that is not really a Jena Model
// for instance when making a facade in front of a service (eg BVM)
public abstract Model getModel();

public SPARQLEndPoint getSPARQLEndPoint() {
	if (sparqlEndPoint == null) {
		Dataset dataSource = DatasetFactory.create(getModel());
		this.sparqlEndPoint = new SPARQLEndPoint(dataSource);
	}
	return this.sparqlEndPoint;
}




/** True iff uri belongs to this dataset. */
public abstract boolean owns(String uri);
/** Is uri, uri supposed in this dataset, the URI of a non information resource? */
public abstract boolean isNonInformationResource(String uri); // what if uri is not in this dataset ???
/**
 * The URI of the RDF representation of a given NIR. 
 * 
 * Must return null if nirURI is not a valid URI for a non-information-resource of this dataset. */
public abstract String nir2rdfURI(String nirURI);
/**
 * The URI of the HTML representation of a given NIR. 
 * 
 * Must return null if nirURI is not a valid URI for a non-information-resource of this dataset. */
public abstract String nir2htmlURI(String nirURI);
/** 
 * The URI of the NIR of this dataset whose RDF representation has URI rdfURI. 
 * 
 * Must return null if rdfURI is not a valid URI, in this dataset, for the rdf resource corresponding to a non-information-resource.*/
public abstract String rdf2nirURI(String rdfURI);
/** 
 * The URI of the NIR of this dataset whose HTML representation has URI htmlURI. 
 * 
 * Must return null if htmlURI is not a valid URI, in this dataset, for the html resource corresponding to a non-information-resource.*/
public abstract String html2nirURI(String htmlURI);

//
// CONSTRUCTION RELATED
//

/** If we want to see some parts of the dataset as a tree. 
 *  To be used with the tree.js javascript */
public void setTreeDefinitions(TreeDefinition[] treeDefinitions) { this.treeDefinitions = treeDefinitions; }
public TreeDefinition[] getTreeDefinition() { return this.treeDefinitions; }

//
// THE RDF TO BE RETURNED ABOUT A RES
//

/**
 * The RDF data to be returned about a URI. 
 * @see getShortRDFAboutRes(String)
 */
public Model getRDFAboutRes(String uri) {
	Resource res = getModel().createResource(uri);
	return getShortLODModelWithLabels(res);
}

/**
 * The minimal RDF data about a URI. 
 */
public Model getShortRDFAboutRes(String uri) {
	Resource res = getModel().createResource(uri);
	return getShortLODModel(res);
}

/** Model to be returned when dereferencing the rdf res corresponding to a NIR */
public Model getRDFAboutNIR(String rdfUri, String nirUri) {
	Model datasetModel = getModel();
	Resource nirRes = datasetModel.createResource(nirUri);
	Model x = getShortLODModelWithLabels(nirRes);
	Resource rdfRes = x.createResource(rdfUri);
	x.add(nirRes, RDFS.isDefinedBy, rdfRes);
	// let's add the link between the nir and the html page
	Property foafPage = x.createProperty("http://xmlns.com/foaf/0.1/page");
	Resource htmlRes = x.createResource(nir2htmlURI(nirUri));
	x.add(nirRes, foafPage, htmlRes);		

	return x;
}

// NOTATION: WE USE THE ADJECTIVE "short" TO REFER TO MODELS THAT ARE EXTRACTED FROM
// this.getModel(), AND TO WHICH WE DO NOT ADD THE EXTRA STATEMENTS LINKING A NIR
// AND ITS CORRESPONDING REPRESENTATIONS

/** Strictly the statements of getModel() where res is subject or object. */ 
private Model getShortLODModel(Resource res) {
	Model x = ModelFactory.createDefaultModel();
	Model mod = getModel();
	StmtIterator stmtIt = mod.listStatements(res, (Property) null, (RDFNode) null);
	x.add(stmtIt);
	stmtIt = mod.listStatements(null, (Property) null, res);
	x.add(stmtIt);
	return x;
}

/** Strictly the statements of getModel() where res is subject or object 
 *  plus the labels of involved resources (provided there are in this.getModel())
 *  plus first level of subtree in case this.treeDefinitions != null. */ 
private Model getShortLODModelWithLabels(Resource res) { // TODO (?) : filter on a lang?
	Model x = ModelFactory.createDefaultModel();
	Model mod = getModel();
	Property labelProp = RDFS.label.inModel(mod); // @TODO cache

	/*StmtIterator stmtIt = mod.listStatements(res, (Property) null, (RDFNode) null);
	x.add(stmtIt);
	stmtIt = mod.listStatements(null, (Property) null, res);
	x.add(stmtIt);*/
	
	StmtIterator stmtIt = mod.listStatements(res, (Property) null, (RDFNode) null);
	for (;stmtIt.hasNext();) {
		Statement sta = stmtIt.nextStatement();
		RDFNode obj = sta.getObject();
		if (obj instanceof Resource) {
			Resource objRes = (Resource) obj;
			// adding label of resource
			StmtIterator labStmtIt = mod.listStatements(objRes, labelProp, (RDFNode) null);
			x.add(labStmtIt);
			//
			if (this.treeDefinitions != null) {
				Property prop = sta.getPredicate();
				// if sta is part of a tree, (that is if objRes is a child of res), add tree statements about objRes
				boolean objIsSonOfRes = false;
				TreeDefinition treeDef = null;
				Property[] props = null;
				for (int itree = 0 ; itree < treeDefinitions.length; itree++) {
					treeDef = this.treeDefinitions[itree];
					props = treeDef.getChildProps();
					if (props != null) {
						for (int i = 0; i < props.length; i++) {
							if (prop.equals(props[i])) {
								objIsSonOfRes = true;
								break;
							}
						}
					}
					if (objIsSonOfRes) break;
				}
				if (objIsSonOfRes) {
					// this statement is a part of a tree
					// let's add tree related statements about objRes (lets add the children and leaves of objRes)
					for (int i = 0; i < props.length; i++) {
						x.add(mod.listStatements(objRes, props[i], (RDFNode) null));
					}
					props = treeDef.getParentProps();
					if (props != null) {
						for (int i = 0; i < props.length; i++) {
							x.add(mod.listStatements((Resource) null, props[i], objRes));
						}
					}
					props = treeDef.getLeafProps();
					for (int i = 0; i < props.length; i++) {
						x.add(mod.listStatements(objRes, props[i], (RDFNode) null));
					}
					props = treeDef.getInvLeafProps();
					if (props != null) {
						for (int i = 0; i < props.length; i++) {
							x.add(mod.listStatements((Resource) null, props[i], objRes));
						}
					}
				}
			}
		}
		x.add(sta);
	}
	stmtIt = mod.listStatements(null, (Property) null, res);
	for (;stmtIt.hasNext();) {
		Statement sta = stmtIt.nextStatement();
		Resource node = sta.getSubject();
		StmtIterator labStmtIt = mod.listStatements(node, labelProp, (RDFNode) null);
		x.add(labStmtIt);
		x.add(sta);
	}
	return x;
}



//
//
//

/** Property used as label. */ // in what circumstances? When searching the model, or on output, or both ???
public Property getLabelProperty() {
	if (this.labelProperty == null)  this.labelProperty = getModel().createProperty(RDFS.label.getURI());
	return this.labelProperty;
}

//
//
//


//
//
//

/*// DEBUG tool
static void numberOf(Model model, String what) {
	Property prop = model.createProperty(RDF.type.getURI());
	Resource res = model.createResource("http://sicg.tpz.renault.fr/sw/2007/10/rdc/schema#" + what);
	Iterator it = model.listStatements(null, prop, res);
	HashSet hs = new HashSet();
	int n = 0;
	for (;it.hasNext();) {
		n++;
		hs.add(it.next());
	}
	System.out.println("Nb de " + what + "s : " + n + " - " + hs.size());
}
*/


}
