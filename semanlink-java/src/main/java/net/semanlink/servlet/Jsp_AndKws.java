package net.semanlink.servlet;
import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.Intersection;
import net.semanlink.semanlink.*;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.*;

/** And of SLKeywords */
public class Jsp_AndKws extends Jsp_DocumentList {
/** le 1er kw sur lequel on recherche. */
private SLKeyword firstKw;

//
//
//

public Jsp_AndKws(SLKeyword firstKw, String[] otherKwUris, HttpServletRequest request) throws Exception {
	super(otherKwUris, request);
	this.firstKw = firstKw;
	setDocs();
}

public Jsp_AndKws(String[] kwUris, HttpServletRequest request) throws Exception {
	this(SLServlet.getSLModel().getKeyword(kwUris[0]), otherKwUris(kwUris), request);
}

/** @since 2020-02 tagAndTag */
public SLKeyword getFirstKw() {
	return this.firstKw;
}


private static String[] otherKwUris(String[] kwUris) {
	int n = kwUris.length;
	String[] x = new String[n-1];
	for (int i = 1; i < n ; i++) {
		x[i-1] = kwUris[i];
	}
	return x;
}

/**
 * Attention, ceci n'est que la liste des documents affectés du 1er des kws.
 * Elle est réduite ensuite via l'appel à setDocs fait par le constructeur.
 * Liste des SLDocuments NE DOIT PAS dépendre du "mode" (children only ou tree) : doit être en  "long mode" (tree)
 * (cela, depuis 2013-03)
 */
protected List computeDocs()  throws Exception {
	List docs = null;
	String sortProperty = null;
	// @find tag cloud when displaying the short list of docs
	// 2013-03 we changed the way the tag cloud is computed: when in "doc short list" mode (which, BTW, is now the default)
	// we compute the tag cloud as if we were in "long list" mode
	// Therefore we comment out the following test 
	// if (!getDisplayMode().isLongListOfDocs()) {
		// docs = this.firstKw.getDocuments();
	// } else {
		SLTree tree = new SLTree(this.firstKw, "children", sortProperty, SLServlet.getSLModel());
		docs = new ArrayList(Arrays.asList(tree.getDocs()));
	// }
	return docs;
}

// 0.5.5 ajout de cet override pour systématiquement prendre la liste "longue" (tree) des docs dans le and
// (corrige bug depuis modif 2013-03, voir computeDocs)
@Override
public void setDocs() throws Exception {
	this.docs = computeDocs();
	if (this.kws != null) {
		filterDocsByKws(this.docs, kws, false);
	}
}


//
//
//

public String getTitle() {
	StringBuffer sb = new StringBuffer(64);
	sb.append(firstKw.getLabel());
	for (int i = 0; i < this.kws.length-1; i++) {
		sb.append(" ; ");
		sb.append(kws[i].getLabel());
	}
	sb.append(" AND ");
	sb.append(kws[kws.length-1].getLabel());
	return sb.toString();
}

// public SLKeyword[] getKeywords() { return this.kws; }

public Bean_KwList  prepareParentsList() {
	this.beanKwList.setList(getParents());
	// this.beanKwList.setUri(this.slDoc.getURI()); // voir pourquoi faut le mettre (cf removekw). Devrait pas
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
	return this.beanKwList;
}

public List getParents() {
	ArrayList x = new ArrayList();
	x.add(this.firstKw);
	for (int i = 0; i < this.kws.length; i++) {
		x.add(this.kws[i]);
	}
	return x;
}

// 2020-02 TODO (what ?)
public String getLinkToThis(String action) throws UnsupportedEncodingException {
	String x = HTML_Link.linkToAndKws(this.firstKw, this.kws, "and", action).getPage();
	return x;
}

public String getLinkToThis() throws UnsupportedEncodingException {
	// 2020-02 TagAndTag
//	String x = HTML_Link.linkToAndKws(this.firstKw, this.kws, "and").getPage();
//	return x;
	boolean resolveAlias = false;
	// TODO vérifier que getContextUrl() en 1er arg marcherait 2020-03
	String x = HTML_Link.andOfTagsHref("", firstKw, this.kws, resolveAlias); // 2020-02 ça craint
	return x;
}

/** "lien vers un mot clé lié" cad vers un AND de this et du mot clé */
public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	// 2020-02 TagAndTag
//	HTML_Link x = HTML_Link.linkToAndKws(this.firstKw, andOtherKw(otherKw), otherKw.getLabel());
//	return x;
	String href = HTML_Link.tagsAndTagHref(this.getContextURL(), getHref(), otherKw.getURI());
	return new HTML_Link(href, otherKw.getLabel());
}

