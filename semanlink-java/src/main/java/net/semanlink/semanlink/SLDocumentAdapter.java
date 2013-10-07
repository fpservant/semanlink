/* Created on 20 sept. 03 */
package net.semanlink.semanlink;
import java.util.*;

/**
 * Met en cache la liste des keywords
 */
public abstract class SLDocumentAdapter extends SLResourceAdapter implements SLDocument {
List keywords;
// CONSTRUCTION
public SLDocumentAdapter(String uri) { super(uri); }

// IMPLEMENTS SLDocument
/** Attention! ne retourne pas une copie */
public List getKeywords() {
	if (this.keywords == null) this.keywords = computeKeywords();
	return this.keywords;
}

public abstract List computeKeywords();
} // class