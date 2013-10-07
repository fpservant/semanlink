/* Created on 3 nov. 2012 */
package net.semanlink.jersey;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.servlet.Jsp_Document;
import net.semanlink.servlet.Manager_Document;
import net.semanlink.servlet.SLServlet;

import com.sun.jersey.api.view.Viewable;

// NOTE semanlink ne donne pas le rdf pour un doc

@Path(Constants.DOC_PATH)
public class DocumentJerseyResource {

@Context protected UriInfo uriInfo;
@Context protected HttpServletRequest servletRequest; // @find test code: how to pass information to the template.jsp
@Context protected HttpServletResponse servletResponse;

// cf CoolUriServlet.doGetOrPost

@GET
@Produces(Constants.MEDIA_TYPE_4_HTML_STRING)
public Response getHTML(
		@QueryParam("uri") String docUri,
		@QueryParam("js") String jsDoubleEncodeURIComponentFlag,
		@QueryParam("imagetobedisplayed") String imageToBeDisplayed
		) throws Exception {
	
	// all this, from CoolUriServlet.doGetOrPost and goDoc
	boolean isJsDoubleEncodeURIComponent = (jsDoubleEncodeURIComponentFlag != null);
	if (docUri != null) {
		if (isJsDoubleEncodeURIComponent) docUri = URLDecoder.decode(docUri,"UTF-8");

		SLModel mod = SLServlet.getSLModel();
		SLDocument doc = mod.getDocument(docUri);
		Jsp_Document jsp = Manager_Document.getDocumentFactory().newJsp_Document(doc, servletRequest);

  	if (imageToBeDisplayed != null) {
  		// CoolUriServlet had following comment:
  		// JE NE COMPRENDS PAS POURQUOI MAIS,
  		// alors que docuri a ete encode, il ne faut pas ici le decoder.
  		// VOIR AUSSI ici + haut et ds Action_NextImage et Action_ShowKeyword
  		jsp.setImageToBeDisplayed(mod.getDocument(imageToBeDisplayed), -1); // en vrai, on n'a besoin que de l'uri - at this time
  	}
  	
  	servletRequest.setAttribute("net.semanlink.servlet.jsp", jsp);
  	//  RequestDispatcher requestDispatcher = servletRequest.getRequestDispatcher(jsp.getTemplate());   
  	//  requestDispatcher.forward(servletRequest, servletResponse);
  	ResponseBuilder builder = Response.status(200).entity(new Viewable(jsp.getTemplate(), null));
  	return builder.build();
	}

	return null; // TODO
}
}
