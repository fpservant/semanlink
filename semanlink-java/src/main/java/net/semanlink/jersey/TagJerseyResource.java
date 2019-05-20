/* Created on 2 nov. 2012 */
package net.semanlink.jersey;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.servlet.BaseAction;
import net.semanlink.servlet.CoolUriServlet;
import net.semanlink.servlet.Jsp_Keyword;
import net.semanlink.servlet.RDFOutput;
import net.semanlink.servlet.RDFOutput_Keyword;
import net.semanlink.servlet.RDFServlet;
import net.semanlink.servlet.SLUrisAsSkos;
import net.semanlink.util.Util;
import net.semanlink.util.jena.RDFWriterUtil;

import org.apache.jena.rdf.model.Model;
// import com.sun.jersey.api.view.Viewable;
import org.glassfish.jersey.server.mvc.Viewable;

// TODO 	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08

// TODO SysRIOT.wireIntoJena(); pour JSON/RDF

// TODO JSON-LD

//TODO : les params (autres que ?edit=true, qui est fait)

@Path(Constants.TAG_PATH + "/{id}")
public class TagJerseyResource {
private @PathParam("id") String id;
private @QueryParam(value="edit") @DefaultValue("false") boolean edit;

@Context protected Providers providers;
@Context protected UriInfo uriInfo;
@Context protected HttpServletRequest servletRequest; // @find test code: how to pass information to the template.jsp
@Context protected HttpServletResponse servletResponse;

@GET
@Produces(Constants.MEDIA_TYPE_4_RDF_XML_STRING)
public StreamingOutput getRDF() {
	System.out.println("TagJerseyResource. RDF()");
  return new StreamingOutput() { 
    public void write(OutputStream out) {
    	writeJenaModel(out, jenaModel(), "RDF/XML-ABBREV");
    }
  };  	    	
}

@GET
@Produces(Constants.MEDIA_TYPE_4_TURTLE_STRING)
public StreamingOutput getTTL() {
	System.out.println("TagJerseyResource. getTTL()");
  return new StreamingOutput() { 
    public void write(OutputStream out) {
    	writeJenaModel(out, jenaModel(), "N3");
    }
  };  	    	
}

@GET
@Produces(Constants.MEDIA_TYPE_4_HTML_STRING)
public Response getHTML() throws Exception {
	// TODO: 303 -- hmm: but not OK with the ".extension" and MediaTypeFilter
	System.out.println("TagJerseyResource. getHTML(), edit: " + edit);
	
	Jsp_Keyword jsp = getJSP();
	servletRequest.setAttribute("net.semanlink.servlet.jsp", jsp);

	// 2013-04
//	String s = uriInfo.getQueryParameters().getFirst("edit");
//	if (s != null) {
//		boolean edit = "true".equals(s.toLowerCase());
//		BaseAction.setEdit(servletRequest, edit);
//	}
	BaseAction.setEdit(servletRequest, edit);

	//  RequestDispatcher requestDispatcher = servletRequest.getRequestDispatcher(jsp.getTemplate());   
	//  requestDispatcher.forward(servletRequest, servletResponse);
	ResponseBuilder builder = Response.status(200).entity(new Viewable(jsp.getTemplate(), null));
	return builder.build();
}




// (from CoolUriServlet)
private Jsp_Keyword getJSP() {
	// System.out.println("uriInfo.getBaseUri()"+uriInfo.getBaseUri()); // http://127.0.0.1:7080/semanlink/sl/resources/
	// String oldTagUri = "http://www.semanlink.net/tag/"+ id; // TODO
	// String oldTagUri = "http://127.0.0.1:7080/semanlink/tag/"+ id; // TODO
	String oldTagUri;
	try {
		oldTagUri = Util.getContextURL(servletRequest) + "/tag/" + id;
	} catch (MalformedURLException e) { throw new RuntimeException(e);	}
	System.out.println(oldTagUri);
  SLKeyword kw = CoolUriServlet.getSLKeyword(oldTagUri, servletRequest);
	return new Jsp_Keyword(kw, servletRequest);
}

//
//
//

protected Model jenaModel() {
	System.out.println("TagJerseyResource.jenaModel()");

	Jsp_Keyword jsp = getJSP();
	try {
		/*
		// return jsp.getRDF(); // this is:
		RDFOutput rdfOutput = new RDFOutput_Keyword(jsp);
		return rdfOutput.getModel();
		*/
		RDFOutput rdfOutput = new RDFOutput_Keyword(jsp, null);
		// to return skos
		rdfOutput.setUrisToUse(SLUrisAsSkos.getInstance());
		return rdfOutput.getModel();
	} catch (Exception e) { throw new RuntimeException(e) ; }
}

// @jenaOutputFormat null "N3" or "RDF/JSON"
public static String writeJenaModel(OutputStream out, Model mod, String jenaOutputFormat) {
	RDFServlet.setPrefixes(mod);
	// (from RDFServlet)
	String relativeURIsProp = "same-document, absolute, relative";
	// I wanted to write in the file a base different from the base I use to actually write the file
	// But there seems to be a bug in jena 2.4: when setting the "xmlbase" attribute
	// (that documents the xml base), the base arg in RDFWriter.write is not used as base
	// (the value of property xmlbase is used instead)
	// String thURI = SLServlet.getSLModel().getDefaultThesaurus().getBase(); // assure un / à la fin
	// rdfWriter.setProperty("xmlbase", Util.getContextURL(request) + "/tag/");
	// rdfWriter.setProperty("xmlbase", thURI);
	String xmlBase = null;
	
	String x = null;
	try {
		RDFWriterUtil.writeRDF(mod, out, xmlBase, jenaOutputFormat, relativeURIsProp, false);
	} catch (IOException e) { throw new RuntimeException(e); }
	return x;   	
}

}
