/* Created on 25 juil. 2010 */
package net.semanlink.servlet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

import net.semanlink.graph.jena.TreeDefinition;
import net.semanlink.lod.LODDataset;
import net.semanlink.lod.SPARQLEndPoint;

//
//@TODO !!! - PARCE QUE LÀ, LE MOINS QU'ON PUISSE DIRE, C'EST QUE C'EST À MINIMA !!!
//
// va quand même pour les liens ds le html généré, (en dépit du fait qu'on n'ait rien documenté ici, parce que "owns" return false,
// et voir class SLRDFIntoDiv ds SLSparqlServlet
// BREF, c'est vraiement du hack
//
/**
 * Ultra minimalist implementation of a LODDataset for semanlink.
 */
public class SLLODDataset implements LODDataset {
private SPARQLEndPoint sparqlEndPoint;
private String contextUrl; // eg http://www.semanlink.net


SLLODDataset(SPARQLEndPoint endpoint, String contextUrl) {
	this.sparqlEndPoint = endpoint;
	this.contextUrl = contextUrl;
}
	
public Property getLabelProperty() {
	return RDFS.label;
}

public Model getRDFAboutNIR(String rdfUri, String nirUri) {
	// TODO Auto-generated method stub
	return null;
}

public Model getRDFAboutRes(String uri) {
	// TODO Auto-generated method stub
	return null;
}

public SPARQLEndPoint getSPARQLEndPoint() {
	return sparqlEndPoint;
}

public Model getShortRDFAboutRes(String uri) {
	// TODO Auto-generated method stub
	return null;
}

public TreeDefinition[] getTreeDefinition() {
	// TODO Auto-generated method stub
	return null;
}


public void setTreeDefinitions(TreeDefinition[] treeDefinitions) {
	// TODO Auto-generated method stub
	
}

// copié de SimpleLODDataset

public boolean owns(String uri) {
	return uri.startsWith(this.contextUrl);
	/*
	///////////////////// A CHANGER HACK TEST 2010/12
	if (uri.startsWith(this.contextUrl)) return true;
	if (uri.startsWith("http://www.semanlink.net")) return true;
	return false;*/
}

// TODO : ceci est un peu n'importe quoi
public boolean isNonInformationResource(String uri) {
	if (uri.endsWith(".rdf")) return false;
	if (uri.endsWith(".html")) return false;
	// normally, we should only have uri pertaining to the dataset here. 
	if ((uri.startsWith(contextUrl) && (uri.contains("tag")))) return true;
	return false; 
}

public String nir2rdfURI(String nirURI) {
	return nirURI + ".rdf";
}

public String nir2htmlURI(String nirURI) {
	return nirURI + ".html";
}

public String rdf2nirURI(String rdfURI) {
	if (rdfURI.endsWith(".rdf")) {
		return rdfURI.substring(0, rdfURI.length()-4);
	}
	// not a rdf resource
	return null;
}

public String html2nirURI(String htmlURI) {
	if (htmlURI.endsWith(".html")) {
		return htmlURI.substring(0, htmlURI.length()-5);
	}
	// not a html resource
	return null;
}

}
