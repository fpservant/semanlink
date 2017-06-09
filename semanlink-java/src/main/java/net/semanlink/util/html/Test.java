/* Created on 9 mars 2004 */
package net.semanlink.util.html;
import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.semanlink.servlet.SLServlet;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 2 approches :
 * à partir du HTMLDocument chargé avec HTMLDocumentLoader,
 * ou via ParserCallback.
 * 
 * Avec le 1, je n'arrive pas à lire, par ex, le Title.
 */
public class Test {
public static void main(String[] args) {
	try {
		// String urlString = "file:///Users/fps/_fps/2004/02/BBC%20NEWS%20-%20Americas%20-%20Science%20wins%20ancient%20bones%20battle.html";
		String urlString = "http://www.lemonde.fr/proche-orient/article/2015/11/10/deux-nouvelles-attaques-au-couteau-a-jerusalem_4806619_3218.html";
		new Test(urlString);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

Test(String urlString) throws Exception {
	// test1(urlString); // javax.swing.text.ChangedCharSetException
	// test3(urlString);
	test4(urlString);
}
void test4(String urlString) throws Exception {
	URL url = new URL(urlString);
	Client simpleHttpClient = ClientBuilder.newClient();
	WebTarget webTarget = simpleHttpClient.target(urlString);
	Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
	boolean isHTML = false;
	if (res.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)) {
		isHTML = true;
	}
	System.out.println("isHTML: " + isHTML);
	assert(isHTML);
	HTMLPageDownload download = new HTMLPageDownload(url, res);
	download.save(new File("/Users/fps/space/trash/index.html"));
}

void test3(String urlString) throws Exception {
	URL url = new URL(urlString);
	Client simpleHttpClient = ClientBuilder.newClient();
  WebTarget webTarget = simpleHttpClient.target(urlString);
  Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
	HTMLDocumentLoader_Extended htmlLoader = new HTMLDocumentLoader_Extended(url, res);
	HTMLDocument htmlDocument = htmlLoader.loadDocument();
	// h23s(htmlDocument);
	// System.out.println("TEXT : \n" + htmlDocument.getText(0,htmlDocument.getLength()));
	System.out.println("LINKS : \n" + getLinks(htmlDocument));
	//System.out.println("REL");
	//getLinkTags(htmlDocument);

	System.out.println("TITLE : " + htmlLoader.getTitle());
	System.out.println("BASE : " + htmlDocument.getBase());
	
	//htmlDocument.
    File localDirectory = new File("/Users/fps/space/trash");
    /*while (remoteFileName.indexOf('/') > -1) {
       String part = remoteFileName.substring(0, remoteFileName.indexOf('/'));
       remoteFileName = remoteFileName.substring(remoteFileName.indexOf('/')+1);
       localDirectory = new File(localDirectory, part);
     }*/
    String remoteFileName = "index.html";
     //if (localDirectory.mkdirs()) {
       File output = new File(localDirectory, remoteFileName);
       FileWriter out = new FileWriter(output);
	htmlLoader.kit.write(out,  htmlDocument,  0,  htmlDocument.getLength());
	out.flush();
	out.close();
}

void test1(String urlString) throws Exception {
	URL url = new URL(urlString);
	
	// le texte, directement à partir de l'url, sans créer de HTMLDocument
	
	System.out.println("-------------------------------------");
	System.out.println(getText(urlString));
	System.out.println("-------------------------------------");	
	//
	
	Client simpleHttpClient = ClientBuilder.newClient();
  WebTarget webTarget = simpleHttpClient.target(urlString);
  Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
	HTMLDocumentLoader_Simple htmlLoader = new HTMLDocumentLoader_Simple(url, res);
	HTMLDocument htmlDocument = htmlLoader.loadDocument();

	// le texte, à partir du HTMLDocument
	
	System.out.println("-------------------------------------");
	System.out.println(htmlDocument.getText(0,htmlDocument.getLength()));
	System.out.println("-------------------------------------");
	
	System.out.println(getLinks(htmlDocument));
	System.out.println(getTitle(htmlDocument));
	
	/*Element el = doc.getDefaultRootElement();
	showElements(doc, el,"");*/
}

/**
 * from : http://javaalmanac.com/egs/javax.swing.text.html/GetLinks.html
 * (retourne les liens transformés en absolus)
 * @throws MalformedURLException
 */
public static String[] getLinks(HTMLDocument doc) throws MalformedURLException {
	URL base = doc.getBase();
	List result = new ArrayList();
	HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.HREF);
			if (link != null) {
				s.removeAttribute(HTML.Attribute.HREF);
				String absolut = (new URL(base, link)).toString();
				s.addAttribute(HTML.Attribute.HREF, absolut);
				// Add the link to the result list
				result.add(absolut);
				System.out.println(absolut);
				
			}
			it.next();
		}	
	}
	return (String[])result.toArray(new String[result.size()]);
}

