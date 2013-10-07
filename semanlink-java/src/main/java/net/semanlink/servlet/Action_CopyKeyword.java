package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
/**
 * A utiliser au sein d'une form (post)
 * @see Action_CopyKeywordGet pour la même opération au sein d'un lien (get).
 */
public class Action_CopyKeyword extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  try {
  	SLKeyword keyword = getSLKeyword(request);
  	SLKeyword[] clipboardKeywords = new SLKeyword[1];
  	clipboardKeywords[0] = keyword;
  	request.getSession().setAttribute("net.semanlink.servlet.ClipboardKeyword",clipboardKeywords);

		// POST REDIRECT
		// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(keyword, request));
		// return mapping.findForward("continue");
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), keyword.getURI(), false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		return null;

  } catch (Exception e) {
    return error(mapping, request, e );
  }
} // end execute
} // end Action
