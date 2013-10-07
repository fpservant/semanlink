package net.semanlink.servlet;
import javax.servlet.http.*;

import net.semanlink.util.AcceptHeader;

import org.apache.struts.action.*;

/**
 */
public class Action_Search extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		String text = request.getParameter("text");
		Jsp_Search jsp = new Jsp_Search(text, request);
		request.setAttribute("net.semanlink.servlet.jsp", jsp);
		AcceptHeader acceptHeader = new AcceptHeader(request);
		if ( (acceptHeader.prefersRDF()) || (mapping.getPath().endsWith(".rdf"))) {
			request.setAttribute("net.semanlink.servlet.rdf", jsp.getRDF("rdf"));
		  x = mapping.findForward("rdf");
		} else if (mapping.getPath().endsWith(".n3")) {
			request.setAttribute("net.semanlink.servlet.rdf", jsp.getRDF("n3"));
		  x = mapping.findForward("n3");
		} else {
		  x = mapping.findForward("continue");
		}
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
