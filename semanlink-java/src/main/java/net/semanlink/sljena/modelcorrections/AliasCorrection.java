/* Created on 1 avril 2013 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.semanlink.semanlink.SLSchema;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * To be used to correct some documents that are tagged with aliases
 * ATTENTION PROBABLEMENT A APPELER A PARTIR DE SLSERVLET 
 * chercher
 *   	// ((JModel) mod).correctOldKwUris(); // commenter (mais A GARDER!!!)
  		((JModel) mod).correctAlias();

 */
public class AliasCorrection extends AbstractCorrection {
private List<Statement> kw2AliasStatements;
public AliasCorrection(List<Statement> kw2AliasStatements) {
	this.kw2AliasStatements = kw2AliasStatements;
}

@Override public boolean correctDocsModel(Model mod) throws IOException {
	boolean x = false;
	ArrayList<Statement> remove = new ArrayList<Statement>();
	ArrayList<Statement> add = new ArrayList<Statement>();
	for (Statement sta : kw2AliasStatements) {
		Resource kw = sta.getSubject();
		Resource alias = (Resource) sta.getObject();
		StmtIterator it = mod.listStatements((Resource) null, SLSchema.tag, alias);
		for(;it.hasNext();) {
			x = true;
			Statement oldst = it.next();
			remove.add(oldst);
			Statement newst = mod.createStatement(oldst.getSubject(), SLSchema.tag, kw);
			add.add(newst);
		}
	}
	mod.remove(remove);
	mod.add(add);
	return x;
}

}
