package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;

/**
 * Action demandant de setter le lebel d'un kw.
 */
public class Action_SetKwLabel extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		SLModel mod = SLServlet.getSLModel();
		String kwuri = request.getParameter("kwuri");
		String label = request.getParameter("kwlabel");
		String lang = request.getParameter("lang");
		if (lang != null) {
			lang = lang.trim();
			if (lang.length() != 2) lang = null;
		}
		mod.setKwProperty(kwuri, SLVocab.PREF_LABEL_PROPERTY, label, lang);
		// POST REDIRECT 
		// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(mod.getKeyword(kwuri), request));
		// return mapping.findForward("continue");
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kwuri, false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		return null;
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute
} // end Action
