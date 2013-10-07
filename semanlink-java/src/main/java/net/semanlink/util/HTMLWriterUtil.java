package net.semanlink.util;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Utilitaires pour l'écriture de HTML
 */
public class HTMLWriterUtil {
static public void writeMeta(Writer out, String name, String content) throws IOException {
	out.write("<meta name=\"");
	out.write(name);
	out.write("\" content=\"");
	out.write(content);
	out.write("\" />\n");
}

static public void writeDiv(Writer out, String className, String content) {
	try {
		if (content == null) return;
		content = content.trim();
		if ("".equals(content)) return;
		StringBuffer sb = new StringBuffer();
		sb.append("<div class=\"");
		sb.append(className);
		sb.append("\">");
		String niceContent;
		try {
			niceContent = niceContent(content);
		} catch (Exception e) { niceContent = content; } // oui oui
		if (content != niceContent) {
			// contenu sur une seule ligne : mettre <div>niceContent</div> sur une seule ligne
			sb.append(niceContent);
		} else {
			// contenu sur plusieurs lignes : 
			sb.append("\n");
			sb.append(content);
			sb.append("\n");
		}
		sb.append("</div>\n");
		out.write(sb.toString());
	} catch(Exception e) {
		e.printStackTrace();
	}
}

/** Si s a une seule ligne, retourne cette 1ere ligne (sans car de fin de ligne)
 * sinon, retourne s elle même */
public static String niceContent(String s) throws IOException {
	if (s == null) return null;
	StringReader sr = new StringReader(s);
	LineNumberReader lr = new LineNumberReader(sr);
	String firstLine = lr.readLine();
	if (lr.readLine() == null) {
		// content a une seule ligne
		return firstLine.trim();
	} else {
		return s;
	}
}
} // class