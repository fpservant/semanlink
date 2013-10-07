/* Created on dec 2012, based on ModelIndexedByLabel2 (mars 2010) */ // SKOSIFY
package net.semanlink.util.index;

import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

// TODO : redéfinir ModelIndexedByLabel2 avec ça

/**
 * Indexing (object, label) pairs.
 * <p>When compared with MultiLabelIndex (which indexes objects, on several labels),
 * this has the advantage of allowing to return the found label.
 * @author fps
 */
public class MultiLabelIndex2<ITEM> extends Index<ObjectLabelPair<ITEM>> {
protected Collator collator;

public MultiLabelIndex2(Iterator<ITEM> resToBeIndexedByLabel, MultiLabelGetter<ITEM> multiLabelGetter, Locale locale) {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), locale);
}

public MultiLabelIndex2(Iterator<ITEM> resToBeIndexedByLabel, MultiLabelGetter<ITEM> multiLabelGetter, IndexEntriesCalculator iec, Locale locale) {
	this(iec, locale);
	addResIterator(resToBeIndexedByLabel, multiLabelGetter);
}

/** addIterator or addCollection must be called after that */
protected MultiLabelIndex2(IndexEntriesCalculator iec, Locale locale) {
	super();
	this.locale = locale;
	this.collator = Collator.getInstance(this.locale);
	collator.setStrength(Collator.PRIMARY);
	this.labelGetter = new LabelGetter<ObjectLabelPair<ITEM>>() {
		public String getLabel(ObjectLabelPair<ITEM> o) { return o.getLabel(); }
	};
	init(labelGetter, iec, locale) ;
}

// avoid to have a res indexed by "truc" and "Truc"
/*
protected void addResIterator(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter) {
	HashSet<ResourceLabelPair> hs = new HashSet<ResourceLabelPair>();
	for (;resToBeIndexedByLabel.hasNext();) {
		Resource res = resToBeIndexedByLabel.nextResource();
		Iterator<String> labels = multiLabelGetter.getLabels(res);
		for (; labels.hasNext(); ) {
			String label = labels.next();
			ResourceLabelPair pair = new ResourceLabelPair(res, label);
			hs.add(pair);
		}
	}
	addCollection(hs);
}
*/
protected void addResIterator(Iterator<ITEM> resToBeIndexedByLabel, MultiLabelGetter<ITEM> multiLabelGetter) {
	HashSet<ObjectLabelPair<ITEM>> hs = new HashSet<ObjectLabelPair<ITEM>>(); // avoid to have a res indexed by "truc" and "Truc"
	for (;resToBeIndexedByLabel.hasNext();) {
		ITEM res = resToBeIndexedByLabel.next();
		Iterator<String> labels = multiLabelGetter.getLabels(res);
		
		if (labels.hasNext()) {
			// avoid adding duplicate labels.
			// We always add the first one. If there are more, we check for duplicates
			String label = labels.next();
			ObjectLabelPair<ITEM> pair = new ObjectLabelPair<ITEM>(res, label);
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
						pair = new ObjectLabelPair<ITEM>(res, label);
						hs.add(pair);
						cks.add(labelCK);
					}
				}
			}
		}
	}
	addCollection(hs);
}




/**
public static class CollatorBasedComparator implements Comparator<ResourceLabelPair> {
	private Collator collator;
	public CollatorBasedComparator(String lang) {
		collator = Collator.getInstance(new Locale(lang));
		// collator.setStrength(Collator.PRIMARY);
	}
	public int compare(ResourceLabelPair p0, ResourceLabelPair p1) {
		return collator.getCollationKey(p0.getLabel()).compareTo(collator.getCollationKey(p1.getLabel()));
	}
}
 */






//
//
//

/** to return any Resource only once. */ 
public Set<ObjectLabelPair<ITEM>> searchDistinctResources(String text) {
	Collection<ObjectLabelPair<ITEM>> hits = searchText(text);
	HashSet<ITEM> set = new HashSet<ITEM>(hits.size());
	HashSet<ObjectLabelPair<ITEM>> x = new HashSet<ObjectLabelPair<ITEM>>(hits.size());	
	for(ObjectLabelPair<ITEM> pair : hits) {
		boolean added = set.add(pair.getObject());
		if (added) x.add(pair);
	}
	return x;
}

}
