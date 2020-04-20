/* Created on Apr 19, 2020 */
package net.semanlink.arxiv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import net.semanlink.metadataextraction.ExtractorData;
import net.semanlink.semanlink.DataLoader;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.servlet.SLServlet;
/**
 * loop to update the metadata about arxiv bookmarks
 */
public class ArxivLoopUpdateTest {

@Test public final void test() throws Exception {
	SLModel m = DataLoader.getSLModel();
	Client client = ClientBuilder.newClient();
	ArxivMetadataExtractor extractor = new ArxivMetadataExtractor();
	Iterator<SLDocument> docs = m.documents();
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		String arxivNum = Arxiv.sldoc2arxivNum(doc, m);
		if (arxivNum == null) continue;
		
		System.out.println(arxivNum + " : " + doc.getURI());
		ExtractorData extractorData = new ExtractorData(doc, m, client);
		
		// it happens that this doesn't handle keywords: very good here
		// (we do  not want to recompute them)
		extractor.doIt(extractorData);
	}
}

// @Test 
public final void ATTENTION_MODIF_VRAI_SEMANLINK_FPS() throws Exception {
	SLModel m = fpsSLModel();
	Client client = ClientBuilder.newClient();
	ArxivMetadataExtractor extractor = new ArxivMetadataExtractor();
	Iterator<SLDocument> docs = m.documents();
	int k = 0;
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		String arxivNum = Arxiv.sldoc2arxivNum(doc, m);
		if (arxivNum == null) continue;
		
		System.out.println(arxivNum + " : " + doc.getURI());
		ExtractorData extractorData = new ExtractorData(doc, m, client);
		
		// it happens that this doesn't handle keywords: very good here
		// (we do  not want to recompute them)
		extractor.doIt(extractorData);
		
		k++;
		if (k > 1) break;
	}
}

static private SLModel fpsSLModel() throws Exception {
	String servletUri = "http://127.0.0.1:8080/semanlink";
	String thUri = "http://www.semanlink.net/tag"; // The thUri is *not* slash terminated
	File thFile = new File("/Users/fps/Semanlink/semanlink-fps/tags/slkws.rdf");
	assertTrue(thFile.exists());
	
	// assumed to be "yearmonth" loading mode
	File docDir = new File("/Users/fps/Sites/fps");
	assertTrue(docDir.exists());
	// String base = "http://www.semanlink.net/tag/";

	return DataLoader.getSLModel(servletUri, thUri, thFile, docDir);
}




}
