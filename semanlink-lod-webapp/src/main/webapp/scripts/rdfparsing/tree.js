/**
DISPLAYING A TREE

How a tree looks like (except for the formatting and spaces that are not included):
<ul class="livetree">
	<li>
		<img src="[getContextURL()]/semanlink/ims/box_closed.gif" alt="" height="8px" width="8px" onclick="triangleClick"/>
		<a href="[uri of son]">[son's label]</a>
	</li>
	...
	<li class="treeLeaf">
		<a href="[uri of leaf]">[leaf's label]</a>
	</li>
	...
</ul>

We keep the treeObject in the ul element in order to be able
to find necessary information (for instance, the kw, and what are the properties used by this tree)
when clicking on a triangle image to create the subtree
Problem with Safari (3, at least) : doesn't work anymore if we leave the page and then click the back button of the browser
// MAYBE a solution would be to store the information needed in a hidden text node of the tree? Obviously, it's more work.
// Also, we'd need to know the tree's container to get the text node
// AND how would we store kb? Storing the name of the global variable would work?

// To open a subtree, we need the uri of the corresponding res (to download the rdf).
// In a first version, we used the href link to get it. But this implied that its position is known and well defined
// this made impossible to easily change the form of the line. 
// We now use an attribute we add to the line (li tag): resuri.
*/

/*
BEWARE: requires the following functions to be defined
displayLinkToRes = function(kb, res, container)
// NO MORE THIS ONE ? displayPropValue = function(kb, rdfNode, container, showLangInCaseOfText) si : quand on traitera des feuilles literal
getContextURL()
linkToRdf(uri)
*/

/**
 * A tree is defined by:
 * - the parsed RDF
 * - the properties to be used as "child". They can be direct properties (from parent to child)
 * or inverse (from child to parent). Both can be used in the definition of one tree.
 * - the properties linking to leaves (as from children, can be direct or inverse)
 * - if a customized way of displaying nodes or leaves is desired, the scripts to display them 
 * (this is just a matter of displaying the corresponding line in the tree: 
 * what is needed to display the triangle images, and the script to open/close a triangle
 * are handled by this script).
 * @kb the knowledge base (the parsed RDF)
 * @param sonProps array of "child" properties
 * @param parentProps array of the "parent" properties
 * @param leafProps array of the "leaf" properties
 * @param leafInvProps array of the "is leaf of" properties
 * @param displayNodeScript script in charge of displaying a non-leaf node of the tree. 
 * Either null, or a function of the form: function(res, thisTreeObject, container)
 * If null, supposes that a method displayLinkToRes is defined somewhere
 * @param displayLeafScript script in charge of displaying a leaf of the tree. 
 * Either null, or a function of the form: function(leaf, thisTreeObject, container)
 * If null, supposes that a method displayLinkToRes is defined somewhere // TODO change: literal case not supported yet
 * @param checkDisplayTriangleScript function in charge of evaluating whether a node line is to be displayed with
 * a leading "open-close" triangle image or not (basically: does the line have children or leaves, or not)
 * Either null, or a function of the form: function(treeObject, res), where res is the resource displayed on the line.
 * If null, the triangle image is always displayed (possibly leading to an empty subtree)
 * This parameter can be set to simpleCheckDisplayTriangleScript. In this case, the triangle will be displayed
 * if, in the rdf contained in treeObject.kb already downloaded, res has at least one child or leaf. This is handy
 * on the javascript side, but it requires that the RDF produced for a resource in the tree already contains
 * this information, that does not relate directly to the main resource.
 */
function TreeObject(kb, sonProps, parentProps, leafProps, leafInvProps, displayNodeScript, displayLeafScript, checkDisplayTriangleScript) {
	this.kb = kb;
	this.sonProps = sonProps;
	this.parentProps = parentProps;
	this.leafProps = leafProps;
	this.leafInvProps = leafInvProps;
	if (displayNodeScript) {
		this.displayNodeScript = displayNodeScript;
	} else {
		this.displayNodeScript = displayNodeDefaultScript;
	}
	if (displayLeafScript) {
		this.displayLeafScript = displayLeafScript;
	} else {
		this.displayLeafScript = displayLeafDefaultScript;
	}
	if (checkDisplayTriangleScript) {
		this.checkDisplayTriangleScript = checkDisplayTriangleScript;
	} else {
		this.checkDisplayTriangleScript = checkDisplayTriangleDefaultScript;
	}
	this.resCompareMethod = function(res1, res2) {
		return resCompare(res1, res2, kb);
	}
}

TreeObject.prototype.displayTree = function (mainRes, container) {
	var x = this.constructDisplay(mainRes);
	if (x) container.appendChild(x);
}

