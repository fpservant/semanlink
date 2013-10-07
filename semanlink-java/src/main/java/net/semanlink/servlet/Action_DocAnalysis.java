package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.metadataextraction.*;
/**
 * Action demandant de (re)faire l'extraction de metadata d'un document
 * @author fps
 */
public class Action_DocAnalysis extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		String docuri = request.getParameter("uri");
		if (docuri == null) docuri = request.getParameter("docuri"); // TODO VIRER non ? oh non, c'est l'autre !!
		SLModel mod = SLServlet.getSLModel();
		SLDocument doc = mod.getDocument(docuri);
		MetadataExtractorManager extr = mod.getMetadataExtractorManager();
		if (extr != null) {
			extr.doIt(doc, mod);
		}
		getJsp_Document(doc, request);
		x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action
