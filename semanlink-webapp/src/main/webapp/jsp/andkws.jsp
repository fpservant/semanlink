<!--andkws.jsp--><%/** * And de keywords. */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.util.*, glguerin.io.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%// SLKeyword kw = (SLKeyword) request.getAttribute("net.semanlink.semanlink.SLKeyword");Jsp_AndKws jsp = (Jsp_AndKws) request.getAttribute("net.semanlink.servlet.jsp");// SLKeyword kw = (SLKeyword) jsp.getSLResource();boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
jsp.prepareParentsList();
%>
<div class="graybox">
			<div class="horizEnumeration">
			<jsp:include page="/jsp/kwlist.jsp" flush="true" />
	</div> <!-- <div class="parentcontainer"> -->
	<div class="clearboth"></div>
</div>






<div class="kwtitleattop">
	<%=jsp.getTitle()%>
</div> <!-- class="title" -->
<div class="keyword"><div class="graybox">
<% /////////////////////////////////////////// KWS IN THE INTERSECTION %>
<%
jsp.prepareIntersectKWsList();
%>
<ul class="livetree"><jsp:include page="/jsp/livetreesons.jsp" flush="true" />
</ul><% /////////////////////////////////////////// IMAGE EVENTUELLE %><jsp:include page="/jsp/image.jsp" flush="true" /><% /////////////////////////////////////////// DOCUMENTS AND/OR THUMBNAILS %><%Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");boolean imagesOnly = false;if (imagesOnlyB != null) {	imagesOnly = imagesOnlyB.booleanValue();}														if (imagesOnly) { %><jsp:include page="imagelist.jsp"/>														<%} else {														jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste														request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());														%><jsp:include page="doclist.jsp"/>													 <% } %>													 <%if (edit) {%><html:link page="<%=jsp.getLinkToNewKeyword()%>">Create Keyword</html:link><%}%>													 <div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%></div>													 </div> <!--keyword--><!--/andkws.jsp-->