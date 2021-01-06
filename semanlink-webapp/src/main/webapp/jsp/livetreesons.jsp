<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"
%><%/**	jsp to be used by the "livetree", a list of sons.	Attention, il n'y a pas ici de tag <ul>, juste les lignes <li>	(le ul de premier niveau est mis par livesearchxml.jsp, ceux de niveau plus bas sont mis ici,	au sein de la <li>)		uses :	- trees.js : livetree behaviour	- css style livetree		params : 
	- request.getAttribute("livesearchxml") true si livesearch.xml (non doc sinon)
	(needed to handle special action defined by pulldown menu "add parent, etc." when editing)
	- request.getParamater"postTagOnClick") true iff we need to handle special action defined by pulldown menu "add parent, etc.")
		- request.getAttribute("livetreelist") : the list to be displayed	- OU BIEN request.getAttribute("kw") : kw dont on affiche les enfants
	
	- request.getAttribute("resolveAlias")
	si non doc,
		- defauts to true if request.getAttribute("livetreelist") != null
		- default to false if request.getAttribute("livetreelist") == null		- request.getParameter("divid") identifies the id of parent (for instance 1_2)	- OU BIEN request.getAttribute("divid");	- OU BIEN null
	
	request.getParameter("withdocs") "true" ou "false" 
	ou request.getAttribute("withdocs") Boolean.TRUE ou autre chose pour false		STYLE DES LIGNES	jusqu'au support de l'inclusion des docs,	n'utilisait que la class "livetree" dans le ul, dont le a recopiait le style des kws.	Mais avec les docs, on a besoin d'avoir le style au niveau 	de la ligne, d'où, pour les lignes de docs, de l'ajout au sein de la li	d'un <span class="docli">.*/

// System.out.println("livetreesons.jsp targeturi" + request.getParameter("targeturi")); // DEBUG 2020
			boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));// soit on a une liste ds un attribut "livetreelist",// soit un kw dont on prendra les children// (passer une liste est nécessaire pour le résultat du livesearch par ex -- en fait,// pour la liste de premier niveau s'il ne s'agit pas simplement d'un seul kwList children = (List)  request.getAttribute("livetreelist");boolean resolveAlias = false;
Boolean boo = (Boolean) request.getAttribute("resolveAlias");
if (boo != null) {
	resolveAlias = boo.booleanValue();
} else {
	resolveAlias = (children != null); // je crois (le 15 aout 2005) qu'on n'a besoin de ne résoudre les alias que dans ce cas
	//c'est en effet seulement au 1er niveau d'un résultat de livesearch (et pas dans les cas de fils de kws)
}
SLKeyword kw = null; // sera mis à non null si on a affaire à un kw qu'on vient de cliquerif (children == null) {	kw = (SLKeyword) request.getAttribute("kw");	children = kw.getChildren();}// ce qui identifie le block (cf toggle/trigger)String parentDivId = request.getParameter("divid");if (parentDivId == null) parentDivId = (String) request.getAttribute("divid"); // pour tree ds la page kwif (parentDivId == null) {		parentDivId = "";} else {	parentDivId += "_";}// avec ou sans les docsboolean withDocs;if ("true".equals(request.getParameter("withdocs"))) {	withDocs = true;} else {	withDocs = Boolean.TRUE.equals(request.getAttribute("withdocs"));}
// this is true if we do not want to have docs on first level
// When it's true, we remove this attribute from request (to let docs displayed on inner levels)
boolean withdocs_notonfirstlevel = (Boolean.TRUE.equals(request.getAttribute("withdocs_notonfirstlevel")));
if (withdocs_notonfirstlevel) {
	request.removeAttribute("withdocs_notonfirstlevel");
}

// should a click on tag send a get or a post? (cf pulldown menu when editing)
boolean postTagOnClick = false;
// si on est ds les résultats de la searchform, il faut mettre le onClick postTag pour faire ce qui est demandé par le popup (add parent, add chiild, ectc)
// si non, (ex : keyword sous forme de tree, ou snip), il faut faire le href standard
if (edit) {
	if (request.getAttribute("livesearchxml") != null) {
		postTagOnClick = true;
	} else if ("true".equals(request.getParameter("postTagOnClick"))) {
		postTagOnClick = true;
	}
}

// NON : quand on est appelé lors ouverture d'un triangle, on n'a pas le "snip"// String context = request.getContextPath();// String snip = request.getParameter("snip");
// if (snip != null) {
//     context = SLServlet.getServletUrl();    
// }
// System.out.println("livetreesons.jsp " + context);
String context = SLServlet.getServletUrl(); 

