/* Created on 29 mars 2005 */
package net.semanlink.metadataextraction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.text.html.HTMLDocument;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.SimpleHttpClient;
import net.semanlink.util.html.HTMLDocumentLoader_Simple;

public class ExtractorData {
private SLDocument slDoc;
private SLModel mod;
private HTMLDocument htmlDocument;
private String text;
/** peut être setté pour transporter une info supplémentaire. */
private Object data;
public ExtractorData(SLDocument slDoc, SLModel mod, SimpleHttpClient simpleHttpClient) {
	this.slDoc = slDoc;
	this.mod = mod;
	
	try { // TODO : si ce n'est pas un doc html !!! (comment : ds Action_Bookmark ? mais si création à partir de doc ds un folder ?

		String uri = slDoc.getURI();
		String contentType = simpleHttpClient.getContentType(uri, false);
		if (contentType == null) {
			if ((uri.endsWith(".html"))||(uri.endsWith(".htm"))) contentType = "text/html";
		}
		if ((contentType != null) && (contentType.indexOf("text/html") > -1)) {
			this.htmlDocument = (new HTMLDocumentLoader_Simple(simpleHttpClient)).loadDocument(new URL(uri));
		}
	} catch (MalformedURLException e) { // TODO
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public Object getData() { return this.data; }
public void setData(Object data) { this.data = data; }

public String getUri() { return this.slDoc.getURI(); }
public SLDocument getSLDocument() { return this.slDoc; }
public SLModel getSLModel() { return this.mod; }
public HTMLDocument getHTMLDocument() { return this.htmlDocument; }
public String getText() {
	try {
		if (this.text == null) {
			if (this.htmlDocument != null) {
				this.text = this.htmlDocument.getText(0,htmlDocument.getLength());
			}
		}
	} catch (Exception e) { e.printStackTrace(); }
	return this.text;
}
/*
String getText4kwExtraction() throws Exception {
	return Util.getWithoutExtension(this.slDoc.getLabel());
}
*/
}
