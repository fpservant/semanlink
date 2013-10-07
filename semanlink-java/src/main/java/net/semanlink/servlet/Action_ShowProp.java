package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;

/** Demande l'affichage des res ayant une certaine (ppté-value). */
public class Action_ShowProp extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		handlePptyUriParam(request);
	  x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
  return x;
} // end execute
} // end Action
// docList = mod.getDocumentsList(propertyUri, objectUri);
// docList = mod.getDocumentsList(propertyUri, propertyValue, lang);
