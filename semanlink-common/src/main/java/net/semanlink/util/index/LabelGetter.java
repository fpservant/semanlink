package net.semanlink.util.index;

import java.util.Iterator;

/** To define how to get the labels of entities such as tags */
public interface LabelGetter<E> {
	public Iterator<String> getLabels(E o);
}
