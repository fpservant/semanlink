package net.semanlink.util.index;

import java.util.Iterator;

public interface MultiLabelGetter<E> {
	public Iterator<String> getLabels(E o);
}
