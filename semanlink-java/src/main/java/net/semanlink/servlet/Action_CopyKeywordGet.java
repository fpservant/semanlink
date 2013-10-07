package net.semanlink.servlet;
import javax.servlet.http.*;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.util.Util;

import org.apache.struts.action.*;
/**
 */
public class Action_CopyKeywordGet extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		/*
		Jsp_Keyword jsp_keyword = handleKwUriParam(request);
		if (jsp_keyword != null) {
			request.getSession().setAttribute("net.semanlink.servlet.ClipboardKeyword",jsp_keyword.getSLKeyword());
			x = mapping.findForward("keyword");
		}
		*/
  	SLKeyword keyword = getSLKeyword(request);
  	SLKeyword[] clipboardKeywords = new SLKeyword[1];
  	clipboardKeywords[0] = keyword;
  	request.getSession().setAttribute("net.semanlink.servlet.ClipboardKeyword",clipboardKeywords);

		// POST REDIRECT
		String redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), keyword.getURI(), false, ".html");
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
  } catch (Exception e) {
    return error(mapping, request, e );
  }
	return x;
} // end execute
} // end Action
