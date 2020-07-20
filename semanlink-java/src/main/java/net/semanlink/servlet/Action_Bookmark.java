package net.semanlink.servlet;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import net.semanlink.arxiv.Arxiv;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLDocumentStuff;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.semanlink.SLModel.Label2KeywordMatching;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.Util;

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




	try {
		
		// passons en mode "edit"
		
		// pour forcer à s'être d'abord identifier, si besoin est
		// Pas terrible : on revient ici, (comment ?), mais on a perdu
		// ce qu'on a pu saisir
		// normally done in Action_Bookmark (search /sl/about/LOGON.htm)
		// Ceinture et bretelle
		// Ou alors, on pourrait ici jeter si !edit
	  if (!Jsp_Page.isEditor(request)) {
	  	response.sendRedirect(response.encodeRedirectURL(Util.getContextURL(request)+"/sl/about/LOGON.htm"));
	  	return null;
	  }
	
		setEdit(request, true);
		
		Foo foo = new Foo(mapping, form, request, response);

		String redirectURL = null;
		if (request.getParameter("bookmark2tagBtn") != null) { 		// @find bookmark2tag
			
			//
			// CREATION OF A TAG FROM THE URI OF A BOOKMARK
			//
			
			redirectURL = bookmark2tag(foo, mapping, form, request, response);					

		} else {

			//
			// CREATION OF A BOOKMARK (OR LOCAL DOC) (if doesn't exist yet)
			//
			
			// 2019-03 URIS for bookmarks
			
			
			
			// Before uris for bookmarks:
			//
			// il y a plusieurs cas possibles de pré-existence de l'url du docOnLine dans le mdoèle, mais
			// on ne s'occupe pas de tous ici, ce qui se justifie
			// - par le fait qu'il sont traités dans Action_BookmarkForm 
			// (qui envoie directement sur la page, par ex du tag ql le doc est homopage du tag)
			// - et que ça permet de tout de même créer un doc (via "new bookmark" dans la barre de droite)
			// pour un tel cas si on y tient vraiment
			//
			// D'OU LE SIMPLE TEST existsAsSubject avant 2019-03
			
			// 2019-03 : on se préoccupe uniquement de l'existence du doc en tant que bookmark
			SLDocument bookmark2019 = foo.mod.bookmarkUrl2Doc(foo.docOnline.getURI());
			if (bookmark2019 != null) {
				
				foo.docToDisplay = bookmark2019;
				
			} else if (foo.mod.existsAsSubject(foo.docOnline)) { // true ssi doc intervient dans au moins un statement en tant que sujet

				// ALREADY EXISTS (en tant que doc)
				
				// - ne prend pas en compte le cas où c juste source d'un doc local, ou un truc pointé par un tag
				// MAIS CELA EST TRAITE AVANT, DS Action_BookmarkForm
				
				foo.docToDisplay = foo.docOnline;
				
			} else {
				
				// il faut encore faire gaffe au cas : document (pas encore créé) situé dans un datafolder existant
				// DocMetadataFile metadata = mod.doc2DocMetadataFile(docOnline.getURI());
				// Problématique à voir avec les assoc du webserver telles qu'elles sont (url en /document/ vs /doc/)
				// Contentons nous donc de voir si c'est une uri semanlink
				// WebServer ws = SLServlet.getWebServer();
				// if ((ws != null) && (ws.owns(docOnline.getURI()))) {
				if (foo.docOnline.getURI().startsWith(SLServlet.getServletUrl())) {
					
					// le sldoc n'existe pas, mais c'est (probablement) un fichier local dans au sein d'un SLDataFolder
					// Pose des pbs (si on passait ds le code plus bas) -> on ne crée pas le truc, on se contente de l'afficher
					
					foo.docToDisplay = foo.docOnline;

					
					
					
				} else {
					// document qui n'existe pas (au sens sl), et qui n'est pas un fichier dans un sous-dossier d'un SLDataFolder
					createBookmark(foo, mapping, form, request, response);	

					
				} // doc already exists or not
			}
		
			// POST REDIRECT
			// x = mapping.findForward("continue");
			redirectURL = Util.getContextURL(request) + HTML_Link.docLink(foo.docToDisplay.getURI());
		}



		response.sendRedirect(response.encodeRedirectURL(redirectURL));
		return null;
	} catch (Exception e) {
		return error(mapping, request, e );
	}
} // end execute



