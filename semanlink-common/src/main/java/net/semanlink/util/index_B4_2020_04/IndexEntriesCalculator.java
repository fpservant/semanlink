package net.semanlink.util.index_B4_2020_04;

import java.util.List;
import java.util.Locale;

/**
 * Defines how we get from a String the entries that should be added to the Index. 
 * <p>Used in the definition of class Index, where we typically want to index items by 
 * a normalized form of the words contained in their labels.</p>
 */
public interface IndexEntriesCalculator {
	/**
	 * Returns for a given String the list of entries that should be added to the index. 
	 * Typically the list of words in s, in a normalized form. */
	public List<String> indexEntries(String s, Locale loc);
}
