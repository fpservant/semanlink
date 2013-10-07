/* Created on 1 sept. 2005 */
package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import net.semanlink.semanlink.PropertyValues;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.Util;
import net.semanlink.util.xml.XMLWriterUtil;

public class RSSHelper implements SLVocab {
private DocumentFactory docFactory;
private String dateProp;
private boolean usesCreationDate;
public RSSHelper(HttpServletRequest request) throws Exception {
	this.docFactory = Manager_Document.getDocumentFactory();
	this.dateProp = SLServlet.getJspParams().getDateProperty(request);
	if (this.dateProp == null) this.dateProp = SL_CREATION_DATE_PROPERTY;
	this.usesCreationDate = (
			(SL_CREATION_DATE_PROPERTY.equals(this.dateProp))
			|| (SL_CREATION_TIME_PROPERTY.equals(this.dateProp))
			|| (this.dateProp == null)
	);
}

String getDateProp() { return this.dateProp; }

public String getDate(SLDocument doc) {
	return getDate(doc, this.usesCreationDate, this.dateProp);
}

/** returns null if not documented */
public static String getCreationDate(SLDocument doc) {
	PropertyValues vals;
	// better to use SL_CREATION_TIME_PROPERTY, if it is documented
	vals = doc.getProperty(SL_CREATION_TIME_PROPERTY);
	if ((vals != null) && (vals.size() > 0)) {
		return vals.getFirstAsString();
	}
	vals = doc.getProperty(SL_CREATION_DATE_PROPERTY);
	if ((vals != null) && (vals.size() > 0)) {
		return vals.getFirstAsString();
	}
	return null;
}

/**
 * 
 * @param doc
 * @param usesCreationDate true iff slCreationTime (or Date if not vailable) is to be used
 * @param dateProp to be dcumented only if !usesCreationDate
 * @return
 */
private static String getDate(SLDocument doc, boolean usesCreationDate, String dateProp) {
	String x = null;
	if (usesCreationDate) {
		x = getCreationDate(doc);
		if (x == null) return "2000-01-01T01:01:01Z";
	} else {
		PropertyValues vals = doc.getProperty(dateProp);
		if ((vals != null) && (vals.size() > 0)) {
			return vals.getFirstAsString();
		}
		x = getCreationDate(doc);
	}
	if (x == null) x = "2000-01-01T01:01:01Z";
	return x;
}




/* pas de liens ds descirption !
public String getComment(SLDocument doc){
	String comment = doc.getComment();
	List tags = doc.getKeywords();
	if ((tags != null) && (tags.size() > 0)) {
		StringBuffer sb = new StringBuffer();
		if (comment != null) sb.append(XMLWriterUtil.xmlEscape(comment));
		sb.append("<p>");
		for (int i = 0; i < tags.size(); i++) {
			sb.append("href=\"");
			SLKeyword tag = (SLKeyword) tags.get(i);
			sb.append(tag.getURI());
			sb.append("\">");
			sb.append(XMLWriterUtil.xmlEscape(tag.getLabel()));
			sb.append("</a>");
		}
		sb.append("</p>");
		comment = sb.toString();
	}
	
	return comment;
}
*/

public String getComment(SLDocument doc){
	String comment = doc.getComment();
	if (comment != null) {
		return XMLWriterUtil.xmlEscape(comment);
	}
	return null;
}


public String getLink(SLDocument doc) throws Exception {
	/*
	// this send to the doc's page in semanlink
	// Why did I want to do that?
	Jsp_Document jspDoc = docFactory.newJsp_Document(doc,this.request);
	return XMLWriterUtil.xmlEscape(getContextUrl() + jspDoc.getLinkToThis()); 
	
	2012 Hmm peut-être pour le cas des cr rdd sicg.
	Utiliser jspDoc.getHREF() ? attention, à implémenter ds jsp_sicgartcle (ya un static getHREF, non appellée !!!)
	
	sinon : se demander pourquoi on a l'air de stocker comme doc pour les articles le vieil article, pas le res de son affichage formaté !!!
	*/
	
	// 2012-12 cr de rdd: je suis emmerdé, parce que le flux rss pointe vers les articles non formattés
	// return XMLWriterUtil.xmlEscape(doc.getURI());

	return XMLWriterUtil.xmlEscape(doc.getURI());
}

}

