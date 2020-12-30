package net.semanlink.servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.Util;

/**
 * Delete a triple
 * params:
 * - "s", "p", "o" and possibly "lang" (if no lang, "o" assumed to be a uri)
 * - "docorkw"
 */

// TODO: there are more controls in Action_SetOrAddProperty
// (eg. valUrlString = FileUriFormat.fileSlashSlashSlashProblem(valUrlString);)
// TODO check lang null in litteral triples

public class Action_DeleteTriple extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		String s = getSPO("s", request);
		String p = getSPO("p", request);
		String o = getSPO("o", request);
		String lang = getLang(request);
		boolean isKwNotDoc = Action_SetOrAddProperty.subjectIsKwNotDoc_(request);
		String redirectURL = null;

		SLModel mod = SLServlet.getSLModel();
		if (isKwNotDoc) {
			if (lang == null) {
				mod.deleteKwTriple(s, p, o);
			} else {
				mod.deleteKwTriple(s, p, o, lang);				
			}
			redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), s, false, ".html");
			
		} else {
			SLDocument doc = mod.getDocument(s);
			if (lang == null) {
				mod.deleteDocTriple(doc, p, o);
			} else {
				mod.deleteDocTriple(doc, p, o, lang);				
			}
			
			// 2020-07 to be able to redirect to doc, not local copy, when quick adding of local copy
			// new param giving the redirect url
			// (same code in Action_SetOrAddProperty)
			
			String redirect = request.getParameter("redirect_uri"); // 2020-07
			if (redirect != null) {
				redirectURL = Util.getContextURL(request) + HTML_Link.docLink(redirect);
			} else {
				redirectURL = Util.getContextURL(request) + HTML_Link.docLink(doc.getURI());
			}

		}
		
		// POST REDIRECT 
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return null;
} // end execute

protected String getPropUri(HttpServletRequest request) {
	return getPropertyUri(request);
}

/*
 * Takes care of ns:xxx
 * @param spo = "s", "p", or "o"
 */
String getSPO(String spo, HttpServletRequest request) {
	String val = request.getParameter(spo);
	if ((val == null) || ("".equals(val.trim()))) {
		throw new RuntimeException("No " + spo + " param");
	}
	
	if ("o".equals(spo)) {
		String lang = getLang(request);
		if (lang != null) {
			return val;
		}
	}
	
	String x = SLServlet.getSemanlinkConfigProps().getUriString(val);
	if (x == null) x = val;
	return x;
}

// null iff not a triple pointing to literal
String getLang(HttpServletRequest request) {
	return request.getParameter("lang");
}

} // end Action