TreeObject.prototype.constructDisplay = function (mainRes) {
	//alert("displayTree " + mainRes.uri);
	var nobjs = 0;
	var sonss = []; // one for each sonProp and parentProp
	if (this.sonProps) {
		for (var i=0 ; i<this.sonProps.length; i++) {
			sonss[i] = this.kb.each(mainRes,this.sonProps[i]);
			nobjs += sonss[i].length;
		}
	}
	if (this.parentProps) {
		for (var i=0, k=sonss.length ; i<this.parentProps.length; i++, k++) {
			sonss[k] = this.kb.each(undefined,this.parentProps[i],mainRes);
			nobjs += sonss[k].length;
		}
	}
	var leavess = []; // one for each leaf prop and leaf inv prop
	if (this.leafProps) {
		for (var i=0 ; i<this.leafProps.length; i++) {
			leavess[i] = this.kb.each(mainRes,this.leafProps[i]);
			nobjs += leavess[i].length;
		}
	}
	if (this.leafInvProps) {
		for (var i=0, k=leavess.length ; i<this.leafInvProps.length; i++, k++) {
			leavess[k] = this.kb.each(undefined,this.leafInvProps[i],mainRes);
			nobjs += leavess[k].length;
		}
	}
	
   	if (nobjs > 0) {
	   	var ul1 = document.createElement("ul");
	   	ul1.treeObject = this;
	   	// GLOBAL_TREE_OBJECT = this; // could be used for safari to solve the problem with back
	   	ul1.className = "livetree";
	   	// 2011-01: we must check not to add twice the same line (can happen as we can have statements from dad 2 child and from child 2 dad)
	   	var alreadyIn = [];
		for (var i=0 ; i<this.sonProps.length+this.parentProps.length; i++) { // loop over the properties that are "son" or "parent" properties
			var objs = sonss[i];
			nobjs = objs.length;
			/////////// TODO : Here we sort only prop by prop
			objs.sort(this.resCompareMethod);
			///////////
			for (var j=0; j<nobjs; j++) {
				var obj = objs[j];
				//alert("son: " + obj);
				if (alreadyIn[obj.uri]) continue;
				else alreadyIn[obj.uri] = true;

				var li1 = document.createElement("li");
				
				
				if (this.checkDisplayTriangleScript(this, obj)) {
					
					var image = document.createElement("img");
					image.src = getContextURL() + "/ims/box_closed.gif";
					// image.id = "trigger:tree_" + j;
					image.alt = "";
					/*image.height="8"; // BEWARE not "8px" (or you won't see any image)
					image.width="8";*/
					image.onclick = triangleClick;
					li1.appendChild(image);
				}
				
				//
				// NEW
				li1.resuri = obj.uri;
				//
				//
				
				this.displayNodeScript(obj, this, li1);
				ul1.appendChild(li1);
			}
		}
		alreadyIn = [];
		for (var i=0 ; i<this.leafProps.length+this.leafInvProps.length; i++) { // loop over the properties that are "leaf" properties
			var objs = leavess[i];
			nobjs = objs.length;
			/////////// TODO : Here we sort only prop by prop
			objs.sort(this.resCompareMethod);
			///////////
			for (var j=0; j<nobjs; j++) {
				var obj = objs[j];
				if (alreadyIn[obj.uri]) continue;
				else alreadyIn[obj.uri] = true;
				var li1 = document.createElement("li");
				li1.className = "treeLeaf";
				
				//
				// NEW
				li1.resuri = obj.uri;
				//
				//

				// displayLinkToRes(this.kb, obj, li1); // TODO: non-uri node (literal)
				this.displayLeafScript(obj, this, li1);
				ul1.appendChild(li1);
			}
		}
	   	return ul1;
	} else {
		return false;
	}
} // constructDisplay 

//
// SORT
//

//
// DISPLAYNG TRIANGLE OR NOT
//

/** True iff obj has sons or leaves in the currently downloaded rdf */
simpleCheckDisplayTriangleScript = function(treeObject, obj) {
	if (treeObject.sonProps) {
		for (var i=0 ; i<treeObject.sonProps.length; i++) {
			if (treeObject.kb.any(obj,treeObject.sonProps[i])) {
				return true;
			}
		}
	}
	if (treeObject.parentProps) {
		for (var i=0 ; i<treeObject.parentProps.length; i++) {
			if (treeObject.kb.any(undefined,treeObject.parentProps[i],obj)) {
				return true;
			}
		}
	}
	if (treeObject.leafProps) {
		for (var i=0 ; i<treeObject.leafProps.length; i++) {
			if (treeObject.kb.any(obj,treeObject.leafProps[i])) {
				return true;
			}
		}
	}
	if (treeObject.leafInvProps) {
		for (var i=0 ; i<treeObject.leafInvProps.length; i++) {
			if (treeObject.kb.any(undefined,treeObject.leafInvProps[i],obj)) {
				return true;
			}
		}
	}
}

