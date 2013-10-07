<!--property.jsp--><%/** * Affichage d'une property : les docs qui en sont affectés. */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.util.*, glguerin.io.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Property jsp = (Jsp_Property) request.getAttribute("net.semanlink.servlet.jsp");Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");boolean imagesOnly = false;if (imagesOnlyB != null) imagesOnly = imagesOnlyB.booleanValue();%><div class="keyword"><div class="graybox">



		<% /////////////////////////////////////////// EW KWS %>
		<%
		jsp.prepareKWsList();
		%>
		<ul class="livetree">
		<jsp:include page="/jsp/livetreesons.jsp" flush="true" />
		</ul>





<% /////////////////////////////////////////// DOCUMENTS AND/OR THUMBNAILS %><%if (imagesOnly) { %>	<jsp:include page="imagelist.jsp"/><%} else { // !imagesOnly	request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());	%>	<% /////////////////////////////////////////// IMAGE EVENTUELLE %>	<jsp:include page="/jsp/image.jsp" flush="true" />	<% /////////////////////////////////////////// DOC LIST %>	<jsp:include page="doclist.jsp"/>					<div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%>
<% } %></div></div> <!--keyword--><% /////////////////////////////////////////// %><!--/property.jsp-->