package net.semanlink.util.html.using_url_connection;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

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
	public HTMLDocument loadDocument(URL url) throws IOException {
		return loadDocument(url, null);
	}

	public HTMLDocument loadDocument(URL url, String charSet) throws IOException {
		return loadDocument((HTMLDocument)kit.createDefaultDocument(), url, charSet);
	}

	public HTMLDocument loadDocument(HTMLDocument doc, URL url, String charSet) throws IOException {
		doc.putProperty(Document.StreamDescriptionProperty, url);	
		/*
		 * This loop allows the document read to be retried if
		 * the character encoding changes during processing.
		 */
		InputStream in = null;
		boolean ignoreCharSet = false;
		
		for (;;) {
				
			URLConnection urlc = url.openConnection();
			// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia
			urlc.setRequestProperty("User-Agent",""); 
			in = urlc.getInputStream();
			try {
				// Remove any document content
				doc.remove(0, doc.getLength());
				Reader reader = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);				   	
				reader = new BufferedReader(reader);
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
				charSet = getNewCharSet(e);
				
				// Prevent recursion by suppressing further exceptions
				ignoreCharSet = true;
				
				// Close original input stream
				in.close();
				
				// Continue the loop to read with the correct encoding
			}
		}
		return doc;
	}
	
		// Methods that allow customization of the parser and the callback
	
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

} // class
