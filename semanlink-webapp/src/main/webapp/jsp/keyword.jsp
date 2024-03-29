<!--keyword.jsp--><%/** * Affichage d'un keyword. * Si l'attribut de session "net.semanlink.servlet.edit" est le Boolean true, montre de quoi le modifier. */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%
Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");SLKeyword kw = (SLKeyword) jsp.getSLResource();boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));request.setAttribute("displayedKW", kw);Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");boolean imagesOnly = false;if (imagesOnlyB != null) {	imagesOnly = imagesOnlyB.booleanValue();}//2020-02// request.setAttribute("displayedKW", kw.getURI());// 2019-09 snipString context = null;String snip = request.getParameter("snip");if (snip != null) {    context = SLServlet.getServletUrl();    } else {	context = request.getContextPath();}
%><% /////////////////////////////////////////// PARENTS %><%
if (jsp.isDisplayParents()) {	jsp.prepareParentsList();	//2020-05 dragndrop2%> 			<%if (edit) {%>    <div id="tag_parents" class="graybox"         ondrop="dropToTagList(event)"        ondragover="dragOver(event)"        ondragenter="dragEnter(event)"         ondragleave="dragLeave(event)"         ondragstart="dragStart(event)"         ondragend="dragEnd(event)">				<div class="what"><%=jsp.i18l("tag.parents")%></div>				<div class="horizEnumeration">				    <jsp:include page="/jsp/kwlistedit.jsp" flush="true" />				</div>			<%} else {				request.setAttribute("net.semanlink.servlet.rdfa.property",SLVocab.HAS_PARENT_PROPERTY); // 2013-08 RDFa			%>	    <div id="tag_parents" class="graybox">				<div class="horizEnumeration">				    <div class="horizEnumerationTitle"><%=jsp.i18l("tag.parents")%></div>
				    <jsp:include page="/jsp/kwlist.jsp" flush="true" />				</div>			<%} // if edit or not%>		<div class="clearboth"></div>	</div>	<%}// if isDisplayParents %>

<%
if (!(jsp.isDisplaySnipOnly())) {
	/////////////////////////////////////////// TITLE	if (!edit) {
		String homePage = jsp.getHomePage();
		String getDescribedByPage = jsp.getFirstAsString(SLVocab.SL_DESCRIBED_BY_PROPERTY);
		boolean someStuffOnTheRight = ((homePage != null) || (getDescribedByPage != null));
		%>		<div class="kwtitleattop" about="<%=jsp.getUri()%>">
			<%if (someStuffOnTheRight) { %>
				 <span style="float:right">
				 	<%if (homePage != null) { %>
				 		<a href="<%=homePage%>"><%=jsp.i18l("tag.homePage")%></a>
				 	<%} %>
				 	<%if (getDescribedByPage != null) { 				 	String linkLabel = null;				 	if (getDescribedByPage.indexOf("wikipedia.org") > -1) {				 		linkLabel = "Wikipedia";				 	} else {				 		linkLabel = jsp.i18l("tag.getDescribedByPage");				 	}				 					 	%>
				 		&nbsp;<a href="<%=getDescribedByPage%>"><%=linkLabel%></a>
				 	<%} %>
				 </span>
			<%} // @find RDFa bergie%>			<span property="<%=SLVocab.PREF_LABEL_PROPERTY%>"><%=jsp.getTitle()%></span>			<!--  <p property="skos:prefLabel" contenteditable="true"><%=jsp.getTitle()%></p>  -->		</div> <!-- class="title" -->		<%
	} else { // edit %>	   <jsp:include page="keywordLabelEdit.jsp"/>	<%} // edit or not %>			
	
	<% /////////////////// COMMENT		 %>							 
	<jsp:include page="comment.jsp"/>
	<% /////////////////// COMMENT       %>	<%if (false) { %>                        	    <jsp:include page="markdownof.jsp"/>	<%}%>
<%}//  if (!(jsp.isDisplaySnipOnly())) %><%	Bean_KwList truc = new Bean_KwList();	truc.setUri(kw.getURI());	request.setAttribute("net.semanlink.servlet.Bean_KwList", truc);		/*    <div id="tag_friends" class="graybox" ondrop="dropToTagList(event)" ondragover="dragOver(event)"        ondragenter="dragEnter(event)" ondragleave="dragLeave(event)" ondragstart="dragStart(event)" ondragend="dragEnd(event)">*/
%>



