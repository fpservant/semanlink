/* Created on 24 sept. 03 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;

import net.semanlink.semanlink.SLKeyword;

/**
 * A list of keywords to be displayed.
 * Classe utilitaire pour l'affichage des JSP presentant des listes de SLKeyword
 * (telles les enfants et parents d'un kw, ou les kws d'un doc).
 */
public class Bean_KwList extends Bean_ResList {
private String ulCssClass;
/** si doc, les liens sont jsp.linkToThisAndKw, sinon, lien vers kw seul. */
private Jsp_Page jsp;
public void setJsp(Jsp_Page jsp) { this.jsp = jsp; }
public Jsp_Page getJsp() { return this.jsp; }
public HTML_Link getLink(int i) throws Exception {
	if (jsp == null) {
		// return HTML_Link.linkToKeyword(getSLKeyword(i)); // 2007/11 bug sur le tag cloud des dossiers (et cr rdd)
		return HTML_Link.getHTML_Link(getSLKeyword(i));
	} else {
		return jsp.linkToThisAndKw(getSLKeyword(i));
	}
}
public SLKeyword getSLKeyword(int i) {
	return (SLKeyword) this.getList().get(i);
}

/** Ne pas oublier l'url rewriting lors de l'appel */
public String getHREF(String contextUrl, int i) throws UnsupportedEncodingException {
	return getHREF(contextUrl, i, ".html");
}
// 2013-08 RDFa
public String getHREF(String contextUrl, int i, String dotExtension) throws UnsupportedEncodingException {
	return HTML_Link.getTagURL(contextUrl, getSLKeyword(i).getURI(), false, dotExtension);
}

public String getLabel(int i) {
	return getSLKeyword(i).getLabel();
}
public String getUlCssClass() {
	return ulCssClass;
}
public void setUlCssClass(String ulCssClass) {
	this.ulCssClass = ulCssClass;
}
} // class


