/* Created on Apr 18, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import net.semanlink.sljena.JModel;
import net.semanlink.sljena.ModelFileIOManager;

public class LoadingDataTest {
	
@Test public final void test() throws Exception {
	SLModel m = getSLModel();
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
	SLModel m = getSLModel();
	
	// on piurrait s'en servir, boucler sur les fichiers
	// charger le jena model, et faire des modifs directes
	for (Object f : m.getOpenDocsFiles()) {
		System.out.println(f);
	}
}

//
// LOADING A SLMODEL
//

private SLModel getSLModel() throws Exception {
	String modelUri = "http://127.0.0.1:7080/semanlink/mod"; // ?
	String thUri = "http://www.semanlink.net/tag"; // The thUri is *not* slash terminated
	File thFile = new File("src/test/files/datadir/tags/slkws.rdf");
	assertTrue(thFile.exists());
	
	// assumed to be "yearmonth" loading mode
	File docDir = new File("src/test/files/datadir/documents/");
	assertTrue(docDir.exists());
	// String base = "http://www.semanlink.net/tag/";

	return loadMinimalSLModel(modelUri, thUri, thFile, docDir);
}

static private SLModel loadMinimalSLModel(String modelUri,
		String thUri, File thFile,
		File docDir) throws Exception {
	
	// do this, or will fail later
	try {
		ModelFileIOManager.getInstance();
	} catch (Exception e) {
		ModelFileIOManager.init("http://127.0.0.1:7080/semanlink");
	}
		
	SLModel slModel = new JModel();
	slModel.setWebServer(new WebServer());
	slModel.setModelUrl(modelUri);
	SLThesaurus th = slModel.loadThesaurus(thUri, thFile.getParentFile());
	thUri = th.getURI(); // if we passed thUri with a / at the end, the / is removed
	slModel.setDefaultThesaurus(th);
	
	// SLDataFolder
	String base = "http://www.semanlink.net/doc/";
	SLModel.LoadingMode loadingMode = new SLModel.LoadingMode("yearMonth");
	SLDataFolder defaultDataFolder = slModel.loadSLDataFolder(docDir, base, thUri, loadingMode);	
	slModel.setDefaultDataFolder(defaultDataFolder);
	
	return slModel;
}
}