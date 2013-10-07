/**
 * Scripts to display the HTML for a Non-Information Resource (NIR) from its RDF 
 *
 * REQUIRES that:
 * - there is a getContextURL() javascript function returning the context of the LEDServlet
 * (for instance http://127.0.0.1:8080/ledservlet (NOT "/" terminated))
 * (There is such a function in template.jsp)
 * 
 * - there are 2 methods:
 * 		- lod_linkToHtml(uri);
 *		- lod_linkToRdf(uri);
 *
 * - and that there is the declaration of a global variable TYPE2METHOD (an array) // 2010-12 // @find display of res in function of their rdf:type
 * 
 * // //@find "using /get/?uri=" for links to uri
 * 
 * 
 */
 
// il ne faut pas confondre les uri et les resources correspondantes,
// qu'on obtient a partir d'une uri via kb.sym(uri)

// For quick access to those namespaces:
FOAF = Namespace("http://xmlns.com/foaf/0.1/")
RDF = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
RDFS = Namespace("http://www.w3.org/2000/01/rdf-schema#")
OWL = Namespace("http://www.w3.org/2002/07/owl#")
DC = Namespace("http://purl.org/dc/elements/1.1/")
RSS = Namespace("http://purl.org/rss/1.0/")

SL = Namespace("http://www.semanlink.net/2001/00/semanlink-schema#")
// Not here, or complaint "getContextURL() not defined" 
// ISO_SH = Namespace(getContextURL() + "/iso3166#")


// RDC = Namespace(getContextURL() + "/rdc/")

// "GLOBAL" VARIABLES
GLOBAL_RDFDisplay='';
GLOBAL_rdfUri='';
GLOBAL_kb='';
GLOBAL_container='';
// GLOBAL_lang = getPrefLanguage();
GLOBAL_lang = "fr";
/** we do not need to connect to other sites. */
GLOBAL_dontNeedPrivileges = true;

//
// @find display of res in function of their rdf:type
// 2010-12 Test to give capacity to dynamically load scripts to handle the display of res in function of their rdf:type
// L'idée : un tableau des functions.
// Attention, une fct doit être définie AVANT d'être mise ds le tableau
// Par ex ci dessous, il faut impérativement que la fct displayTag soit définie avant d'être mise ds le tableau
//

// TYPE2METHOD = false;
// if (!TYPE2METHOD) TYPE2METHOD = new Array();

/* 
if (!TYPE2METHOD) TYPE2METHOD = new Array();

// moved to template.jsp

displayTag = function(kb, res, container) {
	// alert("That's a tag: " + res);
	displayTagInList(kb, res, container);
}
TYPE2METHOD["http://www.semanlink.net/2001/00/semanlink-schema#Tag"]=displayTag;

displayTagInList = function (kb, res, container) {
	var containingDiv;
	containingDiv = document.createElement("div");
 	containingDiv.className = "graybox";	

	var label = getLabel(kb, res);
	var titleParag = document.createElement("h3");
	if (res.uri) { // cf anon res: no uri
		displayLinkToURI(res.uri, "TAG: " + label, titleParag);
	} else { // sould'nt happen: a tag always has a uri
		titleParag.appendChild(document.createTextNode("TAG: " + label));
	}
	container.appendChild(titleParag);
	displayMainResAsSubject(kb, res, containingDiv);
	displayMainResAsObject(kb, res, containingDiv);

	container.appendChild(containingDiv);
}
*/



//
//
//

/**
 * THIS IS THE SCRIPT CALLED "onload" OF THE HTML PAGE
 * @param rdfUri uri that returns some rdf (for instance describing a possibly non information-resource mainRes). 
 * This rdf is supposed to contain a statement (mainRes,isDefinedBy,rdfUri)
 * @param containerId id of the div of the document the display will be inserted into
 * @param mainResUri if omitted, we try to find it in the rdf (prop RDFS(isDefinedBy)). If not found, all "root" res are displayed.
 * @param displayAllResInList boolean true to display all res (not only the main res or only the roots)
 */
doIt = function(rdfUri, containerId, mainResUri, displayAllResInList) {
	ISO_SH = Namespace(getContextURL() + "/iso3166-schema#");

	var x = new RDFDisplay(rdfUri, containerId)
	return x.displayAllInContainer(GLOBAL_kb, rdfUri, GLOBAL_container, mainResUri, displayAllResInList);
	// langIntoForm(GLOBAL_lang);
}

/** this is called from the CPA servlet, but it is more or less the same thing as RDFDisplay TODO A VIRER */ 
jsRDFData = function(rdfUri, containerId, mainResUri) {
	RDFDisplay(rdfUri, containerId);
}

RDFDisplay = function(rdfUri, containerId) {
	// alert("rdf_parsing.js doIt rdfUri " + rdfUri);
	// loading the rdf
	var kb = loadRDF(rdfUri);
	// kb.load(rdfUri);
	// target element in the document  (supposed to be a div)
	var container = document.getElementById(containerId);
	
	// ESSAI POUR AJOUTER A kb LES ELEMENTS DU SCHEMA
	// CF PB FIREWALL
	// kb.load(getContextURL() + "/schema/rdc-schema.owl");

	// ESSAI POUR AJOUTER DES labels AUX PROPS USUELLES
	/*kb.register('dc', "http://purl.org/dc/elements/1.1/")
	kb.register('rdf', "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
	kb.register('rdfs', "http://www.w3.org/2000/01/rdf-schema#")
	kb.register('owl', "http://www.w3.org/2002/07/owl#")*/
	// kb.load("http://www.w3.org/1999/02/22-rdf-syntax-ns"); // Erreur: uncaught exception: Permission refus&eacute;e d'appeler la m&eacute;thode XMLHttpRequest.open
	// They are cached by the browser: this won't generate many calls to the server
	// CF PB FIREWALL
	// kb.load(getContextURL() + "/get/?uri=" + encodeURIComponent("http://www.w3.org/1999/02/22-rdf-syntax-ns"));
	// kb.load(getContextURL() + "/get/?uri=" + encodeURIComponent("http://www.w3.org/2000/01/rdf-schema"));
	
	GLOBAL_RDFDisplay = this;
	GLOBAL_rdfUri = rdfUri;
	GLOBAL_kb = kb;
	GLOBAL_container = container;
}

/**
 * @param uri the URI of the RDF describing the NIR
 */
