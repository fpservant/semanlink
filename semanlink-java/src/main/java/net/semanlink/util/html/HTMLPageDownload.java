package net.semanlink.util.html;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.semanlink.util.SimpleHttpClient;

/**
 * Allow to download an HTML page and to save it, replacing relativ links with absolut
 * Special action on pages from www.lemonde.fr, in print format: remove advertising
 */
public class HTMLPageDownload extends HTMLDocumentLoader_Extended {
HTMLDocument htmlDocument;
private String fileContent;

/** The constructor downloads the page. */
public HTMLPageDownload(SimpleHttpClient client, URL url) throws IOException {
	super(client);
	this.url = url;
	this.htmlDocument = loadDocument(url);
	this.fileContent = getNiceLinkedContent();
	purgeFileContent(); 
}

public void save(File saveAsFile) throws IOException {	
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
	out.write(fileContent);
	out.flush();
	out.close();
}

private String getNiceLinkedContent() throws MalformedURLException, UnsupportedEncodingException {
	URL base = this.htmlDocument.getBase();
	String x = new String(this.content, this.charSet);
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
				} else if (link.indexOf("://") > -1) { // d�j� absolue
				plut�t que les 2 lignes pr�c�dentes :*/
				} else if (link.indexOf(":") > -1) {
				} else {
					// transforme en absolue
					String absolut = (new URL(base, link)).toString();
					x = x.replace("\"" + link + "\"", "\"" + absolut + "\"");
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
					x = x.replace("\"" + src + "\"", "\"" + absolut + "\"");
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
				x = x.replace("\"" + imgSrc + "\"", "\"" + absolut + "\"");
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
					x = x.replace("\"" + link + "\"", "\"" + absolut + "\"");
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
				x = x.replace("\"" + link + "\"", "\"" + absolut + "\"");
			}
		}
	} catch (Exception e) { // une NullPointerException ds HTMLDocumentLoaderNew.getLinkHrefs avec 2 fichier avec un encoding inconnu
		System.err.println("HTMLPageDownload: Exception caught trying to replace links : " + e);
	}
	return x;
}

/** cf virer les pubs du monde */
private void purgeFileContent() {
	if (isLeMondePrintPage(this.url.toString())) {
		for(;;) {
			int k1,k2;
			k1 = fileContent.indexOf("<script");
			if (k1 > -1) {
				k2 = fileContent.indexOf("</script>");
				// System.out.println(k1 + " / " + k2);
				// System.out.println(fileContent.substring(k1,k2+9));
				fileContent = fileContent.substring(0,k1) + fileContent.substring(k2+9);
			} else {
				break;
			}
		}
		for(;;) {
			int k1,k2;
			k1 = fileContent.indexOf("<SCRIPT");
			if (k1 > -1) {
				k2 = fileContent.indexOf("</SCRIPT>");
				// System.out.println(k1 + " / " + k2);
				fileContent = fileContent.substring(0,k1) + fileContent.substring(k2+9);
			} else {
				break;
			}
		}
	}
}

public void replaceTitle(String newTitle) {
	int k1,k2;
	k1 = fileContent.indexOf("<title>");
	if (k1 < 0) k1 = fileContent.indexOf("<TITLE>");
	if (k1 > -1) {
		k2 = fileContent.indexOf("</title>");
		if (k2 < 0) k2 = fileContent.indexOf("</TITLE>");
		if (k2 > -1) {
			fileContent = fileContent.substring(0,k1) 
				+ "<title>"
				+ newTitle
				+ fileContent.substring(k2);
		}
	}
}

public static boolean isLeMondePrintPage(String uri) {
	return (uri.startsWith("http://www.lemonde.fr/web/imprimer_element"));
}

}
