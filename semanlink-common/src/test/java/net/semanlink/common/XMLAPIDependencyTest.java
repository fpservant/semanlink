package net.semanlink.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** to check that we don't get a java.lang.ClassNotFoundException: org.w3c.dom.ElementTraversal
 * that may happen depending on conflict resolution about xml-apis between xom and jena.
 * Has been solved by an exclusion in the dependecy to xom (semanlinkcommons.pom)
 */
public class XMLAPIDependencyTest {

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

@Test
public final void testInitSchema() {
	String s = ASchema.getURI();
	assertEquals(s, "http://www.semanlink.net/2001/00/semanlink-schema#");
	// fail("Not yet implemented"); // TODO
}

public static class ASchema {
  /** <p>The RDF model that holds the vocabulary terms</p> */
  private static Model m_model = ModelFactory.createDefaultModel();
  
  /** <p>The namespace of the vocabulary as a string</p> */
  public static final String NS = "http://www.semanlink.net/2001/00/semanlink-schema#";
  
  /** <p>The namespace of the vocabulary as a string</p>
   *  @see #NS */
  public static String getURI() {return NS;}
  
  /** <p>The namespace of the vocabulary as a resource</p> */
  public static final Resource NAMESPACE = m_model.createResource( NS );
  
  public static final Property comment = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#comment" );
  
  public static final Property creationDate = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#creationDate" );
  
  /** <p></p> */
  public static final Property creationTime = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#creationTime" );
  
  /** <p>Used to state that the subject is the Non-Information Resource (thing or concept) 
   *  described by the object.Utilisée pour indiquer que le sujet est la chose ou 
   *  le concept décrit par l'objet.</p>
   */
  public static final Property describedBy = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#describedBy" );
  
  /** <p>The tag object of this statement is an alias of tag subject. Subject is used 
   *  instead of alias when adding alias as object of a property such as hasParent 
   *  or hasTag</p>
   */
  public static final Property hasAlias = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#hasAlias" );
  
  // @find SKOSIFY
  // public static final Property hasParent = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#hasParent" );
  
  public static final Property prefLabel = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#prefLabel" );
  
  // @find SKOSIFY
  // public static final Property related = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#related" );
  
  /** <p>Indique que la ressource sujet est remplacée par une autreStates that a resource 
   *  is replaced by another one.</p>
   */
  public static final Property replacedBy = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#replacedBy" );
  
  public static final Property tag = m_model.createProperty( "http://www.semanlink.net/2001/00/semanlink-schema#tag" );
  
  public static final Resource Tag = m_model.createResource( "http://www.semanlink.net/2001/00/semanlink-schema#Tag" );
  
  public static final Resource Thesaurus = m_model.createResource( "http://www.semanlink.net/2001/00/semanlink-schema#Thesaurus" );
  
}


}
