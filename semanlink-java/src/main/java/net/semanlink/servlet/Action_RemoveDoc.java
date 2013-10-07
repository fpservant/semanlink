package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
/**
 */
public class Action_RemoveDoc extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		SLModel mod = SLServlet.getSLModel();
		String uri = request.getParameter("docuri");
		// kwuri = java.net.URLDecoder.decode(uri);
		SLDocument doc = mod.getDocument(uri);
		//
		doIt(request, mod, doc);
		//
		// POST REDIRECT
		// getJsp_Document(doc, request);
		// return mapping.findForward("continue");
		String redirectURL = Util.getContextURL(request) + HTML_Link.docLink(doc.getURI());
  	response.sendRedirect(response.encodeRedirectURL(redirectURL));
  	return null;
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute

/**
 * Permet de sous-classer this pour lui faire modifier, par ex les parents au lieu des enfants.
 * @param kw le SLKeyword dont une liste est modifiee (par ex dont la liste des children est modifiee)
 */
protected void doIt(HttpServletRequest request, SLModel mod, SLDocument doc) {
	// DOIT ETRE INUTILE DE PASSER VIA KW (uri suffirait)
	mod.delete(doc);
}
} // end Action
