/* Created on 23 juin 2009 */
package net.semanlink.util.jena;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;

public class RDFServletWriterUtil {

public static void writeRDF(Model model, HttpServletResponse response) throws IOException, ServletException {
	RDFServletWriterUtil.writeRDF(model, response, null, null, null);
}

public static void writeRDF(Model model, HttpServletResponse response, String xmlBase, String rdfKind, String relativeURIsProp) throws IOException, ServletException {
	try {
		
			// TODO !!!
		
			// BEWARE: XMLHTTPRequests prefers "text/xml" - but we should return application/rdf+xml
			// I had changed the content type here to test the javascript rdf parser with ie
			// (there is a special thing in the javascript in case "the server fails to set the content type to "text/html")
			// I should try again with ""application/rdf+xml"
			// AND see what should really be returned for content type: doesn't it depend on the request?
			if ("N3".equals(rdfKind)) {
				response.setContentType("text/rdf+n3; charset=UTF-8"); 
			} else {
				response.setContentType("application/rdf+xml; charset=UTF-8"); // cf javadoc The given content type may include a character encoding specification, for example, text/html;charset=UTF-8
				// response.setContentType("text/xml"); // cf javadoc The given content type may include a character encoding specification, for example, text/html;charset=UTF-8
			}
			
			RDFWriterUtil.writeRDF(model, response.getOutputStream(), xmlBase, rdfKind, relativeURIsProp);
	} catch (Exception e) {
	 e.printStackTrace();
	 throw new RuntimeException(e);
	}
}

}
