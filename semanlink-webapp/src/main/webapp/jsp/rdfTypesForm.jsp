<!--rdfTypesForm.jsp--><%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*,java.util.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
// FORM TO EDIT rdf:type of tags
%>
<script type="text/JavaScript"><!--
function selectedRdfTypeChanged(what) {
    var selectedOptionValue = what.options[what.selectedIndex].value;
    var selectedOptionText = what.options[what.selectedIndex].text;
    document.getElementById('rdfType').value = selectedOptionText;
}

function rdfTypeChanged() {
	alert("rdfTypeChanged");
    document.getElementById('selectRdfType').options[0].selected = true;
}

//--></script>

<div class="graybox">
<div class="what">rdf:type</div>

<%Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");String docorkw;if (jsp instanceof net.semanlink.semanlink.SLDocument) {	docorkw = "doc";} else {	docorkw =  "kw";}%><p><html:form action="setoraddproperty">	<html:hidden property="uri" value="<%=jsp.getSLResource().getURI()%>" />
	<html:hidden property="docorkw" value="<%=docorkw%>" />
	<html:hidden property="property" value="rdf:type" />
	<b>rdf:type </b><html:text styleId="rdfType" property="value" size="40" onchange="rdfTypeChanged()"/>	<html:select property="selectRdfType" styleId="selectRdfType" onchange="selectedRdfTypeChanged(this)">
		<html:option value="-">-</html:option>
		<%
		Iterator it = jsp.rdfTypes4Tags();
		for(;it.hasNext();) {
			String typeValue = it.next().toString();
			%>			<html:option value="<%=typeValue%>"><%=Jsp_Resource.displayUri(typeValue)%></html:option>		<%} // for %>	</html:select>
	<br/>	<html:submit property="<%=Action_SetOrAddProperty.ADD%>"><%=jsp.i18l("properties.addValue")%></html:submit>	<html:submit property="<%=Action_SetOrAddProperty.SET%>"><%=jsp.i18l("properties.setValue")%></html:submit></html:form></p>
</div>
<!--/rdfTypesForm.jsp-->