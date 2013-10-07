/* Created on 5 nov. 06 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;

/** to display a simple html page inside the GUI of semanlink. */
public class Jsp_SimplePage extends Jsp_Page {
public Jsp_SimplePage(HttpServletRequest request) {
	super(request);
}
String htmlFile;
void setHtmlFile(String f) {
	this.htmlFile = f;
}
public String getHtmlFile() { return this.htmlFile; }
}
