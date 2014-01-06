package net.semanlink.util.text;
import java.util.Locale;

import net.semanlink.util.URLUTF8Encoder;


/**
 * To be used to replace characters in strings using a conversion table, built on the knowledge included in a Collator. 
 * 
 * <p>To SimpleCharConverter, adds the possibility to replace a char by a String (instead of just one char)
 * (in order to be able, for instance, to replace "Œ" by "oe". Note that this doesn't seem to be included in the Collator,
 * and must be added to the definition of the table. Such missing knowldge has been
 * added for French and German).</p>
 * 
 * <p>Two methods are provided: convert(String) and urlConvert(String).</p>
 * 
 * <p>Defines a set of "weird chars" to be replaced by a given String ("weirdCharSubstitution").
 * Are considered "weird" all ASCII-127 chars that are not converted to an ASCII-127 letter or digit by SimpleCharConverter.
 * (white space, in particular, are considered weird).
 * To change that definition, subclass and override isWeird(char).</p>
 * 
 * <p>How to use : create an instance, and call convert(String) to convert a String, or
 * use urlConvert(String) to ensure that the result can be used as an URL component.</p>
 * 
 * <p>As there is a relatively heavy computing of the conversion table in the constructor,
 * it is better to keep such a converter once created, and use it several times.<p>
 *
 * @see SimpleCharConverter
 * @author fps[at]semanlink.net
 */
