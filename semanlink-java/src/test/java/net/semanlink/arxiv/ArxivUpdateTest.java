/* Created on Apr 19, 2020 */
package net.semanlink.arxiv;

import static org.junit.Assert.*;

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
/**
 * loop to update the metadata about arxiv bookmarks
 */
public class ArxivUpdateTest {

@Test public final void test() throws Exception {
	SLModel m = DataLoader.getSLModel();
	Client client = ClientBuilder.newClient();
	Iterator<SLDocument> docs = m.documents();
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		String arxivNum = Arxiv.sldoc2arxivNum(doc, m);
		if (arxivNum == null) continue;
		
		System.out.println(arxivNum + " : " + doc.getURI());
		
	}
}


}
