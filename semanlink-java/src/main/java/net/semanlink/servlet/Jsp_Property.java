package net.semanlink.servlet;
import net.semanlink.semanlink.*;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.*;

public class Jsp_Property extends Jsp_DocumentList {
private String propertyUri, objectUri, propertyValue, lang;
//use getter
private List KWs;

//
//
//

/**
 * @params kwUris pour faire un AND de la ppté avec les kws en question
 */
public Jsp_Property(String propertyUri, String objectUri, String[] kwUris, HttpServletRequest request) throws Exception {
	super(kwUris, request);
	// super(SLServlet.getSLModel().getDocument(propertyUri), request);
	this.propertyUri = propertyUri;
	this.objectUri = objectUri;
	setDocs();
}

/**
 * @params kwUris pour faire un AND de la ppté avec les kws en question
 */
public Jsp_Property(String propertyUri, String propertyValue, String lang, String[] kwUris, HttpServletRequest request) throws Exception {
	super(kwUris, request);
	// super(SLServlet.getSLModel().getDocument(propertyUri), request);
	this.propertyUri = propertyUri;
	this.propertyValue = propertyValue;
	this.lang = lang;
	setDocs();
}

public List computeDocs() throws Exception {
	if (this.objectUri != null) {
		return SLServlet.getSLModel().getDocumentsList(this.propertyUri, this.objectUri);
	} else { // cas ppty string
		return SLServlet.getSLModel().getDocumentsList(propertyUri, propertyValue, lang);
	}
}

public String getTitle() {
	String x;
	if (objectUri != null) {
		x = Jsp_Resource.displayUri(propertyUri) + " : " + Jsp_Resource.displayUri(objectUri);
	} else {
		x = Jsp_Resource.displayUri(propertyUri) + " : " + propertyValue;
	}
	if (this.kws == null) return x;
	StringBuffer sb2 = new StringBuffer(x);
	for (int i = 0; i < this.kws.length; i++) {
		sb2.append(" AND ");
		sb2.append(this.kws[i].getLabel());
	}
	return sb2.toString();
}

public String getLinkToThis(String action) throws UnsupportedEncodingException {
	if (this.objectUri != null) {
		if (this.kws == null) {
			return HTML_Link.linkToProp(action, this.propertyUri, this.objectUri, this.objectUri).getPage();
		} else {
			return HTML_Link.propAndKwsPage(action, this.propertyUri, this.objectUri, this.objectUri, this.kws);
		}
	} else {
		if (this.kws == null) {
			return HTML_Link.linkToProp(action, this.propertyUri, this.propertyValue, this.lang, this.propertyValue).getPage();
		} else {
			return HTML_Link.propAndKwsPage(action, this.propertyUri, this.propertyValue, this.lang, this.propertyValue, this.kws);
		}
	}
}

public String getLinkToThis() throws UnsupportedEncodingException {
	return getLinkToThis("/showprop.do");
}

/** "lien vers un mot clé lié" cad vers un AND de this et du mot clé */
public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	SLKeyword[] andkws = andOtherKw(otherKw);
	String page = null;
	if (this.objectUri != null) {
		page = HTML_Link.propAndKwsPage("/showprop.do", this.propertyUri, this.objectUri, this.objectUri, andkws);
	} else {
		page = HTML_Link.propAndKwsPage("/showprop.do", this.propertyUri, this.propertyValue, this.lang, this.propertyValue, andkws);
	}
	return new HTML_Link(page, otherKw.getLabel());
}

//
//
//

public String getContent() throws Exception {
	return "/jsp/property.jsp";
}

// rajouté tardivement (2008/05) pour le cas de la recherche des kws ayant une certaine ppté
// pas fait intersection avec liste de kws
// (Et pas tenté de prendre en compte le fait que, si on part d'un doc, c'est probablment des docs qu'on cherche
// et si on part d'une page tag, c'est proablement des tags
// (on recherche systématiquement et les docs, et les tags)

public List getKWs() throws Exception {
	if (this.KWs == null) computeKWs();
	return this.KWs;
}

protected void computeKWs() throws Exception {
	if (this.objectUri != null) {
		this.KWs = SLServlet.getSLModel().getKeywordsList(this.propertyUri, this.objectUri);
	} else { // cas ppty string
		this.KWs = SLServlet.getSLModel().getKeywordsList(propertyUri, propertyValue, lang);
	}
}

public List  prepareKWsList() throws Exception {
	List x = getKWs();
	request.setAttribute("livetreelist", x);
	request.setAttribute("divid", "intersectkws");
	request.setAttribute("withdocs", Boolean.TRUE);
	request.setAttribute("resolveAlias", Boolean.FALSE);
	return x;
}


} // class
