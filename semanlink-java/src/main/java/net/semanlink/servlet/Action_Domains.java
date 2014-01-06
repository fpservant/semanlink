 /* Created on 2 oct. 06 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Action_Domains extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		Jsp_Domains jsp = new Jsp_Domains(request);
	  request.setAttribute("net.semanlink.servlet.jsp", jsp);
		x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
