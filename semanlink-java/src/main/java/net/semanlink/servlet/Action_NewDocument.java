package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;

/**
 * To change : ne cree aucun statement ! (mais attention, changer peut-etre aussi ou c'est appele
 */
public class Action_NewDocument extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  ActionForward x = null;
  try {
    String docuri = request.getParameter("docuri"); // pas de decode : issu d'un champ de saisie, non code.
    SLModel mod = SLServlet.getSLModel();

	SLDocument doc = mod.getDocument(docuri);
	getJsp_Document(doc, request);
	x = mapping.findForward("continue");
  } catch (Exception e) {
	    return error(mapping, request, e );
  }
  return x;
} // end execute
} // end Action
