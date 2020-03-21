/* Created on 17 mai 2005 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.ThesaurusIndex;
import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.util.index.ObjectLabelPair;

/**
 * Created by Action_LiveSearch, to document the result tree list, cf /jsp/livetreesons.js
 * (could also be used for a "search.jsp" page, for a non livesearch search)
 */
public class Jsp_Search extends Jsp_Page {
private String searchString;
/** BEWARE, the list contains aliases that are not resolved. */
private List<SLKeyword> kws;
public Jsp_Search(String searchString, HttpServletRequest request) {
	super(request);
	this.searchString = searchString;
	SLModel mod = SLServlet.getSLModel();

	// 2020-03-21 now that we use an index based on ObjectLabelPair,
	// we could display the found label in search results,
	// instead of just the skos:prefered label of the kw
	ThesaurusIndex thind = mod.getThesaurusIndex();
	Set<ObjectLabelPair<SLKeyword>> set = thind.searchText(searchString);
	
	ArrayList<ObjectLabelPair<SLKeyword>> pairs = new ArrayList<>(set.size());
	pairs.addAll(set);

	// 2020-03 exact or close matches first in search results
	int n = nbOfWords(searchString);
	ObjectLabelPair<SLKeyword> exactMatch = null;
	List<ObjectLabelPair<SLKeyword>> closeMatchs = new ArrayList<>();
	List<ObjectLabelPair<SLKeyword>> otherMatchs = new ArrayList<>();

	for (ObjectLabelPair<SLKeyword> pair : pairs) {
		String s = pair.getLabel();
		if ((exactMatch == null) && (thind.compareString(s, searchString) == 0)) {
				exactMatch = pair;
		} else {
			int n2 = nbOfWords(s);
			if (n == n2) {
				closeMatchs.add(pair);
			} else {
				otherMatchs.add(pair);
			}
		}
	}
	
	List<SLKeyword> x = new ArrayList<>();
  // to avoid having twice the same kw 
	// (as one kw has several labels, it can matche
	// the search string several times)
	Set<SLKeyword> allReadyIn = new HashSet<>();
	if (exactMatch != null) {
		SLKeyword kw = exactMatch.getObject();
		x.add(kw);
		allReadyIn.add(kw);
	}
	if (closeMatchs.size() > 0) {
		List<SLKeyword> kws = new ArrayList<>(closeMatchs.size());
		for (ObjectLabelPair<SLKeyword> pair : closeMatchs) {
			SLKeyword kw = pair.getObject();
			boolean added = allReadyIn.add(kw);
			if (added) kws.add(kw);
		}
		Collections.sort(kws);
		x.addAll(kws);
	}
	if (otherMatchs.size() > 0) {
		List<SLKeyword> kws = new ArrayList<>(otherMatchs.size());
		for (ObjectLabelPair<SLKeyword> pair : otherMatchs) {
			SLKeyword kw = pair.getObject();
			boolean added = allReadyIn.add(kw);
			if (added) kws.add(kw);
		}
		Collections.sort(kws);
		x.addAll(kws);
	}

	kws = x;

	this.beanKwList.setList(kws);
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
}

private int nbOfWords(String s) {
	// use WordsInString? must requires a locale...
	return s.split("[ -@]").length;
}

public String getContent() { return "/jsp/search.jsp"; } // not used by livesearch
public String getTitle() { return "Search " + searchString; } // not used by livesearch
public String getSearchString() { return this.searchString; } // not used by livesearch

//
// RDF
//

public Model getRDF(String extension) throws Exception {
	RDFOutput rdfOutput = new RDFOutput_Search(this, extension);
	return rdfOutput.getModel();
}

//

public String completePath() throws UnsupportedEncodingException, MalformedURLException {
	String x = super.completePath();
	x = x + "?text=" + URLUTF8Encoder.encode(searchString);
	return x;
}
}
