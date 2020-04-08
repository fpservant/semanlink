/* Created on 16 mai 2005 */
package net.semanlink.util.index;

import java.text.Collator;
import java.util.*;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Index a thesaurus by the words included in its terms. 
 * 
 * <p> Contains both a HashMap (word -> list of things) and a sorted list of the words.
 * The sorted list of words (index entries) allows to search for the beginning of a word. (Should be rewritten using 
 * the TreeMap class?). Searches are performed on these entries considered as simple strings, not "text".
 * The "index entries" should therefore be a normalized form of words 
 * (such as those produced using {@link net.semanlink.util.index.I18nFriendlyIndexEntries I18nFriendlyIndexEntries}).</p>
 *
 * <p>The Index does not store the labels of the items. But labels are often necessary when displaying
 * search results. One way is to have the labels included in the items being indexed. </p>
 * @see net.semanlink.util.index.jena.ModelIndexedByLabel
 * 
 * <p>Note:
 * which label should be displayed in a search result: the one related to the searched terms, or the
 * preferred label of the item found? Well, it depends</p>
 * 
 * <p>Originally built for www.semanlink.net, some methods or parameters still have a name based
 * on the fact that this was developed to index keywords (or tags)</p>
 */
public class LabelIndex<E> extends GenericIndex<ObjectLabelPair<E>> implements IndexInterface<ObjectLabelPair<E>> {
protected LabelGetter<E> labelGetter;
protected IndexEntriesCalculator indexEntryCalculator;
protected Locale locale;
protected Collator collator;

//
// CONSTRUCTION AND UPDATES
//

/**
 * Creates an Index of the items of a Collection. 
 * @param items the things to index.
 * @param labelGetter tells how to get the label of an item
 * @param indexEntriesCalculator tells how to extract index entries from a label. Index entries should typically be
 * words in a normalized form.
 * @throws Exception 
 */
public LabelIndex(Iterator<E> items, LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntriesCalculator, Locale locale) throws Exception {
	this(labelGetter, indexEntriesCalculator, locale);
	try (Update<E> up = new Update<>(this)) {
		up.addIterator(items);
	}
}

public LabelIndex(LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntryCalculator, Locale locale) {
	super();
	init(labelGetter, indexEntryCalculator, locale);
}

public static I18nFriendlyIndexEntries newI18nFriendlyIndexEntries(Locale locale) {
	return new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_"));
}

/**
 * sets the parameters of the indexing.
 * <p>To be used after the default empty constructor.</p>
 * @param labelGetter tells how to get the label of an item
 * @param indexEntryCalculator tells how to extract words from a label
 * @param locale
 */
private void init(LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntryCalculator, Locale locale) {
	this.labelGetter = labelGetter;
	this.indexEntryCalculator = indexEntryCalculator; 
	this.locale = locale;
	this.collator = Collator.getInstance(this.locale);
	collator.setStrength(Collator.PRIMARY);
}

//
// UPDATES
//

public static class Update<E> implements AutoCloseable {
	LabelIndex<E> index;
	boolean needToComputeWords = false;
	boolean needToSortWords = false;
	
	public Update(LabelIndex<E> index) {
		this.index = index;
	}
	
	@Override public void close() throws Exception {
		if (needToComputeWords) {
			index.setHashMap(index.word2tagsHM); // includes the sorting of words // TODO pas très joli
		} else {
			if (needToSortWords) {
				Collections.sort(index.words);
			}
		}
	}
	
	// normally called once, or a few times 
	public void addIterator(Iterator<E> kws) throws Exception {
		boolean initing = true;
		needToComputeWords = initing;
		if (index.word2tagsHM == null) index.word2tagsHM = new HashMap<>();
		for (;kws.hasNext();) {
			addItem(kws.next(), !initing);
		}
	}

	public void addItem(E kw) {
		addItem(kw, true);
	}

	/**
	 * @param kw
	 * @param updateWords if true, this.words is updated, else not. False is used during construction, in order to avoid
	 * sorting each time a kw is added to the hashmap: words are sorted only once, at the end.
	 */
	protected void addItem(E kw, boolean updateWords) {
		Iterator<String> labels = index.labelGetter.getLabels(kw);	
		addLabels(kw, labels, index.locale, updateWords);
	}

