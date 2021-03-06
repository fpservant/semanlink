package net.semanlink.servlet;
import java.net.URLDecoder;

import javax.servlet.http.*;
import org.apache.struts.action.*;

/**
 * Action activée par la livesearch (saisie dans la "searchform")
 */
public class Action_LiveSearch extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		String text = request.getParameter("text");
		if (text != null) {
			text = URLDecoder.decode(text,"UTF-8"); // double appel à EncodeURIComponent ds livesearch.js pour en arriver là
			// (sinon des A© à la palce de é dans text) Pb lié à l'envoi en get.
		}
		request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Search(text, request));
		x = mapping.findForward("continue"); // /jsp/livesearchxml.jsp  
	} catch (Exception e) { // TODO : ceci n'est pas bon: devrait renvoyer un xml ajax
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
