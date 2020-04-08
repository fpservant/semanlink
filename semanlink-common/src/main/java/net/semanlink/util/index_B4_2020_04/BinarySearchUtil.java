package net.semanlink.util.index_B4_2020_04;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BinarySearchUtil {
private BinarySearchUtil() {}
/**
 * Returns the list of the strings in sortedStrings which starts with startOfString
 * @param limit if > 0, max size of the returned list*/
static public List<String> search(List<String> sortedStrings, String startOfString, int limit) {
	ArrayList<String> x;
	if (limit > 0) x = new ArrayList<String>(limit);
	else x = new ArrayList<String>(64);
	int count = 0;
	int k = Collections.binarySearch(sortedStrings, startOfString);
	if (k >= 0) { // startOfString found
		x.add(sortedStrings.get(k));
		count++;
		k++;
	} else { // not found
		k = -1*k -1;
	}
	int n = sortedStrings.size();
	while (k < n) {
		if (limit > 0) {
			if (count >= limit) break;
		}
		String nextString = sortedStrings.get(k);
		if (nextString.startsWith(startOfString)) {
			x.add(nextString);
			count++;
			k++;			
		} else {
			break;
		}
	}
	return x;
}
}