function loadRDF(uri) {
	// TestStore implementation from dig.csail.mit.edu/2005/ajar/ajaw/test/rdf/rdfparser.test.html
	// RDFIndexedFormula from dig.csail.mit.edu/2005/ajar/ajaw/rdf/identity.js
	//  (extends RDFFormula from dig.csail.mit.edu/2005/ajar/ajaw/rdf/term.js which has no indexing and smushing)
	// for the real implementation used by Tabulator which uses indexing and smushing
	
	// var x = new TestStore()
	
	var x = new RDFIndexedFormula(); // (defined in identity.js)
	
	// this, to remove #xxx part of the uri
	var docURI = withoutEnd(uri);
	x.load(docURI, GLOBAL_dontNeedPrivileges);
	return x;
}















//
// TREE
//
doTreeIt = function(rdfUri, containerId, mainResUri) {	
	init(rdfUri, containerId);
	var mainRes = GLOBAL_kb.sym(mainResUri);
	treeIt(GLOBAL_kb, GLOBAL_container, mainRes);
}

treeIt = function (kb, container, mainRes) {
	var treeObject = getTreeObject(kb, mainRes);
	// treeObject.displayAncestors(mainRes, container);
	var x = treeObject.constructAncestorsDisplay(mainRes);
	if (x) container.appendChild(x);
	// treeObject.displayTree(mainRes, container);
	var x = treeObject.constructDisplay(mainRes);
	if (x) container.appendChild(x);
}


/** return the TreObject (no display done) */
getTreeObject = function(kb, mainRes) {
	// type of mainRes?
	var types = kb.each(mainRes,RDF('type'));
	var isSLTag = isTypeInTypes(SL('Tag'), types);
	var sonProps = [];
	var parentProps = [];
	var leafProps = [];
	var leafInvProps = [];
	var checkDisplayTriangleScript = null;

	var isCoreRDCType = false;
	var isDDDType = false;
	if (isSLTag) {
		// parentProps[0] = kb.sym("http://www.semanlink.net/2001/00/semanlink-schema#hasParent"); // @find SKOSIFY
		parentProps[0] = kb.sym("http://www.w3.org/2004/02/skos/core#broader");
		leafInvProps[0] = kb.sym("http://www.semanlink.net/2001/00/semanlink-schema#tag");

	} else {
		// @TODO
		// @VOCAB	
		/*
		var coreRDCTypes = [];
		coreRDCTypes[0] = kb.sym(vocab_getTreeElementType());
		coreRDCTypes[1] = kb.sym(vocab_getElementType());
		coreRDCTypes[2] = kb.sym(vocab_getPGType());
		
		for (var i=0; i < coreRDCTypes.length; i++) {
			if (isTypeInTypes(coreRDCTypes[i], types)) {
				isCoreRDCType = true;
				break;
			}
		}
		if (isCoreRDCType) {
			var treeDef = vocab_getRDCTreeDefinition(kb);
			sonProps = treeDef.sonProps;
			leafProps = treeDef.leafProps;
			// because servlet ensures that sons of
			// a TreeNode are included in kb
			checkDisplayTriangleScript = simpleCheckDisplayTriangleScript;
		} else {
			var dddTypes = [];
			dddTypes[0] = kb.sym(vocab_getDomainType());
			dddTypes[1] = kb.sym(vocab_getPrestationType());
			dddTypes[2] = kb.sym(vocab_getFunctionType());
			dddTypes[3] = kb.sym(vocab_getSymptomType());
			dddTypes[4] = kb.sym(vocab_getOrganType());
			dddTypes[5] = kb.sym(vocab_getSubFunctionType());
			dddTypes[6] = kb.sym(vocab_getDTCType());			
			for (var i=0; i < dddTypes.length; i++) {
				if (isTypeInTypes(dddTypes[i], types)) {				
					isDDDType = true;
					break;
				}
			}
			if (isDDDType) {
				var treeDef = vocab_getDDDTreeDefinition(kb);
				sonProps = treeDef.sonProps;
				leafProps = treeDef.leafProps;
				// because servlet ensures that sons of
				// a TreeNode are included in kb
				checkDisplayTriangleScript = simpleCheckDisplayTriangleScript;
			} else { // A VIRER
				sonProps[0] = kb.sym("http://www.w3.org/2004/02/skos/core#narrower");
				parentProps[0] = kb.sym("http://www.w3.org/2004/02/skos/core#broader");
			}
		}
		*/
		sonProps[0] = kb.sym("http://www.w3.org/2004/02/skos/core#narrower");
		parentProps[0] = kb.sym("http://www.w3.org/2004/02/skos/core#broader");
	}

	var x =  new TreeObject(kb, sonProps, parentProps, leafProps, leafInvProps, null, null, checkDisplayTriangleScript);
	x.isCoreRDCType = isCoreRDCType;
	x.isDDDType = isDDDType;
	return x;
}






/** if displayAllResInList, displays all the res in a list
    if mainResUri undocumented, it is supposed to be found in the rdf (already loaded from rdfUri). */
RDFDisplay.prototype.displayAllInContainer = function(kb, rdfUri, container, mainResUri, displayAllResInList) {
	// alert("rdfUri " + rdfUri + "\n" + "mainResUri " + mainResUri + " displayAllResInList " + displayAllResInList);
	var results = [];
	if (mainResUri && !displayAllResInList) {
		results[0] = kb.sym(mainResUri);
		displayRes(kb, results[0], container);
		return results;
	}
				
	if (!displayAllResInList) {
		// rdfUri gives information about a (probably non information-) resource
		// This main resource is linked to rdfUri by a statement (mainRes,isDefinedBy,rdfUri)
		// Normally, only one, but...
		// var mainRes = kb.any(undefined,RDFS('isDefinedBy'),kb.sym(rdfUri));
		var mainRess = kb.each(undefined,RDFS('isDefinedBy'),kb.sym(rdfUri));
		// alert(mainRess.length);
		results = mainRess;
		if (mainRess.length > 0) {
			for (var j=0; j<mainRess.length; j++) {
				var mainRes = mainRess[j];
				// List documents
				// insertDocsOfTag(kb, mainRes);
				displayRes(kb, mainRes, container);
			}
			return results;
		}
	}

		
	if (!displayAllResInList) {
		// cf sparql request OU deref de rdf sans mainRes declare ou trouvee
		
		// what are the res. We suppose the one which have a rdf:type
		
		// Problem is, in such a sparql request returning any kind of content, to decide
		// what are the resources to display.
		// Any subjects ?
		// I don't know how to use kb.each to quickly return the list of subjects
		// in kb.each(undefined), subjects are repeated (one by statement)
	
		
		// mainRess = kb.each(undefined,RDF('type'));
		
		// results = allRes;
		
		// This displays only the res that are not objects of any statement in kb
		// ("root subjects")
		var count = 0;			

		 // one more hack: in the case of a # uri, 
		if (rdfUri.indexOf("#") > -1) {
			// no filter on "root objects"
			// this displays the whole file
			// results = mainRess;
			
			// this displays only the res
			results[0] = kb.sym(rdfUri);
			displayRes(kb, results[0], container);
			return results;
		}
			
		var allRes = myEach(kb, undefined);
		for (var j=0; j<allRes.length; j++) {
			var mainRes = allRes[j];
			if (isRoot(kb, mainRes)) {
				results[count] = mainRes;
				count++;
			}
		}
	} else {
		var allRes = myEach(kb, undefined);
		results = allRes;
	}
			
			
			
			
			
	results.sort(resCompare); // I don't like it // @TODO (cf iso3166)
	for (var j=0; j<results.length; j++) {
		// alert(results[j]);
		displayResInList(kb, results[j], container);
	}
			
	
	return results;
}

