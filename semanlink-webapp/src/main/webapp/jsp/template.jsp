<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	session="true"
	import="net.semanlink.servlet.*,java.io.*,java.util.*,net.semanlink.semanlink.*"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML+RDFa 1.0//EN" "http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd">
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<% // response.setContentType("text/html; charset=UTF-8"); //???%>
<%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
// 2010-12
if (jsp == null) jsp = (Jsp_Page) request.getAttribute("jsp"); // cd LODServlet
if (jsp != null) request.setAttribute("net.semanlink.servlet.jsp", jsp);


if (jsp == null) { // ceci ne devrait plus arriver depuis que le welcome-file-list ds web.xml ne dirige plus vers template.jsp : si accès direct sur www.semanlink.net
	jsp = new Jsp_Welcome(request);
	request.setAttribute("net.semanlink.servlet.jsp", jsp);
}
String template = jsp.getTemplate();
if (!(template.equals("/jsp/template.jsp") )) {
	%>	<jsp:include page="<%=template%>"/><%
	return;
} else {
boolean edit = jsp.edit();

String contextPath = request.getContextPath();

String tagUri = null;
//if (jsp instanceof Jsp_Keyword) tagUri = jsp.getUri(); // 2012-07: non, retourne uri ds semanlink.net, pas 127.0.0.1:8080/semanlink (ENFIN... c ce qu'on avait voulu faire à l'époque)
if (jsp instanceof Jsp_Keyword) tagUri = HTML_Link.getTagURL(contextPath, jsp.getUri(), false, null); // attention: /semanlink/tag/google_car


%><!--template.jsp-->
<!-- 2013-08 RDFa
<html
	xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"
	xmlns:foaf="http://xmlns.com/foaf/0.1/"><head>
 -->
<html
	prefix="sl <%=SLSchema.NS%> foaf: http://xmlns.com/foaf/0.1/ skos: http://www.w3.org/2004/02/skos/core# rdfs: http://www.w3.org/2000/01/rdf-schema# schema: http://schema.org/"><head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Semanlink - <%=jsp.getTitle()%></title>
	<link rel="stylesheet" href="<%=contextPath%>/css/sidemenu.css" type="text/css" />
	<link rel="stylesheet" href="<%=contextPath%>/css/slstyles.css" type="text/css" />
	<%    
	if ((jsp instanceof Jsp_Keyword) || (jsp instanceof Jsp_ThisMonth)) { // 2007-03 remplace 2007-01
		%>
	<link rel="alternate" type="application/rdf+xml" title="RDF" href="<%=jsp.linkToRDF("rdf")%>" /><%
		if (jsp instanceof Jsp_Keyword) {
			// Some RDFa to say that this HTLM page is the 303-redirection of a non-information resource (the tag)
			// TODO: is this OK for a tag not in the main thesaurus?
			// xml.com article says we can use this, but doesn't work with rdfa js Why ??
			// <meta description="foaf:primaryTopic" content="jsp.getUri()"/>
			// <link rel="foaf:primaryTopic" href="jsp.getUri()>" /> // 2012-07: non, retourne uri ds semanlink.net, pas 127.0.0.1:8080/semanlink (ENFIN... c ce qu'on avait voulu faire à l'époque)
			%>	
			<link rel="foaf:primaryTopic" href="<%=tagUri%>" />
			<% 
			// 2013-08 avoids jsessionid in google
			%>
			<link rel="canonical" href="<%=tagUri%>" /> 
			<%
		}
	}
    
	String rss = jsp.rssFeedUriRelativToSL();
	if (rss != null) {%>	<link rel="alternate" type="application/rss+xml" title="RSS" href="<%=contextPath%>/<%=rss%>" /> <%}%>

	
	<%
	// @find create.js Bergie
	if (jsp instanceof Jsp_Keyword) {
		boolean bergie = false;
		if (bergie) {%>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
		<script src="<%=contextPath%>/scripts/create/deps/jquery-1.7.1.min.js"></script>   
	    <script src="<%=contextPath%>/scripts/create/deps/jquery-ui-1.8.18.custom.min.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/modernizr.custom.80485.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/underscore-min.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/backbone-min.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/vie-min.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/jquery.rdfquery.min.js"></script>    
	    <script src="<%=contextPath%>/scripts/create/deps/annotate-min.js"></script>   
	    <script src="<%=contextPath%>/scripts/create/deps/create.js"></script>
	    <script src="<%=contextPath%>/scripts/create/deps/rangy-core-1.2.3.js"></script>
	    <script src="<%=contextPath%>/scripts/create/deps/hallo.js"></script>
	    
		<script src="<%=contextPath%>/scripts/create/fps_create.js"></script>   
	    <link rel="stylesheet" href="http://createjs.org/js/deps/font-awesome/css/font-awesome.css" />   
	    <link rel="stylesheet" href="http://createjs.org/css/themes/create-ui/css/create-ui.css" />   
	    <link rel="stylesheet" href="http://createjs.org/css/themes/midgard-notifications/midgardnotif.css" />
	<%}}%>

	<script type="text/JavaScript">
	<%
	// .../semanlink/loadsubtree.
	// if faut tenir compte de l'éventuel url rewriting:
	%>
	function liveTreeLoadSubTree() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),contextPath + "/loadsubtree.do")).toString())%>";
	}
	
	<% //nécessaire à livesearch.js %>
	function liveSearchAction() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),contextPath + "/livesearch.do")).toString())%>";
	}
	
	<% // to be able to type directly into the search box %>
	function setFocus() {
		var x = document.getElementById('searchform');
		if (x) x.q.focus();
	}
	
	<%
	// 2013-08 
	// to load scripts that can be loaded after the body
	// Add a script element as a child of the body %>
	function downloadJS() {
		 var element = document.createElement("script");
		 element.src = "<%=contextPath%>/scripts/livesearch.js";
		 document.body.appendChild(element);
		 
		 element = document.createElement("script");
		 element.src = "<%=contextPath%>/scripts/trees.js";
		 document.body.appendChild(element);
	}
	</script>
	
	<% // 2010-12 cf rdf_parsing.js  @find display of res in function of their rdf:type %>
	<script type="text/JavaScript">	
	TYPE2METHOD = new Array();
	displayTag = function(kb, res, container) {
		displayTagInList(kb, res, container);
	}
	TYPE2METHOD["http://www.semanlink.net/2001/00/semanlink-schema#Tag"]=displayTag; <%// must be after the def of the function%>

	<%// ds le cas à la maison, on a le lien en htmlget pour retomber sur uri en 127%>
	displayTagInList = function (kb, res, container) {
		var containingDiv;
		containingDiv = document.createElement("div");
	 	containingDiv.className = "graybox";	

		var label = getLabel(kb, res);
		var titleParag = document.createElement("h2");
		if (res.uri) { // cf anon res: no uri
			displayLinkToURI(res.uri, "TAG: " + label, titleParag);
		} else { // should'nt happen: a tag always has a uri
			titleParag.appendChild(document.createTextNode("TAG: " + label));
		}
		containingDiv.appendChild(titleParag);
		someDisplay(kb, res, containingDiv);
		container.appendChild(containingDiv);

		var div4tree = document.createElement("div");
		div4tree.className = "graybox";	
		if (subTagTree(kb, div4tree, res)) containingDiv.appendChild(div4tree);
	}

	subTagTree = function (kb, container, mainRes) {
		var treeObject = getSubTagsTreeObject(kb, mainRes);
		var x = treeObject.constructAncestorsDisplay(mainRes);
		if (x) container.appendChild(x);
		var y = treeObject.constructDisplay(mainRes);
		if (y) container.appendChild(y);
		return (x || y);
	}
		
	getSubTagsTreeObject = function(kb, mainRes) {
		var sonProps = [];
		var parentProps = [];
		var leafProps = [];
		var leafInvProps = [];
		var checkDisplayTriangleScript = null;

		parentProps[0] = kb.sym("http://www.semanlink.net/2001/00/semanlink-schema#hasParent");
		sonProps[0] = kb.sym("http://www.semanlink.net/2001/00/semanlink-schema#hasChild");
		leafInvProps[0] = kb.sym("http://www.semanlink.net/2001/00/semanlink-schema#tag");

		return  new TreeObject(kb, sonProps, parentProps, leafProps, leafInvProps, null, null, checkDisplayTriangleScript);
	}
		
	</script>
	
	<% // 2010-06 recopié de LOD/template.jsp (achtung, some modifs later)%>	
	<% 
		// it is often useful to know in javascript the path to the web application 
		// (Hmm, there's probably a direct way to get it in js // TODO)
	%>
	<script type="text/JavaScript">
		<% // for instance /semanlink %>
		function getContextPath() { return "<%=contextPath%>"; }
		<% // for instance http://127.0.0.1:8080/semanlink or http://www.semanlink.net %>
		function getContextURL() { return "<%=jsp.getContextURL()%>"; }
	</script>
	
	<%
	List<String> moreHeadersJspList = jsp.getMoreHeadersJspList();
	if (moreHeadersJspList != null) {
		for (String moreHeaders : moreHeadersJspList) {
			if (moreHeaders != null) {
				%><jsp:include page="<%=moreHeaders%>" /><%
			}
		}
	}




	// 2010-06
	%>
	<script type="text/JavaScript">
		Tools = {
			'addEvent': function(obj, evType, fn) { 
				 if (obj.addEventListener){ 
				   obj.addEventListener(evType, fn, false); 
				   return true; 
				 } else if (obj.attachEvent){ 
				   var r = obj.attachEvent("on"+evType, fn); 
				   return r; 
				 } else { 
				   return false; 
				 } 
			}
		}
		<%
		Set<String> onLoadEvents = jsp.getOnLoadEvents();
		if (onLoadEvents != null) {
		for (String onLoadEvent : onLoadEvents) {%>
		Tools.addEvent(window, 'load', <%=onLoadEvent%>);
		<%}}%>
	</script>
	
	
	

