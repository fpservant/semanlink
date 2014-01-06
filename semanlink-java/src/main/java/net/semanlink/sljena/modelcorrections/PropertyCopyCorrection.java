/* Created on 7 dec 2012 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * To be used to copy statements involving a given copy, replacing the property
 * (eg s sl:hasParent o. ->
 * s sl:hasParent o ; skos:broader o.
 */
public class PropertyCopyCorrection extends AbstractCorrection {
private String oldUri;
private String newUri;
private boolean inKwsModel;
private boolean inDocsModel;
/**
 * @param oldUri uri of property to be copied
 * @param newUri 
 * @param inKwsModel true if kwModel has to be changed
 * @param inDocsModel true if docModel has to be changed
 */
public PropertyCopyCorrection(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {
	this.oldUri = oldUri;
	this.newUri = newUri;
	this.inKwsModel = inKwsModel;
	this.inDocsModel = inDocsModel;
}

public boolean correctDocsModel(Model mod) throws IOException {
	if (!this.inDocsModel) return false;
	return correct(mod);
}

public boolean correctKwsModel(Model mod) throws IOException {
	if (!this.inKwsModel) return false;
	return correct(mod);
}

public boolean correct(Model mod) {
	StmtIterator it;
	Property oldProp = mod.getProperty(oldUri);
	Property newProp = mod.getProperty(newUri);
	it = mod.listStatements((Resource) null, oldProp, (RDFNode) null);
	boolean thereIsChange = it.hasNext();
	for (;it.hasNext();) {
		Statement oldSta = it.next();
		Statement newSta = mod.createStatement(oldSta.getSubject(), newProp, oldSta.getObject());
		mod.add(newSta);
	}

	return thereIsChange;
}

}