/** true iff res not included in any <? ? res> statement */
isRoot = function(kb, res) {
	// if its a class, return true (hack pour le cas de la class Country dans le schema)
	// if (kb.each(res,RDF('type'),RDFS('Class')).length > 0) return true; // BEWARE: kb.any returns false, I don't know why. Bug tabulator?
    // the statements mainRes is object of:
    // var sts = kb.statementsMatching(undefined,undefined,mainRes);  // cf identity.js RDFIndexedFormula.prototype.statementsMatching = function(subj,pred,obj,why,justOne)
    // return (!
    if (kb.any(undefined,undefined,res)) return false;
    return true;
}



// @VOCAB
// on pourrait pas faire un truc genre:
// if res de type XXX et displayXXX (cad si la fct existe), alors l'appeler
// TODO TRY THAT
// OK, I try:
// displayT

displayResInList = function(kb, res, container) {
	var types = kb.each(res,RDF('type'));
	for (var i = 0 ; i < types.length ; i++) {
		var uri = types[i].uri;
		/* if (vocab_getMDDocumentType()) {
			if (uri == vocab_getMDDocumentType()) {
				displayDDDDocument(kb, res, container);
				return;
			} else if ( uri == vocab_getOrganType()) {
				displayOrgan(kb, res, container);
				return;
			} else if (uri == vocab_getMRDocumentType()) {
				displayMRDocument(kb, res, container);
				return;
			}
		} */
		if (isCountry(uri)) {
			displayCountry(kb, res, container);
			return;
		}
		
		var func = TYPE2METHOD[uri];
		if (func) {
			func(kb, res, container);
			return;
		}
	}
	defaultDisplayInList(kb, res, container);
}

defaultDisplayInList = function (kb, res, container) {
	var containingDiv;
	containingDiv = document.createElement("div");
 	containingDiv.className = "graybox";	

	// appendParagraphe(containingDiv, getLabel(kb, res), "h3");
	var label = getLabel(kb, res);
	var titleParag = document.createElement("h2");
	if (res.uri) { // cf anon res: no uri
		displayLinkToURI(res.uri, label, titleParag);
	} else {
		titleParag.appendChild(document.createTextNode(label));
	}
	container.appendChild(titleParag);
	someDisplay(kb, res, containingDiv);
	container.appendChild(containingDiv);	
}

// this is used in lists: no back links
someDisplay = function (kb, res, containingDiv) {
	if (res.uri) {
		var ul = document.createElement("ul");
		var li = document.createElement("li");
		li.appendChild(document.createTextNode("URI: " + res.uri));
		ul.appendChild(li);
		containingDiv.appendChild(ul);
	}
	
	displayMainResAsSubject(kb, res, containingDiv);
	// displayMainResAsObject(kb, res, containingDiv); // this is used in lists: no back links
}

displayCountry = function(kb, res, container) {
	var containingDiv;
	containingDiv = document.createElement("div");
 	containingDiv.className = "graybox";	

	var titleParag = document.createElement("h3");
	
	var labelPropsArray = [];
	labelPropsArray[0] = ISO_SH('name'); // kb.sym("http://downlode.org/rdf/iso-3166/schema#name");
	var label = kb.any(res,ISO_SH('alpha2')).toString() + ": " + getLabel(kb, res, labelPropsArray);
	// not really useful, because the link to anchor work only inside the page
	// (as it is generated after the page load) - at leats with ff
	/*var anchor = document.createElement("a");
	anchor.name = kb.any(res, kb.sym("http://downlode.org/rdf/iso-3166/schema#alpha_2"));
	titleParag.appendChild(anchor);
	anchor = document.createElement("a");
	anchor.name = kb.any(res, kb.sym("http://downlode.org/rdf/iso-3166/schema#alpha_3"));
	titleParag.appendChild(anchor);*/
	titleParag.appendChild(document.createTextNode(label));
	container.appendChild(titleParag);
	appendParagraphe(containingDiv, "URI: " + res.uri);
	displayMainResAsSubject(kb, res, containingDiv);
	displayMainResAsObject(kb, res, containingDiv);
	
	container.appendChild(containingDiv);
}



//displayOrgan = function(kb, res, container) {
//	var containingDiv;
//	containingDiv = document.createElement("div");
// 	containingDiv.className = "graybox";	
//
//	appendParagraphe(containingDiv, "ORGAN: " + getLabel(kb, res), "h3");
//	appendParagraphe(containingDiv, "URI: " + res.uri);
//	displayMainResAsSubject(kb, res, containingDiv);
//	
//	container.appendChild(containingDiv);
//}
//
//displayMRDocument = function(kb, res, container) {
//	// loadRDFAboutDialoDoc(kb, res); // this is not necessary with our describe sparql requests
//	var containingDiv;
//	containingDiv = document.createElement("div");
// 	containingDiv.className = "graybox";	
//
//	// appendParagraphe(containingDiv, "REPAIR METHOD: " + getLabel(kb, res), "h3");
//	var titleParag = document.createElement("h3");
//	titleParag.appendChild(document.createTextNode("REPAIR METHOD: "));
//	displayLinkToDialoXMLPage(kb, res, titleParag);
//	container.appendChild(titleParag);
//	
//	// appendParagraphe(containingDiv, "URI of xml file: " + res.uri);
//	// appendParagraphe(containingDiv, "URI of html page: " + htmlUri); // var htmlUri = displayLinkToDialoXMLPage(kb, res, titleParag);
//	
//	displayMainResAsSubject(kb, res, containingDiv);
//	
//	container.appendChild(containingDiv);
//}
//
//displayDDDDocument = function(kb, res, container) {
//	var containingDiv;
//	containingDiv = document.createElement("div");
// 	containingDiv.className = "graybox";	
//
//	// appendParagraphe(containingDiv, "DDD DOCUMENT: " + getLabel(kb, res), "h3");
//	var titleParag = document.createElement("h3");
//	titleParag.appendChild(document.createTextNode("DIAGNOSTIC METHOD: "));
//	displayLinkToDDDXMLPage(kb, res, titleParag);
//	container.appendChild(titleParag);
//	
//	//appendParagraphe(containingDiv, "URI: " + res.uri);
//	
//	displayMainResAsSubject(kb, res, containingDiv);
//	
//	container.appendChild(containingDiv);
//}


