<!--document.jsp--><%/** */%><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,net.semanlink.util.*, java.util.*, java.io.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Document jsp = (Jsp_Document) request.getAttribute("net.semanlink.servlet.jsp");SLDocument x = (SLDocument) jsp.getSLResource();String uri = x.getURI();SLDocumentStuff docStuff = jsp.getSLDocumentStuff(); // 2019-04 boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));/*boolean issicgrdd = (        (jsp instanceof net.semanlink.sicg.Jsp_SicgArticle)        || (jsp instanceof net.semanlink.sicg.Jsp_SicgRdd)        || (jsp instanceof net.semanlink.sicg.Jsp_RddIndex) );*/boolean issicgrdd = false;File file = docStuff.getFile();if (!edit) {%>    <div class="doctitle" id="doctitle">        <%=jsp.getTitleInTitle()%>    </div> <!-- class="title" --><%} else { // edit %>    <div class="graybox">        <div class="what"><%=jsp.i18l("doc.title")%></div>        <html:form action="/setoraddproperty">        <html:hidden property="uri" value="<%=uri%>" />        <html:hidden property="docorkw" value="doc" />        <html:hidden property="property" value="dc:title"/>        <textarea name="docTitle" cols="80" rows="2"><%=jsp.getTitle()%></textarea>        <br/>        <html:select property="lang">            <html:option value="-">-</html:option>            <html:option value="fr">fr</html:option>            <html:option value="en">en</html:option>            <html:option value="es">es</html:option>            <html:option value="pt">pt</html:option>        </html:select>        <html:submit property="<%=Action_SetOrAddProperty.SET%>">Set title</html:submit>        </html:form>    </div> <!-- class="graybox" --><%}// ne sert probablement pas // TODOString pagePathInfo = jsp.getPagePathInfo();if (pagePathInfo != null) {    %><jsp:include page="<%=pagePathInfo%>" flush="true" /><%}%><jsp:include page="documenttags.jsp"/><% /////////////////////////////////////////// IF IT IS A MARKDOWN FILE %><jsp:include page="markdownfile.jsp"/><% /////////////////////////////////////////// AU CAS OU CE DOC EST UN DOSSIER, AFFICHAGE EVENTUEL D'UNE DE SES IMAGES %><% /////////////////////////////////////////// DOC'S CONTENT %><div class="doccontent"><%{ // blocif (jsp.isImage()) {  String href = docStuff.getHref();  if (href != null) {    %>    <div class="docimage">    <a href="<%=href%>"><img src="<%=href%>" width="200" /></a>    </div>    <div class="clearboth"></div>    <%  }} else {        if (file != null) {        if (file.isDirectory()) {                        Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");            boolean imagesOnly = false;            if (imagesOnlyB != null) {                imagesOnly = imagesOnlyB.booleanValue();            }            if (imagesOnly) {                %><jsp:include page="imagelist.jsp"/><%            } else {                 jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste (et voir                // ainsi ceux qui n'ont pas �t� trait�s                request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());                                %>                <div class="graybox">                    <div class="what">Folder containing <%=jsp.getDocList().getList().size()%> documents</div>                    <jsp:include page="/jsp/image.jsp" flush="true" />                    <jsp:include page="doclist.jsp"/>                    <div class="clearboth"></div> <%//ceci est n�cessaire pour que la "graybox" contienne enti�rement l'�ventuelle image%>                </div>                <%                            } // if imagesOnly or not            %>            <%        } // if (file.isDirectory())    } //    if (jsp.isLocal()) {} // if} // block%></div> <!--/doccontent--><% /////////////////// COMMENT       %>                          <jsp:include page="comment.jsp"/><% /////////////////// COMMENT       %><%if (false) { %>                            <jsp:include page="markdownof.jsp"/><%}%><% /////////////////// cf sl:mainDoc (2019-05) %><%List<SLDocument> subdocs = x.mainDocOf();if ((x != null) && (subdocs.size() > 0)) {   DocumentFactory docFactory = Manager_Document.getDocumentFactory();  %>  <div class="graybox" id="subdocs">      <div class="what">Associated documents</div>      <ul class="maindoclist"><%      for (SLDocument subdoc : subdocs) {        request.setAttribute("net.semanlink.servlet.jsp.currentdoc", subdoc);        List kws = subdoc.getKeywords();        if ((kws != null) && (kws.size()>0)) {        	request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", kws);        } else {          request.removeAttribute("net.semanlink.servlet.jsp.currentdoc.kws");        }        %><jsp:include page="<%=docFactory.getDocLineJspName(subdoc)%>"/><%      }%>      </ul>      </div><%}%><% /////////////////////////////////////////// PROPERTIES %><%request.removeAttribute("net.semanlink.servlet.background"); // sinon, pas le titre "properties" au moins si rdd%><jsp:include page="properties.jsp"/><%boolean isNote = Note.isNote(uri);if (isNote) return;/////////////////////////////////////////// FILE INFO // 2019-04 %><div class="graybox" id="file_info">    <div class="what"><%=jsp.i18l("doc.fileInfo")%></div>    <ul>        <li>URI: <a href="<%=uri%>"><%=uri%></a></li>                                        <% // if (!SLServlet.isSemanlinkWebSite()) if (SLServlet.canOpenLocalFileWithDesktop()) { %>                <%        String bookmarkOf = docStuff.getBookmarkOf();        %><li>Bookmark of: <a href="<%=bookmarkOf%>"><%=bookmarkOf%></a></li><%         String newHref = docStuff.getHref(true);        %><li>HREF: <a href="<%=newHref%>"><%=newHref%></a></li><%          String oldAboutHref = docStuff.oldAboutHref();        %><li>OLD ABOUT HREF: <a href="<%=oldAboutHref%>"><%=oldAboutHref%></a></li><%                SLDocument source = docStuff.getSource();        if (source != null) {            %><li>Source: <a href="<%=source.getURI()%>"><%=source.getURI()%></a></li><%        }        if (file != null) {            if (false) {%><li>OLD FILE TO URI: <%=SLServlet.getSLModel().fileToUri(file)%></li><%}                        if (file.exists()) {                %><li><%=jsp.i18l("doc.path")%> <%=file.getPath()%></li><%                               long fileSize = file.length();                String sSize = null;                if (fileSize < 1024) {                    sSize = jsp.getI18l().getFormatedMessage("doc.bytes", Long.toString(fileSize));                } else {                    long ko = fileSize / 1024;                    long reste = fileSize - 1024*ko;                    if (reste > 512) ko++;                    sSize = jsp.getI18l().getFormatedMessage("doc.kbytes", Long.toString(ko));                }                                %>                 <li><%=sSize%></li>                <li><%=jsp.i18l("doc.lastModified")%> <%=(new YearMonthDay(new Date(file.lastModified()))).getYearMonthDay("-")%></li>                <%                if (!(issicgrdd)) {                    if (jsp.isEditor()) {                        // liens vers doc, folder, finder                        // 2019-04 local use of local files                        // getLocalFolderHREF                        // String localFolderHref = SLServlet.hrefLocalUseOfLocalFile(jsp.getLocalFolderHREF2(), Util.getContextURL(request));                        String localFolderHref = docStuff.uriOfParentFolder(true);                        if (localFolderHref != null) {                                                   %><li><a href="<%=localFolderHref%>"><%=jsp.i18l("doc.parentFolderOnDisk")%></a></li><%                        }                                                                                                                                            } // jsp.isEditor() or not                } // if (!(issicgrdd))                        } else {                %>                <li>NO SUCH FILE <%=jsp.i18l("doc.noSuchFile")%>: <%=file.getPath()%></li>                <%            }                                                                                } // file null or not                SLDocument localCopy = docStuff.getLocalCopy();        // System.out.println("document.jsp " + jsp.getLocalCopy()); // TODO REMOVE        if (localCopy != null) {          %><li><%         	            SLDocumentStuff.HrefPossiblyOpeningInDestop localCopyLink = docStuff.getLocalCopyLink();          if (localCopyLink.openingInDesktop()) {            %><a href="#" onclick="desktop_open_hack('<%=localCopyLink.href()%>'); return false;"><%=jsp.i18l("doc.localCopy")%></a><%            		          } else {            String href = response.encodeURL(localCopyLink.href());            %><a href="<%=href%>"><%=jsp.i18l("doc.localCopy")%></a><%           }                    String href = response.encodeURL(docStuff.getLocalCopyPage());          %> <i><a href="<%=href%>"><%=jsp.i18l("doc.about")%></a></i></li><%         }                if (!SLServlet.isSemanlinkWebSite()) {            SLModel.DocMetadataFile metadataFile = jsp.getDocMetadataFile();            %><li>rdf file: <%=metadataFile.getFile()%></li><%            // doit y avoir plus simple            SLDocumentStuff parentStuff = docStuff.parentOfRdfStuff();            SLDocumentStuff.HrefPossiblyOpeningInDestop hr = parentStuff.getHrefPossiblyOpeningInDestop(true);            if (hr.openingInDesktop()) {              %><li>rdf file's folder <a href="#" onclick="desktop_open_hack('<%=hr.href()%>'); return false;">in desktop</a><%                  // String href = response.encodeURL(hr.href()); // donne ouverture ds sl et desktop                   String href = response.encodeURL(parentStuff.getAboutHref());                  %> <a href="<%=href%>">in sl</a></li><%                                  } else {              // doesn't happen              String href = response.encodeURL(hr.href());              %><li><a href="<%=href%>">rdf file's folder</a></li><%             }                      }        }        %>                    </ul></div><!--/document.jsp-->