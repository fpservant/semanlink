<!--localCopyEdit.jsp-->
<%
/**
 * EDIT LOCAL COPY // 2020-12*
 */
%>
<%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,net.semanlink.util.*, java.util.*, java.io.*"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
Jsp_Document jsp = (Jsp_Document) request.getAttribute("net.semanlink.servlet.jsp");
SLDocument x = (SLDocument) jsp.getSLResource();
String uri = x.getURI();
SLDocumentStuff docStuff = jsp.getSLDocumentStuff(); // 2019-04 

SLDocument localCopy = docStuff.getLocalCopy();

String cur_s = "", cur_p = "", cur_o = "", cur_lang = ""; 
String cur_sfile = "";
String s = "", p = "", o = "", lang = "";
String sfile = "";

SLDocumentStuff localCopyStuff = null;
if (localCopy != null) {
	cur_s = localCopy.getURI();
	cur_p = "dc:source";
	cur_o = uri;
	//
	localCopyStuff = docStuff.getLocalCopyStuff();	
// 	SLDocumentStuff.HrefPossiblyOpeningInDestop localCopyLink = docStuff.getLocalCopyLink();
// 	String localHref = localCopyLink.href();
	cur_sfile = localCopyStuff.getFile().getAbsolutePath();
}

s = cur_s;
sfile = cur_sfile;
p = "dc:source";
o = uri;

%>

<div class="graybox" id="local_copy">
<div class="what"><%=jsp.i18l("doc.localCopy")%></div>
<p></p>


<%if (localCopy == null) {%>
<jsp:include page="localCopyCandidate.jsp"/>
<%} else {%>
   <p>
   <%
   SLDocumentStuff.HrefPossiblyOpeningInDestop localCopyLink = docStuff.getLocalCopyLink();
   String localHref = localCopyLink.href();
   String lab = jsp.i18l("doc.localCopy");
   // String lab = localCopyStuff.getFile().getAbsolutePath();

   if (localCopyLink.openingInDesktop()) {
     %><a href="<%=localHref%>" onclick="desktop_open_hack('<%=localHref%>'); return false;"><%=lab%></a>
     <%
   } else {
     String href = response.encodeURL(localHref);
     %><a href="<%=href%>"><%=lab%></a><% 
   }
   String page_href = response.encodeURL(docStuff.getLocalCopyPage());
   %>
   <i><a href="<%=page_href%>"><%=jsp.i18l("doc.about")%></a></i>
   </p>
   <%
}
%>


<p>
<html:form action="changetriple" method="POST">
    File: 
    <%if (localCopy != null) {%>
        <html:hidden property="cur_s" value="<%=cur_s%>" />
        <html:hidden property="cur_p" value="<%=cur_p%>" />
        <html:hidden property="cur_o" value="<%=cur_o%>" />
        <html:hidden property="cur_lang" value="<%=cur_lang%>" />
    <%}%>
<%--     <input type="text" name="s" value="<%=s%>" style="width:100%; box-sizing: border-box;-webkit-box-sizing:border-box;-moz-box-sizing: border-box"> --%>
    <input type="text" name="sfile" value="<%=sfile%>" style="width:100%; box-sizing: border-box">
    <html:hidden property="p" value="<%=p%>" />
    <html:hidden property="o" value="<%=o%>" />
    <html:hidden property="lang" value="<%=lang%>" />
    <html:hidden property="docorkw" value="doc" />
    <html:hidden property="redirect_uri" value="<%=uri%>" />
    <html:submit property="<%=Action_SetOrAddProperty.SET%>">Set</html:submit>
 </html:form>
</p>
</div>

<!--/localCopyEdit.jsp-->