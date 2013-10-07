package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.http.*;

import org.apache.struts.action.*;

import net.semanlink.semanlink.*;
/**
 * Des methodes utiles aux differentes actions
 */
public class BaseAction extends Action {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	String path = mapping.getPath();
	Jsp_Page jsp = null;
	if (path.equals("/welcome")) {
		jsp = new Jsp_Welcome(request);
	} else {
		jsp = new Jsp_Page(request);
		if (path.equals("/newdocform")) {
			jsp.setTitle("New Document");
			jsp.setContent("/jsp/bookmarkform.jsp");
		} else if (path.equals("/help")) {
			jsp.setTitle("Help");
			jsp.setContent("/aboutfiles/help.htm");			
		}
	}
	// if (jsp == null) jsp = new Jsp_Welcome(request);
	request.setAttribute("net.semanlink.servlet.jsp", jsp);
	return mapping.findForward("continue");
} // end Action


public ActionForward error(ActionMapping mapping, HttpServletRequest request, Throwable e ) {
	request.setAttribute("net.semanlink.servlet.error", e);
	e.printStackTrace();
	return mapping.findForward("error");
}

public ActionForward error(ActionMapping mapping, HttpServletRequest request, String errorMess ) {
	request.setAttribute("net.semanlink.servlet.error", errorMess);
	return mapping.findForward("error");
}


static void setEdit(HttpServletRequest request) {
	request.getSession().setAttribute("net.semanlink.servlet.edit",new Boolean(!(getSessionEditState(request))));	
}

// 2013-04 set to public for jersey test with params @find jerseyification
public static void setEdit(HttpServletRequest request, boolean b) {
	request.getSession().setAttribute("net.semanlink.servlet.edit",new Boolean(b));	
}

static boolean getSessionEditState(HttpServletRequest request) {
	HttpSession session = request.getSession();
	boolean currentState;
	Object o = session.getAttribute("net.semanlink.servlet.edit");
	if (o == null) {
		currentState = false;
	} else {
		currentState =  ((Boolean) o).booleanValue();
	}
	return currentState;
}


/** Retourne null ou recupere le param kwuri et documente request avec ce qu'il faut pour la suite
 * * @deprecated since post redirect */
Jsp_Keyword handleKwUriParam(HttpServletRequest request) throws UnsupportedEncodingException {
	SLKeyword kw = getSLKeyword(request);
	if (kw == null) return null;
	Jsp_Keyword jspKw = new Jsp_Keyword(kw, request);
	request.setAttribute("net.semanlink.servlet.jsp", jspKw);	
	return jspKw;
}

/** Retourne null ou recupere le param kwuri */
SLKeyword getSLKeyword(HttpServletRequest request) throws UnsupportedEncodingException {
	SLModel mod = SLServlet.getSLModel();
	String uri = getTagUri(request);
	if (uri == null) return null;
	// JE NE COMPRENDS PAS MAIS ... (RECHERCHER CA)
	// uri = java.net.URLDecoder.decode(uri,"UTF-8");
	return mod.getKeyword(uri);
}

String getTagUri(HttpServletRequest request) {
	// JE NE COMPRENDS PAS MAIS ... (RECHERCHER CA)
	// uri = java.net.URLDecoder.decode(uri,"UTF-8");
	return request.getParameter("kwuri");
}

/** Retourne null ou recupere le param docuri et documente request avec ce qu'il faut pour la suite 
 * @throws Exception
 * @deprecated since post redirect */
Jsp_Document handleDocUriParam(HttpServletRequest request) throws Exception {
	SLDocument doc = getSLDocument(request);
	if (doc == null) return null;
	return getJsp_Document(doc, request);
}

/** Retourne null ou recupere le param docuri
 * @throws Exception*/
SLDocument getSLDocument(HttpServletRequest request) throws Exception {
	SLModel mod = SLServlet.getSLModel();
	String uri = getDocUri(request);
	if (uri == null) return null;
	// JE NE COMPRENDS PAS MAIS ... (RECHERCHER CA)
	// uri = java.net.URLDecoder.decode(uri,"UTF-8");
	return mod.getDocument(uri);
}

String getDocUri(HttpServletRequest request) {
	// JE NE COMPRENDS PAS MAIS ... (RECHERCHER CA)
	// uri = java.net.URLDecoder.decode(uri,"UTF-8");
	return request.getParameter("docuri");
}

