<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="false"
	import="net.semanlink.util.servlet.*, net.semanlink.lod.*, java.util.*"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
String contextPath = request.getContextPath();
//
Jsp_Page jsp = (Jsp_Page) request.getAttribute("jsp");
String topBoxJsp = jsp.getTopBoxJsp();
String leftBoxJsp = jsp.getLeftBoxJsp();
String rightBoxJsp = jsp.getRightBoxJsp();
String centerBoxJsp = jsp.getCenterBoxJsp();

String commentString = "<!--" + jsp.getClass().getName() + "-->";
%><!--  LOD template.jsp --><%=commentString%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<!-- meta http-equiv="pragma" content="no-cache" /  -->
	<link rel="stylesheet" href="<%=contextPath%>/css/wat.css" type="text/css" />
	<title><%=jsp.getTitle()%></title>
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
	<% // 2010-12 cf rdf_parsing.js  @find display of res in function of their rdf:type %>
	<script type="text/JavaScript">	
	TYPE2METHOD = new Array();
	</script>
	
	<%
	// 2010-06 : on n'avait qu'une seule moreHeadersJsp, mais on a ajouté une liste pour RDFIntoDiv
	// Faudra nettoyer ça (gardr que la liste ?)
	String moreHeadersJsp = jsp.getMoreHeadersJsp();
	if (moreHeadersJsp != null) {
		%><jsp:include flush="true" page="<%=moreHeadersJsp%>"></jsp:include><%
	}

	List<String> moreHeadersJspList = jsp.getMoreHeadersJspList();
	if (moreHeadersJspList != null) {
		for (String moreHeaders : moreHeadersJspList) {
			%><jsp:include flush="true" page="<%=moreHeaders%>"></jsp:include><%
		}
	}
	
	Set<String> onLoadEvents = jsp.getOnLoadEvents();
	if (onLoadEvents != null) {%>
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
		<% for (String onLoadEvent : onLoadEvents) {%>
			Tools.addEvent(window, 'load', <%=onLoadEvent%>);
		<%}%>
	</script>
	<%}%>
		
</head>

<%
String bodyTag = jsp.getBodyTag();
if (bodyTag != null) {
	%><%=bodyTag%>
<%} else {%>
<body>
<%}%>
<%
if (topBoxJsp != null) {%>
<div id="topbox">
	<jsp:include flush="true" page="<%=topBoxJsp%>"></jsp:include>
</div><!-- end topbox -->
<%}

if (leftBoxJsp != null) {%>
<div id="leftbox"><div id="leftcontent">
	<jsp:include flush="true" page="<%=leftBoxJsp%>"></jsp:include>
</div></div><!-- end leftbox -->
<%}

if (rightBoxJsp != null) {%>
<div id="rightbox"><div id="rightcontent">
		<jsp:include flush="true" page="<%=rightBoxJsp%>"></jsp:include>
</div></div><!-- end rightbox -->
<%}%>

<div id="centerbox"><div id="centercontent" class="watgraybox">
	<%
	// we always have a div id "centercontent", even if there is no centerBoxJsp
	// cf case where it is contructed in javascript
	if (centerBoxJsp != null) {
		%><jsp:include flush="true" page="<%=centerBoxJsp%>"></jsp:include><%
	}%>
</div></div><!-- end centerbox -->
</body>
</html>
<!--  /LOD template.jsp -->