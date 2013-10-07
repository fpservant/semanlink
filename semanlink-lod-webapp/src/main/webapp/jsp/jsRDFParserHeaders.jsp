<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="false"
	import="net.semanlink.util.servlet.*, net.semanlink.lod.*"
%>
<%

/*
 * Suppose que Jsp_Page met à dispo le mécanisme permettant d'ajouter des onLoad events,
 et que cela est bien implémenté par template.jsp
 */
/**
 * What needs to be included in the HTML header to display RDF using javascript
 * @see net.semanlink.lod.RDFIntoDiv
 *
 * ATTENTION, suppose que template.jsp prend en charge l'ajout des onLoad events, cf Jsp_Page
 */

// ATTENTION contextPath (qui sert pour le script généré par jsp) n'est pas / terminated
// alors que rdfScriptsFolderURL l'est. (Il se termine probablement par scripts/rdfparsing/)
// String contextPath = request.getContextPath();

// An application parameter to allow to store the scripts outside of the war file
// (and hence change it more easily) "/" terminated :
// servletContext.getAttribute("net.semanlink.lod.rdfScriptsFolderURL")
ServletContext servletContext = application;
String rdfScriptsFolderURL = (String) servletContext.getAttribute("net.semanlink.lod.rdfScriptsFolderURL");
if (rdfScriptsFolderURL == null) {
	synchronized(application) {
		try {
			rdfScriptsFolderURL = (String) servletContext.getAttribute("net.semanlink.lod.rdfScriptsFolderURL");
			if (rdfScriptsFolderURL == null) {
				rdfScriptsFolderURL = servletContext.getInitParameter("rdfScriptsFolderURL");
				if ((rdfScriptsFolderURL != null) && (!("".equals(rdfScriptsFolderURL)))) {
					if (!(rdfScriptsFolderURL.endsWith("/"))) rdfScriptsFolderURL = rdfScriptsFolderURL + "/";
				} else {
					// standard location
					
					/*
					// qu'est-ce que j'avais en tête en faisant ça ?
							
					// En tout cas, je le vire pour faire focntionner les scripts dans Euro5Infotech,
					// qui ne définit pas ce paramètre WebAppURL.
					
					String webAppURL = servletContext.getInitParameter("WebAppURL");
					if ((webAppURL == null) || ("".equals(webAppURL))) throw new RuntimeException("InitParameter WebAppURL not defined");
					String s = webAppURL;
					if (!s.endsWith("/")) s += "/";
					rdfScriptsFolderURL = s + "scripts/rdfparsing/";
					*/
					rdfScriptsFolderURL = request.getContextPath() + "/scripts/rdfparsing/";
				}
				servletContext.setAttribute("net.semanlink.lod.rdfScriptsFolderURL", rdfScriptsFolderURL);
			}
		} catch (Exception e) { throw new RuntimeException(e); }
	}
}



// String tabulatorPath = contextPath + "/scripts/tabulator/based-on-tabulator-0.8-2007-02-01T16-43Z/";
String tabulatorPath = rdfScriptsFolderURL + "tabulator/based-on-tabulator-0.8-2007-02-01T16-43Z/";
%>
    <script src="<%=tabulatorPath%>log.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>util.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>uri.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/term.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/match.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/rdfparser.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/identity.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/query.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/sources.js" type="text/javascript"></script>

    <script src="<%=rdfScriptsFolderURL%>rdf_parsing.js?v=0.1.5" type="text/javascript"></script>
    <script src="<%=rdfScriptsFolderURL%>tree.js?v=0.1" type="text/javascript"></script>
<%

//TO ADD onload scripts
//see http://onlinetools.org/articles/unobtrusivejavascript/chapter4.html
//Jsp_RDF2HTMLPage jsp = (Jsp_RDF2HTMLPage) request.getAttribute("jsp");
Jsp_Page jsp = (Jsp_Page) request.getAttribute("jsp");

RDFIntoDiv rdfIntoDiv = (RDFIntoDiv) jsp.getRequest().getAttribute(RDFIntoDiv.class.getName());
if (rdfIntoDiv != null) {
%>
<script type="text/JavaScript">
<%=rdfIntoDiv.downloadRDFJavascript() %>
</script>	
<%
}


// @TODO
// mettre la definition des éventuels arbres js
/*
if (jsp instanceof Jsp_LODPage) {
}
*/
%>
    