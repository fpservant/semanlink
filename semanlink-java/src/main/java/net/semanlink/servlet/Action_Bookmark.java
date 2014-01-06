package net.semanlink.servlet;
import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.Util;
import net.semanlink.util.html.HTMLPageDownload;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/*
 // YA 2 TRUCS NULS : 
  // 1) on lit le html une fois ici, et aune autre ds le addedDoc pour extraire metadata
   // 2) si problème lecture du html ici (générant une RuntimeException), on plante ds 
    // le HTMLPageDoowload, et on n'est même pas capable de suaver le fichier tel quel
     // (simple save) Ex :
      WARN [http-8080-Processor23] (RequestProcessor.java:538) - Unhandled Exception thrown: class net.semanlink.semanlink.SLRuntimeException
      java.lang.RuntimeException: Unexpected top level block close
      at javax.swing.text.html.CSSParser.getNextStatement(CSSParser.java:175)
      at javax.swing.text.html.CSSParser.parse(CSSParser.java:136)
      at javax.swing.text.html.StyleSheet$CssParser.parse(StyleSheet.java:3031)
      at javax.swing.text.html.StyleSheet.addRule(StyleSheet.java:272)
      at javax.swing.text.html.HTMLDocument$HTMLReader.addCSSRules(HTMLDocument.java:3345)
      at javax.swing.text.html.HTMLDocument$HTMLReader$HeadAction.end(HTMLDocument.java:2495)
      at javax.swing.text.html.HTMLDocument$HTMLReader.handleEndTag(HTMLDocument.java:2233)
      at net.semanlink.util.html.HTMLDocumentLoaderNew$1.handleEndTag(HTMLDocumentLoaderNew.java:143)
      at javax.swing.text.html.parser.DocumentParser.handleEndTag(DocumentParser.java:217)
      at javax.swing.text.html.parser.Parser.parse(Parser.java:2072)
      at javax.swing.text.html.parser.DocumentParser.parse(DocumentParser.java:106)
      at javax.swing.text.html.parser.ParserDelegator.parse(ParserDelegator.java:78)
      at net.semanlink.util.html.HTMLPageDownload.loadDocument(HTMLPageDownload.java:119)
      at net.semanlink.util.html.HTMLDocumentLoader.loadDocument(HTMLDocumentLoader.java:22)
      at net.semanlink.util.html.HTMLDocumentLoader.loadDocument(HTMLDocumentLoader.java:18)
      at net.semanlink.util.html.HTMLPageDownload.<init>(HTMLPageDownload.java:31)
      at net.semanlink.servlet.Action_Bookmark.execute(Action_Bookmark.java:52)
      */

/**
 * On arrive ici après Action_BookmarkForm
 * 
 * Question des caratères spéciaux : résolu pour le short name, pas pour le path complet // TODO still true?
 */
