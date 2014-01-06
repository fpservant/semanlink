package net.semanlink.util.html.using_url_connection;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * Reads the data located at a URL and loads an HTMLDocument.
 * Does more complex things than HTMLDocumentLoader_Simple during parsing,
 * such as listing links, images, ...
 */
public class HTMLDocumentLoaderNew extends HTMLDocumentLoader_Simple {
/** celui qui créée le HTMLDocument */
HTMLEditorKit.ParserCallback superParserCallback;
HTMLDocument theDoc;
Result result;
Object state;
// HTMLWriter htmlWriter;
protected URL url;
String charSet;
Reader reader;

/** Exactement la même que ds HTMLDocumentLoader, sauf que je mets le url et charset en attribut 
 *  de façon à pouvoir relire
 */
public HTMLDocument loadDocument(HTMLDocument doc, URL url, String charSet) throws IOException {
	this.url = url;
	this.charSet = charSet;
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
			// Reader reader = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);	
			this.reader = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);
			this.reader = new BufferedReader(this.reader);
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
			in.close();
			
			// Continue the loop to read with the correct encoding
	 	} catch (IOException e) { // cas où on ferme articiellement le reader ds le ParserCallback
	 		// afin de mettre fin aux opérations plus vite (cf ne lire que le titre)
	 		return doc;  	
	 	}
	}
	return doc;
}

static class Result {
	StringBuffer titre = new StringBuffer();
	ArrayList linkHrefsAl = new ArrayList(); // pour les tag link genre css
	ArrayList imagesAl = new ArrayList(); // pour les src des images
}
public String getTitle() { return this.result.titre.toString(); }
public List getLinkHrefs() { return this.result.linkHrefsAl; }
/** liste des srcs des images */
public List getImages() { return this.result.imagesAl; }

// Methods that allow customization of the parser and the callback
public synchronized HTMLEditorKit.ParserCallback getParserCallback(HTMLDocument doc) {
	this.theDoc = doc;
	this.result = new Result();
	// return doc.getReader(0);
	superParserCallback = super.getParserCallback(doc);
	// htmlWriter = new HTMLWriter(new PrintWriter(System.out),doc);
    HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback () {
    		/*public void flush() throws BadLocationException {
    			superParserCallback.flush();
    		}
    		public void handleComment(char[] data, int pos) {
    			superParserCallback.handleComment(data, pos);
    		}
    		public void handleError(String errorMsg, int pos) {
    			System.out.println(errorMsg);
    			superParserCallback.handleError(errorMsg, pos);
    		}
    		public void handleEndOfLineString(String eol) {
    			superParserCallback.handleEndOfLineString(eol);
    		}*/
	    	public void handleText(char[] data, int pos) {
					superParserCallback.handleText(data, pos);
					// System.out.println("handleText "+ pos + " : ");
					// System.out.println(data);
					 if (HTML.Tag.TITLE.equals(state)) {
					 	result.titre.append(data);
					 /*} else if (HTML.Tag.TITLE.equals(state)) {
					System.out.println("LINK : " + data);*/
					 }
	    	}

	    	public void handleSimpleTag(HTML.Tag t,  MutableAttributeSet a,  int pos) {
	      	superParserCallback.handleSimpleTag(t, a, pos);
	      	if (t.equals(HTML.Tag.LINK)) {
						// System.out.println("handleSimpleTag LINK" + a);
						String link = (String) a.getAttribute(HTML.Attribute.HREF);
						if (link != null) {
							// System.out.println("\t" + link);
							result.linkHrefsAl.add(link);
						}
	      	} else if (t.equals(HTML.Tag.IMG)) {
						// System.out.println("handleSimpleTag IMG" + a);
						String img = (String) a.getAttribute(HTML.Attribute.SRC);
						if (img != null) {
							// System.out.println("\t" + img);
							result.imagesAl.add(img);
						}
	      	}
	    	}
	    	
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
           	superParserCallback.handleStartTag(t, a, pos);
            // System.out.println("handleStartTag " + pos + " : " + t);
		        if (t.equals(HTML.Tag.TITLE)) {
							// System.out.println("TITRE DEBUT");
							state = HTML.Tag.TITLE;
				      // si on veut arrêter après le titre :
					    /*} else if (t.equals(HTML.Tag.BODY)) {
			            		if (result.titre.length() > 0) { // parce qu'il y a les tentatives infructueuses du début vaec le pb charset
			            			reader.close(); // génère une IOException ds parser.parse
			            		}
			         */
             }
        }
        public void handleEndTag(HTML.Tag t, int pos) { 
        		superParserCallback.handleEndTag(t, pos);
             // System.out.println("handleEndTag " + pos + " : " + t);
             if (t.equals(HTML.Tag.TITLE)) {
             	state = null;
              }
    		}
    };
    return callback;
}

} // class
