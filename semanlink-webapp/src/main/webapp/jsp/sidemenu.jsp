<!--sidemenu.jsp--><%/** * Menu */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%SLKeyword[] clipboardKws = (SLKeyword[]) session.getAttribute("net.semanlink.servlet.ClipboardKeyword");Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");Jsp_Resource jsp_r = null;if (jsp instanceof Jsp_Resource) {    jsp_r = (Jsp_Resource) jsp;}String linkPage = null;Jsp_Document jsp_doc = null;if (jsp instanceof Jsp_Document) jsp_doc = (Jsp_Document) jsp;String uri = null;if ((jsp_doc != null) || (jsp instanceof Jsp_Keyword)) uri = jsp_r.getUriFormValue();boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));String contextPath = request.getContextPath();String imagFolder = contextPath +  "/ims/";// this needs livesearch.js// and <BODY onload="liveSearchInit()>// and... (see doc)%><div id="sidemenucontainer">    <%    if (jsp != null) {    %>    <div class="sidegraybox">        <div style="margin-top:4px;margin-bottom:2px;text-align:center">         <%        // pas ok pour rss sur welcome if ((jsp instanceof Jsp_Keyword) || (jsp instanceof Jsp_ThisMonth) || (jsp instanceof Jsp_Welcome)) {        if ((jsp instanceof Jsp_Keyword) || (jsp instanceof Jsp_ThisMonth)) {            %><a href="<%=contextPath +"/" + jsp.rssFeedUriRelativToSL()%>"><img style="border:0px" alt="RSS" src="<%=contextPath%>/ims/rss.gif" /></a>&nbsp;<%            %> <a type="application/rdf+xml" href="<%=jsp.linkToRDF("rdf")%>"><img style="border:0px" alt="RDF" src="<%=contextPath%>/ims/rdf.gif" /></a>&nbsp;<%            %> <a type="text/turtle" href="<%=jsp.linkToRDF("n3")%>"><img style="border:0px" alt="n3" src="<%=contextPath%>/ims/n3.gif" height=20 width=20 /></a>&nbsp;<%            if (false) { // javascript tabulator // @find rdfparser            %> <a href="<%=jsp.linkToRdfJs()%>"><img style="border:0px" alt="RDF" src="<%=contextPath%>/ims/rdf.gif" /></a>&nbsp;<%            }        }        %>        <a href="" onclick="printVersion();return false;"><img style="border:0px" alt="<%=jsp.i18l("sidemenu.print")%>" src="<%=contextPath%>/ims/printer.gif" /></a>        <%        if (jsp instanceof Jsp_Welcome) {                linkPage = jsp.getLinkToThisWithParams("lang=");                String sss = linkPage + "en";                %><html:link page="<%=sss%>"><img style="border:0px" alt="en" src="<%=contextPath%>/ims/flags/en.gif" /></html:link>                <%sss = linkPage +"el";                %><html:link page="<%=sss%>"><img style="border:0px" alt="el" src="<%=contextPath%>/ims/flags/el.gif" /></html:link>                <%                // linkPage = jsp.getLinkToThisWithParams("lang=fr");                sss = linkPage + "fr";                %><html:link page="<%=sss%>"><img style="border:0px" alt="fr" src="<%=contextPath%>/ims/flags/fr.gif" /></html:link>                <%        } // if (jsp instanceof Jsp_Welcome) %>        </div>    </div>    <%}%>                    <jsp:include page="/jsp/livesearchform.jsp" flush="true" />    <div class="separ">&nbsp;</div>    <jsp:include page="/jsp/sidemenu_godoc.jsp" flush="true" />    <div class="separ">&nbsp;</div>        <%        if (jsp != null) {        if (jsp.showBtnEdit()) { // afficher le menu "edit"            String editLinkPage = jsp.getEditLinkPage();            if (editLinkPage != null) { // sinon pb avec page new doc                String display="none";                String imag = null;                if (edit) {                    display = "block";                    imag = imagFolder + "box_open.gif";                } else {                    imag = imagFolder + "box_closed.gif";                }                // BOF TODO : reprendre les linkToThis                if (jsp.isEditor()) {                    linkPage = jsp.completePath();                } else {                    linkPage = jsp.logonPage();                }                                if (linkPage != null) { // afficher le menu edit                    linkPage = response.encodeURL(linkPage);                            //                    // EDIT                     //                    %>                    <div class="browser">                        <form id="editform" method="post" action="<%=linkPage%>">                            <div class="title trigger" onclick="document.getElementById('editform').submit()">                                <span style="float:right"><img src="<%=imag%>" id="trigger:editing" alt="" /></span><span><%=jsp.i18l("sidemenu.edit")%></span>                                <input type="hidden" name="edit" value="<%=(new Boolean(!edit)).toString()%>" />                            </div>                        </form>                        <div id="block:editing" class="box" style="display:<%=display%>"> <!-- on peut mettre scrollable ou box -->                            <%if (edit) {%>                                <ul class="sidelink">                                    <li><html:link page="/bookmarkform.do"><%=jsp.i18l("sidemenu.newBookmark")%></html:link></li>                                    <li><html:link page="/newnoteform.do"><%=jsp.i18l("sidemenu.newNote")%></html:link></li>                                    <li><html:link page="<%=jsp.activFolderLinkPage()%>"><%=jsp.i18l("sidemenu.upload")%></html:link></li><%//2020-09%>                                    <%                                    boolean thereIsASecondList = false;                                    if (jsp instanceof Jsp_Keyword) {                                         if (!thereIsASecondList) {                                            thereIsASecondList = true;                                            %></ul><ul class="sidelink"><%                                        }                                        linkPage = jsp.getLinkToThis("/copykwget.do");                                                                                                                %><li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.copy")%></html:link></li><%                                                     if (clipboardKws != null) {                                            String debLinkPage = jsp.getLinkToThis("/pastekwget.do");                                            linkPage = debLinkPage + "&as=parent";                                            %><li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.pasteAsParent")%></html:link></li><%                                                            linkPage = debLinkPage + "&as=friend";                                            %><li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.pasteAsFriend")%></html:link></li><%                                                            linkPage = debLinkPage + "&as=child";                                            %><li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.pasteAsChild")%></html:link></li><%                                                     } // if (clipboardKw != null)                                    } else if (jsp_doc != null) {                                        if (clipboardKws != null) {                                            if (!thereIsASecondList) {                                                thereIsASecondList = true;                                                %></ul><ul class="sidelink"><%                                            }                                            String debLinkPage = jsp.getLinkToThis("/pastekwget.do");                                            linkPage = debLinkPage + "&as=keyword";                                            %>                                            <li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.paste")%></html:link></li>                                            <%                                                      } // if (clipboardKw != null)                                                   } // if (jsp instanceof ...)                                                if (jsp instanceof Jsp_Keyword) {                                        if (!thereIsASecondList) {                                            thereIsASecondList = true;                                            %></ul><ul class="sidelink"><%                                        }                                        linkPage = jsp.getLinkToThis("/deletekw.do");                                                                                String confirmString = "'" + jsp.i18l("sidemenu.confirmDeleteTag") + "'";                                        String onClickStringToConfirm = "return window.confirm(" + confirmString +")";                                        %>                                        <li><html:link page="<%=linkPage%>" onclick="<%=onClickStringToConfirm%>"><%=jsp.i18l("sidemenu.deleteTag")%></html:link></li>                                    <%                                    } else if (jsp_doc != null) {                                        if (!thereIsASecondList) {                                            thereIsASecondList = true;                                            %></ul><ul class="sidelink"><%                                        }                                                                            if (!jsp_doc.isFile()) {                                            linkPage = jsp.getLinkToThis("/download.do");                                                           %>                                            <li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.downloadCopy")%></html:link></li>                                            <%                                        }                                                                                                                    linkPage = jsp.getLinkToThis("/docanalysis.do");                                                        %>                                        <li><html:link page="<%=linkPage%>"><%=jsp.i18l("sidemenu.extractMetadata")%></html:link></li>                                        <%                                        linkPage = jsp.getLinkToThis("/removedoc.do");                                                      String confirmString = "'" + jsp.i18l("sidemenu.confirmRemoveDoc") + "'";                                        String onClickStringToConfirm = "return window.confirm(" + confirmString +")";                                        %>                                        <li><html:link page="<%=linkPage%>" onclick="<%=onClickStringToConfirm%>"><%=jsp.i18l("sidemenu.removeDoc")%></html:link></li>                                        <%                                    }                                    %>                                </ul>                            <%} // if edit %>                        </div>                    </div><!-- browser -->            <%        } // if linkPage != null cad if afficher le menu edit    } // id editlinkpage != null%>        <%} // isEditor() %>                 <% } // jsp != null %><%//// PREFERENCES PRESENTATION//%>        <div class="separ">&nbsp;</div>        <div class="browser">            <%                                    String display="none";            String imag = imagFolder + "box_closed.gif";            %>            <div class="title trigger" onclick="toggle('setprefs');">                <span style="float:right"><img src="<%=imag%>" id="trigger:setprefs" alt="" /></span><span><%=jsp.i18l("sidemenu.preferences")%></span>                <input type="hidden" name="edit" value="<%=(new Boolean(!edit)).toString()%>" />            </div>            <div id="block:setprefs" class="box" style="display:<%=display%>"> <!-- on peut mettre scrollable ou box -->                <jsp:include page="setprefs4doclist.jsp"/>            </div>        </div>        <div class="separ">&nbsp;</div><%/////////////////////////////////////////// LINKED KEYWORDS TAGCLOUD// if ((jsp instanceof Jsp_Keyword)//  || (jsp instanceof Jsp_Document)) {    SLKeywordNb[] linkedKws = jsp.getLinkedKeywordsWithNb();    if (linkedKws != null) {        // TODO CHANGE                      Bean_KwList_Nb truc = new Bean_KwList_Nb();        request.setAttribute("net.semanlink.servlet.Bean_KwList", truc);        truc.setUri(uri);        truc.setList(java.util.Arrays.asList(linkedKws));        truc.setTitle(jsp.i18l("sidemenu.tagCloud"));        %>            <div>                <jsp:include page="/jsp/kwblock_nb2.jsp" flush="true" />            </div>                                                           <%        request.removeAttribute("net.semanlink.servlet.Bean_KwList");    }%>                    </div> <% // sidemenucontainer %><div class="clearboth"></div><!--/sidemenu.jsp-->