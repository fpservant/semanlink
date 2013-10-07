/* Created on 27 avr. 2005 */
package net.semanlink.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.lod.JsonLDSerializer;
import net.semanlink.skos.SKOS;
import net.semanlink.util.AcceptHeader;
import net.semanlink.util.jena.RDFWriterUtil;
import net.semanlink.util.jsonld.JsonLDSerializerImpl;

import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/** This servlet writes RDF to the outputStream. 
 *  The RDF returned is contained in a model passed as a request attribute. 
 *  The rdf lang (n3, etc.) is passed as either a requet param or attribute
 * @author fps
 */
public class RDFServlet extends HttpServlet {
private JsonLDSerializer jsonRDFSerializer;
// protected static boolean rdfJsonTalisInited = false;

public void init() throws ServletException {
	SysRIOT.wireIntoJena();
	jsonRDFSerializer = new JsonLDSerializerImpl(true);
}

public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	doIt(request, response);
}
public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	doIt(request, response);
}

public static void setPrefixes(Model rdfMod) {
	rdfMod.setNsPrefix("rdf",RDF.getURI());				
	rdfMod.setNsPrefix("rdfs",RDFS.getURI());
	rdfMod.setNsPrefix("dc", DC.getURI());
	rdfMod.setNsPrefix("foaf", FOAF.getURI());
	rdfMod.setNsPrefix("skos", SKOS.getURI());
	rdfMod.setNsPrefix("sl","http://www.semanlink.net/2001/00/semanlink-schema#");
	// this doesn't work:
	// rdfMod.setNsPrefix("th",Util.getContextURL(request) + "/");
	// ok:
	// rdfMod.setNsPrefix("th",Util.getContextURL(request) + "/tag/"); // doesn't work with just Util.getContextURL(request) + "/"
	String thURI = SLServlet.getSLModel().getDefaultThesaurus().getBase(); // slash terminated
	rdfMod.setNsPrefix("tag",thURI);	
}

private void doIt(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	Model rdfMod = (Model) request.getAttribute("net.semanlink.servlet.rdf");

	String as = (String) request.getAttribute("net.semanlink.servlet.rdf.lang");
	if (as == null) as = request.getParameter("as");
	if ((as == null)||("".equals(as.trim()))) {
		as = "RDF/XML";
	} else {
		as = as.toUpperCase();
	}
	
	boolean isN3 = ("N3".equals(as));
	boolean isJSONLD = (("JSON".equals(as)) || ("JSONLD".equals(as)));
	boolean isTalisRDFJson = (("RJ".equals(as)) || ("RDF/JSON".equals(as)));
			
	String contentType;
	String jenaRDFKind = null;
	if (isN3) {
		contentType = AcceptHeader.RDF_N3;
		jenaRDFKind = "N3";
	} else if (isJSONLD) { // 2012-08 JSON-LD
		contentType = AcceptHeader.JSON_LD;		
	} else if (isTalisRDFJson) { // 2012-08 RDF/JSON TALIS
		contentType = AcceptHeader.RDF_JSON_TALIS;		
		jenaRDFKind = "RDF/JSON";
//		if (!rdfJsonTalisInited) {
//			SysRIOT.wireIntoJena();
//			rdfJsonTalisInited = true;
//		}
	} else {
		contentType = "application/rdf+xml; charset=UTF-8";
	}
	response.setContentType(contentType);
	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 2012-08
	OutputStream out = response.getOutputStream();

	if (isJSONLD) { // 2012-08 JSON-LD
		jsonRDFSerializer.rdf2jsonld(rdfMod, out);

	} else {
		setPrefixes(rdfMod);
		String relativeURIsProp = "same-document, absolute, relative";
		// I wanted to write in the file a base different from the base I use to actually write the file
		// But there seems to be a bug in jena 2.4: when setting the "xmlbase" attribute
		// (that documents the xml base), the base arg in RDFWriter.write is not used as base
		// (the value of property xmlbase is used instead)
		// String thURI = SLServlet.getSLModel().getDefaultThesaurus().getBase(); // assure un / à la fin
		// rdfWriter.setProperty("xmlbase", Util.getContextURL(request) + "/tag/");
		// rdfWriter.setProperty("xmlbase", thURI);
		String xmlBase = null;
		RDFWriterUtil.writeRDF(rdfMod, out, xmlBase, jenaRDFKind, relativeURIsProp, false);
	}
	out.flush();
}

} // class
