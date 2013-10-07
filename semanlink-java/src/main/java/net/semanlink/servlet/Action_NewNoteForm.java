package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Action_NewNoteForm extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  ActionForward x = null;
  try {
  			Form_NewNote fform = (Form_NewNote) form;
  			fform.reset(); // pour ne pas garder title et comment sur la form pour un nouveau doc
	    Jsp_Page jsp = new Jsp_Page(request);
			jsp.setTitle(jsp.i18l("newnoteform.newnote"));
			jsp.setContent("/jsp/newnoteform.jsp");
			request.setAttribute("net.semanlink.servlet.jsp", jsp);
			x = mapping.findForward("continue");
    
  } catch (Exception e) {
    return error(mapping, request, e );
  }
  return x;
} // end execute
} // end Action
