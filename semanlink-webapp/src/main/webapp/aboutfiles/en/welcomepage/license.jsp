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
<li>Semanlink is free to use if you credit the author fairly: <span property="http://purl.org/dc/terms/creator" resource="http://data.semanticweb.org/person/francois-paul-servant">François-Paul Servant</span>, fps [at] semanlink.net, www.semanlink.net</li>
<li>You use it at your own risk.</li>
<li>Semanlink uses several pieces of free software. <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/credits.htm">Credits...</a></li>

</ul>