private void createBookmark(Foo foo, ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	// document qui n'existe pas (au sens sl), et qui n'est pas un fichier dans un sous-dossier d'un SLDataFolder
	
	String downloadFromUri = getDownloadFromUri(foo, request);
	
	boolean downloadRequested = ((request.getParameter("bookmarkWithCopyBtn") != null)
			|| (request.getParameter("copyBtn") != null));
	Response res = null;
	boolean isHTML = false;
	String dotExtension = null;
	if (downloadRequested) {
		res = Action_Download.getResponse(downloadFromUri);		
		isHTML = Action_Download.isHTML(downloadFromUri, res);
		// 2020-03 changed wo any testing
		// dotExtension = ".html"; // KWOI ???? TODO 
		if (isHTML) {
			dotExtension = ".html";
		} else {
			dotExtension = Util.getDotExtension(downloadFromUri);;
		}
	} else {
		dotExtension = Util.getDotExtension(downloadFromUri);
	}

			
	SLDocument doc = null; // local ou online, c'est selon.
	// Attention ceci n'est pas bon, car reste documenté d'un appel à l'autre
	// (ou alors il faudrait le mettre ces infos à null en fin de execute)
	// if (bookmarkForm.getBookmarkBtn() != null) {
	SLDocument localDoc_SourceToBeAdded = null; // sera !null ds les cas "bookmark avec copy" ou "local avec source" (et égal a localDoc)
	// si on souhaite stocker la source
	// (il faut prendre garde de ne pas créer en 1er le statement SOURCE_PROPERTY,
	// (qui a nécessité un truc spécial dans le "onNewDocument" du listener jena
	// pour ne pas entrainer la création des statements de new doc pour la source)
	// parce que sinon, il n'y a pas le traitement new doc 
	
	// 2019-03 uris for bookmarks
	// which (sl) uri for this new bookmark?
	// let's create it from title (as we were doing for files)
	if (foo.title == null) throw new RuntimeException("No title for doc");
	
	// Hum, marche pas parce qu'il puet y avoir des car à la con ds le nom fichier
	// et donc, on ne peut retrouver le nom du fichier à partir du localname du bkm
//SLDocument bkm = mod.newBookmark(title);	
//File saveAsDir = mod.goodDirToSaveAFile();				
//File saveAs = new File(saveAsDir, Util.getLocalName(bkm.getURI() + dotExtension));
//if (saveAs.exists()) throw new RuntimeException(saveAs + " already exists. Unexpected :-(");
	
	// 2019-05-11 ATTENTION, CECI N'IRAIT PAS POUR UN TRUC fichier ds une dir locale, autre peut-être que default folder				
	SLModel.NewBookmarkCreationData bkmData = new SLModel.NewBookmarkCreationData(foo.mod, foo.title); 
	
	SLDocument bkm = bkmData.getSLDocument();
	File saveAs = bkmData.getSaveAsFile(dotExtension);

	if (downloadRequested) {
		Action_Download.download(downloadFromUri, saveAs, false, res, isHTML);	
	}
	
	if (request.getParameter("bookmarkBtn") != null) {
		
		// clic on "Bookmark" btn
		
		// doc = docOnline;
		doc = bkm;
		
		foo.mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, foo.docOnline.getURI());
		foo.mod.setDocProperty(bkm, SLVocab.TITLE_PROPERTY, foo.title, foo.lang);
		if (foo.comment != null) foo.mod.setDocProperty(bkm, SLVocab.COMMENT_PROPERTY, foo.comment, foo.lang);

		
		
		
		// 2020-07
		SLDocumentStuff localCopyCandidate = Jsp_Document.localCopyCandidate(Util.getContextURL(request));
		if (localCopyCandidate != null) {
			foo.mod.addDocProperty(localCopyCandidate.getSLDocument(), SLVocab.SOURCE_PROPERTY, doc.getURI());
		}
		
		
		
		
	} else {
		
		// boolean overwrite = param2boolean("overwrite", request, false); // 2019-03 : this param never set to true, semble-t-il
		// File saveAs = Action_Download.downloadFile(downloadFromUri, title, overwrite, mod); // title sert à créer le nom du fichier

		String localUri = foo.mod.fileToUri(saveAs);
		SLDocument localDoc = foo.mod.getDocument(localUri);
		
		if (request.getParameter("bookmarkWithCopyBtn") != null) {
			// doc = docOnline;
			doc = bkm;
			
			localDoc_SourceToBeAdded = localDoc;

			foo.mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, foo.docOnline.getURI());
			foo.mod.setDocProperty(bkm, SLVocab.TITLE_PROPERTY, foo.title, foo.lang);
			if (foo.comment != null) foo.mod.setDocProperty(bkm, SLVocab.COMMENT_PROPERTY, foo.comment, foo.lang);
			// source : l'affecter à la vraie source (doconline) ou bien au bkm doc ???
			if (localDoc_SourceToBeAdded != null) {
				
				// 2020-03 Ne me parait pas une bonne idée de mettre le lien vers 2 sources
				
				// Par ailleurs, c'était une très mauvaise idée que d'utiliser dc:source (en réciproque) pour
				// définir la prop "local copy"
				// TODO CHANGE
				
//				// AH,MAIS ATTENTION !!!
//				// Supposons qu'on change le lien bookmarkOf
//				// La copie locale a un dc:source qui n'est plus lié au bkm
//				// et ne peut donc plus être retrouvée à partir de lui
//				
//				// essayons en mettant les 2
//				// ne doit pas gêner pour affichage du lien source sur docline bkm
//
//				// 1) à la source : la vraie source online ? -> localFile dc:source onlineUrl // avantage : lien 1 pour 1 au cas où on aurait plusiuers saved docs attachés au bkm
//				mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, docOnline.getURI());
//				// 2) ou bien le bkm ? -> localFile dc:source bkm // avantage : doit marcher sans modif du code pre2019
				foo.mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, bkm.getURI());		
											
			}

			
			
			
		} else if (request.getParameter("copyBtn") != null) {
			localDoc_SourceToBeAdded = localDoc;
			// ne pas faire ça maintenant, sinon on perd les ajouts de metadata
			// on new doc, becoz of un traitement spécial pour la prop source (voir listeenr
			// avec enw doc)
			// mod.addDocProperty(addSourceTo, SLVocab.SOURCE_PROPERTY, docuri);

			// CREER OU PAS UN "BKM" ???
			// oui -> on aurait aussi pour les docs locaux la possibilité de changer l'uri du doc pointé
			// 			  mais : à la fois uri /doc/... et /document/... : on va se planter
			// non -> séparation doc pointant vers le online et locaux
			
			// DISONS NON : 
			doc = localDoc;
			// doc = bkm;
			
			// mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, localDoc.getURI()); // DISONS NON
			foo.mod.setDocProperty(doc, SLVocab.TITLE_PROPERTY, foo.title, foo.lang);
			if (foo.comment != null) foo.mod.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, foo.comment, foo.lang);
			if (localDoc_SourceToBeAdded != null) {
				// la source : affectée au doc local 
				foo.mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, foo.docOnline.getURI());
				// ou bien au bkm ? bkm source doconline // BEN COMME ON A DIT NON
				// mod.addDocProperty(bkm, SLVocab.SOURCE_PROPERTY, docOnline.getURI());
			}

		}
	}

	foo.mod.onNewDoc(doc);

	// 2007-01 (POST REDIRECT)
	// getJsp_Document(doc, request); // documente l'attribut jsp de la request
	foo.docToDisplay = doc;

}

