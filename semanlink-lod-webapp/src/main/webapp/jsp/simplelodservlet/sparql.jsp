<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="false" import="net.semanlink.util.servlet.*"
%>
<style type="text/css">
<!--
.kod {
	font-family: "Courier New", Courier, mono;
	font-size: 12px;
	background-color: #FFFFCC;
	border: thin solid;
	margin-right: 80px;
	margin-left: 80px;
	margin-bottom: 12px;
	margin-top: 12px;
}
.kod p {
	margin-top: 2px;
	margin-bottom: 2px;
}
-->
</style>
<%
/*
 It seems that we need to encode the query twice if we want it to work
 when it contains diacritics. So,
 - if we use windows.location = url, we must call encodeURIComponent twice.
 - if we use the content of a GET form field, we must encode it once with encodeURIComponent, the 
 form enconding it once more
 
 sparql as rdf: the servlet calls the SPARQLEndpoint, which decodes once the "query"
 parameter of the request
 
 sparql as html: the servlet get the "query" param, encode it, and pass it to the html page that loads rdf,
 where it is written as in in the "onload" on the body tag
 
 But in fact, a single encoding works IF we do not use diacritics (decoding twice something encoding only one is no problem)
 That is, we can encode only once IF we encode the diacritics - that is, if we write "%C3%A9" instead of "é" in an input field.
 
*/
String contextURL =  BasicServlet.getContextURL(request);
String sparqlURL = contextURL + "/sparql/";

%>
<div style="display:none">
<p><b>Prefixes:</b></p>
<form action="#" id="prefixesForm">
<p>
<textarea name="prefixes" cols="80" rows="3">
</textarea></p>
</form>
</div>
<%
   // This is not the form used to submit the query
%>
<p><b>Query:</b></p>
<form action="#" method="get" id="sparqlInputForm">
	<p><textarea name="q" cols="80" rows="8"></textarea></p>
</form>

<% // THE FORM THAT IS ACTUALLY USED TO SUBMIT THE QUERY: 
   // THE 2 "RDF" AND "HTML" BTNS, AND A HIDDEN "query" FIELD %>
 	<form id="sparqlSubmitQueryForm" action="<%=sparqlURL%>" method="get" >
        <p>
        <input type="hidden" name="query" value="" />
        <input type="hidden" name="as" value="" />
        <input type="button" value="RDF/XML" onclick="prepareSparqlQuery() ; asWhat('rdf') ; ; submit()"/>
        <input type="button" value="RDF/N3" onclick="prepareSparqlQuery() ; asWhat('n3') ; ; submit()"/>
        <input type="button" value="HTML" onclick="prepareSparqlQuery() ; asWhat('html') ; submit()"/>
		</p>
	</form>
	<p>&nbsp;</p>

<% // ONE BUTTON FOR EACH OF THE PREPARED EXAMPLES 
   // AND THE TEXAREA
   // This is not the form used to submit the query
%>
<form action="#" method="get" id="formOfButtons">
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex1') ; return false;" />
		Describe subjects (limit to 20).
	</p>
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex2') ; return false;" />
		Search a string (partial, case unsunsitive, but beware of diacritics: é != e)
	</p>
</form>

<div style="display:none">
<form id="sparqlExamples" action="" method="get">
<textarea id="ex2" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s ?p ?text.
        FILTER(REGEX(STR(?text), "some text", "i"))
}
</textarea>

<textarea id="ex1" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s ?p ?o
}
LIMIT 20
</textarea>


</form>
</div> <% // hidden div containing the prepared examples %>

<script>
		<% // put the example into the textarea (where we enter a query). %>
	function chooseSparqlExample(exampleId) {
		document.getElementById('sparqlInputForm').q.value = document.getElementById("prefixesForm").prefixes.value 
		+ document.getElementById(exampleId).value;
	}

	<%
	// prepare and put actual request into the form that is submitted to execute the query
	/** @param textAreaFormId id of form containing the textArea with input of query's body. Must contain a textarea called q
	  * @param formId id of actual form containg the submit button. Must contain an input text called query*/
	%>
	function prepareSparqlQuery() { 
		/*document.getElementById('sparqlSubmitQueryForm').query.value = encodeURIComponent(
			document.getElementById("sparqlInputForm").q.value
		);*/
		document.getElementById('sparqlSubmitQueryForm').query.value = 
			document.getElementById("sparqlInputForm").q.value
		;
		/*
		document.getElementById('output').q.value = encodeURIComponent(
			document.getElementById("sparqlInputForm").q.value
		);*/
		
	}

	function asWhat(what) {
		document.getElementById('sparqlSubmitQueryForm').as.value = what;
	}
</script>
