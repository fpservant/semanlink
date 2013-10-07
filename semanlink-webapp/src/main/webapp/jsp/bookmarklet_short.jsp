<!--bookmarklet_short.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*"%><%Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");%> 	<a href="javascript:function loadScript(scriptURL) { var scriptElem = 	document.createElement('SCRIPT'); scriptElem.setAttribute('language', 'JavaScript'); 	scriptElem.setAttribute('src', scriptURL); document.body.appendChild(scriptElem);} 	loadScript('<%=jsp.getContextUrl()%>/scripts/mark_js.jsp');	">Semanlink it</a>
	<%if (false) { %>
	 (Alternative: <a href="<%=((Jsp_Welcome) jsp).bookmarkletJavascript()%>">Semanlink it2</a>)
	<%} // if (false) %>
<!--/bookmarklet_short.jsp-->