private String getDownloadFromUri(Foo foo, HttpServletRequest request) throws URISyntaxException {
	String downloadFromUri = request.getParameter("downloadfromuri"); // pas de decode : issu d'un champ de saisie, non code (?)
	if ((downloadFromUri == null) || ("".equals(downloadFromUri))) {						
		downloadFromUri = foo.docuri;
		
	} else {
		downloadFromUri = SLUtils.laxistUri2Uri(downloadFromUri);	    	
		String errMess = JenaUtils.getUriViolations(downloadFromUri,false);
		if (errMess != null) {
			throw new RuntimeException(errMess);
		}
	}

	// 2020-03 arxiv
	String arxivPdf = Arxiv.url2pdfUrl(downloadFromUri);
	if (arxivPdf != null) {
		downloadFromUri = arxivPdf;
	}
	return downloadFromUri;
}



// return the redirectUrl
String bookmark2tag(Foo foo, ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	//
	// CREATION OF A TAG FROM THE URI OF A BOOKMARK
	//
	
	// 2020-04 comment: situation is, we've just clicked "create new tag" on the bookmark form
	// (and things are ok until here: if page already bookmarked, no bookmark form)
	// If title entered on bookmark form is already used as label
	// for a tag,
	// if used only for on tag, we are redirected to tag'spage - with the new link to decription added - not too bad)
	// if used for several tags, redirected to first one, with no change to tag (should be improved)
	
	String kwLabel = foo.title;
	if (kwLabel == null) throw new RuntimeException("No label for tag");
	Locale locale = null;
	if (foo.lang != null) {
		locale = new Locale(foo.lang);
	}
	
	String thesaurusUri = foo.mod.getDefaultThesaurus().getURI();
	Label2KeywordMatching match = foo.mod.label2KeywordMatch(kwLabel, thesaurusUri, locale);
	
	SLKeyword[] kws = match.existingMatches();

	String kwUri = null;
	if (kws.length == 0) {
		// alreadyExisted = false;
		
		// kwLabel doesn't match any existing tag
		// try to create one. 
		
		SLKeyword kw = match.create();
		if (kw != null) { // created
			kwUri = kw.getURI();
			foo.mod.addKwProperty(kwUri, SLVocab.SL_DESCRIBED_BY_PROPERTY, foo.docOnline.getURI());
			if (foo.comment != null) foo.mod.setKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, foo.comment, foo.lang); 		
		} else {
			// what would have been the uri of the created kw, but that already exixts
			// just to go to it
			// we should give a alert // TODO
			kwUri = match.label2Uri();
		}
	} else {
		// already existed
		if (kws.length == 1) {
			// a kw corresponding to label already existed, and there is only one
			// Let's add the SL_DESCRIBED_BY_PROPERTY to it
			// (typical case: create kw from a wikipedia page, and there were already such a kw:
			// let's link it to wikipedia)
			kwUri = kws[0].getURI();
			foo.mod.addKwProperty(kwUri, SLVocab.SL_DESCRIBED_BY_PROPERTY, foo.docOnline.getURI());
			// add, pas set seulement // BOF TODO
			if (foo.comment != null) foo.mod.addKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, foo.comment, foo.lang); 

		} else {
			// BOF, should go to a page with a list of 2 kws? And then?
			// TODO CHANGE
			// for now, we go to the first of the kws and do nothing (?)
			// TODO WE SHOULD AT LEAST GIVE A MESSAGE
			kwUri = kws[0].getURI();

		}				
	}
	return HTML_Link.getTagURL(Util.getContextURL(request), kwUri, false, ".html");						
}


