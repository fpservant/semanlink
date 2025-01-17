package net.semanlink.util.text;
import java.util.*;
import java.text.*;
//
// DECOUPER UNE CHAINE EN MOTS
//

import net.semanlink.util.Util;

/*
 *  java retourne 1 seul mot pour "aaa.html" (no patch ?)
 * 
 *  Exactement le contraire de ce qu'il faudrait : le breakIterator donne
 *  C2G-Ediris : 1 seul mot (-> c2g_ediris, car - converti en _ par convert) // ca, on pourrait vivre avec
 *  2001-12-04 : 3 mots
 *  d'ou le remplacement de "-" par " " -> on a 2 mots pour c2g-ediris.
 *  // j'ai arrete ca 2006/12: Pour les dates, le pb est résolu car on ne trie pas si donnerait un numérique en tête (autre sol aurait été de passer en upperCase) 
 */
/** 
 *  Classe pour découper une chaine en mots.
 *  <p>Une heuristique simple est employée pour distinguer entre ce qui est mot et ce qui ne l'est
 *  pas dans les éléments retournés par un BreakIterator.getWordInstance : est considéré comme
 *  un mot tout retour contenant au moins une lettre ou un chiffre.</p>
 *  <p>Attention, java retourne un seul mot pour, par exemple : "l'école" -> mettre patchApostrophe à true pour l'éviter</p>
*/
public class WordsInString {

// ATTRIBUTS PARAMETRANT "L'ALGORITHME"
private boolean moreThan2LettersOnly = true;
private boolean patchApostrophe = true;
//

/**
 * @param moreThan2LettersOnly if true, words less than 3-chars long are not indexed (more precisely, are indexed words with at least 3 chars,
 * or containing a digit, or written in uppercase. Then if an item of the thesaurus doesn't contain any such word, it is indexed by what it has)
 * @param patchApostrophe if true, "l'école" will be indexed by "école"
 */
public WordsInString(boolean moreThan2LettersOnly, boolean patchApostrophe) {
	this.moreThan2LettersOnly = moreThan2LettersOnly;
	this.patchApostrophe = patchApostrophe;
}

/** The list of words in a given String. */
public ArrayList<String> words(String source, Locale locale) {
	if (locale == null) locale = Locale.getDefault();
	ArrayList<String> x = computeWords(source, locale, this.moreThan2LettersOnly, this.patchApostrophe);
	/*System.out.print("WordsInString " + source + " : ") ;
	for(String s : x) {
		System.out.print(s + " ; ");
	}
	System.out.println();*/
	return x;
}


static private ArrayList<String> computeWords(String source, Locale locale, boolean moreThan2LettersOnly, boolean patchApostrophe) {
	ArrayList<String> x = new ArrayList<String>();
	if (source == null) { // 2025-01 added
		return x;
	}
	if (patchApostrophe) {
		source = source.replaceAll("'", " ");
		source = source.replaceAll("-", " ");
	}
	BreakIterator  breaker  = BreakIterator.getWordInstance (locale);

	breaker.setText(source); 
	int start = breaker.first();
	for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
		String s = (source.substring(start,end));
		int n = s.length();
		/*if ( (n < 3) && (moreThan2LettersOnly) ) continue;
		// attention, un BreakIterator retourne aussi ce qui se trouve entre les mots. D'où
		// une heuristique simple pour distinguer ce qui est un mot de ce qui ne l'est pas
		for (int p = 0; p < n; p++) {
			if (Character.isLetterOrDigit(s.charAt(p))) {
				if (converter != null) s = converter.convert(s);
				x.add(s);
				break;
			}
		}*/
		// attention, un BreakIterator retourne aussi ce qui se trouve entre les mots. D'où
		// une heuristique simple pour distinguer ce qui est un mot de ce qui ne l'est pas
		boolean itsAWord = false;
		for (int p = 0; p < n; p++) {
			if (Character.isLetterOrDigit(s.charAt(p))) {
				itsAWord = true;
				break;
			}
		}
		if (itsAWord) {
			boolean keepIt = false;
			if (moreThan2LettersOnly) {
				// on le garde s'il fait plus de trois lettres, ou s'il contient un chiffre, ou en majuscules
				if (n > 2) {
					keepIt = true;
				} else {
					for (int p = 0; p < n; p++) {
						if (Character.isDigit(s.charAt(p))) {
							keepIt = true;
							break;
						}
					}
					if (!keepIt) {
						// on le garde quand même s'il est tout en majuscule
						keepIt = true;
						for (int p = 0; p < n; p++) {
							if (!(Character.isUpperCase(s.charAt(p)))) {
								keepIt = false;
								break;
							}							
						}
					}
				}
			} else {
				keepIt = true;
			}
			if (keepIt) {
				x.add(s);
			}
		}
	}
	// si rien de plus de 2 lettres, on prend ce qu'il y a
	if (x.size() == 0) {
		if (moreThan2LettersOnly) {
			x = computeWords(source, locale, false, false);
		}
	}
	return x;
}

