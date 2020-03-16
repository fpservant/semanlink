package net.semanlink.servlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLFastTree;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.util.Util;

import javax.servlet.http.*;

import org.apache.jena.rdf.model.Model;

/** Modélise une page à afficher. */
public class Jsp_Page extends net.semanlink.util.servlet.Jsp_Page implements SLVocab {
public static int TAG_CLOUD_MAX_SIZE = 50;
public static int TAG_CLOUD_REMOVE_LIMIT = 4; // 2020-01: don't have CamemBERT in the tag cloud if you have BERT - unless a lot of CamemBERT
public static int TAG_CLOUD_MINIMAL_NB = 5; // if more tags than TAG_CLOUD_MAX_SIZE, then remove tags with low nb. Keep at least that nb of tags

/** list of SLDocuments.
 * Achtung, must be accessed through its getter as it's computed only when needed.
 * Must be reset to null if state of this changes
 */
protected Bean_DocList docList;
/**
 * Achtung, must be accessed through its getter as it's computed only when needed. */
protected Bean_KwList beanKwList;

private SLDocument imageToBeDisplayed;
/** éventuellement, on a calculé l'index de l'image à montrer. (Si tel est le cas,
 * on va le passer à la jsp, qui le renverra, et on pourra trouver la next image avec.)
 */
private int imageToBeDisplayedIndex=-1;
/* Achtung, must be accessed through its getter as it's computed only when needed. */
protected String linkToThis;
/** faut-il montrer les keywords d'un doc de getDocList.*/
protected boolean showKeywordsInDocList = false;
/** selon quelle property trier */
protected String sortProperty;
private DisplayMode displayMode = null;
/** pour caractériser les cas simples, la jsp à inclure ("jsp/pagexxx.jsp")*/
protected String content;
// use getter
private String contextUrl;
// use getter
private I18l i18l;
private boolean isIE;
public boolean isIE() { return this.isIE; }

public Jsp_Page(HttpServletRequest request) {
	this(request, null); // @find old Jsp_Page extending new Jsp_Page
}
public Jsp_Page(HttpServletRequest request, HttpServletResponse response) {
	super(request, response);
	this.isIE = Util.isIE(request);
	setSortProperty(getSortProperty());
	
	this.beanKwList = new Bean_KwList();
	this.beanKwList.setUri(getUri());
	// au sujet de la ligne qui suit : 
	// je préfère la répéter ds chaque méthode "preparexxx" pour être
	// sur que l'attribut de la request n'est pas changé entre temps.
	// this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
	
	this.addOnLoadEvents("downloadJS"); // 2013-08 to load scripts that can be loaded after the body
	// 2010-06 was in template.jsp : <body onload="liveSearchInit(); setFocus();"> // Must be AFTER downloadJS
	// this.addOnLoadEvents("liveSearchInit"); // 2013-08: found a way to avoid having to do that on load
	this.addOnLoadEvents("setFocus");
	// 2017-07 sl:comment as markdown
	// 2019-09: to be used only when !edit the comment (=> don't name "slcomment" the div when editing)
	// if (!edit(request)) {
	this.addOnLoadEvents("displayCommentAsMarkdown");	
	this.addOnLoadEvents("rightBar"); // 2020-01 right-bar

	// }
}

//2019-09
public static boolean edit(HttpServletRequest request) {
	HttpSession session = request.getSession();
	if (session == null) {
	   return false;
	}
	return (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
}

public SLModel getSLModel() {
	return SLServlet.getSLModel(request);
}

/** à cause de Action_LogonPage */
public HttpServletRequest getRequest()  { return this.request; }
/** retourne quelque chose genre "/jsp/page.jsp" @see template.jsp */
public String getContent() throws Exception { return this.content; }
public void setContent(String content) { this.content = content; }
/** Défini dans Jsp_Resource pour retourner l'uri de la resource. */ // 2012-07: Hmm, ATTENTION, retourne uri ds semanlink.net, pas 127.0.0.1:8080/semanlink
public String getUri() { return null; }
public boolean edit() { return false; }
public Bean_KwList prepareParentsList() {return null;}

/** par ex http://127.0.0.1:8080/semanlink */
public String getContextUrl() throws MalformedURLException {
	if (this.contextUrl == null) {
		this.contextUrl = Util.getContextURL(this.request);
	}
	return this.contextUrl;
}

public DisplayMode getDisplayMode() {
	if (displayMode == null) computeDisplayMode();
	return displayMode;
}

protected void computeDisplayMode() {
	String childrenAs = request.getParameter("mode");
	// String listMode = request.getParameter("listMode");
	if (childrenAs != null) {
		String longList = request.getParameter("longlist");
		boolean longListB = true;
		if (longList != null) {
			if (longList.toLowerCase().equals("false")) longListB = false;
		}
		this.displayMode = new DisplayMode (childrenAs, longListB);
	} else {
		HttpSession session = this.request.getSession();
		this.displayMode = (DisplayMode) session.getAttribute("net.semanlink.servlet.displayMode");
		if (this.displayMode == null) {
			this.displayMode = DisplayMode.DEFAULT;
		}
	}
}


public String getTitle() { return this.title; }
public void setTitle(String title) { this.title = title; }
/** ce qui est affiché dans la div "title" de la page. */
public String getTitleInTitle() throws Exception { return getTitle(); }
public String getEditLinkPage() throws UnsupportedEncodingException { 
	boolean editState = BaseAction.getSessionEditState(this.request);
	String s = Boolean.toString(!editState);
	String x = getLinkToThis();
	if (x == null) return null; // cause pb avec page new doc
	if (x.indexOf("?") > -1) {
		x = x + "&amp;edit=" + s;
	} else {
		x = x + "?edit=" + s;
	}
	return x;
}

public String getComment() { return null; }

//
// DISPLAYING ONE IMAGE
//

/** imageToBeDisplayed doit toujours etre documente, meme si imageToBeDisplayedIndex l'est.
 *  passer -1 dans imageToBeDisplayedIndex si on ne le connait pas,
 */
public void setImageToBeDisplayed(SLDocument imageToBeDisplayed, int imageToBeDisplayedIndex) {
	this.imageToBeDisplayed = imageToBeDisplayed;
	this.imageToBeDisplayedIndex = imageToBeDisplayedIndex;
}

public SLDocument getImageToBeDisplayed() {
	if (this.imageToBeDisplayed == null) {
		String imageUri = request.getParameter("imagetobedisplayed");
		if (imageUri != null) {
			// JE NE COMPRENDS PAS POURQUOI MAIS,
			// alors que docuri a ete encode, il ne faut pas ici le decoder.
			// VOIR AUSSI 2 fois ds Action_ShowKeyword et ds Action_NextImage et Action_ShowKeyword
			// (test : egypte antique, clic sur temple, 1er next image)
			// imageUri = java.net.URLDecoder.decode(imageUri,"UTF-8");
			setImageToBeDisplayed(SLServlet.getSLModel().getDocument(imageUri), -1); // en vrai, on n'a besoin que de l'uri - at this time
		}
	}
	return this.imageToBeDisplayed;
}

public String getNextImagePage(boolean prevInsteadOfNext) throws UnsupportedEncodingException {
	String x;
	if (this.imageToBeDisplayedIndex > -1) {
		x = getLinkToThis("/nextImage.do") + "&amp;currentimageindex=" + Integer.toString(this.imageToBeDisplayedIndex);
	} else {
		x = getLinkToThis("/nextImage.do") + "&amp;currentimage=" + java.net.URLEncoder.encode(this.imageToBeDisplayed.getURI(), "UTF-8");
	}
	if (prevInsteadOfNext) x = x + "&amp;prev=true";
	return x;	
}

//
// LINK TO THIS
//

//attention, genre /sl/bla
public String getLinkToThis() throws UnsupportedEncodingException {
	if (this.linkToThis == null) {
		this.linkToThis = computelinkToThis();
	}
	return this.linkToThis;
}

/* NON ne va pas : partout ailleurs, retourne qlq chose genre "/welcome.do"
 * pas http://...
 * ET les parmas véentuels !!!! (sauf si tout ala tag)
 * ET QUID des post (-> ceci ne résoudrait pas tout
 * (rq pour les posts, on peut mettre ceci ds un hidden tag de la form)
 * TODO : REPRENDRE TOUT CA
 */
// protected String computelinkToThis() throws UnsupportedEncodingException { return this.request.getRequestURL().toString(); }
// attention, genre /sl/bla
protected String computelinkToThis() throws UnsupportedEncodingException {
	String x = this.request.getServletPath();
	String s = this.request.getPathInfo();
	if (s != null) x = x + s;
	return x;
}

/**
 * @param action par ex /nextImage.do
 * @return la page pour html:link ???
 * @throws UnsupportedEncodingException
 */
public String getLinkToThis(String action) throws UnsupportedEncodingException {
	return null;
}

public String getLinkToThisWithParams(String params) throws UnsupportedEncodingException {
	String s = getLinkToThis();
	if (s.indexOf("?") > -1) return s + "&" + params; // todo encoding de &
	else return s + "?" + params;
}

/** Retourne null ou la page de logon.
 * @return
 * @throws MalformedURLException
 * @throws UnsupportedEncodingException
 */
public String logonPage() throws MalformedURLException, UnsupportedEncodingException {
	
	
	boolean useLogonPage = SLServlet.useLogonPage();
	if (useLogonPage) {
		String c = getContextUrl();
		// on place ds la session là d'où on vient pour pouvoir y revenir après
		this.request.getSession().setAttribute("net.semanlink.servlet.goBackToPage", c + getLinkToThis());
		return c + "/sl/about/LOGON.htm";
	} else {
		if (SLServlet.isEditorByDefault()) {
			// ceci ne devrait pas arriver (le cas isEditorByDefault devrait être
			// traité par l'ihm qui n'appelle pas cette fct dans ce cas.
			// Par sécurité, on reste où on est en settant l'editor et edit à true
			return getLinkToThisWithParams("editor=true&amp;edit=true");
		} else {
			return null;
		}
	}
}

// OLD STUFF
//public String logonPage() throws MalformedURLException, UnsupportedEncodingException {
//	String logonPage = SLServlet.getLogonPage(); // absolu ou relatif à la servlet
//	if (logonPage != null) {
//		// on place ds la session là d'où on vient pour pouvoir revenir après le go sur la page qui
//		// sert à contrôler l'accès (ex page protégée arca)
//		// Ce qui est un peu ennuyeux,c'est que s'il y a echec du logon, 
//		// on reste avec cette chose dans la session // TODO - d'ailleurs, tout ceci est juste pour sicg
//		// virer ça d'ici ?
//		this.request.getSession().setAttribute("net.semanlink.servlet.goBackTo", this);
//		return (new URL(new URL(getContextUrl()), logonPage)).toString();
//	} else {
//		if (SLServlet.isEditorByDefault()) {
//			// ceci ne devrait pas arriver (le cas isEditorByDefault devrait être
//			// traité par l'ihm qui n'appelle pas cette fct dans ce cas.
//			// Par sécurité, on reste où on est ens ettant l'editor et edit à true
//			return getLinkToThisWithParams("editor=true&amp;edit=true");
//		} else {
//			return null;
//		}
//	}
//}


//
// DOC LIST
//

public Bean_DocList getDocList() throws Exception {
	if (this.docList == null) {
		this.docList = computeDocList();
	}
	return this.docList;
}
protected Bean_DocList computeDocList() throws Exception { return null; }
public void setDocList(Bean_DocList docList) {
	this.docList = docList;
}

public String nbDocsMessage() throws Exception {
	int nn = getDocList().getList().size();
	
	java.text.MessageFormat messageFormat;
	if (nn > 1) {
		messageFormat = new java.text.MessageFormat(i18l("x.documents"));
	} else {
		messageFormat = new java.text.MessageFormat(i18l("x.document"));
	}
	Object[] args = new Object[1];
	args[0] = Integer.toString(nn);
	return messageFormat.format(args);	
}



/** Retourne true ssi il faut montrer les keywords d'un doc de getDocList.*/
public boolean getShowKeywordsInDocList() { return this.showKeywordsInDocList; }
public void setShowKeywordsInDocList(boolean b) { this.showKeywordsInDocList = b; }

//
// SORT
//

public void setSortProperty(String sortProperty) { this.sortProperty = sortProperty; }
public String getSortProperty() { 
	if (this.sortProperty == null) {
			this.sortProperty = SLServlet.getJspParams().getDefaultSortProperty();
	}
	return this.sortProperty;
}
/** attention si on veut ne pas prendre en compte un kw ds le sortDocsByKws : pas fait ici */
protected void sort(List<SLDocument> docList) {
	String sortProp = getSortProperty();
	if (SLVocab.HAS_KEYWORD_PROPERTY.equals(sortProp)) {
		SLUtils.sortDocsByKws(docList, null);
	} else {
		SLUtils.sortByProperty(docList, sortProp);
	}
}


public boolean showBtnEdit() {
	if (SLServlet.isEditorByDefault()) return true;
	if (SLServlet.useLogonPage()) return true;
	return false;
}

public boolean isEditor() {
	return isEditor(request);
}

// 2019-09
public static boolean isEditor(HttpServletRequest request) {
	Boolean b = (Boolean) request.getSession().getAttribute("net.semanlink.servlet.editor");
	boolean x;
	if (b == null) {
		x = SLServlet.isEditorByDefault();
		if (x) {
			b = Boolean.TRUE;
		} else {
			// 2019-09
//			String logonPage = SLServlet.getLogonPage();
//			if (logonPage == null) {
//				b = Boolean.FALSE;
//			} else {
//				b = Boolean.TRUE;
//			}
			boolean useLogonPage = SLServlet.useLogonPage();
			if (useLogonPage) {
				b = Boolean.TRUE;
			} else {
				b = Boolean.FALSE;
			}
		}
		b = new Boolean(x);
		request.getSession().setAttribute("net.semanlink.servlet.editor", b);
	} else {
		x = b.booleanValue();
	}
	return x;
}





/** en principe, au moment de l'appel, request a déjà été augmentée des attributs liés au traitement des actions. */
public String getTemplate() throws Exception {
	return SLServlet.getJspParams().getTemplate(request);
}

public String getTopMenu() throws Exception {
	String x = SLServlet.getJspParams().getTopMenu(request);
	if (x == null) {
		// hack pour cas sicg par default
		if (isEditor()) x = "/jsp/topmenu.jsp";
	}
	return x;
}

public String getSideMenu() throws Exception {
	return SLServlet.getJspParams().getSideMenu(request);
}


//
// pour les box de linked keywords // tagcloud
//

public HTML_Link linkToThisAndKw(SLKeyword kw) throws Exception { return null; }

public SLKeywordNb[] getLinkedKeywordsWithNb() throws Exception {
	/*HashMap kw2nb = getLinkedKeywords2NbHashMap();
	if (kw2nb == null) return null;
	return getLinkedKeywordsWithNb(kw2nb);*/ // OKOK
	return getSmartLinkedKeywordsWithNb();
}
/** key: a linked SLKeyword, data nb d'occurrences. */
public HashMap<SLKeyword, Integer> getLinkedKeywords2NbHashMap() throws Exception { return null; }

//static SLKeywordNb[] getLinkedKeywordsWithNb(HashMap<SLKeyword, Integer> kw2nb) {
//	Set<SLKeyword> keys = kw2nb.keySet();
//	int n = keys.size();
//	SLKeywordNb[] x = new SLKeywordNb[n];
//	Iterator<SLKeyword> it = keys.iterator();
//	for (int i = 0; i < n; i++) {
//		SLKeyword linkedKw = it.next() ;
//		Integer nb = kw2nb.get(linkedKw);
//		x[i] = new SLKeywordNb(linkedKw, nb.intValue());
//	}
//	Arrays.sort(x);
//	return x;
//}

static SLKeywordNb[] getLinkedKeywordsWithNb(HashMap<SLKeyword, Integer> kw2nb) {
	Set<SLKeyword> keys = kw2nb.keySet();
	int n = keys.size();
	SLKeywordNb[] x = null;
	if (n > TAG_CLOUD_MAX_SIZE) {
		// remove from the tag cloud tags with low nb
		// We count the nb of tags for each value of nb
		// minimal_n: will be the minimal nb got  tag to be kept
		HashMap<Integer, Integer> nb2n = new HashMap<>();
		for(Entry<SLKeyword, Integer> e : kw2nb.entrySet()) {
			Integer nb = e.getValue();
			Integer nn = nb2n.get(nb);
			if (nn == null) {
				nn = new Integer(1);
			} else {
				nn = new Integer(nn.intValue() + 1);
			}
			nb2n.put(nb,  nn);
		}
		int nn = keys.size();
		int minimal_n = 0;
		for (int i = 1 ; i < nb2n.size(); i++) {
			Integer k = nb2n.get(new Integer(i));
			if (k == null) continue;
			nn = nn - k.intValue();
			if (nn <= TAG_CLOUD_MAX_SIZE) {
				if (nn < TAG_CLOUD_MINIMAL_NB) {
					// don't reduce too much
					minimal_n = i;
				} else {
					minimal_n = i+1;
				}
				break;
			} else {
				
			}
		}
		
		// 2020-01
		// ne garder un kw dans le tag cloud que s'il apparait au moins TAG_CLOUD_MINIMAL_NB fois
		ArrayList<SLKeywordNb> al = new ArrayList<>();
		Iterator<SLKeyword> it = keys.iterator();
		for (int i = 0; i < n; i++) {
			SLKeyword linkedKw = it.next() ;
			Integer nb = kw2nb.get(linkedKw);
			if (nb.intValue() >= minimal_n) {
				al.add(new SLKeywordNb(linkedKw, nb.intValue()));
			}
			x = al.toArray(new SLKeywordNb[al.size()]);
		}
		
	} else {
		x = new SLKeywordNb[n];
		Iterator<SLKeyword> it = keys.iterator();
		for (int i = 0; i < n; i++) {
			SLKeyword linkedKw = it.next() ;
			Integer nb = kw2nb.get(linkedKw);
			x[i] = new SLKeywordNb(linkedKw, nb.intValue());
		}
	}
	Arrays.sort(x);
	return x;
}


/** the method called by the jsp to display the tag cloud 
 *  Redefined in Jsp_Keyword */
public SLKeywordNb[] getSmartLinkedKeywordsWithNb() throws Exception {
	// les kws et leur nb d'occurence
	HashMap<SLKeyword, Integer> directlyLinkedHM = getLinkedKeywords2NbHashMap();
	// return getLinkedKeywordsWithNb(directlyLinkedHM);
	return getSmartLinkedKeywordsWithNb(directlyLinkedHM);
}

public SLKeywordNb[] getSmartLinkedKeywordsWithNb (HashMap<SLKeyword, Integer> directlyLinkedHM) throws Exception {
	if (directlyLinkedHM == null) return null;
	SLModel model = getSLModel();
	// pour chaque kw directement lié, on regarde s'il a des ancetres qui sont aussi directement liés
	// Ces ancêtres doivent voir leur nb d'occurrences augmenter, mais on ne peut le faire de suite,
	// parce que si un pere et un grand-pere sont ds les directly, on augmenterait deux fois le nb d'occurences
	// pour le grand-pere
	// -> une nlle hm pour remplacer directlyLinkedHM
	HashMap<SLKeyword, Integer> directlyLinkedHM_Updated = new HashMap<>();
	Set<SLKeyword> directlyLinkedSet = directlyLinkedHM.keySet();
		
	HashSet<SLKeyword> toBeRemoved = new HashSet<>(); // 2020-01: don't have CamemBERT in the tag cloud if you have BERT
	
	for (Iterator<SLKeyword> directIte = directlyLinkedSet.iterator(); directIte.hasNext() ; ) {
		SLKeyword kw = directIte.next();
		
		// ATTENTION : un mystère ici : si je ne recrée pas les SLKeyword ici, TRES PROBABLEMENT (pas revérifié)
		// le SLFastTree est vide !!! (les SLKeyword dans directlyLinked ne semblent pas avoir de parents !!!)
		// SLFastTree fastTree = new SLFastTree(directlyLinkedElt.getKw(), SLVocab.HAS_PARENT_PROPERTY, model);
		// directlyLinkedKw = model.getKeyword(directlyLinkedKw.getURI());
		
		Integer nb = directlyLinkedHM.get(kw);
		SLFastTree fastTree = new SLFastTree(kw, SLVocab.HAS_PARENT_PROPERTY, model);		
		HashSet<SLKeyword> kwsInTree = fastTree.getKwsSet();
		for (Iterator<SLKeyword> ite = kwsInTree.iterator() ; ite.hasNext() ;) {
			SLKeyword ancetre = ite.next();
			// ancetre est il ds directlyLinked ?			
			// notons qu'ancetre peut être directlyLinkedKw lui même
			// if ancetre not in directlyLinkedHM, (that is, if nb_ancetre != null, forget it
			Integer nb_ancetre = directlyLinkedHM.get(ancetre);
			if (nb_ancetre != null) {
				Integer updated_nb_ancetre = directlyLinkedHM_Updated.get(ancetre);
				if (updated_nb_ancetre == null) {
					updated_nb_ancetre = new Integer(0); // yes, 0. At one time, ancetre is kw, so it will be counted below
				}
				updated_nb_ancetre = new Integer(updated_nb_ancetre.intValue() + nb.intValue());
				directlyLinkedHM_Updated.put(ancetre, updated_nb_ancetre);
				// 2020-01: don't have CamemBERT in the tag cloud if you have BERT
				if (!ancetre.equals(kw)) {
//					// virer ce kw s'il a un ancêtre (autre que lui-même) dans les directement liés -- Pb : ça vire NLP si IA présent
//					// toBeRemoved.add(directlyLinkedKw);
//					// ne le faire que si directlyLinkedKw n'a pas d'enfants
//					List<SLKeyword> sons = directlyLinkedKw.getChildren();
//					if ((sons == null) || (sons.size() == 0)) {
//						toBeRemoved.add(directlyLinkedKw);
//					}
					// note it as to be removed -- but at removal time,
					// we won't remove it if has a big nb
					toBeRemoved.add(kw);
				}
			}
		} // boucle sur les ancetres de kw
	}
	
	/* // non : en fait les directlyLinked sont tous déjà ds ancetre2augmentNb
	for (Iterator directIte = directlyLinkedSet.iterator(); directIte.hasNext() ; ) {
		SLKeyword directlyLinkedKw = (SLKeyword) directIte.next();
		Integer augmentNb = (Integer) ancetre2augmentNb.get(directlyLinkedKw);
		if (augmentNb != null) {
			Integer nb = (Integer) directlyLinkedHM.get(directlyLinkedKw);
			nb = new Integer (nb.intValue() + augmentNb.intValue() );
			directlyLinkedHM.put( directlyLinkedKw, nb);
		}
	}
	return getLinkedKeywordsWithNb(directlyLinkedHM); */
	
	// 2020-01: don't have CamemBERT in the tag cloud if you have BERT 
	for (SLKeyword kw : toBeRemoved) {
		// don't remove if it has a big nb
		// (this way for instance, NLP won't be removed because AI is present)
		Integer nb = directlyLinkedHM_Updated.get(kw);
		if (nb.intValue() < TAG_CLOUD_REMOVE_LIMIT) {
			directlyLinkedHM_Updated.remove(kw);
		}
	}
	
	return getLinkedKeywordsWithNb(directlyLinkedHM_Updated);
}

/** dans cette version, on retient, en sus des tags directements liés,  tous les tags dont 2 descendants
 *  sont directement liés.
 *  Défaut : Géographie est le plus lié à Archéologie.
 */
public SLKeywordNb[] getSmartLinkedKeywordsWithNbV1 () throws Exception {
	// les kws et leur nb d'occurence
	SLKeywordNb[] directlyLinked = getLinkedKeywordsWithNb();
	if (directlyLinked == null) return null;
	// pour chaque kw ancêtre d'un elt de directlyLinked, on calcule :
	// -1) s'il apparaît une fois ou plusieurs dans les ancêtres de directlyLinked
	// -2) la somme des occurrences d'un de ses descendants en tant qu'elt de directlyLinked
	// On retiendra dans x les tels elts apparaissant plusieurs fois en tant qu eancêtres de directlyLinked
	// affectés de la somme des occurrences de ses descendants
	// (et, of course, tous les elts de directlyLinked)
	// Pour 1, on utilise :
	// - une HashMap dans lequel on met les ancêtres de directlyLinked, avec la somme des nb d'occurences des directlyLinkedElts
	// clé : SLKeyword, data : SLKeywordNb
	HashMap<SLKeyword, SLKeywordNb> ancetres = new HashMap<>();
	// - un HashSet dans lequel on met ceux dont on constate qu'il apparaîssent une seconde fois
	HashSet<SLKeywordNb> xHs = new HashSet<>();
	SLModel model = getSLModel();
	for (int i = 0 ; i < directlyLinked.length ; i++) {
		SLKeywordNb directlyLinkedElt = directlyLinked[i];
		int nbOccurrences = directlyLinkedElt.getNb();

		// ATTENTION : un mystère ici : si je ne recrée pas les SLKeyword ici
		// le SLFastTree est vide !!! (les SLKeyword dans directlyLinked ne semblent pas avoir de parents !!!)
		// SLFastTree fastTree = new SLFastTree(directlyLinkedElt.getKw(), SLVocab.HAS_PARENT_PROPERTY, model);
		String uri = directlyLinkedElt.getKw().getURI();
		SLKeyword kw = model.getKeyword(uri);
		SLFastTree fastTree = new SLFastTree(kw, SLVocab.HAS_PARENT_PROPERTY, model);
		
		HashSet<SLKeyword> kwsInTree = fastTree.getKwsSet();
		for (Iterator<SLKeyword> ite = kwsInTree.iterator() ; ite.hasNext() ;) {
			SLKeyword ancetre = ite.next();
			SLKeywordNb ancetreWithNb = ancetres.get(ancetre);
			if (ancetreWithNb == null) {
				ancetreWithNb = new SLKeywordNb(ancetre, nbOccurrences);
				ancetres.put(ancetre, ancetreWithNb);
			} else {
				ancetreWithNb.plus(nbOccurrences);
				xHs.add(ancetreWithNb);
			}
		}
	}
	// ajouter à x les elts de directlyLinked qui n'y serait pas
	for (int i = 0 ; i < directlyLinked.length ; i++) {
		SLKeywordNb directlyLinkedElt = directlyLinked[i];
		if (!(xHs.contains(directlyLinkedElt))) xHs.add(directlyLinkedElt);
	}
	SLKeywordNb[] x = new SLKeywordNb[xHs.size()];
	xHs.toArray(x);
	Arrays.sort(x);
	return x;
}
// rss

public String rssFeedUriRelativToSL() throws UnsupportedEncodingException {
	return null;
}

// avait été fait sur le modèle de rssFeedUriRelativToSL, no more used: remplacé par des appels à linkToRDF. Est-ce bien ? voir Jsp_Keyword
public String linkToRDFRelativToSL() throws Exception {
	return null;
}

//voir addDoc ds RDFOutput (question d'homogénéité de la forme des uri des kws !!!
// 2013-08: replaces public String linkToRDF(boolean includeDotRDFInUri)
/**
 * @param extension eg "rdf" - if null, nothing addded
 */
public String linkToRDF(String extension) throws Exception {
	// prendre getRequestURL ne va pas pour le cas http://.../semanlink, qui redirige vers semanlink/sl/new :
	// il faut reconstituer le path à partir de ses composants
	// StringBuffer sb = this.request.getRequestURL(); // ET POURQUOI PAS ???
	StringBuffer sb = new StringBuffer(getContextUrl());
	sb.append(request.getServletPath());
	// 2013-08
	String s = request.getPathInfo();
	/* // 2013-08
	if (s.endsWith(".html")) sb = new StringBuffer(s.substring(0,s.length()-5));
	if (includeDotRDFInUri) {
		if (! ( (s.endsWith(".rdf")) || (s.endsWith(".n3")))) sb.append(".rdf");
	} */
	sb.append(Util.getWithoutExtension(s));
	if (extension != null) {
		sb.append(".");
		sb.append(extension);
	}
	
	// can't we change all this for request.getQueryString() ?
	// No: we would have "&" instead of "&amp;"
	Enumeration<String> e = this.request.getParameterNames();
	if (e.hasMoreElements()) {
		sb.append("?");
		String param = e.nextElement() ;
		String[] vals = this.request.getParameterValues(param);
		sb.append(param);
		sb.append("=");
		sb.append(vals[0]);
		for (int i = 1; i < vals.length; i++) {
			sb.append("&amp;");
			sb.append(param);
			sb.append("=");
			sb.append(URLUTF8Encoder.encode(vals[i]));
		}
		for (;e.hasMoreElements();) {
			param = e.nextElement() ;
			vals = this.request.getParameterValues(param);
			for (int i = 0; i < vals.length; i++) {
				sb.append("&amp;");
				sb.append(param);
				sb.append("=");
				sb.append(URLUTF8Encoder.encode(vals[i]));
			}
		}
	}
	return sb.toString();
}

// @find rdfparser
public String linkToRdfJs() throws UnsupportedEncodingException, Exception {
	StringBuffer sb = new StringBuffer(this.request.getContextPath()); // /semanlink
	sb.append("/sl/rdfjs?uri=");
	sb.append(URLEncoder.encode(linkToRDF("rdf"), "UTF-8"));
	return sb.toString();
}

//

public String getLabel(SLDocument doc) throws IOException, URISyntaxException {
	return SLUtils.getLabel(doc);
}

//

public SLKeyword getFavori() {
	return getSLModel().getFavori();
}


//
// 2006-02
//

/** Complete URL to this page, beginning with http://, without URL rewriting. 
 * NO PARAMETERS in this implementation: subclasses should overwrite in case of parameters are needed (eg: search)
 * Not optimized;
 * @throws MalformedURLException */
public String completePath() throws UnsupportedEncodingException, MalformedURLException {
	return (new URL( new URL(request.getRequestURL().toString()), relToHostPath() )).toString() ;
}

/** Path to this page, relativ to host:port, beginning with "/". 
 *  Don't forget URL rewriting. 
 *  (should not have been necessary if I had only one servlet)
 *  Not to be used in html:link struts tag. 
 * @throws UnsupportedEncodingException */
public String relToHostPath() throws UnsupportedEncodingException {
	// return this.request.getContextPath() + relToContextPath();
	return this.request.getContextPath() + relToContextPath();
}

/** Path to this page, relativ to host:port/context, beginning with "/". 
 *  To be used with html:link struts tag. 
 * @throws UnsupportedEncodingException */
public String relToContextPath() throws UnsupportedEncodingException {
	return getLinkToThis();
}

//
// RDF
//

public Model getRDF(String extension) throws Exception {
	RDFOutput rdfOutput = new RDFOutput(this, extension);
	return rdfOutput.getModel();
}

//
//
//

public String i18l(String key) {
	return getI18l().getString(key);
}

public I18l getI18l() {
	if (this.i18l == null) {
		this.i18l = I18l.getI18l(this.request.getSession());
	}
	return this.i18l;
}
}