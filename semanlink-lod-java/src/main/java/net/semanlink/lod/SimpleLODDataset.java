/* Created on 26 sept. 08 */
package net.semanlink.lod;

import org.apache.jena.rdf.model.*;

public class SimpleLODDataset extends LODDataset_ModelBased {
protected Model model;
protected String base;

//
// CONSTRUCTORS AND FACTORIES
//

public SimpleLODDataset(Model model, String base) {
	this.model = model;
	this.base = base;
	// TODO: use SKOS vocab for TreeDefinitions
}

//
// abstract methods of LODDataSet
//

public Model getModel() { return this.model; }

public boolean owns(String uri) {
	if (base == null) return true; // HACK A CHANGER mis à cause vin form sur infotech
	return uri.startsWith(this.base);
}

public boolean isNonInformationResource(String uri) {
	if (uri.endsWith(".rdf")) return false;
	if (uri.endsWith(".html")) return false;
	// normally, we should only have uri pertaining to the dataset here. 
	if (base == null) return true; // HACK A CHANGER 
	if (uri.startsWith(base)) return true;
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
} // class
