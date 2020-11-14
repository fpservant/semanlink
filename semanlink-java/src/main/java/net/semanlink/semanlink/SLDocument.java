package net.semanlink.semanlink;
import java.util.List;
/**
 * Represente un document pour Semanlink.
 * Rien de plus en fait qu'un ressource au sens RDF.
 * Ne fait pas explicitement reference a un model, mais les methodes retournant
 * des proprietes dependent du model vis a vis duquel on le considere.
 * @author fps
 */
public interface SLDocument extends SLLabeledResource, SLVocab, Comparable {
/** Retourne une List de SLKeyword. */
public List<SLKeyword> getKeywords();


/**
 * @return null if not a bookmark, the url this res is bookmark of otherwise
 * @since 0.6
 */
default String bookmarkOf() { // 2019-03 uris for bookmarks -- quick'n dirty
	PropertyValues pv = getProperty(SLSchema.bookmarkOf.getURI());
	if (pv == null) return null;
	return pv.getFirstAsString();
}

public List<SLDocument> mainDocOf();

public List<SLDocument> relatedDocs(); // 2020-11
}