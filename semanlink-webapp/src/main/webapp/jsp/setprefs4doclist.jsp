<!--setprefs4doclist.jsp--><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.net.*, java.util.*, glguerin.io.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>	<%		Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");		/*Jsp_Resource jsp_r = null;		if (jsp instanceof Jsp_Resource) {			jsp_r = (Jsp_Resource) jsp;		}*/		// if (jsp_r != null) uri = jsp_r.getUriFormValue();		/*
		String action = jsp.getLinkToThis();		// if (action.indexOf("/semanlink/") < 0) action = Util.getContextURL(request) + action; // TODO reprendre		// action = response.encodeURL(action);
		*/
		
		String action = request.getContextPath() + "/setprefs.do";
		// FAUT-il : ???
		// action = response.encodeURL(action);
		
		
				String checked = "selected=\"selected\"";	%>	<form method="post" action="<%=action%>"><p><%//il faut le p précedent pour être valid strict%>			<input type="hidden" name="do" value="setprefs" />

			<%
			String checkedOrNot=null;
			String currentLang = I18l.getLang(session);
			String[] availableLanguages = I18l.getAvailableLanguages();
			%>
			
			<label><%=jsp.i18l("x.lang")%></label><br/>
				<select name="lang">
					<%
					for (int i = 0 ; i < availableLanguages.length; i++) {
						String lang = availableLanguages[i];
						if (lang.equals(currentLang)) {
							checkedOrNot = " " + checked;
						} else {
							checkedOrNot = "";
						}
						%>
						<option value="<%=lang%>" <%=checkedOrNot%>><%=lang%></option>
						<%
					}%>
				</select>
			<br/>





			<%
			DisplayMode displayMode = jsp.getDisplayMode();			String text;			%>			<label><%=jsp.i18l("sidemenu.descendants-display")%></label><br/>


				<select name="childrenAs">
					<%
					text = jsp.i18l("sidemenu.childrenAsList");
					if (displayMode.isChildrenAsList()) {
						checkedOrNot = " " + checked;
					} else {
						checkedOrNot = "";
					}
					%>
					<option value="<%=DisplayMode.DESCENDANTS_CHILDREN_AS_LIST%>" <%=checkedOrNot%>><%=text%></option>
					<%
					text = jsp.i18l("sidemenu.treeView");
					if (displayMode.isChildrenAsTree()) {
						checkedOrNot = " " + checked;
					} else {
						checkedOrNot = "";
					}
					%>
					<option value="<%=DisplayMode.DESCENDANTS_TREE%>" <%=checkedOrNot%>><%=text%></option>
					<%
					text = jsp.i18l("sidemenu.expandedTree");
					if (displayMode.isChildrenAsExpandedTree()) {
						checkedOrNot = " " + checked;
					} else {
						checkedOrNot = "";
					}
					%>
					<option value="<%=DisplayMode.DESCENDANTS_EXPANDED_TREE%>" <%=checkedOrNot%>><%=text%></option>
				</select>








			<br/><label><%=jsp.i18l("sidemenu.documents-display")%></label><br/>

				<select name="longListOfDocs">					<%					text = jsp.i18l("sidemenu.firstLevelDocs");					if (!(displayMode.isLongListOfDocs())) {
						checkedOrNot = " " + checked;					} else {						checkedOrNot = "";					}					%>					<option value="false" <%=checkedOrNot%>><%=text%></option>					<%					text = jsp.i18l("sidemenu.allDocs");					if (displayMode.isLongListOfDocs()) {
						checkedOrNot = " " + checked;					} else {						checkedOrNot = "";					}					%>					<option value="true" <%=checkedOrNot%>><%=text%></option>				</select>						<%			Object current = Form_Base.getSelectedSortPropInForm(session);						// TODO CHANGE			String[] propShortNames = {				"sl:creationTime",				"dc:date",				"dc:creator",				"dc:source"			};			// TODO : CHANGER LE PASSAGE DES PROPS : PAS URI, PAR PAR SHORT NAME !!!! ??			%>			<br/><label><%=jsp.i18l("sidemenu.sortby")%></label><br/>				<select name="property">
					<%if (false) { %>					<option value="">* Tags *</option>					<%}
										// for (int i = 0; i < SLVocab.COMMON_PROPERTIES.length; i++) {					for (int i = 0; i < propShortNames.length; i++) {						// SLVocab.EasyProperty prop = SLVocab.COMMON_PROPERTIES[i];						// String propShortName = prop.getName();						// String propUri = prop.getUri(); // on pourrait le passer à la form, mais vu qu'on a aussi le cas court						String propShortName = propShortNames[i];						if (propShortName.equals(current)) {							checkedOrNot = " " + checked;						} else {							checkedOrNot = "";						}											%>						<option value="<%=propShortName%>" <%=checkedOrNot%>><%=propShortName%></option>					<%} // for %>				</select>						<%			Boolean bCurrent = (Boolean) session.getAttribute("net.semanlink.servlet.imagesonly");			%>			<br/><label><%=jsp.i18l("sidemenu.imagesOnly")%></label><br/>				<select name="imagesonly">					<%					if (Boolean.FALSE.equals(bCurrent)) {						checkedOrNot = " " + checked;					} else {						checkedOrNot = "";					}					%>					<option value="false" <%=checkedOrNot%>><%=jsp.i18l("sidemenu.no")%></option>					<%					if (Boolean.TRUE.equals(bCurrent)) {						checkedOrNot = " " + checked;					} else {						checkedOrNot = "";					}					%>					<option value="true" <%=checkedOrNot%>><%=jsp.i18l("sidemenu.yes")%></option>				</select>			<br/>
			
			
			
			
						<input type="submit" name="okBtn" value="<%=jsp.i18l("sidemenu.ok")%>" />	</p></form><!-- /setprefs4doclist.jsp -->