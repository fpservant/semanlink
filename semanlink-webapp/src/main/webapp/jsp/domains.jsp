<!--domains.jsp-->
	Arrays.sort(domains);
	HashMap hm = jsp.getDomain2Documents();
	%>
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
		%>
	</div>