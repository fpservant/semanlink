package net.semanlink.sljena.modelcorrections;

import net.semanlink.semanlink.SLVocab;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.DC;


/**
 * To transform a SLModel using old SLVocab ("SLKeyword", "hasKeyword", "hasParent", "slCreationDate"...)
 * to new one ("Tag", "tag", "hasParent", "creationDate")
 * To use, creates an instance, then getModelCorrector, and use this corrector
 */


//@find changing uris

// A VOIR :
// les xml/lang
// label

// public URICorrection(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {

public class OldSLVocab2NewSLVocabTransformer {
private static String SL = SLVocab.SEMANLINK_SCHEMA;
//
// Here are the terms as they were in the old vocab
//
public static final String OLD_KEYWORD_CLASS = SL + "Keyword"; // doit être inutile si on reconnaît un kw au simple fait qu'il est ds kwsModel
public static final String OLD_HAS_KEYWORD_PROPERTY = SL + "hasKeyword";
public static final String OLD_HAS_PARENT_PROPERTY = SL + "hasParent";
public static final String OLD_HAS_FRIEND_PROPERTY = SL + "hasFriend";
public static final String OLD_HAS_ALIAS_PROPERTY = SL + "hasAlias"; // du kw principal vers ses syno
public static final String OLD_COMMENT_PROPERTY = SL + "comment";
public static final String OLD_SL_CREATION_DATE_PROPERTY = SL + "slCreationDate";
public static final String OLD_SL_REPLACED_BY_PROPERTY = SL + "slReplacedBy";

public static final String OLD_SOURCE_PROPERTY = DC.source.getURI();
public static final String OLD_DATE_PARUTION_PROPERTY = DC.date.getURI();
public static final String OLD_CREATOR_PROPERTY = DC.creator.getURI();
public static final String OLD_TITLE_PROPERTY = DC.title.getURI();
//
//
//

//
//
//
private ModelCorrector corrector;
public OldSLVocab2NewSLVocabTransformer(ModelCorrector corrector) {
	this.corrector = corrector;
	add(OLD_KEYWORD_CLASS, SLVocab.KEYWORD_CLASS, true, false);
	add(OLD_HAS_KEYWORD_PROPERTY, SLVocab.HAS_KEYWORD_PROPERTY, false, true);
	add(OLD_HAS_PARENT_PROPERTY, SLVocab.HAS_PARENT_PROPERTY, true, false);
	add(OLD_HAS_FRIEND_PROPERTY, SLVocab.HAS_FRIEND_PROPERTY, true, false);
	add(OLD_HAS_ALIAS_PROPERTY, SLVocab.HAS_ALIAS_PROPERTY, true, false);
	add(OLD_COMMENT_PROPERTY, SLVocab.COMMENT_PROPERTY, true, true);
	add(OLD_SL_CREATION_DATE_PROPERTY, SLVocab.SL_CREATION_DATE_PROPERTY, true, true);
	add(OLD_SL_REPLACED_BY_PROPERTY, SLVocab.SL_REPLACED_BY_PROPERTY, true, true);
	
	add(RDFS.label.getURI(), DC.title.getURI(), false, true);
}

private void add(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {
	if (oldUri.equals(newUri)) return;
	this.corrector.add(new URICorrection(oldUri, newUri, inKwsModel, inDocsModel));
}

public ModelCorrector getModelCorrector() { return this.corrector; }
}

