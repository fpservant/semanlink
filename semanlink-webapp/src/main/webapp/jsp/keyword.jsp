<!--keyword.jsp--><%/** * Affichage d'un keyword. * Si l'attribut de session "net.semanlink.servlet.edit" est le Boolean true, montre de quoi le modifier. */%><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%
boolean new_showdocs = true;
boolean new_showTreeNotExpanded = true;
boolean new_showTreeExpanded = false;
boolean new_showTree = new_showTreeNotExpanded || new_showTreeExpanded;
// SLKeyword kw = (SLKeyword) request.getAttribute("net.semanlink.servlet.SLKeyword");Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");SLKeyword kw = (SLKeyword) jsp.getSLResource();boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));Boolean imagesOnlyB = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");boolean imagesOnly = false;if (imagesOnlyB != null) {	imagesOnly = imagesOnlyB.booleanValue();}
%><% /////////////////////////////////////////// PARENTS %><%
if (jsp.isDisplayParents()) {	jsp.prepareParentsList();	//2017-01 DRAG TEST <div class="what" ondrop="drop(event)" ondragover="allowDrop(event)" style="background-color:pink">	%>	<div class="graybox">			<%if (edit) {%>				<div class="what"><%=jsp.i18l("tag.parents")%></div>				<div class="horizEnumeration">				<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />				</div>			<%} else {				request.setAttribute("net.semanlink.servlet.rdfa.property",SLVocab.HAS_PARENT_PROPERTY); // 2013-08 RDFa				%>				<div class="horizEnumeration">				<div class="horizEnumerationTitle"><%=jsp.i18l("tag.parents")%></div>
				<jsp:include page="/jsp/kwlist.jsp" flush="true" />				</div>			<%} // if edit or not%>		<div class="clearboth"></div>	</div>
<%}// if isDisplayParents %>

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
	} else { // edit %>		<div class="graybox">				<div class="what"><%=jsp.i18l("tag.label")%></div>				<html:form action="setkwlabel">					<html:text property="kwlabel" value="<%=kw.toString()%>" size="60"/>					<html:hidden property="kwuri" value="<%=kw.getURI()%>" />					<html:select property="lang">						<html:option value="-">-</html:option>						<html:option value="de">de</html:option>
						<html:option value="en">en</html:option>						<html:option value="es">es</html:option>						<html:option value="fr">fr</html:option>
						<html:option value="pt">pt</html:option>					</html:select>					<html:submit property="okBtn">OK</html:submit>				</html:form>		</div> <!-- class="graybox" -->	<%} // edit or not %>			
	
	<% /////////////////// COMMENT		 %>							 
	<jsp:include page="comment.jsp"/>

<%}//  if (!(jsp.isDisplaySnipOnly())) %><%	Bean_KwList truc = new Bean_KwList();	truc.setUri(kw.getURI());	request.setAttribute("net.semanlink.servlet.Bean_KwList", truc);
%>



<%if (!(jsp.isDisplaySnipOnly())) {%>
<div class="graybox"><%
	////// FRIENDS
	truc.setList(kw.getFriends());
	truc.setContainerAttr(null);
	truc.setUlCssClass(null);
	if (edit) {%>
		<% /////////////////////////////////////////// EDIT FRIENDS %>
		<%
			truc.setField("friends");
		%>
		<div class="what"><%=jsp.i18l("tag.friends")%></div>
		<div class="horizEnumeration">
			<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />
		</div>
		<div class="clearboth"></div>
		<%
	} else {
		request.setAttribute("net.semanlink.servlet.rdfa.property",SLVocab.HAS_FRIEND_PROPERTY); // 2013-08 RDFa		%>
		<div class="horizEnumeration">
			<div class="horizEnumerationTitle"><%=jsp.i18l("tag.friends")%></div>
			<jsp:include page="/jsp/kwlist.jsp" flush="true" />
		</div>
		<div class="clearboth"></div>
		<%
	}
	
%></div>
<%} %>



<% /////////////////////////////// CHILDREN ////////////////////////// %>




