<%@ page    contentType="text/html;charset=UTF-8"     pageEncoding="UTF-8"	language="java"	session="true"	import="net.semanlink.servlet.*"%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"><%Jsp_SimplePage jsp = (Jsp_SimplePage) request.getAttribute("net.semanlink.servlet.jsp");String contextPath = request.getContextPath();%><!--template_about.jsp--><html><head>	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">	<meta http-equiv="pragma" content="no-cache">	<title>Semanlink - <%=jsp.getTitle()%></title>	<link rel="stylesheet" href="<%=contextPath%>/css/sidemenu.css" type="text/css" >	<link rel="stylesheet" href="<%=contextPath%>/css/slstyles.css" type="text/css" ></head><body><div id="top">	<%		String topMenu=jsp.getTopMenu();		if (topMenu != null)  {			%>				<jsp:include page="<%=topMenu%>"/>
			<%		}	%></div> <!-- </div id="top"> --><div id="left"></div> <!-- </div id="left"> --><div class="clearboth"></div>
<div id="middleprint">
		<div class="simplepage">
		<jsp:include page="<%=jsp.getHtmlFile()%>"/>
		</div>
</div> <!-- middle --></body></html><!--/template_about.jsp-->