/* Created on 18 mars 2010 */
package net.semanlink.util.index.jena;

import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

import org.apache.jena.rdf.model.*;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.Index;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index.MultiLabelGetter;
import net.semanlink.util.index.RDFSLabelGetter;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Indexing (Resource, label) pairs.
 * <p>When compared with ModelIndexedByLabel (which indexes Resources, on several labels),
 * this has the advantage of allowing to return the found label.
 * @author fps
 */
public class ModelIndexedByLabel2_SVG extends Index<ResourceLabelPair_SVG2> {
protected Model model;
protected Collator collator;

/** Default: uses RDFS.label */
public ModelIndexedByLabel2_SVG(ResIterator resToBeIndexedByLabel, Model model, Locale locale) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(locale.getLanguage()), model, locale);
}

public ModelIndexedByLabel2_SVG(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter, Model model, Locale locale) {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel2_SVG(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	this(iec, model, locale);
	addResIterator(resToBeIndexedByLabel, multiLabelGetter);
}

/** addIterator or addCollection must be called after that */
protected ModelIndexedByLabel2_SVG(IndexEntriesCalculator iec, Model model, Locale locale) {
	super();
	this.model = model;
	this.locale = locale;
	this.collator = Collator.getInstance(this.locale);
	collator.setStrength(Collator.PRIMARY);
	this.labelGetter = new LabelGetter<ResourceLabelPair_SVG2>() {
		public String getLabel(ResourceLabelPair_SVG2 o) { return o.getLabel(); }
	};
	init(labelGetter, iec, locale) ;
}

// v1.1.0.2 avoid to have a res indexed by "truc" and "Truc"
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
protected void addResIterator(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter) {
	HashSet<ResourceLabelPair_SVG2> hs = new HashSet<ResourceLabelPair_SVG2>(); // v1.1.0.2 avoid to have a res indexed by "truc" and "Truc"
	for (;resToBeIndexedByLabel.hasNext();) {
		Resource res = resToBeIndexedByLabel.nextResource();
		Iterator<String> labels = multiLabelGetter.getLabels(res);
		
		if (labels.hasNext()) {
			// avoid adding duplicate labels.
			// We always add the first one. If there are more, we check for duplicates
			String label = labels.next();
			ResourceLabelPair_SVG2 pair = new ResourceLabelPair_SVG2(res, label);
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
						pair = new ResourceLabelPair_SVG2(res, label);
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
public Set<ResourceLabelPair_SVG2> searchDistinctResources(String text) {
	Collection<ResourceLabelPair_SVG2> hits = searchText(text);
	HashSet<Resource> set = new HashSet<Resource>(hits.size());
	HashSet<ResourceLabelPair_SVG2> x = new HashSet<ResourceLabelPair_SVG2>(hits.size());	
	for(ResourceLabelPair_SVG2 pair : hits) {
		boolean added = set.add(pair.getResource());
		if (added) x.add(pair);
	}
	return x;
}

}