<div class="graybox">
<%
	boolean displayChildren = true; // to know whether they must be displayed later (set to false if tree displayed or...)		if (edit) {%>		<% /////////////////////////////////////////// EDIT CHILDREEN %>		<%
			truc.setList(kw.getChildren());			truc.setContainerAttr(null);			truc.setUlCssClass(null);
			truc.setField("children");		%>		<div class="what"><%=jsp.i18l("tag.children")%></div>		<div class="horizEnumeration">			<jsp:include page="/jsp/kwlistedit.jsp" flush="true" />		</div>		<div class="clearboth"></div>		<%		displayChildren = false;

	} else {
		// NOT EDIT
		// tree, ou bien enfants (sauf ci ceux-ci édités, car déjà affichés ci-dessus)
		DisplayMode displayMode = jsp.getDisplayMode();												// 2013-04 change display of children list		String changeChildrenDisplay = request.getContextPath()+jsp.computelinkToThis();		if (changeChildrenDisplay.indexOf("?") > -1) changeChildrenDisplay += "&childrenAs=";		else changeChildrenDisplay += "?childrenAs=";		String displayChildrenAs = null;									if (displayMode.isChildrenAsTree()) {			changeChildrenDisplay += DisplayMode.DESCENDANTS_EXPANDED_TREE; // 2013-04			displayChildrenAs = "Expand"; // jsp.i18l("docList.firstLevelDocs");			if (!imagesOnly) { // on ne met pas l'arbre si on affiche les images				request.setAttribute("kw", kw);				request.setAttribute("divid", "kwtree");				request.setAttribute("withdocs", Boolean.TRUE);
				request.setAttribute("withdocs_notonfirstlevel", Boolean.TRUE);				%>
				<%if (!(jsp.isDisplaySnipOnly()))  {
				%><div class="what"><%=jsp.i18l("tag.descendants")%><span style="float:right;text-size:small"><a href="<%=changeChildrenDisplay%>" rel="nofollow"><img src="<%=request.getContextPath()%>/ims/box_closed.gif" alt="<%=displayChildrenAs%>"/></a></span></div><%} %>						<ul class="livetree">					<jsp:include page="livetreesons.jsp"/>				</ul>								<div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%>				<%				displayChildren = false;			}		} else if (displayMode.isChildrenAsExpandedTree()) {			changeChildrenDisplay += DisplayMode.DESCENDANTS_TREE; // 2013-04			displayChildrenAs = "Close"; // jsp.i18l("docList.firstLevelDocs");			if (!imagesOnly) { // on ne met pas l'arbre expanded si on affiche les images				%>				<%if (!(jsp.isDisplaySnipOnly())) {%><div class="what"><%=jsp.i18l("tag.descendants")%><span style="float:right;text-size:small"><a href="<%=changeChildrenDisplay%>" rel="nofollow"><img src="<%=request.getContextPath()%>/ims/box_open.gif" alt="<%=displayChildrenAs%>"/></a></span></div><%} %>
				<% /////////////////////////////////////////// IMAGE EVENTUELLE %>				<jsp:include page="expandedtree.jsp"/>				<div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%>				<%				displayChildren = false;			} // if !imageonly		} // if showTree				{ // pas d'arbre : les enfants (sauf ci ceux-ci édités, car déjà affichés + haut)			if (displayChildren) {				/////////////////////////////////////////// ENFANTS				truc.setList(kw.getChildren());				truc.setContainerAttr(null);				truc.setUlCssClass(null);
				request.setAttribute("net.semanlink.servlet.rdfa.property","skos:narrower"); // 2013-08 RDFa				%>				<div class="horizEnumeration">					<div class="horizEnumerationTitle"><%=jsp.i18l("tag.children")%></div>
					<jsp:include page="/jsp/kwlist.jsp" flush="true" />				</div>				<div class="clearboth"></div>				<%			} // if displayChildren		} 
	} // edit or not	%></div><%//graybox%>





<% /////////////////////////////// DOCUMENTS ////////////////////////// %>





	<%	/////////////////////////////////////////// DOCUMENTS AND/OR THUMBNAILS	if ((!edit) && (imagesOnly)) {		%>			<div class="graybox">			<div class="what">Images</div>				<jsp:include page="imagelist.jsp"/>			</div>		<%	// } else if (Jsp_Page.SHOW_TREE.equals(mode)) {	} else {		jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste		request.setAttribute("net.semanlink.servlet.Bean_DocList", jsp.getDocList());		int nn = jsp.getDocList().getList().size();		
		java.text.MessageFormat messageFormat = new java.text.MessageFormat(jsp.i18l("x.documents"));
		Object[] args = new Object[1];
		args[0] = Integer.toString(nn);
		String ndocs = messageFormat.format(args);			// 2013-03 change display of docs		boolean displayingLongListOfDocs = jsp.getDisplayMode().isLongListOfDocs();
		String changeDocListDisplay = request.getContextPath()+jsp.computelinkToThis();		if (changeDocListDisplay.indexOf("?") > -1) changeDocListDisplay += "&longListOfDocs=";		else changeDocListDisplay += "?longListOfDocs=";		changeDocListDisplay += Boolean.toString(!displayingLongListOfDocs);
		String displayLongListOfDocs = null;		if (displayingLongListOfDocs) displayLongListOfDocs = jsp.i18l("docList.firstLevelDocs");		else displayLongListOfDocs = jsp.i18l("docList.allDocs");		request.setAttribute("net.semanlink.servlet.Bean_DocList_noUL", Boolean.FALSE);		%>		<div class="graybox">			<div class="what"><%=ndocs%> <span style="float:right;text-size:small">(<a href="<%=changeDocListDisplay%>" rel="nofollow"><%=displayLongListOfDocs%></a>)&nbsp;</span></div>			<% /////////////////////////////////////////// IMAGE EVENTUELLE %>			<jsp:include page="/jsp/image.jsp" flush="true" />			<% /////////////////////////////////////////// DOC LIST %>			<jsp:include page="doclist.jsp"/>			<div class="clearboth"></div> <%//ceci est nécessaire pour que la "graybox" contienne entièrement l'éventuelle image%>		</div>		<%	}%>



<%if (!(jsp.isDisplaySnipOnly())) { 
			if (edit) { %>												 				<jsp:include page="rdfTypesForm.jsp"/>
			<%} %>
			<jsp:include page="aliases.jsp"/>
			<jsp:include page="properties.jsp"/>
			<%if (SLServlet.isProto()) { %>
				<jsp:include page="tagOutsideLinks.jsp"/>
			<%}%><%}//  if (!(jsp.isDisplaySnipOnly())) %>
<!--/keyword.jsp-->