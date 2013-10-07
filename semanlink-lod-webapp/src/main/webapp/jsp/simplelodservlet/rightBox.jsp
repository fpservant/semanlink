<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="false" import="net.semanlink.util.servlet.*,net.semanlink.lod.iso3166.*"
%>

<%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("jsp");
String contextURL = jsp.getContextURL();
%>
<div class="watgraybox">
<div class="what">SPARQL</div>
<ul>
<li><a href="<%=contextURL%>/sparql/">Endpoint GUI</a></li>
</ul>
</div>

<p></p>
<div class="watgraybox">
<div class="what">About</div>
<ul>
  	<li><a href="<%=contextURL%>/releasenotes">Release notes</a></li>
  	<li>Mise en ligne : 2009/01/18</li>
</ul>
</div><%//watgraybox %>
<p></p>

