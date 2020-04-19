/* Created on Apr 18, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Test;

import net.semanlink.sljena.JModel;
import net.semanlink.sljena.ModelFileIOManager;

public class LoadingDataTest {
	
@Test public final void test() throws Exception {
	SLModel m = DataLoader.getSLModel();
	System.out.println("Nb of docs: " + m.docsSize() + " nb of kws: " + m.kwsSize());
	
//	List<SLKeyword> kws = m.getKWsInConceptsSpaceArrayList();
//	assertTrue(kws.size() > 0);
//	for (int i = 0 ; i < 10 ; i++) {
//		SLKeyword kw = kws.get(i);
//		System.out.println(kw.getURI() + " : " + kw);
//	}
	
	String thUri = "http://www.semanlink.net/tag";
	assertTrue(m.getThesaurus(thUri) != null);
	
	assertTrue(m.kwExists(thUri + "/nlp"));
	
	String text = "[Yao 2019] KG-BERT: BERT for Knowledge Graph Completion (Arxiv:1909.03193)";
	Collection<SLKeyword> tags = m.getKeywordsInText(text, Locale.FRANCE, null);
	// on trouve knowledge graphs, à cause de KG (an altlabel de knowledge graphs)
	for (SLKeyword tag : tags) {
		System.out.println(tag.getURI() + " : " + tag);
	}
}

@Test public final void loopDocs() throws Exception {
	SLModel m = DataLoader.getSLModel();
	
	// on pourrait s'en servir, boucler sur les fichiers
	// charger le jena model, et faire des modifs directes
	for (Object f : m.getOpenDocsFiles()) {
		System.out.println(f);
	}
	
	Iterator<SLDocument> docs = m.documents();
	
	int k = 0;
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		System.out.println(doc.getURI() + " : " + doc);
		k++;
		if (k > 5) break;
	}
}
}