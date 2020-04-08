/* Created on 16 mai 2005 */
package net.semanlink.util.index_B4_2020_04;

import java.util.*;

import net.semanlink.util.index.MultiLabelGetter;

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
public class MultiLabelIndex<E> extends GenericIndex<E> implements MultiLabelGetter<E>, IndexInterface<E> {
protected MultiLabelGetter<E> labelGetter;
protected IndexEntriesCalculator indexEntryCalculator;
protected Locale locale;

//
// CONSTRUCTION AND UPDATES
//

/**
 * Creates an Index of the items of a Collection. 
 * @param items the things to index.
 * @param labelGetter tells how to get the label of an item
 * @param indexEntriesCalculator tells how to extract index entries from a label. Index entries should typically be
 * words in a normalized form.
 */
public MultiLabelIndex(Collection<E> items, MultiLabelGetter<E> labelGetter, IndexEntriesCalculator indexEntriesCalculator, Locale locale) {
	init(labelGetter, indexEntriesCalculator, locale);
	addIterator(items.iterator());
}

public MultiLabelIndex(Collection<E> items, final LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntriesCalculator, Locale locale) {
	this(items, 
			new MultiLabelGetter<E>() {
				@Override
				public Iterator<String> getLabels(E o) {
					return Collections.singleton(labelGetter.getLabel(o)).iterator();
				}
			}, 
			indexEntriesCalculator, locale);
}

/**
 * Once created, you'll have to call init and then addCollection or addIterator
 */
public MultiLabelIndex() {}

/**
 * sets the parameters of the indexing.
 * <p>To be used after the default empty constructor.</p>
 * @param labelGetter tells how to get the label of an item
 * @param indexEntryCalculator tells how to extract words from a label
 * @param locale
 */
public void init(MultiLabelGetter<E> labelGetter, IndexEntriesCalculator indexEntryCalculator, Locale locale) {
	this.labelGetter = labelGetter;
	this.indexEntryCalculator = indexEntryCalculator; 
	this.locale = locale;
}

/** normally called once, or a few times 
 * @deprecated use addIterator(Iterator<E>) instead */
public void addCollection(Collection<E> items) {
	computeHashMap(items.iterator());
}

/** normally called once, or a few times */
public void addIterator(Iterator<E> items) {
	computeHashMap(items);
}

private void computeHashMap(Iterator<E> kws) {
	if (this.word2tagsHM == null) this.word2tagsHM = new HashMap<String, List<E>>();
	for (;kws.hasNext();) {
		addItem(kws.next(), false);
	}
	setHashMap(this.word2tagsHM); // this includes the update of this.words (including its sorting)
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
	Iterator<String> labels = labelGetter.getLabels(kw);	
	boolean needToSortWords = addLabels(kw, labels, locale, updateWords);
	if (needToSortWords) Collections.sort(this.words);
}

/** Add a label to kw */ // TODO should be renamed
public void addItem(E kw, String label, Locale locale) {
	boolean needToSortWords = addLabel(kw, label, locale, true);
	if (needToSortWords) Collections.sort(this.words);
}

/**
 * modifies this.hm
 * if updateWords, modifies also this.words if needed but, beware, without sorting it
 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
protected boolean addLabels(E kw, Iterator<String> labels, Locale locale, boolean updateWords) {
	boolean needToSortWords = false;
	for(;labels.hasNext();) {
		boolean b = addLabel(kw, labels.next(), locale, updateWords);
		if (b) needToSortWords = true;
	}
	return needToSortWords;
}

/**
 * modifies this.hm
 * if updateWords, modifies also this.words if needed but, beware, without sorting it
 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
protected boolean addLabel(E kw, String label, Locale locale, boolean updateWords) {
	List<String> wordsInLabel = this.indexEntryCalculator.indexEntries(label, locale);
	boolean needToSortWords = false;
	for (int i = 0; i < wordsInLabel.size(); i++) {
		String word = wordsInLabel.get(i);
		boolean b = addWordEntry(word, kw, updateWords);
		if (b) needToSortWords = true;
	}
	return needToSortWords;
}

/** 
 *  add the key word with data kw to this.hm. 
 *  if updateWords, modifies this.words if needed but, beware, without sorting it
 *  Returns true iff this.words has been modified (and therefore needs to be sorted) (This can only happen when updateWords)*/
private boolean addWordEntry(String wordEntry, E kw, boolean updateWords) {
	boolean needToSortWords = false;
	List<E> list = this.word2tagsHM.get(wordEntry);
	if (list == null) { // new word
		list = new ArrayList<E>(1);
		this.word2tagsHM.put(wordEntry, list);
		list.add(kw);
		if (updateWords) {
			this.words.add(wordEntry); // adding this new word to words
			needToSortWords = true;
		}
	} else {
		if (!list.contains(kw)) {
			list.add(kw);
		}
	}
	return needToSortWords;
}

public void deleteItem(E kw) {
	Iterator<String> labels = this.labelGetter.getLabels(kw);
	for(;labels.hasNext();) {
		removeKwLabel(kw, labels.next(), locale);
	}
}

private void removeKwLabel(E kw, String label, Locale locale) {
	List<String> wordsInLabel = this.indexEntryCalculator.indexEntries(label, locale);
	for (int i = 0; i < wordsInLabel.size(); i++) {
		String wordEntry = wordsInLabel.get(i);
		int k = Collections.binarySearch(this.words, wordEntry);
		if (k >= 0) { // word found
			List<E> kws = this.word2tagsHM.get(wordEntry);
			for (int j = 0; j < kws.size(); j++) {
				if (kws.get(j).equals(kw)) {
					kws.remove(j);
					break;
				}
			}
			if (kws.isEmpty()) {
				this.word2tagsHM.remove(wordEntry);
				this.words.remove(k);
			}
		}
	}	
}

//
//
//

@Override // implements MultiLabelGetter
public Iterator<String> getLabels(E o) { return this.labelGetter.getLabels(o); }

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
@Override // implements IndexInterface
public Set<E> searchText(String text) {
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
	return searchWordStarts(wordsInText);
}


/** 
 * for semanlink: Les keywords extraits d'un texte.
 */
public Set<E> getKeywordsInText(String text) {
	HashSet<E> hs = new HashSet<E>();
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
		List<E> kws = this.word2tagsHM.get(word);
		if (kws == null) continue;

		for (int ikw = 0; ikw < kws.size(); ikw++) {
			E kw = kws.get(ikw);
			// (S2):
			// hs.add(kw);
			// (S1)
			// all the words of kw are in wordsInText?

			boolean isOK = false; // passe à true si tous les mots d'un label sont dans le texte
			Iterator<String> labels = labelGetter.getLabels(kw);
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
				// System.out.println("MultiLabelIndex " + text + " : " + label);
				hs.add(kw);			
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
public List<E> label2KeywordList(String kwLabel, Locale locale) {
	List<String> indexEntriesInLabel = this.indexEntryCalculator.indexEntries(kwLabel , locale);
	List<E> alx = andOfWords(indexEntriesInLabel);
	if (alx.size() == 0) return alx;
	
  // kws in alx contain all the words (with more than 2 letters) in kwLabel
	// // This comment was when we were dealing with one label only
	// // They are OK, except if they also contain other words
	// They are OK if one of their labels matches kwLabel
	int n = indexEntriesInLabel.size();
	Collections.sort(indexEntriesInLabel); // to test for equality of lists
	for (int i = alx.size() - 1; i > -1; i--) {
		E kw = alx.get(i);
		// String label = labelGetter.getLabel(kw);
		// if ((this.indexEntryCalculator).indexEntries(label, locale).size() > n) alx.remove(i);
		Iterator<String> labels = labelGetter.getLabels(kw);
		boolean ok = false;
		for (;labels.hasNext();) {
			String label = labels.next();
			List<String> indexEntriesInKW = this.indexEntryCalculator.indexEntries(label, locale);
			if (indexEntriesInKW.size() > n) continue;
			Collections.sort(indexEntriesInKW);  // to test for equality of lists
			if (indexEntriesInKW.equals(indexEntriesInLabel)) {
				ok = true;
				break;
			}
		}
		if (!ok) alx.remove(i);
	}
	return alx;
}

/**
 * The list of ITEMs that are indexed by all the words in a given label.
 * <p>That is a search for the AND of all words in the label.
 * <p>"All the words" meaning: as defined by this.indexEntryCalculator</p>
 */
public List<E> getItemsContainingAllWords(String label, Locale locale) {
	return andOfWords(this.indexEntryCalculator.indexEntries(label , locale));
}

/**
 * The list of ITEMs that are indexed by all the words in a given list.
 * @param words if list is empty, returns an empty list
 * @return
 */
private List<E> andOfWords(List<String> words) {
	int n = words.size();
	if (n == 0) return new ArrayList<E>(0);
	String word = words.get(0);
	List<E> kws = word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new ArrayList<E>(0);
	// we make a clone of kws (in order to not modify kws!)
	ArrayList<E> alx = new ArrayList<E>(kws.size());
	alx.addAll(kws);
	for (int iword = 1; iword < n; iword++) {
		word = words.get(iword);
		kws = word2tagsHM.get(word);
		if (kws == null) return new ArrayList<E>(0);
		for (int i = alx.size() - 1; i > -1; i--) {
			Object kw = alx.get(i);
			if (!(kws.contains(kw))) alx.remove(i);
		}
		if (alx.size() == 0) return alx;
	}
	return alx;
}




}
