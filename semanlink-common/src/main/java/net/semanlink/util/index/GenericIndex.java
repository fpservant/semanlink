/* Created on 16 mai 2005 */
package net.semanlink.util.index;

import java.util.*;

// ATTENTION LES RECHERCHES ICI SONT (DU POINT DE VUE DES Index DE SEMANLINK),
// SUPPOSEES SUR DU TEXTE NORMALISE

/**
 * Index a thesaurus by the words included in its terms. 
 * <p> Contains both a HashMap (word -> list of things) and a sorted list of the words.
 * The sorted list of words allows to search for the beginning of a word. (Should be rewritten using 
 * the TreeMap class?)</p>
 */
public class GenericIndex<ITEM> {
protected HashMap<String, List<ITEM>> word2tagsHM; // thesaurusIndex ds SLModel
/** List of words in tags (in normalzed form, cf CharConverter).
 *  Sorted by construction, used for binary search */
protected ArrayList<String> words;

//
// CONSTRUCTION AND UPDATES
//

public GenericIndex(HashMap<String, List<ITEM>> word2tagsHM) {
	setHashMap(word2tagsHM);
}

/** You must call setHashmap after that */
GenericIndex() {}
final void setHashMap(HashMap<String, List<ITEM>> word2tagsHM) {
	this.word2tagsHM = word2tagsHM;
	this.words = new ArrayList<String>(this.word2tagsHM.keySet());
	Collections.sort(words);	
}

//
// GETS
//

protected HashMap<String, List<ITEM>> getHashMap() { return this.word2tagsHM; }

//
//
//

/**
 *  Search the items of the thesaurus containing all words in wordStarts. 
 *  <p>(search for the beginning of words: if wordStarts is "sem", returns "semanlink", "semantic web", etc.)</p>
 *  <p>Not sorted.</p> 
 *  <p>Implementation note: when there are several words in wordStarts, we compute the intersection of the results of the searches
 *  of the different words. An alternative would be to search for one of the words, and then loop over the
 *  labels of the found items, and check whether other words are included. This would probably be better :
 *  - faster? (probably not: we have to get the labels, and compute their normalized form)
 *  - allow to not return "found" when the words are included in different labels of the found item, but not in one of them only
 *  - return the found label (-> not returning 	 Set<ITEM>, but Map<ITEM, String> or Set<ITEMLabelPair>
 *  </p>
 *  */
public Set<ITEM> searchWordStarts(List<String> wordStarts) {
	int nbWords = wordStarts.size();
	if (nbWords == 0) return new HashSet<ITEM>(0);
	// we search the first word of words first, and then intersect with the result of the search of the other words.
	HashSet<ITEM> hs = new HashSet<ITEM>();
	searchWordStart(wordStarts.get(0), hs);
	
	if (nbWords == 1) return hs;
	ArrayList<ITEM> list = new ArrayList<ITEM>(); // we use allways the same one, with clear() instead of recreating it
	for (int i = 1; i < nbWords; i++) {
		searchWordStart(wordStarts.get(i), list);
		hs.retainAll(list);
		if (hs.size() == 0) return hs;
		list.clear();
	}
	return hs;	
}


// word supposed in normalized form -- that is, search the start of word in the keys
/**
 * Returns the list of the items of the thesaurus whose label contains a word beginning with the word param. 
 * The returned list may contain doubles. */
public ArrayList<ITEM> searchWordStart(String word) {
	ArrayList<ITEM> x = new ArrayList<ITEM>();
	searchWordStart(word, x);
	return x;
} // searchStartOfWord

/**
 * Searches for items indexed by key starting with word, and adds the results to a given collection.
 */
public void searchWordStart(String word, Collection<ITEM> result) {
	int k = Collections.binarySearch(this.words, word);
	if (k >= 0) {
		// word trouvé
		List<ITEM> kws = word2tagsHM.get(word);
		result.addAll(kws);
		k++;
	} else {
		k = -1*k -1;
	}
	int n = this.words.size();
	while (k < n) {
		String nextWord = this.words.get(k);
		if (nextWord.startsWith(word)) {
			List<ITEM> kws = word2tagsHM.get(nextWord);
			result.addAll(kws);
			k++;			
		} else {
			break;
		}
	}
} // searchStartOfWord

/**
 * Searches for items indexed by key starting with word, and adds the results
 * to a given collection. 
 * @param limit max number of items added to result, 0 for no limit
 */
public void searchWordStart(String word, Collection<ITEM> result, int limit) {
	if (limit <= 0) searchWordStart(word, result);
	int resultSizeLimit = result.size() + limit;
	boolean limitReached;
	int k = Collections.binarySearch(this.words, word);
	if (k >= 0) {
		// word trouvé
		List<ITEM> kws = word2tagsHM.get(word);
		limitReached = addItems(kws, result, resultSizeLimit);
		if (limitReached) return;
		k++;
	} else {
		k = -1*k -1;
	}

	int n = this.words.size();
	while (k < n) {
		String nextWord = this.words.get(k);
		if (nextWord.startsWith(word)) {
			List<ITEM> kws = word2tagsHM.get(nextWord);
			limitReached = addItems(kws, result, resultSizeLimit);
			if (limitReached) return;
			k++;
		} else {
			return;
		}
	}
} // searchStartOfWord

/**
 * Adds the items to result until resultSizeLimit be reached
 * @return true if finished (limit reached)
 */
private boolean addItems(List<ITEM> items, Collection<ITEM> result, int resultSizeLimit) {
	int size = result.size();
	if (size >= resultSizeLimit) return true;
	Iterator<ITEM> it = items.iterator();
	for (;it.hasNext();) {
		boolean added = result.add(it.next());
		if (added) size++;
		if (size >= resultSizeLimit) return true;
	}
	return false;
}

//
//
//

// TODO
/*
public List label2KeywordList(String kwLabel, Locale locale) {
	ArrayList<String> wordsInLabelAl = this.wordsInString.words(kwLabel , locale);
	String[] normalizedWordsInLabel = convert(wordsInLabelAl);
	int n = normalizedWordsInLabel.length;
	if (n == 0) return new ArrayList(0);
	String word = normalizedWordsInLabel[0];
	List<Object> kws = word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new ArrayList(0);
	// we make a clone of kws (in order to not modify kws!)
	ArrayList<Object> alx = new ArrayList(kws.size());
	alx.addAll(kws);
	for (int iword = 1; iword < n; iword++) {
		word = normalizedWordsInLabel[iword];
		kws = word2tagsHM.get(word);
		if (kws == null) return new ArrayList(0);
		for (int i = alx.size() - 1; i > -1; i--) {
			Object kw = alx.get(i);
			if (!(kws.contains(kw))) alx.remove(i);
		}
		if (alx.size() == 0) return new ArrayList(0);
	}
	// kws in alx contain all the words (with more than 2 letters) in kwLabel
	// They are OK, except if they also contain other words
	for (int i = alx.size() - 1; i > -1; i--) {
		Object kw = alx.get(i);
		String label = labelGetter.getLabel(kw);
		if ((this.wordsInString).words(label, locale).size() > n) alx.remove(i);
	}
	return alx;
}
*/

}
