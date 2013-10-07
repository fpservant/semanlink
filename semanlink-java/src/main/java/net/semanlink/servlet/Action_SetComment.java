package net.semanlink.servlet;
import net.semanlink.semanlink.SLVocab; 
import javax.servlet.http.HttpServletRequest;

public class Action_SetComment extends Action_SetOrAddProperty {
	protected String getPropUri(HttpServletRequest request) {
		return SLVocab.COMMENT_PROPERTY;
	}
	protected String getPropValue(HttpServletRequest request) {
		return request.getParameter("comment");
	}

}
