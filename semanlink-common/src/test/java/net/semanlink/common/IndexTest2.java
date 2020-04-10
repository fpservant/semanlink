/* Created on 13 déc. 2013 */
package net.semanlink.common;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.semanlink.util.index.WordIndex;
import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.jena.ModelIndexedByLabel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

public class IndexTest2 {

private static Model m;
private static String NS = "http://wwww.semanlink.net/tag/";
private static Resource tag1,tag2,tag3,tag4,tag5;
private static ModelIndexedByLabel index;

@BeforeClass
public static void setUpBeforeClass() throws Exception {
	init();
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

private static void init() throws Exception {
	m = ModelFactory.createDefaultModel();
	tag1 = newTag("semanlink", "Semanlink");
	tag2 = newTag("semantique", "Sémantique");
	tag3 = newTag("semantic_web", "Semantic Web");
	tag4 = newTag("semantic_trip", "Semantic trip");
	tag5 = newTag("web_service", "Web Service");
	
	m.add(tag2,RDFS.label,"semantics","en");
	
	index = new ModelIndexedByLabel(m.listSubjects(), m, Locale.FRANCE, true); // true to index labels in any lang
}

private static Resource newTag(String localName, String label) {
	Resource tag = m.createResource(NS + localName);
	m.add(tag,RDFS.label,label,"fr");
	return tag;
}

private static ObjectLabelPair<Resource> newOLP(String localName, String label) {
	Resource tag = newTag(localName, label);
	return new ObjectLabelPair<Resource>(tag, label);
}

@Test
public void wordsAreSortedTest() throws Exception {
	wordsAreSortedTest(true);
	wordsAreSortedTest(false);	
}

private void wordsAreSortedTest(boolean initing) throws Exception {
	checkWordsAreSorted();
	// add tags and check words are still sorted
	Resource tag = newTag("SEMANLINK2", "SEMANLINK2");
	Resource tag2 = newTag("aaa", "aaa");
	Resource tag3 = newTag("zzzbbb", "zzz bbb");
	ArrayList<Resource> moreTags = new ArrayList<>();
	moreTags.add(tag);
	moreTags.add(tag2);
	moreTags.add(tag3);

	try (WordIndex.Update<Resource> up = index.newUpdate(initing)) {
		up.addIterator(moreTags.iterator());
	}
	checkWordsAreSorted();
	
//	String[] words = index.getWords();
//	for (String w : words) {
//		System.out.println(w);
//	}
	
	// reset for other tests
	init();
}

private void checkWordsAreSorted() {
	String[] words = index.getWords();
	// check words is sorted
	String[] words2 = new String[words.length];
	for (int i = 0 ; i < words.length ; i++) {
		words2[i] = words[i];
	}
	Arrays.sort(words2);
	for (int i = 0 ; i < words.length ; i++) {
		assertTrue(words2[i] == words[i]);
	}
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
	
	x = index.string2entities("SEMant");
	assertTrue(x.size() == 4);
	xr = new HashSet<Resource>();
	for (ObjectLabelPair<Resource> olp : x) {
		xr.add(olp.getObject());
	}
	assertTrue(xr.size() == 3);

	x = index.string2entities("semantic");
	assertTrue(x.size() == 3);
	xr = new HashSet<Resource>();
	for (ObjectLabelPair<Resource> olp : x) {
		xr.add(olp.getObject());
	}
	assertTrue(xr.size() == 3);
}

}
