<!--tagOutsideLinks.jsp-->
String uri =x.getURI();

String dbPediaURI = (new DBPedia()).getResourceURI(jsp.getSLKeyword().getLabel(),null); // TODO lang
%>
<div class="graybox">
	<div class="what">Maybe Same As</div>
<%

	

	
	String docorkw;
	if (x instanceof net.semanlink.semanlink.SLDocument) {
		docorkw = "doc";
	} else {
		docorkw =  "kw";
	}

	%>
	<ul>
	<%
		%><li><a href="<%=dbPediaURI%>"><%=dbPediaURI%></a>
		
		
		<html:form action="setoraddproperty">
			<html:hidden property="uri" value="<%=uri%>" />
			<html:hidden property="docorkw" value="<%=docorkw%>" />
			<html:hidden property="property" value="http://www.semanlink.net/2001/00/semanlink-schema#sameAs" />
			<html:hidden property="value" value="<%=dbPediaURI%>" />
			<html:submit property="<%=Action_SetOrAddProperty.ADD%>">Yes it is!</html:submit>
		</html:form>
		
		
		</li><%
	%>
	</ul>
<%
	
	
	
	
	
</div> <% // graybox %>
