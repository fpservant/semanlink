/* Created on 3 avr. 2005 */
package net.semanlink.servlet;
import javax.servlet.http.HttpServletRequest;
import net.semanlink.semanlink.SLDocument;

/**
 */
public class DocumentFactory {
public Jsp_Document newJsp_Document(SLDocument slDoc, HttpServletRequest request) throws Exception {
	return new Jsp_Document(slDoc, request);
}

/** Nom de la jsp à utiliser pour afficher un doc ds une liste de docs. */
public String getDocLineJspName(SLDocument slDoc) throws Exception {
	return "/jsp/docline.jsp";
}
}
