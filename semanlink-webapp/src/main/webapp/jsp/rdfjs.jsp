<!--rdfjs.jsp--><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.net.*, java.util.*, glguerin.io.*"%><%/** */%>
<%
String contextPath = request.getContextPath();
String tabulatorPath = contextPath + "/scripts/tabulator/current-release/";
%><html>
  <head>
    <title>Testing the javascript RDF parser in Semanlink</title>
    <script src="<%=tabulatorPath%>log.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>util.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>uri.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/term.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/match.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/rdfparser.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/identity.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/query.js" type="text/javascript"></script>
    <script src="<%=tabulatorPath%>rdf/sources.js" type="text/javascript"></script>

    <script src="<%=contextPath%>/scripts/rdf_parsing_sl.js" type="text/javascript"></script>
 </head>
 <body onload="doIt('<%=request.getParameter("uri")%>')">
 <h1><%=request.getParameter("uri")%></h1>
 <h2>DOCUMENTS</h2>
 <div id="documents">
 </div>
 
 </body>
</html>
<!--/rdfjs.jsp-->