</head>
<!-- body onload="liveSearchInit(); setFocus();" 2010-06: see Jsp_page find addOnLoadEvents -->
<!-- body xmlns:skos="http://www.w3.org/2004/02/skos/core#"> --><%// @find create.js bergie %>
<body>
<div id="top"> <%// j'ai essayé de le mettre après via css, mais raté%>
	<%
		String topMenu=jsp.getTopMenu();
		if (topMenu != null)  {
			%>
				<jsp:include page="<%=topMenu%>"/>
			<%
		}
	%>
</div> <!-- </div id="top"> -->
<div class="clearboth"></div>  <% // depuis XHTMl + RDFA, ceci ne marche pas si mis ds la div top: il faut le mettre après la fermeture du div du topmenu (ds template.jsp, donc) %>


<div id="left">
</div> <!-- </div id="left"> -->

<%/*
mettre ceci ici fait qu'on a  2SLTree.walk(expand all)
Si on le met après middle, un seul, mais effet perçu plus mauvais(barre à droite en fin)
*/%>
<div id="right"> <%// ca, ca va après%>
	<jsp:include page="<%=jsp.getSideMenu()%>"/>
</div> <!-- </div id="right"> -->

<% // @find RDFa Bergie
if (jsp instanceof Jsp_Keyword) { // 2013-08 RDFa added typeof
	%><div id="middle" about="<%=tagUri%>" typeof="<%=SLVocab.KEYWORD_CLASS%> <%=net.semanlink.skos.SKOS.Concept%>"><%	
} else {
	%><div id="middle"><%
}
%>

	<%
	Object error = request.getAttribute("net.semanlink.servlet.error");
	if (error != null) {
		if (error instanceof Throwable) {
			Throwable e = (Throwable) error;
			PrintWriter pw = new PrintWriter(out);
			%>
			<div class="title">
			Une exception est survenue
			</div> <!-- class="title" -->
			<pre><%e.printStackTrace(pw);%></pre>
			<%
		} else {
			%>
			<div class="title">
			Une anomalie est survenue
			</div> <!-- class="title" -->
			<pre><%=error.toString()%></pre>
			<%		
		}
	} else if (jsp instanceof Jsp_SimplePage) {
		Jsp_SimplePage simpleJsp = (Jsp_SimplePage) jsp;
		%>
		<div class="simplepage">
		<jsp:include page="<%=simpleJsp.getHtmlFile()%>"/>
		</div>
		<%
	} else {
		%>
	
	
		<% /////////////////////////////////////////// TITLE %>
		<%if (
			(!(jsp instanceof Jsp_Keyword)) 
			&& (!(jsp instanceof Jsp_Document))
			&& (!(jsp instanceof Jsp_AndKws)) ) { %>
			
			<div class="title">
				<%=jsp.getTitleInTitle()%>
			</div> <!-- class="title" -->
		
			<% /////////////////////////////////////////// PARENTS %>
			<%
			Bean_KwList bean_KwList= jsp.prepareParentsList();
			if ((bean_KwList!=null) && (bean_KwList.size() > 0) ) {
				%><div class="graybox">
					<div class="horizEnumeration">
						<%if (edit) {%>
							<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />
						<%} else {%>
							<jsp:include page="/jsp/kwlist.jsp" flush="true" />
						<%} // if edit or not%>
					</div> <!-- <div class="parentcontainer"> -->
					<div class="clearboth"></div>
				  </div><!-- graybox -->
				<%
			}
		} 
		%>
	
		<% /////////////////////////////////////////// CONTENT %>
		<%
		String content = jsp.getContent();
		if (content != null) {
			try {
				%><jsp:include page="<%=content%>" flush="true" /><%
			} catch (Throwable e) {
				// probablement qu'on essaye d'inclure quelque chose qui est en fait un appel à CoolUriServlet, et non une page jsp ou html
				// (par ex, include "/about/..." -- about étant dans les servlet-mapping de CoolUriServlet, cf web.xml)
				%>Impossible to include content: <%=content%>:<%
				PrintWriter pw = new PrintWriter(out);
				%>
				<pre><%e.printStackTrace(pw);%></pre>
				<%
				// throw new RuntimeException(e);
			}
		} else {
			%>What do you think you'll get if you don't say what you want?<%
		}
	} // error or not
%>




</div> <!-- middle -->
</body>
</html>
<%}// if template%>
<!--/template.jsp-->