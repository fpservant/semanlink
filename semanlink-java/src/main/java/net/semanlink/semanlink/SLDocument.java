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
}