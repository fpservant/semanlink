/* Created on Apr 4, 2020 */
package net.semanlink.sljena;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;

public class KWsModelTest {

@Test
public final void loadKwsModelTest() throws JenaException, IOException {
	Model kwsModel = loadTestKwsModel();
	System.out.println(kwsModel.size());
	assertTrue(kwsModel.size() > 0);
	
//	Iterator<Statement> sit = kwsModel.listStatements();
//	for(;sit.hasNext();) {
//		System.out.println(sit.next());
//	}
	
	Resource kw = kwsModel.createResource("http://www.semanlink.net/tag/word2vec");
	// check this kw is the son of some kws
	// (note: we don't find any narrower, as we always only use broader)
	Property p = SKOS.broader;
	Iterator<RDFNode> nit = kwsModel.listObjectsOfProperty(kw, p);
	assertTrue(nit.hasNext());
	for(;nit.hasNext();) {
		System.out.println(nit.next());
	}
	
	// check kw has type sl:Tag
	nit = kwsModel.listObjectsOfProperty(kw, RDF.type); 
	assertTrue(nit.hasNext());
	boolean ok = false;
	for(;nit.hasNext();) {
		RDFNode n = nit.next();
		if (n.equals(SLSchema.Tag)) {
			ok = true;
			break;
		}
	}
	assertTrue(ok);
	
	
}

static public Model loadTestKwsModel() throws JenaException, IOException {
	Model kwsModel = ModelFactory.createDefaultModel();
	File f = new File("src/test/files/datadir/tags/slkws.rdf");
	String base = "http://www.semanlink.net/tag/";
	assertTrue(f.exists());
	ModelFileIOManager.readModel(kwsModel, f.getAbsolutePath(), base);
	return kwsModel;
}

}
