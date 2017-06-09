/* Created on 16 déc. 2013 */
package net.semanlink.common;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.Stack;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.Intersection;
import net.semanlink.graph.jena.JenaModelAsGraph;
import net.semanlink.graph.jena.JenaModelAsSimpleGraph;
import net.semanlink.graph.jena.TreeDefinition;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class GraphTraversalTest {

private static Model m;
private static String NS = "http://wwww.semanlink.net/tag/";
private static Resource niger,music,music_of_niger,sofakolle,moussa_poussi,mali,africa,ali_farka_toure,caetano_veloso;
private static Property narrower;

@BeforeClass
public static void setUpBeforeClass() throws Exception {
	m = ModelFactory.createDefaultModel();
	narrower = m.createProperty("http://skos/narrower");
	niger = newTag("niger");
	music = newTag("music");
	music_of_niger = newTag("music_of_niger");
	sofakolle = newTag("sofakolle");
	moussa_poussi = newTag("moussa_poussi");
	mali = newTag("mali");
	africa = newTag("africa");
	ali_farka_toure = newTag("ali_farka_toure");
	caetano_veloso = newTag("caetano_veloso");
	
	link(africa, niger);
	link(africa, mali);
	link(niger, music_of_niger);
	link(music, music_of_niger);
	link(music_of_niger, sofakolle);
	link(music_of_niger, moussa_poussi);
	link(mali, ali_farka_toure);
	link(music, ali_farka_toure);
	link(music, caetano_veloso);
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

@Test
public void printSimple() throws Exception {
  Stack<RDFNode> stack = new Stack<RDFNode>();
  Stack<Integer> treePosition = new Stack<Integer>();
  Resource[] seeds = new Resource[2] ; seeds[0] = niger ; seeds[1] = music ;
  JenaModelAsSimpleGraph graph = new JenaModelAsSimpleGraph(m, seeds, narrower);
  GraphTraversal.SimplePrinter<RDFNode> printer = new GraphTraversal.SimplePrinter<RDFNode>(stack, treePosition);
  GraphTraversal<RDFNode> traversal = new GraphTraversal<RDFNode>(graph);
  System.out.println("Printing graph made of 2 seeds: niger and music");
  traversal.depthFirstWalk(printer, stack, treePosition);
  Set<RDFNode> nodes = traversal.getNodes();
  assertTrue(nodes.contains(niger));
  assertTrue(nodes.contains(moussa_poussi));
  assertFalse(nodes.contains(mali));
  assertTrue(nodes.contains(ali_farka_toure));
}

@Test
public void print() throws Exception {
  Stack<Resource> stack = new Stack<Resource>();
  Stack<Integer> treePosition = new Stack<Integer>();
  String[] schildProps = new String[1] ; schildProps[0] = narrower.getURI();
  String[] sparentProps = new String[0] ;
  TreeDefinition treeDef = new TreeDefinition(m, schildProps, sparentProps, null, null);
  JenaModelAsGraph graph = new JenaModelAsGraph(africa, treeDef);
  GraphTraversal.SimplePrinter<Resource> printer = new GraphTraversal.SimplePrinter<Resource>(stack, treePosition);
  GraphTraversal<Resource> traversal = new GraphTraversal<Resource>(graph);
  System.out.println("Printing 'africa'");
  traversal.depthFirstWalk(printer, stack, treePosition);
  Set<Resource> nodes = traversal.getNodes();
  assertTrue(nodes.contains(niger));
  assertTrue(nodes.contains(moussa_poussi));
  assertTrue(nodes.contains(mali));
  assertTrue(nodes.contains(ali_farka_toure));
}

@Test
public void simpleGetNodes() throws Exception {
  Resource[] seeds = new Resource[2] ; seeds[0] = africa ; seeds[1] = music ;
  JenaModelAsSimpleGraph graph = new JenaModelAsSimpleGraph(m, seeds, narrower);
  GraphTraversal<RDFNode> traversal = new GraphTraversal<RDFNode>(graph);
  Set<RDFNode> nodes = traversal.getNodes();
  assertTrue(nodes.contains(niger));
  assertTrue(nodes.contains(moussa_poussi));
  assertTrue(nodes.contains(mali));
  assertTrue(nodes.contains(ali_farka_toure));
}

@Test
public void intersection() throws Exception {
  Resource[] seeds = new Resource[1] ; seeds[0] = africa;
  JenaModelAsSimpleGraph g1 = new JenaModelAsSimpleGraph(m, seeds, narrower);
  seeds = new Resource[1] ; seeds[0] = music;
  JenaModelAsSimpleGraph g2 = new JenaModelAsSimpleGraph(m, seeds, narrower);
	Intersection<RDFNode> inter = new Intersection<RDFNode>(g1, g2);	
	fastIntersection(inter);
	fullIntersection(inter);
}

private void fullIntersection(Intersection<RDFNode> inter) throws Exception {
	Set<RDFNode> x = inter.getNodes(false);
	assertTrue(x.contains(music_of_niger));
	assertTrue(x.contains(moussa_poussi));
	assertTrue(x.contains(sofakolle));
	assertTrue(x.contains(ali_farka_toure));
	assertFalse(x.contains(caetano_veloso));
}

private void fastIntersection(Intersection<RDFNode> inter) throws Exception {
	// "fast" intersection: doesn't include the children of its elements
	Set<RDFNode> x = inter.getNodes(true);
	assertTrue(x.contains(music_of_niger));
	assertFalse(x.contains(moussa_poussi));
	assertFalse(x.contains(sofakolle));
	assertTrue(x.contains(ali_farka_toure));
	assertFalse(x.contains(caetano_veloso));
}

//
//
//

private static Resource newTag(String localName) {
	Resource tag = m.createResource(NS + localName);
	return tag;
}

private static void link(Resource parent, Resource child) {
	m.add(parent, narrower, child);
}
}
