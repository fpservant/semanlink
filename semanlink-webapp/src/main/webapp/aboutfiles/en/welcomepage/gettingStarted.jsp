<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	import="net.semanlink.servlet.*"
%>
<%String contextUrl = net.semanlink.util.Util.getContextURL(request); %>
<h2>Getting started</h2>
<h3>Install bookmarklet</h3>
<ul><li>A bookmarklet allows to easily bookmark web pages. To install it, just
  drag following link to your browser toolbar (with IE, right-click it, and
    select menu item &quot;Add to Favorites&quot;): <jsp:include page="/jsp/bookmarklet_short.jsp"/>.
    Once done, just click the bookmarklet to bookmark a page that you're visiting. 
    <%if (false) { %>
		<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/bookmarklet_more.htm">More...</a>
	<%} %>
</li>
</ul>
<h3>Have a look at the <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/getting_started_tutorial.htm">getting started tutorial</a>"</h3>
<ul><li>More documentation under the "<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/help.htm">Help</a>" section in the top menu-bar...</li></ul>
<!-- 
<h3>Optionaly, <a href="<%=contextUrl%>/sl/delicious">import your delicious posts...</a></h3>
 -->
<h3>Enjoy!</h3>
