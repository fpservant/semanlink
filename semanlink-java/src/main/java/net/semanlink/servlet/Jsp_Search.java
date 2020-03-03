/* Created on 17 mai 2005 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.URLUTF8Encoder;

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
	Set<SLKeyword> kwSet = mod.getThesaurusIndex().searchText(searchString);
	this.kws = new ArrayList<SLKeyword>(kwSet.size());
  this.kws.addAll(kwSet);
	
	
	// System.out.println("Jsp_Search size "+ this.kws.size());
	Collections.sort(this.kws);
	
	
	
	// 2020-03 exact or close matches first in search results
	int n = nbOfWords(searchString);
	SLKeyword exactMatch = null;
	List<SLKeyword> closeMatchs = new ArrayList<>();
	List<SLKeyword> endOfList = new ArrayList<>();
	// TODO altLabels
	for (SLKeyword kw : kws) {
		String s = kw.getLabel();
		if (searchString.equals(s)) {
			exactMatch = kw;
		} else {
			if (s == null) {
				endOfList.add(kw);
				continue; // juste au cas où (devrait pa arriver)
			}
			int n2 = nbOfWords(s);
			if (n == n2) {
				closeMatchs.add(kw);
			} else {
				endOfList.add(kw);
			}
		}
	}
	
	List<SLKeyword> x = null;
	if (exactMatch != null) {
		x = new ArrayList<>();
		x.add(exactMatch);
	}
	if (closeMatchs.size() > 0) {
		if (x != null) {
			x.addAll(closeMatchs);
		} else {
			x = closeMatchs;
		}
	}
	if (x == null) {
		x = kws;
	} else {
		x.addAll(endOfList);
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
//RDF
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
