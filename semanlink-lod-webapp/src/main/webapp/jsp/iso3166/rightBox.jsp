<%@ page
    contentType="text/html;charset=UTF-8"  pageEncoding="UTF-8" language="java" session="true" import="net.semanlink.util.servlet.*,net.semanlink.lod.iso3166.*"
%>

<%
Jsp_Page jsp = (Jsp_Page) request.getAttribute("jsp");
String contextURL = jsp.getContextURL();
%>
<div class="watgraybox">
<div class="what">Complete file</div>
<ul>
	<li><a href="<%=contextURL%>/iso3166">RDF/XML</a></li>
	<li><a href="<%=contextURL%>/iso3166.n3">RDF/N3</a></li>
	<li><a href="<%=contextURL%>/iso3166.html">HTML</a></li>
</ul>
</div><%//watgraybox %>
<p></p>

<div class="watgraybox">
<div class="what">Schema</div>
<ul>
	<li><a href="<%=contextURL%>/iso3166-schema">RDF/XML</a></li>
	<li><a href="<%=contextURL%>/iso3166-schema.html">HTML</a></li>
</ul>
</div><%//watgraybox %>
<p></p>

<div class="watgraybox">
<div class="what">Code (2 or 3 chars, or numeric)</div>
<script type="text/javascript">
	/*
	function codeFormSubmitHTML() {
		var code = getCode();
		if (code) {
			// alert(window.location.toString());
			<%
			// goto Http;//.../iso3166.html#xx doesn't work when you're not already on the page
			// hence the test (if we're not on that page, we display only the result of the sparql query
			// else we jump to the anchor
			%>
			if (startsWith(window.location.toString(), "<%=contextURL%>/iso3166.html")) {
				var x = "<%=contextURL%>/iso3166.html#" + code;
				window.location = x;
			} else {
				var x = codeSparqlQuery();
				// if (!x) return;
				x = x + "&as=html";
				window.location = x;
			// }
		}
	}

	function codeFormSubmitRDF() {
		var x = codeSparqlQuery();
		if (x) window.location = x;
	}
	*/

	function codeFormSubmit(inWhat) {
		var x = codeSparqlQuery();
		if (x) {
			if (inWhat) x = x + "&as=" + inWhat;
			window.location = x;
		}
	}

	function codeSparqlQuery() {
		var code = getCode();
		if (!code) return false;
		var prop = "";
		if (isNum(code)) {
			prop = "<%=Iso3166Servlet.SCHEMA_PREFIX%>:num";
		} else {
			prop = "<%=Iso3166Servlet.SCHEMA_PREFIX%>:alpha" + code.length;
		}
		var x = "PREFIX <%=Iso3166Servlet.SCHEMA_PREFIX%>: <<%=Iso3166Servlet.SCHEMA_NS%>>"
		+ " DESCRIBE ?s WHERE { ?s " + prop + " '" + code + "'.}";
		return "<%=contextURL%>/iso3166/sparql/?query=" + encodeURIComponent(x);
	}
	
	function getCode() {
		var code = document.getElementById("codeForm").code.value;
		if (isNum(code)) {
			return code;
		} else {
			if ((code.length != 2) && (code.length != 3)) {
				alert("Code should be a positiv integer or a 2 or 3 chars long string");
				return false;
			}
		}
		return code.toUpperCase();
	}
	
	/* beware : 0 is not consider a number by this */
	function isNum(s) {
		var k = s * 1;
		return ((k) && (k != 0) && (k != NaN));		
	}
	
</script>


<form id="codeForm" action="">
	<p>
	<input type="text" name="code" value="" onkeypress="if(event.keyCode==13) {codeFormSubmit('html'); return false;}" />
	</p><p>
	<input type="button" onclick="codeFormSubmit(false)" value="RDF/XML" />
	<input type="button" onclick="codeFormSubmit('n3')" value="RDF/N3" />
	<input type="button" onclick="codeFormSubmit('html')" value="HTML" />
	</p>
</form>
</div><%//watgraybox %>

<p></p>

<div class="watgraybox">
<div class="what">Search names</div>
<script type="text/javascript">
	/** inWhat = html, or n3 or false for rdf/xml */
	function searchFormSubmit(inWhat) {
		// var x = "<%=contextURL%>/iso3166/sparql/?query=" + encodeURIComponent(encodeURIComponent(searchSparqlQuery()));
		var x = "<%=contextURL%>/iso3166/sparql/?query=" + encodeURIComponent(searchSparqlQuery());
		if (inWhat) x = x + "&as=" + inWhat;
		window.location = x;
	}

	function searchSparqlQuery() {
		return "PREFIX <%=Iso3166Servlet.SCHEMA_PREFIX%>: <<%=Iso3166Servlet.SCHEMA_NS%>>"
		+ " DESCRIBE ?s WHERE { ?s <%=Iso3166Servlet.SCHEMA_PREFIX%>:name ?n FILTER(REGEX(STR(?n), '" + document.getElementById("searchForm").search.value + "', 'i'))}";
	}
	
</script>


<form id="searchForm" action="">
	<p>
	<input type="text" name="search" value="" onkeypress="if(event.keyCode==13) {searchFormSubmit('html'); return false;}" />
	</p><p>
	<input type="button" onclick="searchFormSubmit(false)" value="RDF/XML" />
	<input type="button" onclick="searchFormSubmit('n3')" value="RDF/N3" />
	<input type="button" onclick="searchFormSubmit('html')" value="HTML" />
	</p>
</form>
</div><%//watgraybox %>

<p></p>

<div class="watgraybox">
<div class="what">SPARQL</div>
<ul>
<li><a href="<%=contextURL%>/iso3166/sparql/">Endpoint GUI</a></li>
</ul>
</div>

<p></p>
<div class="watgraybox">
<div class="what">About</div>
<ul>
	<li>A simple application of RDF data publishing</li>
</ul>
</div><%//watgraybox %>
<p></p>

