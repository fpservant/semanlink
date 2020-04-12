/* Created on dec 2012 */
package net.semanlink.util.index;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/** Represents a pair (object, label). 
 *  The label is just a String: doesn't contain lang information. */
public class ObjectLabelPair<E> {
protected final E res;
protected final String label;

public ObjectLabelPair(E res, String label) {
	this.res = res;
	this.label = label;
}

public boolean equals(Object o) {
	if (!(o instanceof ObjectLabelPair<?>)) return false;
	ObjectLabelPair<?> opair = (ObjectLabelPair<?>) o;
	return res.equals(opair.res) && label.equals(opair.label); // TODO to see: shoudn't it be modulo collator???
}

public int hashCode() { 
	// return res.hashCode(); // 2020-04
	return Objects.hash(res, label);
}

public E getObject() { return this.res; }
public String getLabel() { return this.label; }

/** Allows to compare ObjectLabelPair on the text of their labels. */
public static class CollatorBasedComparator implements Comparator<ObjectLabelPair<?>> {
	private Collator collator;
	public CollatorBasedComparator(String lang) {
		collator = Collator.getInstance(new Locale(lang));
		// collator.setStrength(Collator.PRIMARY);
	}
	public int compare(ObjectLabelPair<?> p0, ObjectLabelPair<?> p1) {
		return collator.getCollationKey(p0.getLabel()).compareTo(collator.getCollationKey(p1.getLabel()));
	}
}
}
