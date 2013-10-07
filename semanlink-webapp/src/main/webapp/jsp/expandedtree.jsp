<!-- expandedtree.jsp -->
<%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*, java.util.*"%>
<% 
Jsp_Keyword jsp = (Jsp_Keyword) request.getAttribute("net.semanlink.servlet.jsp");

jsp.setShowKeywordsInDocList(true); // pour afficher les keywords des docs de la liste

// SLKeyword kw = (SLKeyword) jsp.getSLResource();
// SLTree tree = new SLTree(kw, "children", null, SLServlet.getSLModel());
SLTree tree = jsp.getTree();
Stack treePosition = new Stack();
WalkListener walkListener = new WalkListener(request, response, out, "kwtree", treePosition);
%>
<div class="docslistcontainer">
<%
tree.walk(walkListener, treePosition);
%>
</div>
<!-- /expandedtree.jsp -->