static class Foo {
	String redirectURL = null;
	SLModel mod;
	Form_Bookmark bookmarkForm;
	String title;
	String comment;
	String lang;
	String docuri;
	SLDocument docOnline;
	SLDocument docToDisplay;
	
	Foo(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws URISyntaxException {
		mod = SLServlet.getSLModel();
		this.bookmarkForm = (Form_Bookmark) form;
		title = bookmarkForm.getTitle().trim();
		if ("".equals(title)) title = null;
		lang = bookmarkForm.getLang();
		if (lang != null) lang = lang.trim();
		if ("".equals(lang)) lang = null;
		comment = bookmarkForm.getComment().trim();
		if ("".equals(comment)) comment = null;

		docToDisplay = null; // doc to be displayed
		docuri = request.getParameter("docuri"); // pas de decode : issu d'un champ de saisie, non code (?)
		if ((docuri == null) || ("".equals(docuri))) {
			throw new RuntimeException("No document's URI");
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
		String errMess = JenaUtils.getUriViolations(docuri,false);
		if (errMess != null) {
			throw new RuntimeException(errMess);
		}

		docOnline = mod.smarterGetDocument(docuri);
	}
}


//
//
//

//public ActionForward execute_SVG(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
//	// ActionForward x = null;
//
//
//
//
//	try {
//		String redirectURL = null;
//		SLModel mod = SLServlet.getSLModel();
//		
//		
//		
//		
//		
//		
//		
//		// passons en mode "edit"
//		
//		// pour forcer à s'être d'abord identifier, si besoin est
//		// Pas terrible : on revient ici, (comment ?), mais on a perdu
//		// ce qu'on a pu saisir
//		// normally done in Action_Bookmark (search /sl/about/LOGON.htm)
//		// Ceinture et bretelle
//		// Ou alors, on pourrait ici jeter si !edit
//	  if (!Jsp_Page.isEditor(request)) {
//	  	response.sendRedirect(response.encodeRedirectURL(Util.getContextURL(request)+"/sl/about/LOGON.htm"));
//	  	return null;
//	  }
//	
//		setEdit(request, true);
//		
//		
//		
//		
//		
//		
//		
//		
//	
//		Form_Bookmark bookmarkForm = (Form_Bookmark) form;
//		String title = bookmarkForm.getTitle().trim();
//		if ("".equals(title)) title = null;
//		String lang = bookmarkForm.getLang();
//		if (lang != null) lang = lang.trim();
//		if ("".equals(lang)) lang = null;
//		String comment = bookmarkForm.getComment().trim();
//		if ("".equals(comment)) comment = null;
//
//
//
//
//
//
//		SLDocument docToDisplay = null; // doc to be displayed
//		String docuri = request.getParameter("docuri"); // pas de decode : issu d'un champ de saisie, non code (?)
//		if ((docuri == null) || ("".equals(docuri))) {
//			return error(mapping, request, "No document's URI");
//		}
//
//		// docuri peut venir 
//		// 1) de la bookmarklet - auquel cas, elle a été URLDecoder.decodée, et se présente par ex
//		// sous la forme http://a/b/un exemple éé.html
//		// 2) d'un paste de uri récoltée sur une de nos pages (ou, au moins avant, d'une valeur prédoc
//		// à l'uri d'une de nos pages) - auquel cas elle est déjà toute bien comme il faut, avec
//		// des %20 et des %C3
//		// 3) d'une saisie -- auquel cas elle peut être n'importe quoi,
//		// y compris complétement fause
//		// Pour remettre d'équerre ce qui viendrait du cas 1, et refuser les erreurs du cas 3,
//		// on fait la chose suivante (cf Test.testUriSmall)
//		docuri = SLUtils.laxistUri2Uri(docuri);
//		String errMess = JenaUtils.getUriViolations(docuri,false);
//		if (errMess != null) {
//			throw new RuntimeException(errMess);
//		}
//
//		SLDocument docOnline = mod.smarterGetDocument(docuri);
//
//		if (request.getParameter("bookmark2tagBtn") != null) { 		// @find bookmark2tag
//			
//			//
//			// CREATION OF A TAG FROM THE URI OF A BOOKMARK
//			//
//			
//			// 2020-04 comment: situation is, we've just clicked "create new tag" on the bookmark form
//			// (and things are ok until here: if page already bookmarked, no bookmark form)
//			// If title entered on bookmark form is already used as label
//			// for a tag,
//			// if used only for on tag, we are redirected to tag'spage - with the new link to decription added - not too bad)
//			// if used for several tags, redirected to first one, with no change to tag (should be improved)
//			
//			String kwLabel = title;
//			if (kwLabel == null) return error(mapping, request, "No label for tag");
//			Locale locale = null;
//			if (lang != null) {
//				locale = new Locale(lang);
//			}
//			
//			String thesaurusUri = mod.getDefaultThesaurus().getURI();
//			Label2KeywordMatching match = mod.label2KeywordMatch(kwLabel, thesaurusUri, locale);
//			
//			
//			SLKeyword[] kws = match.existingMatches();
//			
//			
//			
//			
//			String kwUri = null;
//			if (kws.length == 0) {
//				// alreadyExisted = false;
//				
//				// kwLabel doesn't match any existing tag
//				// try to create one. 
//				
//				SLKeyword kw = match.create();
//				if (kw != null) { // created
//					kwUri = kw.getURI();
//					mod.addKwProperty(kwUri, SLVocab.SL_DESCRIBED_BY_PROPERTY, docOnline.getURI());
//					if (comment != null) mod.setKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, comment, lang); 		
//				} else {
//					// what would have been the uri of the created kw, but that already exixts
//					// just to go to it
//					// we should give a alert // TODO
//					kwUri = match.label2Uri();
//				}
//			} else {
//				// already existed
//				if (kws.length == 1) {
//					// a kw corresponding to label already existed, and there is only one
//					// Let's add the SL_DESCRIBED_BY_PROPERTY to it
//					// (typical case: create kw from a wikipedia page, and there were already such a kw:
//					// let's link it to wikipedia)
//					kwUri = kws[0].getURI();
//					mod.addKwProperty(kwUri, SLVocab.SL_DESCRIBED_BY_PROPERTY, docOnline.getURI());
//					// add, pas set seulement // BOF TODO
//					if (comment != null) mod.addKwProperty(kwUri, SLVocab.COMMENT_PROPERTY, comment, lang); 
//
//				} else {
//					// BOF, should go to a page with a list of 2 kws? And then?
//					// TODO CHANGE
//					// for now, we go to the first of the kws and do nothing (?)
//					// TODO WE SHOULD AT LEAST GIVE A MESSAGE
//					kwUri = kws[0].getURI();
//
//				}				
//			}
//			redirectURL = HTML_Link.getTagURL(Util.getContextURL(request), kwUri, false, ".html");						
//
//		} else {
//			
//			//
//			// CREATION OF A BOOKMARK (OR LOCAL DOC) (if doesn't exist yet)
//			//
//			
//			// 2019-03 URIS for bookmarks
//			
//			
//			
//			// Before uris for bookmarks:
//			//
//			// il y a plusieurs cas possibles de pré-existence de l'url du docOnLine dans le mdoèle, mais
//			// on ne s'occupe pas de tous ici, ce qui se justifie
//			// - par le fait qu'il sont traités dans Action_BookmarkForm 
//			// (qui envoie directement sur la page, par ex du tag ql le doc est homopage du tag)
//			// - et que ça permet de tout de même créer un doc (via "new bookmark" dans la barre de droite)
//			// pour un tel cas si on y tient vraiment
//			//
//			// D'OU LE SIMPLE TEST existsAsSubject avant 2019-03
//			
//			// 2019-03 : on se préoccupe uniquement de l'existence du doc en tant que bookmark
//			SLDocument bookmark2019 = mod.bookmarkUrl2Doc(docOnline.getURI());
//			if (bookmark2019 != null) {
//				
//				docToDisplay = bookmark2019;
//				
//			} else if (mod.existsAsSubject(docOnline)) { // true ssi doc intervient dans au moins un statement en tant que sujet
//
//				// ALREADY EXISTS (en tant que doc)
//				
//				// - ne prend pas en compte le cas où c juste source d'un doc local, ou un truc pointé par un tag
//				// MAIS CELA EST TRAITE AVANT, DS Action_BookmarkForm
//				
//				docToDisplay = docOnline;
//				
//			} else {
//					
//				// il faut encore faire gaffe au cas : document (pas encore créé) situé dans un datafolder existant
//				// DocMetadataFile metadata = mod.doc2DocMetadataFile(docOnline.getURI());
//				// Problématique à voir avec les assoc du webserver telles qu'elles sont (url en /document/ vs /doc/)
//				// Contentons nous donc de voir si c'est une uri semanlink
//				// WebServer ws = SLServlet.getWebServer();
//				// if ((ws != null) && (ws.owns(docOnline.getURI()))) {
//				if (docOnline.getURI().startsWith(SLServlet.getServletUrl())) {
//					
//					// le sldoc n'existe pas, mais c'est (probablement) un fichier local dans au sein d'un SLDataFolder
//					// Pose des pbs (si on passait ds le code plus bas) -> on ne crée pas le truc, on se contente de l'afficher
//					
//					docToDisplay = docOnline;
//					
//				} else {
//					// document qui n'existe pas (au sens sl), et qui n'est pas un fichier dans un sous-dossier d'un SLDataFolder
//
//					String downloadFromUri = request.getParameter("downloadfromuri"); // pas de decode : issu d'un champ de saisie, non code (?)
//					if ((downloadFromUri == null) || ("".equals(downloadFromUri))) {						
//						downloadFromUri = docuri;
//						
//					} else {
//						downloadFromUri = SLUtils.laxistUri2Uri(downloadFromUri);	    	
//		  			errMess = JenaUtils.getUriViolations(downloadFromUri,false);
//		  			if (errMess != null) {
//		  				throw new RuntimeException(errMess);
//		  			}
//					}
//
//					// 2020-03 arxiv
//					String arxivPdf = Arxiv.url2pdfUrl(downloadFromUri);
//					if (arxivPdf != null) {
//						downloadFromUri = arxivPdf;
//					}
//					
//					boolean downloadRequested = ((request.getParameter("bookmarkWithCopyBtn") != null)
//							|| (request.getParameter("copyBtn") != null));
//					Response res = null;
//					boolean isHTML = false;
//					String dotExtension = null;
//					if (downloadRequested) {
//						res = Action_Download.getResponse(downloadFromUri);		
//						isHTML = Action_Download.isHTML(downloadFromUri, res);
//						// 2020-03 changed wo any testing
//						// dotExtension = ".html"; // KWOI ???? TODO 
//						if (isHTML) {
//							dotExtension = ".html";
//						} else {
//							dotExtension = Util.getDotExtension(downloadFromUri);;
//						}
//					} else {
//						dotExtension = Util.getDotExtension(downloadFromUri);
//					}
//	
//							
//					SLDocument doc = null; // local ou online, c'est selon.
//					// Attention ceci n'est pas bon, car reste documenté d'un appel à l'autre
//					// (ou alors il faudrait le mettre ces infos à null en fin de execute)
//					// if (bookmarkForm.getBookmarkBtn() != null) {
//					SLDocument localDoc_SourceToBeAdded = null; // sera !null ds les cas "bookmark avec copy" ou "local avec source" (et égal a localDoc)
//					// si on souhaite stocker la source
//					// (il faut prendre garde de ne pas créer en 1er le statement SOURCE_PROPERTY,
//					// (qui a nécessité un truc spécial dans le "onNewDocument" du listener jena
//					// pour ne pas entrainer la création des statements de new doc pour la source)
//					// parce que sinon, il n'y a pas le traitement new doc 
//					
//					// 2019-03 uris for bookmarks
//					// which (sl) uri for this new bookmark?
//					// let's create it from title (as we were doing for files)
//					if (title == null) return error(mapping, request, "No title for doc");
//					
//					// Hum, marche pas parce qu'il puet y avoir des car à la con ds le nom fichier
//					// et donc, on ne peut retrouver le nom du fichier à partir du localname du bkm
////				SLDocument bkm = mod.newBookmark(title);	
////				File saveAsDir = mod.goodDirToSaveAFile();				
////				File saveAs = new File(saveAsDir, Util.getLocalName(bkm.getURI() + dotExtension));
////				if (saveAs.exists()) throw new RuntimeException(saveAs + " already exists. Unexpected :-(");
//					
//					// 2019-05-11 ATTENTION, CECI N'IRAIT PAS POUR UN TRUC fichier ds une dir locale, autre peut-être que default folder				
//					SLModel.NewBookmarkCreationData bkmData = new SLModel.NewBookmarkCreationData(mod, title); 
//					
//					SLDocument bkm = bkmData.getSLDocument();
//					File saveAs = bkmData.getSaveAsFile(dotExtension);
//	
//					if (downloadRequested) {
//						Action_Download.download(downloadFromUri, saveAs, false, res, isHTML);	
//					}
//					
//					if (request.getParameter("bookmarkBtn") != null) {
//						
//						// clic on "Bookmark" btn
//						
//						// doc = docOnline;
//						doc = bkm;
//						
//						mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, docOnline.getURI());
//						mod.setDocProperty(bkm, SLVocab.TITLE_PROPERTY, title, lang);
//						if (comment != null) mod.setDocProperty(bkm, SLVocab.COMMENT_PROPERTY, comment, lang);
//		
//						
//					} else {
//						
//						// boolean overwrite = param2boolean("overwrite", request, false); // 2019-03 : this param never set to true, semble-t-il
//						// File saveAs = Action_Download.downloadFile(downloadFromUri, title, overwrite, mod); // title sert à créer le nom du fichier
//	
//						String localUri = mod.fileToUri(saveAs);
//						SLDocument localDoc = mod.getDocument(localUri);
//						
//						if (request.getParameter("bookmarkWithCopyBtn") != null) {
//							// doc = docOnline;
//							doc = bkm;
//							
//							localDoc_SourceToBeAdded = localDoc;
//	
//							mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, docOnline.getURI());
//							mod.setDocProperty(bkm, SLVocab.TITLE_PROPERTY, title, lang);
//							if (comment != null) mod.setDocProperty(bkm, SLVocab.COMMENT_PROPERTY, comment, lang);
//							// source : l'affecter à la vraie source (doconline) ou bien au bkm doc ???
//							if (localDoc_SourceToBeAdded != null) {
//								
//								// 2020-03 Ne me parait pas une bonne idée de mettre le lien vers 2 sources
//								
//								// Par ailleurs, c'était une très mauvaise idée que d'utiliser dc:source (en réciproque) pour
//								// définir la prop "local copy"
//								// TODO CHANGE
//								
////								// AH,MAIS ATTENTION !!!
////								// Supposons qu'on change le lien bookmarkOf
////								// La copie locale a un dc:source qui n'est plus lié au bkm
////								// et ne peut donc plus être retrouvée à partir de lui
////								
////								// essayons en mettant les 2
////								// ne doit pas gêner pour affichage du lien source sur docline bkm
////	
////								// 1) à la source : la vraie source online ? -> localFile dc:source onlineUrl // avantage : lien 1 pour 1 au cas où on aurait plusiuers saved docs attachés au bkm
////								mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, docOnline.getURI());
////								// 2) ou bien le bkm ? -> localFile dc:source bkm // avantage : doit marcher sans modif du code pre2019
//								mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, bkm.getURI());		
//															
//							}
//	
//							
//							
//							
//						} else if (request.getParameter("copyBtn") != null) {
//							localDoc_SourceToBeAdded = localDoc;
//							// ne pas faire ça maintenant, sinon on perd les ajouts de metadata
//							// on new doc, becoz of un traitement spécial pour la prop source (voir listeenr
//							// avec enw doc)
//							// mod.addDocProperty(addSourceTo, SLVocab.SOURCE_PROPERTY, docuri);
//	
//							// CREER OU PAS UN "BKM" ???
//							// oui -> on aurait aussi pour les docs locaux la possibilité de changer l'uri du doc pointé
//							// 			  mais : à la fois uri /doc/... et /document/... : on va se planter
//							// non -> séparation doc pointant vers le online et locaux
//							
//							// DISONS NON : 
//							doc = localDoc;
//							// doc = bkm;
//							
//							// mod.setDocProperty(bkm, SLVocab.SL_BOOKMARK_OF_PROPERTY, localDoc.getURI()); // DISONS NON
//							mod.setDocProperty(doc, SLVocab.TITLE_PROPERTY, title, lang);
//							if (comment != null) mod.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, comment, lang);
//							if (localDoc_SourceToBeAdded != null) {
//								// la source : affectée au doc local 
//								mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, docOnline.getURI());
//								// ou bien au bkm ? bkm source doconline // BEN COMME ON A DIT NON
//								// mod.addDocProperty(bkm, SLVocab.SOURCE_PROPERTY, docOnline.getURI());
//							}
//	
//						}
//					}
//	
//					// b4 2019-03
//	//				mod.setDocProperty(doc, SLVocab.TITLE_PROPERTY, title, lang);
//	//				if (comment != null) mod.setDocProperty(doc, SLVocab.COMMENT_PROPERTY, comment, lang);
//	//				if (localDoc_SourceToBeAdded != null) {
//	//					mod.addDocProperty(localDoc_SourceToBeAdded, SLVocab.SOURCE_PROPERTY, docuri);
//	//				}
//	
//					
//					mod.onNewDoc(doc);
//	
//					// 2007-01 (POST REDIRECT)
//					// getJsp_Document(doc, request); // documente l'attribut jsp de la request
//					docToDisplay = doc;
//				} // doc already exists or not
//			}
//		
//			// POST REDIRECT
//			// x = mapping.findForward("continue");
//			redirectURL = Util.getContextURL(request) + HTML_Link.docLink(docToDisplay.getURI());
//		}
//
//
//
//		response.sendRedirect(response.encodeRedirectURL(redirectURL));
//		return null;
//	} catch (Exception e) {
//		return error(mapping, request, e );
//	}
//} // end execute

} // end Action
