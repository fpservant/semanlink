package net.semanlink.servlet;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.arxiv.Arxiv;
import net.semanlink.semanlink.*;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.Util;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * On peut arriver ici soit
 * - par un clic sur "New document"
 * - via la bookmarklet
 * 
 * J'ai eu bien du mal à réaliser la même chose que Blogmarks.net et leur bookmarklet.
 * En reprenant le même code, qui utilise la fct javascript encodeURIcomponent pour encoder
 * le titre et le texte sélectionné dans la page bookmarkée,
 * et qui envoie tout ça (fatalement), par un http GET,
 * j'obtiens, du côté serveur (Tomcat 5.0, struts 1.1 -- struts est-il en cause ? j'ai en tous cas
 * essayé de faire sans lui, mais ne suis pas tout à fait sûr d'etre allé au bout), et ce, dès le doGet
 * de la SLServlet, des parameters fouareux, avec les "é" transformés en "Ã©".
 * D'anciens comments ds le doGet montraient que j'avais déjà eu des pbs avec les car UTF-8 dans le get,
 * et pas ds le post. J'ai aussi vu des gens se plaindre d'avori un pb avec get et pas post ds des conditions
 * similaires.
 * J'ai pu résoudre ça en procédant à un double appel de encodeUriComponent et, du côté serveur,
 * en appelant URLDecoder.decode(param, "UTF-8")
 * Pour é, encodeUriComponent donne %C3%A9
 * le faire 2 fois donne %25C3%25A9
 * 
 * Le même problème (et la même solution) dans le livesearch.js (rq : ce qui met hors de cause struts,
 * puisque la forme en question, au moment ou j'écris, ne l'utilise pas)
 * 
 * Voir aussi ds CoolUriServlet le parameter js=1
 * 
 * (Rq : on a une chose similaire pour les liens vers un doc avec par ex showdocument.do?docuri=...
 * en effet, le docuri tel qu'on le sauvegarde contient, pour é, %C3%A9, et le HTMLLinklinkToDucment
 * procède à un URLEncoder.encode(uri,"UTF-8") -- ceci dit, côté serveur, on ne fait pas de decode,
 * cf le comment ds Action_ShowDocument 
 * 	// JE NE COMPRENDS PAS POURQUOI MAIS,
 *	// alors que docuri a ete encode, il ne faut pas ici le decoder.
 * Bon, ici, on decode!!!
 * 
 * JAVASCRIPT de la bookmarklet :
 * (on pourrait virer le "via", qui ne sert pas.
 * javascript:q='';r='';l='';if%20(window.getSelection)q=window.getSelection();else%20if%20(document.getSelection)q=document.getSelection();else%20if%20(document.selection)q=document.selection.createRange().text;if%20(document.referrer)%20r=document.referrer;if%20(typeof(_ref)!='undefined')%20r=_ref;void(location.href='http://127.0.0.1:8080/semanlink/bookmarkform.do?docuri='+%20encodeURIComponent(encodeURIComponent(location.href))+'&title='+%20encodeURIComponent(encodeURIComponent(document.title))+'&comment='+encodeURIComponent(encodeURIComponent(q))+'&lang='+%20encodeURIComponent(l)+'&via='+encodeURIComponent(r));
 */
public class Action_BookmarkForm extends BaseAction {

public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		
		//
		// BEWARE docuri here is most likely the url of a web page
		//
		
		String docuri = request.getParameter("docuri"); // attention encodage !!!
		SLModel mod = SLServlet.getSLModel();
		Form_Bookmark bookmarkForm = (Form_Bookmark) form;
		// shall we display the form with only "New Document" button, or with "Bookmark", "Local Copy", etc... ?
		boolean oneBtnOnly = false;
		if (docuri == null) { // cas du clic sur le btn "New document" du menu
			bookmarkForm.reset(); // pour ne pas garder title et comment sur la form pour un nouveau doc
			Jsp_Page jsp = new Jsp_Page(request, response);
			jsp.setTitle(jsp.i18l("bookmarkform.newbookmark"));
			jsp.setContent("/jsp/bookmarkform.jsp");
			request.setAttribute("net.semanlink.servlet.jsp", jsp);
			
		} else { // via la bookmarklet
			bordelEncoding(bookmarkForm);
			docuri = bookmarkForm.getDocuri(); // il faut car a été modifié par bordelEncoding

			// ben non, parce que sur un /document/, on reste où on est !!!
			//        if (docuri.startsWith(SLServlet.getServletUrl())) {
			//        	response.sendRedirect(response.encodeRedirectURL(docuri));
			//        	return null; // EXIT !!!        	
			//        }
			
			// 2020-12 arxiv docs: if we're passed the pdf url,
			// would be better to use the abs url
			// (if the pdf is already bookmarked,
			// we will then miss it below in docUrl, but will only happen 
			// with bookmarks created before this, and
			// we would miss finding it if wehad  pass the abs url anyway)
			
			// Not very useful ? : All this, just for the case we ask to bookmark the pdf.
			// Should we just leave the user make his own errors ?
			// But this allows, in search by url (Action_SearchDoc), to search for the abs
			// whatever the user passes

			Arxiv arx = Arxiv.getByUrl(docuri);
			if (arx != null) {
				String abs = arx.absUrl();
				if (!docuri.equals(abs)) {
					docuri = abs;
					bookmarkForm.setDocuri(docuri);
					bookmarkForm.setTitle(arx.getNum()); // juste pour ne pas avoir d'erreur "no title". De toute façon, c le vrai bon titre qui sera pris
				}
			}
			
			bookmarkForm.setDownloadfromuri(docuri); // not used when arxiv
			
			// is there a URL in sl that matches docuri that we can redirect to?
			String redirectURL = docUrl(docuri, mod, Util.getContextURL(request));
			if (redirectURL != null) {
				response.sendRedirect(response.encodeRedirectURL(redirectURL));
				return null; // EXIT !!!				
			}
			
			Jsp_Page jsp = new Jsp_Page(request, response);
			jsp.setTitle(jsp.i18l("bookmarkform.newbookmark"));
			jsp.setContent("/jsp/bookmarkform.jsp");
			request.setAttribute("net.semanlink.servlet.jsp", jsp);
			// shall we display the form with only "New Document" button, or with "Bookmark", "Local Copy", etc... ?
			oneBtnOnly = mod.isLocalDocument(docuri);
    
		} // docuri null or not (bookmarklet or not)


		// @find nir2tag
		// ok, that's stupid, but I was using struts...
		String nirUri = bookmarkForm.getNir();
		if ((nirUri != null) && (!(nirUri.equals("")))) {
			request.setAttribute("nonInformationResourceUri", nirUri);
		}

		// shall we display the form with only "New Document" button, or with "Bookmark", "Local Copy", etc... ?
		request.setAttribute("oneBtnOnly", new Boolean(oneBtnOnly));

		if (!Jsp_Page.isEditor(request)) {
			// user must be authorized
			// we'll send back to the page the person attempted to bookmark
			request.getSession().setAttribute("net.semanlink.servlet.goBackToPage", docuri);
			response.sendRedirect(response.encodeRedirectURL(Util.getContextURL(request)+"/sl/about/LOGON.htm"));
			return null;
		}

		x = mapping.findForward("continue");

	} catch (Exception e) {
		return error(mapping, request, e );
	}
	return x;
} // end execute


// a good sl page to go given the URL of a, presumably, doc
static public String docUrl(String docuri, SLModel mod, String contextURL) throws Exception {
	docuri = SLUtils.laxistUri2Uri(docuri);
	String errMess = JenaUtils.getUriViolations(docuri,false);
	if (errMess != null) {
		throw new RuntimeException(errMess);
	}

	// 2019-05 Take care of case when bookmarlet used on a local copy (cf /document/ -> /doc/)
	String u = SLDocumentStuff.page2DocUri(docuri, contextURL);
	if (u != null) docuri = u;
	
	// SLDocument doc = mod.getDocument(docuri);
	// if docuri protocol is file, and this file is served by this.webServer, returns the http document.
	SLDocument doc = mod.smarterGetDocument(docuri);

	boolean alreadyExists = false;

	// 2019-03 : uris for bookmarks
	SLDocument bookmark2019 = mod.bookmarkUrl2Doc(doc.getURI());
	if (bookmark2019 != null) {
		return bookmark2019.getURI(); // TODO PAS SUR : voir + bas alreadyExists, HTML_Link.docLink
	}


	if (mod.existsAsSubject(doc)) {
		alreadyExists = true;
		
	} else {
		// il peut s'agir de l'url source d'un doc local
		// (x, source, doc.getUri()) 
		SLDocument localDoc = mod.source2LocalCopy(docuri); // TODO REVOIR
		if (localDoc != null) {
			alreadyExists = true;
			doc = localDoc;
		}       		
	}

	if (!alreadyExists) {
		// il y a un autre cas, où le doc n'existe pas, mais qu'on va traiter comme si il existait :
		// quand on a affaire à un fichier ds un SLDataFolder (qui n'existe pas en tant que doc sl)
		// (comme ça on va être envoyé sur cette page et on pourra tjrs le créer la-bas)
		if (doc.getURI().startsWith(SLServlet.getServletUrl() + CoolUriServlet.DOC_SERVLET_PATH)) {
			alreadyExists = true;
		}
	}

	if (alreadyExists) {
		SLDocumentStuff docStuff = new SLDocumentStuff(doc, mod, contextURL);
		return docStuff.getAboutHref();
	}

	// can be the homepage or the sl:describedBy of a tag
	// In this case, redirect to the tag
	SLKeyword kw = mod.object2Tag(SLVocab.SL_HOME_PAGE_PROPERTY, doc.getURI());
	if (kw == null) kw = mod.object2Tag(SLVocab.SL_DESCRIBED_BY_PROPERTY, doc.getURI());
	if (kw != null) {
		return HTML_Link.getTagURL(contextURL, kw.getURI(), false, ".html");
	}

	return null;
}

/** cf question du double appel à encodeURICompenent ds la bookmarklet */
private void bordelEncoding(Form_Bookmark bookmarkForm) throws UnsupportedEncodingException {
	String s;
	s = bookmarkForm.getDocuri();
	if (s != null) {
		String docuri = URLDecoder.decode(s,"UTF-8");
		bookmarkForm.setDocuri(docuri);
	}
	s = bookmarkForm.getTitle();
	if (s != null) bookmarkForm.setTitle(URLDecoder.decode(s,"UTF-8"));
	s = bookmarkForm.getComment();
	if (s != null) {
		s = URLDecoder.decode(s,"UTF-8");
		// a bit of formatting (cf unwanted spaces or returns coming from the html)
		s = s.replaceAll("\t", " ");
		s = s.replaceAll("  ", " ");

		BufferedReader reader = new BufferedReader(new StringReader(s));
		StringBuffer comment = new StringBuffer();
		String line;
		try {
			for (;;) {
				line = reader.readLine();
				if (line == null) break;
				line = line.trim();
				if ("".equals(line)) {
					continue;
				} else {
					comment.append(line);
					comment.append(" ");
				}
			}
			s = comment.toString();
		} catch (Exception e) {e.printStackTrace();}
		bookmarkForm.setComment(s);
	}

	//@find nir2tag
	// non information resource uri
	s = bookmarkForm.getNir();
	if (s != null) {
		String nirUri = URLDecoder.decode(s,"UTF-8");
		bookmarkForm.setNir(nirUri);
	}
}

} // end Action
