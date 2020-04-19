/* Created on Apr 18, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.io.File;
import net.semanlink.sljena.JModel;
import net.semanlink.sljena.ModelFileIOManager;

/** To load data for tests */
public class DataLoader {
static public SLModel getSLModel() throws Exception {
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