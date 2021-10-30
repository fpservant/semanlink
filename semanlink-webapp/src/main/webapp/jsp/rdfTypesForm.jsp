<!--rdfTypesForm.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
// FORM TO EDIT rdf:type of tags or docs
%>
<script type="text/JavaScript"><!--
function selectedRdfTypeChanged(what) {
    var selectedOptionValue = what.options[what.selectedIndex].value;
    var selectedOptionText = what.options[what.selectedIndex].text;
    document.getElementById('rdfType').value = selectedOptionText;
}

function rdfTypeChanged() {
    document.getElementById('selectRdfType').options[0].selected = true;
}

//--></script>

<div class="graybox">
<div class="what">rdf:type</div>

<%Jsp_Resource jsp = (Jsp_Resource) request.getAttribute("net.semanlink.servlet.jsp");String docorkw;if (jsp instanceof Jsp_Document) {	docorkw = "doc";} else if (jsp instanceof Jsp_Keyword) {	docorkw =  "kw";} else {	throw new RuntimeException("UNEXPECTED");}%><p></p><html:form action="setoraddproperty">	<html:hidden property="uri" value="<%=jsp.getSLResource().getURI()%>" />
	<html:hidden property="docorkw" value="<%=docorkw%>" />
	<html:hidden property="property" value="rdf:type" />
	<html:text styleId="rdfType" property="value" size="40" onchange="rdfTypeChanged()"/>	<html:select property="selectRdfType" styleId="selectRdfType" onchange="selectedRdfTypeChanged(this)">
		<%
		Iterator it = jsp.rdfTypes();
		for(;it.hasNext();) {
			String typeValue = it.next().toString();
			%>			<html:option value="<%=typeValue%>"><%=Jsp_Resource.displayUri(typeValue)%></html:option>		<%} // for %>	</html:select>
	<html:submit property="<%=Action_SetOrAddProperty.ADD%>"><%=jsp.i18l("properties.addValue")%></html:submit>	<html:submit property="<%=Action_SetOrAddProperty.SET%>"><%=jsp.i18l("properties.setValue")%></html:submit></html:form>
</div>
<!--/rdfTypesForm.jsp-->