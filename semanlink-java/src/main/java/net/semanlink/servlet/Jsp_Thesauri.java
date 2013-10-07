/* Created on 25 sept. 2004 */
package net.semanlink.servlet;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fps
 */
public class Jsp_Thesauri extends Jsp_Page {
public Jsp_Thesauri(HttpServletRequest request) {
	super(request);
}
public String getTitle() { return "Thesauri"; }
public String getContent() throws Exception {
	return "/jsp/thesauri.jsp";
}

public String getLinkToThis() throws UnsupportedEncodingException {
	return "/showthesauri.do";
}

}
