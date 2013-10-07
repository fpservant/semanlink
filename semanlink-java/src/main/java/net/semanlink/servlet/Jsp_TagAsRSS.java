/* Created on 1 sept. 2005 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.util.Util;
import net.semanlink.util.xml.XMLWriterUtil;

public class Jsp_TagAsRSS extends Jsp_Keyword implements Jsp_RSS {
private RSSHelper rssHelper;
private String contextUrl;
public Jsp_TagAsRSS(SLKeyword slKw, HttpServletRequest request) throws Exception {
	super(slKw, request);
	rssHelper = new RSSHelper(request);
	this.contextUrl = Util.getContextURL(request);
}

public String getTitle() {
	return XMLWriterUtil.xmlEscape(getSLKeyword().getLabel());
}

public String getAbout() {
	return XMLWriterUtil.xmlEscape(getSLKeyword().getURI());
}

public String getDescription() {
	return XMLWriterUtil.xmlEscape("Documents tagged with " + getSLKeyword().getLabel());
}

public String getLink() throws UnsupportedEncodingException {
	return XMLWriterUtil.xmlEscape(HTML_Link.getTagURL(contextUrl, this.getSLKeyword().getURI(), false, null));
}

public List getDocs() throws Exception {
	Bean_DocList bdl = this.computeDocList(rssHelper.getDateProp(), new DisplayMode(null, true));
	return bdl.getList();
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
