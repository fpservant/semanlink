/* Created on 9 avr. 2005 */
package net.semanlink.servlet;

import net.semanlink.semanlink.SLKeyword;
import java.util.List;

/**
 * liste de kws avec un nb d'occurrence pour chaque.
 */
public class Bean_KwList_Nb extends Bean_KwList {
// use getter
int maxNb = -1;
//use getter
int mean = 0;
public SLKeyword getSLKeyword(int i) {
	return ((SLKeywordNb) this.getList().get(i)).getKw();
}
public int getNb(int i) {
	return ((SLKeywordNb) this.getList().get(i)).getNb();
}
public int getMaxNb() {
	if (this.maxNb < 0) computeStats();
	return this.maxNb;
}
public int getMean() {
	if (this.mean == 0) computeStats();
	return this.mean;
}
private void computeStats() {
	List list = this.getList();
	int n = list.size();
	for (int i = 0; i < n; i++) {
		int k = ((SLKeywordNb) list.get(i)).getNb();
		if (k > maxNb) maxNb = k;
		mean += k;
	}
	mean = mean / n;
}
/** entre 1 et 5 */ // TODO
public String getNormalizedN(int i) {
	int nb = getNb(i);
	if (nb == 1) return "1";
	int maxN = getMaxNb();
	if (maxN <= 5) return Integer.toString(nb);
	maxN--;
	nb--;
	if (maxN < 21) {
		if (4*nb <= maxN) return "1";
		if (3*nb <= maxN) return "2";
		if (2*nb <= maxN) return "3";
		if (3*nb <= 2*maxN) return "4";
		return "5";
	} else if (maxN < 100) {
		if (10*nb <= maxN) return "1";
		if (5*nb <= maxN) return "2";
		if (4*nb <= maxN) return "3";
		if (3*nb <= maxN) return "4";
		if (2*nb <= 1*maxN) return "5";
		if (3*nb <= 2*maxN) return "6";
		return "7";
	} else {
		if (20*nb <= maxN) return "1";
		if (10*nb <= maxN) return "2";
		if (5*nb <= maxN) return "3";
		if (4*nb <= maxN) return "4";
		if (3*nb <= 1*maxN) return "5";
		if (2*nb <= 1*maxN) return "6";
		return "7";
	}
}
}
