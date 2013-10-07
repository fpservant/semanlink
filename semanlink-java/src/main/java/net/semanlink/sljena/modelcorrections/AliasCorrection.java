/* Created on 1 avril 2013 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;
import net.semanlink.sljena.JModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

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
//			if (alias.getURI().endsWith("lost_boy")) {
//				System.out.println("hello " + kw + " alais : " + alias);
//				System.out.println("\tvire " + oldst);
//				System.out.println("\tadd  " + newst);
//			}
		}
	}
	mod.remove(remove);
	mod.add(add);
	return x;
}

}