	/**
	 * modifies this.hm
	 * if updateWords, modifies also this.words if needed but, beware, without sorting it
	 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
	protected void addLabels(E kw, Iterator<String> labels, Locale locale, boolean updateWords) {
		for(;labels.hasNext();) {
			addLabel(kw, labels.next(), locale, updateWords);
		}
	}
	
	/**
	 * modifies this.hm
	 * if updateWords, modifies also this.words if needed but, beware, without sorting it
	 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
	public void addLabel(E kw, String label, Locale locale, boolean updateWords) {
		List<String> wordsInLabel = index.indexEntryCalculator.indexEntries(label, locale);
		ObjectLabelPair<E> olp = new ObjectLabelPair<>(kw, label);
		for (int i = 0; i < wordsInLabel.size(); i++) {
			String word = wordsInLabel.get(i);
			boolean b = index.addWordEntry(word, olp, updateWords);
			if (b) needToSortWords = true;
		}
	}
	
	//
	// DELETE
	//
	
	public void deleteItem(E kw) {
		Iterator<String> labels = index.labelGetter.getLabels(kw);
		for(;labels.hasNext();) {
			String label = labels.next();
			removeKwLabel(kw, label, index.locale);
		}
	}

	private void removeKwLabel(E kw, String label, Locale locale) {
		ObjectLabelPair<E> olp = new ObjectLabelPair<>(kw, label);
		List<String> wordEntries = index.indexEntryCalculator.indexEntries(label, locale);
		index.removeEntries(olp, wordEntries);
	}
} // class Update

/** return their normalized form */
protected List<String> label2indexEntries(String label, Locale locale) {
	return this.indexEntryCalculator.indexEntries(label, locale);
}

///**
// *  Search the items containing all words in text. 
// *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
// *  <p>Not sorted.</p> */
//public List<E> search(String text) {
//	Set<E> x = searchText(text);
//	ArrayList<E> alx = new ArrayList<E>(x.size());
//	alx.addAll(x);
//	return alx;
//}

/**
 *  Search the items containing all words in text. 
 *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
 */
// @Override // implements IndexInterface
public Set<ObjectLabelPair<E>> searchText(String text) {
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
	Set<ObjectLabelPair<E>> olps = searchWordStarts(wordsInText);
	return searchWordStarts(wordsInText);
}


/** 
 * for semanlink: Les keywords extraits d'un texte.
 */
// TODO CHANGE FOR AHO and cette version a été blindly updated
public Set<ObjectLabelPair<E>> getKeywordsInText(String text) {
	HashSet<ObjectLabelPair<E>> hs = new HashSet<ObjectLabelPair<E>>();
	// word in text
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
	// We sort them, because we use it (search for "wordsInText needs to be sorted")
	Collections.sort(wordsInText);
	
	// Several different ways to decide whether a kw applies to the text:
	// (S3):
	// label of kw is in text
	// (S3 bis)
	// normalized label of kw is in normalized text
	// (S2):
	// hs.add(kw);
	// (S1)
	// all the words of kw are in wordsInText?
	// - (S1) all the words of kw are in wordsInText
	// - (S2) one of the words of kw is in wordsInText (NO : condition is too weak, at least to add, by default, this kw to the model
	// - (S3) label of kw is in text
	// - (S3 bis) normalized label of kw is in normalized text (don't sort wordsInText to do that)
	for (String word : wordsInText) {
		// les kws contenant word
		List<ObjectLabelPair<E>> olps = this.word2tagsHM.get(word);
		if (olps == null) continue;

		for (int ikw = 0; ikw < olps.size(); ikw++) {
			ObjectLabelPair<E> olp = olps.get(ikw);
			// (S2):
			// hs.add(kw);
			// (S1)
			// all the words of kw are in wordsInText?

			boolean isOK = false; // passe à true si tous les mots d'un label sont dans le texte
			Iterator<String> labels = labelGetter.getLabels(olp.getObject());
			String label = null;
			for (;labels.hasNext();) {
				label = labels.next();
				List<String> indexEntriesInLabel = label2indexEntries(label, locale);
				// (S1): verify whether all the tokens composing kw are in wordsInText
				boolean addIt = true;
				for (String kwItem : indexEntriesInLabel) {
					// is-it in wordsInText ?
					// if kwItem is word itself, then yes, of course, it is in wordsInText
					if (kwItem.equals(word)) continue;
					// here, we scan the list of wordsInText
					// if they are sorted, we can do a binarysearch
					//					boolean inWords = false;
					//					for (int j = 0; j < nbWords; j++) {
					//						if (kwItem.equals(wordsInText.get(j))) {
					//							inWords = true;
					//							break;
					//						}
					//					}
					//					if (!inWords) {
					//						addIt = false;
					//						break;
					//					}
					int k = Collections.binarySearch(wordsInText, kwItem); // "wordsInText needs to be sorted"
					if (k < 0) { // kwItem not in wordsInText
						addIt = false;
						break;
					}
				} // for iToken
				if (addIt) {
					isOK = true;
					break;
				}
			} // for labels
			
			if (isOK) {
				// System.out.println("LabelIndex " + text + " : " + label);
				hs.add(olp);			
			}
		} // for ikws
	}
	// but maybe we got for a text such as "semantic web services are..."
	// the kws "semantic web", "web services" and "semantic web services"
	// We should try to filter the returned set
	return hs;
}

//
//
//

// made for semanlink
/**
 * The list of ITEMs which have a label "matching exactly" a given label, that is, 
 * composed of exactly the same words (word in the meaning of IndexEntriesCalculator)
 * (but maybe not in the same order).
 * <p>to get the tag corresponding to a given label.</p>
 */
public List<ObjectLabelPair<E>> label2KeywordList(String kwLabel, Locale locale) {
	List<String> indexEntriesInLabel = this.indexEntryCalculator.indexEntries(kwLabel , locale);
	List<ObjectLabelPair<E>> alx = andOfWords(indexEntriesInLabel);
	if (alx.size() == 0) return alx;
	
  // kws in alx contain all the words (with more than 2 letters) in kwLabel
	// // This comment was when we were dealing with one label only
	// // They are OK, except if they also contain other words
	// They are OK if one of their labels matches kwLabel
	int n = indexEntriesInLabel.size();
	Collections.sort(indexEntriesInLabel); // to test for equality of lists -- hum, is it a good idea to reoder?
	for (int i = alx.size() - 1; i > -1; i--) {
		ObjectLabelPair<E> kw = alx.get(i);
		// String label = labelGetter.getLabel(kw);
		// if ((this.indexEntryCalculator).indexEntries(label, locale).size() > n) alx.remove(i);
		
//		Iterator<String> labels = labelGetter.getLabels(kw);
//		boolean ok = false;
//		for (;labels.hasNext();) {
//			String label = labels.next();
//			List<String> indexEntriesInKW = this.indexEntryCalculator.indexEntries(label, locale);
//			if (indexEntriesInKW.size() > n) continue;
//			Collections.sort(indexEntriesInKW);  // to test for equality of lists
//			if (indexEntriesInKW.equals(indexEntriesInLabel)) {
//				ok = true;
//				break;
//			}
//		}

		String label = kw.getLabel();
		List<String> indexEntriesInKW = this.indexEntryCalculator.indexEntries(label, locale);
		if (indexEntriesInKW.size() > n) continue;
		Collections.sort(indexEntriesInKW);  // to test for equality of lists -- hum, is it a good idea to reoder?
		if (!indexEntriesInKW.equals(indexEntriesInLabel)) {
			alx.remove(i);
		}
	}
	return alx;
}

///**
// * The list of ITEMs that are indexed by all the words in a given label.
// * <p>That is a search for the AND of all words in the label.
// * <p>"All the words" meaning: as defined by this.indexEntryCalculator</p>
// */
//public List<E> getItemsContainingAllWords(String label, Locale locale) {
//	return andOfWords(this.indexEntryCalculator.indexEntries(label , locale));
//}
//
/**
 * The list of ITEMs that are indexed by all the words in a given list.
 * @param words if list is empty, returns an empty list
 * @return
 */
private List<ObjectLabelPair<E>> andOfWords(List<String> words) {
	int n = words.size();
	if (n == 0) return new ArrayList<>(0);
	String word = words.get(0);
	List<ObjectLabelPair<E>> kws = word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new ArrayList<>(0);
	// we make a clone of kws (in order to not modify kws!)
	ArrayList<ObjectLabelPair<E>> alx = new ArrayList<>(kws.size());
	alx.addAll(kws);
	for (int iword = 1; iword < n; iword++) {
		word = words.get(iword);
		kws = word2tagsHM.get(word);
		if (kws == null) return new ArrayList<>(0);
		for (int i = alx.size() - 1; i > -1; i--) {
			Object kw = alx.get(i);
			if (!(kws.contains(kw))) alx.remove(i);
		}
		if (alx.size() == 0) return alx;
	}
	return alx;
}




}
