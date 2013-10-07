package net.semanlink.util.html.using_url_connection;

import java.io.BufferedReader;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import net.semanlink.util.CopyFiles;

/**
 * Allow to download an HTML page and to save it, replacing relativ links with absolut
 * Special action on pages from www.lemonde.fr, in print format: remove advertising
 */
public class HTMLPageDownload extends HTMLDocumentLoaderNew {
private String contentType;
String fileContent;
HTMLDocument htmlDocument;

/** The constructor downloads the page. */
public HTMLPageDownload(URL url) throws IOException {
	this.url = url;
	this.htmlDocument = loadDocument(url);
}

public void save(File saveAsFile) throws IOException {
	this.fileContent = getNiceLinkedContent();
	
	// purgeFileContent(); 
	Writer out;
	// System.out.println("charSet: " + charSet);
	File dir = new File(saveAsFile.getParent());
	if (!dir.exists()) dir.mkdirs();
	if (this.charSet != null) {
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveAsFile), this.charSet));
		} catch (UnsupportedEncodingException e) {
			System.err.println("UnsupportedEncodingException saving " + saveAsFile);
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveAsFile)));	
		}
	} else {
		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveAsFile)));	
	}
	out.write(this.fileContent);
	out.flush();
	out.close();
}

/** Exactement la même que ds HTMLDocumentLoader, sauf que je mets le url et charset en attribut 
 *  de façon à pouvoir relire, et aussi : purge etc.
 */
public HTMLDocument loadDocument(HTMLDocument doc, URL url, String theCharSet) throws IOException {
	// System.out.println("loadDocument charSet : " + charSet);
	this.url = url;
	this.charSet = theCharSet;
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
		this.contentType = urlc.getContentType();
		in = urlc.getInputStream();
		
		// on n' pas mis les lignes plus ahut ds le try afin d'avoir l'IOException si on n'arrive pas à se conneter à l'url
		try {
			// Remove any document content
			doc.remove(0, doc.getLength());
			
			
			// on voudrait ne télécharger qu'une fois,  (donc, comme on a besoin
			// de le lire pour avoir le "fileContent brut", on voudrait enseuite
			// charger le htmldoc à partir du filecontent.
			// "mais pb du charset initialement non connu et de l'exception qui le remet bien
			// -> lecture ds filecontent (complete !), suivi du htmldoc 
			// avec exception puis relecture ds filecontent puis htmldoc qui marche
			// Donc, plutot : je fais d'abor htmldoc qui plnate suivie de filecontent complet
			//
			// Reader reader = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);	
			/*
			System.out.println("reading charset : " + this.charSet);
			this.reader = (charSet == null) ? new InputStreamReader(in) : new InputStreamReader(in, charSet);
			this.reader = new BufferedReader(this.reader);
			this.fileContent = new String(CopyFiles.reader2chars(this.reader));
			this.reader = new StringReader(fileContent);
			
		   	HTMLEditorKit.Parser parser = getParser();
		   	HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);

		   	parser.parse(reader, htmlReader, ignoreCharSet);
		   	htmlReader.flush();
		   	*/
			if (this.charSet == null) {
				// laissons planter vite avec chargement htmldoc d'abord
				this.reader = new BufferedReader(new InputStreamReader(in));
			   	HTMLEditorKit.Parser parser = getParser();
			   	HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);
			   	parser.parse(reader, htmlReader, ignoreCharSet);
			   	htmlReader.flush();
			   	// et si jamais ne plantait pas : // NOT TESTED 
			   	urlc = url.openConnection();
				in = urlc.getInputStream();			   	
				this.reader = new BufferedReader(new InputStreamReader(in));
				this.fileContent = new String(CopyFiles.reader2chars(this.reader));
				purgeFileContent();
			} else {
				try {
					this.reader = new BufferedReader(new InputStreamReader(in, this.charSet));
					this.fileContent = new String(CopyFiles.reader2chars(this.reader));
					purgeFileContent();
	
					this.reader = new StringReader(fileContent);
					
			   	HTMLEditorKit.Parser parser = getParser();
			   	HTMLEditorKit.ParserCallback htmlReader = getParserCallback(doc);

			   	parser.parse(reader, htmlReader, ignoreCharSet);
			   	htmlReader.flush();
				} catch (UnsupportedEncodingException e) {
					// cas où n'arrive pas à lire à cause d'un encoding inconnu
				  urlc = url.openConnection();
					in = urlc.getInputStream();			   	
					this.reader = new BufferedReader(new InputStreamReader(in));
					this.fileContent = new String(CopyFiles.reader2chars(this.reader));
					purgeFileContent();				
				}
			}
			
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
	 		System.out.println("HTMLPageDownload IOException " + e);
	 		return doc;  	
	 	}
	}
	return doc;
}


