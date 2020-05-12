<!--kwlistedit.jsp-->
<%
/**
 * Suppose défini un bean Bean_KwList dans un attribut de request
 */
%>
<%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*, java.util.*"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<div class="clearboth"></div> <%// à cause du float left pour le horiztitle %>
<%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
Bean_KwList truc = (Bean_KwList) request.getAttribute("net.semanlink.servlet.Bean_KwList");
String contextPath = request.getContextPath();
%>

	<%
		List list = truc.getList();
		String field = truc.getField();
		int tabindex = 0;
		// TODO change
		String docorkwuri = null;
		if ("tags".equals(field)) {
			docorkwuri = "docuri";
			tabindex = 2;
		} else if ("parents".equals(field)) {
			docorkwuri = "kwuri";
			tabindex = 2;
		} else if ("children".equals(field)) {
			docorkwuri = "kwuri";
			tabindex = 12;
		} else if ("friends".equals(field)) {
			docorkwuri = "kwuri";
			tabindex = 7;
		} else {
			field = "unknown field";
			tabindex = 19;
		}
		
		// BOF BOF ci dessous, pour les add, utilisait jsp.getUriFormValue() ???????
		// Beware, no background-color to have aqua buttons in safari // @find aquaform: safari 3
		%>
		<p></p>
        <html:form action="edittaglist">
            <html:hidden property="<%=docorkwuri%>" value="<%=truc.getUri()%>" />
            <html:hidden property="field" value="<%=field%>" />
            <%int n = list.size();
            if (n > 0) {
                %><ul><%
                    for (int i = 0; i < n; i++) {
                        SLKeyword ke = (SLKeyword) list.get(i);
                        String value = java.net.URLEncoder.encode(ke.getURI(),"UTF-8");
                        %>
                        <li><html:multibox property="kwuris" value="<%=value%>"/><a href="<%=response.encodeURL(truc.getHREF(contextPath, i))%>"><%=ke.getLabel()%></a></li>
                        <%
    
                    } // for
                %><p></p></ul>
                    <input type="submit" name="cut" tabindex="<%=tabindex+3%>" value="<%=jsp.i18l("x.cut")%>" />
                    <input type="submit" name="remove" tabindex="<%=tabindex+4%>" value="<%=jsp.i18l("x.remove")%>" />
            <%} // (n > 0) 
            SLKeyword[] clipboardKws = (SLKeyword[]) session.getAttribute("net.semanlink.servlet.ClipboardKeyword");
            if (clipboardKws != null) {
            %>
                    <input type="submit" name="paste" tabindex="<%=tabindex+2%>" value="<%=jsp.i18l("x.paste")%>" />
            <%}%>
        </html:form>
<!--kwlistedit.jsp-->
