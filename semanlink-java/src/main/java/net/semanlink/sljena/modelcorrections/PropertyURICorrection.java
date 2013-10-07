/* Created on 3 janv. 2006 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * To be used to change the URI of a property, for instance to change
 * rdfs:label by skos:prefLabel
 */
public class PropertyURICorrection extends AbstractCorrection {
private String oldUri;
private String newUri;
private boolean tagProp;
private boolean docProp;
/**
 * @param oldUri uri of property to be changed
 * @param newUri 
 * @param tagProp true if kwModel has to be changed
 * @param docProp true if docModel has to be changed
 */
public PropertyURICorrection(String oldUri, String newUri, boolean tagProp, boolean docProp) {
	this.oldUri = oldUri;
	this.newUri = newUri;
	this.tagProp = tagProp;
	this.docProp = docProp;
}

public boolean correctDocsModel(Model mod) throws IOException {
	if (!this.docProp) return false;
	return correct(mod);
}

public boolean correctKwsModel(Model mod) throws IOException {
	if (!this.tagProp) return false;
	return correct(mod);
}

public boolean correct(Model mod) {
	Property oldProp = mod.getProperty(oldUri);
	Property newProp = mod.getProperty(newUri);
	StmtIterator it = mod.listStatements((Resource) null, oldProp, (RDFNode) null);
	List<Statement> oldStaList = it.toList();
	int n = oldStaList.size();
	boolean x = (n > 0);
	it.close();
	for (int i = 0;i < oldStaList.size();i++) {
		Statement oldSta = oldStaList.get(i);
		// System.out.println("REM " + oldSta);
		Statement newSta = mod.createStatement(oldSta.getSubject(), newProp, oldSta.getObject());
		mod.add(newSta);
		// System.out.println("ADD " + newSta);
	}
	if (x) mod.remove(oldStaList);
	return x;
}

}
