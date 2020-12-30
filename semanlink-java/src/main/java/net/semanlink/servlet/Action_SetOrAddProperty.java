package net.semanlink.servlet;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.FileUriFormat;
import net.semanlink.util.Util;

/**
 * Action demandant d'affecter une propriété à un document ou kw
 * 
 * Qlqs questions se posent :
 * <B>au sujet de la propriete</B>
 * elle est passee via le parameter "property" de la request.
 * Il faut faire le lien avec une propriete de model rdf, cad une uri.
 * Alors, ce param "property" de la requete, que contient-il ?
 * 1) a-t-on affaire a une url de ppty ou une string interpretee par l'appli (a transformer en url de ppty) ?
 * 2) si "url de ppty", url longue ou relative (a quoi ?) ou un truc genre dc:title ?
 * Pour l'instant, on suppose qu'il s'agit d'une string a interpreter par l'appli, qlq chose
 * genre "comment", et on en deduit qu'il faut l'attribuer a sl:comment
 * Pour cela, gere une liste de noms courts de proprietes accepatbles en tant que 
 * parameter "property" de la request.
 */

// 2020-12 found TODO: the subject of the triple, given by param "uri" 
// (or, this is new, the param "file", cf. local copy)
// is supposed to be a long uri. Should also accept short uris using namespaces
// including "tag:" and "doc:"
// (and all actions should allow this. Would make less ugly eg. ?docuri=bla)

public class Action_SetOrAddProperty extends BaseAction {
public static final String ADD = "add";
public static final String SET = "set";
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		String propUri = getPropUri(request); // this take care of ns:xxx

		if (SLVocab.TITLE_PROPERTY.equals(propUri)) {
			setOrAddProp(SLVocab.TITLE_PROPERTY, request.getParameter("docTitle"), request, response);
		} else {
			setOrAddProp(propUri, getPropValue(request), request, response);
		}
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return null;
} // end execute

protected String getPropUri(HttpServletRequest request) {
	return getPropertyUri(request);
}

protected String getPropValue(HttpServletRequest request) {
	return request.getParameter("value");
}

static public boolean subjectIsKwNotDoc_(HttpServletRequest request) {
	return ("kw".equals(request.getParameter("docorkw")));
}

protected boolean subjectIsKwNotDoc(HttpServletRequest request) {
	return subjectIsKwNotDoc_(request);
}

protected String getSubjectUri(HttpServletRequest request) {
	String x =  request.getParameter("uri");
	if ((x == null) || ("".equals(x.trim()))) {
		// if no uri param, use file param (cf. 2020-12 for local copy in document.jsp script setLocalCopyFile2)
		x = null;
		String f = request.getParameter("file");
		try {
			x = SLServlet.getSLModel().filenameToUri(f);
		} catch (MalformedURLException | URISyntaxException e) { throw new RuntimeException(e) ; }
	}
	if (x == null) throw new RuntimeException("no uri");
	return x;
}

