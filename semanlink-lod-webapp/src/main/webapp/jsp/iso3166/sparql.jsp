<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="true" import="net.semanlink.util.servlet.*, net.semanlink.lod.iso3166.*"
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
String sparqlURL = contextURL + "/iso3166/sparql/";

%>
<h2>Querying the ISO 3166 dataset with SPARQL</h2>
<p>The address of the sparql endpoint is </p>
<div class="kod"><p><%=sparqlURL%></p></div><p> so requests have the form:</p>
<div class="kod"><p><%=sparqlURL%>?query=[content of the SPARQL query]</p></div>
<p>The invocation of the service is a very standard HTTP connection to a URL which has one and only one parameter, called "query", and that must contain the actual query.</p>
<p>Here is a SPARQL query that requests to "DESCRIBE" (that is, to return the RDF corresponding to) the country whose 3 chars code is "BRA":</p>
<div class="kod"><pre>
PREFIX <%=Iso3166Servlet.SCHEMA_PREFIX%>: &lt;<%=Iso3166Servlet.SCHEMA_NS%>&gt;
DESCRIBE ?s WHERE {
	?s iso:alpha3 "BRA".
}
</pre></div>
<p>The formatting is just for readability, we could write as well:</p>
<%
String theQuery = "PREFIX " + Iso3166Servlet.SCHEMA_PREFIX + ":<" + Iso3166Servlet.SCHEMA_NS + "> DESCRIBE ?s WHERE {?s iso:alpha3 \"BRA\".}";
String theQueryInHTML = "PREFIX " + Iso3166Servlet.SCHEMA_PREFIX + ":&lt;" + Iso3166Servlet.SCHEMA_NS + "&gt; DESCRIBE ?s WHERE {?s iso:alpha3 \"BRA\".}";
String theEncodedQuery = java.net.URLEncoder.encode(theQuery,"UTF-8");
%>
<div class="kod"><p>
<%=theQueryInHTML%>
</p></div>
<p>To know the URIs of the properties that can be used in requests (such as the "iso:alpha3" property in the example),
look at the schema describing the ISO 3166 RDF vocabulary (link available in the right bar)</p>
<p>The "query" parameter of the request must be "URL encoded". If it contains non ASCII 127 chars,
these must be in UTF-8 (The query parameter can be computed in java with java.net.URLEncoder.encode(string, "UTF-8"), 
and in javascript with encodeURIComponent(string) One can remark a slight difference between these two functions: the former
converts a space to "+" while the latter converts it to "%20", but this doesn't matter).</p>
<p>Here is therefore the link corresponding to our example. To send the query to the SPARQL endpoint and receive the result, just click it:</p>
<div class="kod"><p>
<a href="<%=sparqlURL%>?query=<%=theEncodedQuery%>">
<%=sparqlURL%>?query=<%=theEncodedQuery%></a>
</p></div>
<p>To get the result as "N3" or "html" just add a parameter as=n3 or as=html to the request (this is a feature of this application,
outside of standard SPARQL). For instance, to get the result as N3, follow the link:</p>
<div class="kod"><p>
<a href="<%=sparqlURL%>?query=<%=theEncodedQuery%>&amp;as=n3">
<%=sparqlURL%>?query=<%=theEncodedQuery%>&amp;amp;as=n3</a>
</p></div>

<p>Such HTTP requests can be sent from a browser as we're doing here, but you can as well
use other tools supporting HTTP. For instance, you can do it in a DOS command using a tool such as WGET</p>
<h2>Try it yourself</h2>
<p>Either enter a query in the textarea, or select one of the examples (clicking one of the "Try" buttons). 
Then click one of the "RDF/XML", "RDF/N3" and "HTML" buttons to execute it. (The "Try" buttons do not exececute the query. They just provide you with
sample queries that you can modify before executing them).
</p>

<div style="display:none">
<p><b>Prefixes:</b></p>
<form action="#" id="prefixesForm">
<p>
<textarea name="prefixes" cols="80" rows="3">
PREFIX <%=Iso3166Servlet.SCHEMA_PREFIX%>: &lt;<%=Iso3166Servlet.SCHEMA_NS%>&gt;
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
        <input type="button" value="RDF/XML" onclick="prepareSparqlQuery() ; asWhat('') ; ; submit()"/>
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
		<input type="button" value="Try" onclick="chooseSparqlExample('ex3') ; return false;" />
		Search a country by its 3 char code.
	</p>
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex5') ; return false;" />
		Search a country by its numeric code.
	</p>
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex1') ; return false;" />
		Search a country by name (exact match, case sensitive).
	</p>
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex2') ; return false;" />
		Search a string in the names (partial, case unsunsitive, but beware of diacritics: é != e)
	</p>
	<p>
		<input type="button" value="Try" onclick="chooseSparqlExample('ex4') ; return false;" />
		SELECT the "républiques" and display their code 2 et 3 chars, and their name. (Note that this example uses a different kind
		of query: a SELECT and not a DESCRIBE as the former ones. SELECT returns a result as XML (not RDF. A very simple XML
		defined by the SPARQL norm, and that it easy to use to draw columns of results. In the case of such SELECT
		queries, all the 3 buttons of the form displays the raw XML, without any formatting)
	</p>
</form>

<div style="display:none">
<form id="sparqlExamples" action="" method="get">
<textarea id="ex1" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:name "NIGER"@fr.
}
</textarea>

<textarea id="ex2" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:name ?name.
        FILTER(REGEX(STR(?name), "répub", "i"))
}
</textarea>

<textarea id="ex3" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:alpha3 "BRA".
}
</textarea>

<textarea id="ex4" rows="4" cols="60">
SELECT ?a2 ?a3 ?name WHERE {
	?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:name ?name.
	?s iso:alpha2 ?a2.
	?s iso:alpha2 ?a3.
    FILTER(REGEX(STR(?name), "république", "i"))
}
</textarea>

<textarea id="ex5" rows="4" cols="60">
DESCRIBE ?s WHERE {
	?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:num "250".
}
</textarea>

</form>
</div> <% // hidden div containing the prepared examples %>

<script type="text/javascript">
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
