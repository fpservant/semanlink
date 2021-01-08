/* Created on Jan 7, 2021 */
package net.semanlink.semanlink;

import java.util.ArrayList;
import java.util.List;

import net.semanlink.util.YearMonthDay;

public class RecentDocsAndKws {
private SLModel mod;
private int nbOfDays; // beware !!! modified in getRecentDocs
private int minNbOfDocs;
private String dateProp;
private List<SLDocument> recentDocs; // use getter
private List<SLKeyword> recentTags; // use getter

/**
 * @param mod
 * @param nbOfDays
 * @param minNbOfDocs // but we won't try older than 1 year
 * @param dateProp
 */
public RecentDocsAndKws(SLModel mod, int nbOfDays, int minNbOfDocs, String dateProp) { // 2021-01
	this.mod = mod;
	this.nbOfDays = nbOfDays;
	this.minNbOfDocs = minNbOfDocs;
	this.dateProp = dateProp;
}

// beware, may be changed by getRecentDocs() (if not enough docs)
public int getNbOfDays() {
	return nbOfDays;
}

// peut s'optimiser (par ex en ne recalculant pas tout de YearMonthDay.daysAgo(i)
public List<SLDocument> getRecentDocs() throws Exception {
	if (recentDocs == null) {
//	List<SLDocument> x = getDocumentsList(dateProp, (new YearMonthDay()).getYearMonthDay("-"),null);
//	for (int i = 1; i < nbOfDays+1; i++) {
//		List<SLDocument> y = getDocumentsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"),null);
//		SLUtils.reverseSortByProperty(y, dateProp);
//		x.addAll(y);
//	}
		List<SLDocument> x = new ArrayList<>();
		for (int i = -1; i < nbOfDays+1; i++) { // -1 for tomorrow (avoid pbs when deploying to kattare just created docs
			List<SLDocument> y = mod.getDocumentsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"), null);
			SLUtils.reverseSortByProperty(y, dateProp);
			x.addAll(y);
		}
		
		// 2021-01: if not enough doc, go farther in the past
		// but no more than one year, to be sure to exit the loop
		for (int j = 0 ; j < 366 ; j++) {
			if (x.size() >= minNbOfDocs) break;
			this.nbOfDays++;
			List<SLDocument> y = mod.getDocumentsList(dateProp, (YearMonthDay.daysAgo(this.nbOfDays)).getYearMonthDay("-"), null);
			SLUtils.reverseSortByProperty(y, dateProp);
			x.addAll(y);
		}
		
		recentDocs = x;
	}
	return recentDocs;
}

// peut s'optimiser (par ex en ne recalculant pas tout de YearMonthDay.daysAgo(i)
/** ATTENTION, si on veut aussi recentDocs, commencer par recentDocs */
// (parce que recentDocs peut augmenter this.nbOfDays )
public List<SLKeyword> getRecentTags() throws Exception {
	if (recentTags == null) {
//	List<SLKeyword> x = getKeywordsList(dateProp, (new YearMonthDay()).getYearMonthDay("-"),null);
//	for (int i = 1; i < nbOfDays+1; i++) {
//		List<SLKeyword> y = getKeywordsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"), null);
//		SLUtils.reverseSortByProperty(y, dateProp);
//		x.addAll(y);
//	}
		List<SLKeyword> x = new ArrayList<>();
		for (int i = -1; i < nbOfDays+1; i++) { // -1 for tomorrow (avoid pbs when deploying to kattare just created docs
			List<SLKeyword> y = mod.getKeywordsList(dateProp, (YearMonthDay.daysAgo(i)).getYearMonthDay("-"),null);
			SLUtils.reverseSortByProperty(y, dateProp);
			x.addAll(y);
		}
		// Patch bug googlebot 2008-09
		/*for (int i = x.size()-1; i > -1; i--) {
			SLKeyword kw = (SLKeyword) x.get(i);
			List docs = kw.getDocuments();
			if ((docs == null) || (docs.size() == 0)) {
				x.remove(i);
			}
		}*/
		recentTags = x;
	}
	return recentTags;
}

}
