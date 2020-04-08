package net.semanlink.util.index;

import java.util.Iterator;

public interface LabelGetter<E> {
	public Iterator<String> getLabels(E o);
}
