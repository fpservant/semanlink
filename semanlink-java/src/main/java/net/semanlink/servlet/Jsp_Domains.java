/* Created on 19 oct. 2004 */
package net.semanlink.servlet;

import java.io.IOException;
import java.net.*;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.Intersection;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLTree;


public class Jsp_Domains extends Jsp_ThisMonth {
// use getter
private HashMap domain2Documents;

public Jsp_Domains(HttpServletRequest request) throws Exception {
	super(request);
}

public String getTitle() throws Exception {
	// String x = i18l("topmenu.newEntries");
	String x = "Domains used in bookmarks";
	return x;
}


public String getContent() throws Exception {
	return "/jsp/domains.jsp";
}

/**
 * key : domain, data : list of SLDocuments
 * @throws Exception 
 */
public HashMap getDomain2Documents() throws Exception {
	if (domain2Documents == null) {
		domain2Documents = getDomain2Documents(getDocs());
	}
	return domain2Documents;
}

/**
 * @param docs list of SLDocument(s)
 * @return
 * @throws Exception
 */
static public HashMap getDomain2Documents(List docs) throws Exception {
		SLModel slMod = SLServlet.getSLModel();
		HashMap x = new HashMap();
		for (int i = 0; i < docs.size(); i++) {
			SLDocument doc = (SLDocument) docs.get(i);
			String uri = doc.getURI();
			if (slMod.getFile(uri) != null) continue; // local TODO : if source documented
			
			String domain = domain(uri);
			ArrayList data = (ArrayList) x.get(domain);
			if (data == null) {
				data = new ArrayList();
				x.put(domain, data);
			}
			data.add(doc);
		}
		return x;
}


/** not sorted 
 * @throws Exception */
public String[] getDomains() throws Exception {
	HashMap hm = getDomain2Documents();
	Set set = hm.keySet();
	String[] x = new String[set.size()];
	set.toArray(x);
	return x;
}

static private String domain(String url) throws URISyntaxException, MalformedURLException {
	//URI uri = new URI(url);
	//return uri.getHost();
	URL x = new URL(new URL(url),"/");
	return x.toString();
}

public String aboutList(int nb) throws Exception {
	return aboutList(i18l("found.domains"), nb);
}

} //