// 2020-02
// COMMENT SAVOIR QU'ON EST SUR UNE PAGE KW ???
// String displayedKW = (String) request.getAttribute("displayedKW");
// SLKeyword displayedKW = (SLKeyword) request.getAttribute("displayedKW");
String targetUri = request.getParameter("targeturi");
if (targetUri == null) {
	targetUri = (String) request.getAttribute("targeturi");
} else {
	request.setAttribute("targeturi", targetUri);
}

for (int i = 0; i < children.size(); i++) {	// We check whether a son has children or not, (or eventually a doc) in order to display correctly the "open" image	// Calculating the complete grandChildren is a waste of time:	// enough to know whether there is one or not.	SLKeyword son = (SLKeyword) children.get(i);	String label = son.getLabel();	String sonUri = son.getURI();	String encodedSonUri = java.net.URLEncoder.encode(sonUri,"UTF-8");	String sonDivId = parentDivId + Integer.toString(i);									boolean canBeOpened = (son.hasChild());	if ((withDocs) && (!canBeOpened)) {		canBeOpened = son.hasDocument();	}	
	// public static HTML_Link linkToAndKws(SLKeyword firstKw, SLKeyword[] otherKws, String label) throws UnsupportedEncodingException 
	// attention : se pose un pb avec les alias qui ne sont pas résolus.	// On ne va tout de même pas les résoudre tous : il suffit de résoudre celui sur lequel on cliquera	// String href = HTML_Link.getTagHref(request.getContextPath(), sonUri, resolveAlias); // /semanlink/tag/... 	// 2013-09 RDFa
	// 2020-02 (?)
	
	
	// THIS IS OK NO AND
	// String href = HTML_Link.getTagURL(context, sonUri, resolveAlias, null); // RDFa I don't like it: we lose the direct link to the html (cf null arg) // TODO
	
	
	// 2020-02 : switch TagAndTag ON/OFF
	// Ca pourrait se faire ici simplement avec une variable passée en plus
	
	// (OU BIEN : là où on document targetUri)
	
	// MAIS ce serait un peu primaire : faudrait recharger tout pour passer de l'un à l'autre
	// Alors qu'il devrait être possible de le faire via un lien sous forme de script
	// (avec pour param le tag trouvé + un boolean onOrOff)
	
	// Ce script, il faut le mettre ICI : changer le HTML_Link.tagsAndTagHref(targetUri, sonUri)
	// ET HTML_Link.getTagURL pour appel à ce script 
	// POUR DOCUMENTATION DE onClick
	
	String href = null;
	boolean itsAnAndKW = false;
	if (!edit) { // "and kws" in search // 2020-02 tagAndTag
		if (targetUri != null) {
			if (targetUri.contains("/tag/")
			    || (targetUri.startsWith(SLServlet.getSLModel().getDefaultThesaurus().getURI()))) { // hum, cf sicg thesaurus // TODO(?)

				itsAnAndKW = true;
				
				// link to AND kws // ATTENTION ICI ON SUPPOSE QUE targetUri est en tag/
				
// 			    String[] otherKws = new String[1]; otherKws[0] = sonUri;
// 			    // this, using andkws?
// 			    // HTML_Link link = HTML_Link.linkToAndKws(targetUri, otherKws, label);
// 			    // href = request.getContextPath() + link.getPage();
			    


				// 2020-03
                // TagAndTag : c'est ici qu'on constitue le lien dans l'arbre résultat de recherche
                // quand on est sur une page kw (ou and de kw)
                // targetUri : en gros, la page courante (+ précisément, ce dont on a besoin pour faire le AND kw:
                // uri de kw si page kw, url de la page and si on est déjà sur une page and)
			    href = HTML_Link.tagsAndTagHref(context, targetUri, sonUri);
			}
		}
	}
	if (!itsAnAndKW) {
        href = HTML_Link.getTagURL(context, sonUri, resolveAlias, null); // RDFa I don't like it: we lose the direct link to the html (cf null arg) // TODO		
	}
	
	
		// attention à l'éventuel URL rewriting !	href = response.encodeURL(href);	String onClick = "";
	if (postTagOnClick) {
		// si on est ds les résultats de la searchform, il faut mettre le onClick postTag pour faire ce qui est demandé par le popup (add parent, add child, etc)
		// si non, (ex : keyword sous forme de tree, ou snip), il faut faire le href standard
		onClick = " onClick=\"postTag('" + href + "');return false\"";
	}    // 2017-01 DRAG TEST: <li  draggable="true" ondragstart="drag(event)" >
	
	// BEWARE, what gives the id of the line is associated to an image with id="trigger:id" 
	// -- neccasary even there is no event (script)
	// (it ws not possible to have the id associated with li, because we use it for highlight
	// in displaying the livesearch results, cf id LSHighlight)
	
    if (!canBeOpened) {
	      // ATTENTION A NE PAS INTRODUIRE DE TEXTE "VIDE" ENTRE LES DIFFERENTS ELEMENTS
	      // cas sans image : une image vide pour avoir le même nb de fils ds les 2 cas 
	      // et porter l'id de ligne (mise en fait sur l'image, parce que l'id de la ligne sert
	      // poir le highlight
	      // avoir aussi le trigger:divid qui sert à se repérer ds le parcours de l'arbre.
	      
	      if (kw == null) {
	          %><%//PAS DE VIDE!!!%><li><img src="<%=context%>/ims/box_nada.gif" height="0" width="8" alt="" id="trigger:<%=sonDivId%>" /><a href="<%=href%>"<%=onClick%>><%=label%></a></li><%//PAS DE VIDE!!!%><%
	      } else { // 2013-08 RDFa
	          %><%//PAS DE VIDE!!!%><li><img src="<%=context%>/ims/box_nada.gif" height="0" width="8" alt="" id="trigger:<%=sonDivId%>" /><a property="skos:narrower" href="<%=href%>"<%=onClick%>><%=label%></a></li><%//PAS DE VIDE!!!%><%          
	      }
	} else { // son has sons
	      if (kw == null) {
	          %><%//PAS DE VIDE!!!%><li><img src="<%=context%>/ims/box_closed.gif" id="trigger:<%=sonDivId%>" alt="" height="8" width="8" onclick="toggle2('<%=sonDivId%>', '<%=encodedSonUri%>', '<%=withDocs%>', '<%=postTagOnClick%>', '<%=targetUri%>')" /><% // ne rien mettre entre les 2
	                   // ne rien mettre entre les 2%><a href="<%=href%>"<%=onClick%>><%=label%></a></li><%//PAS DE VIDE!!!%><%
	      } else {
	          %><%//PAS DE VIDE!!!%><li><img src="<%=context%>/ims/box_closed.gif" id="trigger:<%=sonDivId%>" alt="" height="8" width="8" onclick="toggle2('<%=sonDivId%>', '<%=encodedSonUri%>', '<%=withDocs%>', '<%=postTagOnClick%>', '<%=targetUri%>')" /><% // ne rien mettre entre les 2
	           // ne rien mettre entre les 2%><a property="skos:narrower" href="<%=href%>"<%=onClick%>><%=label%></a></li><%//PAS DE VIDE!!!%><%
	      }
	} // if (son.hasChild())
			} // for/* // CE COMMENATIRE EST-IL ENCORE PERTINENT ???

