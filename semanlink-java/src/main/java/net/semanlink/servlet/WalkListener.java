/* Created on 15 déc. 03 */
package net.semanlink.servlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLLabeledResource;
import net.semanlink.semanlink.SLTree;
/**
 * To get an expanded tree
 * ATTENTION à ne pas mettre de blanc ou cr dans le dom de l'arbre (cf livetreesons.jsp)
 * (d'où les print et non println)
 * @author fps
 */
public class WalkListener implements SLTree.SLWalkListener {
private HttpServletRequest request;
private HttpServletResponse response;
private JspWriter out;
public static String DIV_ID_SEPAR = "_";
private String divIdRoot;
private String contextPath;
private Stack treePosition;

static public boolean REMOVE_PARENT_TAG_FROM_DOCS_LIST_OF_TAGS = false;
public boolean DISPLAY_DOCS_OF_ROOT = false;

/**
 * @param request
 * @param response
 * @param out
 * @param jsp par exemple "/jsp/docline.jsp"
 * @param divIdRoot sert de début aux id des divs
 */
public WalkListener(HttpServletRequest request, HttpServletResponse response, JspWriter out, String divIdRoot, Stack treePosition) {
	this.request = request;
	this.contextPath = request.getContextPath();
	String snip = request.getParameter("snip");
	if (snip != null) { // to have complete absolute urls
	    contextPath = SLServlet.getServletUrl();    
	}
	this.response = response;
	this.out = out;
	this.divIdRoot = divIdRoot;
	this.treePosition = treePosition;
	//
	// this.docJspDispatcher = request.getRequestDispatcher(jsp);
}

public void startSeed(SLKeyword kw) throws IOException {
	this.out.print("<!--WalkListener.startWalk-->");
	// this.out.print("<ul id=\"block:root" + treePosition2DivId(treePosition) + "\" class=\"livetree\">");
}
public void startList(SLLabeledResource kw) throws IOException {
	// this.out.println("<!--startList " + kw.getLabel() + "--><ul>");
	this.out.print("<ul id=\"block:" + treePosition2DivId(treePosition) + "\" class=\"livetree\">");
	//System.out.println("ul " + kw.getLabel() + " : " + treePosition2DivId(treePosition));
}
public void startKwList(SLLabeledResource kw) throws IOException {}
public void startKeyword(SLKeyword kw) throws ServletException, IOException {
	// this.out.println("<!--start keyword --><li>");
	String divId = treePosition2DivId(treePosition);
	String encodedUri = java.net.URLEncoder.encode(kw.getURI(),"UTF-8");
	this.out.print("<li><img src=\"" + this.contextPath + "/ims/box_open.gif\" id=\"trigger:" + divId +"\" alt=\"\" width=\"8px\" onclick=\"toggle2('" + divId+"', '"+encodedUri+"', 'true', 'false')\"/>");
	printHRefToKw(kw);
	//System.out.println("li " + kw.getLabel() + " : " + treePosition2DivId(treePosition));
}
public void endKeyword(SLKeyword kw) throws IOException {
	// this.out.println("</li><!--end keyword -->");
	this.out.print("</li>");
}
public void repeatKeyword(SLKeyword kw) throws ServletException, IOException {
	/*this.out.println("<li>");
	printHRefToKw1(kw);
	this.out.println("(...)</li>");*/
	// on le met, fermé. Il faudrait tester si il y a des enfants ou pas (rare!) // TODO
	String divId = treePosition2DivId(treePosition);
	String encodedUri = java.net.URLEncoder.encode(kw.getURI(),"UTF-8");
	this.out.print("<li><img src=\"" + this.contextPath + "/ims/box_closed.gif\" id=\"trigger:" + divId +"\" alt=\"\" width=\"8px\" onclick=\"toggle2('" + divId+"', '"+encodedUri+"', 'true', 'false')\"/>");
	printHRefToKw(kw);
	// il faut pour le livetree (le js toggle2 rajoute des li dedans)
	this.out.print("<ul id=\"block:" + treePosition2DivId(treePosition) + "\" class=\"livetree\">");
	this.out.print("<li></li></ul>");
}
public void endKwList(SLLabeledResource kw) throws IOException {}
public void startDocList(SLKeyword kw) {}
/**
 * @param currentKw le kw au dessous duquel on affiche doc. Permet,
 * si on le souhaite, de purger kwsOfDoc de sa valeur à l'affichage
 * @param kwsOfDoc liste des SLKeyword du doc (égale à doc.getSLKeywords())
 * (parent du doc ds l'arbre)
 */
public void printDocument(SLDocument doc, SLKeyword currentKw, List<SLKeyword> kwsOfDoc) throws Exception {
	
	if (!DISPLAY_DOCS_OF_ROOT) { // 2021-01 no doc for root
		if (treePosition.size() == 1) return;
	}
		
	this.request.setAttribute("net.semanlink.servlet.jsp.currentdoc",doc);
	
	// show or not the parent tag, depending on REMOVE_PARENT_TAG_FROM_DOCS_LIST_OF_TAGS // 2021-01
	List<SLKeyword> kwsToShow = null;
	if (REMOVE_PARENT_TAG_FROM_DOCS_LIST_OF_TAGS) {
		int n = kwsOfDoc.size();
		kwsToShow = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			SLKeyword kw = kwsOfDoc.get(i); 
			if (!currentKw.equals(kw)) kwsToShow.add(kw);
		}
				
	} else {
		kwsToShow = kwsOfDoc;
	}	
	
