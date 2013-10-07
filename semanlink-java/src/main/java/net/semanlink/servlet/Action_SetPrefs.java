package net.semanlink.servlet;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
/**
* Attention, le code ici pour retourner sur la bonne page ne fonctionne que pour kw et doc. A reprendre pour andKws, etc...
 */
public class Action_SetPrefs extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		HttpSession session = request.getSession();
		// uri de retour (où aller après 
		// String uri = request.getParameter("uri"); // ne faut-il pas decoder ?

    booleanParamIntoAttribute("longListOfDocs", request, session);
    boolean longListOfDocs = "true".equals(request.getParameter("longListOfDocs"));
    DisplayMode displayMode = new DisplayMode(request.getParameter("childrenAs"), longListOfDocs);
    session.setAttribute("net.semanlink.servlet.displayMode", displayMode);
    
		
    String sortProp = getPropertyUri(request);
    if ( (sortProp  != null) && (!("".equals(sortProp))) ) {
    		session.setAttribute( "net.semanlink.servlet.SortProperty", sortProp);
    } else {
    		session.setAttribute( "net.semanlink.servlet.SortProperty", SLVocab.HAS_KEYWORD_PROPERTY); // cas * Keywords *
    }
    
    booleanParamIntoAttribute("imagesonly", request, session);
    
    String lang = request.getParameter("lang");
    if (lang != null) {
  		session.setAttribute("net.semanlink.servlet.lang", lang);
    }
    
    // 2005/05
		String action = request.getParameter("action");
		if (action != null) {
			ActionForward x = new ActionForward("/semanlink/jsp/" + action + ".do");
			return x;
		}
		
		// POST REDIRECT 
		/*if (handleKwUriParam(request) != null) {
		} else if (handleDocUriParam(request) != null) {
		} else if (handleKwUrisParam(request) != null) {
		} else {
		}
		return mapping.findForward("continue");*/
		// The modified resource is the referer
		// note: referer contains sessionId, if cookies are off (and a session maintained)
		// If we use it in a sendRedirect, we won't have to add it
		String referer = request.getHeader("referer");
   	response.sendRedirect(response.encodeRedirectURL(referer));
   	return null;

	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute

static void booleanParamIntoAttribute(String paramName, HttpServletRequest request, HttpSession session) {
    String s = request.getParameter(paramName);
    Boolean b = Boolean.FALSE;
    if (s != null) {
    		b = Boolean.valueOf(s);
    }
    session.setAttribute("net.semanlink.servlet."+paramName, b);
}


} // end Action

