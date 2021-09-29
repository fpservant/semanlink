package net.semanlink.servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.Util;

/**
 * Change a triple
 * path="/changetriple"
 * params: the "current triple" to be replaced by a new triple
 * - "cur_s", "cur_p", "cur_o" and possibly "cur_lang" (if otype not defined and no cur_lang, "cur_o" assumed to be a uri)
 * - "s", "p", "o" and possibly "lang" (if otype not defined and  no lang, "o" assumed to be a uri)
 * - "docorkw" ("doc", or "kw")
 * - "otype": "res" or "lit" or not defined says whether the triple, by nature of its prop,
 * points to a resource or a literal;
 * s (and cur_s) may be replaced by sfile (resp. cur_sfile: in this case, a file to be replaced by
 * its uri to get s (resp cur_s)
 * 
 * @since 2020-12
 */

// Based on Action_SetOrAddProperty and Action_DeleteTriple

// TODO: there are more controls in Action_SetOrAddProperty
// (eg. valUrlString = FileUriFormat.fileSlashSlashSlashProblem(valUrlString);)
// TODO check lang null in litteral triples

// TODO check que les uris sont des uris

public class Action_ChangeTriple extends BaseAction { // 2020-12
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	try {
		String cur_s = getSPO("cur_s", request);
		String cur_p = getSPO("cur_p", request);
		String cur_o = getSPO("cur_o", request);
		String cur_lang = request.getParameter("cur_lang");
		String s = getSPO("s", request);
		String p = getSPO("p", request);
		String o = getSPO("o", request);
		String lang = request.getParameter("lang");
		boolean isKwNotDoc = Action_SetOrAddProperty.subjectIsKwNotDoc_(request);
		String redirectURL = null;

		// TODO: when we have creation and delete,
		// do it in one step
		// (easy for docs with the update class, more difficult for kws, I think)
		
		SLModel mod = SLServlet.getSLModel();
		if (isKwNotDoc) {
			
			if (cur_p != null) { // there is a "current triple" to delete
				if (cur_s == null) throw new RuntimeException("Current Triple: no subject");
				if (cur_o == null) throw new RuntimeException("Current Triple: no object");

				if (cur_p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					// we must ensure that the Tag remains a Tag!
					throw new RuntimeException("Cannot remove the tag type of a tag");
				}
				
				if (cur_lang == null) {
					mod.deleteKwTriple(cur_s, cur_p, cur_o);

				} else {
					mod.deleteKwTriple(cur_s, cur_p, cur_o, cur_lang);				
				}
			} // if (cur_p != null)
			
			if ((p != null) 
					&& (s != null)
					&& (o != null)) {  // there is a "new triple" to create
				
				// mod.addKwProperty method assumes that the kw exist.
				// So, we must check it.
				
				SLKeyword kw = mod.getKeywordIfExists(s);
				if (kw == null) throw new RuntimeException("Keyword doesn't exist: " + s);
				
				if ((lang == null)||("".contentEquals(lang))) {
					mod.addKwProperty(s, p, o);

				} else {
					mod.addKwProperty(s, p, o, jenaLangArg(lang));		
				}
			} // if (p != null)
						
			redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), s, false, ".html");
			
		} else { // doc
			SLDocument doc=null, cur_doc=null;

			if (s == null) {
				String sfile = request.getParameter("sfile");
				if ((sfile != null) && (!"".equals(sfile))) {
					s = mod.filenameToUri(sfile);
				}				
			}

			if (cur_p != null) { // there is a "current triple" to delete
				if (cur_s == null) throw new RuntimeException("Current Triple: no subject");
				if (cur_o == null) throw new RuntimeException("Current Triple: no object");

				cur_doc = mod.getDocument(cur_s);
				
				if ((cur_lang == null) || ("".contentEquals(cur_lang))) {
					mod.deleteDocTriple(cur_doc, cur_p, cur_o);
	
				} else {
					mod.deleteDocTriple(cur_doc, cur_p, cur_o, jenaLangArg(cur_lang));				
				}
			} // if (cur_p != null)
				
			if ((p != null) 
					&& (s != null)
					&& (o != null)) {  // there is a "new triple" to create

				// comment from Action_SetOrAddProperty: cas creation date traité ds JModel.setDocProperty
				// comment from Action_SetOrAddProperty: test sur nullité ou "" de la prop est faite ds jmodel
				
				doc = mod.getDocument(s);				
				if ((lang == null)||("".contentEquals(lang))) {
					mod.addDocProperty(doc, p, o);

				} else {
					mod.addDocProperty(doc, p, o, jenaLangArg(lang));
				}
			} // if (p != null)
			
			// 2020-07 to be able to redirect to doc, not local copy, when quick adding of local copy
			// new param giving the redirect url
			// (same code in Action_SetOrAddProperty)
			
			String redirect = request.getParameter("redirect_uri"); // 2020-07
			if (redirect != null) {
				redirectURL = Util.getContextURL(request) + HTML_Link.docLink(redirect);

			} else {
				SLDocument d = doc;
				if (d == null) d = cur_doc;
				redirectURL = Util.getContextURL(request) + HTML_Link.docLink(d.getURI());
			}

		}
		
		// POST REDIRECT 
		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return null;
} // end execute

/*
 * Takes care of ns:xxx
 * @param spo = "s", "p", "o", "cur_s", "cur_p" or "cur_o"
 */
private String getSPO(String spo, HttpServletRequest request) {
	String val = request.getParameter(spo);
	if ((val == null) || ("".equals(val.trim()))) {
		return null;
	}
	
	// for "o" or "cur_o", if there is a corresponding lang (respec cur_lang) param,
	// it's a literal, not a uri
	// (hence, no question of namespace)
	
	// TODO CA NE VA PAS !!! FAIRE COMME DS Action_SetOrAddProperty:
	// se baser sur le contenu de la string o : est-ce une uri (including the namespaces case) ou pas
	// (ce qui n'est pas génial non plus : on pourrait vouloir stocker comme literal un truc
	// qui a la  forme d'une uri)
	
	// SANS COMPTER, pour lang, que "" peut vouloir dire literal sans lang
	
	// PALIATIF PARTIEL : "otype" param, peut-être documenté à "lit" ou "res"
	// -> reste modif à faire voir // TODO CHANGE plus bas
	
	if ("o".equals(spo)) {
		if ("lit".equals(request.getParameter("otype"))) {
			return val;
		} else if (!"res".equals(request.getParameter("otype"))) {
			// TODO CHANGE
			String lang = request.getParameter("lang");
			if ((lang != null)&&(!"".equals(lang))) {
				return val;
			}
		}
	} else if ("cur_o".equals(spo)) {
		if ("lit".equals(request.getParameter("otype"))) {
			return val;
		} else if (!"res".equals(request.getParameter("otype"))) {
		  // TODO CHANGE
			String lang = request.getParameter("cur_lang");
			if ((lang != null)&&(!"".equals(lang))) {
				return val;
			}
		}
	}
	
	// it's a uri -- maybe written with a short name
	
	String x = SLServlet.getSemanlinkConfigProps().getUriString(val);
	if (x == null) x = val;
	
	// is uri correct ?
	// (it can be entered by the user )
	String errMess = JenaUtils.getUriViolations(x, false);
	if (errMess != null) {
		throw new RuntimeException(errMess);
	}	

	return x;
}

// TODO check
private String jenaLangArg(String lang) {
	if ( ("".equals(lang)) || ("-".equals(lang)) ) return null;	
	return lang;
}

} // end Action
