package net.semanlink.util.html;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

/**
 * @author fps
 */
public class HTMLUtil {
/** Retourne le fragment de HTML compris entre start et end (sans html, body). 
 *  ATTENTION, suppose que le fragment est situé juste sous body ds htmlDocument. */
public static String getHtmlFragment(HTMLDocument htmlDocument, int start, int end) throws IOException, BadLocationException {
	Writer writer = new StringWriter();
	HTMLWriter htmlWriter = new HTMLWriter(writer, htmlDocument, start, end-start);
	htmlWriter.write(); // ecrit un fichier html
	writer.flush();
	String x = writer.toString();
	
	int deb = x.indexOf("<body");
	int fin = x.indexOf("</body>");
	if (fin < deb) fin = x.length();
	x = x.substring(deb,fin);
	deb = x.indexOf(">");
	x = x.substring(deb+1);
	return x;
}

public static String getAttribute(javax.swing.text.Element elem, String name) {
	AttributeSet attributeSet = elem.getAttributes();
	Enumeration nue = attributeSet.getAttributeNames();
	for (;nue.hasMoreElements() ;) {
		Object na = nue.nextElement();
		if (name.equals(na.toString())) {
			return (String) attributeSet.getAttribute(na);
		}
	}
	return null;
}

/**
 * Retourne l'ensemble des liens vers d'autres fichiers, transformés en url absolues. 
 * Modifie aussi ces liens au sein du HTMLDocument
 * 
 * Les urls vers le même fichier ("#xxx") ne sont pas comprises (la méthode prend soin de les éliminer).
 * d'après : http://javaalmanac.com/egs/javax.swing.text.html/GetLinks.html
 * @throws MalformedURLException
 */
public static String[] getHREFs(HTMLDocument doc) throws MalformedURLException {
	URL base = doc.getBase();
	List result = new ArrayList();
	HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.HREF);
			if (link != null) {
				if (link.startsWith("#")) {
				} else {
					if (link.indexOf("://") < 0) { // si pas déjà absolue
						// transforme en absolue
						link = (new URL(base, link)).toString();
						// modifie le HTMLDocument
						s.removeAttribute(HTML.Attribute.HREF);
						s.addAttribute(HTML.Attribute.HREF, link);
					}
					// Add the link to the result list
					result.add(link);
				}
			}
			it.next();
		}	
	}
	return (String[])result.toArray(new String[result.size()]);
}

/** cf les liens vers css */
/*public static String[] getRELs(HTMLDocument doc) throws MalformedURLException {
	URL base = doc.getBase();
	List result = new ArrayList();
	HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.LINK);
	if (it != null) {
		while (it.isValid()) {
			SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
			String link = (String) s.getAttribute(HTML.Attribute.REL);
			if (link != null) {
				String absolut = (new URL(base, link)).toString();
				s.removeAttribute(HTML.Attribute.REL);
				s.addAttribute(HTML.Attribute.REL, absolut);
				// Add the link to the result list
				result.add(absolut);
				System.out.println(absolut);
				
			}
			it.next();
		}	
	}
	return (String[])result.toArray(new String[result.size()]);
}*/


}
