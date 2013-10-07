/* Created on 22 sept. 07 */
package net.semanlink.semanticsemanticweb;

import java.util.ArrayList;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Model;

import net.semanlink.util.text.WordsInString;

// used also by tagOutsideLinks.jsp

/** RSimple attempt to try to represents the way dbPedia mints URI. */
public class DBPedia {
public static final String NS = "http://dbpedia.org/resource/";
public DBPedia() {}

public String getResourceURI(String tagLabel, String lang) {
	Locale locale = null;
	if (lang != null) {
		if (lang.length() == 2) {
			lang = lang.toLowerCase();
			locale = new Locale(lang);
		} else {
			lang = null;
		}
	}
	if (locale == null)	locale = Locale.getDefault();

	/** the words in tagLabel. */
	WordsInString wordsInString = new WordsInString(false, true);
	ArrayList words = wordsInString.words(tagLabel, locale);

	return getResourceURI(words, lang);
}

public String getResourceURI(ArrayList words, String lang) {
	if (("en".equals(lang)) || (lang == null)) {
		return NS + getEnResourceShortURI(words);
	} else {
		 throw new RuntimeException(lang + " not supported yet");
	}
}

// 	en.wikipedia.org/wiki/United_Kingdom

// TODO : traiter les accents. Ce n'est pas une question de Converter

String getEnResourceShortURI(ArrayList words) {
	int nbWords = words.size();
	if (nbWords > 0) {
		String word = (String) words.get(0);
		word = word.substring(0,1).toUpperCase() + word.substring(1);
		
		if (nbWords == 1) return word;
		
		StringBuffer sb = new StringBuffer(word);
		for (int i = 1 ; i < nbWords; i++) {
			word = (String) words.get(i);
			word = word.substring(0,1).toUpperCase() + word.substring(1);
			sb.append("_");
			sb.append(word);
		}
		return sb.toString();
	} else {
		throw new IllegalArgumentException("No words in tagLabel");
	}
}

//
// 2008-09 NEW
// (completely independent of what was done before
// we'll try here to return a dpPedia URI "matching" a string or a NIR passed as argument)
//
/*
Model tryToRecognize(String nirURI) {
	
}
*/
}
