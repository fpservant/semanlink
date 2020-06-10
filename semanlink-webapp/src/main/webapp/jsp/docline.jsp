<%/** * Affichage d'une ligne d'une liste de documents au sein d'une Jsp_Resource : doc d'un kw, fichier d'un dossier,... * appel�e par doclist.jsp (donc document.jsp, keyword.jsp, andKws, ...) et class WalkListener * @param request.getAttribute("net.semanlink.servlet.jsp.currentdoc") SLDocument en question * @param request.getAttribute("net.semanlink.servlet.jsp.currentdoc.kws") la liste de SLKeywords � montrer, ou null s'il ne faut pas en montrer */%><!--docline.jsp--><%@ page language="java" session="true"	import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.util.*"%><%@ taglib	uri="http://struts.apache.org/tags-bean" prefix="bean"%><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%><%Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");SLDocument doc = (SLDocument) request.getAttribute("net.semanlink.servlet.jsp.currentdoc");SLModel mod = SLServlet.getSLModel();SLDocumentStuff docStuff = new SLDocumentStuff(doc, mod, jsp.getContextURL()); // 2019-04// HTML_Link link = HTML_Link.linkToDocument(doc);// String linkToThisPage = jsp.getLinkToThis(); // ceci est mis en cache par jsp -- pas grave de l'appeler ds chaque docline.jsp de la listeString uri = doc.getURI(); // ds le cas d'un doc servi par le web server, c bien l'url servi par le ws// 2020-01 #DocList up until now, we used ul list.// 2013-08 RDFa// 2020-06boolean isImage = Util.isImage(uri);String displayedImgId1 = null;String displayedImgId2 = null;String displayedImgId3 = null;String context = null;String imageLinkPage = null;if (isImage) {	Random r = new Random();	String shortId = Integer.toString(r.nextInt());	displayedImgId1 = "displayedImgId1_" + shortId;	displayedImgId2 = "displayedImgId2" + shortId;	displayedImgId3 = "displayedImgId3" + shortId;	  //	  // IMAGE	  //	  	  // pour le script du btn "image"	  SLDocument imageToBeDisplayed = mod.getDocument(docStuff.getHref()); // 2019-03: Hum c'est pas doc? TODO check	 	  Jsp_Document imageJsp_doc = Manager_Document.getDocumentFactory().newJsp_Document(imageToBeDisplayed, request);	  imageLinkPage = imageJsp_doc.getLinkToThis();	  	  String snip = request.getParameter("snip");	  if (snip != null) {	      context = SLServlet.getServletUrl();    	  } else {	      context = request.getContextPath();	  }}%><div class="docline_indiv" about="<%=uri%>">    <%if (isImage) { %>        <div>        <div class="docimageright" style="display:none" id="<%=displayedImgId1%>"><a id="<%=displayedImgId2%>" href=""><img id="<%=displayedImgId3%>" src="<%=context%>/ims/scrogneugneu.gif" alt="" width="200" /></a></div>    <%}%>	<div class="docline_title">		<% // ajout� pour tree - �tait avant au niveau LI        if (isImage) {             %>		<%// span pour ne pas avoir le d�calage vers gauche mis pour les triangles des kws cf css .graybox ul li img %>		<span style="margin-left: 8px"> <img			src="<%=context%>/ims/image.gif" class="imageBtn" alt=""			onclick="loadImage('<%=docStuff.getHref()%>', '<%=context+imageLinkPage%>' , '<%=displayedImgId1%>' , '<%=displayedImgId2%>', '<%=displayedImgId3%>')" />		</span>		<%        }  // if (Util.isImage(uri))                //        // LABEL        //                String docLabel = null;        if (jsp != null) {            docLabel = jsp.getLabel(doc);        }        if (docLabel != null) docLabel = Util.toHTMLOutput(docLabel);                //        // LINK TO DOC        //                // 2017-09 similar stuff in comment.jsp @find doc2markdownHref        // String mdHref = mod.doc2markdownHref(jsp.getContextUrl(), uri);        // open in desktop? not if it's a dir        boolean doOpenInDesktop = false;        if (docStuff.getFile() != null) {            if (!docStuff.isDir()) {                doOpenInDesktop = true;            }        }        if (!doOpenInDesktop) {            String href = response.encodeURL(Util.handleAmpersandInHREF(docStuff.getHref()));            %><a href="<%=href%>"><span property="rdfs:label"><%=docLabel%></span></a>		<%        } else { // doOpenInDesktop                      SLDocumentStuff.HrefPossiblyOpeningInDestop hr = docStuff.getHrefPossiblyOpeningInDestop(true);          if (hr.openingInDesktop()) {           %><a href="<%=hr.href()%>"			onclick="desktop_open_hack('<%=hr.href()%>'); return false;"><%=docLabel%></a>		<%                           } else {           String href = response.encodeURL(hr.href());           %><a href="<%=href%>"><%=docLabel%></a>		<%                      }        }        %> 	<%              %></div><% // line 1 %>        <%    //    // KWS DU DOC    //      %>    <div class="horizEnumeration">        <!-- div class="horizEnumerationTitle">Tags:</div>  -->        <jsp:include page="/jsp/kwsofdoc.jsp" />    </div>    <%    //    // COMMENT    //        String comment = doc.getComment();    if (comment != null) {        comment = Util.toHTMLOutput(comment);        %><div class="docline_comment" property="rdfs:comment">           <textarea><%=comment %></textarea>        </div><% // 2013-08 RDFa          }    %>    <%if (isImage) { %>        </div>        <div class="clearboth"></div> <%//ceci est n�cessaire pour que la "graybox" contienne enti�rement l'�ventuelle image%>            <%}%>            <%    //    // SORT PROP, ABOUT, etc.    //        %><div><% // line 2        //    // SORT PROPERTY    //    String sortpropVal = jsp.sortPropValue4Display(doc);    if (sortpropVal != null) {        %><span style="float:right"><%=sortpropVal%></span><%    }        %><span><%        //    // LINK TO DOC PAGE ("about")    //        String href;    href = response.encodeURL(docStuff.getAboutHref());    %> <a href="<%=href%>"><%=jsp.i18l("doc.about")%></a><%         //    // LINK TO LOCAL COPY AND/OR TO SOURCE    //        SLDocument localCopy = docStuff.getLocalCopy();    if (localCopy != null) {       SLDocumentStuff.HrefPossiblyOpeningInDestop localCopyLink = docStuff.getLocalCopyLink();       if (localCopyLink.openingInDesktop()) {        %> - <a href="<%=localCopyLink.href()%>"        onclick="desktop_open_hack('<%=localCopyLink.href()%>'); return false;"><%=jsp.i18l("doc.localCopy")%></a><%                               } else {        href = response.encodeURL(localCopyLink.href());        %> - <a href="<%=href%>"><%=jsp.i18l("doc.localCopy")%></a><%                   }           } else {        SLDocument source = docStuff.getSource();        if (source != null) {            %> - <a href="<%=source.getURI()%>"><%=jsp.i18l("doc.source")%></a><%        }    }                %></span></div><% // DIV_ABOUT%></div><!--/docline.jsp-->