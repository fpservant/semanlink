/* Created on Apr 18, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

public class LoadingDataTest {
	
@Test public final void test() throws Exception {
	SLModel m = DataLoader.testSLModel();
	System.out.println("Nb of docs: " + m.docsSize() + " nb of kws: " + m.kwsSize());
	
	List<SLKeyword> kws = m.getKWsInConceptsSpaceArrayList();
	assertTrue(kws.size() > 0);
	for (int i = 0 ; i < 10 ; i++) {
		SLKeyword kw = kws.get(i);
		System.out.println(kw.getURI() + " : " + kw);
	}
	
	String thUri = "http://www.semanlink.net/tag";
	assertTrue(m.getThesaurus(thUri) != null);
	
	System.out.println("defaultThesaurus uri: " + m.getDefaultThesaurus().getURI() + " base " + m.getDefaultThesaurus().getBase());
	
	assertTrue(m.kwExists(thUri + "/nlp"));
	
	String text = "[Yao 2019] KG-BERT: BERT for Knowledge Graph Completion (Arxiv:1909.03193)";
	Collection<SLKeyword> tags = m.getKeywordsInText(text, Locale.FRANCE, null);
	// on trouve knowledge graphs, à cause de KG (an altlabel de knowledge graphs)
	for (SLKeyword tag : tags) {
		System.out.println(tag.getURI() + " : " + tag);
	}
}

@Test public final void loopDocs() throws Exception {
	SLModel m = DataLoader.testSLModel();
	
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
	
	SLDocument doc = m.bookmarkUrl2Doc("https://github.com/raphaelsty/kdmkr");
}

@Test public final void oneDoc() throws Exception {
	SLModel m = DataLoader.testSLModel();
	
	SLDocument doc = m.bookmarkUrl2Doc("https://github.com/raphaelsty/kdmkr");
	System.out.println(doc.getURI() + " : " + doc);
	List<SLKeyword> kws = doc.getKeywords();
	for (SLKeyword kw : kws) {
		System.out.println("\t"+ kw.getURI() + " : " + kw);
	}
	
	System.out.println();
	
	// TODO REVOIR CES APPELS
	SLKeyword kw1 = m.getKeyword(m.kwLabel2ExistingKwUri("raphaelsty", null));
	System.out.println(kw1.getURI() + " : " + kw1);
	SLKeyword kw2 = m.getKeyword(m.kwLabel2ExistingKwUri("Raphaël Sourty", null));
	System.out.println(kw2.getURI() + " : " + kw2);
	m.kwLabel2SLKw("Raphaël Sourty", null, null);
	SLKeyword kw3 = m.getKeyword(m.kwLabel2ExistingKwUri("raphaelsty", null));
	System.out.println(kw3.getURI() + " : " + kw3);
	
	try (SLDocUpdate du = m.newSLDocUpdate(doc)) {
		du.addKeyword(kw3);
		// du.setDocProperty(SLSchema.NS + "arxiv_" + "firstAuthor", "RAPHAEL", "");
	}
	
	for (SLKeyword kw : doc.getKeywords()) {
		System.out.println("\t"+ kw.getURI() + " : " + kw);
	}
	
}

}