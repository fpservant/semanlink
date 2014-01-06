// @find changing uris
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * To be used to change a namespace to another one
 */
public class NameSpaceCorrection extends AbstractCorrection {
private String oldUri;
private String newUri;
private int nOld;
private boolean inKwsModel;
private boolean inDocsModel;
/**
 * @param oldUri uri of namespace to be changed. Must end with either # or /
 * @param newUri. Must end with either # or /
 * @param tagProp true if kwModel has to be changed
 * @param docProp true if docModel has to be changed
 */
public NameSpaceCorrection(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {
	if (! ((oldUri.endsWith("#")) || (oldUri.endsWith("/"))) ) throw new IllegalArgumentException("oldUri must end with either # or / ");
	if (! ((newUri.endsWith("#")) || (newUri.endsWith("/"))) ) throw new IllegalArgumentException("newUri must end with either # or / ");
	this.oldUri = oldUri;
	this.nOld = this.oldUri.length();
	this.newUri = newUri;
	this.inKwsModel = inKwsModel;
	this.inDocsModel = inDocsModel;
}

public NameSpaceCorrection(String oldUri, String newUri) {
	this(oldUri, newUri, true, true);
}

public boolean correctDocsModel(Model mod) throws IOException {
	if (!this.inDocsModel) return false;
	return correct(mod);
}

public boolean correctKwsModel(Model mod) throws IOException {
	if (!this.inKwsModel) return false;
	return correct(mod);
}

String changedUri(Resource res) {
	String uri = res.getURI();
	if (!uri.startsWith(oldUri)) return null;
	return newUri + uri.substring(nOld, uri.length());
}

public boolean correct(Model mod) {
	StmtIterator it = mod.listStatements();
	
	List<Statement> oldStaList = new ArrayList<Statement>();
	List<Statement> newStaList = new ArrayList<Statement>();

	for (;it.hasNext();) {
		Statement sta = it.nextStatement();
		boolean change = false;
		String newUri;
		
		Resource subject = sta.getSubject();
		newUri = changedUri(subject);
		if (newUri != null) {
			change = true;
			subject = mod.createResource(newUri);
		}
		
		Property prop = sta.getPredicate();
		newUri = changedUri(prop);
		if (newUri != null) {
			change = true;
			prop = mod.createProperty(newUri);
		}
		
		RDFNode obj = sta.getObject();
		if (obj instanceof Resource) {
			newUri = changedUri((Resource) obj);
			if (newUri != null) {
				change = true;
				obj = mod.createResource(newUri);
			}
		}
		
		if (change) {
			oldStaList.add(sta);
			newStaList.add(mod.createStatement(subject, prop, obj));
		}
	}
	if (oldStaList.size() > 0) {
		mod.remove(oldStaList);
		mod.add(newStaList);
		return true;
	} else {
		return false;
	}
}

}
