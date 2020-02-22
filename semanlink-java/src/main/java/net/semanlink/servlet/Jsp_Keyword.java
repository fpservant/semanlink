package net.semanlink.servlet;
import net.semanlink.semanlink.*;

import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.*;

import org.apache.jena.rdf.model.Model;

/** Affichage d'un SLKeyword */
public class Jsp_Keyword extends Jsp_Resource {
private SLKeyword slKw;
/** Achtung, must be accessed through its getter as it's computed only when needed. */
private SLTree tree;
// WHAT MUST BE DISPLAYED
private boolean displayParents = true;
private boolean displayChildrenAndDocs = true;
private boolean displaySnipOnly = false;

//
//
//

public Jsp_Keyword(SLKeyword slKw, HttpServletRequest request) {
	super(slKw, request);
	this.slKw = slKw;
	// System.out.println("NEW Jsp_Keyword " + slKw.getURI());
}

public SLKeyword getSLKeyword() { return this.slKw; }

public String getEditLinkPage() throws UnsupportedEncodingException {
	// return "/editkeyword.do?kwuri=" + java.net.URLEncoder.encode(this.uri, "UTF-8");
	return getLinkToThis() + "?edit=true";
}

//
//
//

public Bean_KwList prepareParentsList() {
	this.beanKwList.setList(this.slKw.getParents());
	this.beanKwList.setUri(this.slKw.getURI()); // voir pourquoi faut le mettre (cf removekw). Devrait pas
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
	if (edit()) {
		this.beanKwList.setField("parents");
	}
	return this.beanKwList;
}

//
//
//

public SLTree getTree() {
	return getTree(this.sortProperty );
}
public SLTree getTree(String sortProperty) {
	if ((this.tree == null) || (!this.tree.isValid(sortProperty))) {
		setTree(new SLTree(this.slKw, "children", sortProperty, SLServlet.getSLModel()));
	}
	return this.tree;
}
private void setTree(SLTree tree) {
	this.tree = tree;
	this.docList = null;
}

// TODO :
// ceci met les kws liés aux docs. Prendre un arg pour pouvoir ne pas le faire
protected Bean_DocList computeDocList()  throws Exception {
	return computeDocList(getSortProperty(), getDisplayMode());
}

public Bean_DocList computeDocList(String sortProp, DisplayMode mode)  throws Exception {
	List<SLDocument> list = null;
	SLKeyword[] dontShow = null;
	boolean showKwsOfDocs = true; // UNUSED cf setShowKwsOfDocs TODO
	
// 2015-10: allow to display long list of docs when kw is edited
//	if (edit()) {
//		list = this.slKw.getDocuments();
//	} else {
		if (!mode.isLongListOfDocs()) {
			list = this.slKw.getDocuments();
		} else {
			SLTree tree;
			if ((mode.isChildrenAsExpandedTree()) || (mode.isChildrenAsTree())) {
				 tree = getTree(sortProp);
			} else {
					tree = getTree(null);
			}
			list = Arrays.asList(tree.getDocs());
		}
//	}
	// dontShow = new SLKeyword[1]; dontShow[0] = this.slKw; // used to not show this tag in the list of tags of the docs ; removed 2013-08 RDFa 
	sort(list, dontShow, sortProp);

	Bean_DocList x = new Bean_DocList();
	x.setList(list);
	x.setShowKwsOfDocs(showKwsOfDocs, dontShow);
	x.setUri(this.slKw.getURI());
	return x;
}
/* Before 2007/10/27
public Bean_DocList computeDocList(String sortProp, Object mode)  throws Exception {
	List list = null;
	SLKeyword[] dontShow = null;
	boolean showKwsOfDocs = true; // UNUSED cf setShowKwsOfDocs TODO
	if (SHOW_EXPANDED_TREE.equals(mode)) {
		// ce cas est appelé lors du clic sur les btns next/prev des images
		 SLTree tree = getTree(sortProp);
		 list = Arrays.asList(tree.getDocs());
		 showKwsOfDocs = false;
		 
	} else if (SHOW_TREE.equals(mode)) {
		// ce cas serait appelé lors du clic sur les btns next/prev des images - en fait pas possible
		 SLTree tree = getTree(sortProp);
		 list = Arrays.asList(tree.getDocs());
		 showKwsOfDocs = false;

	} else {
		if (SHOW_ALL_DOCUMENTS.equals(mode)) {
			// TODO : vérifier si on ne calcule pas 2 fois les kws des docs -- on s'en fout : mis en cache par SLDocumentAdapter
			SLTree tree = getTree(null);
			list = Arrays.asList(tree.getDocs());
			
		} else {
			list = this.slKw.getDocuments();
		}
		dontShow = new SLKeyword[1]; dontShow[0] = this.slKw;
		sort(list, dontShow, sortProp);
	}
	Bean_DocList x = new Bean_DocList();
	x.setList(list);
	x.setShowKwsOfDocs(showKwsOfDocs, dontShow);
	x.setUri(this.slKw.getURI());
	return x;
}
*/


public void sort(List<SLDocument> docList, SLKeyword[] dontShow) {
	sort (docList, dontShow, this.getSortProperty());
}

public static void sort(List<SLDocument> docList, SLKeyword[] dontShow, String sortProp) {
	if (SLVocab.HAS_KEYWORD_PROPERTY.equals(sortProp)) {
		SLUtils.sortDocsByKws(docList, dontShow);
	} else {
		SLUtils.sortByProperty(docList, sortProp);
	}
}

public String computelinkToThis() throws UnsupportedEncodingException {
	return "/tag/" + HTML_Link.getRelativHREF(this.slKw);
}

public String getLinkToThis(String action) throws UnsupportedEncodingException {
	String x = HTML_Link.linkToKeyword(this.slKw, action).getPage();
	if (this.tree != null) x = x + "&amp;tree=children"; // A QUOI CA SERT ??? A RIEN JE PENSE // TODO
	return x;
}

public String getComment() { return this.slKw.getComment(); }

//
// Linked Keywords - TAG CLOUD
//

/** la liste des mots clés liés, cad ayant un doc en commun */
/*public SLKeyword[] getLinkedKeywords() throws Exception {
	SLKeyword[] x=null;
	Object mode = getDescendantsMode();
	if (SHOW_CHILDREN_ONLY.equals(mode)) {
		x = SLUtils.getLinkedKeywords(this.slKw);
	} else {
		SLTree tree = getTree(null);
		x = tree.getLinkedKeywords();
	}
	return x;
}*/

public HashMap<SLKeyword, Integer> getLinkedKeywords2NbHashMap() throws Exception {
	HashMap<SLKeyword, Integer> x=null;
	// DisplayMode mode = getDisplayMode();
	// 2013-03
	// the tag cloud was computed on the list of displayed documents
	// But for the short list of docs, it's probably better to display the big cloud too
	// (the cloud for the log list of docs)
	// @find tag cloud when displaying the short list of docs
	// if (!(mode.isLongListOfDocs())) {
	//	x = getLinkedKeywords2NbHashMap(this.slKw);
	// } else {
		SLTree tree = getTree(null);
		x = tree.getLinkedKeywords2NbHashMap();
	// }
	return x;
}

// il y a getLinkedKeywords ds SLUtils
/** Calcul des kws liés à un kw. 
 *  Un kw est lié à kw ssi ils ont un document en commun.
 */
public static SLKeywordNb[] getLinkedKeywordsWithNb(SLKeyword kw) {
	return getLinkedKeywordsWithNb(getLinkedKeywords2NbHashMap(kw));
}
/** Calcul des kws liés à un kw. 
 *  Un kw est lié à kw ssi il ont un document en commun.
 */
public static HashMap<SLKeyword, Integer> getLinkedKeywords2NbHashMap(SLKeyword kw) {
	HashMap<SLKeyword, Integer> kw2nb = SLUtils.getLinkedKeywords2NbHashMap(kw.getDocuments());
	// retirer kw
	kw2nb.remove(kw);
	// et ses enfants
	/*List children = kw.getChildren();
	for (int i = 0; i < children.size(); i++) {
		kw2nb.remove(children.get(i));
	}*/
	return kw2nb;
}

/** the method called by the jsp to display the tag cloud 
 *  Redefinition of method in Jsp_Page */
public SLKeywordNb[] getSmartLinkedKeywordsWithNb() throws Exception {
	// les kws et leur nb d'occurence
	HashMap<SLKeyword, Integer> directlyLinkedHM = getLinkedKeywords2NbHashMap();
	// pour éviter, par ex, d'avoir un méga "Favoris" quand on cherche "Niger",
	// on commence par éliminer les directlyLinkedHM qui sont ancetres de this
	SLFastTree fastTree = new SLFastTree(this.slKw, SLVocab.HAS_PARENT_PROPERTY, getSLModel());		
	HashSet<SLKeyword> kwsInTree = fastTree.getKwsSet();
	for (Iterator<SLKeyword> ite = kwsInTree.iterator() ; ite.hasNext() ;) {
		SLKeyword ancetre = ite.next();
		// ancetre est il ds directlyLinked ?
		directlyLinkedHM.remove(ancetre);
	}
	return getSmartLinkedKeywordsWithNb(directlyLinkedHM);
}

//
//
//

// 2020-02 (?)
/** "lien vers un mot clé lié" cad vers un AND de this et du mot clé */
public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	SLKeyword[] otherKws = new SLKeyword[1];
	otherKws[0] = otherKw;
	HTML_Link x = HTML_Link.linkToAndKws(this.slKw, otherKws, otherKw.getLabel());
	return x;
	// Collection findDocs(List kws)
}

