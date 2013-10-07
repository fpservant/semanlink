package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;

public class Action_ThisMonth extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
	  String[] kwuris = request.getParameterValues("kwuris");
	  if (kwuris != null) {
	    for (int i = 0; i < kwuris.length; i++) {
	    		kwuris[i] = java.net.URLDecoder.decode(kwuris[i],"UTF-8");
	    }
	  }
		Jsp_ThisMonth jsp = new Jsp_ThisMonth(kwuris,request);
	  request.setAttribute("net.semanlink.servlet.jsp", jsp);
		if (mapping.getPath().endsWith(".rdf")) {
			request.setAttribute("net.semanlink.servlet.rdf", jsp.getRDF("rdf"));
		  x = mapping.findForward("rdf");
		} else if (mapping.getPath().endsWith(".n3")) {
				request.setAttribute("net.semanlink.servlet.rdf", jsp.getRDF("n3"));
				request.setAttribute("net.semanlink.servlet.rdf.lang", "n3");
			  x = mapping.findForward("rdf");
		} else {
		  x = mapping.findForward("continue");
		}
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
