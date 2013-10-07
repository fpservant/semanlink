<%@ page    contentType="text/html;charset=UTF-8"     pageEncoding="UTF-8"	language="java"	session="true"	import="net.semanlink.servlet.*,net.semanlink.semanlink.*, java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><!--home.jsp-->
<%
Jsp_Welcome jsp = (Jsp_Welcome) request.getAttribute("net.semanlink.servlet.jsp");
SLModel model = SLServlet.getSLModel();
ServletContext servletContext = getServletContext();
String contextUrl = net.semanlink.util.Util.getContextURL(request);
String fn = I18l.pathToI18lFile("/welcomepage/veryshortintro.jsp", session, servletContext);
%>
<div class="graybox">
	<p><jsp:include page="<%=fn%>"/> <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/intro_more.htm"><%=jsp.i18l("welcome.more")%></a></p>
	<%if (!SLServlet.isSemanlinkWebSite()) { %>
		<p><%=jsp.getI18l().getFormatedMessage("welcome.seewebsite","<a href=\"http://www.semanlink.net\">www.semanlink.net</a>")%></p>
	<%} else {
		fn = I18l.pathToI18lFile("/welcomepage/juc.jsp", session, servletContext);
		%><jsp:include page="<%=fn%>"/>
	<%} %>
</div>

<%if (!SLServlet.isSemanlinkWebSite()) {
	fn = I18l.pathToI18lFile("/welcomepage/gettingStarted.jsp", session, servletContext);
	%>
	<div class="graybox">
		<jsp:include page="<%=fn%>"/>
	</div>

	<%
	if (jsp.isEditor()) {
		
		// FAVORIS
		SLKeyword favori = jsp.getFavori();
		if (favori != null) {
			request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(favori, request));
			%>
			<div class="graybox">
				<jsp:include page="keyword_short.jsp"/>
			</div>
		<% } // favori null or not %>
		
		
		<div class="graybox">
			<h2><%=jsp.i18l("x.aboutModel")%></h2>
			<%
			String[] args = new String[4];
			args[0] = Long.toString(model.numberOfDocs());
			args[1] = Long.toString(model.size());
			args[2] = Long.toString(model.docsSize());
			args[3] = Long.toString(model.kwsSize());
			%>
			<p><%=jsp.getI18l().getFormatedMessage("welcome.docsAndStatementsNbs",args)%></p>
			<h3><%=jsp.i18l("welcome.thesauri")%></h3>
			<jsp:include page="thesauri.jsp"/>
			<%
			if (true) {
				if (jsp.isEditor()) { // DEBUG %>
					<%Vector v = null;
					v = model.getDataFolderList();%>
						<h3><%=jsp.i18l("welcome.dataFolders")%></h3>
						<ul>
							<% {
								for (int i = 0; i < v.size(); i++) {
									SLDataFolder dataFolder = (SLDataFolder) v.elementAt(i);
									%><li><%=dataFolder.getFilename()%></li><%
								}
							}%>
						</ul>
				<%
				} // if if (jsp.isEditor()) (debug)
			} // if false
			
			if (SLServlet.isProto()) { 
				%>
				<a href="<%=contextUrl%>/sl/domains">Domains</a>
				<%
			}
			%>
		</div>
		
		<%if (false) {%>
			<%Vector v = null; %>
			
			<%v = model.getOpenKWsFiles();%>
			<div class="graybox">
				<h2>Keyword files: <%=v.size()%></h2>
				<div class="kwlist">
					<%
					{
						for (int i = 0; i < v.size(); i++) {
							%><%=v.elementAt(i).toString()%><br><%
						}
					}
					%>
				</div>
			</div>
			
			<% v = model.getOpenDocsFiles();%>
			<div class="graybox">
				<h2>Doc files: <%=v.size()%></h2>
				<div class="kwlist">
					<%
					{
						for (int i = 0; i < v.size(); i++) {
							%><%=v.elementAt(i).toString()%><br><%
							
						}
					}
					%>
				</div>
			</div>
		<%} // if false %>
		
		<%
		if (false) {
			if (jsp.isEditor()) { // DEBUG %>
				<%ArrayList al = model.debugRealKWsNotInConceptSpace(); %>
				
				<div class="graybox">
					<h2>Keywords sans type: <%=al.size()%></h2>
					<div class="kwlist">
						<%
						{
							for (int i = 0; i < al.size(); i++) {
								SLKeyword kw = (SLKeyword) al.get(i);
								HTML_Link link = HTML_Link.linkToKeyword(kw);
								%><html:link page="<%=link.getPage()%>"><%=link.getLabel()%></html:link><br><%
							}
						}
						%>
					</div>
				</div>
			<%
			} // if if (jsp.isEditor()) (debug)
		} // if false %>
		
		
		
	<%} // if jsp.isEditor() %>

