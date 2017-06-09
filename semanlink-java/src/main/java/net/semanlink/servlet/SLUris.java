package net.semanlink.servlet;

import org.apache.jena.rdf.model.*;

import net.semanlink.semanlink.SLVocab;
import net.semanlink.skos.SKOS;

/** les urls du vocab sl à la mode sl (et non skos). */
class SLUris implements SLVocab {
	private static SLUris self = new SLUris();
	public static SLUris getInstance() { return self; }

	public String getKeywordClassURI() { return KEYWORD_CLASS; }
	public String getHasKeywordPropertyURI() { return HAS_KEYWORD_PROPERTY; }
	public String getHasParentPropertyURI() { return HAS_PARENT_PROPERTY; }
	public String getHasFriendPropertyURI() { return HAS_FRIEND_PROPERTY; }
	public String getHasAliasPropertyURI() { return HAS_ALIAS_PROPERTY; }
	public String getKeywordPrefLabelURI() { return PREF_LABEL_PROPERTY; }
	public String getCommentPropertyURI() { return COMMENT_PROPERTY; }
	public String getCreationDatePropertyURI() { return SL_CREATION_DATE_PROPERTY; }
	public String getCreationTimePropertyURI() { return SL_CREATION_TIME_PROPERTY; }
	public String getReplacedByPropertyURI() { return SL_REPLACED_BY_PROPERTY; }
	
	//
	//
	//
	
	public Resource getKeywordClass(Model mod) { return mod.createResource(getKeywordClassURI()); }
	public Property getHasKeywordProperty(Model mod) { return mod.createProperty(getHasKeywordPropertyURI()); }
	public Property getHasParentProperty(Model mod) { return mod.createProperty(getHasParentPropertyURI()); }
	public Property getHasFriendProperty(Model mod) { return mod.createProperty(getHasFriendPropertyURI()); }
	public Property getHasAliasProperty(Model mod) { return mod.createProperty(getHasAliasPropertyURI()); }
	public Property getKeywordPrefLabel(Model mod) { return mod.createProperty(getKeywordPrefLabelURI()); }
	public Property getCommentProperty(Model mod) { return mod.createProperty(getCommentPropertyURI()); }
	public Property getCreationDateProperty(Model mod) { return mod.createProperty(this.getCreationDatePropertyURI()); }
	public Property getCreationTimeProperty(Model mod) { return mod.createProperty(getCreationTimePropertyURI()); }
	public Property getReplacedByProperty(Model mod) { return mod.createProperty(getReplacedByPropertyURI()); }
}