<%if (!(jsp.isDisplaySnipOnly())) {
	////// FRIENDS
	truc.setList(kw.getFriends());
	truc.setContainerAttr(null);
	truc.setUlCssClass(null);
	if (edit) {%>
		<% /////////////////////////////////////////// EDIT FRIENDS %>
		<%
			truc.setField("friends");
		%>
    <div id="tag_friends" class="graybox" ondrop="dropToTagList(event)" ondragover="dragOver(event)"        ondragenter="dragEnter(event)" ondragleave="dragLeave(event)" ondragstart="dragStart(event)" ondragend="dragEnd(event)">		<div class="what"><%=jsp.i18l("tag.friends")%></div>
		<div class="horizEnumeration">
			<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />
		</div>
		<div class="clearboth"></div>
		<%
	} else {
		request.setAttribute("net.semanlink.servlet.rdfa.property",SLVocab.HAS_FRIEND_PROPERTY); // 2013-08 RDFa		%>
    <div id="tag_friends" class="graybox">		<div class="horizEnumeration">
			<div class="horizEnumerationTitle"><%=jsp.i18l("tag.friends")%></div>
			<jsp:include page="/jsp/kwlist.jsp" flush="true" />
		</div>
		<div class="clearboth"></div>
		<%
	}
	
%></div>
<%} %>



<% /////////////////////////////// CHILDREN ////////////////////////// %>


<%
//2020-05 dragndrop2	boolean displayChildren = true; // to know whether they must be displayed later (set to false if tree displayed or...)		if (edit) {%>    <div id="tag_children" class="graybox" ondrop="dropToTagList(event)" ondragover="dragOver(event)"        ondragenter="dragEnter(event)" ondragleave="dragLeave(event)" ondragstart="dragStart(event)" ondragend="dragEnd(event)">		<% /////////////////////////////////////////// EDIT CHILDREEN %>		<%
			truc.setList(kw.getChildren());			truc.setContainerAttr(null);			truc.setUlCssClass(null);
			truc.setField("children");		%>		<div class="what"><%=jsp.i18l("tag.children")%></div>		<div class="horizEnumeration">			<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />		</div>		<div class="clearboth"></div>		<%		displayChildren = false;

	} else {	   %>	   <div id="tag_children" class="graybox">	   <%		// NOT EDIT
		// tree, ou bien enfants (sauf ci ceux-ci �dit�s, car d�j� affich�s ci-dessus)
		DisplayMode displayMode = jsp.getDisplayMode();												// 2013-04 change display of children list		String changeChildrenDisplay = context+jsp.computelinkToThis();		if (changeChildrenDisplay.indexOf("?") > -1) changeChildrenDisplay += "&childrenAs=";		else changeChildrenDisplay += "?childrenAs=";		String displayChildrenAs = null;									if (displayMode.isChildrenAsTree()) {			changeChildrenDisplay += DisplayMode.DESCENDANTS_EXPANDED_TREE; // 2013-04			displayChildrenAs = "Expand"; // jsp.i18l("docList.firstLevelDocs");			if (!imagesOnly) { // on ne met pas l'arbre si on affiche les images				request.setAttribute("kw", kw);				request.setAttribute("divid", "kwtree");				request.setAttribute("withdocs", Boolean.TRUE);
				request.setAttribute("withdocs_notonfirstlevel", Boolean.TRUE);				%>
				<%if (!(jsp.isDisplaySnipOnly()))  {
				%><div class="what"><a href="<%=changeChildrenDisplay%>" rel="nofollow"><img src="<%=context%>/ims/box_closed.gif" alt="<%=displayChildrenAs%>"/></a><%=jsp.i18l("tag.descendants")%></div><%} %>						<ul class="livetree">					<jsp:include page="livetreesons.jsp"/>				</ul>								<div class="clearboth"></div> <%//ceci est n�cessaire pour que la "graybox" contienne enti�rement l'�ventuelle image%>				<%				displayChildren = false;			}					} else if (displayMode.isChildrenAsExpandedTree()) {			changeChildrenDisplay += DisplayMode.DESCENDANTS_TREE; // 2013-04			displayChildrenAs = "Close"; // jsp.i18l("docList.firstLevelDocs");			if (!imagesOnly) { // on ne met pas l'arbre expanded si on affiche les images				%>				<%if (!(jsp.isDisplaySnipOnly())) {%><div class="what"><a href="<%=changeChildrenDisplay%>" rel="nofollow"><img src="<%=context%>/ims/box_open.gif" alt="<%=displayChildrenAs%>"/></a><%=jsp.i18l("tag.descendants")%></div><%} %>
				<% /////////////////////////////////////////// IMAGE EVENTUELLE %>				<jsp:include page="expandedtree.jsp"/>				<div class="clearboth"></div> <%//ceci est n�cessaire pour que la "graybox" contienne enti�rement l'�ventuelle image%>				<%				displayChildren = false;			} // if !imageonly		} // if showTree				{ // pas d'arbre : les enfants (sauf ci ceux-ci �dit�s, car d�j� affich�s + haut)			if (displayChildren) {				/////////////////////////////////////////// ENFANTS				truc.setList(kw.getChildren());				truc.setContainerAttr(null);				truc.setUlCssClass(null);
				request.setAttribute("net.semanlink.servlet.rdfa.property","skos:narrower"); // 2013-08 RDFa				%>				<div class="horizEnumeration">					<div class="horizEnumerationTitle"><%=jsp.i18l("tag.children")%></div>
					<jsp:include page="/jsp/kwlist.jsp" flush="true" />				</div>				<div class="clearboth"></div>				<%			} // if displayChildren		} 
	} // edit or not	%></div><%//graybox%>





