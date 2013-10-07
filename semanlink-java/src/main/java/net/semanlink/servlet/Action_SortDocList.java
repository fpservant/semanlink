package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
/**
 * Attention, le code ici ne fonctionne que pour kw et doc. A reprendre pour andKws, etc...
 */
public class Action_SortDocList extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  /*ActionForward x = mapping.findForward("continue");
  Jsp_Resource jsp = null;*/
  try {
    // SLModel mod = SLServlet.getSLModel();
    String sortProp = getPropertyUri(request);
    if ( (sortProp  != null) && (!("".equals(sortProp))) ) {
    		request.getSession().setAttribute( "net.semanlink.servlet.SortProperty", sortProp);
    } else {
   		request.getSession().setAttribute( "net.semanlink.servlet.SortProperty", SLServlet.getJspParams().getDefaultSortProperty());
    }
    /*String kwuri = request.getParameter("kwuri");
    if (kwuri != null) {
	    	SLKeyword keyword = mod.getKeyword(kwuri);
	    	jsp = new Jsp_Keyword(keyword, request);
    } else {
	    	String docuri = request.getParameter("docuri");
	    	if (docuri != null) {
		    	SLDocument doc = mod.getDocument(docuri);
		    	jsp = Jsp_Document.newJsp_Document(doc, request);
	    	} else {
	    		String gopage = request.getParameter("gopage");
	    		gopage = java.net.URLDecoder.decode(gopage);
	    		return new ActionForward(gopage);
	    	}
    }
 	request.setAttribute("net.semanlink.servlet.jsp", jsp); // was bug si affichage tree : il était perdu
 	*/
	String gopage = request.getParameter("gopage");
	gopage = java.net.URLDecoder.decode(gopage,"UTF-8");
	return new ActionForward(gopage);

	} catch (Exception e) {
	    return error(mapping, request, e );
	}
  // return x;
} // end execute
} // end Action
