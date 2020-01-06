<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	session="true"
	import="net.semanlink.servlet.*,java.io.*,java.util.*,net.semanlink.semanlink.*"
%><!DOCTYPE HTML>
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
	<link rel="stylesheet" href="<%=contextPath%>/css/slstyles.css?v=060-6" type="text/css" />
	
    <script src="<%=contextPath%>/scripts/markdown-it.min.910.js"></script>   
	<script src="<%=contextPath%>/scripts/markdown-it-replace-link.min.js"></script>   
    <script src="<%=contextPath%>/scripts/markdown-sl.js?v=060-6"></script>   
	
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
	
	function rightBar() {
		var rightBarString = sessionStorage.getItem("rightBar"); // 0 pour cacher, 1 pour montrer, 1 par défaut
		if (!rightBarString) {
			rightBarString = "1";
		}
		
		// si queryparam, on écoute le qp
		// et on change la var de session, au besoin
		var queryString = location.search.substring(0); // includes the ? or &		
		var k = queryString.indexOf("rightbar=");
		if(k > 0) {
			// alert(queryString.substring(k+9,k+10));
			var rightBarString = queryString.substring(k+9,k+10); // 0 ou 1
	        if (rightBarString == "0") {
	            displayRightBar(false);
	            setRightBar("0");
	        } else {
	        	displayRightBar(true);
	        	setRightBar("1");
	        }
			
		} else { // no query param
			// si false, on cache s'il faut (assumant qu'on montre par défaut)
	        if (rightBarString == "0") {
	            displayRightBar(false)
	        }
			
		}
		
	}
	
	function setRightBar(rightBarString) {
		sessionStorage.setItem("rightBar", rightBarString);
	}
	
	// set the right bar according to rightBarState (if true, show the right bar)
	function displayRightBar(b) {
		var disp = false;
		if (!b) {
			disp = "none";
		} else {
			disp = "block";
		}
	    var d = false;
	    d = document.getElementById("right");
	    if (d) {
	        d.style.display = disp;
	    }
	    d = document.getElementById("navcontainer");
	    if (d) {
	        d.style.display = disp;
	    }
	    d = document.getElementById("logo");
	    if (d) {
	        // d.style.display = "none";
	    }
	    d = document.getElementById("file_info")
	    if (d) {
	        d.style.display = disp;
	    }
	    d = document.getElementById("aboutThisDoc")
	    if (d) {
	        d.style.display = disp;
	    }
	    d = document.getElementById("documenttags")
	    if (d) {
	        d.style.display = disp;
	    }
	    if (b) {
            d = document.getElementById("middleprint")
            if (d) {
                d.id = "middle";
            }           
	    	
	    } else {
	        d = document.getElementById("middle")
	        if (d) {
	            d.id = "middleprint";
	        }	    	
	    }
	}

	<%
	// 2013-08 
	// to load scripts that can be loaded after the body
	// Add a script element as a child of the body %>
	function downloadJS() {
		 var element = document.createElement("script");
		 element.src = "<%=contextPath%>/scripts/livesearch.js?v=0.6.2";
		 document.body.appendChild(element);
		 
		 element = document.createElement("script");
		 element.src = "<%=contextPath%>/scripts/trees.js?v=0.6.2";
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

<% // 2017-01 DRAG TEST %>
	

</head>
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

<%
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
		if (content == null) {
			// prudence est mère des vertus // 2019-08 sicg 
			content = "/jsp/welcome.jsp";
		}
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
	} // error or not
%>




</div> <!-- middle -->
</body>

<% // put at the end, in order to allow included .jsp to add events to jsp (it is the case of document.jsp, for the markdown) %>
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

</html>
<%}// if template%>
<!--/template.jsp-->