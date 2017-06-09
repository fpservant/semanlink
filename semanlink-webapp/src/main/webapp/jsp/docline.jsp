<%

SLModel mod = SLServlet.getSLModel();
		String docLabel = null;
		if (jsp != null) {
			docLabel = jsp.getLabel(doc);
		} else {
			docLabel = doc.getLabel();
		}
		if (docLabel != null) docLabel = Util.toHTMLOutput(docLabel);
			    %><a href="<%=Util.handleAmpersandInHREF(uri)%>"><span property="rdfs:label"><%=docLabel%></span></a><% // 2013-08 RDFa
		
		SLDocument localCopy = mod.source2LocalCopy(uri);
		if (localCopy != null) {
			%> <i>(<a href="<%=localCopy.getURI()%>"><%=jsp.i18l("doc.localCopy")%></a>)</i><%
		} else if (SLServlet.getWebServer().owns(uri)) {
			SLDocument source = mod.doc2Source(uri);
			if (source != null) {
				%> <i>(<a href="<%=source.getURI()%>"><%=jsp.i18l("doc.source")%></a>)</i><%
			}
		}
		comment = Util.toHTMLOutput(comment);
	if (jsp != null) { // on a le cas jsp null avec deliciousimport 2006/10/1
	}