package net.semanlink.util.index;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BinarySearchUtil {
/**
 * Returns the list of the strings in sortedStrings which starts with startOfString
 * @param limit if > 0, max size of the returned list*/
static public ArrayList<String> search(List<String> sortedStrings, String startOfString, int limit) {
	ArrayList<String> x;
	if (limit > 0) x = new ArrayList<String>(limit);
	else x = new ArrayList<String>(64);
	int count = 0;
	int k = Collections.binarySearch(sortedStrings, startOfString);
	if (k >= 0) {
		// startOfString found
		x.add(sortedStrings.get(k));
		count++;
		k++;
	} else {
		k = -1*k -1;
	}
	int n = sortedStrings.size();
	while (k < n) {
		if (limit > 0) {
			if (count >= limit) break;
		}
		String nextCode = sortedStrings.get(k);
		if (nextCode.startsWith(startOfString)) {
			x.add(nextCode);
			count++;
			k++;			
		} else {
			break;
		}
	}
	return x;
} // search

/**
 * Returns the list of the strings in sortedStrings which starts with startOfString
 * @param limit if > 0, max size of the returned list*/
static public ArrayList<String> search(String[] sortedStrings, String startOfString, int limit) {
	return search(Arrays.asList(sortedStrings), startOfString, limit);
}
}
