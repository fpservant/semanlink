<!--bookmarkform.jsp-->
<%@ page language="java" session="true" import="net.semanlink.servlet.*"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
Boolean oneBtnOnlyB = (Boolean) request.getAttribute("oneBtnOnly");
boolean oneBtnOnly = false;
if (oneBtnOnlyB != null) {
    oneBtnOnly = oneBtnOnlyB.booleanValue();
}

%>
<div class="graybox">
    <html:form action="bookmark" method="post">
        <b><%=jsp.i18l("bookmarkform.docuri")%></b> <%=jsp.i18l("bookmarkform.docuri2")%><br/>
        <html:text property="docuri" size="70"/><br/><br/>
        <%if (!oneBtnOnly) {%>
        <b><%=jsp.i18l("bookmarkform.downloaduri")%></b><br/>
        <html:text property="downloadfromuri" size="70"/><br/><br/>
        <%}%>
        <b><%=jsp.i18l("x.title-colon")%></b><br/>
        <html:text property="title" size="70"/><br/><br/>
        <b><%=jsp.i18l("x.comment-colon")%></b><br/>
        <html:textarea property="comment" cols="70" rows="10"/><br/>
        <!--<b>Lang:</b> <html:text property="lang" size="10"/> -->
        <!--    <html:select property="lang">
                <html:option value="-">-</html:option>
                <html:option value="fr">fr</html:option>
                <html:option value="en">en</html:option>
                <html:option value="es">es</html:option>
                <html:option value="pt">pt</html:option>
            </html:select> -->
        <br/>
        <%
        if (oneBtnOnly) {%>
            <html:submit property="bookmarkBtn"><%=jsp.i18l("x.bookmark")%></html:submit>
            <%
        } else {%>
        <span>
            <html:submit property="bookmarkBtn"><%=jsp.i18l("x.bookmark")%></html:submit>
            <html:submit property="bookmarkWithCopyBtn"><%=jsp.i18l("bookmarkform.bookmarkAndCopy")%></html:submit>
            <html:submit property="copyBtn"><%=jsp.i18l("bookmarkform.copy")%></html:submit>
        </span>
            <span style="float: right">
            <html:submit property="bookmark2tagBtn"><%=jsp.i18l("bookmarkform.createTag")%></html:submit>
        </span>
        <%}%>
    </html:form> 


</div>


<!--/bookmarkform.jsp-->