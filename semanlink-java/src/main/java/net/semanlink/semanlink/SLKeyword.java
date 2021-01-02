package net.semanlink.semanlink;

import java.util.List;

/**
 * A tag. 
 * 
 * (I was using the word "keyword" instead of "tag" when this was written)
 * 
 * Represente un keyword pour Semanlink.
 * Ne fait pas explicitement reference a un model, mais les methodes retournant
 * des proprietes dependent du model vis a vis duquel on le considere.
 * @author fps
 */
public interface SLKeyword extends SLLabeledResource, SLVocab, Comparable {
	List<SLKeyword> getParents();
	List<SLKeyword> getChildren();
	List<SLKeyword> getFriends();
	List<SLDocument> getDocuments();
	// used by livetree
	boolean hasChild();
	boolean hasDocument();
	List<LabelLN> getAltLabels(); //2021-01
//	default List<LabelLN> getAltLabels() {
//		throw new UnsupportedOperationException();
//	}
}
