// sur une base de longwell pour le fonctionnement des arbres// avec du XMLHttpRequest pour son chargement.// suppose definie la fct liveTreeLoadSubTree() qui retourne quelque chose genre :// "http://127.0.0.1:8080/semanlink/loadsubtree.do" @see template.jsp// author fps// sert a avoir la bonne image (ouvert/ferme)function makeConsistent(id,element) {	var trigger = document.getElementById("trigger:" + id);	if (trigger) {	    if ((element.style.display != "none") && (trigger.src.indexOf('closed') > -1)) {	        trigger.src = trigger.src.replace(/closed/gi,"open");	    } else if ((element.style.display == "none") && (trigger.src.indexOf('open') > -1)) {	        trigger.src = trigger.src.replace(/open/gi,"closed");	    }	}}function toggle(id) {    var block = document.getElementById("block:" + id);	if (block.innerHTML == "") {		// alert("load the subtree");		// block.innerHTML = "<li>aaaa</li><li>bbb</b>";		// block.innerHTML = retrieveURL(urlToRetrieve.value)	} else {		with (block.style) {			if (display=="none") {				display="block";			} else {				display="none";			}		}	}    // sert a avoir la bonne image (ouvert/ferme)	makeConsistent(id,block);}// pour un livetree// param id par ex 1_1_2// param encodedId l'uri du kw, encodee// postTagOnClick true si le href doit avoir un onclick avec un post pour cas du pulldown du livesearch en edit=true// targetUri : 2020-02 TagAndTagfunction toggle2(id, encodedId, withDocs, postTagOnClick, targetUri) {    var block = document.getElementById("block:" + id);	if (!block) { // the subtree has not been downloaded yet		// alert("load the subtree" + encodedId);		// ci-dessous : le & devrait etre encode, non ?		var arg = liveTreeLoadSubTree() + "?kwuri=" + encodedId +'&divid=' + id;		if (withDocs == 'true') arg = arg + "&withdocs=true";		if (postTagOnClick == 'true') arg = arg + "&postTagOnClick=true";		if (targetUri) arg = arg + "&targeturi=" + encodeURIComponent(targetUri);		retrieveURL(arg , id); // debug ok	} else {		with (block.style) {			if (display=="none") {				display="block";			} else {				display="none";			}		}	    // sert a avoir la bonne image (ouvert/ferme)		makeConsistent(id,block);	}}var searchStr = "";////   AJAX POUR LE LIVETREE DEBUT//var req;var theId;var isIE=false;// see toggle2function retrieveURL(url, id) {	// alert(url);	theId = id;	if (window.XMLHttpRequest) { // Non-IE browsers		req = new XMLHttpRequest();		req.onreadystatechange = processStateChange;		try {			req.open("GET", url, true);		} catch (e) {			alert(e);		}	  req.send(null);	} else if (window.ActiveXObject) { // IE		isIE = true;		req = new ActiveXObject("Microsoft.XMLHTTP");		if (req) {			req.onreadystatechange = processStateChange;			req.open("GET", url, true);			req.send();		}	}}function processStateChange() {	if (req.readyState == 4) { // Complete	  if (req.status == 200) { // OK response		  		var item = document.getElementById('trigger:' + theId);		if (!item) {			alert("That's unexpected: item not found " + theId);			return;		}		// we add the returned data to the innerHTML 		// of the containing line 		var mother_li = mother_li = item.parentNode;		var innerli = mother_li.innerHTML;		innerli += '<ul id="block:' +theId+ '" class="livetree">';		innerli += req.responseText;		innerli += '</ul>';		mother_li.innerHTML = innerli;			    // sert a avoir la bonne image (ouvert/ferme)		var block = document.getElementById("block:" + theId);		makeConsistent(theId,block);		// highlight: used by livesearch. Set by an id ("LSHighlight") on the line		var highlight = document.getElementById("LSHighlight");		if (highlight) {			// le changer			highlight.removeAttribute("id"); // set the hilite to false			son = highlight.firstChild.nextSibling.nextSibling.firstChild;			if (!isIE) {				highlight = son.nextSibling;			} else {				highlight = son;			}			highlight.setAttribute("id","LSHighlight");		}				displayCommentAsMarkdown() // 2019-09	  } else {		alert("Problem: " + req.statusText);	  }	}}  ////   AJAX POUR LE LIVETREE FIN//  ////////////////////////////////////////////// ceci n'a rien a voir avec les scripts precedents, juste mis là par paresse// LOAD IMAGEfunction loadImage(imageUri, imageLinkToPage) {	var img = document.getElementById("displayedimage");	img.src = imageUri;	var panel = document.getElementById("imagetobedisplayedpanel");	panel.style.display="block";	// ce qui suit est ok avec safari : on tombe sur le href. Mais avec firefox, prend le commentaire entre deux	// panel.firstChild.setAttribute("href",imageLinkToPage);	var theHref = document.getElementById("displayedimagehref");	theHref.setAttribute("href",imageLinkToPage);}function printVersion() { // 2020-01	displayRightBar(false);    var d = document.getElementById("logo");    if (d) {        d.style.display = "none";    }}function printVersion_TOBEDELETED() {	var d = false;	d = document.getElementById("right");	if (d) {		d.style.display = "none";	}	d = document.getElementById("navcontainer");	if (d) {		d.style.display = "none";	}	d = document.getElementById("logo");	if (d) {		d.style.display = "none";	}	d = document.getElementById("file_info")	if (d) {		d.style.display = "none";	}	d = document.getElementById("aboutThisDoc")	if (d) {		d.style.display = "none";	}	d = document.getElementById("documenttags")	if (d) {		d.style.display = "none";	}	d = document.getElementById("middle")	if (d) {		d.id = "middleprint";	}}//// DRAG'N DROP 2020-05////// DRAG'N DROP ONTO Comment field//function dropToComment(ev) {//	alert("drop " + JSON.stringify(ev.dataTransfer));//	alert("drop" + JSON.stringify(ev.dataTransfer.getData("text/plain") ))		var t = ev.dataTransfer.getData("text");	if (!t) return;	if (!(t.startsWith("http://")) && !(t.startsWith("https://"))) return;		//	// That's a link that's being dragged (well, most probably)	// We try to convert it to a markdown link, with a label.	// 1) try to get the label of the link	// 2) update the field (better to do it *after* browser's default drop)		var href = t;		var linkLabel = false;		// attempt to retrieve a text for the link from the content of the page	// the <a> tags in the page	// (so we maybe won't find a tag's label	// if dragged from a checkbox)	// we also don't find it if the html does not contain a long url	// (for instance in the tag chekboxes)	var links = document.querySelectorAll("a[href='" + href + "']");			if (links.length > 0) {		linkLabel = links[0].textContent;	} else {		// could be as a link starting with '/' in the html		var host = window.location.protocol + '//' + window.location.host		var con = host + getContextPath();		if (href.startsWith(con + '/')) {			links = document.querySelectorAll("a[href='" + href.substring(host.length) + "']");			if (links.length > 0) {				linkLabel = links[0].textContent;			}		}	}	var oldText = ev.target.value;	setTimeout(() => dropLinkIntoMarkdown(ev.target, oldText, href, linkLabel), 0);			}/** * Replace in a textarea a given href text by a markdown link. * (This is to be called by a drop event function whose default has been executed) * @param target textarea the link *has been* dropped on * @param oldText: content of the textarea before the dropping * @param href: the dragged href (note: it is a long url, even if not in the html - at least, it seems to be so) * @param linkLabel (maybe false) * @returns */function dropLinkIntoMarkdown(target, oldText, href, linkLabel) {	var text = target.value; // new text		// we must find where the href has been added (just looking for href	// in text could lead to a change somewhere else in the textarea, if it already contained	// mention of href)	// text is of the form: t1 + href + t2	// with oldText == t1 + t2	var oldTextLen = oldText.length;	var kInsertion = 0; // position where href has been inserted	for(;;) {		kInsertion = text.indexOf(href, kInsertion);		if (kInsertion < 0) {			// not found			return;		}		// is it the right one?		var ok = (oldText.substring(0,kInsertion) + href + oldText.substring(kInsertion) == text);		if (ok) {			break;		} else {			if (kInsertion > oldTextLen) {				// not found				return;			}			kInsertion = kInsertion+href.length;		}	}	// we want to replace the occurrence of href in text	// by a markdown link	var link = false;		// is it a link to semanlink stuff?	var con = window.location.protocol + '//' + window.location.host + getContextPath();	var conSlash = con + '/';	if (!href.startsWith(conSlash)) { // not a link to semanlink		if (linkLabel) {			link = '[' + linkLabel + '](' + href + ')';		} else {			link = '<' + href + '>';		}			} else { // a link to semanlink		if (!linkLabel) {			var k = href.lastIndexOf("/");			if (k > 0) {				linkLabel = href.substring(k+1);				if (linkLabel.endsWith(".html")) {					linkLabel = linkLabel.substring(0, linkLabel.length-5);				}			}		}		if (linkLabel) {			var shortHref = href.substring(con.length);			link = '[' + linkLabel + '](' + shortHref + ')';		} else {			link = '<' + href + '>';		}	}		var newText = oldText.substring(0, kInsertion) + link + oldText.substring(kInsertion);		target.value = newText;	// to refresh the display of the formatted markdown	if (target.editor) {		target.editor.update();	}	// select the text corresponding to added link	target.selectionStart = kInsertion;	target.selectionEnd = kInsertion+link.length;}//// DRAG'N DROP ONTO List of tags (parents, children,...)//////function onDragStart(ev) {//	alert("dragstart " + JSON.stringify(ev));//  // ev.dataTransfer.setData("text", ev.target.id);//}//////function drag(ev) {//	alert("drag " + ev);//  // ev.dataTransfer.setData("text", ev.target.id);//}function allowDrop(ev) {	const isLink = ev.dataTransfer.types.includes("text/uri-list");	if (isLink) {		ev.preventDefault();	}}// ev.currentTarget: the top node// ev.target: the actual node (inside ev.currentTargetfunction dropToTagList(ev) {	var tagHref = draggedTagHref(ev);	if (!tagHref) return;		alert("dropToTagList " + tagHref);	const targetId = ev.currentTarget.id; 	if (targetId == 'tag_parents') {		addTag(window.location, "add2parents", tagHref)	} else if (targetId == 'tag_children') {		addTag(window.location, "add2children", tagHref)	} else if (targetId == 'tag_friends') {		addTag(window.location, "add2friends", tagHref)	} else {		alert('unexpecteg target ' + targetId);	}	// in firefox, the default behavior for a drop even is to navigate to the link. Hence this:	ev.preventDefault();}// false if its not a tag that's being dragged// it is a long url (including servlet's host)function draggedTagHref(ev) {	if (!ev.dataTransfer) {		alert("dropToParents !ev.dataTransfer");		return false;	}	var t = ev.dataTransfer.getData("text");	if (!t) return false;	// is it a tag?	var con = window.location.protocol + '//' + window.location.host + getContextPath();	var tagns = con + '/tag/';	if (!t.startsWith(tagns)) return false;	// it's a tag	if (t.endsWith('.html')) t = t.substring(0, t.length-5);	return t;}// supposed to be called on page displaying the tag// actionProp: see goTagfunction addTag(targetUrl, actionProp, tagHref) {	var params = {'action2020' : actionProp,			'uri' : tagHref};		post(targetUrl, params);}// POSTING TO ADD TAG (add parent, add child, etc)// 3 ways have been tested. All seem to workfunction post(targetUrl, params) {	// post_usingAjax(targetUrl, params);	// post_usingForm(targetUrl, params);	post_usingExistingForm(targetUrl, params)}//see CoolUriServlet action2020//see template.jsp form id="tag_parents_form"function post_usingExistingForm(targetUrl, params) {	var form = document.getElementById('tag_parents_form');	// form.getElementByName('uri').value	document.getElementById('tag_parents_form_action2020').value = params['action2020'];	document.getElementById('tag_parents_form_uri').value = params['uri'];		document.getElementById("tag_parents_form").submit();}// https://stackoverflow.com/questions/133925/javascript-post-request-like-a-form-submitfunction post_usingForm(targetUrl, params, method='POST') {	  var form = document.createElement('form');	  form.method = method;	  form.action = targetUrl;	  for (const key in params) {	      const hiddenField = document.createElement('input');	      hiddenField.type = 'hidden';	      hiddenField.name = key;	      hiddenField.value = params[key];	      form.appendChild(hiddenField);	  }	  document.body.appendChild(form);	  alert("About to submit");	  form.submit();	  document.body.removeChild(form);}function post_usingAjax(targetUrl, params) {    var request = new XMLHttpRequest();    request.open("POST", targetUrl, true);    request.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');    request.onreadystatechange = function () {        if (request.readyState === 4 && request.status === 200) {        	// TOD REVOIR CE QU'il en est:        	// j'vaais abandonné suite à pb with firefox, en définitive sans rapport        	// tried this, when was returning the content of the page        	// doesn't work with firefox (move to linked kw (!?)//            document.open();//            document.write(request.response);//            document.close();        	// TODO, OPTIM: the page is computed once with the post, and now we acees again to the result        	// (would be better with a form            location.reload();        }    };    var p = false;	for (const key in params) {		if (p) {			p += '&';		} else {			p = '';		}		p += key;		p += '=';		p += encodeURIComponent(params[key]);	}    request.send(p);}//function dragOver(ev) {	allowDrop(ev) // needed (calling on dragEnter is not enough	var dropZone = ev.currentTarget;		dragndropHighlight(dropZone, true);}function dragEnter(ev) {//	var x = "enter " + ev.target //	if ( ev.target.id) x += " id: " + ev.target.id;//	if (dropZoneLeft) {//		dropZoneLeft = false;//	}//	console.log("enter " + x);		// lastDropEnter = ev.target;	// var dropZone = document.getElementById("tag_parents");	var dropZone = ev.currentTarget;		dragndropHighlight(dropZone, true);	allowDrop(ev);}// var dropZoneLeft = false;// var lastDropEnter = false;function dragLeave(ev) {	// alert("dragLeave " + ev.target);	//	var x = "leave " + ev.target;//	if ( ev.target.id) x += " id: " + ev.target.id;//	if (ev.currentTarget) {//		if ( ev.currentTarget.id) x += " currentTarget: " + ev.currentTarget.id;//	}//	console.log(x);		// var dropZone = document.getElementById("tag_parents");	var dropZone = ev.currentTarget;	// if (ev.target === dropZone) {		// console.log("leaving " + ev.target.id);		// dropZoneLeft  = true;		dragndropHighlight(dropZone, false);	// }}function dragEnd(ev) {	dragndropHighlight(ev.currentTarget, false);}function dragStart(ev) {	// ev.dataTransfer.dropEffect = "copy"; // marche pas}function dragndropHighlight(dropZone, bHighlight) {	if (bHighlight) {		dropZone.style.outline = "thin solid blue";	} else {		dropZone.style.outline = "";	}		}