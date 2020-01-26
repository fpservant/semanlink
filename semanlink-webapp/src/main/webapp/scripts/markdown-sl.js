// ATTENTION suppose définie la fct getContextURL() (qui retourne par ex http://127.0.0.1:7080/semanlink)/** * Display a markdown document (identified by uri), * within the div id "md", * replacing relative links (supposed to be relative to uri)  * @param uri uri of the displayed file * @param requestURL url of the request, if we want to make relative links relative to it it rather than relative to uri  * @param hrefOpenInDesktop only useful when we use the title in doc as title in sl's page, to have the ability to pass the url to open file in desktop * */function displayMarkdownDocument(uri, requestURL, hrefOpenInDesktop) {	var baseUri = uri;	var displayMd = function(mdText) {		var md = window.markdownit({			html: true,			replaceLink: replaceLinkFct(baseUri, requestURL)		});								// 2019-09: hide sl title if the markdown contains one		if (mdText.startsWith("# ")) {			document.getElementById("doctitle").style.display = "none";						// but we don't want to lose the ability to open the doc in desktop			if (hrefOpenInDesktop) {				mdText = replaceAll(mdText, "\r\n", "\n");				var k = mdText.indexOf("\n");				if (k > -1) {					mdText = "# [" + mdText.substring(2,k) + "](" + hrefOpenInDesktop + ")" + mdText.substring(k);				}							}								}		md.use(window.markdownitReplaceLink);		var uslugify = function(s) {			return window.uslug(s);		}		md.use(window.markdownItAnchor, { /*permalink: true, permalinkBefore: true, permalinkSymbol: '§',*/ slugify: uslugify/*, level: 2*/ });		md.use(window.markdownItTocDoneRight, { slugify: uslugify, level: 1 });		document.getElementById("md").innerHTML = md.render(mdText);			};	dollarGet(uri, displayMd);}/** * Return the function used by markdownit's ReplaceLink plug in  * to compute how to replace a link inside md document * @param baseUri the uri relative links are relative to.  * @param requestURL because for .md files, we want to have them relative to /doc (en fait, un moyen de passer http;//.../doc à la méthode) * baseUri: base for docs supposed to be served by themselves while requestURL is the base for files supposed to be served by semanlink */// NOT OK for folders... (au moins si no webserver) qu'y faire ?// si pas terminé par "/"// par ex dans un doc md// [scikit related](../../2015/10/scikit_python_notes/)(include sample code to read from csv file, training, train set/test set)// donne // http://127.0.0.1:7080/semanlink/document/2015/10/scikit_python_notes// LE PROBLEME, c'est avec les fichers md semanlink qui contiennent des url relatives (pour pouvoir// focntionner en local - ex : des images ds un sous-dossier /ims - MAIS surtout// avec les liens vers du md exprimés en relatif : il faut essayer de retourner un lien en /doc, sinon doc?uri=function replaceLinkFct(baseUri, requestURL) {	// ne pourrait-t-on pas utiliser alert(window.location.href) comme baseUri ??? -- au moins qd elle	// contient /sl/doc/ // TODO A VOIR	// console.log("replaceLinkFct baseUri " + baseUri +"\nrequestURL " + requestURL);		// 2019-09 var k = baseUri.lastIndexOf("/");	// 2019-09 var baseslash = baseUri.substring(0,k+1); // http://127.0.0.1/~fps/fps/2015/09/		return function (link, env) {		// link, eg.: ../../2015/04/unFichier.md		// if link is absolute, return it		var x = link;		// var isAbsolute = (new URL(document.baseURI).origin === new URL(link, document.baseURI).origin);		// var isAbsolute = (link.startsWith("http"))		var isAbsolute = (link.indexOf(':') > -1) && (link.indexOf(':') < 10); // link contains a protocol // ouais, on doit pouvoir faire mieux comme test		if (isAbsolute) {				// cas lien externe : ne rien faire			// cas lien vers semanlink en dur (parce que ds un fichier md dont on veut qu'il marche): on voudrait éventuellement passer à la vraie url semanlink ex 127 -> semanlink.net BOF					} else if (link.startsWith("/")) {						// 2017-10 			// such a link can only be considered absolute wrt to the servlet			// (absolute links inside comments **MUST** be considered as absolute wrt to the servlet)			// (ouais sauf que si on a copié-collé /semanlink/tag/toto, c pas bon)						if (link.startsWith("/semanlink")) {				// 2019-09 if it is /semanlink, consider it is absolute (~)				// (getContextURL() probably already contains /semanlink)				x = getContextURL() + x.substring(10);			} else {				x = getContextURL() + x;							}						//			//			// this won't work, but I don't know what could be done.//			// Make it relative to webserver root? to doc folder?//			// Also, we have the case of docs on the disk, such as /Users/fps/adoc//			//			// cf below: will work if /sl/doc or (see elsewhere, if /document or /semanlink) -- HMM SEE WHERE ?					} else {			// relative link 			// eg.: ../../2015/04/unFichier.md			// or ims/img.jpg									var useBase = true;			if (requestURL) {				// if .md (or dir), then link should be considered as relative to requestURL (which is a .md)				// (eg. relative link to a .md file must be in /doc, not /document				if ((x.endsWith(".md")) || (x.indexOf("#") > -1) || (x.endsWith("/"))) {					useBase = false;				}							}			if (useBase) {				// x = relative2absolute(baseslash, link); // HUM HUM				x = relative2absolute(baseUri, link); // 2019-09			} else {				x = relative2absolute(requestURL, link);			}					}		// console.log("x1: " + x + " was link: " + link);				//		// x is now absolute		//				if (!x.startsWith(getContextURL())) {			// don't change it		} else {			// var k = x.indexOf("/sl/doc/"); // TODO ATTENTION adherence !!! // 2019-05 : HUM TODO CHECK			var k = x.indexOf("/doc/"); // TODO ATTENTION adherence !!! // 2019-05 : HUM TODO CHECK			if (k < 0) k = x.indexOf("/tag/");			// en toute logique, on devrait decommenter ligne suivante, 			// mais alors, pour un dossier, on retourne "xxx/document/2017/04/", 			// (on ne passe pas ds doc/?uri= et ca ne va pas (/document/ unsupported pour dossiers)			if (k < 0) k = x.indexOf("/document/"); // TODO ATTENTION adherence StaticFileServlet.PATH			if (k >= 0) { // new 2017-09-19				if (getContextURL() + x.substring(k) != x) {					// console.log("BEN SI, UTILE " + getContextURL() + x.substring(k) + " -> " + x);				}								x = getContextURL() + x.substring(k); 							} else {				// for .md and folders, we would like to serve them at /doc				// This is better than nothing				// (assuming it's a doc)				if ((x.endsWith(".md")) || (x.indexOf(".md#") > -1) || (x.endsWith("/"))) {  // this probably implies that it is a doc							// alert("x : " + x + " - getContextURL() : " + getContextURL());					if (x.startsWith(getContextURL())) { // uniquement si local (ou webserver, il faudrait, mais ne sait pas faire)												// @find doc2markdownHref -- ATTENTION adherence !!!								x = getContextURL() + "/doc/?uri=" + encodeURIComponent(x); // je ne sais pas faire mieux : comment couper l'url au bon endroit ?						// mais en a-t-on encore besoin ? est-ce que ça arrive encore (légitimement) ?						// ben oui : dans les fichiers md qui utilisent des url relatives (afin de focntionner						// aussi en standalone)						// console.log("OUAICHE  " + x);					}									}				}		}		// console.log("xF " + x);		return x;	};}/** * Convert a relative url to an absolute one * Beware, relative supposed to be... relative  * see https://stackoverflow.com/questions/14780350/convert-relative-path-to-absolute-using-javascript */function relative2absolute(base, relative) {	// 2019-09	if (relative.startsWith("#")) {		var k = base.lastIndexOf("#");		if (k > -1) {			base = base.substring(0,k)		}		return base + relative;	}    var stack = base.split("/"),        parts = relative.split("/");    stack.pop(); // remove current file name (or empty string)                 // (omit if "base" is the current folder without trailing slash)    for (var i=0; i<parts.length; i++) {        if (parts[i] == ".")            continue;        if (parts[i] == "..")            stack.pop();        else            stack.push(parts[i]);    }    return stack.join("/");}function displayRawMarkdown(uri) {	var displayRawMd = function(mdText) {		document.getElementById("rawmd").value = mdText;	};	dollarGet(uri, displayRawMd);}// in edit mode: display styled and raw markdownfunction displayEditMarkdown(uri) {	var displayBothMd = function(mdText) {		document.getElementById("rawmd").value = mdText;		var md = window.markdownit({			replaceLink: replaceLinkFct(uri)		});		md.use(window.markdownitReplaceLink);		document.getElementById("md").innerHTML = md.render(mdText);	};		// pour être sûr qu'on recharge vraiment les données à partir du serveur (?)	// ca a pas l'air de marcher	// safari Failed to load resource: "Request header field Cache-Control" is not allowed by Access-Control-Allow-Headers	// firefox locage d’une requête multiorigines (Cross-Origin Request) : la politique « Same Origin » ne permet pas de consulter la ressource distante située sur http://127.0.0.1/~fps/fps/2017/04/ElasticSearch_Renault.md. Raison : jeton « cache-control » manquant dans l’en-tête CORS « Access-Control-Allow-Headers » du canal de pré-vérification des requêtes CORS.	// dollarGet(uri, displayBothMd, true);	// alert("displayEditMarkdown " + uri);	dollarGet(uri, displayBothMd);}///////** http get  * @param forceReload to avoid taking result in cache */ // ca a pas l'air de marcher// function dollarGet(uri, success, forceReload) {function dollarGet(uri, success) {	var xhr = new XMLHttpRequest();	xhr.open('GET', uri);	xhr.onload = function() {		if (xhr.status === 200) {			if (success) {				success(xhr.responseText);			}    	} else {			alert('Request failed.  Returned status: ' + xhr.status);		}	};//	if (forceReload === true) {//		xhr.setRequestHeader("Cache-Control", "max-age=0");//	}	xhr.send();}function invalidateCache(url) {	var xhr = new XMLHttpRequest();	xhr.open("GET", url, true);	xhr.setRequestHeader("Cache-Control", "max-age=0");	xhr.send();}// ////function Editor(input, preview) {	this.update = function () {		var md = window.markdownit();		var result = md.render(input.value);    		preview.innerHTML = result;	};	input.editor = this;	this.update();};//// SL:COMMENT FIELD//function _getMarkdownit4Comments() {	var baseUri = window.location.href;	var md = window.markdownit({		replaceLink: replaceLinkFct(baseUri)	});	md.use(window.markdownitReplaceLink);	return md;}/** to display sl:comment, converting markdown to html */// (so, not to be used when editing the comment)// la jsp met le texte du comment brut dans la page (dans un textarea).// Ici, on le reprend, le convertit en html si c'est du md // (ça peut être aussi du html), et on met le html comme innerHTML du container// (à la place du text area)//// 2018-09: there is a bug : ca ne marche pas pour les docs dans un arbre de kws// (parce que on ne doit pas appeler ceci) (mais ça marche sur expanded tree ???)function displayCommentAsMarkdown() {	var md = false;	var commentDiv = document.getElementById("slcomment");	if (commentDiv) {		md = _getMarkdownit4Comments();		_handleComment(commentDiv, md, false);	}		var commentsAboutDocs = document.getElementsByClassName("docline_comment");	if (commentsAboutDocs) {		if (!md) md = _getMarkdownit4Comments();		for (var i = 0 ; i < commentsAboutDocs.length ; i++) {			_handleComment(commentsAboutDocs[i], md, false);		}		}}function isHtmlComment(comment) {	if (!comment) return false;	if (!(comment.indexOf("<") > -1)) return false;	return ((comment.indexOf("<br") > -1)	   || (comment.indexOf("<ul>") > -1)	   || (comment.indexOf("<a href") > -1)	   || (comment.indexOf("</object>") > -1));}// if text in commentContainer is markdown, replace it by formatted markdown function _handleComment(commentContainer, md, boolean_inline) {				if (!commentContainer) {		return;	}	if (!commentContainer.children) {		return;	}	if (!commentContainer.children[0]) {		return;	}		var commentAsText = commentContainer.children[0].value;					if (!commentAsText) {		return;	}	if (isHtmlComment(commentAsText)) {		// probably it's html, don't change		// - that is, just replace textarea by its content		commentContainer.innerHTML = commentAsText;		return;	}		if (boolean_inline) {		commentContainer.innerHTML = md.renderInline(commentAsText);	} else {		commentContainer.innerHTML = md.render(commentAsText);			}}function replaceAll(str, find, replace) {    return str.replace(new RegExp(find, 'g'), replace);}//////// just a quick hack to open a file in desktop// uri: see docStuff.getLocalCopyLinkfunction desktop_open_hack(uri) {	dollarGet(uri, false);}