POUR AFFICHER AUSSI LES DOCUMENTSATTENTION : cette version ne supporte pas le "highlight"RESTE A FAIRE :- style des liens (comment ? pour le moment, tous ds style livetree cf slstyles.css)- le highlight : ne déclenche pas la bonne action (modifier liveSearchSubmit ds livesearch.js, et livesearchform.jsppour y mettre ds un autre attribut hidden le dochref (et non le kwhref)- lien : ouvre la page doc, pas le doc lui même (devrait dépendre du edit)- pas d'icône image pour afficher image - pourrait être à la place image vide du cas sans fils- affichage en plein pagePOUR AVOIR POTENTIELLEMENT PLUSIEURS ARBRES SUR UNE MEME PAGE :- modifier l'id de ligne : de 1_2 à [treeid]1_2 (cf sonid)- ds livesearch.js : modif du event handler// DUR DUR !!!*/// Bean_DocList beanDocList = (Bean_DocList) request.getAttribute("net.semanlink.servlet.Bean_DocList");
//2007-08 Niamey intersection in andKws// if (withDocs) {
if (!withdocs_notonfirstlevel) {
	if ((withDocs) && (kw != null)) {		List<SLDocument> docs = kw.getDocuments();		int nn = docs.size();		if (nn > 0) {			Jsp_Keyword jsp_keyword = new Jsp_Keyword(kw, request);			request.setAttribute("net.semanlink.servlet.jsp", jsp_keyword);			// code copié de Jsp_Keyword. 2021-01: y comprends rien. Tant pis
			
			Bean_DocList x = null;
			if (WalkListener.REMOVE_PARENT_TAG_FROM_DOCS_LIST_OF_TAGS) {
		        SLKeyword[] dontShow = new SLKeyword[1]; dontShow[0] = kw;
		        jsp_keyword.sort(docs, dontShow);
		        x = new Bean_DocList();
		        x.setList(docs);
		        x.setShowKwsOfDocs(true, dontShow); // 2021-01			
			} else {
		        SLKeyword[] dontShow = new SLKeyword[0];
		        jsp_keyword.sort(docs, dontShow);
		        x = new Bean_DocList();
		        x.setList(docs);
		        x.setShowKwsOfDocs(true, dontShow); // 2021-01          				
			}			x.setUri(kw.getURI());			request.setAttribute("net.semanlink.servlet.Bean_DocList", x);			%>			<jsp:include page="doclist.jsp"/>			<%		} // nn > 0	}
}%>