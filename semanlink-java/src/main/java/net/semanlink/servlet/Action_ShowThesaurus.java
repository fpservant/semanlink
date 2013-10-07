package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;

/** Demande l'affichage d'un Keyword. */
public class Action_ShowThesaurus extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
	SLModel mod = SLServlet.getSLModel();
	  String uri = request.getParameter("uri");
	  uri = java.net.URLDecoder.decode(uri,"UTF-8");
	  SLThesaurus th = mod.getThesaurus(uri);
	  Jsp_Thesaurus jsp = new Jsp_Thesaurus(th, request);
	  request.setAttribute("net.semanlink.servlet.jsp", jsp);
	  x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
