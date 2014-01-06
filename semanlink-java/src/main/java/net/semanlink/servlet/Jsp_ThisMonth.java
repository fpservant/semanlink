/* Created on 19 oct. 2004 */
package net.semanlink.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;


public class Jsp_ThisMonth extends Jsp_DocumentList {
protected String dateProp;
public int nbOfDays = 31;
// use getter
private List KWs;
public Jsp_ThisMonth(HttpServletRequest request) throws Exception {
	this(null, request);
}

public Jsp_ThisMonth(String[] kwUris, HttpServletRequest request) throws Exception {
	super(kwUris, request);
	String s = request.getParameter("days");
	if (s != null) {
		try {
			this.nbOfDays = Integer.parseInt(s);
		} catch (NumberFormatException e) {}
	}
	this.dateProp = SLServlet.getJspParams().getDateProperty(request);
	// setSortProperty(this.dateProp); // TODO
	setDocs();
}

// peut s'optimiser (par ex en ne recalculant pas tout de YearMonthDay.daysAgo(i)
public List computeDocs() throws Exception {
	SLModel mod = SLServlet.getSLModel();
	return mod.getRecentDocs(this.nbOfDays, this.dateProp);
}

public List getKWs() throws Exception {
	if (this.KWs == null) computeKWs();
	return this.KWs;
}

protected void computeKWs() throws Exception {
	this.KWs = SLServlet.getSLModel().getRecentKws(this.nbOfDays, this.dateProp);
}


//
//
//

public String getTitle() {
	// String x = "New Entries (" + this.getDocs().size() + " documents in the last " + nbOfDays + " days)";
	String x = i18l("topmenu.newEntries");
	if (this.kws == null) return x;
	StringBuffer sb2 = new StringBuffer(x);
	for (int i = 0; i < this.kws.length; i++) {
		sb2.append(" AND ");
		sb2.append(this.kws[i].getLabel());
	}
	return sb2.toString();
}

public int getNbOfDays() { return this.nbOfDays; }
public int nbOfDocs() throws Exception { return this.getDocs().size(); }
/*public String aboutList() throws Exception {
	// StringBuffer sb = new StringBuffer(this.getDocs().size() + " documents ");
	MessageFormat messageFormat = new MessageFormat(i18l("newEntries.foundDocs"));
	Object[] args = new Object[2];
	// messageFormat.format(args);
	StringBuffer sb = new StringBuffer(messageFormat.format(args));
	if ((nbOfDays == 30) || (nbOfDays == 31)) {
		// sb.append("during the last month");
		args[1] = i18l("newEntries.foundDocsDuring1");
	} else if (nbOfDays == 7) {
		// sb.append("during the last week");		
		args[1] = i18l("newEntries.foundDocsDuring2");
	} else if ((nbOfDays == 14) || (nbOfDays == 15)) {
		// sb.append("during the last two weeks");		
		args[1] = i18l("newEntries.foundDocsDuring3");
	} else {
		// sb.append("during the last " + nbOfDays + " days");
		MessageFormat mf = new MessageFormat(i18l("newEntries.foundDocsDuring4"));
		args[0] = Integer.toString(nbOfDays);
		args[1] = mf.format(args);
	}
	args[0] = this.getDocs().size();
	
	
	return messageFormat.format(args);
} */

public String aboutList() throws Exception {
	return aboutList(i18l("found.documents"), this.getDocs().size());
}


public String aboutList(String what, int nb) throws Exception {
	MessageFormat messageFormat = new MessageFormat(i18l("found"));
	Object[] args = new Object[3];
	// messageFormat.format(args);
	StringBuffer sb = new StringBuffer(messageFormat.format(args));
	if ((nbOfDays == 30) || (nbOfDays == 31)) {
		// sb.append("during the last month");
		args[1] = i18l("found.during1");
	} else if (nbOfDays == 7) {
		// sb.append("during the last week");		
		args[1] = i18l("found.during2");
	} else if ((nbOfDays == 14) || (nbOfDays == 15)) {
		// sb.append("during the last two weeks");		
		args[1] = i18l("found.during3");
	} else {
		// sb.append("during the last " + nbOfDays + " days");
		MessageFormat mf = new MessageFormat(i18l("found.during4"));
		args[0] = Integer.toString(nbOfDays);
		args[1] = mf.format(args);
	}
	args[0] = nb;
	args[2] = what;
	
	
	return messageFormat.format(args);
}


/*public String getLinkToThis() throws UnsupportedEncodingException {
	return "/neworupdated.do?"; // le ? pour image
}*/
/* (non-Javadoc)
 * @see net.semanlink.servlet.Jsp_DocList#getLinkToThis(java.lang.String)
 */
/*public String getLinkToThis(String action) throws UnsupportedEncodingException {
	return action + "?action=neworupdated"; // bof bof // todo
}*/
/* (non-Javadoc)
 * @see net.semanlink.servlet.Jsp_DocList#linkToThisAndKw(net.semanlink.semanlink.SLKeyword)
 */
public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	/*
	// ceci donne new AND tag
	SLKeyword[] andkws = andOtherKw(otherKw);
	// return HTML_Link.linkToAndKws(andkws, otherKw.getLabel());
	StringBuffer sb = new StringBuffer(128);
	sb.append("/neworupdated.do?");
	HTML_Link.appendsKwsParams(andkws, sb);
	String page = sb.toString() ;
	return new HTML_Link(page, otherKw.getLabel() );
	*/
	return HTML_Link.getHTML_Link(otherKw);
}

//rss

/** 
 * L'uri complète est req.getContextPath() +"/" + rssFeedUriRelativToSL()
 * url relative à req.getContextPath() (id est à xxx/semanlink) 
 * (ce qu'il faut mettre au bout pour avoir l'uri du kw) */
public String rssFeedUriRelativToSL() throws UnsupportedEncodingException {
	return "rss/"; // trailing slash needed on kattare
}

//
//
//

public String getContent() throws Exception {
	return "/jsp/thisMonth.jsp";
}


public List  prepareNewKWsList() throws Exception {
	List x = getKWs();
	request.setAttribute("livetreelist", x);
	request.setAttribute("divid", "intersectkws");
	request.setAttribute("withdocs", Boolean.TRUE);
	request.setAttribute("resolveAlias", Boolean.FALSE);
	return x;
}


} //
