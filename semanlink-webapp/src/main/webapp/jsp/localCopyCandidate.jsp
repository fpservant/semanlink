<!--localCopyCandidate.jsp-->
<%
/**
 * LOCAL COPY CANDIDATE // 2020-12
 * to be included within localCopyEdit when localCopy == null
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

SLDocumentStuff localCopyCandidate = jsp.localCopyCandidate(jsp.getTitle(), true, jsp.getContextURL()); // 2020-07
if (localCopyCandidate != null) {
  %>
  <html:form action="setoraddproperty" method="POST">
  No local copy defined. Following file seems to match: 
  <a href="<%=localCopyCandidate.getURI()%>"><%=localCopyCandidate.getFile().getName()%></a>.
  Set it as Local Copy?
  <html:hidden property="uri" value="<%=localCopyCandidate.getURI()%>" />
  <html:hidden property="docorkw" value="doc" />
  <html:hidden property="property" value="dc:source" />
  <html:hidden property="value" value="<%=uri%>" />
  <html:hidden property="redirect_uri" value="<%=uri%>" />
  <html:submit property="<%=Action_SetOrAddProperty.ADD%>">Do it!</html:submit>
  </html:form>
  
  <script>
//               function setLocalCopyFile(uri) {
//                   files = document.getElementById('localCopyFileChooser').files;
//                   if ((!files) || (files.length == 0)) {
//                       alert("No file chosen")
//                   }
//                   for (var i = 0; i < files.length; i++) {
//                 	  alert(files[i]);
//                       setLocalCopyFile2(uri, files[i]);
//                   }                
//               }
                           
//               function setLocalCopyFile2(uri, file) {
//             	  // NO WAY: no way to get the path of a file from the file object in javascript
//                   var formData = new FormData();
//                   // formData.append("file", file); // this would be for formdata, but not given what is done below
//                   formData.append("file", file.name);
//                   formData.append("docorkw", "doc");
//                   formData.append("property", "dc:source");
//                   formData.append("value", uri);
//                   formData.append("redirect_uri", uri);
<%--                   formData.append("<%=Action_SetOrAddProperty.ADD%>", 'true'); --%>
//                   alert(file.name);
 
//                   var xhr = new XMLHttpRequest();
//                   xhr.addEventListener("load", setLocalCopyFileComplete, false);
//                   // 2020-09 TODO
//                   xhr.open("POST", getContextPath() + "/setoraddproperty.do", true); // If async=false, then you'll miss progress bar support.
//                   xhr.setRequestHeader("content-type", "application/x-www-form-urlencoded");
                  
//                   // this doesn't work, given what is done in the servlet: 
//                   // we ends up with something getParameter(paramName)
//                   // xhr.send(formData);
//                   // following borrowed from https://ultimatecourses.com/blog/transform-formdata-into-query-string
//                   const formDataAsString = [...formData.entries()]
//                   .map(x => `${encodeURIComponent(x[0])}=${encodeURIComponent(x[1])}`)
//                   .join('&');
//                   alert(formDataAsString);
//                   xhr.send(formDataAsString);
//               }

//               function setLocalCopyFileComplete(event) {
//                   if (event.target.status == 200) {
<%--                       window.location.href = '<%=uri%>'; --%>
//                   } else {
//                       alert('Error ' + event.target.responseText); // TODO change servlet
//                   }
//               }             
              </script>
  
<!--               <input type="file" name="file2" id="localCopyFileChooser"> -->
<%--               <button onclick="setLocalCopyFile('<%=uri%>')">Set as Local Copy</button> --%>
  
<%}%>
<!--/localCopyCandidate.jsp-->