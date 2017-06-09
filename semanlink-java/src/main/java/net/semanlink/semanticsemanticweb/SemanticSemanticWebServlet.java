/* Created on 20 mai 07 */
package net.semanlink.semanticsemanticweb;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.jena.rdf.model.*;

import net.semanlink.semanlink.SLModel;
import net.semanlink.servlet.SLServlet;

public class SemanticSemanticWebServlet extends HttpServlet {
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	doIt(req,res);
}
public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	doIt(req,res);
}

public void doIt(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	req.setCharacterEncoding("UTF-8");
	SemanticSemanticWebRequest sswRequest = new SemanticSemanticWebRequest(req, res);
	sswRequest.handleSemanlink();
	sswRequest.handleDelicious();
	sswRequest.handleDbPedia();
	req.setAttribute("net.semanlink.servlet.rdf", sswRequest.getModel());
	RequestDispatcher requestDispatcher = req.getRequestDispatcher("/rdf"); // forward to the RDFServlet
	requestDispatcher.forward(req, res);
}

}
