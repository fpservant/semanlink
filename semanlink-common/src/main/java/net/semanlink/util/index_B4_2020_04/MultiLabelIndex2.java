/* Created on dec 2012, based on ModelIndexedByLabel2 (mars 2010) */ // SKOSIFY
package net.semanlink.util.index_B4_2020_04;

import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Indexing (object, label) pairs.
 * <p>When compared to LabelIndex (which indexes objects, on several labels),
 * this has the advantage of allowing to return the found label.
 * @author fps
 */
public class MultiLabelIndex2<E> extends MultiLabelIndex<ObjectLabelPair<E>> {
// BEWARE, this.labelGetter is a label getter<ObjectLabelPair<E>>
// NOT a label getter<E>
protected Collator collator;

public MultiLabelIndex2(Iterator<E> resToBeIndexedByLabel, LabelGetter<E> multiLabelGetter, Locale locale) {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), locale);
}

public MultiLabelIndex2(Iterator<E> resToBeIndexedByLabel, LabelGetter<E> multiLabelGetter, IndexEntriesCalculator iec, Locale locale) {
	this(iec, locale);
	addResIterator(resToBeIndexedByLabel, multiLabelGetter);
}

/** addIterator or addCollection must be called after that */
protected MultiLabelIndex2(IndexEntriesCalculator iec, Locale locale) {
	super();
	this.locale = locale;
	this.collator = Collator.getInstance(this.locale);
	collator.setStrength(Collator.PRIMARY);
	// BEWARE, this.labelGetter is a label getter<ObjectLabelPair<E>>
	// NOT a label getter<E>
	this.labelGetter = new LabelGetter<ObjectLabelPair<E>>() {
		public Iterator<String> getLabels(ObjectLabelPair<E> o) { return Collections.singleton(o.getLabel()).iterator(); }
	};
	init(labelGetter, iec, locale) ;
}

public void addResIterator(Iterator<E> resToBeIndexedByLabel, LabelGetter<E> multiLabelGetter) {
	HashSet<ObjectLabelPair<E>> hs = new HashSet<ObjectLabelPair<E>>(); // avoid to have a res indexed by "truc" and "Truc"
	for (;resToBeIndexedByLabel.hasNext();) {
		E res = resToBeIndexedByLabel.next();
		Iterator<String> labels = multiLabelGetter.getLabels(res);
		
		if (labels.hasNext()) {
			// avoid adding duplicate labels.
			// We always add the first one. If there are more, we check for duplicates
			String label = labels.next();
			ObjectLabelPair<E> pair = new ObjectLabelPair<E>(res, label);
			hs.add(pair);
			if (labels.hasNext()) {
				ArrayList<CollationKey> cks = new ArrayList<CollationKey>(32);
				cks.add(collator.getCollationKey(label));
				for (; labels.hasNext(); ) {
					label = labels.next();
					// label already added?
					CollationKey labelCK = collator.getCollationKey(label);
					boolean alreadyIn = false;
					for (CollationKey addedCK : cks) {
						if (labelCK.compareTo(addedCK)  == 0) {
							alreadyIn = true;
							break;
						}
					}
					if (!alreadyIn) {
						pair = new ObjectLabelPair<E>(res, label);
						hs.add(pair);
						cks.add(labelCK);
					}
				}
			}
		}
	}
	
	addIterator(hs.iterator());
}

//
//
//

/** to return any Resource only once. */ 
public Set<ObjectLabelPair<E>> searchDistinctResources(String text) {
	Collection<ObjectLabelPair<E>> hits = searchText(text);
	HashSet<E> set = new HashSet<E>(hits.size());
	HashSet<ObjectLabelPair<E>> x = new HashSet<ObjectLabelPair<E>>(hits.size());	
	for(ObjectLabelPair<E> pair : hits) {
		boolean added = set.add(pair.getObject());
		if (added) x.add(pair);
	}
	return x;
}

}
