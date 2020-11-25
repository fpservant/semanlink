/* Created on Apr 18, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.io.File;
import net.semanlink.sljena.JModel;
import net.semanlink.sljena.ModelFileIOManager;
import net.semanlink.util.Directory;

/** To load data for tests */
public class DataLoader {
static public SLModel getSLModel() throws Exception {
	String servletUri = "http://127.0.0.1:7080/semanlink";
	String thUri = "http://www.semanlink.net/tag"; // The thUri is *not* slash terminated
	File thFile = new File("src/test/files/datadir/tags/slkws.rdf");
	assertTrue(thFile.exists());
	
	// assumed to be "yearmonth" loading mode
	File docDir = new File("src/test/files/datadir/documents/");
	assertTrue(docDir.exists());
	// String base = "http://www.semanlink.net/tag/";

	return getSLModel(servletUri, thUri, thFile, docDir);
}

static public SLModel getSLModel(String servletUri,
		String thUri, File thFile,
		File docDir) throws Exception {
	
	// do this, or will fail later
	try {
		ModelFileIOManager.getInstance();
	} catch (Exception e) {
		ModelFileIOManager.init(servletUri);
	}
		
	SLModel slModel = new JModel();
	slModel.setWebServer(new WebServer());
	String modelUrl = servletUri;
	slModel.setModelUrl(modelUrl);
	SLThesaurus th = slModel.loadThesaurus(thUri, thFile.getParentFile());
	thUri = th.getURI(); // if we passed thUri with a / at the end, the / is removed
	slModel.setDefaultThesaurus(th);
	
	// SLDataFolder
	String base = servletUri + "/doc/";
	SLModel.LoadingMode loadingMode = new SLModel.LoadingMode("yearMonth");
	SLDataFolder defaultDataFolder = slModel.loadSLDataFolder(docDir, base, thUri, loadingMode);	
	slModel.setDefaultDataFolder(defaultDataFolder);
	
	return slModel;
}

static public SLModel fpsSLModel() throws Exception {
	String servletUri = "http://127.0.0.1:8080/semanlink";
	String thUri = "http://www.semanlink.net/tag"; // The thUri is *not* slash terminated
	File thFile = new File("/Users/fps/Semanlink/semanlink-fps/tags/slkws.rdf");
	assertTrue(thFile.exists());
	
	// assumed to be "yearmonth" loading mode
	File docDir = new File("/Users/fps/Sites/fps");
	assertTrue(docDir.exists());
	// String base = "http://www.semanlink.net/tag/";

	return getSLModel(servletUri, thUri, thFile, docDir);
}

// BEWARE WITH THIS !!! -- THIS DELETE FILES
static public void cleanTestDir() throws Exception {
	Directory.Action cleaning = new Directory.Action() {
		@Override
		public void handleFile(File f) throws Exception {
			String fn = f.getName();
			if (fn.endsWith(".rdf")) {
				if (fn.equals("sl.rdf")) return;
				if (fn.equals("slkws.rdf")) return;
				System.out.println("DELETING " + f);
				f.delete();
			}
		}	
	};
	File docDir = new File("src/test/files/datadir/documents/");
	Directory dir = new Directory(docDir);
	dir.doIt(cleaning, true);
	
	docDir = new File("src/test/files/datadir/tags/");
	dir = new Directory(docDir);
	dir.doIt(cleaning, true);
}


}