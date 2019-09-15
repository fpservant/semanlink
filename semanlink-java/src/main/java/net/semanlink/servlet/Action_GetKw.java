package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;

/** Liste des fils d'un kw pour le "live tree" */
public class Action_GetKw extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  ActionForward x = null;
  try {
  	// System.out.println("Action_GetKw params :");
  	// this.printParams(request); // debug
 
  	String kwuri = request.getParameter("kwuri");
    // kwuri = java.net.URLDecoder.decode(kwuri,"UTF-8"); // 2007/10 Grec
    SLModel mod = SLServlet.getSLModel();
    SLKeyword kw = mod.getKeyword(kwuri);
    request.setAttribute("kw", kw);
    
    // String withdocs = request.getParameter("withdocs");
    // String postTagOnClick = request.getParameter("postTagOnClick");
    
  	// snip on list of kws // 2019-09
    response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08

    x = mapping.findForward("continue");
  } catch (Exception e) {
	    return error(mapping, request, e );
  }
  return x;
} // end execute
} // end Action