//
//
//

public String getContent() throws Exception {
	// if (edit()) return "/jsp/keywordedit.jsp";
	return "/jsp/keyword.jsp";
}

//
// alias
//

public String[] getAliases() {
	// List aliasUriList = this.getSLKeyword().getPropertyAsStrings(SLVocab.HAS_ALIAS_PROPERTY);
	List aliasUriList = this.getSLModel().getAliasUriList(this.getSLKeyword());
	if (aliasUriList == null) return null;
	String[] x = new String[aliasUriList.size()];
	aliasUriList.toArray(x);
	return x;
}

public String getAliasLabel(String aliasUri) {
	return SLServlet.getSLModel().getKeyword(aliasUri).getLabel();
}

//rss

/** 
 * L'uri complète est req.getContextPath() +"/" + rssFeedUriRelativToSL()
 * url relative à req.getContextPath() (id est à xxx/semanlink) 
 * (ce qu'il faut mettre au bout pour avoir l'uri du kw) */
public String rssFeedUriRelativToSL() throws UnsupportedEncodingException {
	// return "rss/" + HTML_Link.getRelativHREF(getSLKeyword()); // 2007-01
	return "tag/" + HTML_Link.getKwRelativHREF(getSLKeyword().getURI(), ".rss");
}

//avait été fait sur le modèle de rssFeedUriRelativToSL, no more used: remplacé par des appels à linkToRDF. Est-ce bien ? voir Jsp_Page.linkToRDF
public String linkToRDFRelativToSL() throws Exception {
	return "tag/" + HTML_Link.getKwRelativHREF(getSLKeyword().getURI(), ".rdf");
}

