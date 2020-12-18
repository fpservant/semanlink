package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.semanlink.*;
import net.semanlink.util.Util;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

// 2020-03 started from Action_BookmarkForm
public class Action_GoDoc extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		String what = request.getParameter("godoc_q");
		SLModel mod = SLServlet.getSLModel();
		
		if ((what.startsWith("http://")) || (what.startsWith("https://"))) {
			String docuri = what; // attention encodage !!!

			String redirectURL = Action_BookmarkForm.docUrl(docuri, mod, Util.getContextURL(request));
			if (redirectURL != null) {
				response.sendRedirect(response.encodeRedirectURL(redirectURL));
				return null; // EXIT !!!				
			}			
		}
		
		// search as a string with sparql

		x = mapping.findForward("continue");

	} catch (Exception e) {
		return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
