<!--thesaurus.jsp--><%/** */%><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*, java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Thesaurus jsp = (Jsp_Thesaurus) request.getAttribute("net.semanlink.servlet.jsp");%><%/////////////////////////////////////// ACTIV FOLDERS
if (false) {jsp.prepareActivFiles();%><div class="graybox">	<div class="what">Activ folders</div>	<jsp:include page="doclist.jsp"/></div><%
} // if (false)
%>
<%/////////////////////////////////////// ANCETRESjsp.prepareKwsWithoutParentsList();%><div class="graybox">	<div class="what"><%=jsp.i18l("tag.ancestors")%></div>	<div class="horizEnumeration">		<jsp:include page="kwlist.jsp"/>	</div></div><%/////////////////////////////////////// LISTE DES KEYWORDSjsp.prepareKwsList();
java.text.MessageFormat messageFormat = new java.text.MessageFormat(jsp.i18l("x.tags"));
Object[] args = new Object[1];
args[0] = Integer.toString(jsp.getSize());
String ntags = messageFormat.format(args);		
%><div class="graybox">	<div class="what"><%=ntags%></div>	<div class="horizEnumeration">		<jsp:include page="kwlist.jsp"/>	</div></div><!--/thesaurus.jsp-->