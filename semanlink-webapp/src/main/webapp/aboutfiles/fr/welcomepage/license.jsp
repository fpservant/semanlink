<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	import="net.semanlink.servlet.*"
%>
<%
String contextUrl = net.semanlink.util.Util.getContextURL(request);
%>
<h2>License</h2>
<ul>
<li>L'utilisation de Semanlink est libre pourvu que vous fassiez référence à son auteur : François-Paul Servant, fps [at] semanlink.net, www.semanlink.net</li>
<li>Vous l'utilisez à vos risques et périls.</li>
<li>Semanlink utilise <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/credits.htm">plusieurs logiciels libres...</a></li>
</ul>
