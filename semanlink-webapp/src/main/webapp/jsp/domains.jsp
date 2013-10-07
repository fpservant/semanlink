<!--domains.jsp--><%%><%@ page language="java" session="true" import="net.semanlink.servlet.*, java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Domains jsp = (Jsp_Domains) request.getAttribute("net.semanlink.servlet.jsp");%><div class="keyword"><% /////////////////////////////////////////// DOMAINS %><%	String[] domains = jsp.getDomains();
	Arrays.sort(domains);
	HashMap hm = jsp.getDomain2Documents();
	%>	<div class="graybox">		<div class="what"><%=jsp.aboutList(domains.length)%></div>
		<ul>
		<%
		for (int idom = 0; idom < domains.length; idom++) {
			String domain = domains[idom];
			%><li><a href="<%=domain%>"><%=domain%>
				<%
				List docs = (List) hm.get(domain);
				if (docs != null) {
					Bean_DocList blist = new Bean_DocList();
					blist.setList(docs);
					request.setAttribute("net.semanlink.servlet.Bean_DocList", blist);
					%><jsp:include page="/jsp/doclist.jsp"/><%
				}
				%>
			
			
			</li><%
		}
		%>		</ul>
	</div></div><% /////////////////////////////////////////// %><!--/domains.jsp-->