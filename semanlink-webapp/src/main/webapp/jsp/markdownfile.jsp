<!--markdownfile.jsp-->
<%
/**
 */
%>
<%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*"%>
<%
String contextPath = request.getContextPath();
%>
<style>

/* SEE ALSO markdown-sl.js for paremetring */

.table-of-contents ol { counter-reset: list-item-toc; }
.table-of-contents li { display: block; counter-increment: list-item-toc; }
.table-of-contents li:before { content: counters(list-item-toc,'.') ' '; }

/* More about nested counters @ <https://www.w3.org/TR/css-lists-3/> */
</style>

 <script src="<%=contextPath%>/scripts/markdown-it-anchor/dist/markdownItAnchor.umd.js"></script>
 <script src="<%=contextPath%>/scripts/markdown-it-toc-done-right/uslug.js"></script>
 <script src="<%=contextPath%>/scripts/markdown-it-toc-done-right/dist/markdownItTocDoneRight.umd.js"></script>
<%
Jsp_Document jsp = (Jsp_Document) request.getAttribute("net.semanlink.servlet.jsp");
SLDocument x = (SLDocument) jsp.getSLResource();
String uri = x.getURI();
SLDocumentStuff docStuff = jsp.getSLDocumentStuff(); // 2019-04 
boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));

String rawMdUrl = docStuff.getRawMarkdownUrl();
boolean isMarkDown = (rawMdUrl != null);
if (!isMarkDown) return;

if (!edit) {
     
    //
    // NOT EDIT
    //
 
    // 2019-05: change below displayMarkdownDocument(uri, ) to the url to download the raw md from
    %>
    <script>
    function doMarkdown() {
        displayMarkdownDocument("<%=rawMdUrl%>", "<%=jsp.getRequestURL()%>", "<%=docStuff.getHrefPossiblyOpeningInDestop(true).href()%>");

        // 2020-09: this was annoying, actually
//      // 2020-01 rightbar : by default, no rightbar if dispalying a md file
//      if (!getRightBarString()){
//          // if rightbar state not otherwise defined, hide it
//          displayRightBar(false);
//      }
    }
    </script>
    <%
    jsp.addOnLoadEvents("doMarkdown");
    %>
     
    <div class="graybox">
    <div id="md" class="markdowndoc">
        <p>DISPLAY MARKDOWN</p>
    </div></div><%// /markdown/graybox
 
 
} else { // edit
    
    //
    // EDIT
    //

    %>
    
    <script>
    
    // in edit mode: display styled and raw markdown
    function editMarkdown() {
        displayEditMarkdown("<%=rawMdUrl%>");
    }
    
    function createEditor() {
        var $ = function (id) { return document.getElementById(id); };
        new Editor($("rawmd"), $("md"));
    }
    
    function saveMarkdown() {
        var xhr = new XMLHttpRequest();
        var url = '<%=jsp.getContextUrl()%>' + '/savemd.do';
        xhr.open("POST", url, true);

        // Send the proper header information along with the request
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                // alert("saveMarkdown success " + xhr.responseText);
            } else {
                alert('Request failed.  Returned status: ' + xhr.status);
            }
        };
        
        // pas bon
        // var data = new FormData(); data.append(...)

		var uriq = '<%=uri.replace("\'", "\\'")%>';
        var data = "docuri=" + encodeURIComponent(uriq);
        data = data + "&content=" + encodeURIComponent(document.getElementById("rawmd").value);
        xhr.send(data);
        
        // pb cache (au moins avec safari)
        invalidateCache(uriq);
    }
    </script>
     
    <%
    jsp.addOnLoadEvents("createEditor");
    jsp.addOnLoadEvents("editMarkdown");
    %>
         
         
         
    <div class="graybox">
        <div class="what"><%=jsp.i18l("markdown.raw")%></div>
         <textarea id="rawmd" name="comment" rows="12" 
                    onpaste="mypaste('rawmd')"
                    oninput="this.editor.update()" 
                    ondrop="dropToComment(event)">Type **Markdown** here.</textarea>
         
         <br/>
         <input type="submit" value="Save" onclick="saveMarkdown()"/>
    </div>
         
         
    <div class="graybox">
    <div id="md" class="markdowndoc">
    <p>DISPLAY MARKDOWN</p>
    </div></div>        
<%} // edit or not%>

<!--/markdownfile.jsp-->