/** Retourne les mots dans un nom de fichier. */
public ArrayList<String> wordsInFilename(String source, Locale locale) {
	return words(Util.getWithoutExtension(source), locale);
}

//
// LE CODE AU DESSUS PROVIENT DES METHODES STATIQUES SUIVANTES
//

/** Retourne les mots de source.
 *  Une heuristique simple est employée pour distinguer entre ce qui est mot et ce qui ne l'est
 *  pas dans les éléments retournés par un BreakIterator.getWordInstance : est considéré comme
 *  un mot tout retour contenant au moins une lettre.
 *  Attention, java retourne un seul mot pour, par exemple : l'école
 *  Et aussi pour aaa.html
 */
static ArrayList<String> words1(String source, Locale locale) {
	BreakIterator  breaker  = BreakIterator.getWordInstance (locale);
	ArrayList<String> x = new ArrayList<String>();
	breaker.setText(source); 
	int start = breaker.first();
	for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
		String s = (source.substring(start,end));
		for (int p = 0; p < s.length(); p++) {
			if (Character.isLetter(s.charAt(p))) {
				x.add(s);
				break;
			}
		}
	}
	return x;
}
/** Par rapport à getWords, élimine les mots de 2 lettres ou moins et tente
 * de résoudre le problème genre l'école.
 */
static ArrayList<String> words2(String source, Locale locale) {
	BreakIterator  breaker  = BreakIterator.getWordInstance (locale);
	ArrayList<String> x = new ArrayList<String>();
	source = source.replaceAll("'", " ");
	breaker.setText(source); 
	int start = breaker.first();
	for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
		String s = (source.substring(start,end));
		int n = s.length();
		if (n < 3) continue;
		for (int p = 0; p < n; p++) {
			if (Character.isLetter(s.charAt(p))) {
				x.add(s);
				break;
			}
		}
	}
	return x;
}

/** ce qui était utilisé jusqu'à mai 2005 */
ArrayList<String> words200405(String source, Locale locale) {
	if (locale == null) locale = Locale.getDefault();
	BreakIterator  breaker  = BreakIterator.getWordInstance (locale);
	ArrayList<String> x = new ArrayList<String>();
	if (patchApostrophe) source = source.replaceAll("'", " ");
	breaker.setText(source); 
	int start = breaker.first();
	for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker.next()) {
		String s = (source.substring(start,end));
		int n = s.length();
		if ( (n < 3) && (this.moreThan2LettersOnly) ) continue;
		// attention, un BreakIterator retourne aussi ce qui se trouve entre les mots. D'où
		// une heuristique simple pour distinguer ce qui est un mot de ce qui ne l'est pas
		for (int p = 0; p < n; p++) {
			if (Character.isLetter(s.charAt(p))) {
				x.add(s);
				break;
			}
		}
	}
	Collections.sort(x);
	return x;
}

/** Retourne les mots de plus de 2 lettres d'un nom de fichier. */
static ArrayList<String> wordsInFilename2(String source, Locale locale) {
	return words2(Util.getWithoutExtension(source), locale);
}
}
