package net.semanlink.servlet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import net.semanlink.semanlink.*;
import net.semanlink.util.Util;

import javax.servlet.http.*;

public class Jsp_Welcome extends Jsp_Page { // juste pour la liste des docs de activ folder
private SLModel model;
private Bean_KwList bean_KwList;
private Bean_DocList bean_DocList;
public Jsp_Welcome(HttpServletRequest request) {
	super(request);
	this.model = SLServlet.getSLModel();
	this.bean_KwList = new Bean_KwList();
	this.bean_DocList = new Bean_DocList();
	request.setAttribute("net.semanlink.servlet.Bean_KwList", bean_KwList);
	request.setAttribute("net.semanlink.servlet.jsp", this);
}

public String getTitle() { return "Semanlink Home Page"; }

/** attention, change au fur et à mesure (voir par ex getActivFolder) */
public Bean_DocList getDocList() { return this.bean_DocList; }
public Bean_KwList getKwList() { return this.bean_KwList; }


/** A appeler pour préparer la liste des ancêtres avant d'inclure dans la jsp kwslist.jsp. */
public void prepareKwsWithoutParentsList() {
	//this.bean_KwList = new Bean_KwList();
	//request.setAttribute("net.semanlink.servlet.Bean_KwList", bean_KwList);
	this.bean_KwList.setList(model.withoutParents(model.getKWsInConceptsSpaceArrayList()));
}

public SLKeyword getActivFolder() {
	SLKeyword x = getSLModel().getActivFolder();
	this.bean_DocList.setList(x.getDocuments());
	return x;
}

public String getContent() throws Exception {
	return SLServlet.getJspParams().getWelcomePageJsp();
}

/*public String getLinkToThis() throws UnsupportedEncodingException {
	// return "/welcome.do";
	return this.request.getPathInfo();
}*/

// used ?? (yes in link "semanlink it2")
public String bookmarkletJavascript() throws MalformedURLException {
	StringBuffer x = new StringBuffer(128);
	x.append("javascript:");
	x.append("s='';");
	x.append("q='';");
	// x.append("r='';");
	x.append("l='';");
	String mainFrame = SLServlet.getMainFrame();
	if (mainFrame != null) { // case of an app with frames
		x.append("if%20(parent."+mainFrame+")%20{s=parent."+mainFrame+".location;}");
		x.append("%20else%20{s=location.href;}");
	} else {
		x.append("s=location.href;");
	}
	x.append("%20if(window.getSelection){q=window.getSelection();}");
	x.append("%20else%20if%20(document.getSelection)%20{q=document.getSelection();}");
	if (!Util.isIE(request)) { // following js line doesn't seem to work in ie
		x.append("%20else%20if%20(document.selection)%20{q=document.selection.createRange().text;}");
	}
	// x.append("if (document.referrer) r=document.referrer;");
	// x.append("if (typeof(_ref)!='undefined') r=_ref;");
	x.append("%20location.href='");
	x.append(Util.getContextURL(request));
	x.append("/bookmarkform.do?docuri='+encodeURIComponent(encodeURIComponent(s))+'&title='+encodeURIComponent(encodeURIComponent(document.title))+'&comment='+encodeURIComponent(encodeURIComponent(q))+'&lang='+encodeURIComponent(l)"); // +'&via='+encodeURIComponent(r);");
	return x.toString();
}

//
// 2020-05 all this to display tag cloud on home page
//

// deactivated actually, see getLinkedKeywords2NbHashMap

public HashMap getLinkedKeywords2NbHashMap() throws Exception {
	boolean displayTagCloudOnHonePage = false;
	if (displayTagCloudOnHonePage) {
		List docs = getDocs();
		return SLUtils.getLinkedKeywords2NbHashMap(docs);
	} else {
		return null;
	}
}

public List getDocs() throws Exception {
	return computeDocs();
}

public List computeDocs() throws Exception {
	SLModel mod = SLServlet.getSLModel();
	return mod.getRecentDocs(30, SLServlet.getJspParams().getDateProperty(request));
}

public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	return HTML_Link.getHTML_Link(otherKw);
}

//
// all this to display tag cloud on home page END
//


//
//
//

} // class Jsp_Welcome
