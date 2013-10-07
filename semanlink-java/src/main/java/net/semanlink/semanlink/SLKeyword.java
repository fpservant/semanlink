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
	public List getParents();
	public List getChildren();
	public List getFriends();
	public List getDocuments();
	// used by livetree
	public boolean hasChild();
	public boolean hasDocument();
	//
	// public String getHomePageURI();
}
