package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;

import net.semanlink.semanlink.SLVocab;

/**
 */
public class Params_Jsp {
private String welcomePageJsp;
public String getDefaultSortProperty() {
	String x = SLServlet.getDefaultSortProperty();
	if (x == null) return SLVocab.SL_CREATION_TIME_PROPERTY;
	return x;
}

public String getTemplate(HttpServletRequest req) throws Exception {
	return "/jsp/template.jsp";
}


public String getTopMenu(HttpServletRequest req) throws Exception {
	return "/jsp/topmenu.jsp";
}

public String getSideMenu(HttpServletRequest req) throws Exception {
	return "/jsp/sidemenu.jsp";
}

/** cf this month ds si cg TODO : à voir */
public String getDateProperty(HttpServletRequest req) throws Exception {
	String x = SLServlet.getDefaultDateProperty();
	if (x == null) return SLVocab.SL_CREATION_DATE_PROPERTY;
	return x;
}

/** name of file containing text to be displayed on welcome page. */
public String getWelcomePageJsp() { return this.welcomePageJsp; }
void setWelcomePageJsp(String s) { this.welcomePageJsp = s;}
}
