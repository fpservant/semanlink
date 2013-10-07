/* Created on 16 mai 2005 */
package net.semanlink.semanlink;

import java.util.*;

import net.semanlink.util.text.WordsInString;

/*
 * Contains both an HashMap (word -> kw list) and a sorted list of the words.
 * The sorted list of words allows to search for the beginning of a word
 * (When we say "word" here, we speak of a normalized text, as produced by CharConverter)
 */
/**
 * Index a thesaurus by text of words included in tags. 
 * 
 * Used in particular by the livesearch, and to extract tags from a text. 
 */
public class ThesaurusIndexOKold {
private SLModel mod;
private WordsInString wordsInString; // more than 2 letters only
/**
 * clés : les mots présents ds les short uri de kws (par ex homo)
 * data : les kws les contenant (par ex .../homo_sapiens). Attention, les alias sont présents,
 * mais non résolus. (see comments de getHashMap)*/
private HashMap word2tagsHM; // thesaurusIndex ds SLModel
/** List of words in tags (in normalzed form, cf CharConverter).
 *  Sorted by construction, used for binary search */
private ArrayList words;
/*
 * Keys: label of tags (in normalized form)
 * Data: corresponding tag - but there could be more than one, so, it must be a list (or : either a tag if there is only one, a list if there is more than one?)
 */
// private HashMap label2tagsHM;

//
// CONSTRUCTION AND UPDATES
//

ThesaurusIndexOKold(SLModel mod) {
	this.mod = mod;
	this.wordsInString = new WordsInString(true, true); // more than 2 letters only
	computeHashMap();
	this.words = new ArrayList(this.word2tagsHM.keySet());
	Collections.sort(words);
}

private void computeHashMap() {
	List kws = this.mod.getKWsInConceptsSpaceArrayList();	
	int n = kws.size();
	this.word2tagsHM = new HashMap(n);
	for (int i = 0; i < n; i++) {
		SLKeyword kw = (SLKeyword) kws.get(i);
		// add(kw.getURI(), kw, false);
		addKw(kw, false);
	}
	
	/* il faut aussi prendre les aliases // fait ci-dessus
	List aliases = new ArrayList();
	this.mod.aliasesIntoCollectionOfUris(aliases);
	n = aliases.size();
	for (int i = 0; i < n; i++) {
		String aliasUri = (String) aliases.get(i);
		SLKeyword alias = this.mod.getKeyword(aliasUri); // ceci ne résoud pas
		add(aliasUri, alias, false);
	}*/
}

/* @deprecated */
/*
private void computeHashMap_BasedOnUrisOfKws() {
	List kws = this.mod.getKWsInConceptsSpaceArrayList();	
	int n = kws.size();
	this.hm = new HashMap(n);
	for (int i = 0; i < n; i++) {
		SLKeyword kw = (SLKeyword) kws.get(i);
		add(kw.getURI(), kw, false);
	}
	
	// il faut aussi prendre les aliases // quoique je me demande s'ils ne sont pas déjà retournés plus haut (peut-être pas en tant que alias non résolus ? )
	List aliases = new ArrayList();
	this.mod.aliasesIntoCollectionOfUris(aliases);
	n = aliases.size();
	for (int i = 0; i < n; i++) {
		String aliasUri = (String) aliases.get(i);
		SLKeyword alias = this.mod.getKeyword(aliasUri); // ceci ne résoud pas
		add(aliasUri, alias, false);
	}
}
*/


public void addKw(SLKeyword kw) {
	addKw(kw, true);
}

// HMMM, NE PREND QU'UN SEUL LABEL POUR LE KW TODO

/**
 * 
 * @param kw
 * @param updateWords if true, this.words is updated, else not. False is used during construction, in order to avoid
 * sorting each time a kw is added to the hashmap: words are sorted only once, at the end.
 */
private void addKw(SLKeyword kw, boolean updateWords) {
	String label = kw.getLabel();
	Locale locale = Locale.getDefault();
	boolean needToSortWords = addLabel(kw, label, locale, updateWords); // todo locale
	
	List aliasUriList = this.mod.getAliasUriList(kw);
	if (aliasUriList != null) {
		for (int ial = 0; ial < aliasUriList.size(); ial++) {
			String aliasUri = (String) aliasUriList.get(ial);
			SLKeyword alias = this.mod.getKeyword(aliasUri);
			label = alias.getLabel();
			boolean b = addLabel(alias, label, locale, updateWords); // todo locale
			if (b) needToSortWords = true;
		}
	}
	if (needToSortWords) Collections.sort(this.words);
}

public void addKw(SLKeyword kw, String label, Locale locale) {
	boolean needToSortWords = addLabel(kw, label, locale, true);
	if (needToSortWords) Collections.sort(this.words);
}

/*
void addAlias(String aliasUri) {
	SLKeyword alias = this.mod.getKeyword(aliasUri); // ceci ne résoud pas
	add(aliasUri, alias, true);	
}
*/

/** Attention, s'il s'agit d'un alias, le kw doit être le pseudokw alias (non résolu) */
/*
void add(String uriText, SLKeyword kw) {
	add(uriText, kw, true);
}
*/

/** Attention, s'il s'agit d'un alias, le kw doit être le pseudokw alias (non résolu) */
/*private void add(String uriText, SLKeyword kw, boolean updateWords) {
	boolean needToSortWords = false;
	StringTokenizer st = kwUri2words(uriText);
	for (;st.hasMoreElements();) {
		String word = st.nextToken();
		boolean b = addWord(word, kw, updateWords);
		if (updateWords && b) needToSortWords = true;
	}
	if (needToSortWords) Collections.sort(this.words);
}*/

/**
 * modifies this.hm
 * if updateWords, modifies also this.words if needed but, beware, without sorting it
 * Returns true iff this.words has been modified (and therefore needs to be sorted) */
private boolean addLabel(SLKeyword kw, String label, Locale locale, boolean updateWords) {
	ArrayList wordsInLabel = (this.wordsInString).words(label, locale); // words > than 2 letters only
	boolean needToSortWords = false;
	for (int i = 0; i < wordsInLabel.size(); i++) {
		String word = (String) wordsInLabel.get(i);
		boolean b = addWord(word, kw, updateWords);
		if (b) needToSortWords = true;
	}
	return needToSortWords;
}

/** 
 *  add the key word with data kw to this.hm. 
 *  if updateWords, modifies this.words if needed but, beware, without sorting it
 *  Returns true iff this.words has been modified (and therefore needs to be sorted) (This can only happen when updateWords)*/
private boolean addWord(String word, SLKeyword kw, boolean updateWords) {
	word = this.mod.converter.convert(word);
	boolean needToSortWords = false;
	List list = (List) this.word2tagsHM.get(word);
	if (list == null) {
		list = new ArrayList(1);
		this.word2tagsHM.put(word, list);
		list.add(kw);
		if (updateWords) {
			this.words.add(word);
			needToSortWords = true;
		}
	} else {
		if (!list.contains(kw)) {
			list.add(kw);
		}
	}
	return needToSortWords;
}

/** BEWARE: only looks for the main label, doesn't take care of alias // to be changed when will switch to using several labels
 * instead of alias */
public void deleteKw(SLKeyword kw) {
	String label = kw.getLabel();
	Locale locale = Locale.getDefault(); // TODO
	removeKwLabel(kw, label, locale);
}

public void removeKwLabel(SLKeyword kw, String label, Locale locale) {
	ArrayList wordsInLabel = (this.wordsInString).words(label, locale);
	for (int i = 0; i < wordsInLabel.size(); i++) {
		String word = (String) wordsInLabel.get(i);
		word = this.mod.converter.convert(word);
		int k = Collections.binarySearch(this.words, word);
		if (k >= 0) {
			// word trouvé
			List kws = (List ) this.word2tagsHM.get(word);
			for (int j = 0; j < kws.size(); j++) {
				if (kws.get(j).equals(kw)) {
					kws.remove(j);
					break;
				}
			}
			if (kws.size() == 0) {
				this.word2tagsHM.remove(word);
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
HashMap getHashMap() {
	return this.word2tagsHM;
}

//
//
//

/** return their normalzed form */
String[] kw2words(SLKeyword kw, Locale locale) {
	String label = kw.getLabel();
	ArrayList words = (this.wordsInString).words(label, locale); // words > than 2 letters only
	return convert(words);
}

/** Return the normalized forms of words in words */
String[] convert(ArrayList words) {
	int nbWords = words.size();
	String[] x = new String[nbWords];
	for (int i = 0; i < nbWords ; i++) {
		x[i] = this.mod.converter.convert((String) words.get(i));
	}
	return x;
}

/*private StringTokenizer kwUri2words(String kwUri) {
	// String s = Util.getLastItem(kwUri, '#');// #thing
	String s = Util.getLastItem(kwUri, '/');// #thing // BEWARE: no / in kw short uri
	return new StringTokenizer(s, "_");
}*/

/**
 *  Search for tags containing all words in text, cf livesearch. 
 *  (search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)
 *  Beware, the result list also contains aliases that are not resolved. Not sorted. */
public List search(String text) {
	// 1er boolean false : permettrait de garder les mots de moins de 3 lettres (serait mieux : ne le garder que pour le dernier (en cours saisie)
	// mais pose des pbs : ex recherche de "Afrique de" ne donne rien, alors
	// qu'il y a "afrique de l'est" ds la liste
	ArrayList wordsInText = (this.wordsInString).words(text, Locale.getDefault());
	int nbWords = wordsInText.size();
	if (nbWords == 0) return new ArrayList(0);
	// we search the first word of words first, and then intersect this list with the result of the search of the other words.
	ArrayList list0 = searchWord((String) wordsInText.get(0));
	if (nbWords == 1) return list0;
	HashSet hs = null;
	ArrayList alx = list0;
	for (int i = 1; i < nbWords; i++) {
		hs = new HashSet(alx);
		ArrayList list = searchWord((String) wordsInText.get(i));
		alx = new ArrayList(hs.size()); // to compute intersection of x and list
		for (int j = 0; j < list.size(); j++) {
			Object o = list.get(j);
			if (hs.contains(o)) alx.add(o);
		}
		if (alx.size() == 0) return new ArrayList(0);
	}
	return alx;
}



/**
 * @param word supposé sous forme convertie (minuscule, sans diacritique, etc)
 * @return une arrayList de SLKeyword (attention, parce que c'est comme ça ds hm,
 * il y a des alias non résolus - pour le moment, tant pis. D'ailleurs pas si mal :
 * on affiche dans la liste résultat pour "j" à la fois "javascript" et "js")
 * @return une liste (that may contains doubles)
 */
public ArrayList searchWord(String word) {
	word = this.mod.converter.convert(word);
	// il faudrait peut-être prendre un set car on peut avoir des doubles (rarement)
	ArrayList x = new ArrayList();
	int k = Collections.binarySearch(this.words, word);
	if (k >= 0) {
		// word trouvé
		List kws = (List ) word2tagsHM.get(word);
		x.addAll(kws);
		k++;
	} else {
		k = -1*k -1;
	}
	int n = this.words.size();
	while (k < n) {
		String nextWord = (String) this.words.get(k);
		if (nextWord.startsWith(word)) {
			List kws = (List ) word2tagsHM.get(nextWord);
			x.addAll(kws);
			k++;			
		} else {
			break;
		}
	}
	return x;
} // searchWord


/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */
public SLKeyword[] getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	// ne s'occupe pas des doubles ? // TODO
	HashSet hs = new HashSet();
	// word in text
	ArrayList wordsInText = (this.wordsInString).words(text, locale);
	int nbWords = wordsInText.size();
	ArrayList convertedWordsInText = new ArrayList(nbWords);
	for (int i = 0; i < nbWords ; i++) {
		convertedWordsInText.add(this.mod.converter.convert((String) wordsInText.get(i)));
	}
	wordsInText = convertedWordsInText;
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
	for (int i = 0; i < nbWords ; i++) {
		String word = (String) wordsInText.get(i);
		/*// ne recherche que les kws constitués d'un seul mot
		String uri = this.kwLabel2Uri(word, thesaurusUri, locale);
		if (kwExists(uri)) x.add(uri);*/
		
		// les kws contenant word
		List kws = (List ) this.word2tagsHM.get(word);
		
		if (kws == null) continue;
		if (thesaurusUri != null) {
			// supprimer de la liste les kws trouvés qui ne sont pas de ce thesaurus
			// Ce qui implique de cloner la liste pour éviter les effets de bord
			ArrayList al = new ArrayList(kws.size());
			for (int j = 0; j < kws.size(); j++) {
				SLKeyword kw = (SLKeyword) kws.get(j);
				if (kw.getURI().startsWith(thesaurusUri)) {
					al.add(kw);
				}
			}
			kws = al;
		}
		
		// remarquons qu'un kw d'un seul mot est a priori bon à prendre,
		// mais attention, pour éviter de mettre à la fois, par ex pour le texte "C2G rend visite à Nissan",
		// à la fois c2g, nissan et c2g_nissan, on ne retiendra en définitive un tel kw que
		// si on ne retient pas de kw de plusieurs mots le contenant, d'où :
		SLKeyword oneTokenKw = null; // éventuel kw à un seul token (supposé unique ds kws : ne serait
		// pas le cas uniquement si même short kw ds 2 thesaurus différents)
		boolean addOneTokenKw = true; // a priori, si oneTokenKw != null, on le mettra, si on ne met pas de kw plus long
		for (int ikw = 0; ikw < kws.size(); ikw++) {
			SLKeyword kw = (SLKeyword) kws.get(ikw);
			// (S2):
			// hs.add(kw);
			// (S1)
			// all the words of kw are in wordsInText?

			// si un des éléments est un kw à un seul token, celui est a priori à prendre, (cf plus haut)
			// mais seulement si on ne met pas de kw à plusiers mots le contenant
			String[] normalizedWordsInKw = kw2words(kw, locale);
			int nbTokens = normalizedWordsInKw.length;
			if (nbTokens == 1) {
				// hs.add(kw);
				oneTokenKw = kw;
			} else { // kw composé de plusieurs mots
				// (S1): verify whether all the tokens composing kw are in wordsInText
				boolean addIt = true;
				for (int iToken = 0; iToken < nbTokens; iToken++) {
					String kwItem = normalizedWordsInKw[iToken];
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
					// Beware, kw can be an alias
					// therefore, don't do:
					// hs.add(kw);
					// but:
					hs.add(this.mod.resolveAlias(kw.getURI()));
					addOneTokenKw = false;
				}
			} // if nbTokens
		} // for ikws
		if ((oneTokenKw != null) && (addOneTokenKw)) {
			// Beware, oneTokenKw can be an alias
			// therefore, don't do:
			// hs.add(kw);
			// but:
			hs.add(this.mod.resolveAlias(oneTokenKw.getURI()));
		}
	}
	SLKeyword[] x = new SLKeyword[hs.size()];
	hs.toArray(x);
	return x;
}

//
//
//

public SLKeyword[] label2Keyword(String kwLabel, Locale locale) {
	ArrayList wordsInLabelAl = (this.wordsInString).words(kwLabel , locale);
	String[] normalizedWordsInLabel = convert(wordsInLabelAl);
	int n = normalizedWordsInLabel.length;
	if (n == 0) return new SLKeyword[0];
	String word = normalizedWordsInLabel[0];
	ArrayList kws = (ArrayList) word2tagsHM.get(word);
	if ((kws == null) || (kws.size() == 0)) return new SLKeyword[0];
	// we make a clone of kws (in order to not modify kws!)
	ArrayList alx = new ArrayList(kws.size());
	for (int i = 0; i < kws.size(); i++) {
		alx.add(kws.get(i));
	}
	for (int iword = 1; iword < n; iword++) {
		word = normalizedWordsInLabel[iword];
		kws = (ArrayList) word2tagsHM.get(word);
		if (kws == null) return new SLKeyword[0];
		for (int i = alx.size() - 1; i > -1; i--) {
			Object kw = alx.get(i);
			if (!(kws.contains(kw))) alx.remove(i);
		}
		if (alx.size() == 0) return new SLKeyword[0];
	}
	// kws in alx contain all the words (with more than 2 letters) in kwLabel
	// They are OK, except if they also contain other words
	for (int i = alx.size() - 1; i > -1; i--) {
		SLKeyword kw = (SLKeyword) alx.get(i);
		String label = kw.getLabel();
		if ((this.wordsInString).words(label , locale).size() > n) alx.remove(i);
	}
	SLKeyword[] x = new SLKeyword[alx.size()];
	alx.toArray(x);
	return x;
}

}
