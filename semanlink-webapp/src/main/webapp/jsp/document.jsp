<!--document.jsp--><%/** */%><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,net.semanlink.util.*, java.util.*, java.io.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Document jsp = (Jsp_Document) request.getAttribute("net.semanlink.servlet.jsp");SLDocument x = (SLDocument) jsp.getSLResource();String uri = x.getURI();boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));

/*boolean issicgrdd = (
		(jsp instanceof net.semanlink.sicg.Jsp_SicgArticle)
		|| (jsp instanceof net.semanlink.sicg.Jsp_SicgRdd)
		|| (jsp instanceof net.semanlink.sicg.Jsp_RddIndex) );*/
boolean issicgrdd = false;
File file = null;if (jsp.isFile()) file = jsp.getFile();
if (!edit) {%>
	<div class="doctitle">
		<%=jsp.getTitleInTitle()%>
	</div> <!-- class="title" -->
<%} else { // edit %>
	<div class="graybox">
		<div class="what"><%=jsp.i18l("doc.title")%></div>
		<html:form action="/setoraddproperty">
		<html:hidden property="uri" value="<%=uri%>" />
		<html:hidden property="docorkw" value="doc" />
		<html:hidden property="property" value="dc:title"/>
		<textarea name="docTitle" cols="80" rows="2"><%=jsp.getTitle()%></textarea>
		<br/>
		<html:select property="lang">
			<html:option value="-">-</html:option>
			<html:option value="fr">fr</html:option>
			<html:option value="en">en</html:option>
			<html:option value="es">es</html:option>
			<html:option value="pt">pt</html:option>
		</html:select>
		<html:submit property="<%=Action_SetOrAddProperty.SET%>">Set title</html:submit>
		</html:form>
	</div> <!-- class="graybox" -->






<%
}