/** By default, we allways put a triangle image for a tree's node. */
checkDisplayTriangleDefaultScript = function(treeObject, obj) {
	return true;
}

//
// CLICK ON TRIANGLE
//

/** fired onclick on the triangle image of a tree. */
triangleClick = function(event) {
	// this is the triangle image
	// parent of this is the li tag
	var parentLi = this.parentNode;
	var parentUL = parentLi.parentNode
	var treeObject = parentUL.treeObject; // after a back to this page in safari, this is undefined
	// if (!treeObject) treeObject = GLOBAL_TREE_OBJECT; // after a back to this page in safari, this is ok
	
	// the triangle is either opened or closed
	var isClosed = (this.src.indexOf("closed.gif") > 0); // TODO
	if (isClosed) {
		// either create the subtree, or open it
		this.src = getContextURL() + "/ims/box_open.gif"; // TODO
		if (parentLi.childNodes.length < 3) {
			// subtree not created yet (or empty! what we're doing here is not very good,
			// because in this case, we try to create it again. But I
			// don't think it is very bad, as we already dereferenced the uri,
			// so it should be in the cache. However, would be better to handle that in another way
			// But how ? // TODO
			
			// res uri is the "resuri" attribute of parentLi
			var uri = parentLi.resuri; // after a back to this page in safari, this is undefined
			var rdfUri = linkToRdf(withoutEnd(uri)); // distinguish between uris served by the serving web app and others // withoutEnd: remove #xxx
			treeObject.kb.load(rdfUri, GLOBAL_dontNeedPrivileges);
			treeObject.displayTree(treeObject.kb.sym(uri), parentLi);
		} else {
			// subtree already created, just open it
			// last child is the subtree-ul
			var subtreeUL = parentLi.lastChild;
			subtreeUL.style.display = 'block';
		}
	} else {
		// close the subtree
		this.src = getContextURL() + "/ims/box_closed.gif"; // TODO
		// third child if the subtree-ul
		// (it is the last one, but we cannot use lastChild without checking first that the subtree is not empty)
		if (parentLi.childNodes.length > 2) { // this test, because the subtree could be empty
			var subtreeUL = parentLi.lastChild;
			subtreeUL.style.display = 'none';
		}
	}
}

/** TO BE COMPLETED (up in the ancestors) */
triangleUpClick = function(event) {
	// this is the triangle image
	// parent of this is the li tag
	var parentLi = this.parentNode;
	var parentUL = parentLi.parentNode
	var kb = parentUL.kb;
	var sonProps = parentUL.sonProps;
	
	// the triangle is either opened or closed
	var isClosed = true; // (this.src.indexOf("closed.gif") > 0); // TODO
	
	if (isClosed) {
		// either create the subtree, or open it
		// this.src = getContextURL() + "/ims/box_open.gif"; // TODO
		// if (parentLi.childNodes.length < 3) {
			// subtree not created yet (or empty! what we're doing here is not very good,
			// because in this case, we try to create it again. But I
			// don't think it is very bad, as we already dereferenced the uri,
			// so it should be in the cache. However, woud be better to handle that in another way
			// But how ? // TODO
			
			// the next sibling of the triangle image is the link
			var uri = this.nextSibling.href;
			
			// uri can be the uri of a resource
			// or a call to servlet with a /get servletPath
			// In this case, the uri of the resource is the value of the uri param
			// @find "using /get/?uri=" for links to uri // ET PAS htmlget ??????? // TODO CHECK
			var end = theEndOnly(uri, getContextURL() + "/get/?uri=");
			if (end) {
				uri = decodeURIComponent(end);
			}
			
			uri = withoutEnd(uri); // ?
		    // HACK TO CHANGE HERE // TODO:
		    // if we request uri, because of content negociation, we receive html. TODO check why - doesn't seem correct
		    // (probably SimpleHttpClient is configured to doRedirect)
		    // We could also use the isDefinedBy prop?
			kb.load(uri + ".rdf", GLOBAL_dontNeedPrivileges);
			
			displayAncestors(kb, sonProps, parentProps, leafProps, invLeafProps, kb.sym(uri), parentUL.parentNode);
			
		/*} else {
			// subtree already created, just open it
			// last child is the subtree-ul
			var subtreeUL = parentLi.lastChild;
			subtreeUL.style.display = 'block';
		}*/
	} /*else {
		// close the subtree
		this.src = getContextURL() + "/ims/box_closed.gif"; // TODO
		// third child if the subtree-ul
		// (it is the last one, but we cannot use lastChild without checking first that the subtree is not empty)
		if (parentLi.childNodes.length > 2) { // this test, because the subtree could be empty
			var subtreeUL = parentLi.lastChild;
			subtreeUL.style.display = 'none';
		}
	}*/
}

