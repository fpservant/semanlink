<!--properties.jsp-->
function selectedPropertyChanged(what) {
    var selectedOptionValue = what.options[what.selectedIndex].value;
    var selectedOptionText = what.options[what.selectedIndex].text;
    document.getElementById('property').value = selectedOptionText;
}

function propertyChanged() {
    document.getElementById('selectProp').options[0].selected = true;
}

//--></script>

<%
	}
if (false) {
	if (!edit) {
				<div class="what"><%=jsp.i18l("x.comment")%></div>
				<%
				if (comment.indexOf("<p>") > -1) { // BOF %>
					<%=comment%>
				<%} else { %>
					<p><%=comment%></p>
				<%}%>
			</div><%
				<p>
				</p>
} // if false
String[] propUris = new String[hm.size()];
hm.keySet().toArray(propUris);
Arrays.sort(propUris);
	// if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propUri)) continue;
	String propUriDisplay = jsp.displayUri(propUri);
				%>
			} else {
							%>
							<li><a href="<%=objUri%>"><%=props.getString(i)%></a></li>
							<%
						} else { // ppt� rdf:type: display tags with this type (except when it is the type tag!)
							if (objUri.equals(SLVocab.KEYWORD_CLASS)) {
								%>
								<li><%=Jsp_Resource.displayUri(props.getString(i))%></li>
								<%
							} else {
								link = HTML_Link.linkToProp(propUri, props, i, "find");				
								%>
								<li><html:link page="<%=link.getPage()%>"><%=Jsp_Resource.displayUri(props.getString(i))%></html:link></li>
								<%
							}
						}
		<html:hidden property="uri" value="<%=uri%>" />
			<html:option value="-">-</html:option>
			<%
			/*for (int i = 0; i < SLVocab.COMMON_PROPERTIES.length; i++) {
			SLVocab.EasyProperty[] easyProps = SLServlet.getEasyProps(); 
			for (int i = 0; i < easyProps.length; i++) {
				SLVocab.EasyProperty prop = easyProps[i];
				if (SLVocab.TITLE_PROPERTY.equals(propUri)) continue;
				if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propUri)) continue;
				%>
		<br/>
	</html:form>