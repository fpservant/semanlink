package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
import net.semanlink.metadataextraction.*;
/**
 * Action demandant de (re)faire l'extraction de metadata d'un document
 */
public class Action_DocAnalysis extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		String docuri = request.getParameter("uri");
		if (docuri == null) docuri = request.getParameter("docuri"); // TODO VIRER non ? oh non, c'est l'autre !!
		SLModel mod = SLServlet.getSLModel();
		SLDocument doc = mod.getDocument(docuri);
		MetadataExtractorManager extr = mod.getMetadataExtractorManager();
		if (extr != null) {
			extr.doIt(doc, mod);
		}
		
		// 2020-03 hum, compare with Action_Download -- different,
		// with one effect; here, we end up with a url of the form docanalysis.do?docuri=http%3A%2F
		// but with one less redirect
		// Doing now as in Action_Download
		// looks like the "POST RDIRECT" had been neglected here
		
//		getJsp_Document(doc, request);
//		ActionForward x = mapping.findForward("continue");
		
		String redirectURL = null;
		if (docuri.startsWith(SLServlet.getServletUrl())) {
			redirectURL = docuri;
		} else {
			// pre uris for bookmarks
			redirectURL = Util.getContextURL(request) + HTML_Link.docLink(docuri);
		}
		
  	response.sendRedirect(response.encodeRedirectURL(redirectURL));
  	return null;
		
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
} // end execute
} // end Action