//
//
//

/**
 * returns the part of uri before the #
 */
function withoutEnd(uri) {
	var k = uri.indexOf('#');
	if (k > -1) {
		return uri.slice(0,k);
	} else {
		return uri;
	}
}

//
//
//

/**
 * Display one resource
 */
displayRes  = function(kb, mainRes, container) {
	//alert("displayRes: " + mainRes.uri);
	
	
	
	
	
	// DISPLAY A TITLE
	/*
	var mainResTitleDiv = document.createElement("div");
	mainResTitleDiv.setAttribute("class", "doctitle"); // ie 6 and 7 doesn't display the style
 	mainResTitleDiv.appendChild(document.createTextNode(getLabel(kb, mainRes)));
 	container.appendChild(mainResTitleDiv); */
 	appendParagraphe(container, getLabel(kb, mainRes), "h2");

	//
	var containingDiv;
	//	
	
	containingDiv = document.createElement("div");
	// containingDiv.setAttribute("class", "graybox"); // doesn't work with ie
 	containingDiv.className = "graybox";
 	
  	// appendParagraphe(containingDiv, "URI of resource: " + mainRes.uri);
	var ul = document.createElement("ul");
	var li = appendParagraphe(containingDiv, "URI of resource: " + mainRes.uri, "li");
	ul.appendChild(li);
	
  	li = appendParagraphe(containingDiv, "Type of resource", "li");
  	var types = kb.each(mainRes,RDF('type')); // @todo optim: this is computed several times. Store it
	displayValues(kb, types, li);	
	ul.appendChild(li);
  	containingDiv.appendChild(ul);
  	
	container.appendChild(containingDiv); 
	
	//
	
	containingDiv = document.createElement("div");
	// containingDiv.setAttribute("class", "graybox"); // doesn't work with ie
	containingDiv.className = "graybox";	
	
	


	

	//
	
	var treeObject = getTreeObject(kb, mainRes);
	// treeObject.displayAncestors(mainRes, container);
	var ancestors = treeObject.constructAncestorsDisplay(mainRes);
	// treeObject.displayTree(mainRes, container);
	var children = treeObject.constructDisplay(mainRes);
	if (children || ancestors) {
		if (treeObject.isCoreRDCType) {
	  		appendParagraphe(container, 'RDC Hierarchy' ,"h3");
	  	} else if (treeObject.isDDDType) {
	  		appendParagraphe(container, 'DDD Hierarchy' ,"h3");
		} else {
	  		appendParagraphe(container, 'Hierarchy' ,"h3");
		}
		if (ancestors) containingDiv.appendChild(ancestors);
		if (children) {
			// appendParagraphe(containingDiv, "Narrower term(s): ","p");
			containingDiv.appendChild(children);;
		}
		
	}
	
	//
	
	






	container.appendChild(containingDiv); 
	
	//
	
  	appendParagraphe(container, "Properties" ,"h3");
	
	//
	
 	containingDiv = document.createElement("div");
	// containingDiv.setAttribute("class", "graybox"); // doesn't work with ie
 	containingDiv.className = "graybox";	
    displayMainResAsSubject(kb, mainRes, containingDiv);
    displayMainResAsObject(kb, mainRes, containingDiv);
	container.appendChild(containingDiv);
	
	//
	// TEST TO LINK TO DIALOGYS
	//
	
	// dialogys(kb, mainRes, container);	
} // displayRes











//
//
//

/**
 * Display the statements  <mainRes,?,?>. */
displayMainResAsSubject  = function(kb, mainRes, container) {
  	// the statements mainRes is subject of:
    var sts = kb.statementsMatching(mainRes);  // cf identity.js RDFIndexedFormula.prototype.statementsMatching = function(subj,pred,obj,why,justOne)
   	// appendParagraphe(container, "<res,?,?>");
   	// props is the list of predicates used in statementList
	// we'll loop over them, and for each, list the values
	var props = predicatesInStatements(sts);
	displayPropsOfRes(kb, mainRes, props, container);
}

/** Display props of a res in an <ul> list
 *  @param props an array of properties
 */
displayPropsOfRes = function(kb, res, props, container) {
	var forms = '';
	var ul = '';
	var formsContainer = '';
	for (var i=0; i<props.length; i++) {
		var prop = props[i];
//		if (prop.uri == "http://www.lod.org/form#form") {
//			if (!formsContainer) formsContainer = document.createElement("div");
//			displayForm(kb, res, prop, formsContainer);
//		} else {
			if (!ul) ul = document.createElement("ul");
			var li = document.createElement("li");
			displayPropValues(kb, res, props[i], li);
			ul.appendChild(li);
//		}
	}
	if (ul) container.appendChild(ul);
	if (formsContainer) container.appendChild(formsContainer);
}

displayPropsOfResWithoutForms = function(kb, res, props, container) {
	var nprops = props.length;
	if (nprops > 0) {
		var ul = document.createElement("ul");
		for (var i=0; i<nprops; i++) {
			var li = document.createElement("li");
			displayPropValues(kb, res, props[i], li);
			ul.appendChild(li);
		}
		container.appendChild(ul);
	} // if (nprops > 0)
}

/**
 * Display values of one prop of a res */
displayPropValues = function(kb, res, prop, container) {
	container.appendChild(document.createTextNode(getLabel(kb, prop)));
	var objs = kb.each(res,prop,undefined);
	displayValues(kb, objs, container);
}


