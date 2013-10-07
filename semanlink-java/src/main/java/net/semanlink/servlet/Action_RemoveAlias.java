/* Created on 9 avr. 2005 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.Util;
public class Action_RemoveAlias extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		SLModel mod = SLServlet.getSLModel();
		String kwuri = request.getParameter("uri"); // de qui ont retire des enfants
		// kwuri = java.net.URLDecoder.decode(kwuri); // why ?
		SLKeyword kw = mod.getKeyword(kwuri);
		String[] aliasuris = request.getParameterValues(removedListParamName());
		if (aliasuris != null) {
			for (int i = 0; i < aliasuris.length; i++) {
				aliasuris[i] = java.net.URLDecoder.decode(aliasuris[i],"UTF-8");
			}
			doRemove(mod, kw, aliasuris);
		}
		// POST REDIRECT
		// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
		// x = mapping.findForward("continue");
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kw.getURI(), false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		return null;
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute

/** fait le remove. Overrider pour remover autre chose */
protected void doRemove(SLModel mod, SLKeyword kw, String[] aliasuris) {
	mod.removeAlias(kw, aliasuris);
}
/** param contenant la liste à remover. */
protected String removedListParamName() {
	return "aliasuris";
}
} // end Action

