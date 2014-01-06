package net.semanlink.util.text;
import java.util.*;
import java.text.*;

/**
 * Character conversion table, and implementation of the construction of such a table based on Collators, useful
 * for instance to create human/computer friendly URLs (without "%" chars) based on strings containing accented characters and white spaces.
 * 
 * <p>A character conversion table is a table that gives, for any given character c, the character
 * to replace c with, when performing the conversion of a String (method convert(String)).
 * (This class supports only one-to-one conversion: you cannot convert one char to a String
 * whose length is not 1)</p>
 * 
 * <p>How to use: create an instance, (or get one using one of the static methods provided),
 * and then call convert(String) to convert a String.</p>
 * 
 * <p>This class implements the construction of a table based on a Collator. 
 * Using java.text.Collator, you can indeed perform locale-sensitive String comparisons. 
 * For a well chosen Collator, you can, for instance, have the strings
 * "AéEàâç" and "aeeaac" be considered as equal. So, you can create a conversion table based
 * on that knowledge included in a Collator.</p>
 * 
 * @see CharConverter to get more functionalities
 * @author fps[at]semanlink.net
 */
public class SimpleCharConverter {
/** The conversion table: for each character, the char intended to replace it. 
 *  For instance, you may want to have conversion['é'] = 'e' */
protected char[] conversion;

/** Conversion of the characters of a string. 
 *  Characters outside the conversion table are left unchanged.
 *  @param s the string to be converted. */
public String convert(String s) {
	int n = s.length();
	int dim = conversion.length;
	StringBuffer sb = new StringBuffer(n);
	for (int i = 0; i < n; i++) {
	  char c = s.charAt(i);
	  if (c < dim) {
	  	sb.append(conversion[c]);
	  } else { // chars greater than dim are left unchanged
	  	sb.append(c);
	  }
	}
	return sb.toString();
}

/**
 * @param conversion defines, for each character (that is to say for each index) the character that replaces it: char index is to be replaced by conversion[index]. */
public SimpleCharConverter(char[] conversion) {
	this.conversion = conversion;
}

public static SimpleCharConverter collatorBasedConverter(Locale locale) {
	return new SimpleCharConverter(computeCollatorBasedConversionTable(256*256, locale));
}

/**  @param dim dimension of conversion table. Normally 256*256 */
public static SimpleCharConverter collatorBasedConverter(int dim, Locale locale) {
	return new SimpleCharConverter(computeCollatorBasedConversionTable(dim, locale));
}

/** Computes a conversion table in order to have, for instance, conversion['é'] = 'e'. 
 * @param dim normally 256*256, but we can limit ourselves to a shorter table (256 for instance)
 * @param locale if null, default Locale is used. */
public static char[] computeCollatorBasedConversionTable(int dim, Locale locale) {
	if (locale == null) locale = Locale.getDefault();
	Collator coll = Collator.getInstance(locale);
	coll.setStrength(Collator.PRIMARY);
	return computeCollatorBasedConversionTable(dim, coll);
} // computeCollatorBasedConversionTable

/** Computes the conversion table corresponding to a Collator. 
 *
 * When set to a strength of Collator.PRIMARY, a Collator considers as equal
 * variants of characters such as 'E', 'e', 'é' (e acute) , 'è' (e grave)...
 *
 * This method attempts to use this knowledge included in a Collator to compute a conversion table,
 * that says, for instance, that all different 'e' characters must be replaced by "e":
 * 'E' -> "e", 'é' -> "e", etc.
 * (more precisely, the computed table says that all different 'e' characters are
 * to be replaced by the character among them that is the first in the order defined by the Collator
 * when its strength is set to Collator.IDENTICAL. With Collators such as the one defined
 * for English, French and German, this happens to replace all 'e' characters by 'e'
 * (and not 'E', as one would expect from the ASCII order). 
 * Note that the algorithm used here would not be able to take into account rules of a RuleBasedCollator
 * such as "&Œ,OE" (a rule that would mean that "Œ" and "oe" can be considered as equal)
 * (But, BTW, java.text.Collator do not have such rules)
 * @param dim normally 256*256, but we can limit ourselves to a shorter table (256 for instance)
 * @param coll the Collator whose knowledge is to be used to compute the conversion table. Its
 * strength should normally be set to Collator.PRIMARY.
 */
public static char[] computeCollatorBasedConversionTable(int dim, Collator coll) {
	// We first compute a table of characters (as strings),
	// ordered in the order defined by the Collator
	// (with its strength set to Collator.IDENTICAL, to have a total order)
	// that it is to say something such as:
	// ..., 'd', 'D', 'e', 'E', 'é', 'É', 'è',...

	// table inited with character index at position index
	String[] s = new String[dim];
	char[] t = new char[1];
	for (int i = 0; i < dim; i++) {
	  t[0] = (char) (i);
	  s[i] = new String(t);
	}
	// we sort s in the order defined by the Collator,
	// (with its strength set to Collator.IDENTICAL)
	int strength = coll.getStrength();
	coll.setStrength(Collator.IDENTICAL);
	Arrays.sort(s, coll);
	coll.setStrength(strength); // set back coll to what it was
	
	char[] x = new char[dim];

	// Loop over s. Chars that are equal (wrt the Collator) are one after the other in s.
	// They all will be converted to the first encountered.
	String smallestAmongEqualChars = s[0];
	for (int i = 0; i < dim; i++) {
	  if (coll.compare(s[i], smallestAmongEqualChars) != 0) {
	  	smallestAmongEqualChars = s[i];
	  }
	  x[s[i].charAt(0)] = smallestAmongEqualChars.charAt(0);
	}
	return x;
} // computeSimpleConversionTable
}

