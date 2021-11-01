package net.semanlink.servlet;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;

import net.semanlink.semanlink.SLDocCommentUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.Util;

public class Action_SetComment extends Action_SetOrAddProperty {
protected String getPropUri(HttpServletRequest request) {
	return SLVocab.COMMENT_PROPERTY;
}
protected String getPropValue(HttpServletRequest request) {
	return request.getParameter("comment");
}


// propertyUri is the comment prop
@Override protected void setDocProperty(HttpServletRequest request, SLModel mod, SLDocument doc, String propertyUri, String propertyValue, String lang) { // 2020-11
	String contextUrl;
	try {
		contextUrl = Util.getContextURL(request);
	} catch (MalformedURLException e) { throw new RuntimeException(e); }
	
	SLDocCommentUpdate.changeComment(mod, doc, propertyValue, lang, contextUrl); // 2020-11
}
}