/** Retourne null ou recupere le param kwuris et documente request avec ce qu'il faut pour la suite. 
 * @throws Exception*/
Jsp_AndKws handleKwUrisParam(HttpServletRequest request) throws Exception {
	String[] uris = request.getParameterValues("kwuris");
	if (uris == null) return null;
	// JE NE COMPRENDS PAS MAIS ... (RECHERCHER CA) // en fait, je faias et reproduit le comment des méthodes précédentes
	// uri = java.net.URLDecoder.decode(uri,"UTF-8");
	Jsp_AndKws jsp = new Jsp_AndKws(uris, request);
	request.setAttribute("net.semanlink.servlet.jsp", jsp);	
	return jsp;
}


/** Retourne null ou recupere le param pptyuri et documente request avec ce qu'il faut pour la suite 
 * @throws Exception*/
Jsp_Property handlePptyUriParam(HttpServletRequest request) throws Exception {
  	Jsp_Property jsp = null;
    String propertyUri = request.getParameter("pptyuri");
	if (propertyUri == null) return null;
    propertyUri = java.net.URLDecoder.decode(propertyUri,"UTF-8");
    String objectUri = request.getParameter("objuri");
    String[] kwuris = request.getParameterValues("kwuris");
    if (kwuris != null) {
	    for (int i = 0; i < kwuris.length; i++) {
	    		kwuris[i] = java.net.URLDecoder.decode(kwuris[i],"UTF-8");
	    }
    }
    if (objectUri != null) {
	    	//objectUri = java.net.URLDecoder.decode(objectUri,"UTF-8");
	    	// docList = mod.getDocumentsList(propertyUri, objectUri);
	    	jsp = new Jsp_Property(propertyUri, objectUri, kwuris, request);   	
    } else {
    		String propertyValue = request.getParameter("pptyval");
	    // je n'avais pas la ligne suivnate, jusqu'à pb avec find François-Paul 2004-08
    		// (j'ai du aussi faire un double encodage, voir HTML_Link)
	    propertyValue = java.net.URLDecoder.decode(propertyValue,"UTF-8");
	    String lang = request.getParameter("lang");
		if ( ("".equals(lang)) || ("-".equals(lang)) ) lang = null;
	    // docList = mod.getDocumentsList(propertyUri, propertyValue, lang);
	    jsp = new Jsp_Property(propertyUri, propertyValue, lang, kwuris, request);   	
    }
    
	/*String imageToBeDisplayed = request.getParameter("imagetobedisplayed");
	if (imageToBeDisplayed != null) {
		// JE NE COMPRENDS PAS POURQUOI MAIS,
		// alors que docuri a ete encode, il ne faut pas ici le decoder.
		// VOIR AUSSI ids Action_NextImage et Action_ShowKeyword
		jsp.setImageToBeDisplayed(SLServlet.getSLModel().getDocument(imageToBeDisplayed), -1); // en vrai, on n'a besoin que de l'uri - at this time
	}*/
   
    request.setAttribute("net.semanlink.servlet.jsp", jsp);
	return jsp;
}

/** attention, param2SessionAttribute et booleanParam2SessionAttribute n'ont pas
 * pas le meme type de comportement pour ce qui est de l'absence de valeur. // TODO
 */ 
protected void booleanParam2SessionAttribute(String paramName, HttpServletRequest request) {
	String s = request.getParameter(paramName);
	if (s != null) {
		HttpSession session = request.getSession();
		Boolean b = (Boolean) session.getAttribute("net.semanlink.servlet." + paramName);
		if (b == null) b = Boolean.FALSE;
		session.setAttribute("net.semanlink.servlet." + paramName, new Boolean(!b.booleanValue()));
	}
}

/** attention, param2SessionAttribute et booleanParam2SessionAttribute n'ont pas
 * pas le meme type de comportement pour ce qui est de l'absence de valeur. // TODO
 */ 
static void param2SessionAttribute(String paramName, HttpServletRequest request) {
	HttpSession session = request.getSession();
	String s = request.getParameter(paramName);
	if (s != null) {
		session.setAttribute("net.semanlink.servlet." + paramName, s);
	} else {
		session.removeAttribute("net.semanlink.servlet."+ paramName);
	}
}

