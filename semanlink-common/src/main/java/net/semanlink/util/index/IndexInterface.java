/* Created on 29 déc. 2010 */
package net.semanlink.util.index;

import java.util.Collection;

/** Items (typically tags) indexed by words */
public interface IndexInterface<E> {
	/** words in searchString to entities */
	Collection<E> searchText(String searchString);
}
