package net.semanlink.servlet;
import javax.servlet.http.*;

import net.semanlink.semanlink.SLKeyword;

import org.apache.struts.action.*;

/** Demande l'affichage d'un and de Keywords. */
public class Action_AndKws2 extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  ActionForward x = null;
  try {
  	// pourrait utiliser handleKwUrisParam (code en double)
    String[] kwuris = request.getParameterValues("uri");
    for (int i = 0; i < kwuris.length; i++) {
    		kwuris[i] = java.net.URLDecoder.decode(kwuris[i],"UTF-8");
    }

    Jsp_Page jsp = null;
    Jsp_AndKws jspAndKws = new Jsp_AndKws(kwuris, request);
    
    if (request.getParameter("newkw") != null) {
    		SLKeyword newKw = jspAndKws.toNewKeyword();
    		jsp = new Jsp_Keyword(newKw, request);
    } else {
    		jsp = jspAndKws;
    }
    
    request.setAttribute("net.semanlink.servlet.jsp", jsp);

    x = mapping.findForward("continue");
  } catch (Exception e) {
    return error(mapping, request, e );
  }
  return x;
} // end execute
} // end Action
