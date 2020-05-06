<!--comment.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Resource jsp = (Jsp_Resource) request.getAttribute("net.semanlink.servlet.jsp");boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));SLResource x = jsp.getSLResource();String uri = x.getURI();String comment = x.getComment();if (comment == null) comment = "";// beware, comment can contain markdown or html (old way of storing formated content in sl:comment)// In both cases the "<" and ">" have been replaced by &lt; and &gt; in the rdf xml// They are converted back to "<" and ">" when we receive (that's what we see in JenaUtils.getComment())if (!edit) {		// The idea: we put the comment (typically markdown) inside a div,	// and once the page is loaded, js convert the markdown in place.	// pb : markdown may contain <http://www.a.com>    // which cannot be included as such inside a div.    //    // That's why I first put the raw comment inside a text area    // Then, in js, once the page is loaded, I convert this content to html    // (when it is md : else, it is already correct html)    // and replace the inner of the containg div with this html    // (removing the text area)	if ((comment != null) && (!("".equals(comment)))) {        %><div class="graybox"><div id= "slcomment" class="markdown" property="rdfs:comment"><textarea><%=comment%></textarea></div></div><%      	} // comment !null} else { // edit	String docorkw;	if (x instanceof net.semanlink.semanlink.SLDocument) {		docorkw = "doc";	} else {		docorkw =  "kw";	}	%>	    <script>    function createCommentEditor() {        var $ = function (id) { return document.getElementById(id); };        new Editor($("slcomment_raw"), $("slcomment_formatted"));    }    </script>		    <%    jsp.addOnLoadEvents("createCommentEditor");    %><!--     <div class="graybox" ondrop="drop(event)" ondragover="allowDrop(event)" style="background-color:pink">   -->    <div class="graybox">        <div class="what"><%=jsp.i18l("x.comment")%></div>        <p></p> 			<html:form action="setcomment">				<html:hidden property="uri" value="<%=uri%>" />				<html:hidden property="docorkw" value="<%=docorkw%>" />				<textarea id="slcomment_raw" name="comment" cols="80" rows="5" 				    oninput="this.editor.update()" ondrop="dropToComment(event)"><%=comment%></textarea>					<!-- html:hidden property="property" value="comment" / -->					<html:select property="lang">						<html:option value="-">-</html:option>						<html:option value="fr">fr</html:option>						<html:option value="en">en</html:option>						<html:option value="es">es</html:option>						<html:option value="pt">pt</html:option>					</html:select>					<html:submit property="<%=Action_SetOrAddProperty.SET%>"><%=jsp.i18l("x.setComment")%></html:submit>               			</html:form>            <div id= "slcomment_formatted" class="markdown" property="rdfs:comment"></div>	</div>		<% // graybox} // edit or not%><!--/comment.jsp-->