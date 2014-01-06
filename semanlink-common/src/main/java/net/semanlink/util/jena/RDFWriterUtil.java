/* Created on 15 mai 08 */
package net.semanlink.util.jena;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
public class RDFWriterUtil {
private RDFWriterUtil() {}
	
public static void writeRDF(Model model, File rdfFile, String xmlBase, String rdfKind, String relativeURIsProp) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(rdfFile));
    writeRDF(model, out, xmlBase, rdfKind, relativeURIsProp);
    out.close();
}

public static void writeRDF(Model model, File rdfFile, String xmlBase) throws IOException {
  writeRDF(model, rdfFile, xmlBase, null, null);
}

//
//
//

/** Use it as relativeURIsProp arg of this class methods to have a nice output. */
public static String relativeURIsNiceSetting() { return "same-document, absolute, relative"; }

/**
 * 
 * @param model
 * @param out
 * @param xmlBase
 * @param rdfKind "RDF/XML" (default) or "RDF/XML-ABBREV" or "N3"
 * @param relativeURIsProp cf property of RDFWriter. No set by default. Use relativeURIsNiceSetting() for a pretty output
 * @throws IOException
 */
public static void writeRDF(Model model, OutputStream out, String xmlBase, String rdfKind, String relativeURIsProp) throws IOException {
  writeRDF(model, out, xmlBase, rdfKind, relativeURIsProp, true);
}

public static void writeRDF(Model model, OutputStream out, String xmlBase, String rdfKind, String relativeURIsProp, boolean includeSomeUsualPrefixes) throws IOException {
	if (includeSomeUsualPrefixes) {
		// NAMESPACES DECLARATION FOR THE RDF OUTPUT
	  model.setNsPrefix("rdfs",RDFS.getURI());
	  model.setNsPrefix("dc", DC.getURI());
		// 2007-04 Jena 2.5.2
		// if I don't add following line,
		// I get something like this is the output ("DOCTYPE null"):
		/*
		<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE null [
		  <!ENTITY sl 'http://www.semanlink.net/2001/00/semanlink-schema#'>
		  <!ENTITY tag 'http://127.0.0.1:9080/semanlink/tag/'>]>
		<rdf:RDF
		    xmlns:sl="&sl;"
		    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
		    xmlns:dc="http://purl.org/dc/elements/1.1/"
		    xmlns:tag="&tag;" > 
		 */
		/*
		Jeremy Carroll wrote:
		I've reproduced this error, created a test case, and fixed the code 
		(essentially using your line below), so that this is now fixed in the 
		CVS copy. Your work-around is correct for Jena 2.5.2, and will remain 
		correct but unnecessary in later releases.			 */
	  model.setNsPrefix("rdf",RDF.getURI());
	}

	// There is a problem when we have a NS prefix whose prefix is ""
	// The writer writes things such as:
	/*
	<!DOCTYPE rdf:RDF [
	                   <!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>
	                   <!ENTITY  'http://www.example.com/schema#'>]>
	                 <rdf:RDF
	                     xmlns:rdf="&rdf;"
	                     xmlns="&;"
	*/
	// and that gives problems // Which ones? not a problem to report to Jena?
  if (!("N3".equals(rdfKind))) {
  	model.removeNsPrefix("");
  }
	
  //WRITING THE RDF FILE
  // using RDF/XML-ABBREV to have lists serialized as rdf:parseType="Collection" // ??
  if (rdfKind == null) rdfKind = "RDF/XML"; // "RDF/XML-ABBREV"
  RDFWriter rdfWriter = model.getWriter(rdfKind);
  
	if (relativeURIsProp != null) {
		rdfWriter.setProperty("relativeURIs",relativeURIsProp);
	}

  // Writing the xml header
  if (rdfKind.indexOf("XML") > -1) {
      boolean wantEncodingWritten = true;
      if (wantEncodingWritten) {
          rdfWriter.setProperty("showXmlDeclaration", "false"); // with true, we do not get the encoding written // and is it bad?
          String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
          out.write(xmlDeclaration.getBytes());
      } else {
          rdfWriter.setProperty("showXmlDeclaration", "true"); // with true, we do not get the encoding written // and is it bad?
      }
      rdfWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);

      // WE DO NOT USE base here: avoids having to use it when reading the file
      // BEN SI, ET IL FAUDRA
      if (xmlBase != null) rdfWriter.setProperty("xmlbase", xmlBase);     
  }
  rdfWriter.write (model, out, null);
  out.flush();
}





}
