package net.semanlink.servlet;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.*;

import org.apache.struts.action.*;

import net.semanlink.semanlink.*;
import net.semanlink.util.Util;


public class Action_NewNote extends BaseAction {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		try {
			/*String docuri = request.getParameter("docuri"); // pas de decode : issu d'un champ de saisie, non code (?)
			if ((docuri == null) || ("".equals(docuri))) {
				return error(mapping, request, "No document's URI");
			}
			docuri = SLUtils.laxistUri2Uri(docuri);
			*/

			SLModel mod = SLServlet.getSLModel();

			Form_NewNote fform = (Form_NewNote) form;

			String title = fform.getTitle().trim();
			if ("".equals(title)) title = null;
			String lang = fform.getLang();
			if (lang != null) lang = lang.trim();
			if ("".equals(lang)) lang = null;
			String comment = fform.getComment().trim();
			if ("".equals(comment)) comment = null;
			/*String note = fform.getNote().trim();
			if ("".equals(note)) note = null;*/

			File dir = mod.dirToSaveANote();
			String sfn = getShortFilename(title, null);
			File saveAs = new File(dir, sfn);
			if (saveAs.exists()) {
				boolean overwrite = param2boolean("overwrite", request, false);
				if (!overwrite) return error(mapping, request, "A file " + saveAs.toString() + " already exists.");
			}
			String localUri = mod.fileToUri(saveAs);
			// on pourrait aussi bien construire directement son url : auquel cas on n'aurait peut-être même pas à mettre
			// dans webServer l'association pour les notes ? (si : il faut bien pouvoir passer de l'un à l'autre
			// au moment de décider où on écrit, avec quelle base)
			SLDocument localDoc = mod.getDocument(localUri);
			SLDocument doc = localDoc;
			mod.setDocProperty(doc, SLVocab.TITLE_PROPERTY, title, lang);
			if (comment != null) mod.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, comment, lang);

			mod.onNewDoc(doc);

			// POST REDIRECT
			// getJsp_Document(doc, request);
			// x = mapping.findForward("continue");
			String redirectURL = Util.getContextURL(request) + HTML_Link.docLink(doc.getURI());
    	response.sendRedirect(response.encodeRedirectURL(redirectURL));
    	return null;
		} catch (Exception e) {
			return error(mapping, request, e );
		}
	} // end execute
	
	/** si le HTMLPageDownload est présent ds le Form_Bookmark, le prend, sinon le calcule.
	 *  N'y a-t-il pas un pb si on a déjà fait un HTMLPageDownload sur un autre doc, puis
	 *  qu'il n'y en a pas de remis au dernier apple ? Si, surement : il doit faloir remettre
	 * à vide le HTMLPageDownload de Form_Bookmark une fois qu'il est consommé. Ceci est fait en fin
	 * de l'execute
	 * @param bookmarkForm
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	/*private HTMLPageDownload getDownload(Form_Bookmark bookmarkForm) throws MalformedURLException, IOException {
	 // return new HTMLPageDownload(new URL(docuri));
	  HTMLPageDownload x = bookmarkForm.getDownload();
	  if (x == null) x = new HTMLPageDownload(new URL(bookmarkForm.getDocuri()));
	  return x;
	  }*/
	
	/*private String getTitle(Form_Bookmark bookmarkForm) throws MalformedURLException, IOException {
	 String title = bookmarkForm.getTitle();
	 if ((title == null)||("".equals(title))) title = getDownload(bookmarkForm).getTitle();
	 // title = getDownload(bookmarkForm).getTitle();
	  return title;
	  }*/
	
	private String getShortFilename(String title, String docuri) {
		return SLUtils.shortFilenameFromString(title);
	}
	
} // end Action
