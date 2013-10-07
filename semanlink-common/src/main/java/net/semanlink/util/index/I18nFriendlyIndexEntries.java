package net.semanlink.util.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Computes index entries in a normalized form useful for fast i18n friendly searches. 
 * <p>This class allows indeed to converts words of latin languages in a form
 * without accents nor diacritics.</p>
 */
public class I18nFriendlyIndexEntries implements IndexEntriesCalculator {
private WordsInString wordsInString;
private CharConverter converter;
/**
 * @param wordsInString dictates how we extract words from a String
 * @param converter to be used to convert labels to strings in a normalized form
 */
public I18nFriendlyIndexEntries(WordsInString wordsInString, CharConverter converter) {
	this.wordsInString = wordsInString;
	this.converter = converter;
}

public List<String> indexEntries(String s, Locale loc) {
	ArrayList<String> x = this.wordsInString.words(s, loc);
	for (int i = 0 ; i < x.size(); i++) {
		x.set(i, this.converter.convert(x.get(i)));
	}
	return x;
}
}
