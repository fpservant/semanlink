package net.semanlink.servlet;
import javax.servlet.http.*;

import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.FileUriFormat;
import net.semanlink.util.Util;

import java.net.*;

/**
 * Action demandant d'affecter une propriété à un document. 
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
 * 
 * <b>au sujet de la valeur de la propriete</b>
 * pourrait etre une uri ou un literal.
 * On suppose ici qu'on a affaire a un literal.
 * @author fps
 */
public class Action_SetOrAddProperty extends BaseAction {
public static final String ADD = "add";
public static final String SET = "set";
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	// ActionForward x = null;
	try {
		String propUri = getPropUri(request);

		// URI uriTesting = new URI(propUri);
		// ((JModel) SLServlet.getSLModel()).getDocsModel().createProperty(propUri);

		if (SLVocab.TITLE_PROPERTY.equals(propUri)) {
			setOrAddProp(SLVocab.TITLE_PROPERTY, request.getParameter("docTitle"), request, response);
		} else {
			setOrAddProp(getPropUri(request), getPropValue(request), request, response);
		}
		// POST REDIRECT 
		// x = mapping.findForward("continue");
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	// POST REDIRECT 
	// return x;
	return null;
} // end execute

protected String getPropUri(HttpServletRequest request) {
	return getPropertyUri(request);
}

protected String getPropValue(HttpServletRequest request) {
	return request.getParameter("value");
}

protected boolean subjectIsKwNotDoc(HttpServletRequest request) {
	return ("kw".equals(request.getParameter("docorkw")));
}

protected String getSubjectUri(HttpServletRequest request) {
	return request.getParameter("uri");
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
	// SLDocument doc = mod.getDocument(docuri);
	
	// 2005-02-23
	String lang = request.getParameter("lang");
	if ( ("".equals(lang)) || ("-".equals(lang)) ) lang = null;
	/*if (valUrlString == null) {
		lang = request.getParameter("lang");
		if (lang == null) lang = "fr"; // todo
	}*/
	
	
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
		// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
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
		// getJsp_Document(doc, request);
		
		// 2020-07 to be able to redirect to doc, not local copy, when quick adding of local copy
		// new param given the redirect url
		
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
