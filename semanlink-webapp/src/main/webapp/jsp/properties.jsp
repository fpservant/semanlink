<!--properties.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><script type="text/JavaScript"><!--
function selectedPropertyChanged(what) {
    var selectedOptionValue = what.options[what.selectedIndex].value;
    var selectedOptionText = what.options[what.selectedIndex].text;
    document.getElementById('property').value = selectedOptionText;
}

function propertyChanged() {
    document.getElementById('selectProp').options[0].selected = true;
}

//--></script>

<%Jsp_Resource jsp = (Jsp_Resource) request.getAttribute("net.semanlink.servlet.jsp");// PARAMETRAGE DU DISPLAYboolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));String background = (String) request.getAttribute("net.semanlink.servlet.background");int bg = 0;if (background != null) {	bg = 1;}// séparer le Comment des autres pptés ?// boolean commentAPart = ((bg == 0) && (!edit));// boolean commentAPart = (bg == 0);// mettre le titre "Properties", "Comment", etc...boolean writeTitle = (bg == 0);String title = null;if (writeTitle) {	if (jsp instanceof Jsp_Document) {		title = "<div class=\"what\">" + jsp.i18l("properties.aboutThisDoc") + "</div>";	} else {		title = "<div class=\"what\">" + jsp.i18l("properties.properties") + "</div>";
	}}//SLResource x = jsp.getSLResource();String uri =x.getURI();%><% /////////////////////////////////////////// PROPERTIES %><%// ne rien afficher si liste vide -- sauf si editboolean inited = false;if (edit) {	%><div class="graybox"><%	if (writeTitle) {%>		<%=title%>	<%} // if (writeTitle) %>	<ul>	<%	inited = true;} // if (edit)HashMap hm = x.getProperties();
String[] propUris = new String[hm.size()];
hm.keySet().toArray(propUris);
Arrays.sort(propUris);for (int iprop=0;iprop<propUris.length;iprop++) {	String propUri = propUris[iprop];	// if (propUri.startsWith(SLVocab.SEMANLINK_SCHEMA)) continue; // ne pas afficher les ressources semanlink, sensées être par ailleurs ds cette page	if (SLVocab.HAS_KEYWORD_PROPERTY.equals(propUri)) continue;	if (SLVocab.HAS_PARENT_PROPERTY.equals(propUri)) continue;	if (SLVocab.HAS_ALIAS_PROPERTY.equals(propUri)) continue;	if (SLVocab.HAS_FRIEND_PROPERTY.equals(propUri)) continue;
	// if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propUri)) continue;	// if (propUri.endsWith("rdf-syntax-ns#type")) continue;	if (SLVocab.COMMENT_PROPERTY.equals(propUri)) continue;	if (SLVocab.TITLE_PROPERTY.equals(propUri)) continue;	if ("http://www.w3.org/2000/01/rdf-schema#label".equals(propUri)) continue;		if (!inited) { // 2019-02		%><div class="graybox" id="aboutThisDoc"><%		if (writeTitle) {%>			<%=title%>		<%} // if (writeTitle) %>		<ul>		<%		inited = true;	}	PropertyValues props = (PropertyValues) hm.get(propUri);	int n = props.size();	HTML_Link link = null;
	String propUriDisplay = jsp.displayUri(propUri);	if (n < 2) {			String objUri = props.getUri(0);			if (objUri != null) {				if ("rdf:type".equals(propUriDisplay)) {					link = HTML_Link.linkToProp("/showprop.do", propUri, objUri, null);			        %>			        <li><%=propUriDisplay%> : <html:link page="<%=link.getPage()%>"><%=Jsp_Resource.displayUri(props.getString(0))%></html:link></a></li>			        <%				} else {					// uri http: on l'affiche
					%>					<li><%=propUriDisplay%> : <a href="<%=objUri%>"><%=Jsp_Resource.displayUri(props.getString(0))%></a></li>					<%				}
			} else {				// la prop est une string. On met un lien seulement si pas trop longue (genre date)				// Pas sur un label				String tex = props.getString(0);				if ((tex.length() < 32) && (!propUriDisplay.toLowerCase().contains("label"))) {					link = HTML_Link.linkToProp(propUri, props, 0, "find");									%>					<li><%=propUriDisplay%> : <html:link page="<%=link.getPage()%>"><%=tex%></html:link></li>					<%				} else {					%>					<li><%=propUriDisplay%> : <%=tex%></li>					<%				}			}	} else {			%>			<li><%=propUriDisplay%> : <ul>				<%				for (int i = 0; i < n; i++) {					String objUri = props.getUri(i);					if (objUri != null) {						// uri http: on l'affiche - avec un cas spécial des tags ayant pour type le type objUri						if (!propUri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
							%>
							<li><a href="<%=objUri%>"><%=props.getString(i)%></a></li>
							<%
						} else { // ppté rdf:type: display tags with this type (except when it is the type tag!)
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
						}					} else {			            // la prop est une string. On met un lien seulement si pas trop longue (genre date)			            // Pas sur un label			            String tex = props.getString(i);			            if ((tex.length() < 32) && (!propUriDisplay.contains("label"))) {							link = HTML_Link.linkToProp(propUri, props, i, "find");											%>							<li><html:link page="<%=link.getPage()%>"><%=tex%></html:link></li>							<%						} else {							%>							<li><%=tex%></li>							<%						}					}				}				%>				</ul>			</li>			<%	}}if (inited) { %></ul><% } %><%if (edit) {	String docorkw;	if (x instanceof net.semanlink.semanlink.SLDocument) {		docorkw = "doc";	} else {		docorkw =  "kw";	}	%>	<html:form action="setoraddproperty">		<p>
		<html:hidden property="uri" value="<%=uri%>" />		<html:hidden property="docorkw" value="<%=docorkw%>" />		<b><%=jsp.i18l("properties.editProp")%> </b><html:text styleId="property" property="property" size="40" onchange="propertyChanged()"/>		<html:select property="selectProp" styleId="selectProp" onchange="selectedPropertyChanged(this)">
			<html:option value="-">-</html:option>
			<%
			/*for (int i = 0; i < SLVocab.COMMON_PROPERTIES.length; i++) {				SLVocab.EasyProperty prop = SLVocab.COMMON_PROPERTIES[i];*/
			SLVocab.EasyProperty[] easyProps = SLServlet.getEasyProps(); 
			for (int i = 0; i < easyProps.length; i++) {
				SLVocab.EasyProperty prop = easyProps[i];				String propShortName = prop.getName();				String propUri = prop.getUri(); // on pourrait le passer à la form, mais vu qu'on a aussi le cas court
				if (SLVocab.TITLE_PROPERTY.equals(propUri)) continue;				if (SLVocab.COMMENT_PROPERTY.equals(propUri)) continue;
				if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propUri)) continue;
				%>				<html:option value="<%=propShortName%>"><%=propShortName%></html:option>			<%} // for %>		</html:select>
		<br/>		<html:textarea property="value" cols="80" rows="6"/>		<br/>		<html:select property="lang">			<html:option value="-">-</html:option>			<html:option value="fr">fr</html:option>			<html:option value="en">en</html:option>			<html:option value="es">es</html:option>			<html:option value="pt">pt</html:option>		</html:select>		<html:submit property="<%=Action_SetOrAddProperty.ADD%>"><%=jsp.i18l("properties.addValue")%></html:submit>		<html:submit property="<%=Action_SetOrAddProperty.SET%>"><%=jsp.i18l("properties.setValue")%></html:submit>	</p>
	</html:form><% } // if (editif (inited) { %>	</div> <%//graybox%><% } %><!--/properties.jsp-->