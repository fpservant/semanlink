<!--keywordLabelEdit.jsp--><%/** * Edit the labels of a keyword //  2021-01 */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%
Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");SLKeyword kw = (SLKeyword) jsp.getSLResource();LabelLN lln = kw.getLabelLN();String lab = lln.getLabel();String lang = lln.getLang();request.setAttribute("net.semanlink.servlet.jsp.lang", lang);%><div class="graybox">		<div class="what"><%=jsp.i18l("tag.label")%></div>		<p></p>		<html:form action="setkwlabel">			<html:text property="kwlabel" value="<%=lab%>" size="60"/>			<html:hidden property="kwuri" value="<%=kw.getURI()%>" />			<jsp:include page="/jsp/langSelect.jsp" flush="true" />			<html:submit property="okBtn">OK</html:submit>		</html:form></div> <!-- class="graybox" --><!--/keywordLabelEdit.jsp-->