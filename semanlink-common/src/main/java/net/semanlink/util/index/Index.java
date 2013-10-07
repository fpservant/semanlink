/* Created on 16 mai 2005 */
package net.semanlink.util.index;

import java.util.*;

/*
 * DE LA DIFFICULTE D'AVOIR PLUSIEURS LABELS POUR UN ITEM
 * 
 * (Being able to have several labels for one object requires to keep the link object -> list of labels in order to be able
 * to implement the getKeywordsInText and label2KeywordList methods, as well as searchWordStarts - moins évident sur ce derier. Pourrait être ok
 * en l'état)
 */
/**
 * Index a thesaurus by the words included in its terms. 
 * <p> Contains both a HashMap (word -> list of things) and a sorted list of the words.
 * The sorted list of words (index entries) allows to search for the beginning of a word. (Should be rewritten using 
 * the TreeMap class?). Searches are performed on these entries considered as simple strings, not "text".
 * The "index entries" should therefore be a normalized form of words 
 * (such as those produced using {@link net.semanlink.util.index.I18nFriendlyIndexEntries I18lFriendlyIndexEntries}).</p>
 * <p>The Index does not store the labels of the items. But labels are often necessary when displaying
 * search results. One way is to have the labels included in the items being indexed. (Note:
 * which label should be displayed in a search result: the one related to the searched terms, or the
 * prefered label on the item found? Well, it depends)</p>
 * <p>Does not handle several labels for one item (LabelGetter only defines how to get one Label) (but you can 
 * pass the same object several times with different labels if you include it in a couple (object - label)) Or
 * you can use MultiLabelIndex instead.</p>
 * <p>Originally built for www.semanlink.net, some methods or parameters still have a name based
 * on the fact that this was developed to index keywords (or tags)</p>
 */