///**
// * Display a form associated to document res
// * @param res the document this form is associated with
// * @propForm "http://www.lod.org/form#form"
// */
//displayForm = function(kb, res, propForm, container) {
//	var htmlForm = document.createElement("form");
//	var vinProp = kb.sym("http://www.lod.org/form#vin");
//	// http://www.lod.org/form#unknownVariable
//	// the "unknown objects"
//	var mess = "This document may be irrelevant to vehicle depending on:";
//	appendParagraphe (htmlForm, mess, "p");
//	var forms = kb.each(res, propForm, undefined); // normally only one
//	var unknownObjProp = kb.sym("http://www.lod.org/form#unknownVariable");
//	var critProp = kb.sym("http://sicg.tpz.renault.fr/sw/2007/10/schema/lexic#crit"); // @TODO @find lexvocab
//	for (var i=0; i<forms.length; i++) {
//		var form = forms[i]; // the anon res representing the form
//		htmlForm.id = theEnd(form.toString(), ':');
//		var unknownObjs = kb.each(form, unknownObjProp, undefined);
//		var cont;
//		for (var i=0; i<unknownObjs.length; i++) {
//			cont = document.createElement("p");
//			cont.appendChild(document.createTextNode(getLabel(kb, unknownObjs[i])));
//			var crits = kb.each(unknownObjs[i], critProp, undefined);
//			var radioGroupName = unknownObjs[i].uri;
//			// var radioGroupName = theEnd(unknownObjs[i].uri,'/'); // Hmm, ATTENTION, suppose / uri, not # uri // TODO @find bvm/lexvocab dependent
//			for (var icrit=0; icrit < crits.length; icrit++) {
//				var label = document.createElement("label");
//				var critInput = document.createElement("input");
//				critInput.type = "radio";
//				critInput.name = radioGroupName;
//				critInput.value = crits[icrit].uri;
//				// following line added because of ie
//				critInput.onclick = clickRadio; // @find internet explorer pb with radio buttons
//				label.appendChild(critInput);
//				label.appendChild(document.createTextNode(theEnd(crits[icrit].uri,'_'))); // TODO @find bvm/lexvocab dependent
//				cont.appendChild(label);
//				// cont.appendChild(document.createElement("br"));
//			}
//			htmlForm.appendChild(cont);
//		}
//		
//		
//		var vin = kb.any(form, vinProp, undefined);
//		if ((vin) && (vin.uri)) {
//			var vinField = document.createElement("input");
//			vinField.type = "hidden";
//			vinField.name = "vin";
//			vinField.value = vin.uri;
//			htmlForm.appendChild(vinField);
//			
//			var btn = document.createElement("input");
//			btn.type = "submit";
//			btn.value = "OK";
//			btn.onclick = sendSparqlUpdate;
//			htmlForm.appendChild(btn);
//		} else {
//			// no vin: anonymous res - case of a request with an anonymous veh description
//			// We have to set the action of the form to the quenry that was sent,
//			// including the veh description it had, and adding to that description depending on what the user choose in this form
//			
//			// TODO!!!!!
//		}
//	}
//	var graybox = document.createElement("div");
//	// graybox.setAttribute("class", "graybox"); // doesn't work with ie
// 	graybox.className = "graybox";	
//	graybox.appendChild(htmlForm);
//	container.appendChild(graybox);
//	
//	// We use a hidden form to actually submit the query
//	// This form is shared by all the visibles forms: we create only one such form
//	var submitForm = document.getElementById('sparulSubmitForm');
//	if (!submitForm) {
//		submitForm = document.createElement("form");
//		submitForm.id = "sparulSubmitForm";
//		submitForm.action = getContextURL() + "/update/";
//		submitForm.method = "post";
//		var hiddenInput = document.createElement("textarea");
//		hiddenInput.name = "query";
//		// following line added, because document.getElementById('sparulSubmitForm').query.value doesn't seem to work with that fucking Internet Explorer
//		// (at least ie 6 in the context of forms generated by script)
//		hiddenInput.id = "sparulSubmitForm_Query" // @find internet explorer problem with form.name.value
//		submitForm.appendChild(hiddenInput);
//		submitForm.style.display = 'none';
//		container.appendChild(submitForm);
//	}
//}

//sendSparqlUpdate = function(event) {
//	// this is the clicked btn
//	var htmlForm = this.form;
//	// following line needed for ie patch:
//	var vinUri = ""; // @find internet explorer problem with form.name.value
//	// we first list the variables to be set, with corresponding values
//	// (normally, "renault objects" and corresponding criteria)
//	var vars = [];
//	var vals = [];
//	var k = 0;
//	for (var i = 0; i < htmlForm.length; i++) {
//		var elt = htmlForm.elements[i];
//		if (elt.type == 'radio') {
//			if (elt.checked) {
//				vars[k] = elt.name;
//				vals[k] = elt.value;
//				k++;
//			}
//		} else {
//			// this needed for ie patch @find internet explorer problem with form.name.value
//			if (elt.name == "vin") vinUri = elt.value;
//		}
//	}
//	// loop over the variables to be set to compute the SPARQL request string
//	if (vars.length > 0) {
//		// @find internet explorer problem with form.name.value
//		// following line doesn't seem to work with ie 6
//		// var vinUri = htmlForm.vin.value;
//		var dec = decomposeUri(vinUri);
//		var vinNS = dec[0];
//		var shortVin = dec[1];
//		dec = decomposeUri(vars[0]);
//		var lexNS = dec[0];
//		
//		var sparul = "PREFIX vin: <" + vinNS + ">\n"
//			+ "PREFIX lex: <" + lexNS + ">\n"
//			+ "INSERT DATA { \n"; 
//		for (var i = 0; i < vars.length; i++) {
//			/*
//			sparul = sparul 
//				+ "<" + vinUri + "> "
//				+ "<" + vars[i] + "> "
//				+ "<" + vals[i] + "> .\n";
//			*/
//			sparul = sparul
//				+ "vin:" + shortVin
//				+ " " + n3uri("lex", lexNS, vars[i])
//				+ " " + n3uri("lex", lexNS, vals[i])
//				+ " .\n";
//		}
//		sparul = sparul + "}";
//		var submitForm = document.getElementById('sparulSubmitForm');
//		// following line doesn't seem to wotk with internet explorer 6
//		// submitForm.query.value = sparul;
//		document.getElementById('sparulSubmitForm_Query').value = sparul; // @find internet explorer problem with form.name.value
//		submitForm.submit();
//	}
//	return false;
//}



/** Return either <uri> or prefixName:endOfUri if uri startsWith prefixValue */
n3uri = function(prefixName, prefixValue, uri) {
	var k = startsWith(uri, prefixValue);
	if (k) {
		return prefixName + ":" + uri.slice(k);
	} else {
		return "<" + uri + ">";
	}
}

decomposeUri = function (uri) {
	var k = uri.lastIndexOf('#');
	if (k < 0) k = uri.lastIndexOf('/');
	var x = [];
	x[0] = uri.slice(0,k+1);
	x[1] = uri.slice(k+1);
	return x;
}


function theEnd(s, aChar) {
	var k = s.lastIndexOf(aChar);
	if (k > -1) {
		return s.slice(k+1);
	} else {
		return s;
	}
}

