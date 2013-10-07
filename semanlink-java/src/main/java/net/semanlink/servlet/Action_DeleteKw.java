package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
/**
 */
public class Action_DeleteKw extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		SLModel mod = SLServlet.getSLModel();
		String kwuri = request.getParameter("kwuri");
		// kwuri = java.net.URLDecoder.decode(docuri);
		SLKeyword kw = mod.getKeyword(kwuri);
		//
		doIt(request, mod, kw);
		/*
		// on réaffiche le même kw, maintenant inconnu (bof, pas génial)
	    kw = mod.getKeyword(kwuri);
		Jsp_Page jspKw = new Jsp_Keyword(kw, request);
		request.setAttribute("net.semanlink.servlet.jsp", jspKw);
		*/
		// x = mapping.findForward("continue");
		// POST REDIRECT 
		// on réaffiche le même kw, maintenant inconnu
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kwuri, false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
	  } catch (Exception e) {
	    return error(mapping, request, e );
	  }
	return x;
} // end execute

/**
 * Permet de sous-classer this pour lui faire modifier, par ex les parents au lieu des enfants.
 * @param kw le SLKeyword dont une liste est modifiee (par ex dont la liste des children est modifiee)
 */
protected void doIt(HttpServletRequest request, SLModel mod, SLKeyword kw) {
	// DOIT ETRE INUTILE DE PASSER VIA KW (uri suffirait)
	mod.delete(kw);
}
} // end Action
