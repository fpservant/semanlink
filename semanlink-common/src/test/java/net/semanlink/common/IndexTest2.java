/* Created on 13 déc. 2013 */
package net.semanlink.common;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.jena.ModelIndexedByLabel2;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class IndexTest2 {

private static Model m;
private static String NS = "http://wwww.semanlink.net/tag/";
private static Resource tag1,tag2,tag3,tag4,tag5;
private static ModelIndexedByLabel2 index;

@BeforeClass
public static void setUpBeforeClass() throws Exception {
	m = ModelFactory.createDefaultModel();
	tag1 = newTag("semanlink", "Semanlink");
	tag2 = newTag("semantique", "Sémantique");
	tag3 = newTag("semantic_web", "Semantic Web");
	tag4 = newTag("semantic_trip", "Semantic trip");
	tag5 = newTag("web_service", "Web Service");
	
	m.add(tag2,RDFS.label,"semantics","en");
	
	index = new ModelIndexedByLabel2(m.listSubjects(), m, Locale.FRANCE, true); // true to index labels in any lang
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
	m = null;
	index = null;
}

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

@Test
public void exactMatch() {	
	List<ObjectLabelPair<Resource>> l;
	l = index.label2KeywordList("semantique", Locale.FRANCE);
	assertTrue(l.size() == 1);
	assertTrue(l.get(0).getObject().equals(tag2));
			
	l = index.label2KeywordList("semantics", Locale.US);
	assertTrue(l.size() == 1);
	assertTrue(l.get(0).getObject().equals(tag2));
			
	l = index.label2KeywordList("web semantic", Locale.FRANCE);
	assertTrue(l.size() == 1);
	assertTrue(l.get(0).getObject().equals(tag3));

	l = index.label2KeywordList("seman", Locale.FRANCE);
	assertTrue(l.size() == 0);
}

@Test
public void partialSearch() {
	Set<ObjectLabelPair<Resource>> x;
	Set<Resource> xr;
	
	// tag2 is returned in 2 different ObjectLabelPairs when searching for, say, "semant"
	
	x = index.searchText("SEMant");
	assertTrue(x.size() == 4);
	xr = new HashSet<Resource>();
	for (ObjectLabelPair<Resource> olp : x) {
		xr.add(olp.getObject());
	}
	assertTrue(xr.size() == 3);

	x = index.searchText("semantic");
	assertTrue(x.size() == 3);
	xr = new HashSet<Resource>();
	for (ObjectLabelPair<Resource> olp : x) {
		xr.add(olp.getObject());
	}
	assertTrue(xr.size() == 3);
}

private static Resource newTag(String localName, String label) {
	Resource tag = m.createResource(NS + localName);
	m.add(tag,RDFS.label,label,"fr");
	return tag;
}

}
