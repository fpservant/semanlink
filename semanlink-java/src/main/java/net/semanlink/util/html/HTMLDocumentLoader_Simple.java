package net.semanlink.util.html;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import net.semanlink.util.SimpleHttpClient;

/**
 * Reads the data located at a URL and loads a javax.swing.text.html.HTMLDocument. 
 * Handles a problem of "ChangedCharSetException" that we get with the javax.swing.text classes.
 * This is a simple version, because it uses the default HTMLEditorKit.Parser
 * and HTMLEditorKit.ParserCallback. This doesn't allow to do much thing. For more
 * complex things (listing of images, links, ...), use HTMLDocumentLoader
 */
public class HTMLDocumentLoader_Simple {
protected static HTMLEditorKit kit;	
protected static HTMLEditorKit.Parser parser;
static { kit = new HTMLEditorKit(); }
//
/** in a first version, we were using an URLConnction to get content.
 *  We use now an HttpClient
 */
protected SimpleHttpClient httpClient;
/** with the httpClient, it seems that we have to read all the content
 * on the stream and have it in a buffer, before doing the parsing
 * (should be possible to do otherwise, but i'm not sure how).
 * Not that bad: because of the problem of the charset exception,
 * we frequently have to read again : let's keep the bytes
 * read in a buffer
 */
protected byte[] content;
protected URL url;
protected String charSet;
protected String contentType;

// protected Reader reader;

/** in a first version, we were using an URLConnction to get content.
 *  We use now an HttpClient
 */
public HTMLDocumentLoader_Simple(SimpleHttpClient httpClient) {
	this.httpClient = httpClient;
}
//
public HTMLDocument loadDocument(URL url) throws IOException {
	// System.out.println("HTMLDocumentLoader_Simple.loadDocument " + url);
	return loadDocument(url, null);
}

public HTMLDocument loadDocument(URL url, String charSet) throws IOException {
	return loadDocument((HTMLDocument)kit.createDefaultDocument(), url, charSet);
}

public HTMLDocument loadDocument(HTMLDocument doc, URL url, String charSet) throws IOException {
	this.url = url;
	this.charSet = charSet;
	this.content = null; // init : important if we use this several times (for several urls)
	doc.putProperty(Document.StreamDescriptionProperty, url);	
	/*
	 * This loop allows to retry parsing the doc if
	 * the character encoding changes during processing.
	 */
	boolean ignoreCharSet = false;
	
	for (;;) {
		Reader reader = getReader(url);
		try {
			// Remove any document content
			doc.remove(0, doc.getLength());
			
	   	HTMLEditorKit.Parser parser = getParser();
	   	HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);

	   	parser.parse(reader, htmlReader, ignoreCharSet);
	   	htmlReader.flush();
	   	// All done
	   	break;
		} catch (BadLocationException ex) {
			// Should not happen - throw an IOException
			throw new IOException(ex.getMessage());
		} catch (ChangedCharSetException e) {
			// The character set has changed - restart
			this.charSet = getNewCharSet(e);
			
			// Prevent recursion by suppressing further exceptions
			ignoreCharSet = true;
			
			// Close original input stream
			reader.close();
			
			// Continue the loop to read with the correct encoding
	 	} catch (IOException e) { // cas o� on ferme articiellement le reader ds le ParserCallback
	 		// afin de mettre fin aux op�rations plus vite (cf ne lire que le titre)
	 		return doc;  	
	 	}
	}
	return doc;
}

/** Returns the Reader to be passed to parser.parse. */
protected Reader getReader(URL url) throws IOException {
	/* when we were using a simple URLConnection
	URLConnection urlc = url.openConnection();
	// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia
	urlc.setRequestProperty("User-Agent",""); 
	InputStream in = urlc.getInputStream();
	*/
	InputStream in = null;
	String surl = url.toString();
	if (surl.startsWith("http://")) {
		if (this.content == null) {
			// url not read yet
			
			// Pb with www.dannyayers.com that (badly ?) returns a 404 with this:
			// SimpleHttpClient.Response res =  this.httpClient.doGet(surl, "text/html");
			// This should be better (precedence is : text/html then text/* then */*)
			// (see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for information about http headers)
			SimpleHttpClient.Response res =  this.httpClient.doGet(surl, "text/html, text/*, */*");
			this.content = res.getResponseBody();
	    this.charSet = res.getCharSet();
	    this.contentType = res.getContentType();
		}
		in = new ByteArrayInputStream(this.content);
	} else { // not an http url
		URLConnection urlc = url.openConnection();
		// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia // doesn't happen anymore: not an http url. Anyway:
		urlc.setRequestProperty("User-Agent","");
		in = urlc.getInputStream();
	}
	Reader x = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);
	return new BufferedReader(x);
}

//
// Methods that allow customization of the parser and the callback
//

public synchronized HTMLEditorKit.Parser getParser() {
	if (parser == null) {
		try {
			Class c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
			parser = (HTMLEditorKit.Parser)c.newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	return parser;
}	

public synchronized HTMLEditorKit.ParserCallback getParserCallback(HTMLDocument doc) {
	return doc.getReader(0);
}

//
//
//

protected String getNewCharSet(ChangedCharSetException e) {
	String spec = e.getCharSetSpec();
	if (e.keyEqualsCharSet()) {
		// The event contains the new CharSet
		return spec;
	} 
	
	// The event contains the content type
	// plus ";" plus qualifiers which may
	// contain a "charset" directive. First
	// remove the content type.
	int index = spec.indexOf(";");
	if (index != -1) {
		spec = spec.substring(index + 1);
	}
	
	// Force the string to lower case
	spec = spec.toLowerCase();

	StringTokenizer st = new StringTokenizer(spec, " \t=", true);
	boolean foundCharSet = false;
	boolean foundEquals = false;
	while (st.hasMoreTokens()) {
		String token = st.nextToken();
		if (token.equals(" ") || token.equals("\t")) {
			continue;
		}
		if (foundCharSet == false && 
				foundEquals == false &&
				token.equals("charset")) {
			foundCharSet = true;
			continue;
		} else if (foundEquals == false && 
				token.equals("=")) {
			foundEquals = true;
			continue;
		} else if (foundEquals == true &&
				foundCharSet == true) {
			return token;
		}

		// Not recognized
		foundCharSet = false;
		foundEquals = false;
	}

	// No charset found - return a guess
	return "8859_1";
}

public String getContentType() { return this.contentType; }

} // class