displayValues = function(kb, objs, container) {
	var nobjs = objs.length;
	if (nobjs > 1) {
		// presentation as a list
		var ul = document.createElement("ul");
		objs.sort(nodeCompare);
		for (var j=0; j<nobjs; j++) {
			var obj = objs[j];
			var li = document.createElement("li");
			ul.appendChild(li);
			displayPropValue(kb, obj, li, true);
		}
		container.appendChild(ul);
	} else if (nobjs == 1) { // single value
		container.appendChild(document.createTextNode(" : "));
		displayPropValue(kb, objs[0], container, false);
	}
}

/**
 * Display the statements  <?,?,mainRes>. */
displayMainResAsObject  = function(kb, mainRes, container) {
    // the statements mainRes is object of:
    var sts = kb.statementsMatching(undefined,undefined,mainRes);  // cf identity.js RDFIndexedFormula.prototype.statementsMatching = function(subj,pred,obj,why,justOne)
	// props is the list of predicates used in statementList
	// we'll loop over them, and for each, list the values
	var props = predicatesInStatements(sts);
	displayPropsWithRes(kb, mainRes, props, container);
}

/** Display props with a res as object in an <ul> list
 *  @param props an array of properties
 */
displayPropsWithRes  = function(kb, mainRes, props, container) {
	var nprops = props.length;
	if (nprops > 0) {
		var ul = document.createElement("ul");
		for (var i=0; i<nprops; i++) {
			var prop = props[i];
			var li = document.createElement("li");
			ul.appendChild(li);
			li.appendChild(document.createTextNode("is " + getLabel(kb, prop) + " of "));
			
			var subjs = kb.each(undefined,prop,mainRes);
			var nsubjs = subjs.length;
			if (nsubjs > 1) {
				// presentation as list
				var ul2 = document.createElement("ul");
				li.appendChild(ul2);
				subjs.sort(resCompare);
				for (var j=0; j<nsubjs; j++) {
					var subj = subjs[j];
					var li2 = document.createElement("li");
					ul2.appendChild(li2);
					if (subj.uri) displayLinkToRes(kb, subj, li2);
					else li2.appendChild(document.createTextNode(getLabel(kb, subj))); // cf anon res
				}
			} else { // single value
				li.appendChild(document.createTextNode(" : "));
				// displayLinkToRes(kb, subjs[0], li);
				var subj = subjs[0];
				if (subj.uri) displayLinkToRes(kb, subj, li);
				else li.appendChild(document.createTextNode(getLabel(kb, subj))); // cf anon res
			}
		}
		container.appendChild(ul);
	} // if (nprops > 0)
}


/**
 * returns the predicates used in a list of statements */
predicatesInStatements = function (statementList) {
	// testPush();
    // we want to compute the set of predicates involved in the statements of statementList
    // I don't know js. Anyway, here is a way to do it // @TODO MAKE IT BETTER
    var x = [];
	var set = []; // an associative array, but used just as a java set
	var prop;
    var i, n=statementList.length;
	for (i=0; i<n; i++) {
		prop = statementList[i].predicate;
		if (typeof set[prop] == 'undefined') {
			set[prop] = prop; // put anything in it: doesn't use, just to know the prop already added to x
			x.push(prop);
		}
	}
	return x;
}

/*
getLabel = function(kb, res) {
	var xs = kb.each(res,RDFS('label'));
	var x = null;
	if (xs.length == 0) {
		xs = kb.each(res,DC('title'));
	}
	if (xs.length == 0) {
		x = res.uri;
		if ((!x) || (x == "undefined"))  x = res;
	} else {
		// return x[0];
		var xInLang = getNode4Lang(xs, GLOBAL_lang);
		if (xInLang == '') {
			x = xs[0];
		} else {
			x = xInLang;
		}
	}
	//alert("getLabel of " + res.uri + " : " + x);
	return x;
}
*/

getLabel = function(kb, res, labelPropsArray) {
	if (!labelPropsArray) { // @TODO optim
		/*labelPropsArray = [];
		labelPropsArray[0] = RDFS('label');
		labelPropsArray[1] = DC('title');*/
		labelPropsArray = getLabelPropsArray(kb, res);
	}
	var x = null;
	var xs = [];
	for (var i = 0; i < labelPropsArray.length; i++) {
		xs = kb.each(res,labelPropsArray[i]);
		if (xs.length > 0) break;
	}
	if (xs.length == 0) {
		x = res.uri;
		if ((!x) || (x == "undefined"))  x = res;
	} else {
		// return x[0];
		if (!GLOBAL_lang) GLOBAL_lang = "fr";
		var xInLang = getNode4Lang(xs, GLOBAL_lang);
		if (xInLang == '') {
			x = xs[0];
		} else {
			x = xInLang;
		}
	}
	// alert("getLabel of " + res.uri + " : " + x);
	return x;
}

// @TODO optim
getLabelPropsArray = function(kb, res) {
	var labelPropsArray = [];
	var types = kb.each(res,RDF('type'));
	for (var i = 0 ; i < types.length ; i++) {
		var uri = types[i].uri;
		/*if (uri == vocab_getMDDocumentType()) {
			displayDDDDocument(kb, res, container);
			return;
		} else if ( uri == vocab_getOrganType()) {
			displayOrgan(kb, res, container);
			return;
		} else if (uri == vocab_getMRDocumentType()) {
			displayMRDocument(kb, res, container);
			return;
		} else */ if (isCountry(uri)) {
			labelPropsArray[0] = ISO_SH('name'); // kb.sym("http://downlode.org/rdf/iso-3166/schema#name");
			return labelPropsArray;
		}
	}
	labelPropsArray[0] = RDFS('label');
	labelPropsArray[1] = DC('title');
	return labelPropsArray;
}

/** Return the first node of nodeArray (a result of kb.each) that is in lang. */
getNode4Lang = function (nodeArray, lang) {
	for (var j=0; j<nodeArray.length; j++) {
		var node = nodeArray[j];
		if (node.lang == lang) return node;
	} 
	return '';
}

//
// UTILS
//

appendParagraphe = function(container, text, paragrapheKind) {
	if (!paragrapheKind) paragrapheKind = "p"
	var x = document.createElement(paragrapheKind);
	x.appendChild(document.createTextNode(text));
	container.appendChild(x);
	return x;
}

/** return starts.length if s starts with start, else return null; */
function startsWith(s, start) {
	var l = start.length;
	if (s.slice(0,l) == start) {
		return l;
	} else {
		return null;
	}
}

function endsWith(s, end) {
	var ls = s.length;
	var le = end.length;
	if (ls < le) return false;
	if (s.slice(ls-le) == end) {
		return true;
	} else {
		return false;
	}
}

//

function isTypeInTypes(type, types) {
	for (var i = 0 ; i < types.length ; i++) {
		if (types[i].uri == type.uri) {
			return true;
		}
	}
	return false;
}

//

