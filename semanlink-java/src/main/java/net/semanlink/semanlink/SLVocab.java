package net.semanlink.semanlink;

import net.semanlink.skos.SKOS;

import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.DC;

/**
 * Definition of Semanlink RDF vocabulary
 */
public interface SLVocab {
public static final String SEMANLINK_SCHEMA = SLSchema.NS;
/** Object in a statement with predicate rdf:type, states that the subject is a Keyword. */
public static final String KEYWORD_CLASS = SLSchema.Tag.getURI();
// @find SKOSIFY 
// public static final String PREF_LABEL_PROPERTY = RDFS.label.getURI(); // SLSchema.prefLabel.getURI();
public static final String PREF_LABEL_PROPERTY = SKOS.prefLabel.getURI(); // SLSchema.prefLabel.getURI();

public static final String HAS_KEYWORD_PROPERTY = SLSchema.tag.getURI();
// @find SKOSIFY
// public static final String HAS_PARENT_PROPERTY = SLSchema.hasParent.getURI();
public static final String HAS_PARENT_PROPERTY = SKOS.broader.getURI();

//@find SKOSIFY
// public static final String HAS_FRIEND_PROPERTY = SLSchema.related.getURI();
public static final String HAS_FRIEND_PROPERTY = SKOS.related.getURI();

public static final String HAS_ALIAS_PROPERTY = SLSchema.hasAlias.getURI(); // du kw principal vers ses syno
public static final String COMMENT_PROPERTY = SLSchema.comment.getURI();
public static final String SL_CREATION_DATE_PROPERTY =  SLSchema.creationDate.getURI();
public static final String SL_CREATION_TIME_PROPERTY =  SLSchema.creationTime.getURI();
public static final String SL_REPLACED_BY_PROPERTY =  SLSchema.replacedBy.getURI();
// public static final String SL_MARKDOWN_PROPERTY =  SLSchema.markdown.getURI();
public static final String SL_MARKDOWN_OF_PROPERTY =  SLSchema.markdownOf.getURI();

public static final String SOURCE_PROPERTY = DC.source.getURI();
public static final String DATE_PARUTION_PROPERTY = DC.date.getURI();
public static final String CREATOR_PROPERTY = DC.creator.getURI();
public static final String TITLE_PROPERTY = DC.title.getURI();

public static final String SL_HOME_PAGE_PROPERTY = "http://xmlns.com/foaf/0.1/homepage";

public static final String SL_DESCRIBED_BY_PROPERTY =  SLSchema.describedBy.getURI();

public static final String DATE_DELIM = "-";

public static final String ACTIV_FOLDER = "activ_folder";


// OLDIES

/** voir MyRDFWriter. */
static String[] xmlEntitiesNames = {"sl","kw"};
/** voir MyRDFWriter. */
static String[] xmlEntitiesValues = {SEMANLINK_SCHEMA};

public static EasyProperty[] COMMON_PROPERTIES = {
	new EasyProperty("sl:comment", COMMENT_PROPERTY)
	,new EasyProperty("dc:title", DC.title.getURI())
	,new EasyProperty("dc:subject", DC.subject.getURI())
	,new EasyProperty("dc:creator", CREATOR_PROPERTY)
	,new EasyProperty("dc:source", SOURCE_PROPERTY)
	,new EasyProperty("dc:date", DATE_PARUTION_PROPERTY)
	,new EasyProperty("sl:creationDate", SL_CREATION_DATE_PROPERTY)
	,new EasyProperty("sl:creationTime", SL_CREATION_TIME_PROPERTY)
	,new EasyProperty("rdfs:seeAlso" , RDFS.seeAlso.getURI())
	,new EasyProperty("sl:replacedBy" , SL_REPLACED_BY_PROPERTY)
	,new EasyProperty("sl:markdown" , SL_MARKDOWN_OF_PROPERTY)
};

/** Associe un "petit nom", genre "dc:title" a une uri. */
public class EasyProperty implements Comparable {
	private String name;
	private String uri;
	public EasyProperty(String name, String uri) {
		this.name = name;
		this.uri = uri;
	}
	public String getName() { return this.name; }
	public String getUri() { return this.uri; }
	public int compareTo(Object arg0) {
		return uri.compareTo(((EasyProperty) arg0).uri);
	}
}

//
//
//


}
