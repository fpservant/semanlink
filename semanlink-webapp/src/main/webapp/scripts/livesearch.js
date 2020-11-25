/*// Based on Bitflux live search code, adapted to be used for a list returned// by the search which is the first level of a live tree.// Live tree code by fps@semanlink.net// Other modifications (2007/12) to get it working with ie7//// Copyright notice of original code:// +----------------------------------------------------------------------+// | Copyright (c) 2004 Bitflux GmbH                                      |// +----------------------------------------------------------------------+// | Licensed under the Apache License, Version 2.0 (the "License");      |// | you may not use this file except in compliance with the License.     |// | You may obtain a copy of the License at                              |// | http://www.apache.org/licenses/LICENSE-2.0                           |// | Unless required by applicable law or agreed to in writing, software  |// | distributed under the License is distributed on an "AS IS" BASIS,    |// | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or      |// | implied. See the License for the specific language governing         |// | permissions and limitations under the License.                       |// +----------------------------------------------------------------------+// | Author: Bitflux GmbH <devel@bitflux.ch>                              |// +----------------------------------------------------------------------+//  some of modifs are marked with // fpsuses :- the result list (UL) has id LiveSearchRes- LSHighlight css id is used to highlight the selected LI tag of list- each LI supposed to contain a <A> tag as SECOND child- body onload : liveSearchInit- livesearchform.jsp with elements id : livesearch, LSRes LSShadow,...- livesearchxml.jsp (which includes livetreesons.jsp)- script liveSearchAction defined in template.jspSur les arbres (et highlight) : beaucoup repose sur le frere du frere du fils next fils, etc...Ne serait-il pas possible et plus simple de se baser uniquement sur l'identifiant ala 1.1.1.2de la ligne ? we would have more freedom for content*/liveSearchReq = false;var t = null;var liveSearchLast = "";var lastRightArrowTime = 0;var lastValueOnReturn = null;var nosearch = false;isIE = false;// on !IE we only have to initialize it once// but beware, the test (window.XMLHttpRequest) is true with ie7// and ie7 needs initialisation for each request/*if (window.XMLHttpRequest) {	liveSearchReq = new XMLHttpRequest();}*/function needsOnlyOneXMLHttpRequestInit() {	if (window.ActiveXObject) {		return false;	} else if (window.XMLHttpRequest) {		return true;	}}if (needsOnlyOneXMLHttpRequestInit()) {	liveSearchReq = new XMLHttpRequest();}function liveSearchInit() {	var livesearchElt = document.getElementById('livesearch');	if (livesearchElt.addEventListener) {		if (navigator.userAgent.indexOf("Safari") > 0) {			livesearchElt.addEventListener("keydown",liveSearchKeyPress,false); // for safari		} else {			livesearchElt.addEventListener("keypress",liveSearchKeyPress,false); // for gecko		}	} else if (livesearchElt.attachEvent) {		livesearchElt.attachEvent('onkeydown',liveSearchKeyPress);		isIE = true;	} else {		alert("Unable to add event listener");	}	livesearchElt.setAttribute("autocomplete","off");}// 2013-08 to avoid having to call liveSearchInit on loadliveSearchInit();function liveSearchHideDelayed() {	window.setTimeout("liveSearchHide()",400);}	function liveSearchHide() {	document.getElementById("LSResult").style.display = "none";	var highlight = document.getElementById("LSHighlight");	if (highlight) {		highlight.removeAttribute("id");	}}function liveSearchKeyPress(event) {	/*if (event.keyCode == 13 ) {		// ENTER		highlight = document.getElementById("LSHighlight");		alert(highlight);		if (!highlight) {			highlight = firstResLi();			alert(highlight);		}		if (!isIE) { event.preventDefault(); }	} else */	if (event.keyCode == 40 ) {		//KEY DOWN		highlight = document.getElementById("LSHighlight");		if (!highlight) {			highlight = firstResLi();		} else {			highlight.removeAttribute("id");			highlight = tree_nextLi(highlight);		}		if (highlight) {			highlight.setAttribute("id","LSHighlight");		    var node = document.getElementById("livesearch");		    node.unselect();		} else {			// back to input field		    var node = document.getElementById("livesearch");		    node.select();		}		if (!isIE) { event.preventDefault(); }	} else if (event.keyCode == 38 ) {		//KEY UP		highlight = document.getElementById("LSHighlight");		if (!highlight) {			highlight = lastResLi();		} else {			highlight.removeAttribute("id");			highlight = tree_prevLi(highlight);		}		if (highlight) {			highlight.setAttribute("id","LSHighlight");		    var node = document.getElementById("livesearch");		    node.unselect();		} else {			// back to input field		    var node = document.getElementById("livesearch");		    node.select();		}		if (!isIE) { event.preventDefault(); }	} else if (event.keyCode == 27) {		//ESC		highlight = document.getElementById("LSHighlight");		if (highlight) {			highlight.removeAttribute("id");		}		document.getElementById("LSResult").style.display = "none";	} else if (event.keyCode == 39) {		// Right arrow		// There's a problem with Safari (2): code is executed twice (generally at least)		// hence that trick :		now = (new Date()).getTime();		// alert(now + "/" + lastRightArrowTime);		if (now - lastRightArrowTime > 200) {			highlight = document.getElementById("LSHighlight");			if (highlight) {				// alert ("highlight " + highlight.id + "/" + highlight.name);				/* OK, juste pour le premier niveau				highlight.removeAttribute("id");				highlight.firstChild.onclick();				*/				ul = tree_ulInsideLi(highlight);				var alreadyLoaded = (ul != undefined);				if (alreadyLoaded) {					alreadyLoaded = (ul.innerHTML != "");				}				if (alreadyLoaded) { // son already loaded					highlight.removeAttribute("id"); // set the hilite to false					if ((ul.style.display == "")||(ul.style.display == "block")) {					} else { // "none", tree is closed. Click to open it						highlight.firstChild.onclick();					}					highlight = son.nextSibling; // LI					if (!highlight) highlight = son; // ie					highlight.setAttribute("id","LSHighlight");									} else { // son not loaded yet.					// Click on image to open and donwload son					highlight.firstChild.onclick();					// To hilite the son, we must wait for the server reply					// That's why we do not set the hilite to false now:					// searching for the LSHighlight in the onclick handler					// will allow us to know whether we have to hilite the first of the downloaded lines.				}			}		}		lastRightArrowTime = now;	} else if (event.keyCode == 8) {		// delete key				// the onkeypress of input field not fired when clicking back in ie7		// if (isIE) liveSearchStart();		// HUM, seems to be the same with safari and firefox // 2020-01		liveSearchStart();	}}/** cf onkeypress="liveSearchStart()" of input field */function liveSearchStart() {	// if (!nosearch) { // ???	// alert("liveSearchStart");		if (t) {			window.clearTimeout(t);		}		t = window.setTimeout("liveSearchDoSearch()",200);	// }}function liveSearchDoSearch() {	/*if (typeof liveSearchRoot == "undefined") {		liveSearchRoot = "";	}	if (typeof liveSearchRootSubDir == "undefined") {		liveSearchRootSubDir = "";	}*/	if (typeof liveSearchParams == "undefined") {		liveSearchParams = "";	}	if (liveSearchLast != document.getElementById('searchform').q.value) {		if (!nosearch) {			if (liveSearchReq && liveSearchReq.readyState < 4) {				liveSearchReq.abort();			}			if ( document.getElementById('searchform').q.value == "") {				liveSearchHide();				return false;			}			// fps			// This is not OK with ie7, which return true on window.XMLHttpRequest			// but requires however a new init of request every time			/*			if (window.XMLHttpRequest) {			// branch for IE/Windows ActiveX version			} else if (window.ActiveXObject) {				liveSearchReq = new ActiveXObject("Microsoft.XMLHTTP");			}			*/			if (!needsOnlyOneXMLHttpRequestInit()) {				try {					liveSearchReq = new ActiveXObject("Msxml2.XMLHTTP");				} catch (e) {					try {						liveSearchReq = new ActiveXObject("Microsoft.XMLHTTP");					} catch(e) {						alert("Ajax not available");					}				}			}			liveSearchReq.onreadystatechange= liveSearchProcessReqChange;			// fps 			// with following line, problem with non ascii characters.			// liveSearchReq.open("GET", liveSearchRoot + "/semanlink/livesearch.do?text=" + document.getElementById('searchform').q.value + liveSearchParams);			// This way, non ascii characters are decoded OK with URLEncoder.decode(request.getParameter("text"))			// (same trick in the SL bookmarklet)			// BEWARE to url rewriting ! This is not good if url rewriting necessary			// liveSearchReq.open("GET", liveSearchRoot + "/semanlink/livesearch.do?text=" + encodeURIComponent(encodeURIComponent(document.getElementById('searchform').q.value)) + liveSearchParams);			url = liveSearchAction(); // defini dans template.jsp			url = url + "?text=" + encodeURIComponent(encodeURIComponent(document.getElementById('searchform').q.value)) + liveSearchParams;									var targeturi = document.getElementById('searchform').targeturi; // 2020-02 TagAndTag			if (targeturi) {				url += "&targeturi=" + encodeURIComponent(targeturi.value);			}						// alert("livesearch.js getting " + url);			liveSearchReq.open("GET", url);			liveSearchLast = document.getElementById('searchform').q.value;			liveSearchReq.send(null);		}	}	nosearch = false;}function liveSearchProcessReqChange() {		if (liveSearchReq.readyState == 4) {		var  res = document.getElementById("LSResult");		res.style.display = "block";		var  sh = document.getElementById("LSShadow");				sh.innerHTML = liveSearchReq.responseText;		///////		// highlight = firstResLi();		// @see livetreesons.jsp : from highlighted <LI> to link (LI includes an image before the <a> tag )		// if ((highlight) && (highlight.firstChild) && (highlight.firstChild.nextSibling)) {			// highlight.setAttribute("id","LSHighlight");		//}		///////		 	}}// 2 methodes differentes peuvent etre invoquees par livesearchform au moment du clic selon qu'on est// en get (go kw) ou en post (add kw)function liveSearchSubmit4Get() {	// var highlight = getResult();	highlight = document.getElementById("LSHighlight");	if (highlight) {		// SIMPLE SOLUTION : same as clicking the link of the highlighted line (or first line if none is selected).		// THIS WORKS FINE to follow the link (but do not allow to do any kind of other action)		// (this directly sets the window.location. Searchform is not invoked.		window.location = highlight.firstChild.nextSibling.getAttribute("href");		return false; // Searchform is not invoked.				// CECI MARCHE AUSSI : on set l'action de la form et on l'invoque		// document.searchform.action = highlight.firstChild.nextSibling.getAttribute("href"); // attention pour le moment href est showkw?... (maintenant, �a doit �tre /semanlink/tag/... -- avec l'�ventuelle session de l'url rewriting		// return true; // let the searchform do the get	} else {		//  nothing highlighted (nor a line in list) -> action defined in searchform		//  Has to handle case of a search without result		// return true;		highlight = safeFirstResLi();		if (highlight) {			highlight.setAttribute("id","LSHighlight");		}		return false; // Searchform is not invoked.	}}function liveSearchSubmit4Post() {	// var highlight = getResult();	highlight = document.getElementById("LSHighlight");	if (highlight) {		// si il s'agit d'ajouter un tag (mode edit), on demande confirmation		// En mode edit, il y a un pulldown menu permettant de choisir add 2 parents, ...		// Donc, si ce pulldown n'est pas present, ne pas afficher de dialogue de confirmation		if (document.getElementById('searchform').actionprop) {			// (note: on ne veut pas afficher de dialogue de confirmation 			// si c'est go qui est choisi. This is handled in askConfirmation)			if (!(askConfirmation())) return false;		}		// SIMPLE SOLUTION : same as clicking the link of the highlighted line (or first line if none is selected).		// THIS WORKS FINE to follow the link (but do not allow to do any kind of other action)		// (this directly sets the window.location. Searchform is not invoked.		// window.location = highlight.firstChild.nextSibling.getAttribute("href");		// return false; // Searchform is not invoked.				// THIS sets an hidden value of searchform with the "href" and let the searchform do the post		// note href looks like: /semanlink/tag/... -- with sessionId of url rewriting, if any		// alert(highlight.firstChild.nextSibling.getAttribute("href"));		document.getElementById('searchform').kwhref.value = highlight.firstChild.nextSibling.getAttribute("href"); // href: /semanlink/tag/... -- avec l'éventuelle session de l'url rewriting		return true; // let the searchform do the post			} else {		//  nothing highlighted (nor a line in list) -> action defined in searchform		//  Has to handle case of a search without result		// return true;				// 2020-11: no more selecting first line after return (edit mode)		// in order to give possibility to click "Create Tag" button//		highlight = safeFirstResLi(); // 2020-11//		if (highlight) { // if there is a line in the list//			//  With usual (not live) search,the user type some chars, and hit return to do the search//			// we do not want in this case to add a new tag, or add the first line//			// document.getElementById('searchform').q.value = highlight.firstChild.nextSibling.text;//			if (document.getElementById('searchform').q.value != lastValueOnReturn) {//				// keep record of value of input field when return has been hit//				lastValueOnReturn = document.getElementById('searchform').q.value;//				// set the input field to text of first tag in returned list//				nosearch = true; // trick to prevent doing search now (this search is useless, and it would prevent highlighting of line that would be reseted immediately)//				// 2020-04 commented out // document.getElementById('searchform').q.value = highlight.firstChild.nextSibling.text;//				highlight.setAttribute("id","LSHighlight"); // would't work (reseted immediatly by search) without "nosearch" trick//			} else {//				// the user hit return again with the same content in input field://				// he really wants it//				if (!(askConfirmation())) return false;//				return true; // let the searchform do the post//			}//			//			return false; // Searchform is not invoked.//		} else {//			// no value corresponding to the input//			// invoke searchform on the content of the input field//			if (!(askConfirmation())) return false;//			return true; // let the searchform do the post//		}				// invoke searchform on the content of the input field		if (!(askConfirmation('Add tag: ' + document.getElementById('searchform').q.value + '?'))) {			return false;		}		return true; // let the searchform do the post			}}function askConfirmation(mess) {	// no confirmation dialog when "go" is selected	if (!mess) {		if (document.getElementById('searchform').actionprop.value == "go") return true;			}	if (!mess) mess = 'Add this tag?'	return window.confirm(mess);}function getResult() {	var highlight = document.getElementById("LSHighlight");	if (!highlight) {			highlight = firstResLi();	}	// @see livetreesons.jsp : from highlighted <LI> to link (LI includes an image before the <a> tag )	if ((highlight) && (highlight.firstChild) && (highlight.firstChild.nextSibling)) {		return highlight;	} else {		return false;	}}/** this function gets called when clicking on a tag of the livesearch form results when editing is on * @param tagHRef: /semanlink/tag/... */function postTag(tagHRef) {	if (!(askConfirmation())) return false;	document.getElementById('searchform').kwhref.value = tagHRef;	document.getElementById('searchform').submit();}/** returns the first <LI> of result *  Doesn't check that the result is not null @see safeFirstResLi */function firstResLi() {	if (isIE) {		return document.getElementById("LiveSearchRes").firstChild;	} else {		return document.getElementById("LiveSearchRes").firstChild.nextSibling;	}}/** returns the first <LI> of result or null if result is "" */function safeFirstResLi() {	ul = document.getElementById("LiveSearchRes");	if (!ul) return null;	if (isIE) {		return ul.firstChild;	} else {		return ul.firstChild.nextSibling;	}}function lastResLi() {	if (isIE) {		return document.getElementById("LiveSearchRes").lastChild;	} else {		return document.getElementById("LiveSearchRes").lastChild.previousSibling; // li	}}function tree_ulInsideLi(li) {	if (isIE) {		return li.firstChild.nextSibling;	} else {		return li.firstChild.nextSibling.nextSibling;	}}function lineId(li) {	x = li.firstChild.id; // "trigger:1_1"	x = x.substring(x.indexOf(":") + 1);	// alert("lineId :" + x);	return x;}function upInTree(li) {	liid = lineId(li);	if (!liid) {		x = null;	} else if (liid.indexOf("_") < 0) {		x = null;		// alert("upInTree : null");	} else {		items = liid.split("_");		parentId = items[0];		k = 1;		while(k < items.length - 1) {			parentId = parentId + "_" + items[k];			k = k+1;		}		parentUL = document.getElementById("block:" + parentId);		parentLI = parentUL.parentNode; // la ligne parente		x = parentLI;		// alert("upInTree2 : " + x + " : " + parentId);	}	return x;}function tree_nextLi(li) {	nextLi = li.nextSibling;	// normaly, the test : (!nextLi) should say whether we are on the first line or not,	// but this doesn't work, because there is a Text object between the last LI and the /UL	// hence this test :	if ((!nextLi) || (!nextLi.firstChild)) { // last line		parentLi = upInTree(li);		if (parentLi) {			uncleLi = parentLi.nextSibling;			if (!uncleLi) {				x = tree_nextLi(parentLi);			} else {				x = uncleLi;			}		} else {			// 2020-04 we do not loop anymore to last line when on first one			// we now want to get back to the input field: so we return false			// to inform the caller to take approripate action			// this "loops":			// x = firstResLi();			x = false;		}	} else {		x = nextLi;	}	// alert("tree_nextLi : " + x);	return x;}function tree_prevLi(li) {	prevli = li.previousSibling;	// normaly, the test : (!prevli) should say whether we are on the first line or not,	// but this doesn't work, because there is a Text object between the UL and the first LI.	// hence this test :	if ((!prevli) || (!prevli.firstChild)) { // 1ere ligne		parentLi = upInTree(li);		if (parentLi) {			x = parentLi;		} else {			// 2020-04 we do not loop anymore to last line when on first one			// we now want to get back to the input field: so we return false			// to inform the caller to take approripate action			// this "loops":			// x = lastResLi();			x = false;		}	} else {		x = prevli;	}	return x;}//// 2017-06 to add link as text in comments // TODO 2020-05 : is this used ???//function insertAtCaret(areaId, text) {    var txtarea = document.getElementById(areaId);    var scrollPos = txtarea.scrollTop;    var caretPos = txtarea.selectionStart;    var front = (txtarea.value).substring(0, caretPos);    var back = (txtarea.value).substring(txtarea.selectionEnd, txtarea.value.length);    txtarea.value = front + text + back;    caretPos = caretPos + text.length;    txtarea.selectionStart = caretPos;    txtarea.selectionEnd = caretPos;    txtarea.focus();    txtarea.scrollTop = scrollPos;}//////// https://stackoverflow.com/questions/985272/selecting-text-in-an-element-akin-to-highlighting-with-your-mousefunction selectText(node) {    node = document.getElementById(node);    node.focus();    node.select();    node.unselect();//    if (document.body.createTextRange) {//        const range = document.body.createTextRange();//        range.moveToElementText(node);//        range.select();//    } else if (window.getSelection) {//        const selection = window.getSelection();//        const range = document.createRange();//        range.selectNodeContents(node);//        selection.removeAllRanges();//        selection.addRange(range);//    } else {//        console.warn("Could not select text in node: Unsupported browser.");//    }}