public static boolean param2boolean(String paramName, HttpServletRequest request, boolean bDefault) {
	String s = request.getParameter(paramName);
	if (s != null) {
		s = s.toLowerCase();
		if ("true".equals(s)) return true;
		if ("false".equals(s)) return false;
		return bDefault;
	} else {
		return bDefault;
	}
}

/** Retourne le SLKeyword auquel il est fait référence dans la request via le label donné dans 
 * le parameter labelParamName
 * @param createsKwIfDoesntExist : si true, crée au besoin les statements dans le model, 
 * sinon, non (mais retourne quand même un SLKeyword)
 * 
 * Pourrait s'améliorer : si il y a une kwuri par ailleurs ds la req, prendre la base correspondante
 * (cas ajout d'un fils,...)
 */
/*protected SLKeyword request2SLKeyword(String labelParamName, HttpServletRequest request, SLModel mod, boolean createsKwIfDoesntExist) throws Exception {
	String kwLabel = request.getParameter(labelParamName);
	if ((kwLabel == null) || (kwLabel.equals(""))) throw new IllegalArgumentException("No kw label");
	String thesaurusURI = request.getParameter("thesaurusuri");
	if (thesaurusURI == null) {
		thesaurusURI = (String) request.getSession().getAttribute("net.semanlink.servlet.thesaurusuri");
	} else {
		request.getSession().setAttribute("net.semanlink.servlet.thesaurusuri", thesaurusURI);
	}
	String kwuri;
	if (createsKwIfDoesntExist) {
		String lang = request.getParameter("lang");
		if (lang == null) lang = "fr"; //TODO
		kwuri = mod.kwLabel2UriCreatingKwIfNecessary(kwLabel, lang, thesaurusURI);
	} else {
		kwuri = mod.kwLabel2Uri(kwLabel, thesaurusURI);
	}
	return mod.getKeyword(kwuri);
}*/

//
// DESIGNER UNE URI DE PROPERTY DS LA REQUEST
//

/** Retourne l'uri de la propriete designee dans la request. 
 *  Celle ci est caracterisee par son parameter "property".
 *  @return uri de la proriete a affecter au document, ou null.
 */
protected static String getPropertyUri(HttpServletRequest request) {
	/*String param = request.getParameter("property");
	return pptyShortName2PptyUri(param);*/
	String param = null;
	// traitement du cas d'une prop autre ds le champs de saisie
	// String s = request.getParameter("otherprop");
	// if ((s == null) || ("".equals(s))) {
		String s = request.getParameter("property");
	// }
	String x = SLServlet.getSemanlinkConfigProps().getUriString(s);
	if (x == null) x = s;
	return x;
}

// De quoi gerer des noms courts de proprietes pouvant etre passes comme parameter de la request
/** Retourne l'uri de property correspondant a un parameter "property" acceptable de la request.
 *  Retourne null si ce n'est pas le cas.
 */
/*
static String pptyShortName2PptyUri(String shortName) {
	SLVocab.EasyProperty[] easyProps = SLServlet.getEasyProps(); 
	for (int i = 0; i < easyProps.length; i++) {
	for (int i = 0; i < SLVocab.COMMON_PROPERTIES.length; i++) {
		if (SLVocab.COMMON_PROPERTIES[i].getName().equals(shortName)) return SLVocab.COMMON_PROPERTIES[i].getUri();
	}
	return null;
}*/

//
// DEBUG
//

public static void printParams(HttpServletRequest request) {
	System.out.println("request params:");
	Enumeration names = request.getParameterNames();
	for (;names.hasMoreElements();) {
		String name = (String) names.nextElement();
		System.out.println(name + " : " + request.getParameter(name));
	}
}

public static void printSessionAttributes(HttpServletRequest request) {
	System.out.println("session atts:");
	HttpSession session = request.getSession();
	Enumeration names = session.getAttributeNames();
	for (;names.hasMoreElements();) {
		String name = (String) names.nextElement();
		System.out.println(name + " : " + session.getAttribute(name));
	}
}


//
// JSP_DOCUMENT
//

public static Jsp_Document getJsp_Document(SLDocument doc, HttpServletRequest request) throws Exception {
	Jsp_Document jsp = Manager_Document.getDocumentFactory().newJsp_Document(doc, request);
	request.setAttribute("net.semanlink.servlet.jsp", jsp);
	return jsp;
}
} // end Action
