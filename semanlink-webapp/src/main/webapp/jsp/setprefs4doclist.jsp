<!--setprefs4doclist.jsp-->
		String action = jsp.getLinkToThis();
		*/
		
		String action = request.getContextPath() + "/setprefs.do";
		// FAUT-il : ???
		// action = response.encodeURL(action);
		
		
		

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
			DisplayMode displayMode = jsp.getDisplayMode();


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

				<select name="longListOfDocs">
						checkedOrNot = " " + checked;
						checkedOrNot = " " + checked;
					<%if (false) { %>
					
			
			
			
			
			