	this.request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", kwsToShow);	
	this.out.flush();
	// this.docJspDispatcher.include(this.request, this.response);
	String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(doc);
	request.getRequestDispatcher(docLineJspName).include(this.request, this.response);
}
public void endDocList(SLKeyword kw) {}
public void endList(SLLabeledResource kw) throws IOException {
	// this.out.println("</ul><!--endList "+ kw.getLabel() +"-->");
	this.out.print("</ul>");
}
public void endSeed(SLKeyword kw) throws IOException {
	// this.out.print("</ul>");
	this.out.print("<!--WalkListener.endWalk-->");
}

/** Ecrit un lien vers un kw */
private void printHRefToKw(SLKeyword kw) throws IOException {
	// attention à l'url rewriting !
	// this.out.print("<a href=\"" + HTML_Link.getHREF(kw) + "\">");
	// 2020-02 (?)
	this.out.print("<a href=\"" + this.response.encodeURL(HTML_Link.getTagURL(this.contextPath, kw.getURI(), false, ".html")) + "\">");
	this.out.print(kw.getLabel());
	this.out.print("</a>");
}

/** @see livetreesons.jsp */
private String treePosition2DivId(Stack treePosition) {
	StringBuffer sb = new StringBuffer(this.divIdRoot);
	for (int i = 0; i < treePosition.size(); i++) {
		sb.append(DIV_ID_SEPAR);
		sb.append(((Integer) treePosition.get(i)).toString());
	}
	return sb.toString();
}

/**
 * ATTENTION A NE PAS INTRODUIRE DE TEXTE "VIDE" ENTRE LES DIFFERENTS ELEMENTS
 * une image vide pour avoir le même nb de fils que dans le cas avec descendant (cf highlight de livesearch) et
 * avoir aussi le trigger:divid qui sert à se repérer ds le parcours de l'arbre.
 *
 * ci-dessous, mettre une id à li ne marche pas parce que pour le highlight, on met à LSHighlight,
 * puis à null, l'id de la ligne sélectionnée
 * @throws UnsupportedEncodingException
 */
public static String treeLineKwWoDescendant(SLKeyword kw, String divId) throws UnsupportedEncodingException {
// <li><img width="8px" id="trigger:<%=sonDivId%>"><html:link page="<%=link.getPage()%>"><%=label%></html:link></li>
	StringBuffer sb = new StringBuffer(64);
	sb.append("<li><img width=\"8px\" id=\"trigger:");
	sb.append(divId);
	///// ??!!!!!! HTML_Link link = HTML_Link.linkToKeyword(kw, true);
	return sb.toString();
}
} // class

