/* Created on 12 juil. 2010 */
package net.semanlink.util.index.jena;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Resource;

/** Represents a pair (resource, label). 
 *  The label is just a String: doesn't contain lang information. */
public class ResourceLabelPair_SVG { // implements Comparable<ResourceLabelPair> {
protected Resource res;
protected String label;

public ResourceLabelPair_SVG(Resource res, String label) {
	this.res = res;
	this.label = label;
}
public boolean equals(Object o) {
	if (!(o instanceof ResourceLabelPair_SVG)) return false;
	ResourceLabelPair_SVG opair = (ResourceLabelPair_SVG) o;
	return res.equals(opair.res) && label.equals(opair.label);
}
public int hashCode() {
	return res.hashCode();
}
public Resource getResource() { return this.res; }
public String getLabel() { return this.label; }

/*public int compareTo(ResourceLabelPair p) {
	int x = getLabel().compareTo(p.getLabel());
	if (x != 0) return x;
	return getURI().compareTo(p.getURI());
}*/

/** Allows to compare UriLabelPairs on the text of their labels. */
public static class CollatorBasedComparator implements Comparator<ResourceLabelPair_SVG> {
	private Collator collator;
	public CollatorBasedComparator(String lang) {
		collator = Collator.getInstance(new Locale(lang));
		// collator.setStrength(Collator.PRIMARY);
	}
	public int compare(ResourceLabelPair_SVG p0, ResourceLabelPair_SVG p1) {
		return collator.getCollationKey(p0.getLabel()).compareTo(collator.getCollationKey(p1.getLabel()));
	}
}
/*public static MultiLabelGetter<ResourceLabelPair> getMultiLabelGetterInstance() {
	
}*/
}
