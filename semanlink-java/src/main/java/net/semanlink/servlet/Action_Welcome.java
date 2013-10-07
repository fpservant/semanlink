/* Created on 13 mars 2006 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Action_Welcome extends BaseAction {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		ActionForward x = null;
		try {
			Jsp_Welcome jsp = new Jsp_Welcome(request);   	
		  request.setAttribute("net.semanlink.servlet.jsp", jsp);
		  x = mapping.findForward("continue");
			if (mapping.getPath().endsWith(".rdf")) {
				request.setAttribute("net.semanlink.servlet.rdf", jsp.getRDF("rdf"));
			  x = mapping.findForward("rdf");
			} else {
			  x = mapping.findForward("continue");
			}
		} catch (Exception e) {
		    return error(mapping, request, e );
		}
		return x;
	} // end execute
}
