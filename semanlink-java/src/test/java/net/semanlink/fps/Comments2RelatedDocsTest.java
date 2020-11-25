/* Created on Nov 23, 2020 */
package net.semanlink.fps;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.semanlink.DataLoader;
import net.semanlink.semanlink.SLDocCommentUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JDocument;
import net.semanlink.util.FileUriFormat;

public class Comments2RelatedDocsTest { // 2020-11

@BeforeClass
public static void setUpBeforeClass() throws Exception {
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
}

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

// SLUtils.title2shortFilename(title)

@Test public final void title2shortFilenameTest() throws Exception {
	System.out.println((int)'«');
	String s = "« ça va allé ô j'aime à+ alors";
	String sfn = SLUtils.title2shortFilename(s);
	System.out.println(sfn);
	System.out.println(URLEncoder.encode(sfn,"UTF-8"));
	
	// from NewBookmarkCreationData:
	// bkmUri = mod.fileToUri(new File(bkmDir, shortFilename));
	// return FileUriFormat.filenameToUri(f.getPath());
	
	String bkmDir = "/Users/fps/Sites/fps/2020/11";
	System.out.println(FileUriFormat.filenameToUri(bkmDir + sfn)); // manque l'histoire du webserver
	
	SLModel m = DataLoader.fpsSLModel();
	System.out.println(m.fileToUri(new File(bkmDir, sfn)));
	
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	String bkmDirUrl = contextUrl + "/doc/2020/11/";
	
	URI uri = new URI (bkmDirUrl + sfn);
	String x = uri.toASCIIString();

	System.out.println(x); // that's good
	
	String title = "« ça va allé ô j'aime à+ alors";
	x = title2bookmarkUri(title, bkmDirUrl);
	assertTrue(x.equals("http://127.0.0.1:8080/semanlink/doc/2020/11/%C2%AB_ca_va_alle_o_j_aime_a_alors"));
}

/**
 * The URI to use for a bookmark
 * 
 * Attention, ne vérifie pas si existe ou pas
 * 
 * @param title
 * @param bkmDirUrl eg. http://127.0.0.1:8080/semanlink/doc/2020/11/ == contextUrl + "/doc/2020/11/"
 * @throws URISyntaxException 
 * @throws Exception
 */
public static String title2bookmarkUri(String title, String bkmDirUrl) throws URISyntaxException {
	// System.out.println((int)'«');
	// String title = "« ça va allé ô j'aime à+ alors";
	String sfn = SLUtils.title2shortFilename(title); // «_ca_va_alle_o_j_aime_a_alors
	// System.out.println(URLEncoder.encode(sfn,"UTF-8")); // %C2%AB_ca_va_alle_o_j_aime_a_alors
	
	// from NewBookmarkCreationData:
	// bkmUri = mod.fileToUri(new File(bkmDir, shortFilename));
	// return FileUriFormat.filenameToUri(f.getPath());
	
	// String bkmDir = "/Users/fps/Sites/fps/2020/11";
	// System.out.println(FileUriFormat.filenameToUri(bkmDir + sfn)); // file:///Users/fps/Sites/fps/2020/11%C2%AB_ca_va_alle_o_j_aime_a_alors // manque l'histoire du webserver	
	// SLModel m = DataLoader.fpsSLModel();
	// System.out.println(m.fileToUri(new File(bkmDir, sfn))); // http://127.0.0.1:8080/semanlink/doc/2020/11/%C2%AB_ca_va_alle_o_j_aime_a_alors
	
	if (!bkmDirUrl.endsWith("/")) bkmDirUrl += "/";
	URI uri = new URI (bkmDirUrl + sfn);
	return uri.toASCIIString();
}



// @Test
public final void test() throws Exception {
	SLModel m = DataLoader.fpsSLModel();

	// OUAIS : le context qu'on passe dans plusieurs méthodes de SLModel,
	// il semble qu'il ne soit autre chose que SLModel.getModelUrl() !!!
	// TODO CHECK THAT, ça serait cool !
	
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	System.out.println(m.getModelUrl() + "\n" + contextUrl);
	assertTrue(contextUrl.equals(m.getModelUrl()));

	// ben non, c un vieux doc, son uri la page sur le web
	// String docUri = "http://127.0.0.1:8080/semanlink/doc/?uri=https%3A%2F%2Farxiv.org%2Fabs%2F1902.09229";
	String docUri = "https://arxiv.org/abs/1902.09229";
	SLDocument doc = 	m.getDocument(docUri);
	Literal lit = getComment( ((JDocument) doc).getRes());
	String comment = lit.getString();
	List<String> links = SLDocCommentUpdate.extractLinks(comment, contextUrl);
	
	for (String link : links) {
		try {
			String linkUri = SLDocCommentUpdate.link2slUri(link, m, contextUrl);
			if (linkUri == null) continue;
			System.out.println(link + " \n-> " + linkUri);
		} catch (Exception e) {
			System.err.println("Bad link: "+ link);
		}	
	}

}

// @Test
public final void ATTENTION_MODIF_VRAI_SEMANLINK_FPS() throws Exception {
	SLModel m = DataLoader.fpsSLModel();
	m.getModelUrl();
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	System.out.println(m.getModelUrl() + "\n" + contextUrl);
	assertTrue(contextUrl.equals(m.getModelUrl()));
	
	Iterator<SLDocument> docs = m.documents();
	int k = 0;
	int kchanges = 0;
	
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		// zut, la langue
		//		String comment = doc.getComment();
		
		
		Literal lit = getComment( ((JDocument) doc).getRes());
		if (lit == null) continue;
		String comment = lit.getString();
		String lang = lit.getLanguage();

		
		// System.out.println(doc.getURI() + " lang : " + lang + "\n" + comment + "\n");
		
		try {
			
			// if (doc.getURI().equals("https://www2018.thewebconf.org/program/web-content-analysis/")) {
				boolean somethingChanged = SLDocCommentUpdate.changeComment(m, doc, comment, lang, contextUrl);
				if (somethingChanged) {
					System.out.println("change: " + doc.getURI());
					kchanges++;
				}
			// }

		} catch (Exception e) {
			// exception on http://arxiv.org/pdf/cs.DS/0310019
			e.printStackTrace();
		}
		
		k++;
		
		if (kchanges > 0) break;
		// if (k > 100) break;
	}
}

// cf. JenaUtils
static public Literal getComment(Resource res) {
	Model m = res.getModel();
	NodeIterator ite = m.listObjectsOfProperty(res, m.getProperty(SLVocab.COMMENT_PROPERTY));
	Literal x = null;
	if (ite.hasNext()) {
		
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (node instanceof Literal) {
				x = (Literal) node;
				if (ite.hasNext()) {
					throw new RuntimeException("several comments for " + res);
				}
			} else {
				throw new RuntimeException("not a literal comment " + res);
			}
		}
	}
	ite.close();
	return x;
}

}
