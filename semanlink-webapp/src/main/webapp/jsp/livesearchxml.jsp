<%@ page
    contentType="text/xml;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	session="true"
	import="net.semanlink.servlet.*"
%><?xml version='1.0' encoding='utf-8'  ?>
<%
// résultat de liveSearch: une liste de kws. On va passer à livetreesons pour affichage 1er niveau d'arbre
Bean_KwList truc = (Bean_KwList) request.getAttribute("net.semanlink.servlet.Bean_KwList");
if (truc != null) {
		int n = truc.size();
		if (n > 0) {
			// cet arg permet ds livetreesons de faire la distinction entre résultat du livesearch et arbre normal
			request.setAttribute("livesearchxml","true");
			request.setAttribute("livetreelist",truc.getList());
			
			// System.out.println("livesearchxml.jsp targeturi" + request.getParameter("targeturi")); // DEBUG 2020
			%>
				<ul id="LiveSearchRes" class="livetree" style="padding-left:0"><jsp:include page="/jsp/livetreesons.jsp" /></ul>
			<%
			request.removeAttribute("livesearchxml");
		} // if (n > 0)
} // if (truc != null)
%>