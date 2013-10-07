package net.semanlink.servlet;
import javax.servlet.http.*;

import org.apache.struts.action.*;

import net.semanlink.semanlink.*;
/**
 * Pour utilisation au sein d'une form
 * rq : il pourrait être pas mal d'être capable de passer de l'uri à ce que c'est :
 * kw ou doc. Quoique : on a 2 modèles différents, id est 2 recherches pour savoir.
 * Quoique du quoique : si un seul modèle, 2 recherches aussi : la seconde pour avoir
 * les infos sur la nature (rdf class) de l'entité.Ce qui impliquerait aussi d'avoir
 * bien ds le modèle les statements rdfclass = akeyword -- ce qui n'est peut-être pas tjrs le cas,
 * il y avait un bug l'autre jour dont je ne me souviens pas s'il est corrigé : un cas de création
 * de kw (genre label pas trouvé) où on n' pas la création de tous les statements ds le model.
 * Rq au sujet du fonctionnement avec 2 modèles : on peut aussi ajouter à la volée
 * (lors du chargement) tous les tels statements. (moyen de faire un modelcorrector). Mais si on voulait
 * garder l'vaantage de ne pas avoir ces statements redondants avec le fait
 * d'être ds un modèle dédié kw (est-ce réaliste?), il faudr	ait ne pas les écrires lors du save (
 * pour cela, virer les statements du model) 
 */
public class Action_PasteKeyword extends BaseAction {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		try {
			String pasteToUri = request.getParameter("uri");
			if (pasteToUri == null) pasteToUri = request.getParameter("uri");
			String asWhat = request.getParameter("as");
			String field = null;
			
			if ("keyword".equals(asWhat)) {
				field = "tags";
				if (pasteToUri == null) pasteToUri = getDocUri(request);
			} else {
				// paste to a keyword
				if (pasteToUri == null) pasteToUri = getTagUri(request);
				if (asWhat.indexOf("child") > -1) {
					field = "children";
				} else if (asWhat.indexOf("parent") > -1) {
					field = "parents";
				} else if (asWhat.indexOf("friend") > -1) {
					field = "friends";
				} else if (asWhat == null) {
					throw new IllegalArgumentException("No as parameter in request");
				} else {
					throw new IllegalArgumentException("Incorrect as parameter in request");
				}			
			}
			Action_EditTagList.doPaste(pasteToUri, field, request.getSession(), SLServlet.getSLModel());
			// POST REDIRECT
		  String referer = request.getHeader("referer");
		  response.sendRedirect(response.encodeRedirectURL(referer));
		  return null;
	  } catch (Exception e) {
	    return error(mapping, request, e );
	  }
	} // end execute

} // end Action
