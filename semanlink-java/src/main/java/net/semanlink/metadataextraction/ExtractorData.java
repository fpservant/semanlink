/* Created on 29 mars 2005 */
package net.semanlink.metadataextraction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.text.html.HTMLDocument;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.util.html.HTMLDocumentLoader_Simple;
import net.semanlink.util.html.HTMLPageDownload;

public class ExtractorData {
private SLDocument slDoc;
private SLModel mod;
private HTMLDocument htmlDocument;
private String text;
/** peut être setté pour transporter une info supplémentaire. */
private Object data;
public ExtractorData(SLDocument slDoc, SLModel mod, Client simpleHttpClient) {
	this.slDoc = slDoc;
	this.mod = mod;
	
	try { // TODO : si ce n'est pas un doc html !!! (comment : ds Action_Bookmark ? mais si création à partir de doc ds un folder ?

		String uri = slDoc.getURI();

		WebTarget webTarget = simpleHttpClient.target(uri);
		Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
		boolean isHTML = false;
		if (res.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)) {
			isHTML = true;
		} else {
			if ((uri.endsWith(".html"))||(uri.endsWith(".htm"))) isHTML = true;
		}

		if (isHTML) {
			this.htmlDocument = (new HTMLDocumentLoader_Simple(new URL(uri), res).loadDocument());
		}
	} catch (MalformedURLException e) { // TODO
		System.err.println(e);
	} catch (UnknownHostException e) { // TODO cf quand on n'arrive pas à se connecter
		System.err.println(e);
	} catch (IOException e) {
		System.err.println(e);
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
