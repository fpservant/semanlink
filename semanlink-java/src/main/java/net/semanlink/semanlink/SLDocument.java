package net.semanlink.semanlink;
import java.util.*;
/**
 * Represente un document pour Semanlink.
 * Rien de plus en fait qu'un ressource au sens RDF.
 * Ne fait pas explicitement reference a un model, mais les methodes retournant
 * des proprietes dependent du model vis a vis duquel on le considere.
 * @author fps
 */
public interface SLDocument extends SLLabeledResource, SLVocab, Comparable {
/** Retourne une List de SLKeyword. */
public List getKeywords();

// 2019-03 uris for bookmarks -- quick'n dirty
/**
 * @return null if not a bookmark, the url this res is bookmark of otherwise
 */
default String bookmarkOf() {
	PropertyValues pv = getProperty(SLSchema.bookmarkOf.getURI());
	if (pv == null) return null;
	return pv.getFirstAsString();
}
}