/* Created on 3 janv. 2006 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * To be used to change a URI to another one
 */
public class URICorrection extends AbstractCorrection {
private String oldUri;
private String newUri;
private boolean inKwsModel;
private boolean inDocsModel;
/**
 * @param oldUri uri of property to be changed
 * @param newUri 
 * @param tagProp true if kwModel has to be changed
 * @param docProp true if docModel has to be changed
 */
public URICorrection(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {
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
	boolean x = false;
	
	Resource oldRes = mod.getResource(oldUri);
	Resource newRes = mod.getResource(newUri);

	StmtIterator it;
	List<Statement> oldStaList;
	boolean thereIsChange;
	// StmtIterator it = mod.listStatements((Resource) null, (Property) null, (Resource) null);
	
	it = mod.listStatements(oldRes, (Property) null, (RDFNode) null);
	oldStaList = it.toList();
	int n = oldStaList.size();
	thereIsChange = (n > 0);
	it.close();
	for (int i = 0;i < n;i++) {
		Statement oldSta = oldStaList.get(i);
		Statement newSta = mod.createStatement(newRes, oldSta.getPredicate(), oldSta.getObject());
		mod.add(newSta);
	}
	if (thereIsChange) mod.remove(oldStaList);
	x = x || thereIsChange;
	
	Property oldProp = mod.getProperty(oldUri);
	Property newProp = mod.getProperty(newUri);
	it = mod.listStatements((Resource) null, oldProp, (RDFNode) null);
	oldStaList = it.toList();
	n = oldStaList.size();
	thereIsChange = (n > 0);
	it.close();
	for (int i = 0;i < n;i++) {
		Statement oldSta = oldStaList.get(i);
		Statement newSta = mod.createStatement(oldSta.getSubject(), newProp, oldSta.getObject());
		mod.add(newSta);
	}
	if (thereIsChange) mod.remove(oldStaList);
	x = x || thereIsChange;

	it = mod.listStatements((Resource) null, (Property) null, oldRes);
	oldStaList = it.toList();
	n = oldStaList.size();
	thereIsChange = (n > 0);
	it.close();
	for (int i = 0;i < n;i++) {
		Statement oldSta = oldStaList.get(i);
		Statement newSta = mod.createStatement(oldSta.getSubject(), oldSta.getPredicate(), newRes);
		mod.add(newSta);
	}
	if (thereIsChange) mod.remove(oldStaList);
	x = x || thereIsChange;
	
	return x;
}

}
