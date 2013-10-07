<!--thisMonth.jsp--><%%><%@ page language="java" session="true" import="net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_ThisMonth jsp = (Jsp_ThisMonth) request.getAttribute("net.semanlink.servlet.jsp");Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");boolean imagesOnly = false;if (imagesOnlyB != null) imagesOnly = imagesOnlyB.booleanValue();%><div class="keyword"><% /////////////////////////////////////////// DOCUMENTS AND/OR THUMBNAILS %><%if (imagesOnly) { %>	<jsp:include page="imagelist.jsp"/><%} else { // !imagesOnly	request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());	%>	<div class="graybox">		<div class="what"><%=jsp.aboutList()%></div>		<% /////////////////////////////////////////// IMAGE EVENTUELLE %>		<jsp:include page="/jsp/image.jsp" flush="true" />
		
		
		
		
		<% /////////////////////////////////////////// EW KWS %>
		<%
		jsp.prepareNewKWsList();
		%>
		<ul class="livetree">
		<jsp:include page="/jsp/livetreesons.jsp" flush="true" />
		</ul>
		
		
		
		
		
		
				<% /////////////////////////////////////////// DOC LIST %>		<jsp:include page="doclist.jsp"/>	</div><% } %></div> <!--keyword--><% /////////////////////////////////////////// %><!--/thisMonth.jsp-->