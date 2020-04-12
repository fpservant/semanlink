/* Created on 16 mai 2005 */
package net.semanlink.util.index;

import java.text.Collator;
import java.util.*;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Index a set of entities by the words of their labels. 
 * 
 * Contains both a Map (word -> list of couples (entity, label)) and a sorted list of the words.
 * 
 * The sorted list of words (index entries) allows to search for the beginning of a word. (Should be rewritten using 
 * the TreeMap class?). Searches are performed on these entries considered as simple strings, not "text".
 * 
 * The "index entries" should therefore be a normalized form of words 
 * (such as those produced using {@link net.semanlink.util.index.I18nFriendlyIndexEntries I18nFriendlyIndexEntries}).
 *
 */

//
// WHEN RETURNING LISTS, BEWARE TO NOT DIRECTLY RETURN A LIST FROM THE INDEX THAT COULD BE MODIFIED EXTERNALLY !!!
//

public class WordIndex<E> extends GenericWordIndex<ObjectLabelPair<E>> implements WordIndexInterface<ObjectLabelPair<E>> {
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
public WordIndex(Iterator<E> items, LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntriesCalculator, Locale locale) throws Exception {
	this(labelGetter, indexEntriesCalculator, locale);
	try (Update<E> up = new Update<>(this, true)) {
		up.addIterator(items);
	}
}

public WordIndex(LabelGetter<E> labelGetter, IndexEntriesCalculator indexEntryCalculator, Locale locale) {
	super();
	init(labelGetter, indexEntryCalculator, locale);
}

public static I18nFriendlyIndexEntries newI18nFriendlyIndexEntries(Locale locale) {
	return newI18nFriendlyIndexEntries(new CharConverter(locale, "_"));
}

public static I18nFriendlyIndexEntries newI18nFriendlyIndexEntries(CharConverter converter) {
	return new I18nFriendlyIndexEntries(new WordsInString(true, true), converter);
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

// to update the index, create an Update then add items to it.
// Update is an AutoClosable: structures are updated on close.

public Update<E> newUpdate(boolean initing) {
	return new Update<>(this, initing);
}

public static class Update<E> implements AutoCloseable {
	WordIndex<E> index;
	protected final boolean initing;
	private boolean needToComputeWords;
	private boolean needToSortWords;
	
	/**
	 * @param index
	 * @param initing true for an init, or a "big update", false otherwise
	 */
	Update(WordIndex<E> index, boolean initing) {
		this.index = index;
		this.initing = initing;
		this.needToComputeWords = initing;
		this.needToSortWords = initing;
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
		/// boolean initing = true;
		/// needToComputeWords = initing;
		if (index.word2tagsHM == null) index.word2tagsHM = new HashMap<>();
		for (;kws.hasNext();) {
			addItem(kws.next());
		}
	}

	public void addItem(E kw) {
		Iterator<String> labels = index.labelGetter.getLabels(kw);	
		addLabels(kw, labels, index.locale);
	}

	public void addLabels(E kw, Iterator<String> labels, Locale locale) {
		for(;labels.hasNext();) {
			addLabel(kw, labels.next(), locale);
		}
	}
	
	public void addLabel(E kw, String label, Locale locale) {
		List<String> wordEntries = index.indexEntryCalculator.indexEntries(label, locale);
		ObjectLabelPair<E> olp = new ObjectLabelPair<>(kw, label);
		boolean updateWords = !initing;
		for (int i = 0; i < wordEntries.size(); i++) {
			String word = wordEntries.get(i);
			// if updateWords, this modifies also this.words if needed but, beware, without sorting it:
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
			removeLabel(kw, label, index.locale);
		}
	}

	public void removeLabel(E kw, String label, Locale locale) {
		ObjectLabelPair<E> olp = new ObjectLabelPair<>(kw, label);
		List<String> wordEntries = index.indexEntryCalculator.indexEntries(label, locale);
		index.removeEntries(olp, wordEntries);
	}
} // class Update

/** return their normalized form */
protected List<String> label2indexEntries(String label, Locale locale) {
	return this.indexEntryCalculator.indexEntries(label, locale);
}

/**
 *  Search the items containing all words in text. 
 *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
 */
// @Override // implements WordIndexInterface
public Set<ObjectLabelPair<E>> string2entities(String text) {
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
	return searchWordStarts(wordsInText);
}


/** 
 * for semanlink: Les keywords extraits d'un texte.
 * 
 * @deprecated use AhoCorasick instead
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
				// System.out.println("WordIndex " + text + " : " + label);
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

// Until 2020-04, used by SLModel.kwLabel2KwCreatingItIfNecessary

// 2020-04 comments: the "equality" is seen independenltly of the order of words
// "Justine Gado" == "Gado Justine"
// 
/**
 * 
 * Get the tag(s) corresponding to a given label.
 * 
 * The list of ITEMs which have a label "matching exactly" a given label, that is, 
 * composed of exactly the same words (word in the meaning of IndexEntriesCalculator)
 * (but maybe not in the same order).
 * 
 * This is used in particular by the livesearch. Hum, is it? Only when creating tags, I think
 * 
 * WHy this "independent-of-word-order equality"?
 * well, because I seem to be used to it!
 * eg. searching for "sem web" to find "semantic web" or web "sémantique"
 * (in that case, wouldn't heart, as both labels are used for the tag.
 * But when you don't know and are genuinely searching? eg. searching
 * for a person by name, then thinking you should also give firstname)
 * 
 */
public List<ObjectLabelPair<E>> label2KeywordList(String kwLabel, Locale locale) {
	List<String> indexEntriesInLabel = this.indexEntryCalculator.indexEntries(kwLabel , locale);
	List<ObjectLabelPair<E>> alx = andOfWords(indexEntriesInLabel);
	if (alx.size() == 0) return alx;
	
  // kws in alx contain all the words (with more than 2 letters) in kwLabel
	// A kw in alx OK if one of its labels matches kwLabel exactly
	int n = indexEntriesInLabel.size(); // nb of words in kwLabel
	Collections.sort(indexEntriesInLabel); // to test for equality of lists -- hum, is it a good idea to reorder?
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
		if (indexEntriesInKW.size() != n) { // 2 lists of words have different size 
			alx.remove(i);
		} else {
			Collections.sort(indexEntriesInKW);  // to test for equality of lists -- hum, is it a good idea to reorder?
			if (!indexEntriesInKW.equals(indexEntriesInLabel)) {
				alx.remove(i);
			}
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
// BEWARE, WE MUST RETURN A COPY OF THE CONTENT TO AVOID MODIFYING word2tagsHM !!!
private List<ObjectLabelPair<E>> andOfWords(List<String> words) { // 2020-04 TODO : si un seul mot, optim
	int n = words.size();
	if (n == 0) return new ArrayList<>(0);
	String word = words.get(0);
	List<ObjectLabelPair<E>> kws = word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new ArrayList<>(0);
	// NO, don't do that, see next line 
	// if (n == 1) return kws;
	// we make a clone of kws (in order to not modify kws!)
	ArrayList<ObjectLabelPair<E>> alx = new ArrayList<>(kws.size());
	alx.addAll(kws);
	if (n == 1) return alx; // now we may
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
