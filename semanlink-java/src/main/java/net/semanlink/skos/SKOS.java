/* CVS $Id: SKOS.java,v 1.1 2010/06/25 16:15:23 fps Exp $ */
package net.semanlink.skos; 
import org.apache.jena.rdf.model.*;
 
/**
 * BEWARE THIS IS AN OLD VERSION OS SKOS // TODO
 * 
 * Vocabulary definitions from http://www.w3.org/2004/02/skos/core 
 * @author Auto-generated by schemagen on 19 janv. 2007 01:33 
 */
public class SKOS {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2004/02/skos/core#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Acronyms, abbreviations, spelling variants, and irregular plural/singular 
     *  forms may be included among the alternative labels for a concept. Mis-spelled 
     *  terms are normally included as hidden labels (see skos:hiddenLabel).</p>
     */
    public static final Property altLabel = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#altLabel" );
    
    public static final Property scopeNote = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#scopeNote" );
    
    /** <p>Narrower concepts are typically rendered as children in a concept hierarchy 
     *  (tree).</p>
     */
    public static final Property narrower = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#narrower" );
    
    /** <p>This property may be used directly, or as a super-property for more specific 
     *  note types.</p>
     */
    public static final Property note = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#note" );
    
    public static final Property isSubjectOf = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#isSubjectOf" );
    
    public static final Property altSymbol = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#altSymbol" );
    
    /** <p>Broader concepts are typically rendered as parents in a concept hierarchy 
     *  (tree).</p>
     */
    public static final Property broader = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#broader" );
    
    public static final Property definition = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#definition" );
    
    /** <p>This property allows subject indicators to be used for concept identification 
     *  in place of or in addition to directly assigned URIs.</p>
     */
    public static final Property subjectIndicator = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#subjectIndicator" );
    
    /** <p>The following rule may be applied for this property: [(?d skos:subject ?x)(?x 
     *  skos:broader ?y) implies (?d skos:subject ?y)]</p>
     */
    public static final Property subject = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#subject" );
    
    /** <p>A concept may be a member of more than one concept scheme.</p> */
    public static final Property inScheme = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#inScheme" );
    
    public static final Property historyNote = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#historyNote" );
    
    public static final Property hiddenLabel = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#hiddenLabel" );
    
    /** <p>No two concepts in the same concept scheme may have the same value for skos:prefSymbol.</p> */
    public static final Property prefSymbol = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#prefSymbol" );
    
    /** <p>A resource may have only one primary subject per concept scheme.</p> */
    public static final Property primarySubject = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#primarySubject" );
    
    public static final Property related = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#related" );
    
    public static final Property member = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#member" );
    
    /** <p>The following rule applies for this property: [(?c skos:memberList ?l) elementOfList(?e,?l) 
     *  implies (?c skos:member ?e)]</p>
     */
    public static final Property memberList = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#memberList" );
    
    public static final Property example = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#example" );
    
    /** <p>This property should not be used directly, but as a super-property for all 
     *  properties denoting a relationship of meaning between concepts.</p>
     */
    public static final Property semanticRelation = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#semanticRelation" );
    
    public static final Property changeNote = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#changeNote" );
    
    public static final Property hasTopConcept = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#hasTopConcept" );
    
    /** <p>This property is roughly analagous to rdfs:label, but for labelling resources 
     *  with images that have retrievable representations, rather than RDF literals.</p>
     */
    public static final Property symbol = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#symbol" );
    
    /** <p>No two concepts in the same concept scheme may have the same value for skos:prefLabel 
     *  in a given language.</p>
     */
    public static final Property prefLabel = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#prefLabel" );
    
    public static final Property editorialNote = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#editorialNote" );
    
    public static final Property isPrimarySubjectOf = m_model.createProperty( "http://www.w3.org/2004/02/skos/core#isPrimarySubjectOf" );
    
    /** <p>The following rule applies for this property: [(?x ?p ?c) (?c skos:member 
     *  ?y) (?p rdf:type skos:CollectableProperty) implies (?x ?p ?y)]</p>
     */
    public static final Resource CollectableProperty = m_model.createResource( "http://www.w3.org/2004/02/skos/core#CollectableProperty" );
    
    /** <p>Ordered collections can be used with collectable semantic relation properties, 
     *  where you would like a set of concepts to be displayed in a specific order, 
     *  and optionally under a 'node label'.</p>
     */
    public static final Resource OrderedCollection = m_model.createResource( "http://www.w3.org/2004/02/skos/core#OrderedCollection" );
    
    /** <p>Labelled collections can be used with collectable semantic relation properties 
     *  e.g. skos:narrower, where you would like a set of concepts to be displayed 
     *  under a 'node label' in the hierarchy.</p>
     */
    public static final Resource Collection = m_model.createResource( "http://www.w3.org/2004/02/skos/core#Collection" );
    
    public static final Resource Concept = m_model.createResource( "http://www.w3.org/2004/02/skos/core#Concept" );
    
    /** <p>Thesauri, classification schemes, subject heading lists, taxonomies, 'folksonomies', 
     *  and other types of controlled vocabulary are all examples of concept schemes. 
     *  Concept schemes are also embedded in glossaries and terminologies.A concept 
     *  scheme may be defined to include concepts from different sources.</p>
     */
    public static final Resource ConceptScheme = m_model.createResource( "http://www.w3.org/2004/02/skos/core#ConceptScheme" );
    
}