// HAS BEEN USED TO DEBUG IE PB
/** create an hypertext link that loads the rdf at a given url. 
    Returns the dom element. */
// use this param to load information about whatUri
// var sparql = getContextURL() + "/sparql/?prefixes=%0D%0A%09%09&query=DESCRIBE+%3C" + encodeURIComponent(whatUri) + "%3E";
function createDownloadLink(url) {
	var x = document.createElement("a");
	var onclick = 'GLOBAL_kb.load("' + url + '",true);';
	x.setAttribute("href", "#");
	x.setAttribute("onclick", onclick);
	x.appendChild(document.createTextNode("."));
	return x;
}

/** each in match.js states: Only one of s p o can be undefined
 * We want to remove this limitation. */
myEach = function(kb,s,p,o,w) {
    var results = []
    var st, sts = kb.statementsMatching(s,p,o,w)
    var i, n=sts.length
    if (typeof s == 'undefined') {
	for (i=0; i<n; i++) {st=sts[i]; add2set(results, st.subject)}
    } else if (typeof p == 'undefined') {
	for (i=0; i<n; i++) {st=sts[i]; add2set(results, st.predicate)}
    } else if (typeof o == 'undefined') {
	for (i=0; i<n; i++) {st=sts[i]; add2set(results, st.object)}
    } else if (typeof w == 'undefined') {
	for (i=0; i<n; i++) {st=sts[i]; add2set(results, st.why)}
    }
    return results
}

/** push elt to the array called set iff it's not already there. */
add2set = function(set, elt) {
	// There are probably better ways to do that
	// (Howto to make set in javascript?) // @TODO
	for (var i = 0; i < set.length; i++) {
		// if (set[i] == elt) return; // no!
		if (set[i].uri == elt.uri) return;
	}
	set.push(elt);
}


//
//
//

displayPropValue = function(kb, rdfNode, container, showLangInCaseOfText) {
	if (rdfNode.uri == undefined) {
		if (showLangInCaseOfText) {
			container.appendChild(document.createTextNode(rdfNode.lang + " : " + rdfNode));
		} else {
			container.appendChild(document.createTextNode(rdfNode));
		}
	} else {
		displayLinkToRes(kb, rdfNode, container);
	}
}

/*
displayURI = function(kb, uri, container) {
	var lien = document.createElement("a");
	lien.setAttribute("href", linkTo(uri)); //@find "using /get/?uri=" for links to uri
	
	lien.appendChild(document.createTextNode(getLabel(kb,kb.sym(uri))));
	container.appendChild(lien);
}
*/

// beware, doesn't work with anon res
displayLinkToRes = function(kb, res, container) { // would be a good idea to pass the property: we may want to change the form of the link depending on it
	var lien = document.createElement("a");
	lien.setAttribute("href", linkTo(res.uri)); //@find "using /get/?uri=" for links to uri
	
	lien.appendChild(document.createTextNode(getLabel(kb,res)));
	container.appendChild(lien);
}

displayLinkToURI = function(uri, label, container) {
	var lien = document.createElement("a");
	lien.setAttribute("href", linkTo(uri));

	if (!label) label = uri;
	lien.appendChild(document.createTextNode(label));
	container.appendChild(lien);
}

// TODO // A REVOIR
//@find "using /get/?uri=" for links to uri
/**
 * Return the uri to be used to link to uri
 * (direct connection to uri is possible only when it is inside getContextURL(). For others
 * uri, we use the LODServlet that acts as an "HTTP proxy") 
 */
function linkTo(uri) {
	var context = getContextURL();
	if (startsWith(uri,context)) {
		// if (uri.indexOf("#") < 0) return uri; /////////////////
		return uri; 
	}
	
	// ceci ne va pas avec ds du rdf, un lien vers du rdf (ex : tag ds une page sl)
	// return context + "/get/?uri=" + encodeURIComponent(uri);	
	// ceci va pour les tags sl. Mais pas pour un lien vers une page html !!!
	// return context + "/rdf2html/?uri=" + encodeURIComponent(uri);
	// return context + "/htmlget/?uri=" + encodeURIComponent(withoutEnd(uri));		
	// return context + "/htmlget/?uri=" + encodeURIComponent(uri);		
	return lod_linkToHtml(uri); // 2010-07
}


// tree new way
/**
 * @param uri that points to some rdf, at least when dereferenced with correct accept header
 */
function linkToRdf(uri) {
				// If uri is a nir that belongs to this dataset, we could think of just returning uri.
				// if we get uri, because of content negociation, we receive html
				// (why? we should set the accept header in js, I suppose. Anyway,
				// kb.load doesn't attempt to do so)
				// So, if we just return "uri", we get html instead of rdf
				// A HACK is to use the fact that we know how we mint uris in our dataset
				// This has an advantage: we directly get the rdf (no 303 redirect, "no httprange-14 roundtrip")
				
				// WE COULD ALSO, and it probably would be better,
				// only use the servletpath getrdf, and handle that in the servlet // TODO
	var context = getContextURL();
	if (startsWith(uri,context)) {
		// don't add .rdf in case of # uri Can happen??? Don't know
		// if ((!(endsWith(uri, ".rdf"))) && (uri.indexOf("#")<0)) uri = uri + ".rdf";
		if (!(endsWith(uri, ".rdf"))) uri = uri + ".rdf";
		return uri;
	} else {
		// ceci ne va pas avec, ds du rdf, un lien vers du rdf (ex : tag ds une page sl)
		// return context + "/get/?uri=" + encodeURIComponent(uri);	
		// ceci va pour les tags sl. Mais pas pour un lien vers une page html !!!
		// return context + "/rdf2html/?uri=" + encodeURIComponent(uri);
		// return context + "/getrdf/?uri=" + encodeURIComponent(uri);	
		return lod_linkToRdf(uri); // 2010-07
	}
}

//
// SORT
//

// @TODO optim
resCompare = function(res1, res2, kb) {
	if (!kb) kb = GLOBAL_kb;
	var s1 = getResSortOrder(kb, res1);
	var s2 = getResSortOrder(kb, res2);
	if (s1 < s2) return -1;
	else if (s1 > s2) return 1;
	else return 0;
}

// @TODO optim
getResSortOrder = function(kb, res) {
	var types = kb.each(res,RDF('type'));
	for (var i = 0 ; i < types.length ; i++) {
		var uri = types[i].uri;
//		/*if (uri == vocab_getMDDocumentType()) {
//			displayDDDDocument(kb, res, container);
//			return;
//		} else if ( uri == vocab_getOrganType()) {
//			displayOrgan(kb, res, container);
//			return;
//		} else if (uri == vocab_getMRDocumentType()) {
//			displayMRDocument(kb, res, container);
//			return;
//		} else */ 
		if (isCountry(uri)) {
			var x = kb.any(res,ISO_SH('alpha2')); // only for the "true" iso3166 servlet
			if (x) return x.toString();
		}
	}
	return getLabel(kb, res).toString().toLowerCase();
}