String pagePathInfo = jsp.getPagePathInfo();if (pagePathInfo != null) {	%><jsp:include page="<%=pagePathInfo%>" flush="true" /><%}%>
<jsp:include page="documenttags.jsp"/><% /////////////////////////////////////////// AU CAS OU CE DOC EST UN DOSSIER, AFFICHAGE EVENTUEL D'UNE DE SES IMAGES %><% /////////////////////////////////////////// DOC'S CONTENT %><div class="doccontent"><%{ // blocif (jsp.isImage()) {	%>	<div class="docimage">	<a href="<%=jsp.getHREF()%>"><img src="<%=jsp.getHREF()%>" width="200" /></a>	</div>	<div class="clearboth"></div>	<%} else {		if (file != null) {		if (file.isDirectory()) {						Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");			boolean imagesOnly = false;			if (imagesOnlyB != null) {				imagesOnly = imagesOnlyB.booleanValue();			}			if (imagesOnly) {				%><jsp:include page="imagelist.jsp"/><%			} else { 				jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste (et voir				// ainsi ceux qui n'ont pas été traités				request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());								%>				<div class="graybox">					<div class="what">Folder containing <%=jsp.getDocList().getList().size()%> documents</div>					<jsp:include page="/jsp/image.jsp" flush="true" />					<jsp:include page="doclist.jsp"/>					<div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%>				</div>				<%							} // if imagesOnly or not			%>			<%		} // if (file.isDirectory())	} // 	if (jsp.isLocal()) {} // if} // block%></div> <!--/doccontent-->
<% /////////////////// COMMENT		 %>							 
<jsp:include page="comment.jsp"/>
<% /////////////////////////////////////////// PROPERTIES %><%request.removeAttribute("net.semanlink.servlet.background"); // sinon, pas le titre "properties" au moins si rdd%><jsp:include page="properties.jsp"/>
<%
boolean isNote = Note.isNote(uri);
if (!isNote) {
String href = jsp.getHREF();
%><% /////////////////////////////////////////// FILE INFO %><div class="graybox">	<div class="what"><%=jsp.i18l("doc.fileInfo")%></div>	<ul>		<li>URI: <a href="<%=href%>"><%=href%></a></li>		<% 		if (file != null) {
			if ( (uri.startsWith("file:")) || (jsp.isEditor()) ) {				%><li><%=jsp.i18l("doc.path")%> <%=file.getPath()%></li><%			}
			if (file.exists()) {
				
				if (false) {%><li><a href="<%=jsp.getLocalHREF()%>">File protocol URI</a></li><%}	
				
				
				
				
				
				long fileSize = file.length();				String sSize = null;				if (fileSize < 1024) {					sSize = jsp.getI18l().getFormatedMessage("doc.bytes", Long.toString(fileSize));				} else {					long ko = fileSize / 1024;					long reste = fileSize - 1024*ko;					if (reste > 512) ko++;					sSize = jsp.getI18l().getFormatedMessage("doc.kbytes", Long.toString(ko));
				}								%> 				<li><%=sSize%></li>				<li><%=jsp.i18l("doc.lastModified")%> <%=(new YearMonthDay(new Date(file.lastModified()))).getYearMonthDay("-")%></li>				<%			} else {
				
				
				
				%>				<li><%=jsp.i18l("doc.noSuchFile")%></li>				<%			}
			
			if (!(issicgrdd)) {
				if (jsp.isEditor()) {
					// liens vers doc, folder, finder
					%><li><a href="<%=jsp.getFolderPage(response)%>"><%=jsp.i18l("doc.parentFolderInSL")%></a></li><%
					%><li><a href="<%=jsp.getLocalFolderHREF()%>"><%=jsp.i18l("doc.parentFolderOnDisk")%></a> <%=jsp.i18l("doc.cpNavBar")%></li><%		
				} // jsp.isEditor() or not
			} // if (!(issicgrdd))																	} // file null or nor				SLDocument localCopy = jsp.getLocalCopy();		if (localCopy != null) {			%><li><a href="<%=localCopy.getURI()%>"><%=jsp.i18l("doc.localCopy")%></a></li><%		}
		%>			</ul>	               	</div><% /////////////////////////////////////////// IT IS A MARKDOWN FILE %><%  boolean isMarkDown = uri.endsWith(".md");  if (isMarkDown) {  	if (!edit) {  		      //      // NOT EDIT      //        	%>    <script>    function doMarkdown() {        var mddiv = document.getElementById("md");        displayMarkdown("<%=uri%>");    }    </script>    <%  	jsp.addOnLoadEvents("doMarkdown");    %>         <div class="graybox">    <div id="md" class="markdown">    <p>DISPLAY MARKDOWN</p>    </div></div><%// /markdown/graybox          	} else { // edit  		      //      // EDIT      //  		%>  		  	<script>//     function rawMarkdown() {//         // var mddiv = document.getElementById("rawmd");<%--         displayRawMarkdown("<%=uri%>"); --%>//     }//     function doMarkdown() {//         // var mddiv = document.getElementById("md");<%--         displayMarkdown("<%=uri%>"); --%>//     }        // in edit mode: display styled and raw markdown    function editMarkdown() {    	displayEditMarkdown("<%=uri%>");    }        function createEditor() {        var $ = function (id) { return document.getElementById(id); };        new Editor($("rawmd"), $("md"));    }        function saveMarkdown() {        var xhr = new XMLHttpRequest();        var url = '<%=jsp.getContextUrl()%>' + '/savemd.do';        xhr.open("POST", url, true);        // Send the proper header information along with the request        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");                xhr.onload = function() {            if (xhr.status === 200) {                // alert("saveMarkdown success " + xhr.responseText);            } else {                alert('Request failed.  Returned status: ' + xhr.status);            }        };                // pas bon//         var data = new FormData(); data.append(...)        var data = "docuri=" + encodeURIComponent('<%=uri%>');        data = data + "&content=" + encodeURIComponent(document.getElementById("rawmd").value);        xhr.send(data);                // pb cache (au moins avec safari)        invalidateCache('<%=uri%>');    }            </script>        <%    jsp.addOnLoadEvents("createEditor");    jsp.addOnLoadEvents("editMarkdown");    %>  		  		  		    <div class="graybox">        <div class="what"><%=jsp.i18l("markdown.raw")%></div>  		 <textarea id="rawmd" oninput="this.editor.update()"              rows="12" cols="60">Type **Markdown** here.</textarea> 		 <br/>		 <input type="submit" value="Save" onclick="saveMarkdown()"/>		    </div>  		  		  		  		  		  		  		  		    <div class="graybox">    <div id="md" class="markdown">    <p>DISPLAY MARKDOWN</p>    </div></div>		  		  		<%	    	}  } 
} // (if isNote) %><!--/document.jsp-->