public class Index<ITEM> extends GenericIndex<ITEM> implements IndexInterface<ITEM> {
protected LabelGetter<ITEM> labelGetter;
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
 * @param locale
 */
public Index(Collection<ITEM> items, LabelGetter<ITEM> labelGetter, IndexEntriesCalculator indexEntriesCalculator, Locale locale) {
	init(labelGetter, indexEntriesCalculator, locale);
	addIterator(items.iterator());
}



/**
 * Once created, you'll have to call init and then addCollection or addIterator
 */
public Index() {}

/**
 * sets the parameters of the indexation.
 * <p>To be used after the default empty constructor.</p>
 * @param labelGetter tells how to get the label of an item
 * @param indexEntryCalculator tells how to extract words from a label
 * @param locale
 */
public void init(LabelGetter<ITEM> labelGetter, IndexEntriesCalculator indexEntryCalculator, Locale locale) {
	this.labelGetter = labelGetter;
	this.indexEntryCalculator = indexEntryCalculator; 
	this.locale = locale;
}

/** normally called once, or a few times */
public void addCollection(Collection<ITEM> items) {
	computeHashMap(items);
}

public void addIterator(Iterator<ITEM> items) {
	computeHashMap(items);
}

private void computeHashMap(Collection<ITEM> kws) {
	computeHashMap(kws.iterator());
}

private void computeHashMap(Iterator<ITEM> kws) {
	if (this.word2tagsHM == null) this.word2tagsHM = new HashMap<String, List<ITEM>>();
	for (;kws.hasNext();) {
		addItem(kws.next(), false);
	}
	setHashMap(this.word2tagsHM); // ok, pas terrible: forcer à recalculer la liste des mots
}

public void addItem(ITEM kw) {
	addItem(kw, true);
}

/**
 * @param kw
 * @param updateWords if true, this.words is updated, else not. False is used during construction, in order to avoid
 * sorting each time a kw is added to the hashmap: words are sorted only once, at the end.
 */
protected void addItem(ITEM kw, boolean updateWords) {
	String label = labelGetter.getLabel(kw);	
	boolean needToSortWords = addLabel(kw, label, locale, updateWords);
	if (needToSortWords) Collections.sort(this.words);
}

public void addItem(ITEM kw, String label, Locale locale) {
	boolean needToSortWords = addLabel(kw, label, locale, true);
	if (needToSortWords) Collections.sort(this.words);
}

/**
 * modifies this.hm
 * if updateWords, modifies also this.words if needed but, beware, without sorting it
 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
protected boolean addLabel(ITEM kw, String label, Locale locale, boolean updateWords) {
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
private boolean addWordEntry(String wordEntry, ITEM kw, boolean updateWords) {
	boolean needToSortWords = false;
	List<ITEM> list = this.word2tagsHM.get(wordEntry);
	if (list == null) {
		list = new ArrayList<ITEM>(1);
		this.word2tagsHM.put(wordEntry, list);
		list.add(kw);
		if (updateWords) {
			this.words.add(wordEntry);
			needToSortWords = true;
		}
	} else {
		if (!list.contains(kw)) {
			list.add(kw);
		}
	}
	return needToSortWords;
}

/** BEWARE: only looks for one label, doesn't take care of alias // to be changed when we'll switch to using several labels
 * instead of alias */
public void deleteItem(ITEM kw) {
	String label = this.labelGetter.getLabel(kw);
	removeKwLabel(kw, label, locale);
}

private void removeKwLabel(ITEM kw, String label, Locale locale) {
	List<String> wordsInLabel = this.indexEntryCalculator.indexEntries(label, locale);
	for (int i = 0; i < wordsInLabel.size(); i++) {
		String wordEntry = wordsInLabel.get(i);
		int k = Collections.binarySearch(this.words, wordEntry);
		if (k >= 0) {
			// word trouvé
			List<ITEM> kws = this.word2tagsHM.get(wordEntry);
			for (int j = 0; j < kws.size(); j++) {
				if (kws.get(j).equals(kw)) {
					kws.remove(j);
					break;
				}
			}
			if (kws.size() == 0) {
				this.word2tagsHM.remove(wordEntry);
				this.words.remove(k);
			}
		}
	}	
}

//
// GETS
//

/**
 * clés : les mots présents ds les short uri de kws (par ex homo)
 * data : les kws les contenant (par ex .../homo_sapiens)
 * ATTENTION : les mots des alias sont dedans, mais ACHTUNG,
 * ils pointent vers un pseudo SLKeyword constitué de l'uri de l'alias (non résolu) :
 * en effet, on a besoin des mots de l'uri de l'alias pour la recherche des kws ds un texte
 * dans le cas où le label de l'alias a plusieurs mots : pour savoir si l'alias s'applique,
 * il faut avoir l'ensemble de ses mots - id est son uri
 * @see getKeywordsInText
 * (RQ : il doit y avoir possibilité d'optimiser pour les recherches: si le label de l'alias
 * n'a qu'un seul mot, on doit pouvoir le faire pointer directement vers le kw - mais on perd
 * du temps à l'indexation)
 * C'EST BIEN BEAU CA, mais pose un pb pour le Search : quand on tombe sur un alias, donne l'alias
 * -> le search fait le resolve
 * RQ // TODO ? : on n'aurait pas besoin d'y mettre les kws composés d'un seul mot
 * (par ex afrique) parce qu'on peut par ailleurs retrouver le kw en question
 * (voir getKeywordsInText - qui serait alors à modifier pour en profiter)
 * @return
 */
protected HashMap<String, List<ITEM>> getHashMap() { return super.getHashMap(); }

//
//
//

// @find C'EST A CAUSE DE CA QU'ON NE PEUT AVOIR PLUSIEURS LABELS POUR UN OBJET
// (utilisé dans getKeywordsInText) -- et aussi label2KeywordList
/** return their normalized form */
protected List<String> kw2indexEntries(ITEM kw, Locale locale) { 
	String label = labelGetter.getLabel(kw);
	return this.indexEntryCalculator.indexEntries(label, locale);
}

/*
 *  Search the items containing all words in text. 
 *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
 *  <p>Not sorted.</p> 
 *  @deprecated use searchText instead
 */
/*public List<ITEM> search(String text) {
	Set<ITEM> x = search1(text);
	ArrayList<ITEM> alx = new ArrayList<ITEM>(x.size());
	alx.addAll(x);
	return alx;
}*/

/**
 *  Search the items containing all words in text. 
 *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
 */
public Set<ITEM> searchText(String text) {
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
	return searchWordStarts(wordsInText);
}





/** 
 * for semanlink: Les keywords extraits d'un texte.
 */
public Set<ITEM> getKeywordsInText(String text) {
	// ne s'occupe pas des doubles ? // TODO
	HashSet<ITEM> hs = new HashSet<ITEM>();
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
	// - (S2) one of the words of kw is in wordsInText (NO : condition is to weak, at least to add, by default, this kw to the model
	// - (S3) label of kw is in text
	// - (S3 bis) normalized label of kw is in normalized text (don't sort wordsInText to do that)
	for (String word : wordsInText) {
		// les kws contenant word
		List<ITEM> kws = this.word2tagsHM.get(word);
		
		if (kws == null) continue;
		
		// remarquons qu'un kw d'un seul mot est a priori bon à prendre,
		// mais attention, pour éviter de mettre à la fois, par ex pour le texte "C2G rend visite à Nissan",
		// à la fois c2g, nissan et c2g_nissan, on ne retiendra en définitive un tel kw que
		// si on ne retient pas de kw de plusieurs mots le contenant, d'où :
		ITEM oneTokenKw = null; // éventuel kw à un seul token (supposé unique ds kws : ne serait
		// pas le cas uniquement si même short kw ds 2 thesaurus différents)
		boolean addOneTokenKw = true; // a priori, si oneTokenKw != null, on le mettra, si on ne met pas de kw plus long
		for (int ikw = 0; ikw < kws.size(); ikw++) {
			ITEM kw = kws.get(ikw);
			// (S2):
			// hs.add(kw);
			// (S1)
			// all the words of kw are in wordsInText?

			// si un des éléments est un kw à un seul token, celui est a priori à prendre, (cf plus haut)
			// mais seulement si on ne met pas de kw à plusiers mots le contenant
			List<String> indexEntriesInKW = kw2indexEntries(kw, locale); // @find C'EST A CAUSE DE CA QU'ON NE PEUT AVOIR PLUSIEURS LABELS POUR UN OBJET
			int nbTokens = indexEntriesInKW.size();
			if (nbTokens == 1) {
				// hs.add(kw);
				oneTokenKw = kw;
			} else { // kw composé de plusieurs mots
				// (S1): verify whether all the tokens composing kw are in wordsInText
				boolean addIt = true;
				for (String kwItem : indexEntriesInKW) {
					// is-it in wordsInText ?
					// if kwItem is word itself, then yes, of course, it is in wordsInText
					if (kwItem.equals(word)) continue;
					// here, we scan the list of wordsInText
					// if they are sorted, we can do a binarysearch
					/*
					boolean inWords = false;
					for (int j = 0; j < nbWords; j++) {
						if (kwItem.equals(wordsInText.get(j))) {
							inWords = true;
							break;
						}
					}
					if (!inWords) {
						addIt = false;
						break;
					}
					*/
					int k = Collections.binarySearch(wordsInText, kwItem); // "wordsInText needs to be sorted"
					if (k < 0) { // kwItem not in wordsInText
						addIt = false;
						break;
					}
				} // for iToken
				if (addIt) {
					hs.add(kw);
					addOneTokenKw = false;
				}
			} // if nbTokens
		} // for ikws
		if ((oneTokenKw != null) && (addOneTokenKw)) {
			hs.add(oneTokenKw);
		}
	}
	return hs;
}

//
//
//

//@find C'EST A CAUSE DE CA QU'ON NE PEUT AVOIR PLUSIEURS LABELS POUR UN OBJET
// (c'est aussi à cause de ça)
/** for semanlink */
public List<ITEM> label2KeywordList(String kwLabel, Locale locale) {
	List<String> indexEntriesInLabel = this.indexEntryCalculator.indexEntries(kwLabel , locale);
	int n = indexEntriesInLabel.size();
	if (n == 0) return new ArrayList<ITEM>(0);
	String word = indexEntriesInLabel.get(0);
	List<ITEM> kws = word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new ArrayList<ITEM>(0);
	// we make a clone of kws (in order to not modify kws!)
	ArrayList<ITEM> alx = new ArrayList<ITEM>(kws.size());
	alx.addAll(kws);
	for (int iword = 1; iword < n; iword++) {
		word = indexEntriesInLabel.get(iword);
		kws = word2tagsHM.get(word);
		if (kws == null) return new ArrayList<ITEM>(0);
		for (int i = alx.size() - 1; i > -1; i--) {
			Object kw = alx.get(i);
			if (!(kws.contains(kw))) alx.remove(i);
		}
		if (alx.size() == 0) return new ArrayList<ITEM>(0);
	}
	// kws in alx contain all the words (with more than 2 letters) in kwLabel
	// They are OK, except if they also contain other words
	for (int i = alx.size() - 1; i > -1; i--) {
		ITEM kw = alx.get(i);
		String label = labelGetter.getLabel(kw);
		if ((this.indexEntryCalculator).indexEntries(label, locale).size() > n) alx.remove(i);
	}
	return alx;
}

}
