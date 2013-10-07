/* Created on 13 nov. 2005 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.util.xml.XMLWriterUtil;
import net.semanlink.util.Util;

public class Jsp_NewsAsRSS extends Jsp_ThisMonth implements Jsp_RSS {
private DocumentFactory docFactory;
private String about;
private RSSHelper rssHelper;
public Jsp_NewsAsRSS(HttpServletRequest request) throws Exception {
	super(request);
	rssHelper = new RSSHelper(request);
	this.docFactory = Manager_Document.getDocumentFactory();
	about = XMLWriterUtil.xmlEscape(Util.getContextURL(request) + this.getLinkToThis());
}

public String getTitle() {
	return "Semanlink : new entries";
}

public String getDescription() {
	return "Semanlink : new entries";
}

public String getAbout() throws UnsupportedEncodingException, MalformedURLException {
	return about;
}

public String getLink() throws UnsupportedEncodingException, MalformedURLException {
	return about;
}

public List getDocs() throws Exception {
	return computeDocs();
}

public String getDate(SLDocument doc) {
	return rssHelper.getDate(doc);
}

public String getComment(SLDocument doc){
	return rssHelper.getComment(doc);
}

public String getLink(SLDocument doc) throws Exception {
	return rssHelper.getLink(doc);
}
}
