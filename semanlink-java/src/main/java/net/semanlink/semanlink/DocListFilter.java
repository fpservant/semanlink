/* Created on 13 oct. 2005 */
package net.semanlink.semanlink;

import java.util.ArrayList;
import java.util.List;

/**
 * Permet de filtrer une liste de documents
 */
public abstract class DocListFilter {
public abstract boolean keepIt(SLDocument doc);
/** Modifie list. */
public void filter(List list) {
	for (int i = list.size() - 1; i > -1; i--) {
		if (!keepIt((SLDocument) list.get(i))) list.remove(i);
	}
}
/** Retourne une nouvelle liste purgée. */
public List filteredList(List list) {
	int n = list.size();
	ArrayList x = new ArrayList(n);
	for (int i = 0 ; i < n ; i++) {
		SLDocument doc = (SLDocument) list.get(i);
		if (keepIt(doc)) x.add(doc);
	}
	return x;
}
}
