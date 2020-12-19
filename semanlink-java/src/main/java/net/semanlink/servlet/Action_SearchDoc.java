package net.semanlink.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.lod.SLSparqlEndPoint;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.Util;

// 2020-03 started from Action_BookmarkForm
public class Action_SearchDoc extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		String what = request.getParameter("godoc_q");
		SLModel mod = SLServlet.getSLModel();
		String contextUrl = Util.getContextURL(request);
		
		if ((what.startsWith("http://")) || (what.startsWith("https://"))) {
			String docuri = what; // attention encodage !!!

			String redirectURL = Action_BookmarkForm.docUrl(docuri, mod, contextUrl);
			if (redirectURL != null) {
				response.sendRedirect(response.encodeRedirectURL(redirectURL));
				return null; // EXIT !!!				
			}			
		}
		
		// search as a string with sparql
		
		// create the query

		SLSparqlEndPoint endPoint = SLServlet.getSLSparqlEndPoint();
		List<SLDocument> docs = getDocs(what, endPoint, mod);
		
		if (docs.size() == 1) {
			SLDocument doc = docs.get(0);
			// TODO SIMPLIFIER
			String redirectURL = Action_BookmarkForm.docUrl(doc.getURI(), mod, contextUrl);
			if (redirectURL == null) redirectURL = doc.getURI(); // ?
			response.sendRedirect(response.encodeRedirectURL(redirectURL));
			return null; // EXIT !!!				
		}
		
		x = mapping.findForward("continue");

	} catch (Exception e) {
		return error(mapping, request, e );
	}
	return x;
} // end execute


/**
 * @param what: la phrase passée
 */
private static List<SLDocument> getDocs(String what, SLSparqlEndPoint endPoint, SLModel mod) {
	String queryString = sparqlQString(what);
	Query q = QueryFactory.create(queryString) ;
	q.setLimit(50);
	QueryExecution qexec = QueryExecutionFactory.create(q, endPoint.getDataset());
	ResultSet results = qexec.execSelect() ;
	
	ArrayList<SLDocument> docs = new ArrayList<>();
	for(;results.hasNext();) {
		QuerySolution qs = results.next();
		Resource doc = qs.getResource("doc");
		SLDocument sldoc = mod.getDocument(doc.getURI());
		docs.add(sldoc);
	}
	return docs;
}

private static String sparqlQString(String what) {
	// beware to characters in what that may be special chars for regex
	// hence the \Q around what \E (this is what returns java.util.regex.Pattern.quote(what))
	// Why do we need \\Q and \\E don't know
	return 
	"SELECT DISTINCT ?doc WHERE {\n" +
	"GRAPH <" + SLServlet.getServletUrl() + "/docs/> {\n" +
	"?doc ?p ?x .\n" +
	"FILTER regex(?x, \"" + "\\\\Q" + what + "\\\\E" + "\", \"i\")\n" +
	"}\n" +
	"}";
}

} // end Action
