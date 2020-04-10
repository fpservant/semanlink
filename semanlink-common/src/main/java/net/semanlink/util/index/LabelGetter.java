package net.semanlink.util.index;

import java.util.Iterator;

/** To define how we get the labels of entities such as the tags in a thesaurus */
public interface LabelGetter<E> {
	public Iterator<String> getLabels(E o);
}
