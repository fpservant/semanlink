/* Created on 9 dec 2012 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * To be used to change in tags the label from using RDFS.label to SKOS.prefLabel
 */
public class AliasToSkosAltLabelCorrection extends AbstractCorrection {
public AliasToSkosAltLabelCorrection() {
}

public boolean correctDocsModel(Model mod) throws IOException {
	return false;
}

public boolean correctKwsModel(Model mod) throws IOException {
	return correct(mod);
}

public boolean correct(Model mod) {
	StmtIterator sit = mod.listStatements((Resource) null, SLSchema.hasAlias, (Resource) null);
	for (;sit.hasNext();) {
		Statement sta = sit.next();
		Resource s = sta.getSubject();
		Resource alias = (Resource) sta.getObject();
		NodeIterator labels = mod.listObjectsOfProperty(alias, RDFS.label);
		for (;labels.hasNext();) {
			mod.add(s,SKOS.altLabel,labels.next());
		}
		StmtIterator sitLabels = mod.listStatements(alias, RDFS.label, (RDFNode) null);
		mod.remove(sitLabels);
	}
	sit = mod.listStatements((Resource) null, SLSchema.hasAlias, (Resource) null);
	mod.remove(sit);
	return true;
}

}
