<!--documenttags.jsp--><%/** */%><%@ page language="java" session="true" import="net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Resource jsp = (Jsp_Resource) request.getAttribute("net.semanlink.servlet.jsp");boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));jsp.prepareParentsList();// 2019-02 // 2021-01 dragndrop%>		<%if (edit) {%><div id="documenttags" class="graybox"        ondrop="dropToTagList(event)"        ondragover="dragOver(event)"        ondragenter="dragEnter(event)"         ondragleave="dragLeave(event)"         ondragstart="dragStart(event)"         ondragend="dragEnd(event)">			<div class="horizEnumeration">
			<div class="what">Tags: </div>			<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />			</div></div>		<%} else {%><div id="documenttags" class="graybox">			<span class="horizEnumerationTitle">Tags: </span>			<div class="horizEnumeration">
			<jsp:include page="/jsp/kwlist.jsp" flush="true" />			<div class="clearboth"></div>			</div></div>		<%} // if edit or not%><!--/documenttags.jsp-->