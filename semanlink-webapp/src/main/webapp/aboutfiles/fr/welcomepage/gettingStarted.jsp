<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	import="net.semanlink.servlet.*"
%>
<%String contextUrl = net.semanlink.util.Util.getContextURL(request); %>
<h2>Premiers pas avec Semanlink</h2>
<h3>Installer la bookmarklet</h3>
<ul><li>Une "bookmarklet" permet de facilement marquer les pages web qui vous intéressent (bookmarks). Pour installer cette bookmarklet, faites glisser le lien suivant
 sur la barre d'outil de votre navigateur (avec Internet Explorer, clic droit sur le lien, puis
    choisir, dans le menu, &quot;Ajouter aux Favoris&quot;): <jsp:include page="/jsp/bookmarklet_short.jsp"/>.
    Une fois la bookmarklet installée, il suffit de la cliquer pour marquer la page affichée dans votre navigateur.
    <%if (false) { %>
		<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/bookmarklet_more.htm">More...</a>
	<%} %>
</li>
</ul>
<h3>Consultez le manuel <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/getting_started_tutorial.htm">"Getting started tutorial</a>"</h3>
<ul><li>Plus de documentation est disponible : consultez "<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/help.htm">l'aide</a>" (bouton en haut de page)...</li></ul>
<h3>Éventuellement, <a href="<%=contextUrl%>/sl/delicious">importez votre compte delicious...</a></h3>
