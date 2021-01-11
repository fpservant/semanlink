/* Created on Jan 8, 2021 */
package net.semanlink.fps;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.semanlink.DataLoader;
import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLDataFolder;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLResource;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JModel;
import net.semanlink.semanlink.SLModel.DocMetadataFile;

/**
 * to create creation dates for old stuff which had no
 * (docs older than 2002-12, tags than 2004 (?)
 */
public class CreationDateCorrection {

private static SLModel m;
private static String tagns;
private static String contextUrl;

@BeforeClass
public static void setUpBeforeClass() throws Exception {
	m = DataLoader.fpsSLModel();
	contextUrl = "http://127.0.0.1:8080/semanlink";
	tagns = m.getDefaultThesaurus().getBase();
	System.out.println("tag ns (thesaurus uri): " + tagns);
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
	m = null;
	contextUrl = null;
}

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

@Test
public final void test() throws Exception {
//	SLKeyword tag = m.getKeyword(tagns + "nlp");
//	HashMap hm = tag.getPropertiesAsStrings();
//	Iterator it = hm.entrySet().iterator();
//	for (;it.hasNext();) {
//		Entry e = (Entry) it.next();
//		System.out.println(e.getKey() + " : " + e.getValue());
//	}

	SLKeyword tag;
//	tag = m.getKeyword(tagns + "nlp");
//	printCreationDate(tag);
	tag = m.getKeyword(tagns + "einstein");
	printCreationDate(tag);
	
	oldestDocDate(tag);
}

@Test
public final void tagloop() throws Exception {
	List<SLKeyword> alltags = m.getKWs(m.getDefaultThesaurus());
	for (SLKeyword tag : alltags) {
		String cdate = getCreationDate(tag);
		if (cdate != null) continue;
		
		cdate = oldestDocDate(tag);
		System.out.println("Tag wo creation date: " + tag + " oldest doc date: " + cdate);
	}
}

//basé uniquement sur les creationDate existantes
private SLDocument oldestDocBasedOnCreationDate(SLKeyword tag) {
	List<SLDocument> docs = tag.getDocuments();
	SLUtils.sortByProperty(docs, SLVocab.SL_CREATION_DATE_PROPERTY);
	//for (SLDocument doc : docs) {
	//	printCreationDate(doc);
	//	docUri2YyyyMm(doc);
	//}
	return docs.get(docs.size()-1); // last one is oldest -- but creation date maybe null;
}

// ACH BORDEL : cas de tag sans creation date, sans doc mais avec des fils (qui ont des docs
// ou une creation date)

// remaining pbs : 
// - 2000-00
// - oldest doc date: null
// - doc wo yyymm: http://fr.wikipedia.org

private String oldestDocDate(SLKeyword tag) throws IOException, URISyntaxException {
	List<SLDocument> docs = tag.getDocuments();
	SLUtils.sortByProperty(docs, SLVocab.SL_CREATION_DATE_PROPERTY);
	String xdate = "3000-01-01";
	SLDocument xdoc = null;
	for (SLDocument doc : docs) {
		String date = getCreationDateExtended(doc);
		if (date == null) {
			continue;
		}
		if (date.compareTo(xdate) < 0) {
			xdate = date;
			xdoc = doc;
		}
	}
	// System.out.println("oldest doc for " + tag.getURI() + " : " + xdoc + " date : " + xdate);
	
	if ("3000-01-01".equals(xdate)) {
		xdate = null;
	}
	return xdate;
}





// basé uniquement sur les creationDate existantes
private String getCreationDate(SLResource slres) {
	PropertyValues pv = slres.getProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
	if (pv != null) {
		if (pv.size() > 1) {
			System.err.println("More than one creation date : " + slres);
			// throw new RuntimeException("More than one creation date : " + slres);
		}
		String creation = pv.getFirstAsString();
		return creation;
	} else {
		return null;
	}
}

// regarde l'uri du doc si sl:creationDate n'existe pas
private String getCreationDateExtended(SLDocument doc) throws IOException, URISyntaxException {
	String date = getCreationDate(doc);
	if (date == null) {
		SLModel.YYYYMM ym = docUri2YyyyMm(doc);
		if (ym != null) {
			date = ym.yyyy + "-" + ym.mm + "-15";
		}
	}
	return date;
}


// basé uniquement sur url du doc
// Manque donc les pages web
private SLModel.YYYYMM docUri2YyyyMm(SLDocument doc) throws IOException, URISyntaxException {
	String uri = doc.getURI();
	if (uri.startsWith(contextUrl)) {
		if (!uri.startsWith(contextUrl + "/doc/")) {
			throw new RuntimeException(uri);
		}
	} else {
		// ceci ne marche pas du tout - méthode inutilisée, virée de SLModel, du coup
//		String folder = m.docUri2Folder(uri);
//		System.out.println("doc wo yyymm: " + doc + " FOLDER: " + folder);
		DocMetadataFile dmf = m.doc2DocMetadataFile(uri);
		// System.out.println("doc wo yyymm: " + doc + " FOLDER: " + dmf.getDataFolder().getFile());
		
		// PROBLEME
		// ex http://open.bbc.co.uk/catalogue/infax
		// défini dans 2006/04
		// pas de date de création
		// DocMetadataFile.getDataFolder().getFile() retourne /Users/fps/Sites/fps
		// C'est là que sont ajoutées de nlles metadonnées (testé avec ajout d'un kw)
		// Mais of course, impossible de supprimer un ancien kw
		
		// -> IL FAUDRAIT CORRIGER CES CAS LA
		// (docs sans creation date, dans des dossiers yyyy/mm)
		// Il me semblait que ça avait été résolu via un modelcorrections
		// net.semanlink.sljena.modelcorrections.CreationDateCorrection ?
		// Manifestement non
		
		// ennuyeux à corriger : passe forcément par un truc genre modelcorrection
		// vu qu'il faut faire fichier par fichier (accéder au fichier pour savoir sa date
		// et la mettre au doc)
		
		/*
  <rdf:Description rdf:about="http://open.bbc.co.uk/catalogue/infax">
    <sl:tag rdf:resource="&tag;bbc"/>
    <sl:tag rdf:resource="&tag;huge_rdf_data_source"/>
    <dc:source>BBC</dc:source>
  </rdf:Description>
		 */
		return null;
	}
	String s = uri.substring((contextUrl + "/doc/").length());
	String yyyy = s.substring(0,4);
	Integer.parseInt(yyyy);
	String mm = s.substring(5,7);
	Integer.parseInt(mm);
	// System.out.println("\tyyyy-mm: " + yyyy + "-" + mm);
	return new SLModel.YYYYMM(yyyy, mm);
}

private void printCreationDate(SLResource slres) {
	String creation = getCreationDate(slres);
	System.out.println(slres + " creation: " +creation);
}

//
//
//

// @Test // attention, change le doc en question
public final void setCreationDateOfAnOldDoc() throws Exception {
	SLDocument doc = m.getDocumentIfExists(contextUrl + "/doc/2002/01/Tomcat_OSX/");
	String curCreationdate = getCreationDate(doc);
	System.out.println("Doc: " + doc + " creation: " + curCreationdate);
	
	SLModel.YYYYMM ym = docUri2YyyyMm(doc);
	if (ym != null) {
		System.out.println("File's yyyy-mm: " + ym.yyyy + "-" + ym.mm);
	} else {
		System.out.println("zut");
	}

//	List<SLKeyword> tags = doc.getKeywords();
//	for (SLKeyword tag : tags) {
//		printCreationDate(tag);
//		// oldestDoc(tag);
//	}
	
	if (curCreationdate == null) {
		if (ym != null) {
			String newCreationDate = ym.yyyy + "-" + ym.mm + "-15";
			m.setDocProperty(doc, SLVocab.SL_CREATION_DATE_PROPERTY, newCreationDate, null);
		}
	}
}

// j'ai cru que ca ne marchait pas, ben si, ça marche
@Test
public final void noBugWithDocInOldFolders() throws Exception {
	SLDocument doc = m.getDocumentIfExists(contextUrl + "/doc/2000/1999/plantu-annulation-dette.jpg");
	// il a une date de création, ailleurs
	String curCreationdate = getCreationDate(doc);
	System.out.println("Doc: " + doc + " creation: " + curCreationdate);
	
	SLModel.YYYYMM ym = docUri2YyyyMm(doc);
	if (ym != null) {
		System.out.println("File's yyyy-mm: " + ym.yyyy + "-" + ym.mm);
	} else {
		System.out.println("zut");
	}
	
	//List<SLKeyword> tags = doc.getKeywords();
	//for (SLKeyword tag : tags) {
	//	printCreationDate(tag);
	//	// oldestDoc(tag);
	//}
	
	m.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, "qu'est-ce qu'on dit?", null);