isCountry = function(typeUri) {
	// return (typeUri.indexOf("#Country") > -1);
	return (ISO_SH('Country').toString() == typeUri);
}

nodeCompare = function(node1, node2, kb) {
	if (!kb) kb = GLOBAL_kb;
	var s1 = toComparableString(node1, kb);
	var s2 = toComparableString(node2, kb);
	if (s1 < s2) return -1;
	else if (s1 > s2) return 1;
	else return 0;
}

toComparableString = function(rdfNode, kb) {
	var x;
	if (rdfNode.uri == undefined) {
		x = rdfNode.lang + " : " + rdfNode.toString().toLowerCase();
	} else {
		if (!kb) kb = GLOBAL_kb;
		x = getLabel(kb, rdfNode).toString().toLowerCase();
	}
	return x;
}

//
// DIALOGYS
//


//loadRDFAboutDialoDoc = function (kb , dialoDoc) {
//		// describe les res qui ont pour element mainRes (cad, describe les doc dialo correspondants a elementRes)
//		// On n'en a pas besoin, car elles sont deja dans kb
//		// var sparql = getContextURL() + "/sparql/?prefixes=&q=DESCRIBE+%3Fs+WHERE+%7B%0D%0A%09%3Fs+%3Chttp%3A%2F%2Fsicg.tpz.renault.fr%2Fdialogys%2Fschema%23element%3E+%3C" + encodeURIComponent(elementRes.uri) + "%3E%0D%0A%7D%09%09";
// 		
// 		// Request to describe dialoDoc (data about dialoDoc not included in kb)
// 		var sparql = getContextURL() + "/sparql/?prefixes=%0D%0A%09%09&query=DESCRIBE+%3C" + encodeURIComponent(dialoDoc.uri) + "%3E";
// 		kb.load(sparql,true, true); // the second "true" is to prevent internet explorer to raise an alert when it's not able to loadXML
//}
//
//
//applicabilityOfDialogysDoc = function(kb, dialoDoc, container) {
//		var applicabilityProp = kb.sym(vocab_getMTCProp());
//		var mtcs = kb.each(dialoDoc,applicabilityProp);
//		if (mtcs.length > 0) {
//			var mtcul = document.createElement("ul");
//			for (var j=0; j<mtcs.length; j++) {
//				var li = document.createElement("li");
//				li.appendChild(document.createTextNode(mtcs[j]));
//				mtcul.appendChild(li);
//			}
//			container.appendChild(mtcul);
//		}		
//}
//
//displayMTC = function(kb, mtcs, container) {
//	if (mtcs.length > 0) {
//		var mtcul = document.createElement("ul");
//		for (var j=0; j<mtcs.length; j++) {
//			var li = document.createElement("li");
//			li.appendChild(document.createTextNode(mtcs[j]));
//			mtcul.appendChild(li);
//		}
//		container.appendChild(mtcul);
//	}
//}
//
///** display a link to a dialogys xml page, and return the corresponding uri. */
//// we suppose here the xml files are served by this servlet
//displayLinkToDialoXMLPage = function(kb, dialoDoc, container) { // would be a good idea to pass the property: we may want to change the form of the link depending on it	/*
//	// this displays the link to the html document
//	var x = getContextURL() + "/xml2html/?uri=" + encodeURIComponent(dialoDoc.uri);
//	var lien = document.createElement("a");
//	lien.setAttribute("href", x);
//	
//	lien.appendChild(document.createTextNode(getLabel(kb,dialoDoc)));
//	container.appendChild(lien);
//	
//	/* DEBUG PB IE
//	// ceci crée un lien qui dit : charger les infos sur dialo doc
//	var lien2 = document.createElement("a");
//	var sparql = getContextURL() + "/sparql/?prefixes=%0D%0A%09%09&query=DESCRIBE+%3C" + encodeURIComponent(dialoDoc.uri) + "%3E";
//	// var onclick = 'GLOBAL_kb.load(' + sparql + ',true);return false;';
//	var onclick = 'GLOBAL_kb.load("' + sparql + '",true);';
//	lien2.setAttribute("href", "#");
//	lien2.setAttribute("onclick", onclick);
//	lien2.appendChild(document.createTextNode("."));
//	container.appendChild(lien2);
//	// DEBUG PB IE FIN */
//	
//	return x;
//}
//
///** display a link to a dialogys xml page, and return the corresponding uri. */
//// we suppose here the xml files are served by this servlet
//displayLinkToDDDXMLPage = function(kb, dddDoc, container) { // would be a good idea to pass the property: we may want to change the form of the link depending on it
//	var path = kb.any(dddDoc,kb.sym(vocab_getPathProp()));
//	var x = getContextURL() + "/xml2html/?path=" + encodeURIComponent(path);
//	var lien = document.createElement("a");
//	lien.setAttribute("href", x);
//	
//	
//	lien.appendChild(document.createTextNode(getLabel(kb,dddDoc)));
//	container.appendChild(lien);
//	return x;
//}
//
//
//
///*
//// we suppose here the xml files are served by this servlet
//displayLinkToDialoXMLPage = function(kb, dialoDoc, container) { // would be a good idea to pass the property: we may want to change the form of the link depending on it
//	var lien = document.createElement("a");
//	lien.setAttribute("href", getContextURL() + "/xml2html/?uri=" + encodeURIComponent(dialoDoc.uri));
//	
//	lien.appendChild(document.createTextNode(getLabel(kb,dialoDoc)));
//	container.appendChild(lien);
//}
//*/
//
//displayLinkToVINFilteringService = function(kb, mainRes, vin, container) { // would be a good idea to pass the property: we may want to change the form of the link depending on it
//	var lien = document.createElement("a");
//	lien.setAttribute("href", mainRes.uri+"?vin=" + vin);
//	
//	lien.appendChild(document.createTextNode(vin));
//	container.appendChild(lien);
//}

/**
 * Internet Explorer patch
 * I don't understand why, but radio buttons in a form generated by script do not work with that f. ie 6 (at least) 
 * (they do not turn checked when you click them)
 * Hence a call to this script, that emulates this standard behaviour 
 * @find internet explorer pb with radio buttons */
clickRadio = function(event) {
	for (var i = 0; i < this.form.length; i++) {
		if (this.form.elements[i].name == this.name) this.form.elements[i].checked = false;
	}
	this.checked = true;
}