//
//
//

TreeObject.prototype.displayAncestors = function (mainRes, container) {
	var x = this.constructAncestorsDisplay(mainRes);
	if (x) {
		if (container.childNodes.length == 0) {
	   		container.appendChild(x);
	   		// container.appendChild(narrowerLabel);
	   	} else {
	   		container.insertBefore(x, container.childNodes[0]);
	   		// container.insertAfter(narrowerLabel, x);
	   	}
	}
}

/** 
 * if s starts with start, returns the end of s (everything after start)
 * else returns null */
function theEndOnly(s,start) { // this is the end, my only friend, the end
	var l = start.length;
	if (s.slice(0,l) == start) {
		return s.slice(l);
	} else {
		return null;
	}
}

TreeObject.prototype.constructAncestorsDisplay = function (mainRes) {
	var subjss = [];
	var n = 0;
	var k = 0;
	if (this.sonProps) {
		for (var i=0 ; i<this.sonProps.length ; i++, k++) {
			subjss[k] = this.kb.each(undefined,this.sonProps[i],mainRes);
			n = n + subjss[k].length;
		}
	}
	if (this.leafProps) {
		for (var i=0 ; i<this.leafProps.length ; i++, k++) {
			subjss[k] = this.kb.each(undefined,this.leafProps[i],mainRes);
			n = n + subjss[k].length;
		}
	}
	if (this.parentProps) {
		for (var i=0 ; i<this.parentProps.length ; i++, k++) {
			subjss[k] = this.kb.each(mainRes,this.parentProps[i]);
			n = n + subjss[k].length;
		}
	}	
	if (this.invLeafProps) {
		for (var i=0 ; i<this.invLeafProps.length ; i++, k++) {
			subjss[k] = this.kb.each(mainRes,this.invLeafProps[i]);
			n = n + subjss[k].length;
		}
	}	
	
   	if (n > 0) {
	   	var ul1 = document.createElement("p");
	   	// probably useless
	   	/*ul1.kb = this.kb;
	   	ul1.sonProps = this.sonProps;*/
	   	
	   	ul1.appendChild(document.createTextNode("Broader term(s): "));
	   	
	   	var items = [];
	   	var alreadyIn = [];
	   	var k=0
		for (var i=0 ; i<subjss.length ; i++) {
			var subjs = subjss[i];
			n = subjs.length;
			for (var j=0; j<n; j++) {
				var sub = subjs[j];
				if (alreadyIn[sub.uri]) continue;
				alreadyIn[sub.uri] = true;
				items[k] = sub;
				k++;
			}
		}
	   	
		/*
		for (var i=0 ; i<subjss.length ; i++) {
			var subjs = subjss[i];
			n = subjs.length;
			/////////// TODO : Here we sort only prop by prop
			subjs.sort(this.resCompareMethod);
			///////////
			
			for (var j=0; j<n; j++) {
				var sub = subjs[j];
				var li1 = document.createElement("span");
	
				displayLinkToRes(this.kb, sub, li1);
				if (j<n-1) li1.appendChild(document.createTextNode(" - "));
	
				ul1.appendChild(li1);
			}
		}
		*/
		items.sort(this.resCompareMethod);
		var n = items.length;
		for (var i=0 ; i<n ; i++) {
				var li1 = document.createElement("span");
				/* CECI EST PAS TROP MAL, MAIS A BESOIN D'ETRE CONTINUE */
				/*var image = document.createElement("img");
				image.src = getContextURL() + "/ims/box_up.gif"; // TODO
				image.alt = "";
				//image.height="8"; // BEWARE not "8px" (or you won't see any image)
				// image.width="8"
				// image.onclick = triangleUpClick;
				li1.appendChild(image);*/

				displayLinkToRes(this.kb, items[i], li1);
				if (i<n-1) li1.appendChild(document.createTextNode(" - "));
	
				ul1.appendChild(li1);
		}

		
		/*
		var narrowerLabel = document.createElement("p");
   		narrowerLabel.appendChild(document.createTextNode("Narrower term(s): "));
   		*/
   		return ul1;

	} else {
		return false;
	}
}

/** How a node (other than a leaf) is displayed, by default (that is, if tree created without displayNodeScript) */
displayNodeDefaultScript = function(res, thisTreeObject, container) {
	displayLinkToRes(thisTreeObject.kb, res, container);
}

/** How a leaf is displayed, by default (that is, if tree created without displayLeafScript) */
displayLeafDefaultScript = function(leafRDFNode, thisTreeObject, container) {
	displayLinkToRes(thisTreeObject.kb, leafRDFNode, container);  // TODO: non-uri node (literal)
}


