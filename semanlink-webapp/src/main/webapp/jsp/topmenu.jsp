<!--topmenu.jsp--><%/** * Top Menu */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%SLKeyword[] clipboardKws = (SLKeyword[]) session.getAttribute("net.semanlink.servlet.ClipboardKeyword");
SLKeyword clipboardKw = null;
if (clipboardKws != null) clipboardKw = clipboardKws[0];
Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");String linkPage = null;// String uri = null;// if ((jsp_doc != null) || (jsp instanceof Jsp_Keyword)) uri = jsp_r.getUriFormValue();String contextPath = request.getContextPath();String contextUrl = net.semanlink.util.Util.getContextURL(request);
// J'Y COMPRENDS RIEN, (PB CSS)  MAIS S'IL Y A DES BLANCS ENTRE LES </li> ET <li>,// DONNE UN ESPACE ENTRE LES MENU ITEMS// D'OU GAFFE A NE PAS EN METTRE// 2020-01: display rightbar if click on logo%><div id="logo"><a href="" onclick="toggleRightBar();return false;"><img src="<%=contextPath%>/ims/slogo.gif" height="38" width="154" alt="semanlink" /></a><% //=SLServlet.getSemanlinkVersion()%></div><div id="navcontainer">	<ul> 		<%		%><li><html:link page="/sl/home"><%=jsp.i18l("topmenu.home")%></html:link></li><%
		
		if (!SLServlet.isSemanlinkWebSite()) {			%><li><a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/help.htm"><%=jsp.i18l("topmenu.help")%></a></li><%
		}				%><li><html:link page="/sl/new"><%=jsp.i18l("topmenu.newEntries")%></html:link></li><%				SLKeyword favori = jsp.getFavori();		if (favori != null) {			if (false) { // 2013-03			%><li><a href="<%=response.encodeURL(HTML_Link.getTagHref(contextPath, favori.getURI(), false))%>?mode=1"><%=jsp.i18l("topmenu.favorites")%></a></li><%			}			%><li><a href="<%=response.encodeURL(HTML_Link.getTagHref(contextPath, favori.getURI(), false))%>"><%=jsp.i18l("topmenu.favorites")%></a></li><%		}				if (false) {			linkPage = null; // "/" + net.semanlink.sicg.Jsp_RddIndex.getRelativHREF();			%><li><html:link page="<%=linkPage%>">Index des CR SICG</html:link></li><%		}				%><li><html:link page="/sl/sparql"><%=jsp.i18l("topmenu.sparql")%></html:link></li><%		if (clipboardKw != null) {			%><li><a href="<%=response.encodeURL(HTML_Link.getTagHref(contextPath, clipboardKw.getURI(), false))%>"><i><%=clipboardKw.getLabel()%></i></a></li><%		} // if (clipboardKw != null)		/*if (!(				(jsp instanceof net.semanlink.sicg.Jsp_SicgArticle)				|| (jsp instanceof net.semanlink.sicg.Jsp_SicgRdd)				|| (jsp instanceof net.semanlink.sicg.Jsp_RddIndex)			)) {*/			if (jsp.isEditor()) {
				SLModel mod = SLServlet.getSLModel();
				SLDocument currentFolder = mod.smarterGetDocument(mod.filenameToUri(mod.goodDirToSaveAFile().getPath()));	            String currentFolderUri = currentFolder.getURI();	            if (!currentFolderUri.endsWith("/")) currentFolderUri+="/"; // hack pour que le titre pr�sente bien (url compl�te et non short name)                // linkPage = HTML_Link.docLink(currentFolderUri);                SLDocumentStuff docStuff = new SLDocumentStuff(currentFolder, mod, jsp.getContextURL());                String href = docStuff.getHref();                if (!href.endsWith("/")) href+="/"; // hack pour que le titre pr�sente bien (url compl�te et non short name)                // href = response.encodeURL(href);                linkPage = HTML_Link.removeContext(href, jsp.getContextURL());                %><li><html:link page="<%=linkPage%>"><%=jsp.i18l("topmenu.activFolder")%></html:link></li><% 			} // jsp.isEditor() or not		// } // test non cr rdd 		%>	</ul> </div><div class="clearboth"></div> <% // depuis XHTMl + RDFA, ceci ne marche pas : il faut le mettre apr�s la fermeture du div du topmenu (ds template.jsp, donc) %><!--/topmenu.jsp-->