/* Created on Mar 24, 2020 */
package net.semanlink.arxiv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.semanlink.servlet.SLServlet;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import javax.xml.parsers.*;

// import javax.xml.Parsers.DocumentBuilder;

public class ArxivTest {

public static String convertStreamToString(java.io.InputStream is) { // java sucks
////how to avoid the false-positive warning in eclipse:
//try (Scanner s = new Scanner(is)) {
//	try (Scanner sames = s.useDelimiter("\\A")) {
//		return sames.hasNext() ? sames.next() : "";
//	}
//}

	try (@SuppressWarnings("resource")
	Scanner s = new Scanner(is).useDelimiter("\\A")) {
		return s.hasNext() ? s.next() : "";
	}
}

@Test
public final void test() throws JAXBException, IOException, ParserConfigurationException, SAXException {
	// Client client = SLServlet.getSimpleHttpClient();
	Client client = ClientBuilder.newClient();
	String uri = "http://export.arxiv.org/api/query?id_list=2003.02320";
	WebTarget webTarget = client.target(uri);
	// Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
	InputStream in = webTarget.request(MediaType.WILDCARD_TYPE).get(InputStream.class);
	// System.out.println(convertStreamToString(in));
	
	
//	// http://blog.bdoughan.com/2010/09/processing-atom-feeds-with-jaxb.html
//	// https://stackoverflow.com/questions/11463231/how-to-generate-jaxb-classes-from-xsd
//	// JAXBContext jc = JAXBContext.newInstance("org.w3._2005.atom");
//	JAXBContext jc = JAXBContext.newInstance();
//  // Marshaller marshaller = jc.createMarshaller();
//	Unmarshaller unmarshaller = jc.createUnmarshaller();
//  JAXBElement<JAXBElement> feed = unmarshaller.unmarshal(new StreamSource(in), JAXBElement.class);
//  in.close();
//
//  System.out.println(feed.getName() + " : " + feed.getValue());
	
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
  DocumentBuilder builder;  

  	// factory.setFeature(Constants.DISALLOW_DOCTYPE_DECLARATION,true);
      builder = factory.newDocumentBuilder();  
      Document doc = builder.parse( in );     
      
      NodeList children = doc.getChildNodes();

}

}
