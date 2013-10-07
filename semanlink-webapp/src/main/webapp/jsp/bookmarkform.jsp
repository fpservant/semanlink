<!--bookmarkform.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
Boolean oneBtnOnlyB = (Boolean) request.getAttribute("oneBtnOnly");boolean oneBtnOnly = false;if (oneBtnOnlyB != null) {	oneBtnOnly = oneBtnOnlyB.booleanValue();}
//@find nir2tag
String nonInformationResourceUri = (String) request.getAttribute("nonInformationResourceUri");
%><div class="graybox">
	<%=jsp.i18l("bookmarkform.1")%>
</div>
<div class="graybox">
	<script type="text/JavaScript">
		function godoc(s) {
			location.href=s;
		}	
		function showgodoc(s) {
			window.status=s;
		}	
		function valueByName(eltName) {
			return document.getElementsByName(eltName)[0].value;
		}
	</script>
	<%
	// I was using godoc(docuri.value)
	// works in safari (2.0.4), but doesn't in firefox (2.0.0.4)
	// (this link is necessary because we cannot go back to page where we clicked the bookmarklet: this is a pb with the bookmarklet.
	// Here, we go to the page given by the content of the input field - which is predocumented after the click on the bookmarklet)
	//
	// the onMouseOver doesn't work onMouseOver="showgodocByFldName('docuri')" (neither in safari, nor in firefox)
	// 
	%>
	<html:form action="bookmark" method="post">
		<b><a onclick="godoc(valueByName('docuri')); return false;" onMouseOver="showgodoc(valueByName('docuri'));return true" href="#" ><%=jsp.i18l("bookmarkform.docuri")%></a></b> <%=jsp.i18l("bookmarkform.docuri2")%><br/>		<html:text property="docuri" size="70"/><br/>		<%if (!oneBtnOnly) {%>		<b><a href="#" onclick="godoc(valueByName('downloadfromuri')); return false;" onMouseOver="showgodoc(valueByName('downloadfromuri'));return true"><%=jsp.i18l("bookmarkform.downloaduri")%></a></b> <%=jsp.i18l("bookmarkform.downloaduri2")%><br/>		<html:text property="downloadfromuri" size="70"/><br/>		<%}%>
		<%
		if (false) { // tentative ds bookmarklet de trouver le lien vers une nir
			if ((SLServlet.isProto()) && (nonInformationResourceUri != null)) { %>
				<b><%=jsp.i18l("bookmarkform.nir")%></b><br/>
				<html:text property="nir" size="70"/><br/>
			<%}
		} // if (false) %>
		<b><%=jsp.i18l("x.title-colon")%></b><br/>		<html:text property="title" size="70"/><br/>		<b><%=jsp.i18l("x.comment-colon")%></b><br/>		<html:textarea property="comment" cols="70" rows="10"/><br/>		<!--<b>Lang:</b> <html:text property="lang" size="10"/> -->		<!--	<html:select property="lang">				<html:option value="-">-</html:option>				<html:option value="fr">fr</html:option>				<html:option value="en">en</html:option>				<html:option value="es">es</html:option>				<html:option value="pt">pt</html:option>			</html:select> -->
		<br/>		<%		if (oneBtnOnly) {%>			<html:submit property="bookmarkBtn"><%=jsp.i18l("x.bookmark")%></html:submit>		    <%		} else {%>			<html:submit property="bookmarkBtn"><%=jsp.i18l("x.bookmark")%></html:submit>			<html:submit property="bookmarkWithCopyBtn"><%=jsp.i18l("bookmarkform.bookmarkAndCopy")%></html:submit>			<html:submit property="localDocBtn"><%=jsp.i18l("bookmarkform.copy")%></html:submit>			<html:submit property="copyWithBookmarkBtn"><%=jsp.i18l("bookmarkform.copyWithSource")%></html:submit>
			<%
			if (false) { // tentative ds bookmarklet de trouver le lien vers une nir
				if (nonInformationResourceUri != null) { // @find nir2tag %>
					<br/>
					<html:submit property="nirTagBtn"><%=jsp.i18l("bookmarkform.createTag")%></html:submit>
				<%}
			} // if (false)
				
			// @find bookmark2tag
			%>
			<br/>
			<html:submit property="bookmark2tagBtn"><%=jsp.i18l("bookmarkform.createTag")%></html:submit>
			<%
		}		%>	</html:form> 
<%if (!oneBtnOnly) {%>
	<div class="graybox">
		<%=jsp.i18l("bookmarkform.2")%><br/>
	</div>
<%}%>


</div>

<!--/bookmarkform.jsp-->