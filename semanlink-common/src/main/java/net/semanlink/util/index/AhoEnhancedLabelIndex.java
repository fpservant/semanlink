/* Created on 16 mai 2005 */
package net.semanlink.util.index;

import java.text.Collator;
import java.util.*;

import org.apache.jena.rdf.model.Resource;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;
import org.ahocorasick.trie.Trie;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 */
public class AhoEnhancedLabelIndex<E> extends LabelIndex<E> {
private Aho<E> aho;
Aho<E> getAho() { return this.aho ; }

// it looks like we're obliged to recompute the all trie
// after each update.
// We could (maybe) save the trieBuilder
static class Aho<E> {
	private CharConverter converter;
	private PayloadTrieBuilder<E> trieBuilder; // use getter
	private PayloadTrie<E> trie;
	
	Aho(CharConverter converter) {
		this.converter = converter;
	}
	
	CharConverter getConverter() { return this.converter ; }
	
	PayloadTrieBuilder<E> getTrieBuilder() {
		if (trieBuilder == null) {
			trieBuilder = PayloadTrie.builder();
			trieBuilder.ignoreOverlaps()
					// .ignoreCase()
					.onlyWholeWords();
					// .onlyWholeWordsWhiteSpaceSeparated(); // no probably cause the conevrter we use
		}
		return trieBuilder;
	}
	
	PayloadTrie<E> build() {
		trie = getTrieBuilder().build();
		return trie;
	}
}

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
public AhoEnhancedLabelIndex(Iterator<E> items, LabelGetter<E> labelGetter, CharConverter converter, Locale locale) throws Exception {
	this(labelGetter, converter, locale);
	try (Update<E> up = new Update<>(this, true)) {
		up.addIterator(items);
	}
}

// hum, converter for super and this not necessarily the same one.
// AND/OR not necessarily replacing spcaes by "_"
// TODO

public AhoEnhancedLabelIndex(LabelGetter<E> labelGetter, CharConverter converter, Locale locale) {
	super(labelGetter, newI18nFriendlyIndexEntries(converter), locale);
	this.aho = new Aho<>(converter);
}

//
// UPDATES
//

// to update the index, create an Update then add items to it.
// Update is an AutoClosable: structures are updated on close.

public Update<E> newUpdate(boolean initing) {
	return new AhoUpdate<>(this, initing);
}

public static class AhoUpdate<E> extends Update<E> {
	private Aho<E> aho;
	
	/**
	 * @param index
	 * @param initing true for an init, or a "big update", false otherwise
	 */
	AhoUpdate(AhoEnhancedLabelIndex<E> index, boolean initing) {
		super(index, initing);
		this.aho = index.getAho();
		// PayloadTrieBuilder<E> trieBuilder = index.getAho().getTrieBuilder();
	}
	
	@Override public void close() throws Exception {
		super.close();
	}
	
	@Override public void addLabel(E kw, String label, Locale locale) {
		super.addLabel(kw, label, locale);
		
		// todo optim (a lot already computed in super)
		Iterator<String> labs = index.labelGetter.getLabels(kw);
		for (;labs.hasNext();) {
			String lab = labs.next();
			// convert the labels to a normalized form
			lab = aho.getConverter().convert(lab);
			aho.getTrieBuilder().addKeyword(lab, kw);
		}

	}

	@Override public void removeLabel(E kw, String label, Locale locale) {
		super.removeLabel(kw, label, locale);
		
	}
} // class Update

///** return their normalized form */
//protected List<String> label2indexEntries(String label, Locale locale) {
//	return this.indexEntryCalculator.indexEntries(label, locale);
//}

/**
 *  Search the items containing all words in text. 
 *  <p>(search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)</p>
 */
// @Override // implements IndexInterface
public Set<ObjectLabelPair<E>> searchText(String text) {
	List<String> wordsInText = this.indexEntryCalculator.indexEntries(text, locale);
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
		if (indexEntriesInKW.size() > n) continue;
		Collections.sort(indexEntriesInKW);  // to test for equality of lists -- hum, is it a good idea to reorder?
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
