/* Created on 29 déc. 2010 */
package net.semanlink.util.index;

import java.util.Collection;

/** Items (typically tags) indexed by words */
public interface WordIndexInterface<E> {
	/**
	 * words in a string to entities - typically searching the entities matching a given label. 
	 * (used for instance in the live-search of tags) */
	Collection<E> string2entities(String searchString);
}
