<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<h1>Logon</h1>
<html:form action="logon">
    <label for="username">Username:</label>
    <html:text property="username"/>
    <label for="pass">Password:</label>
    <html:password property="password" />
    <html:submit property="okBtn">OK</html:submit>
</html:form>
</div>
