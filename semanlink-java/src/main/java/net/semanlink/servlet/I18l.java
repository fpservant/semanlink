/* Created on 20 oct. 07 */
package net.semanlink.servlet;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.semanlink.servlet.SLServlet;
import net.semanlink.util.Util;
import net.semanlink.util.xml.*;

/**
 * To deal with internationalization of strings. 
 * (Simple implementation of ResourceBundle).
 */
public class I18l { // we could say extends ResourceBundle
	
//
// RELATED TO THE SET OF I1!l AVAILABLE TO WEB APPLICATION
//

private static String[] availableLanguages = {"el","en","fr"};
/** Hashmap of available I18l. Key lang, data for that language. */
static HashMap<String, I18l> i18lHM;
static Locale preferedLocale;
public static void initAvailableLanguages(SLServlet slServlet, Locale prefLocale) throws IOException {
	i18lHM = new HashMap<String, I18l>();
	for (int i = 0; i < availableLanguages.length; i++) {
			loadAvailableLang(availableLanguages[i], slServlet);
	}
	preferedLocale = prefLocale;
}

static private void loadAvailableLang(String lang, SLServlet slServlet) throws IOException {
	I18l i18l = newInstance(slServlet, new Locale(lang));
	i18lHM.put(lang, i18l);
}

//

public static I18l getI18l(HttpSession session) {
	String lang = null;
	if (session != null) {
		lang = (String) session.getAttribute("net.semanlink.servlet.lang");
	}
	if (lang == null) lang = preferedLocale.getLanguage();
	I18l x = (I18l) i18lHM.get(lang);
	if (x == null) x = (I18l) i18lHM.get("en");
	return x;
}

public static String[] getAvailableLanguages() {
	return availableLanguages;
}

/*
public static I18l getI18l(HttpSession session) {
	String lang = null;
	if (session == null) {
		lang = "en";
	} else {
		lang = (String) session.getAttribute("net.semanlink.servlet.lang");
	}
	if (lang == null) lang = "en";
	I18l x = (I18l) i18lHM.get(lang);
	if (x == null) x = (I18l) i18lHM.get("en");
	return x;
}
*/


//

public static String get(HttpSession session, String key) {
	return getI18l(session).properties.getProperty(key);
}

public static String getLang(HttpSession session) {
	return getI18l(session).locale.getLanguage();
}

/*
public static String getFormatedMessage(HttpSession session, String key, String variable) {
	return getI18l(session).getFormatedMessage(key,variable);	
}

public static String getFormatedMessage(HttpSession session, String key, Object[] args) {
	return getI18l(session).getFormatedMessage(key,args);	
}	
*/
	
	
//
//
//
	

/**
 * Convert an (abstract) path (without localization) to the path to the localized file, cf /aboutfiles directory
 * @param pathInfo fr instance "/help.htm" wrt to the "aboutfiles" dir
 * @param context
 * @throws MalformedURLException 
 */
static public String pathToI18lFile(String pathInfo, HttpSession session, ServletContext context) throws MalformedURLException {
  	String shortFilename = pathInfo;
  	String path = null;
  	String lang = I18l.getLang(session);
  	if (!("en".equals(lang))) {
			// obligé de choisir un dossier qui ne s'appelle pas "about" (cad, pas comme ds le path de la request)
  		path = "/aboutfiles/" + lang + shortFilename;
  		/* Not OK with war files that are not expanded 
  		String fn = context.getRealPath(path);
  		File f = new File(fn);
  		if (!f.exists()) path = "/aboutfiles/en" + shortFilename;*/
  		URL url = context.getResource(path);
  		if (url == null) path = "/aboutfiles/en" + shortFilename;
  	} else {
  		path = "/aboutfiles/en" + shortFilename;
  	}
  	return path;
}

//
//
//
	
private Properties properties;
private Locale locale; 
/** If the resource for language is not found, use English instead */
public static I18l newInstance(SLServlet slServlet, Locale locale) throws IOException {
	// we look first if there is a localized file in the conf dir
	// (this way, Dimitris will be able to test his translation without expanding the war)
	// File dir = SLServlet.getConfigDir();
	
	// folder where the files are stored in the distribution
	// This doesn't work from a war that is not expanded (cf kattare)
	/*String folder = slServlet.getServletContext().getRealPath("/i18l/");
	File dir = new File(folder);
	if (!dir.exists()) throw new RuntimeException("No such file: " + folder);
	if (!dir.isDirectory()) throw new RuntimeException(folder + " not a directory"); */
	URL folderURL = slServlet.getServletContext().getResource("/i18l/");;
	if (folderURL == null) throw new RuntimeException("No such file: " + folderURL);
	// load values from the dir inside the distribution
	// I18l x = new I18l(dir, locale);
	I18l x = new I18l(slServlet.getServletContext(), locale);

	
	// But we want also to look whether there are values defined in the sl "conf" dir
	// which allows to add translations without modifying the servlet distrib,
	// hence allowing Dimitris (or another translator) to test his translations without expanding the war
	// So, we first load props from the standard dir, and then from the "conf" dir
	File outOfDistribDir = new File(slServlet.getConfigDir(),"i18l");
	// load values from the dir inside servlet's conf dir
	if (outOfDistribDir.exists()) {
		load(x.properties, outOfDistribDir, locale);		
	}
	
	return x;
}

/**
 * @param dir supposed to contains "[lang]" directories. Each is supposed to contain ".xml" or ".txt" files
 * @param locale
 * @throws IOException
 */
public I18l(File dir, Locale locale) throws IOException {
	this.properties = new Properties();
	this.locale = locale;
	load(this.properties, dir, locale);
}

/**
 * @param context supposed to contains "/i18l/[lang]" directories. Each is supposed to contain ".xml" or ".txt" files
 * @param locale
 * @throws IOException
 */
public I18l(ServletContext context, Locale locale) throws IOException {
	this.properties = new Properties();
	this.locale = locale;
	load(this.properties, context, locale);
}

// 2013-03
public I18l(ServletContext context, String dirPath, Locale locale) throws IOException {
	this.properties = new Properties();
	this.locale = locale;
	load(this.properties, context, dirPath, locale);
}


/** Loads english properties, then, if Locale is not english, loads properties for that language. 
 *  This ensures that we hava data for Locale.getLanguage, even if no data available for it
 *  (in this case, we have the english values). */
private static void load(Properties props, File dir, Locale locale) throws IOException {
	String lang = "en";
	load(props, dir, lang);
	
	lang = locale.getLanguage();
	
	if (lang != null) {
		if (!lang.equals("")) {
			if (!lang.equals("en")) {
				load(props, dir, lang);
			}
		}
	}
}



/** Loads english properties, then, if Locale is not english, loads properties for that language. 
 *  This ensures that we hava data for Locale.getLanguage, even if no data available for it
 *  (in this case, we have the english values). */
private static void load(Properties props, ServletContext context, Locale locale) throws IOException {
	String lang = "en";
	load(props, context, lang);
	
	lang = locale.getLanguage();
	
	if (lang != null) {
		if (!lang.equals("")) {
			if (!lang.equals("en")) {
				load(props, context, lang);
			}
		}
	}
}

// 2013-03
private static void load(Properties props, ServletContext context, String dirPath, Locale locale) throws IOException {
	String lang = "en";
	load(props, context, dirPath, lang);
	
	lang = locale.getLanguage();
	
	if (lang != null) {
		if (!lang.equals("")) {
			if (!lang.equals("en")) {
				load(props, context, dirPath, lang);
			}
		}
	}
}



/** Load properties for a given language. 
 *  looks for either a ".xml" file, or a ".properties" file. 
 *  If a xml file is found, doesn't look for a properties file
 *  @param props Properties to load data into
 *  Requires java 1.5 
 * @throws IOException */
private static void load(Properties props, File dir, String lang) throws IOException {
	InputStream in = null;
	File f;
	File dirLang = new File(dir, lang); // i18l/[lang]

	if (dirLang.exists()) {
		if (dirLang.isDirectory()) {
			String[] sfn = dirLang.list();
			for (int i = 0; i < sfn.length; i++) {
				String s = sfn[i];
				if (s.endsWith(".xml")) {
					f = xmlFile(new File(dirLang, s));
					in = new FileInputStream(f);
					props.loadFromXML(in);
				} else if (s.endsWith(".txt")) {
					f = new File(dirLang, s);
					in = new FileInputStream(f);
					props.load(in);
				} else {
					// System.err.println("what are you doing? " + new File(dirLang, s));
				}
			}
		}
	}

	// in = context.getResourceAsStream(propertiesFile(lang));
	/*f = propertiesFile(dir, lang);
	if (f.exists()) in = new FileInputStream((f.getPath()));
	if (in != null) {
		System.out.println(f);
		props.load(in);
	}*/
}

/** Load properties for a given language, supposed to be in a "/i18l/[lang]" directory
 *  looks for either a ".xml" file, or a ".properties" file. 
 *  If a xml file is found, doesn't look for a properties file
 *  @param props Properties to load data into
 *  Requires java 1.5 
 * @throws IOException */
private static void load(Properties props, ServletContext context, String lang) throws IOException {
// 2013-03
//	InputStream in = null;
//	String dirPath = "/i18l/" + lang + "/";
//	URL langDirURL = context.getResource(dirPath);
//
//	System.out.println("I18l-1 " + dirPath + " -> " + langDirURL);
//
//	if (langDirURL != null) {
//		// String[] sfn = dirLang.list();
//		Set paths = context.getResourcePaths(dirPath);
//		
//		for (Iterator it = paths.iterator(); it.hasNext(); ) {
//			String s = (String) it.next();
//			if (s.endsWith(".xml")) {
//				in = context.getResourceAsStream(s);
//				props.loadFromXML(in);
//			} else if (s.endsWith(".txt")) {
//				in = context.getResourceAsStream(s);
//				props.load(in);
//			} else {
//				// System.err.println("what are you doing? " + new File(dirLang, s));
//			}
//		}
//	}
	load(props, context, "/i18l/", lang);
}

// 2013-03
// dirPath eg. /i18l/ (the dr that contains the lang folder)
private static void load(Properties props, ServletContext context, String dirPath, String lang) throws IOException {
	InputStream in = null;
	dirPath = dirPath + lang + "/";
	URL langDirURL = context.getResource(dirPath);

	// System.out.println("I18l-2 " + dirPath + " -> " + langDirURL);

	if (langDirURL != null) {
		// String[] sfn = dirLang.list();
		Set<String> paths = context.getResourcePaths(dirPath);
		
		for (String s : paths ) {
			if (s.endsWith(".xml")) {
				in = context.getResourceAsStream(s);
				props.loadFromXML(in);
			} else if (s.endsWith(".txt")) {
				in = context.getResourceAsStream(s);
				props.load(in);
			} else {
				// System.err.println("what are you doing? " + new File(dirLang, s));
			}
		}
	}
}


//
// I first created property files, then I switched to XML. Some utiities
// to save props into a file (no more used)
//

/** use with caution: beware not to overwrite important files */
private static void storeToXML(Properties props, File dir, String lang) throws IOException {
	System.out.println("storeToXML: " + xmlFile(dir, lang));
	/* this is not very good:
	 * I am not sure it writes UTF-8
	 * We lose the order of resources in initial file
	 */
	OutputStream out = new FileOutputStream((xmlFile(dir, lang).getPath()));
	String comments = "";
	props.storeToXML(out, comments);
}

private static void convertTXT2XML(File txtFile, File xmlFile) throws IOException {
	InputStream in = new FileInputStream(txtFile);
	Properties props = new Properties();
	props.load(in);
	BufferedReader reader = new BufferedReader(new FileReader(txtFile));
	OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(xmlFile),"UTF8");
	writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.write("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
	writer.write("\n");
	writer.write("<properties>");
	writer.write("\n");
	writer.write("<comment></comment>");
	writer.write("\n");
	try {
		for (;;) {
			String s = reader.readLine();
			if (s == null) break;
			if (s.trim().startsWith("#")) continue;
			int k = s.indexOf("=");
			if (k < 0) continue;
			s = s.substring(0,k-1);
			s = s.trim();
			String key = s;
			String val = props.getProperty(key);
			writer.write("<entry key=\"");
			writer.write(XMLWriterUtil.xmlEscape(key));
			writer.write("\">");
			writer.write(XMLWriterUtil.xmlEscape(val));
			writer.write("</entry>");
			writer.write("\n");
		}
	} finally {
		writer.write("</properties>");
		writer.flush();
		writer.close();
	}
}

private static File xmlFile(File dir, String lang) {
	File x = new File(dir,lang);
	if (x.getPath().endsWith(lang + ".txt")) {
		String s = Util.getWithoutExtension(x.getName());
		x = new File(dir, s + ".xml");
	} else {
		// having lang in the filename is not useful (lang is already in the dir)
		// It is just to avoid errors when copying it to another dir.
		x = new File(x, "strings-" + lang + ".xml");
	}
	return x;
}

private static File xmlFile(File f) {
	File x = null;
	if (f.getPath().endsWith(".txt")) {
		String s = Util.getWithoutExtension(f.getName());
		x = new File(f.getParent(), s + ".xml");
	} else 	if (f.getPath().endsWith(".xml")) {
		return f;
	} else {
		System.err.println("what are you doing here? " + f);
	}
	return x;
}

private static File propertiesFile(File dir, String lang) {
	File x = new File(dir,lang);
	x = new File(x, "strings-" + lang + ".txt");
	return x;
}

//
//
//

static public void main (String args[]) {
	try {
		test("/Users/fps/Semanlink/semanlink-eclipse/WebContent/i18l/", "en");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

static protected void test(String folder,  String lang) throws IOException {
	File dir = new File(folder);
	dir = new File(dir,lang);
	File txtFile = new File(dir,"strings-" + lang + ".txt");
	File xmlFile = new File(dir,"strings-" + lang + ".xml");
	convertTXT2XML(txtFile, xmlFile);
}

//
// ATTENTION, get est ici static, utilisé par SLServlet pour ses propres strings
// utiliser getString pour this
//

public Enumeration getKeys() {
	return this.properties.keys();
}

/** cf ResourceBunble */
protected Object handleGetObject(String key) {
	// TODO Auto-generated method stub
	return this.properties.get(key);
}

/*public static String get(String key) {
	return i18l.properties.getProperty(key);
}*/


public String getString(String key) {
	return (String) this.properties.get(key);
}

//
//
//

/*
public static String getFilePath(String shortPath) {
	return i18l.getFilePath(shortPath);
}

public String getFilePath(String shortPath) {
	i18l.getFilePath(shortPath);
}
*/

//
// MESSAGE FORMAT UTILS
//

public String getFormatedMessage(String key, String variable) {
	Object[] args = new Object[1];
	args[0] = variable;
	return getFormatedMessage(key,args);	
}

public String getFormatedMessage(String key, Object[] args) {
	java.text.MessageFormat messageFormat = new java.text.MessageFormat(getString(key));
	return messageFormat.format(args);	
}

}
