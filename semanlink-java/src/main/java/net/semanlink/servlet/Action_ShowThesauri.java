package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;

/** Demande l'affichage d'un Keyword. */
public class Action_ShowThesauri extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
	  Jsp_Thesauri jsp = new Jsp_Thesauri(request);
	request.setAttribute("net.semanlink.servlet.jsp", jsp);
	  x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
