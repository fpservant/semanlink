<%
/**
 * Affichage d'une ligne d'une liste de documents au sein d'une Jsp_Resource : doc d'un kw, fichier d'un dossier,...
 * appelée par doclist.jsp (donc document.jsp, keyword.jsp, andKws, ...) et class WalkListener
 * @param request.getAttribute("net.semanlink.servlet.jsp.currentdoc") SLDocument en question
 * @param request.getAttribute("net.semanlink.servlet.jsp.currentdoc.kws") la liste de SLKeywords à montrer, ou null s'il ne faut pas en montrer
 */
%>
<!--docline.jsp-->
<%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.util.*"
%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
SLDocument doc = (SLDocument) request.getAttribute("net.semanlink.servlet.jsp.currentdoc");

boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
SLModel mod = SLServlet.getSLModel();
// HTML_Link link = HTML_Link.linkToDocument(doc);

// String linkToThisPage = jsp.getLinkToThis(); // ceci est mis en cache par jsp -- pas grave de l'appeler ds chaque docline.jsp de la liste
String uri = doc.getURI(); // ds le cas d'un doc servi par le web server, c bien l'url servi par le ws
// 2013-08 RDFa
%><li class="docline" about="<%=uri%>"><%
    %><span><% // ajouté pour tree - était avant au niveau LI
        if (Util.isImage(uri)) {

            //
            // IMAGE
            //
            
            // pour le script du btn "image", cf image.jsp
            SLDocument imageToBeDisplayed = mod.getDocument(uri); // 2019-03: Hum c'est pas doc? TODO check
            Jsp_Document imageJsp_doc = Manager_Document.getDocumentFactory().newJsp_Document(imageToBeDisplayed, request);
            String imageLinkPage = imageJsp_doc.getLinkToThis();

            String contextPath = request.getContextPath();
            %>
                <%// span pour ne pas avoir le décalage vers gauche mis pour les triangles des kws cf css .graybox ul li img %>
                <span style="margin-left:8px">
                <img src="<%=contextPath%>/ims/image.gif" class="imageBtn" alt="" onclick="loadImage('<%=uri%>', '<%=contextPath+imageLinkPage%>')" />
                </span>
            <%
        }  // if (Util.isImage(uri))
        
        //
        // LABEL
        //
        
        String docLabel = null;
        if (jsp != null) {
            docLabel = jsp.getLabel(doc);
        } else {
            docLabel = doc.getLabel();
        }
        if (docLabel != null) docLabel = Util.toHTMLOutput(docLabel);
        
        //
        // LINK TO DOC
        //
        
        // String docPageUrl = HTML_Link.htmlLinkPage(doc);
        String docPageUrl = null;
        if ((edit) || 
            ((uri.endsWith("/")) && ( (SLServlet.getWebServer().owns(uri)) || (uri.startsWith("file:"))))) { // cas dossier
        	docPageUrl = HTML_Link.docLink(uri);
            %><html:link page="<%=docPageUrl%>" styleClass="a1"><span property="rdfs:label"><%=docLabel%></span></html:link><% // 2013-08 RDFa
        
        } else { // !edit
            // 2017-09 similar stuff in comment.jsp @find doc2markdownHref
            String mdHref = mod.doc2markdownHref(jsp.getContextUrl(), uri);
            if (mdHref != null) { // TODO : 2019-03 uris for bookmarks
              
               %><a href="<%=mdHref%>"><span property="rdfs:label"><%=docLabel%></span></a><%
               docPageUrl = HTML_Link.docLink(uri);
               // 2018-01 LINK TO DOC PAGE ("about")
               %> <i><html:link page="<%=docPageUrl%>"><%=jsp.i18l("doc.about")%></html:link></i><%
            } else {
              // 2019-03 uris for bookmarks
              
              String bookmarkOf = doc.bookmarkOf();
              String u = null;
              if (bookmarkOf != null) {
                %><a href="<%=Util.handleAmpersandInHREF(bookmarkOf)%>"><span property="rdfs:label"><%=docLabel%></span></a><% // 2013-08 RDFa
                // 2018-01 LINK TO DOC PAGE ("about")
                %> <i><a href="<%=uri%>"><%=jsp.i18l("doc.about")%></a></i><%
             } else {
              	docPageUrl = HTML_Link.docLink(uri);
                %><a href="<%=Util.handleAmpersandInHREF(uri)%>"><span property="rdfs:label"><%=docLabel%></span></a><% // 2013-08 RDFa
                // 2018-01 LINK TO DOC PAGE ("about")
                %> <i><html:link page="<%=docPageUrl%>"><%=jsp.i18l("doc.about")%></html:link></i><%
             }
              
             
              
            }
            
            
            
        }
        
        //
        // LINK TO LOCAL COPY
        //
        
        SLDocument localCopy = mod.source2LocalCopy(uri);
        if (localCopy != null) {
 
        	
          // 2019-04 local use of local files
          String href = SLServlet.hrefLocalUseOfLocalFile(localCopy.getURI(), Util.getContextURL(request));
          
          %> <i>(<a href="<%=href%>"><%=jsp.i18l("doc.localCopy")%></a>)</i><%
            
        } else if (SLServlet.getWebServer().owns(uri)) {
            SLDocument source = mod.doc2Source(uri);
            if (source != null) {
                %> <i>(<a href="<%=source.getURI()%>"><%=jsp.i18l("doc.source")%></a>)</i><%
            }
        }
        
    %></span><%
    String comment = doc.getComment();
    if (comment != null) {
        comment = Util.toHTMLOutput(comment);
        %><br/><span class="docline_comment" property="rdfs:comment"><%=comment %></span><% // 2013-08 RDFa
    }
    %><br/><%
    
    //
    // AFFICHAGE DE LA VALEUR DE LA SORTPROPERTY
    //
    
    if (jsp != null) { // on a le cas jsp null avec deliciousimport 2006/10/1
        String prop = jsp.getSortProperty();
        // prop = "http://www.semanlink.net/2001/00/semanlink-schema#creationTime";
        // TODO  change
        if (prop != null) {
            String propVal = null;
            // if (prop.equals(SLVocab.SL_CREATION_DATE_PROPERTY)) {
            // } else if (!prop.equals(SLVocab.HAS_KEYWORD_PROPERTY)) {
            if (!prop.equals(SLVocab.HAS_KEYWORD_PROPERTY)) {
                // 2012-12: better to display the date than the time
                /*
                HashMap hm = doc.getPropertiesAsStrings();
                java.util.List al = (java.util.List) hm.get(prop);
                if (al != null) {
                    propVal = al.get(0).toString();
                } else { // times ago, we only had the DATE prop, not the TIME
                    if (prop.equals(SLVocab.SL_CREATION_TIME_PROPERTY)) {
                        al = (java.util.List) hm.get(SLVocab.SL_CREATION_DATE_PROPERTY);
                        if (al != null) propVal = al.get(0).toString();
                    }
                }
                */
                HashMap hm = doc.getPropertiesAsStrings();
                if (prop.equals(SLVocab.SL_CREATION_TIME_PROPERTY)) prop = SLVocab.SL_CREATION_DATE_PROPERTY;
                java.util.List al = (java.util.List) hm.get(prop);
                if (al != null) {
                    propVal = al.get(0).toString();
                }
            }
            if (propVal != null) {
                %><span class="docline_prop"><%=propVal%></span> <%
            }
        }
    }

    //
    // KWS DU DOC
    //
    %><jsp:include page="/jsp/kwsofdoc.jsp"/><%
%></li><!--/docline.jsp-->