<%} // 	if (!SLServlet.isSemanlinkWebSite())   %>


<%if (SLServlet.isSemanlinkWebSite()) {
	fn = I18l.pathToI18lFile("/welcomepage/webSiteText.jsp", session, servletContext);
	%>
	<jsp:include page="<%=fn%>"/>

	<div class="graybox">
		<h2><%=jsp.i18l("welcome.download")%></h2>
			<p><a href="http://www.semanlink.net/files/download/semanlink-<%=SLServlet.getSemanlinkVersion()%>.zip"><%=jsp.getI18l().getFormatedMessage("welcome.download2","Semanlink-" + SLServlet.getSemanlinkVersion() + ".zip")%></a></p>
			<p><a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/releasenotes.htm"><%=jsp.i18l("welcome.releaseNotes")%></a></p>
	</div>
<%} else { %>
	<div class="graybox">
	<h2><%=jsp.getI18l().getFormatedMessage("welcome.version", SLServlet.getSemanlinkVersion())%></h2>
		<p><a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/releasenotes.htm"><%=jsp.i18l("welcome.releaseNotes")%></a></p>
</div>
<%	
} // 	if (SLServlet.isSemanlinkWebSite()) or not  %>

<%if (SLServlet.isProto()) { %>
<div class="graybox">
	<h2>"Real World Servlet"</h2>
	<script type="text/JavaScript">
	function realWorldSubmit(what) {
			window.location = "<%=request.getContextPath()%>" + "/sl/realworld/" + what + "?uri=" 
			+ encodeURIComponent(document.getElementsByName("realWorldUri")[0].value);
	}
	</script>
	<form name="realworld" method="get" action="<%=request.getContextPath() + "/sl/realworld/"%>" accept-charset="UTF-8">
		<p>
		Page URL: <input type="text" name="realWorldUri" size="40" />
		<input type="button" onclick="realWorldSubmit('get')" value="OK">
		<input type="button" onclick="realWorldSubmit('rdf')" value="RDF">
		</p>
	</form> 

	<h2>"NEW Real World Servlet"</h2>
	<script type="text/JavaScript">
	function realWorldSubmit2(what) {
		var s = document.getElementsByName("realWorldUri2")[0].value;
		if (s.indexOf("http://") == 0) s = s.substring(7);
		window.location = "<%=request.getContextPath()%>" + "/sl/realworld/" + what +"/" + s;
	}
	</script>
	<form name="realworld" method="get" action="<%=request.getContextPath() + "/sl/realworld/"%>" accept-charset="UTF-8">
		<p>
		Page URL: <input type="text" name="realWorldUri2" size="40" />
		<input type="button" onclick="realWorldSubmit2('get')" value="OK">
		<input type="button" onclick="realWorldSubmit2('rdf')" value="RDF">
		</p>
	</form> 



	
</div><%//graybox %>
<div class="graybox">
	<h2>Other stuff: "Semantic Semantic Web Servlet"</h2>
	<form name="semanticsemanticweb" method="get" action="<%=request.getContextPath() + "/sl/semanticsemanticweb"%>" accept-charset="UTF-8">
		<p>
		Concept label: <input type="text" name="s" size="40" />
		lang: <input type="text" name="lang" size="2" />
		<input type="submit" name="okBtn" value="OK" />
		</p>
	</form> 
	
</div><%//graybox %>
<%} // isProto %>

<div class="graybox">
	<% fn = I18l.pathToI18lFile("/welcomepage/license.jsp", session, servletContext); %>
	<jsp:include page="<%=fn%>"/>
</div><%//graybox %>
<!--/home.jsp-->