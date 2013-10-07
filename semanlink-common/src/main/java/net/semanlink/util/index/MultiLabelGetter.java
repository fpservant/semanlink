package net.semanlink.util.index;

import java.util.Iterator;

public interface MultiLabelGetter<ITEM> {
	public Iterator<String> getLabels(ITEM o);
}
