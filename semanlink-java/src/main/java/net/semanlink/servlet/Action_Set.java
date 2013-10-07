package net.semanlink.servlet;

import javax.servlet.http.*;
import org.apache.struts.action.*;
/**
 * Action demandant de setter une variable de session.
 * (imagesonly en l'occurrence - overrider getParamName() et toSessionAttribute pour changer)
 */
public class Action_Set extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		toSessionAttribute(request);
		if (handleKwUriParam(request) != null) {
			x = mapping.findForward("keyword");
		} else if (handleDocUriParam(request) != null) {
			x = mapping.findForward("document");
		} else {
			x = mapping.findForward("continue");
		}
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute


String getParamName() {
	return "imagesonly";
}

/*void paramName2SessionAttribute(HttpServletRequest request) {
	booleanParam2SessionAttribute(getParamName(), request);
}*/

void toSessionAttribute(HttpServletRequest request) {
	booleanParam2SessionAttribute(getParamName(), request);
}
} // end Action
