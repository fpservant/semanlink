/* Created on 24 oct. 07 */
package net.semanlink.lod;

import net.semanlink.graph.jena.TreeDefinition;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A set of "Non Information Resources", using slash URIs, to be published as Linked Data. 
 * 
 * methods to define<ul>
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
public interface LODDataset {
	
/** Can be null if this dataset doesn't support SPARQL. */
public SPARQLEndPoint getSPARQLEndPoint();

/** True iff uri belongs to this dataset. */
public boolean owns(String uri);
/** Is uri, uri supposed in this dataset, the URI of a non information resource? */
public boolean isNonInformationResource(String uri); // what if uri is not in this dataset ???
/**
 * The URI of the RDF representation of a given NIR. 
 * 
 * Must return null if nirURI is not a valid URI for a non-information-resource of this dataset. */
public String nir2rdfURI(String nirURI);
/**
 * The URI of the HTML representation of a given NIR. 
 * 
 * Must return null if nirURI is not a valid URI for a non-information-resource of this dataset. */
public String nir2htmlURI(String nirURI);
/** 
 * The URI of the NIR of this dataset whose RDF representation has URI rdfURI. 
 * 
 * Must return null if rdfURI is not a valid URI, in this dataset, for the rdf resource corresponding to a non-information-resource.*/
public String rdf2nirURI(String rdfURI);
/** 
 * The URI of the NIR of this dataset whose HTML representation has URI htmlURI. 
 * 
 * Must return null if htmlURI is not a valid URI, in this dataset, for the html resource corresponding to a non-information-resource.*/
public String html2nirURI(String htmlURI);

//
// CONSTRUCTION RELATED
//

/** If we want to see some parts of the dataset as a tree. 
 *  To be used with the tree.js javascript */
public void setTreeDefinitions(TreeDefinition[] treeDefinitions);
public TreeDefinition[] getTreeDefinition();

//
// THE RDF TO BE RETURNED ABOUT A RES
//

/**
 * The RDF data to be returned about a URI. 
 * @see getShortRDFAboutRes(String)
 */
public Model getRDFAboutRes(String uri);

/**
 * The minimal RDF data about a URI. 
 */
public Model getShortRDFAboutRes(String uri);

/** Model to be returned when dereferencing the rdf res corresponding to a NIR */
public Model getRDFAboutNIR(String rdfUri, String nirUri);

//
//
//

/** Property used as label. */ // in what circumstances? When searching the model, or on output, or both ???
public Property getLabelProperty();
}