public class CharConverter {
private static int MAX_DIM = 256*256;
private static final String WEIRD_CHAR_DEFAULT_SUBSTITUTION = "_";
private static char MAX_ASCII = 122; // z, in fact
private boolean weirdCharNeedToBeURLEncoded;

/** A character, and the String to replace it. */
public static class CharConversion {
	char c;
	String s;
	CharConversion(char c, String s) {
		this.c = c;
		this.s = s;
	}
}

// Better not to use characters (such as 'Æ', '©', etc...), or we can get problems here, I don't understand why. Probably a problem with Eclipse
/** To perform some conversions that usual Collators do not allow. */
private static CharConversion[] MORE_CONVERSIONS = {
		new CharConversion((char) 338, "oe") // Œ (OE)
		,new CharConversion((char) 339, "oe") // œ (oe)
		,new CharConversion((char) 198, "ae") // Æ (AE)
		,new CharConversion((char) 230, "ae") // æ (eclipse on my mac displays 'oe', but it is 'ae')
		,new CharConversion((char) 8482, "tm")
		,new CharConversion((char) 169,"c") // ©
};
/** to be added to MORE_CONVERSIONS. */
private static CharConversion[] GERMAN_CONVERSIONS = {
	new CharConversion((char) 228, "ae") // ä (a Umlaut)
	,new CharConversion((char) 196, "ae") // Ä (A Umlaut)
	,new CharConversion((char) 246, "oe") // ö (o Umlaut)
	,new CharConversion((char) 214, "oe") // Ö (O Umlaut)
	,new CharConversion((char) 252, "ue") // ü (u Umlaut)
	,new CharConversion((char) 220, "ue") // Ü (U Umlaut)
};
private static CharConversion[] germanConversions() {
	CharConversion[] x = new CharConversion[MORE_CONVERSIONS.length + GERMAN_CONVERSIONS.length];
	for (int i = 0; i < MORE_CONVERSIONS.length; i++) {
		x[i] = MORE_CONVERSIONS[i];
	}
	for (int i = 0, j = MORE_CONVERSIONS.length; i < GERMAN_CONVERSIONS.length; i++,j++) {
		x[j] = GERMAN_CONVERSIONS[i];
	}
	return x;
}
/** Default value for param moreConversions of constructor. */
private static CharConversion[] moreConversions(Locale locale) {
	if ("de".equals(locale.getLanguage())) {
		return germanConversions();
	}
	return MORE_CONVERSIONS;
}

/** String to be used to replace weird chars. */
private String weirdCharSubstitution;
/** the conversion table. */
private String conversion[];

//
// CONSTRUCTORS
//

public CharConverter(Locale locale) {
	this(locale, WEIRD_CHAR_DEFAULT_SUBSTITUTION);
}

public CharConverter(Locale locale, String weirdCharSubstitution) {
	this(locale, weirdCharSubstitution, moreConversions(locale));
}

/**
 *  @param locale
 *  @param weirdCharSubstitution the String used to replace "weird" chars (weird chars are defined by the isWeird(char) method)
 *  @param moreConversions can be used to define char conversions that are not defined by SimpleCharConverter.computeCollatorBasedConversionTable(dim, locale)
 *  (for instance, java.text.Collator do not know that "œ" could be replaced by "oe")
 */
public  CharConverter(Locale locale, String weirdCharSubstitution, CharConversion[] moreConversions) {
	char[] c = SimpleCharConverter.computeCollatorBasedConversionTable(MAX_DIM, locale);
	this.weirdCharSubstitution = weirdCharSubstitution;
	char[] t = new char[1];
	String[] x = new String[c.length];
	for (int i = 0; i < c.length; i++) {
		char cc = c[i];
		if (isWeird(cc)) {
			x[i] = this.weirdCharSubstitution;
		} else {
			t[0] = cc;
			String s = new String(t);
			if (s.equals(this.weirdCharSubstitution)) s = this.weirdCharSubstitution; // in order to be allowed to use "==" to test for equality with weirdCharSubstitution in convert method
			x[i] = s;
		}
	}
	if (moreConversions != null) {
		for (int i = 0; i < moreConversions.length; i++) {
			CharConversion conv = moreConversions[i];
			if (conv.c < MAX_DIM){
				if (conv.s.equals(weirdCharSubstitution)) {
					x[conv.c] = weirdCharSubstitution; // in order to be allowed to use "==" to test for equality with weirdCharSubstitution in convert method
				} else {
					x[conv.c] = conv.s;						
				}
			} else {
				throw new IllegalArgumentException("Character converted to value higher than " + MAX_DIM);
			}
		}
	}
	this.conversion = x;
	// we compute whether weirdCharSubstitution needs to be URLEncoded
	for (int i = 0; i < weirdCharSubstitution.length(); i++) {
		if (weirdCharSubstitution.charAt(i) > MAX_ASCII) {
			this.weirdCharNeedToBeURLEncoded = true;
			break;
		}
	}
}

/** Defines the characters to be replaced by weirdCharSubstitution. 
 *  all chars < 'z' that are not converted to an ASCII-127 letter or digit by SimpleCharConverter
 *  (white space, in particular, are considered weird)
 *  @param c : character as it is once converted by the corresponding SimpleCharConverter
 */
protected boolean isWeird(char c) {
	if (c < 48) return true; // before '0'
	// if (c > 122) return true; // after 'z' // changed 2007/09/19 : all greek characters would be weird! 
	if ((c > 57) && (c < 65)) return true; // between '9' and 'A'
	if ((c > 90) && (c < 97)) return true; // between 'Z and 'a'
	return false;
}

/** Converts a String based on the conversion table. */
public String convert(String s) {
	StringBuilder sb = new StringBuilder();
	String prev = null;
	for (int i = 0; i < s.length(); i++) {
		if (prev != this.weirdCharSubstitution) {
			prev = conversion[s.charAt(i)];
			sb.append(prev);
		} else { // we don't add weirdCharSubstitution twice in raw
			prev = conversion[s.charAt(i)];
			if (prev != this.weirdCharSubstitution) { // we took care to use this.weirdCharSubstitution in conversion (and not a String equal to it)
				sb.append(prev);
			}
		}
	} // for
	return sb.toString();
}

/** Converts using the conversion table, ensuring the result is URL encoded if needed. 
 *  <p>Note: This is not the same as URLUTF8Encoder.encode(convert(s)). 
 *  If the table says that a character c is to be converted to a character outside ASCII 127, it is NOT converted to that character,
 *  but it is converted to the URL-encoded form of c. (In any case, we have to URL-encode here. If you want to transform
 *  c to the URL-encoded form of its replacement in the table, use URLUTF8Encoder.encode(convert(s)))</p>
 *  <p>This behavior is intended to accomodate non latin alphabets. 
 *  As the conversion table is computed automatically based on Collators,
 *	this can ends up creating a form that does not respect the language.
 *	For instance, when the table is computed with a greek Locale,
 *	"Λεωνίδας" (Leonidas) gets converted to "λεωνίδασ", which is not correct Greek.
 *	Both "ς" and "σ" correspond to the latin "S". "ς" is used anywhere within words, but not at the end of a word:
 *	"σ" is used at the end of words (and only there). For instance, σωκράτης is Sokratis.
 */
public String urlConvert(String s) {
	StringBuilder sb = new StringBuilder();
	String prev = null;
	String next;
	boolean urlEncodingNeeded = false; // will be set to true if URLUTF8Encoder.encode need to be called on result
	for (int i = 0; i < s.length(); i++) {
		next = conversion[s.charAt(i)];
		if (next == weirdCharSubstitution) { // we took care to use this.weirdCharSubstitution in conversion (and not a String equal to it)
			if (!urlEncodingNeeded) urlEncodingNeeded = weirdCharNeedToBeURLEncoded;
			if (prev != weirdCharSubstitution) { // we don't add weirdCharSubstitution twice in raw
				sb.append(weirdCharSubstitution);
				prev = next;
			} // else prev == next == weirdCharSubstitution
		} else { // justAddedWeirdChar == false
			// we don't convert if conversion yields to a char higher than ASCII 127
			// This wouldn't make sense, (because the char will have to be URLEncoded anyway)
			// and, as the conversion table is computed automatically based on Collators,
			// this can ends up creating a form that do not respct the language.
			boolean dontConvert = false;
			for (int j = 0; j < next.length(); j++) {
				if (next.charAt(j) > MAX_ASCII) {
					dontConvert = true;
					break;
				}
			}
			if (dontConvert) {
				// we don't convert, that is, we keep the original char.
				sb.append(s.charAt(i)); // could it be weird char? Probably not. Nevermind: would not be that bad if we end up putting it twice in raw
				// but most probably, URL encoding will be needed
				// (we don't compute it right now, to avoid multiplying calls to URLUTF8Encoder.encode for single chars
				urlEncodingNeeded = true;
				// I don't want to document the string prev, because it is costly for nothing (would be useful
				// only if the char is weirdCharSubstitution
				prev = null;
			} else {
				sb.append(next);
				prev = next;
			}
		}
	} // for
	if (urlEncodingNeeded) {
		// the only difference is about char 126 (~)
		// return java.net.URLEncoder.encode(sb.toString(),"UTF-8");
		return URLUTF8Encoder.encode(sb.toString());
	} else {
		return sb.toString();
	}
}

} // class CharConverter


