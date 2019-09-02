package net.semanlink.servlet;
import javax.servlet.http.*;

import net.semanlink.servlet.BaseAction;
import net.semanlink.servlet.Jsp_Page;
import net.semanlink.util.Util;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

// 2019-09
/**
 * Comme on a pris soin, au moment du clic vers la page logon, de stocker dans un attribut de session
 * la page de départ, on peut ici y retourner.
 */
public class Action_Logon extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		HttpSession session = request.getSession();
		
		
		Form_Logon f = (Form_Logon) form;

		String username = f.getUsername().trim();
		String password = f.getPassword();

		boolean ok = false;
		if ("fps".equals(username)) {
			ok = true;
		}
		
		Boolean edit = new Boolean(ok);

		String backToPage = (String) session.getAttribute("net.semanlink.servlet.goBackToPage");
		session.removeAttribute("net.semanlink.servlet.goBackToPage");
				
		session.setAttribute("net.semanlink.servlet.editor", edit);
		session.setAttribute("net.semanlink.servlet.edit", edit);

		response.sendRedirect(response.encodeRedirectURL(backToPage));
  	return null;
	} catch (Exception e) {
		return error(mapping, request, e );
	}
} // end execute
} // end Action