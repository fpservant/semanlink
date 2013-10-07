<!--tagOutsideLinks.jsp--><%/** */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*,net.semanlink.semanticsemanticweb.*, java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");SLResource x = jsp.getSLResource();
String uri =x.getURI();

String dbPediaURI = (new DBPedia()).getResourceURI(jsp.getSLKeyword().getLabel(),null); // TODO lang
%>
<div class="graybox">
	<div class="what">Maybe Same As</div>
<%
if (!edit) {		%>				<ul>				<%					%><li><a href="<%=dbPediaURI%>"><%=dbPediaURI%></a></li><%				%>				</ul>		<%} else { // edit
	

	
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
	
	
	
	
	} // if edit%>
</div> <% // graybox %>
<!--/tagOutsideLinks.jsp-->