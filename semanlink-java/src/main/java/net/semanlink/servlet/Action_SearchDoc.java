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
		
		Jsp_DocumentList_Simple jsp = new Jsp_DocumentList_Simple(docs, request, 
				I18l.getI18l(request.getSession()).getString("sidemenu.godoc") +" \""+ what + "\"");
	  request.setAttribute("net.semanlink.servlet.jsp", jsp);

		x = mapping.findForward("continue");

	} catch (Exception e) {
		return error(mapping, request, e );
	}
	return x;
} // end execute


/**
 * @param what: la phrase passée
 */
static int SPARQL_LIMIT = 30;
private static List<SLDocument> getDocs(String what, SLSparqlEndPoint endPoint, SLModel mod) {
	String queryString = sparqlQString(what);
	Query q = QueryFactory.create(queryString) ;
	q.setLimit(SPARQL_LIMIT);
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
	// we want to search the phrase anywhere inside one of the text props
	// (eg. the last name of an arxiv author)
	// Hence the use of a regex.
	
	// Beware to characters in what that may be special chars for regex
	// hence the \Q around what \E (this is what returns java.util.regex.Pattern.quote(what))
	// Why do we need \\Q and \\E I don't know

	// pb when there is a quote (") inside what
	// (exception)
	// and can't find what to do
	// With this, we ends up with \" instead of quote in the sparql string
	// No more exception, but doesn't find the string
	// what = what.replaceAll("\"", "\\\\\"");
	// Hack to keep the largest chunk between quotes
	int k = what.indexOf("\"");
	if (k > -1) {
		String[] chunks = what.split("\"");
		int maxi = 0;
		String s = null;
		for (int i = 0 ; i < chunks.length ; i++) {
			if (chunks[i].length() > maxi) {
				maxi = chunks[i].length();
				s = chunks[i];
			}
		}
		what = s;
	}
	
	// Still some problems
	// for instance with "&" in
	// Kingsley Uyi Idehen sur Twitter : "When I read this & other articles, I leverage our @datasniff browser ext. for highlighting key terms;
	// (not found)
	// Or with \& : exception
	
	try {
		String x = 
		"SELECT DISTINCT ?doc WHERE {\n" +
		"GRAPH <" + SLServlet.getServletUrl() + "/docs/> {\n" +
		"?doc ?p ?x .\n" +
		"FILTER regex(?x, \"" + "\\\\Q" + what + "\\\\E" + "\", \"i\")\n" +
		"}\n" +
		"}";
		return x;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

} // end Action