public class Action_Bookmark extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	// ActionForward x = null;





	String redirectURL = null;
	SLModel mod = SLServlet.getSLModel();
	// passons en mode "edit"
	setEdit(request, true);

	Form_Bookmark bookmarkForm = (Form_Bookmark) form;
	String title = bookmarkForm.getTitle().trim();
	if ("".equals(title)) title = null;
	String lang = bookmarkForm.getLang();
	if (lang != null) lang = lang.trim();
	if ("".equals(lang)) lang = null;
	String comment = bookmarkForm.getComment().trim();
	if ("".equals(comment)) comment = null;

	try {
		
		
		
		
		if ((false) && (request.getParameter("nirTagBtn") != null)) { // @find nir2tag
			// cas de création d'un tag à partir de l'uri d'une non information resource
			String nonInformationResourceUri = request.getParameter("nir"); // pas de decode : issu d'un champ de saisie, non code (?)
			if ((nonInformationResourceUri == null) || ("".equals(nonInformationResourceUri))) {
				return error(mapping, request, "No 'non information resource' URI");
			}
			nonInformationResourceUri = SLUtils.laxistUri2Uri(nonInformationResourceUri);
			// mod.setExternalNonInformationResource4Tag(nonInformationResourceUri)
			
			String kwLabel = title;
			if (kwLabel == null) return error(mapping, request, "No label for tag");
			Locale locale = null;
			if (lang != null) {
				locale = new Locale(lang);
			} else {
				locale = Locale.getDefault();
			}
			SLKeyword kw = mod.kwLabel2KwCreatingItIfNecessary(kwLabel, mod.getDefaultThesaurus().getURI(), locale);
			String kwUri = kw.getURI();
			mod.setKwProperty(kwUri, "http://xmlns.com/foaf/0.1/primaryTopic", nonInformationResourceUri);
			if (comment != null) mod.addKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, comment, lang); // si on était sûr que c'est un new kw, on pourrait faire set seulement
			
			redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kwUri, false, ".html");

			
		} else {
			SLDocument docToDisplay = null; // doc to be displayed
			String docuri = request.getParameter("docuri"); // pas de decode : issu d'un champ de saisie, non code (?)
			if ((docuri == null) || ("".equals(docuri))) {
				return error(mapping, request, "No document's URI");
			}
	
			// docuri peut venir 
			// 1) de la bookmarklet - auquel cas, elle a été URLDecoder.decodée, et se présente par ex
			// sous la forme http://a/b/un exemple éé.html
			// 2) d'un paste de uri récoltée sur une de nos pages (ou, au moins avant, d'une valeur prédoc
			// à l'uri d'une de nos pages) - auquel cas elle est déjà toute bien comme il faut, avec
			// des %20 et des %C3
			// 3) d'une saisie -- auquel cas elle peut être n'importe quoi,
			// y compris complétement fause
			// Pour remettre d'équerre ce qui viendrait du cas 1, et refuser les erreurs du cas 3,
			// on fait la chose suivante (cf Test.testUriSmall)
			docuri = SLUtils.laxistUri2Uri(docuri);
			
			// SLDocument docOnline = mod.getDocument(docuri);
			SLDocument docOnline = mod.smarterGetDocument(docuri);
			
			
			
			
			
			if (request.getParameter("bookmark2tagBtn") != null) { 		// @find bookmark2tag
				// cas de création d'un tag à partir de l'uri d'un bookmark
				String kwLabel = title;
				if (kwLabel == null) return error(mapping, request, "No label for tag");
				Locale locale = null;
				if (lang != null) {
					locale = new Locale(lang);
				} else {
					locale = Locale.getDefault();
				}
				SLKeyword kw = mod.kwLabel2KwCreatingItIfNecessary(kwLabel, mod.getDefaultThesaurus().getURI(), locale);
				String kwUri = kw.getURI();
				mod.addKwProperty(kwUri, SLVocab.SL_DESCRIBED_BY_PROPERTY, docOnline.getURI());
				if (comment != null) mod.addKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, comment, lang); // si on était sûr que c'est un new kw, on pourrait faire set seulement
				
				redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kwUri, false, ".html");
				
				
				
				
				
			} else {
				
				
				
				
				
				
				
				// creation of a bookmark
				if (mod.existsAsSubject(docOnline)) {
					// 2007-01 (POST REDIRECT)
					// getJsp_Document(docOnline, request); // documente l'attribut jsp de la request
					docToDisplay = docOnline;
				} else {
					/*
					Form_Bookmark bookmarkForm = (Form_Bookmark) form;
					String title = bookmarkForm.getTitle().trim();
					if ("".equals(title)) title = null;
					String lang = bookmarkForm.getLang();
					if (lang != null) lang = lang.trim();
					if ("".equals(lang)) lang = null;
					String comment = bookmarkForm.getComment().trim();
					if ("".equals(comment)) comment = null;
					*/
		
					String downloadFromUri = request.getParameter("downloadfromuri"); // pas de decode : issu d'un champ de saisie, non code (?)
					if ((downloadFromUri == null) || ("".equals(downloadFromUri))) {
						downloadFromUri = docuri;
					} else {
						downloadFromUri = SLUtils.laxistUri2Uri(downloadFromUri);	    	
					}
		
					if (HTMLPageDownload.isLeMondePrintPage(downloadFromUri)) {
						// on a le titre à la con "Imprimer page"
						if (comment != null) {
							if ((title == null) || (title.indexOf("Imprimez un élément") > -1)) {
								title = comment;
								comment = null;
							}
						}
					}
		
					SLDocument doc = null; // local ou online, c'est selon.
					// Attention ceci n'est pas bon, car reste documenté d'un appel à l'autre
					// (ou alors il faudrait le mettre ces infos à null en fin de execute)
					// if (bookmarkForm.getBookmarkBtn() != null) {
					SLDocument localDoc_SourceToBeAdded = null; // sera !null (et égal a localDoc)
					// si on souhaite stocker la source
					// (il faut prendre garde de ne pas créer en 1er le statement SOURCE_PROPERTY,
					// (qui a nécessité un truc spécial dans le "onNewDocument" du listener jena
					// pour ne pas entrainer la création des statements de new doc pour la source)
					// parce que sinon, il n'y a pas le traitement new doc 
					if (request.getParameter("bookmarkBtn") != null) {
						doc = docOnline;
					} else {
						boolean overwrite = param2boolean("overwrite", request, false);
						File saveAs = Action_Download.downloadFile(downloadFromUri, title, overwrite, mod);
		
						// String localUri = mod.filenameToUri(saveAs.toString());
						String localUri = mod.fileToUri(saveAs);
						SLDocument localDoc = mod.getDocument(localUri);
						if (request.getParameter("bookmarkWithCopyBtn") != null) {
							doc = docOnline;
							localDoc_SourceToBeAdded = localDoc;
							// mod.addDocProperty(addSourceTo, SLVocab.SOURCE_PROPERTY, docuri);
						} else if (request.getParameter("copyWithBookmarkBtn") != null) {
							doc = localDoc;
							localDoc_SourceToBeAdded = localDoc;
							// ne pas faire ça maintenant, sinon on perd les ajouts de metadata
							// on new doc, becoz of un traitement spécial pour la prop source (voir listeenr
							// avec enw doc)
							// mod.addDocProperty(addSourceTo, SLVocab.SOURCE_PROPERTY, docuri);
						} else if (request.getParameter("localDocBtn") != null) {
							doc = localDoc;
						}
					}
					mod.setDocProperty(doc, SLVocab.TITLE_PROPERTY, title, lang);
					if (comment != null) mod.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, comment, lang);
					if (localDoc_SourceToBeAdded != null) {
						mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, docuri);
					}
					mod.onNewDoc(doc);
					
					
					
					// 2007-01 (POST REDIRECT)
					// getJsp_Document(doc, request); // documente l'attribut jsp de la request
					docToDisplay = doc;
				} // doc already exists or not
		
				// POST REDIRECT
				// x = mapping.findForward("continue");
				redirectURL = Util.getContextURL(request) + HTML_Link.docLink(docToDisplay.getURI());
			}
			
			
		}	
			
		
		
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

} // end Action
