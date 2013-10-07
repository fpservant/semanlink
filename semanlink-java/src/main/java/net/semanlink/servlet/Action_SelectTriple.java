package net.semanlink.servlet;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import net.semanlink.semanlink.*;

/** Demande l'affichage des res ayant une certaine (ppté-value). */
public class Action_SelectTriple extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		handleSPOParams(request);
	  x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute

String handleParam(HttpServletRequest request, String paramName) throws UnsupportedEncodingException {
	String s = request.getParameter(paramName);
	if (s == null) return null;
	s = s.trim();
	if (s.equals("")) return null;
	// return java.net.URLDecoder.decode(s,"UTF-8"); // don't do that!
	return s;
}

Jsp_Page handleSPOParams(HttpServletRequest request) throws Exception {
	Jsp_Page jsp = null;
    String s = handleParam(request, "s");
    String p = handleParam(request, "p");
    String o = handleParam(request, "o");
 	// est-ce une url ?
	URL oUrl = null;
	if ((o != null) && (!("".equals(o)))) {
		try {
			oUrl = new URL(o);
		} catch (Exception e) {}
	}
    
	if (s != null) {
	    SLModel mod = SLServlet.getSLModel();
	    SLDocument doc = mod.getDocument(s);
	    jsp = getJsp_Document(doc, request);

	} else { // s null
	    String[] kwUris = null;
	    if (oUrl != null) {
		    	jsp = new Jsp_Property(p, o, kwUris, request);   	
	    } else {
		    // je n'avais pas la ligne suivnate, jusqu'à pb avec find François-Paul 2004-08
	    		// (j'ai du aussi faire un double encodage, voir HTML_Link)
		    // done ds handleParam propertyValue = java.net.URLDecoder.decode(propertyValue,"UTF-8");
	       	String lang = handleParam(request, "lang");
		    // docList = mod.getDocumentsList(propertyUri, propertyValue, lang);
		    jsp = new Jsp_Property(p, o, lang, kwUris, request);   	
	    }
	    
		/*String imageToBeDisplayed = request.getParameter("imagetobedisplayed");
		if (imageToBeDisplayed != null) {
			// JE NE COMPRENDS PAS POURQUOI MAIS,
			// alors que docuri a ete encode, il ne faut pas ici le decoder.
			// VOIR AUSSI ids Action_NextImage et Action_ShowKeyword
			jsp.setImageToBeDisplayed(SLServlet.getSLModel().getDocument(imageToBeDisplayed), -1); // en vrai, on n'a besoin que de l'uri - at this time
		}*/
	}
    request.setAttribute("net.semanlink.servlet.jsp", jsp);
	return jsp;
}
} // end Action
// docList = mod.getDocumentsList(propertyUri, objectUri);
// docList = mod.getDocumentsList(propertyUri, propertyValue, lang);