//
// WHAT MUST BE DISPLAYED (cf snip)
//

public boolean isDisplayChildrenAndDocs() {
	return displayChildrenAndDocs;
}

public void setDisplayChildrenAndDocs(boolean displayChildrenAndDocs) {
	this.displayChildrenAndDocs = displayChildrenAndDocs;
}

public boolean isDisplayParents() {
	return displayParents;
}

public void setDisplayParents(boolean displayParents) {
	this.displayParents = displayParents;
}

public boolean isDisplaySnipOnly() {
	return displaySnipOnly;
}

public void setDisplaySnipOnly(boolean displaySnipOnly) {
	this.displaySnipOnly = displaySnipOnly;
}

//
//RDF
//

public Model getRDF(String extension) throws Exception {
	RDFOutput rdfOutput = new RDFOutput_Keyword(this, extension);
	return rdfOutput.getModel();
}

// 2010-12
public Model getRawRDF(String extension) throws Exception {
	RDFOutput rdfOutput = new RDFOutput_Keyword(this, extension);
	rdfOutput.setTag2ResConversion(false);
	return rdfOutput.getModel();
}


//
//
//

public String getHomePage() {
	/*PropertyValues pvs = this.slKw.getProperty(SL_HOME_PAGE_PROPERTY);
	if (pvs != null) return pvs.getFirstAsString();
	return null;*/
	return getFirstAsString(SL_HOME_PAGE_PROPERTY);
}

public String getDescribedByPage() {
	return getFirstAsString(SL_DESCRIBED_BY_PROPERTY);
}


/*public PropertyValues getDescribedBy() {
	PropertyValues pvs = this.slKw.getProperty(SL_DESCRIBED_BY_PROPERTY);
	return pvs;
}*/

public Iterator rdfTypes4Tags() {
	return SLServlet.getSLModel().rdfTypes4Tags();
}

} // class