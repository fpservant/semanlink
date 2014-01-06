package net.semanlink.servlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import net.semanlink.semanlink.SLThesaurus;

/**
 * Il y a des choses qui ne servent pas ds Jsp_Page qu'on étend ici, mais enfin...
 */
public class Jsp_Thesaurus extends Jsp_Page {
private SLThesaurus th;
private List kws;
public Jsp_Thesaurus(SLThesaurus th, HttpServletRequest request) throws Exception {
	super(request);
	this.th = th;
	kws = getSLModel().getKWs(th);
}

public String getTitle() { return "Thesaurus : " + th.toString() ; }
public String getLinkToThis() throws UnsupportedEncodingException {
	String x = HTML_Link.linkToThesaurus(this.th).getPage();
	// String x = super.getLinkToThis() + "?uri=" + URLEncoder.encode(this.th.getURI(), "UTF-8");
	return x;
}

public int getSize() { return this.kws.size(); }

/** A appeler pour préparer la liste des kws avant d'inclure dans la jsp kwslist.jsp. */
public Bean_KwList prepareKwsList() {
	this.beanKwList.setList(kws);
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
	return this.beanKwList;
}

/** A appeler pour préparer la liste des ancêtres avant d'inclure dans la jsp kwslist.jsp. */
public void prepareKwsWithoutParentsList() {
	this.beanKwList.setList(getSLModel().withoutParents(kws));
	this.request.setAttribute("net.semanlink.servlet.Bean_KwList", this.beanKwList);
}

public void prepareActivFiles() throws IOException, URISyntaxException {
	Bean_DocList beanDocList = new Bean_DocList();
	beanDocList.setList(SLServlet.getSLModel().getActivFiles(this.th));
	this.request.setAttribute("net.semanlink.servlet.Bean_DocList", beanDocList);
}

public String getContent() throws Exception {
	return "/jsp/thesaurus.jsp";
}

}