<% /////////////////////////////// DOCUMENTS ////////////////////////// %>





	<%	/////////////////////////////////////////// DOCUMENTS AND/OR THUMBNAILS	if ((!edit) && (imagesOnly)) {		%>			<div class="graybox">			<div class="what">Images</div>				<jsp:include page="imagelist.jsp"/>			</div>		<%	// } else if (Jsp_Page.SHOW_TREE.equals(mode)) {	} else if (!edit) { // don't display list of docs if edit		// DISPLAY LIST OF DOCS		jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste		request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());				// 2013-03 change display of docs		boolean displayingLongListOfDocs = jsp.getDisplayMode().isLongListOfDocs();
		String changeDocListDisplay = context+jsp.computelinkToThis();		if (changeDocListDisplay.indexOf("?") > -1) changeDocListDisplay += "&longListOfDocs=";		else changeDocListDisplay += "?longListOfDocs=";		changeDocListDisplay += Boolean.toString(!displayingLongListOfDocs);
		String displayLongListOfDocs = null;		if (displayingLongListOfDocs) displayLongListOfDocs = jsp.i18l("docList.firstLevelDocs");		else displayLongListOfDocs = jsp.i18l("docList.allDocs");		%>        <div class= "xdocs"><div class="graybox">			<div class="what"><%=jsp.nbDocsMessage()%> <span style="float:right;text-size:small">(<a href="<%=changeDocListDisplay%>" rel="nofollow"><%=displayLongListOfDocs%></a>)&nbsp;</span></div>			<jsp:include page="doclist.jsp"/> 		</div></div>		<%	}%>



<%if (!(jsp.isDisplaySnipOnly())) { 
			if (edit) { %>												 				<jsp:include page="rdfTypesForm.jsp"/>
			<%} %>
			<jsp:include page="aliases.jsp"/>
			<jsp:include page="properties.jsp"/>
			<%if (false) { // if (SLServlet.isProto()) { %>
				<jsp:include page="tagOutsideLinks.jsp"/>
			<%}%><%}//  if (!(jsp.isDisplaySnipOnly())) %><!--/keyword.jsp-->