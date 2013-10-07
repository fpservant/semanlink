/* Created on 15 juil. 07 */
package net.semanlink.semanlink;

import net.semanlink.servlet.CoolUriServlet;
import net.semanlink.servlet.SLServlet;

public class Note {
public static boolean isNote(String uri) {
	return uri.startsWith(SLServlet.getServletUrl() + CoolUriServlet.NOTE_SERVLET_PATH + "/");
}
}
