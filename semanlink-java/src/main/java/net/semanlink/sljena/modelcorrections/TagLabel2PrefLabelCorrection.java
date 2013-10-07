/* Created on 9 dec 2012 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;
import net.semanlink.util.jena.JenaUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * To be used to change in tags the label from using RDFS.label to SKOS.prefLabel
 */
public class TagLabel2PrefLabelCorrection extends AbstractCorrection {
private String oldUri;
private String newUri;

public TagLabel2PrefLabelCorrection() {
	this.oldUri = RDFS.label.getURI();
	this.newUri = SKOS.prefLabel.getURI();
}

public boolean correctDocsModel(Model mod) throws IOException {
	return false;
}

public boolean correctKwsModel(Model mod) throws IOException {
	return correct(mod);
}

public boolean correct(Model mod) {
	Property oldProp = mod.createProperty(oldUri);
	Property newProp = mod.createProperty(newUri);
	StmtIterator it = mod.listStatements((Resource) null, oldProp, (RDFNode) null);
	List<Statement> oldStaList = new ArrayList<Statement>();
	List<Statement> toBeAddedStaList = new ArrayList<Statement>();
	
	for (;it.hasNext();) {
		Statement sta = it.next();
		Resource s = sta.getSubject();
		// vérifier que le sujet est bien un tag
		// MAIS ATTENTION, il semble que certains tags ds slkws n'aient pas le type Tag
		// (des histoires liés à des aliaïsations anciennes ?)
		// Donc, si ce n'est pas un Tag, je regarde si ça a un broader ou un narrower
		// et si oui, je fait aussi, et rajoute le fait que c un tag
		boolean isTag = JenaUtils.hasRDFType(s, SLSchema.Tag);
		if (!isTag) {
			// est-ce que ca ne devrait pas en être un ?
			StmtIterator xit = null;
			xit = mod.listStatements(s, SKOS.broader,(RDFNode) null);
			if (xit.hasNext()) isTag = true;
			xit.close();
			if (!isTag) {
				xit = mod.listStatements((Resource) null, SKOS.broader, s);
				if (xit.hasNext()) isTag = true;
				xit.close();
				if (!isTag) {
					xit = mod.listStatements(s, SKOS.related,(RDFNode) null);
					if (xit.hasNext()) isTag = true;
					xit.close();
					if (!isTag) {
						xit = mod.listStatements((Resource) null, SKOS.related, s);
						if (xit.hasNext()) isTag = true;
						xit.close();
					}
				}
			}
			if (isTag) {
				Statement newSta = mod.createStatement(s, RDF.type, SLSchema.Tag);
				toBeAddedStaList.add(newSta);
			}
		}

		if (isTag) {
			// System.out.println("REM " + sta);
			Statement newSta = mod.createStatement(s, newProp, sta.getObject());
			toBeAddedStaList.add(newSta);
			oldStaList.add(sta);
			// System.out.println("ADD " + newSta);
		}
	}
	boolean x = false;
	if (oldStaList.size() > 0) {
		x = true;
		mod.remove(oldStaList);
	}
	if (toBeAddedStaList.size() > 0) {
		x = true;
		mod.add(toBeAddedStaList);
	}
	return x;
}

}
