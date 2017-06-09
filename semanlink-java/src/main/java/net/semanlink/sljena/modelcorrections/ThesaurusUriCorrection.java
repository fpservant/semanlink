// @find changing uris
/* Created on 18 juil. 06 */
package net.semanlink.sljena.modelcorrections;
import java.io.IOException;
import java.util.ArrayList;

import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JThing;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.JenaException;

public class ThesaurusUriCorrection extends AbstractCorrection {
private String oldVocabUri;
private String newVocabUri;
public ThesaurusUriCorrection(String oldVocabUri, String newVocabUri) {
	this.oldVocabUri = oldVocabUri;
	this.newVocabUri = newVocabUri;
	if (!oldVocabUri.endsWith("/")) {
		if (!oldVocabUri.endsWith("#")) { // ce test à virer (juste pour les anciennes formes de sl qui utilisaient le # // #thing
			this.oldVocabUri += "/";
		}
	}
	if (!newVocabUri.endsWith("/")) {
		if (!newVocabUri.endsWith("#")) { // ce test à virer (juste pour les anciennes formes de sl qui utilisaient le # // #thing
			this.newVocabUri += "/";
		}
	}
}
/** Corrige le docsModel mod.
 *  Retourne true ssi il y a eu un changement.
 */
public boolean correctDocsModel(Model mod) throws IOException {
	return correctVocabUri(mod);
}
/** Corrige le kwsModel mod.
 *  Retourne true ssi il y a eu un changement.
 */
public boolean correctKwsModel(Model mod) throws IOException {
	return correctVocabUri(mod);
}

private boolean correctVocabUri(Model model) throws JenaException, IOException {
	boolean x = false;
	int nn = oldVocabUri.length();
	Property hasKwPpty = model.getProperty(SLVocab.HAS_KEYWORD_PROPERTY);
	StmtIterator it = model.listStatements(null, hasKwPpty, (RDFNode) null);
	ArrayList<Statement> aVirer = new ArrayList<Statement>();
	ArrayList<Statement> aAjouter = new ArrayList<Statement>();
	for (;it.hasNext();) {
		Statement sta = it.nextStatement();
		RDFNode obj = sta.getObject();
		Node node = obj.asNode();
		String oldUri = node.getURI();
		if (oldUri.startsWith(oldVocabUri)) {
			aVirer.add(sta);
			String newUri = newVocabUri + oldUri.substring(nn);
			JThing trip = new JThing(model, sta.getSubject().getURI(), SLVocab.HAS_KEYWORD_PROPERTY, newUri); // trip comme triple
			aAjouter.add(trip.getStatement());
		}
	}
	int n = aVirer.size(); // le même que celle de aAjouter
	if (n > 0) {
		for (int i = 0; i < n; i++) {
			model.remove(aVirer.get(i));
		}
		for (int i = 0; i < n; i++) {
			model.add(aAjouter.get(i));
		}
		x = true;
	}
	return x;
}

}