public static String[] getLinkTags(HTMLDocument doc) throws MalformedURLException {
	URL base = doc.getBase();
	List result = new ArrayList();
	HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.LINK);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.REL);
			if (link != null) {
				s.removeAttribute(HTML.Attribute.REL);
				String absolut = (new URL(base, link)).toString();
				s.addAttribute(HTML.Attribute.REL, absolut);
				// Add the link to the result list
				result.add(absolut);
				System.out.println(absolut);
				
			}
			it.next();
		}	
	}
	return (String[])result.toArray(new String[result.size()]);
}


// ko
public String getTitle(HTMLDocument doc) {
	HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.TITLE);
	while (it.isValid()) {
	}
	return "";
}
/**
 * from : http://javaalmanac.com/egs/javax.swing.text.html/GetText.html
 */
public static String getText(String uriStr) throws Exception {
	final StringBuffer buf = new StringBuffer(1000);
	
		// Create an HTML document that appends all text to buf
		HTMLDocument doc = new HTMLDocument() {
			public HTMLEditorKit.ParserCallback getReader(int pos) {
				return new HTMLEditorKit.ParserCallback() {
					// This method is whenever text is encountered in the HTML file
					public void handleText(char[] data, int pos) {
						buf.append(data);
						buf.append('\n');
					}
				};
			}
		};
		
		// Create a reader on the HTML content
		URL url = new URI(uriStr).toURL();
		URLConnection conn = url.openConnection();
		Reader rd = new InputStreamReader(conn.getInputStream());
		
		// Parse the HTML
		EditorKit kit = new HTMLEditorKit();
		kit.read(rd, doc, 0);
	
	// Return the text
	return buf.toString();
}


public static String getTitle(String uriStr) throws Exception {
		final StringBuffer buf = new StringBuffer(1000);
		final StringBuffer buf1 = new StringBuffer(1000);
		final StringBuffer buf2 = new StringBuffer(1000);
	
		// Create an HTML document that appends all text to buf
		HTMLDocument doc = new HTMLDocument() {
			public HTMLEditorKit.ParserCallback getReader(int pos) {
				return new HTMLEditorKit.ParserCallback() {
					// This method is whenever text is encountered in the HTML file
					public void handleText(char[] data, int pos) {
						// if ((ititle1 != 0) && (ititle2 == 0)) buf.append(data);
						if ((buf1.length() != 0) && ((buf2.length() == 0))) buf.append(data);
						// buf.append('\n');
					}
					public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
						if (t.equals(HTML.Tag.TITLE)) {
							buf1.append("debut tire");
							return;
						}
					}
					/*// viré pour éviter un warning, mais je ne comprends pas bien ce que c'etait
					public void handleEndTag(HTML.Tag t, MutableAttributeSet a, int pos) {
						if (t.equals(HTML.Tag.TITLE)) {
							buf2.append("fin tire");
							return;
						}
					}*/
				};
			}
			
			
		};
		
		// Create a reader on the HTML content
		URL url = new URI(uriStr).toURL();
		URLConnection conn = url.openConnection();
		Reader rd = new InputStreamReader(conn.getInputStream());
		
		// Parse the HTML
		EditorKit kit = new HTMLEditorKit();
		kit.read(rd, doc, 0);
	
	// Return the text
	return buf.toString();
}




} // class
