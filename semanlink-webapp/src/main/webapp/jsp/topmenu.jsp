<!--topmenu.jsp-->
SLKeyword clipboardKw = null;
if (clipboardKws != null) clipboardKw = clipboardKws[0];


		
		if (!SLServlet.isSemanlinkWebSite()) {
		}
				SLModel mod = SLServlet.getSLModel();
				SLDocument currentFolder = mod.smarterGetDocument(mod.filenameToUri(mod.goodDirToSaveAFile().getPath()));
				String currentFolderUri = currentFolder.getURI();
				if (!currentFolderUri.endsWith("/")) currentFolderUri+="/"; // hack pour que le titre pr�sente bien (url compl�te et non short name)
				linkPage = HTML_Link.docLink(currentFolderUri);
				%><li><html:link page="<%=linkPage%>"><%=jsp.i18l("topmenu.activFolder")%></html:link></li><%
			} // jsp.isEditor() or not