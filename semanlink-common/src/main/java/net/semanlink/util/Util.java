package net.semanlink.util;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Des methodes statiques diverses.
 */
public class Util {

//
// FILES
//

public static String getLastItem(String s, char separator) {
	return s.substring(s.lastIndexOf(separator) + 1);
}

/** Retourne l'extension d'un nom de fichier.
 *  (attention, retourne "" s'il ne contient pas de ".")
 */
public static String getExtension(String filename) {
	int n = filename.lastIndexOf('.');
	if (n < 0) return "";
	return filename.substring(n + 1);
}
/** Retourne "." l'extension d'un nom de fichier (par ex ".html")
 *  (attention, retourne "" s'il ne contient pas de ".")
 */
public static String getDotExtension(String filename) {
	int n = filename.lastIndexOf('.');
	if (n < 0) return "";
	return filename.substring(n);
}

/** Retourne le nom prive de son eventuelle extension */
public static String getWithoutExtension(String filename) {
	/*int n = filename.lastIndexOf('.');
	if (n < 0) return filename;
	return filename.substring(0, n);*/
	return getWithoutLastItem(filename,'.');
}
/** Retourne le nom prive de son eventuel dernier item (supprime aussi separator) */
public static String getWithoutLastItem(String s, char separator) {
	int n = s.lastIndexOf(separator);
	if (n < 0) return s;
	return s.substring(0, n);
}

public static String getLocalName(String httpUri) {
	String x = getLastItem(httpUri, '/');
	int n = x.lastIndexOf('#');
	if (n < 0) return x;
	return x.substring(n + 1);
}

public static boolean isImage(String uri) {
	String extension = Util.getExtension(uri);
	extension = extension.toUpperCase();
	return (
		("JPG".equals(extension))
		|| ("JPEG".equals(extension))
		|| ("PNG".equals(extension))
		|| ("GIF".equals(extension))
	);
}

public static boolean isDirectory(String uri) {
	// CODE DE FAINEANT
	if (!(uri.startsWith("file:"))) return false;
	return uri.endsWith("/");
}

/** Delete a directory and all files it contains.
(java.io.File.delete() only works with an empty directory). 
true iff sucessfully deleted */
public static boolean deleteDir(File dir) throws SecurityException {
	return CopyFiles.deleteFolder(dir);
}

/** Delete all files inside a directory. */
public static void emptyDir(File dir) throws SecurityException {
	String[] liste;
	String name;
	File f;
	liste = dir.list();
	int n = liste.length;
	for (int i = 0; i<n; i++) {
		name = liste[i];
		f = new File(dir,name);
		if (f.isDirectory()) {
			deleteDir(f);
		} else {
			f.delete();
		}
	}
}

//
// DATES
//


public static long shortDate2Long(String date, Locale locale) throws ParseException {
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
	return dateFormat.parse(date).getTime();
}

// DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
// String date = dateFormat.format(new Date());

//
// UTILS DEBUG
//

public static void printParams(HttpServletRequest request) {
	for (Enumeration<String> e = request.getParameterNames();e.hasMoreElements();) {
		String param = e.nextElement();
		String val = request.getParameter(param);
		System.out.println(param + ": " + val);
	}
}

//
// Strings
//

public static String getFirstLine(String s) throws IOException {
	if (s == null) return null;
	StringReader sr = new StringReader(s);
	LineNumberReader lr = new LineNumberReader(sr);
	return lr.readLine();
}

//
// servlets
//

/**
 * Context URL
 * for instance http://127.0.0.1:8080/semanlink or http://www.semanlink.net
 */
public static String getContextURL(HttpServletRequest request) throws MalformedURLException {
	// utilisait la ligne suivante, mais HttpUtils est deprecated.
	// return new URL( new URL(HttpUtils.getRequestURL(request).toString()), request.getContextPath());
	// chose curieuse, HttpUtils.getRequestURL(request) et request.getRequestURL() ne retournent
	// pas la même chose :
	// request.getRequestURL() http://127.0.0.1:8080/semanlink/dev.do
	// HttpUtils.getRequestURL(request) (deprecated) : http://127.0.0.1:8080/semanlink/jsp/page.jsp
	String contextPath = request.getContextPath(); // "/semanlink" in case of "http://127.0.0.1:8080/semanlink", "" in case of http://www.semanlink.net
	String reqURL = request.getRequestURL().toString();
	if ("".equals(contextPath)) {
		String x = new URL( new URL(reqURL), "/" ).toString() ;
		return x.substring(0, x.length()-1); // to remove final slash
	} else {
		return (new URL( new URL(reqURL), contextPath )).toString() ;
	}
}

//
//
//

public static boolean isIE(HttpServletRequest req) {
	String s = req.getHeader("user-agent");
	if (s == null) return true; // si l'user-agent n'est pas défini, assumons qu'on a affaire à un brouteur merdique
	return (s.indexOf("MSIE") > -1);
}

public static void printHttpHeaders(HttpServletRequest req) {
	Enumeration<String> nue = req.getHeaderNames();
	for (;nue.hasMoreElements();) {
		String header = nue.nextElement();
		System.out.println(header + " : " + req.getHeader(header));
	}
}

//
// XML
//

/**
 * BEWARE: doesn't take care of unicode high values
 * doesn't take care of <, &, etc.
 * http://www.w3.org/TR/REC-xml/#syntax defines the XML Character Range as follows:
 * Char	   ::=   	#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]	(any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.)
 */
public static String removeIllegalXMLChars(String s) {
	if (s == null) return null;
	int n = s.length();
	StringBuilder sb = new StringBuilder(n);
	for (int i = 0; i < n; i++) {
	  char c = s.charAt(i);
	  if (isLegalXMLChar(c)) sb.append(c);
	}
	return sb.toString();
}

/**
 * BEWARE: doesn't take care of unicode high values // TODO
 * http://www.w3.org/TR/REC-xml/#syntax defines the XML Character Range as follows:
 * Char	   ::=   	#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]	(any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.)
 */
// private static int xD800 = 8*16*16 + 13*16*16*16;
static public boolean isLegalXMLChar(int c) {
	if (c < 32) {
		if (c == 13) return true;
		if (c == 10) return true;
		if (c == 9) return true;
		return false;
	} else {
		// return c < xD800;
		return true;
	}
}

//
//
//

/** replace & by &amp; */
public static String handleAmpersandInHREF(String uri) {
	int k = uri.indexOf("&");
	if (k < 0) return uri;
	k = uri.indexOf("?");
	if (k < 0) return uri;
	k++;
	StringBuilder x = new StringBuilder(uri.substring(0,k));
	if (k < uri.length()) {
		String end = uri.substring(k);
		for(;;) {
			k = end.indexOf("&");
			if (k < 0) {
				x.append(end);
				break;
			}
			k++;
			x.append(end.substring(0,k));
			x.append("amp;");
			end = end.substring(k);
			if (end.startsWith("amp;")) end = end.substring(4);
		}
	}
	return x.toString();
}

//TODO
/** Problem: to have valid html when putting the text from the database into the page. */
public static String toHTMLOutput(String s) {
	return handleAmpersandInHREF(s);
}


}
