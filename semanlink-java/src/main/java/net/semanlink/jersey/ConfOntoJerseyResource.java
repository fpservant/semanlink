/* Created on 2 nov. 2012 */
package net.semanlink.jersey;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.servlet.CoolUriServlet;
import net.semanlink.servlet.Jsp_Keyword;
import net.semanlink.servlet.RDFOutput;
import net.semanlink.servlet.RDFOutput_Keyword;
import net.semanlink.servlet.RDFServlet;
import net.semanlink.servlet.SLServlet;
import net.semanlink.servlet.SLUrisAsSkos;
import net.semanlink.util.jena.RDFWriterUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.jersey.api.view.Viewable;



// TODO 	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08

// TODO SysRIOT.wireIntoJena(); pour JSON/RDF

// TODO JSON-LD


@Path("configurationontology")
public class ConfOntoJerseyResource {


@GET
@Produces(Constants.MEDIA_TYPE_4_RDF_XML_STRING)
public StreamingOutput getRDF() {
  return new StreamingOutput() { 
    public void write(OutputStream out) {
    	try {
    		jenaModel().write(out,"RDF/XML-ABBREV");
    	} catch (IOException e) { throw new RuntimeException(e); }
    }
  };  	    	
}

@GET
@Produces(Constants.MEDIA_TYPE_4_TURTLE_STRING)
// public StreamingOutput getTTL() {
public Response getTTL() throws Exception {
//  return new StreamingOutput() { 
//    public void write(OutputStream out) {
//    	try {
//    		jenaModel().write(out,"TURTLE");
//    	} catch (IOException e) { throw new RuntimeException(e); }
//    }
//  };  
	File f = new File(SLServlet.getMainDataDir() + "/configurationontology/configurationontology.ttl");
	InputStream in = new BufferedInputStream(new FileInputStream(f));
	final CacheControl cc  = new CacheControl();
	cc.setMaxAge(3600);
	return Response.ok().entity(in).type(Constants.MEDIA_TYPE_4_TURTLE).cacheControl(cc).build();
}

@GET
@Produces(Constants.MEDIA_TYPE_4_HTML_STRING)
public Response getHTML() throws Exception {
	File f = new File(SLServlet.getMainDataDir() + "/configurationontology/configurationontology.html");
	InputStream in = new BufferedInputStream(new FileInputStream(f));
	final CacheControl cc  = new CacheControl();
	cc.setMaxAge(3600);
	return Response.ok().entity(in).type(MediaType.TEXT_HTML_TYPE).cacheControl(cc).build();
}

//
//
//

protected Model jenaModel() throws FileNotFoundException {
	Model x = ModelFactory.createDefaultModel();
	File f = new File(SLServlet.getMainDataDir() + "/configurationontology/configurationontology.ttl");
	InputStream in = new BufferedInputStream(new FileInputStream(f));
	x.read(in, null, "TURTLE");
	return x;
}

//// @jenaOutputFormat null "N3" or "RDF/JSON"
//public static String writeJenaModel(OutputStream out, Model mod, String jenaOutputFormat) {
//	RDFServlet.setPrefixes(mod);
//	// (from RDFServlet)
//	String relativeURIsProp = "same-document, absolute, relative";
//	// I wanted to write in the file a base different from the base I use to actually write the file
//	// But there seems to be a bug in jena 2.4: when setting the "xmlbase" attribute
//	// (that documents the xml base), the base arg in RDFWriter.write is not used as base
//	// (the value of property xmlbase is used instead)
//	// String thURI = SLServlet.getSLModel().getDefaultThesaurus().getBase(); // assure un / à la fin
//	// rdfWriter.setProperty("xmlbase", Util.getContextURL(request) + "/tag/");
//	// rdfWriter.setProperty("xmlbase", thURI);
//	String xmlBase = null;
//	
//	String x = null;
//	try {
//		RDFWriterUtil.writeRDF(mod, out, xmlBase, jenaOutputFormat, relativeURIsProp, false);
//	} catch (IOException e) { throw new RuntimeException(e); }
//	return x;   	
//}

}