	System.out.println(doc.getComment());
	
	// on relaod, pour voir si ca a marché
	m = DataLoader.fpsSLModel();
	
	System.out.println("after reload: " + doc.getComment());
}

// quid des choses dans media ? Pas chargé dans fpsSLModel()
// http://127.0.0.1:8080/semanlink/doc/media/2000/lafemmedessables
@Test
public final void docInMedia() throws Exception {
	SLDocument doc = m.getDocumentIfExists(contextUrl + "/doc/media/2000/lafemmedessables");
	
	// n'existe pas !!!
	// Hum lié à fpsSLModel() qui ne charge pas tout
}

//
//
//

// cf JModel.numberOfDocs
// Pas économique : on créee ici tous les SLDocument

@Test
public final void listDocsWOCreationDate() throws Exception {
	// lister tous les docs, c chiant
	
	System.out.println("DOC WO CREATION DATE: ");
	Iterable<SLDocument> allDocs = allDocs(m);
	for(SLDocument doc : allDocs) {
		String cdate = this.getCreationDate(doc);
		if (cdate == null) {
			System.out.println(doc.getURI());
		}
	}
}

// YEN A 17 - LE FAIRE A LA MAIN
@Test
public final void listDocsWOCreationDate_NotEasilyChanged() throws Exception {
	System.out.println("DOC WO CREATION, DURS A CORRIGER: ");
	Iterable<SLDocument> allDocs = allDocs(m);
	for(SLDocument doc : allDocs) {
		String cdate = this.getCreationDateExtended(doc);
		if (cdate == null) {
			System.out.println(doc.getURI());
		}
	}
}

static Iterable<SLDocument> allDocs(SLModel m) {
	JModel jm = (JModel) m;
	Model docsModel = jm.getDocsModel(); // jena model
	ResIterator ite = docsModel.listSubjectsWithProperty(docsModel.createProperty(SLVocab.HAS_KEYWORD_PROPERTY));
	Iterable<SLDocument> it = new Iterable<SLDocument>() {

		@Override
		public Iterator<SLDocument> iterator() {
			return new Iterator<SLDocument>() {

				@Override
				public boolean hasNext() {
					return ite.hasNext();
				}

				@Override
				public SLDocument next() {
					Resource res = ite.next();
					return m.getDocument(res.getURI());
				}
				
			};
		}		
	};
	return it;
}

}
