<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"
%>
	- request.getAttribute("livesearchxml") true si livesearch.xml (non doc sinon)
	(needed to handle special action defined by pulldown menu "add parent, etc." when editing)
	- request.getParamater"postTagOnClick") true iff we need to handle special action defined by pulldown menu "add parent, etc.")
	
	
	- request.getAttribute("resolveAlias")
	si non doc,
		- defauts to true if request.getAttribute("livetreelist") != null
		- default to false if request.getAttribute("livetreelist") == null
	
	request.getParameter("withdocs") "true" ou "false" 
	ou request.getAttribute("withdocs") Boolean.TRUE ou autre chose pour false
Boolean boo = (Boolean) request.getAttribute("resolveAlias");
if (boo != null) {
	resolveAlias = boo.booleanValue();
} else {
	resolveAlias = (children != null); // je crois (le 15 aout 2005) qu'on n'a besoin de ne résoudre les alias que dans ce cas
	//c'est en effet seulement au 1er niveau d'un résultat de livesearch (et pas dans les cas de fils de kws)
}

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

	String href = HTML_Link.getTagURL(request.getContextPath(), sonUri, resolveAlias, null); // RDFa I don't like it: we lose the direct link to the html (cf null arg) // TODO
	
	if (postTagOnClick) {
		// si on est ds les résultats de la searchform, il faut mettre le onClick postTag pour faire ce qui est demandé par le popup (add parent, add child, etc)
		// si non, (ex : keyword sous forme de tree, ou snip), il faut faire le href standard
		onClick = " onClick=\"postTag('" + href + "');return false\"";
	}
			%><%//PAS DE VIDE!!!%><li><img src="<%=contextPath%>/ims/box_nada.gif" height="0px" width="8px" alt="" id="trigger:<%=sonDivId%>" /><a property="skos:narrower" href="<%=href%>"<%=onClick%>><%=label%></a></li><%//PAS DE VIDE!!!%><%			
		}
	} else { // son has sons
			%><%//PAS DE VIDE!!!%><li><img src="<%=contextPath%>/ims/box_closed.gif" id="trigger:<%=sonDivId%>" alt="" height="8px" width="8px" onclick="toggle2('<%=sonDivId%>', '<%=encodedSonUri%>', '<%=withDocs%>', '<%=postTagOnClick%>')" /><% // ne rien mettre entre les 2
		} else {
			%><%//PAS DE VIDE!!!%><li><img src="<%=contextPath%>/ims/box_closed.gif" id="trigger:<%=sonDivId%>" alt="" height="8px" width="8px" onclick="toggle2('<%=sonDivId%>', '<%=encodedSonUri%>', '<%=withDocs%>', '<%=postTagOnClick%>')" /><% // ne rien mettre entre les 2
			 // ne rien mettre entre les 2%><a property="skos:narrower" href="<%=href%>"<%=onClick%>><%=label%></a><%//PAS DE
			// VIDE !!!%><ul id="block:<%=sonDivId%>" class="livetree"><li style="display:none"></li><%//ATTENTION A NE RIEN METTRE DS LE UL (cf script)%></ul><%// PAS DE
		// VIDE!!!%></li><%//PAS DE VIDE!!!%><%
		}

POUR AFFICHER AUSSI LES DOCUMENTS
//2007-08 Niamey intersection in andKws
if (!withdocs_notonfirstlevel) {
	if ((withDocs) && (kw != null)) {
}