public String getHref() throws UnsupportedEncodingException {
//	String[] otherKws = new String[kws.length];
//	for (int i = 0 ; i < kws.length ; i++) {
//		otherKws[i] = kws[i].getURI();
//	}
//	return HTML_Link.tagAndTagsHref(SLServlet.getServletUrl(), this.firstKw.getURI(), otherKws, false);
	boolean resolveAlias = false;
	//TODO vérifier que getContextUrl() en 1er arg marcherait 2020-03
	return HTML_Link.andOfTagsHref(SLServlet.getServletUrl(), firstKw, this.kws, resolveAlias); // 2020-02 ça craint
}

public String getContent() throws Exception {
	return "/jsp/andkws.jsp";
}

//
// Pouyr transformer la liste en un nouveau kw
//

public SLKeyword toNewKeyword() throws Exception {
	SLKeyword[] allKws = new SLKeyword[this.kws.length+1];
	allKws[0] = this.firstKw;
	for (int i = 0; i < this.kws.length; i++) {
		allKws[i+1] = this.kws[i];
	}
	SLModel mod = SLServlet.getSLModel();
	SLKeyword x = mod.kwLabel2NewKeyword(getTitle(), mod.getDefaultThesaurus().getURI(),null);
	if (x == null) throw new RuntimeException("A keyword already exists with that label/uri: " + getTitle());
	List docs = getDocs();
	for (int i = 0; i < docs.size(); i++) {
		SLDocument doc = (SLDocument) docs.get(i);
		mod.addKeyword(doc, x);
		mod.removeKeywords(doc, allKws);
	}
	for (int i = 0; i < allKws.length; i++) {
		mod.addParent(x, allKws[i]);
	}
	return x;
}

public String getLinkToNewKeyword() throws UnsupportedEncodingException {
	// 2020-02 TagAndTag
//	String x = HTML_Link.linkToAndKws(this.firstKw, this.kws, "and").getPage() + "&amp;newkw=true";
//	return x;
	
	boolean resolveAlias = false;
	
	// PROBLEME ICI : UTILISE PAR andKws.jsp
	// 2020-02 TagAndTag TODO
	String x = HTML_Link.andOfTagsHref("", this.firstKw, kws, resolveAlias); // 2020-02 ça craint
	return x + "&amp;newkw=true";
}

//
//
//

// called by andkws.jsp to display the list of common descendants 
public List  prepareIntersectKWsList() throws Exception {
	SLTree firstSLTree = new SLTree(this.firstKw, "children", this.sortProperty, getSLModel()) ;
	Graph firstGraph = firstSLTree.getGraph();
	HashSet hsx = (new GraphTraversal(firstGraph)).getNodes();
	
	for (int i = 0; i < this.kws.length; i++) {
		SLTree slTree2 = new SLTree(this.kws[i], "children", this.sortProperty, getSLModel()) ;
		Graph graph2 = slTree2.getGraph();
		Intersection intersection = new Intersection(hsx, graph2);
		if (i < this.kws.length - 1) {
			hsx = intersection.getNodes(false);		
		} else {
			hsx = intersection.getNodes(true);		
		}
	}

	List x = Arrays.asList(hsx.toArray());

	request.setAttribute("livetreelist", x);
	request.setAttribute("divid", "intersectkws");
	request.setAttribute("withdocs", Boolean.TRUE);
	
	return x;
}


} // class
