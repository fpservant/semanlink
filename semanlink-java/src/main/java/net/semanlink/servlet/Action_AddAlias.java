package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
/**
 */
public class Action_AddAlias extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		SLModel mod = SLServlet.getSLModel();
		String kwuri = request.getParameter("uri");
		SLKeyword slKw = mod.getKeyword(kwuri);
		String label = request.getParameter("aliasLabel");
		String lang = request.getParameter("lang");
		if ( ("".equals(lang)) || ("-".equals(lang)) ) lang = null;
		mod.addAlias(label, lang, slKw);
		// POST REDIRECT
		// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(slKw, request));
		// return mapping.findForward("continue");
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), slKw.getURI(), false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		return null;
	} catch (Exception e) {
		return error(mapping, request, e );
	}
} // end execute
} // end Action