private String getNiceLinkedContent() throws MalformedURLException {
	URL base = this.htmlDocument.getBase();
	String x = this.fileContent;
	HTMLDocument.Iterator it;
	// liens href
	it = this.htmlDocument.getIterator(HTML.Tag.A);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.HREF);
			// System.out.println(link);
			if (link != null) {
				if (link.startsWith("#")) {
				/*
				else if (link.startsWith("javascript")) { //  ex href="javascript:dotruc()"
				} else if (link.indexOf("://") > -1) { // déjà absolue
				plutôt que les 2 lignes précédentes :*/
				} else if (link.indexOf(":") > -1) {
				} else {
					// transforme en absolue
					String absolut = (new URL(base, link)).toString();
					x = x.replaceAll("\"" + link + "\"", "\"" + absolut + "\"");
				}
			}
			it.next();
		}	
	}

	// images
	/* ca ne semble pas marcher, nullpo sur le SimpleAttributeSet
	it = this.htmlDocument.getIterator(HTML.Tag.IMG);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String src = (String) s.getAttribute(HTML.Attribute.SRC);
			// System.out.println(link);
			if (src != null) {
				if (src.indexOf(":") > -1) {
				} else {
					// transforme en absolue
					String absolut = (new URL(base, src)).toString();
					x = x.replaceAll("\"" + src + "\"", "\"" + absolut + "\"");
				}
			}
			it.next();
		}	
	}*/
	try {
		List al = getImages();
		for (int i = 0; i < al.size(); i++) {
			String imgSrc = (String) al.get(i);
			if (imgSrc.indexOf("://") > -1) {
			} else {
				// transforme en absolue
				String absolut = (new URL(base, imgSrc)).toString();
				x = x.replaceAll("\"" + imgSrc + "\"", "\"" + absolut + "\"");
			}
		}
	} catch (Exception e) {
		System.err.println("HTMLPageDownload: Exception caught trying to replace images : " + e);
	}

	// les liens vers css, ...
	/* Ceci devrait (?) marcher, mais ne marche pas
	it = this.htmlDocument.getIterator(HTML.Tag.LINK);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.HREF);
			System.out.println(s);
			if (link != null) {
				if (link.startsWith("#")) {
				} else if (link.indexOf("://") > -1) {
				} else {
					// transforme en absolue
					String absolut = (new URL(base, link)).toString();
					x = x.replaceAll("\"" + link + "\"", "\"" + absolut + "\"");
				}
			}
			it.next();
		}	
	}*/
	try {
		List al = getLinkHrefs();
		for (int i = 0; i < al.size(); i++) {
			String link = (String) al.get(i);
			if (link.startsWith("#")) {
			} else if (link.indexOf("://") > -1) {
			} else {
				// transforme en absolue
				String absolut = (new URL(base, link)).toString();
				x = x.replaceAll("\"" + link + "\"", "\"" + absolut + "\"");
			}
		}
	} catch (Exception e) { // une NullPointerException ds HTMLDocumentLoaderNew.getLinkHrefs avec 2 fichier avec un encoding inconnu
		System.err.println("HTMLPageDownload: Exception caught trying to replace links : " + e);
	}
	return x;
}

/** cf virer les pubs du monde */
void purgeFileContent() {
	if (isLeMondePrintPage(this.url.toString())) {
		for(;;) {
			int k1,k2;
			k1 = this.fileContent.indexOf("<script");
			if (k1 > -1) {
				k2 = this.fileContent.indexOf("</script>");
				// System.out.println(k1 + " / " + k2);
				// System.out.println(this.fileContent.substring(k1,k2+9));
				this.fileContent = this.fileContent.substring(0,k1) + this.fileContent.substring(k2+9);
			} else {
				break;
			}
		}
		for(;;) {
			int k1,k2;
			k1 = this.fileContent.indexOf("<SCRIPT");
			if (k1 > -1) {
				k2 = this.fileContent.indexOf("</SCRIPT>");
				// System.out.println(k1 + " / " + k2);
				this.fileContent = this.fileContent.substring(0,k1) + this.fileContent.substring(k2+9);
			} else {
				break;
			}
		}
	}
}

public void replaceTitle(String newTitle) {
	int k1,k2;
	k1 = this.fileContent.indexOf("<title>");
	if (k1 < 0) k1 = this.fileContent.indexOf("<TITLE>");
	if (k1 > -1) {
		k2 = this.fileContent.indexOf("</title>");
		if (k2 < 0) k2 = this.fileContent.indexOf("</TITLE>");
		if (k2 > -1) {
			this.fileContent = this.fileContent.substring(0,k1) 
				+ "<title>"
				+ newTitle
				+ this.fileContent.substring(k2);
		}
	}
}

public static boolean isLeMondePrintPage(String uri) {
	return (uri.startsWith("http://www.lemonde.fr/web/imprimer_element"));
}

public String getContentType() { return this.contentType; }
}