protected void setOrAddProp(String propertyUri, String propertyValue, HttpServletRequest request, HttpServletResponse response) throws Exception {
	String redirectURL = null;
	boolean isKwNotDoc = subjectIsKwNotDoc(request);
		
	String uri = getSubjectUri(request);
	SLModel mod = SLServlet.getSLModel();
	
	boolean isAddAction = (request.getParameter(ADD) != null);
	
	// is propertyUri correct ?
	// (it can b entered by the user )
	String errMess = JenaUtils.getUriViolations(propertyUri, false);
	if (errMess != null) {
		throw new RuntimeException(errMess);
	}	
	
	// la value est-elle une url ?
	String valUrlString = null;
	if ((propertyValue != null) && (!("".equals(propertyValue)))) {
		try {
			// propertyValue of the form ns:xxx ?
			String s = Jsp_Resource.namespacedUri2Uri(propertyValue);
			if (s != null) propertyValue = s;
			
			URL valUrl = new URL(propertyValue);
			valUrlString = valUrl.toString();
			// MAIS on a un problème si l'url est une url de forme "file:/xxx".
			// Ce pb a déjà été vu dans fileNameToUri ds SLModel :
			// pb de relativization quand on sauve le modèle -- il faut
			// absolument avoir file:///
			// On pourrait essyaer de comprendre pourquoi ici on récupère file:/xxx, même
			// si on a saisi "file:///" dans le champs de saisi (phénomène qu'on n'a pas l'air d'avoir ailluers,
			// cf btn "new doc". Mais de toute façon, et si on avait saisi "file:/xxx" ?
			// (d'ailleurs, que se passe-t-il dans new doc si on le fait ? // TODO to see)
			// if (valUrlString.startsWith("file:/")) valUrlString = FileUriFormat.one2threeSlashs(valUrlString);
			valUrlString = FileUriFormat.fileSlashSlashSlashProblem(valUrlString);
			
		} catch (Exception e) {
			// yes, nada
		}
		// 2007-04
		if (valUrlString != null) {
			// 2020-02 hum, setting a dc:source
			// we have something that has following url in the browser:
			// http://127.0.0.1:7080/semanlink/doc/2020/02/enquete_sur_les_usines_d’antibi
			// but whose actual URI in sl is:
			// http://127.0.0.1:7080/semanlink/doc/2020/02/enquete_sur_les_usines_d%E2%80%99antibi
			// (because we pass through laxistUri2Uri at creation time)
			// So we must check here that we do the same
			// or we won't be able to access the copy
			// TODO see how to simplfy. NOTE that the '’' in the url is not a uriViolation
			valUrlString = SLUtils.laxistUri2Uri(valUrlString); // 2020-02 added
					
			errMess = JenaUtils.getUriViolations(valUrlString,false);
			if (errMess != null) {
				throw new RuntimeException(errMess);
			}
		}
	}
	
	// 2005-02-23
	String lang = request.getParameter("lang");
	if ( ("".equals(lang)) || ("-".equals(lang)) ) lang = null;	
	
	if (lang != null) { 
		// if a date, don't set lang
		if (SLVocab.DATE_PARUTION_PROPERTY.equals(propertyUri)) { // 2019-09
			lang = null;
		} else if (SemanlinkConfig.PUBLISH_PROP.equals(propertyUri)) { // 2020-11
			lang = null;
		}
	}

	if (isKwNotDoc) {
		// kw
		SLKeyword kw = mod.getKeyword(uri);
		if (isAddAction) {
			if (valUrlString != null) {
				mod.addKwProperty(uri, propertyUri, valUrlString);
			} else {		
				mod.addKwProperty(uri, propertyUri, propertyValue, lang);
			}
			
		} else {
			if (valUrlString != null) {
				mod.setKwProperty(uri, propertyUri, valUrlString);
			} else {		
				mod.setKwProperty(uri, propertyUri, propertyValue, lang);
			}		
			if (propertyUri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				// we must ensure that the Tag remains a Tag!
				mod.addKwProperty(uri, propertyUri, SLVocab.KEYWORD_CLASS);
			}
		}		
		// POST REDIRECT 
		redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kw.getURI(), false, ".html");
	
	} else {
		// doc
		
		// rq : cas creation date traité ds JModel.setDocProperty
		
		SLDocument doc = mod.getDocument(uri);
		// test sur nullité ou "" de la prop est faite ds jmodel
		if (isAddAction) {
			if (valUrlString != null) {
				mod.addDocProperty(doc, propertyUri, valUrlString);
			} else {
				mod.addDocProperty(doc, propertyUri, propertyValue, lang);
			}
			
		} else {
			if (valUrlString != null) {
				mod.setDocProperty(doc, propertyUri, valUrlString);
			} else {
				// 2020-11 special things for comment property
				// mod.setDocProperty(doc, propertyUri, propertyValue, lang);
				setDocProperty(request, mod, doc, propertyUri, propertyValue, lang);
			}		
		}
		
		// POST REDIRECT 
		
		// 2020-07 to be able to redirect to doc, not local copy, when quick adding of local copy
		// new param giving the redirect url
		// (same code in Action_DeleteTriple)
		
		String redirect = request.getParameter("redirect_uri"); // 2020-07
		if (redirect != null) {
			redirectURL = Util.getContextURL(request) + HTML_Link.docLink(redirect);
		} else {
			redirectURL = Util.getContextURL(request) + HTML_Link.docLink(doc.getURI());
		}

	}
	// POST REDIRECT 
	response.sendRedirect(response.encodeRedirectURL(redirectURL));
}

// overriden for comments in Action_SetComment
protected void setDocProperty(HttpServletRequest request, SLModel mod, SLDocument doc, String propertyUri, String propertyValue, String lang) { // 2020-11
	mod.setDocProperty(doc, propertyUri, propertyValue, lang);
}

} // end Action
