package net.semanlink.servlet;
import java.util.Locale;

import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
import net.semanlink.util.Util;
import net.semanlink.util.servlet.BasicServlet;


// VOIR Action_EditTagList : pourquoi les 2 ??????

/**
 * Action demandant d'afficher un kw donné par une simple String (son label)
 * OU BIEN from livesearchform
 * 
 * Au sujet du livesearchform, voir les scripts js liveSearchSubmit4Get et liveSearchSubmit4Post
 * Le livesearchform (suite à highlight + enter ou enter ds la boite de saisie)
 * retourne ds les params :
 * kwhref, qui est l'href du 1er élément de la liste (s'il y en a )
 * q qui est ce qui est saisi ds la boite
 */
public class Action_GoKeyword extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	SLKeyword kw = null;
	boolean edit = getSessionEditState(request);
	try {
		SLModel mod = SLServlet.getSLModel();
		String kwhref = request.getParameter("kwhref");
		// System.out.println("Action_GoKeyword href : " + kwhref );
		boolean isActionOnFoundList = false; // est-on ds le cas clic ou enter sur un elt de la liste trouvee
		if (kwhref != null) { // livesearchform (et on est en edit, sinon, cette action n'est pas invoquée)
			// ONLY IN EDIT MODE, SENT FROM THE LIVE SEARCH
			// THIS IS NOT VERY GOOD AND SHOULD BE CHANGED (STARTING FROM THE CONTENT
			// OF THE REQUEST SENT BY THE LIVE SEARCH)
			
			// BEWARE :
			// 2007-01 (httprange-14): where we had a tag without extension (eg /semanlink/tag/john_sofakolle?resolvealias=true)
			// we now have /semanlink/tag/john_sofakolle.html?resolvealias=true
			// Comments have not been corrected
			
			// kwhref de la forme :
			// /semanlink/tag/john_sofakolle?resolvealias=true
			// /semanlink/tag/john_sofakolle;jsessionid=8B48D779E19AFE48311F448EE41D16D3?resolvealias=true
			// ou
			// /semanlink/tag/?uri=http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g&resolvealias=true
			// /semanlink/tag/;jsessionid=5E69D62C1C96376D4A082E5CB2B55B6B?uri=http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g&resolvealias=true // j'ai un doute sur le & :
			// n'est-ce pas - ou ne devrait-ce pas être &amp; ?
			// Attention, avec l'éventuel sessionId de l'url rewriting
			// (cas où on récupère le href de la ligne hilitée).
			
			// ATTENTION : avec IE, j'ai 127.0.0.1:8080 en tête
			// (on l'a déjà au niveau du liveSearchSubmit ds livesearch.js)
			// ceci n'est donc pas bon dans ce cas
			// String tag = kwhref.substring(15);
			// il faut :
			// String tag = kwhref.substring(kwhref.indexOf("/semanlink/tag/") + 15);

			// MAIS attention encore au cas où l'utilisateur a tapé une chaine qui n'est pas un tag
			int k = kwhref.indexOf("/tag/");
			if (k > -1) {
				k = k+5;
				if (kwhref.length() > k) { // tout va bien, on a bien un tag
					isActionOnFoundList = true;
					String tag = kwhref.substring(k);
					// tag vaut :
					// john_sofakolle?resolvealias=true
					// john_sofakolle;jsessionid=8B48D779E19AFE48311F448EE41D16D3?resolvealias=true
					// ou
					// ?uri=http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g&resolvealias=true
					// ;jsessionid=5E69D62C1C96376D4A082E5CB2B55B6B?uri=http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g&resolvealias=true // j'ai un doute sur le & :

					k = tag.indexOf("?uri=");
					if (k > -1) {
						tag = tag.substring(k+5);
						// tag vaut :
						// http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g&resolvealias=true
						k = tag.indexOf("&"); // > -1 si url rewriting
						if (k > -1) tag = tag.substring(0,k);
						// tag vaut :
						// http%3A%2F%2Fsiicg.tpz.renault.fr%2Fthesaurus%23c2g
						tag = java.net.URLDecoder.decode(tag,"UTF-8");
					} else {
						// kwuri vaut :
						// john_sofakolle?resolvealias=true
						// ou
						// john_sofakolle;jsessionid=8B48D779E19AFE48311F448EE41D16D3?resolvealias=true
						k = tag.indexOf(";"); // > -1 si url rewriting
						if (k < 0) k = tag.indexOf("?");
						if (k > -1) tag = tag.substring(0,k);
						// tag vaut :
						// john_sofakolle
					}

					// 2007-01 (httprange-14) we have here ".html" at the end
					tag = CoolUriServlet.getTagUri(tag);
					if (tag.endsWith(".html")) tag = tag.substring(0, tag.length()-5); // 2007-01 (httprange-14)
					
					// System.out.println("kwuri : " + kwuri);
					// Il se trouve qu'on peut avoir des alias (ceci dit, uniquement sur le 1er niveau de l'arbre,
					// voir Jsp_Search et ThesaurusIndex. On ne peut donc faire simplement :
					// kw = mod.getKeyword(kwuri);
					kw = mod.resolveAlias(tag);
	
					/*String whatToDo = request.getParameter("actionprop");
					String targetUri = request.getParameter("targeturi");
					if ("add2doc".equals(whatToDo)) {
						SLDocument doc = mod.getDocument(targetUri);
						mod.addKeyword(doc, kw);
						BaseAction.getJsp_Document(doc, request);
					} else if ("add2parents".equals(whatToDo)) {
						SLKeyword targetKw = mod.getKeyword( targetUri) ;
						mod.addChild(kw, targetKw);
						request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));
					} else if ("add2children".equals(whatToDo)) {
						SLKeyword targetKw = mod.getKeyword( targetUri) ;
						mod.addChild(targetKw, kw);
						request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));
					} else { // go kw
						request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
					}*/
				}			
			} // livesearchform avec action sur la liste trouvée
		} 
		
		if (!isActionOnFoundList) {
			// request.setCharacterEncoding("UTF-8"); // TODO
			String kwLabel = request.getParameter("kw"); // old "go" form
			if (kwLabel == null) kwLabel = request.getParameter("q"); // live search form
			
			// optim possible
	  	if (!edit) { // cf "googlebot bug" (we were not paying attention not to create kw when not in edit mode)
	  		String kwUri = mod.kwLabel2ExistingKwUri(kwLabel, mod.getDefaultThesaurus().getURI(), null, true);
	  		// if (kwUri != null) kw = mod.getKeyword(kwUri);
	  		if (kwUri != null) kwUri = mod.kwLabel2UriQuick(kwLabel, mod.getDefaultThesaurus().getURI(), null); // in this case, we'll just go to an unexisting kw
	  		kw = mod.getKeyword(kwUri);
	  	} else {
				kw = mod.kwLabel2KwCreatingItIfNecessary(kwLabel, mod.getDefaultThesaurus().getURI(), null); // optim possible
	  	}
		}
		
		// At this point, kw not null (but maybe doesn't exist -- only in the case !edit)
		
		String whatToDo = request.getParameter("actionprop");
		String targetUri = request.getParameter("targeturi");
		/*
		// This was before 2007-01:
		// everything OK, except that we get urls such as "..../gokeyword.do" in the browser nav box
		// DON'T DELETE
		if ("add2doc".equals(whatToDo)) {
			SLDocument doc = mod.getDocument(targetUri);
			mod.addKeyword(doc, kw);
			BaseAction.getJsp_Document(doc, request);

		} else if ("add2parents".equals(whatToDo)) {
			SLKeyword targetKw = mod.getKeyword( targetUri) ;
			mod.addChild(kw, targetKw);
			request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));

		} else if ("add2children".equals(whatToDo)) {
			SLKeyword targetKw = mod.getKeyword( targetUri) ;
			mod.addChild(targetKw, kw);
			request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));

		} else { // go kw
			request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
		}
		*/

		// POST REDIRECT 
		// we want to display a bookmarkable url (the one of the resource we modify) after a post
		// The modified resource is the referer
		// note: referer contains sessionId, if cookies are off (and a session maintained)
		// If we use it in a sendRedirect, we won't have to add it
		String referer = request.getHeader("referer");
		
		// a first idea has been to try to set the "Content-Location" http header (to the referer)
		// (every thing else beeing the same in the "before 2007-01" piece of code above)
		// Doesn't give the expected result (having this url displayed
		// by the browser). Should have we tried to set the status code of the response?
		
		// So, OK, let's do it using a redirect
		// (this has a cost: one more exchange between client and server
		// - and a positive side: less programming on the server side)
		// Very well, but here, struts expect that we returns an ActionForward
		// It seems that returning null is OK.
		
		// could referer be null?
		// This would be a strange situation. Anyway, we have a targeturi
		// param in the request. We should be able to compute the redirect url from it.
		// But hey, we want to simplify our life. So...
		// (and I don't see at the moment how the referer could be something else than the targeturi)
		// (NOTE: we could want to go somewhere else - we'll see that when such a situation occurs.
		// It's not the case here)
		// NEVERTHELESS, lets play it conservativ: if referer not null,
		// we use it, else, we continue as we were doing
		
		// if we do a redirect, we leave x to null
		String redirectURL = null;
		if ("add2doc".equals(whatToDo)) {
			if (!edit) throw new RuntimeException("Action forbidden when not in edit mode");
			SLDocument doc = mod.getDocument(targetUri); 
			mod.addKeyword(doc, kw);
			if (referer == null) {
				// pre 2007-01, normally useless now
				BaseAction.getJsp_Document(doc, request); 
			} else {
				redirectURL = referer;
			}

		} else if ("add2parents".equals(whatToDo)) {
			if (!edit) throw new RuntimeException("Action forbidden when not in edit mode");
			SLKeyword targetKw = mod.getKeyword( targetUri) ;
			mod.addChild(kw, targetKw);
			if (referer == null) {
				// pre 2007-01, normally useless now
				request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));
			} else {
				redirectURL = referer;
			}

		} else if ("add2children".equals(whatToDo)) {
			if (!edit) throw new RuntimeException("Action forbidden when not in edit mode");
			SLKeyword targetKw = mod.getKeyword( targetUri) ;
			mod.addChild(targetKw, kw);
			if (referer == null) {
				// pre 2007-01, normally useless now
				request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));
			} else {
				redirectURL = referer;
			}

		} else if ("add2friends".equals(whatToDo)) {
			if (!edit) throw new RuntimeException("Action forbidden when not in edit mode");
			SLKeyword targetKw = mod.getKeyword( targetUri) ;
			mod.addFriend(targetKw, kw);
			if (referer == null) {
				// pre 2007-01, normally useless now
				request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(targetKw, request));
			} else {
				redirectURL = referer;
			}

		} else { // go kw
			// POST REDIRECT 
			// request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
			redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kw.getURI(), false, ".html");
		}
		
		if (redirectURL != null) {
			// x = null; // already the case
    	response.sendRedirect(response.encodeRedirectURL(redirectURL));
		} else {
			// should not happen any more (POST REDIRECT)
			x = mapping.findForward("continue");			
		}
	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute
} // end Action


