/* Created on 29 déc. 2010 */
package net.semanlink.util.index;

import java.util.Collection;

public interface IndexInterface<ITEM> {
	Collection<ITEM> searchText(String searchString);
}
