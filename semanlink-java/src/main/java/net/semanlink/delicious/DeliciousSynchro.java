///* Created on 25 sept. 06 */
///*
// * Possible errors
// * org.xml.sax.SAXParseException: The string "--" is not permitted within comments.
// * del.icio.us.DeliciousException: Response parsing error
// * 
//SLModel.getFile: http://sites.wiwiss.fu-berlin.de/suhl/bizer/ng4j/disco/
//importOneDate: Wed Jan 17 02:00:00 CET 2007
//[Fatal Error] :-1:-1: Premature end of file.
//ERROR [http-7080-1] (Delicious.java:372) - org.xml.sax.SAXParseException: Premature end of file.
//21 déc. 2010 16:25:08 org.apache.catalina.core.ApplicationDispatcher invoke
//GRAVE: "Servlet.service()" pour la servlet jsp a lancé une exception
//org.xml.sax.SAXParseException: Premature end of file.
//	at org.apache.xerces.parsers.DOMParser.parse(Unknown Source)
// *
// */
//package net.semanlink.delicious;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Locale;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.jsp.JspWriter;
//
//import org.apache.jena.rdf.model.Literal;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.rdf.model.Statement;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.Property;
//import org.apache.jena.rdf.model.StmtIterator;
//import org.apache.jena.shared.JenaException;
//import org.apache.jena.vocabulary.RDF;
//
//import java.io.File;
//
//import net.semanlink.semanlink.PropertyValues;
//import net.semanlink.semanlink.SLDocument;
//import net.semanlink.semanlink.SLKeyword;
//import net.semanlink.semanlink.SLModel;
//import net.semanlink.semanlink.SLUtils;
//import net.semanlink.semanlink.SLVocab;
//import net.semanlink.servlet.DocumentFactory;
//import net.semanlink.servlet.Manager_Document;
//import net.semanlink.servlet.SLServlet;
//import net.semanlink.servlet.SemanlinkConfig;
//import net.semanlink.sljena.JFileModel;
//import net.semanlink.util.Util;
//import net.semanlink.util.YYYYMMDD;
//import net.semanlink.util.YearMonthDay;
//
//import del.icio.us.*;
//import del.icio.us.beans.Bundle;
//import del.icio.us.beans.DeliciousDate;
//import del.icio.us.beans.Post;
//public class DeliciousSynchro {
//private String user;
//private SLModel mod;
//private Delicious delicious;
//private DeliciousCaller deliciousCaller;
//private Locale locale;
//private boolean synchroInited = false;
//private boolean importInited = false;
///** format "yyyy-mm-dd" */
//private String today;
//private SaveInfoAboutSynchro saveInfoAboutSynchro;
//private boolean importPostsCompleted = false;
//private boolean exportPostsCompleted = false;
//private boolean synchroCompleted = false;
///** list of SLDocument imported so far (in order to be able to consolidate it at the end of import :
// * when going away and back to page, everything would disappear. */
//private LinkedHashSet importedList;
//private LinkedHashSet exportedList;
//// IMPORT RELATED
///** ordered list of delicious dates that have not been imported yet. The next date to be imported is the first of this list
// *  (inited by initImport, or initSynchro) */
//private List delDatesAl;
//// IMPORT ALL AT ONCE RELATED
//private List allDelPosts;
///** total number of posts in delicious */
//private int allDelPostsSize;
//// EXPORT RELATED
//public static int nbofDaysForExport = 100;
//private static String LAST_EXPORT_DATE_IF_NEVER_EXPORTED = "2006-01-01"; // BOF BOF
//private List docListToBeExported;
//private String dateOfLastExportedDoc;
//// BUNDLES RELATED
//List bundlesAsSLTagList;
//
//
//// 2010-12 : global import likely to fail (seems to get non-xml from delicious if "unexpected content" in the comments of the bookmark)
//// quick and dirty hack to try to handle that (we swith to import one by one in case of error)
//
//public DeliciousSynchro(SLModel mod, String user, String password, 
//		String proxyHost, int proxyPort, String proxyUserName, String proxyPassword) throws Exception {
//	this.mod = mod;
//	this.user = user;
//	this.deliciousCaller = new DeliciousCaller();
//	this.deliciousCaller.waitPlease();
//	this.delicious = new Delicious(user,password);
//	if ((proxyHost != null) && (!"".equals(proxyHost))) {
//		this.delicious.setProxyConfiguration(proxyHost,proxyPort);
//	}
//	if ((proxyUserName != null) && (!"".equals(proxyUserName))) {
//		this.delicious.setProxyAuthenticationConfiguration(proxyUserName, proxyPassword);
//	}
//	this.deliciousCaller.done();
//	this.locale = Locale.getDefault();
//	this.saveInfoAboutSynchro = new SaveInfoAboutSynchro(user);
//	this.today = (new YearMonthDay()).getYearMonthDay("-");
//}	
//
//public String getUser() { return this.user; }
//public List getImportedList() {
//	if (this.importedList == null) return null;
//	return Arrays.asList(this.importedList.toArray());
//}
//public boolean isImportPostsCompleted() { return this.importPostsCompleted; }
//public String getLastDeliciousImportDate() {
//	if (this.saveInfoAboutSynchro != null) {
//		return this.saveInfoAboutSynchro.getLastDeliciousImportDate();
//	}
//	return null;
//}
//public List getExportedList() { 
//	if (this.exportedList == null) return null;
//	return Arrays.asList(this.exportedList.toArray());
//}
//public boolean isExportPostsCompleted() { return this.exportPostsCompleted; }
//public String getLastDeliciousExportDate() {
//	if (this.saveInfoAboutSynchro != null) {
//		return this.saveInfoAboutSynchro.getLastDeliciousExportDate();
//	}
//	return null;
//}
//public boolean isSynchroCompleted() { return this.synchroCompleted; }
//
////
////
////
//
///** To make delious happy: wait one second between calls. 
// *  So call waitPlease before sending a request to delicious,
// *  and call done after. */
//class DeliciousCaller {
//	private long lastCall;
//	void waitPlease() {
//		if (lastCall == 0) {
//			lastCall = System.currentTimeMillis();
//			return;
//		}
//		for (;;) {
//			if (System.currentTimeMillis() - lastCall > 1000) {
//				return;
//			}
//		}
//	}
//	
//	void done() {
//		lastCall = System.currentTimeMillis();
//		int httpResult = delicious.getHttpResult();
//		System.out.println("httpResult : " + httpResult);
//	}
//}
//
////
//// UTILS GENERAUX
////
//
///** could be useful in other places */
//public static Date slCreationDate2JavaDate(SLDocument doc) {
//	PropertyValues vals = doc.getProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
//	if (vals != null) {
//		String prop = vals.getFirstAsString();
//		if ((prop != null) && (prop.length() >= 10)) {
//			String yyyy = prop.substring(0, 4);
//			String mm = prop.substring(5,7);
//			String dd = prop.substring(8,10);
//			try {
//				int y = Integer.parseInt(yyyy);
//				int m = Integer.parseInt(mm);
//				int d = Integer.parseInt(dd);
//				return (new YYYYMMDD(y,m,d)).getCalendar().getTime();
//			} catch (NumberFormatException e) {
//			}	
//		}
//	}
//	return null;
//}
//
////
//// tests del -> sl bas� sur les tags
////
//
///** on prend sa short uri */
//private String slKeyword2DelTag(SLKeyword kw) {
//	return Util.getLastItem(kw.getURI(),'/');
//}
//
////
//// IMPORT
////
//
///** loads the list of "delicious dates" (dates with posts). Removes those older or equal to last import */
//public void initImport() throws Exception {
//	if (!importInited) {
//		importInited = true;
//		this.delDatesAl = computeDelDates();
//		String lastImportDate = this.saveInfoAboutSynchro.getLastDeliciousImportDate();
//		if (lastImportDate != null) {
//			int n = this.delDatesAl.size();
//			for (int i = 0; i < n; i++) {
//				DeliciousDate delDate = (DeliciousDate) this.delDatesAl.get(0);
//				if (delDate.getDate().compareTo(lastImportDate) <= 0) {
//					this.delDatesAl.remove(0);
//				} else {
//					break;
//				}
//			}
//		}
//		this.importedList = new LinkedHashSet();
//	}
//}
//
//// ne faudrait-il pas retourner une copie ?
//private List computeDelDates() {
//	this.deliciousCaller.waitPlease();
//	List x = this.delicious.getDatesWithPost();
//	this.deliciousCaller.done();
//
//	Comparator comp = new Comparator() {
//		public int compare(Object o1, Object o2) {
//			DeliciousDate d1 = (DeliciousDate) o1;
//			DeliciousDate d2 = (DeliciousDate) o2;
//			return (d1.getDate().compareTo(d2.getDate()));
//		}		
//	};
//	Collections.sort(x, comp);
//	return x;
//}
//
///* Handle differently case where no import has been done yet, and case it has. */
///** called in loop by an ajax script, returns the docs for one delicious date at a time. */
//public void importNext(HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	if (this.importedList == null) initImport();
//	String lastImportDate = this.saveInfoAboutSynchro.getLastDeliciousImportDate();
//	if (lastImportDate == null) {
//	// if (this.delDatesAl == null) {
//		// never imported before: build sldocs from allDelPosts
//		
//		// 2010-12
//		try {
//		
//			if (this.allDelPosts == null) initAllDelPosts();
//			for(;;) {
//				int n = this.allDelPosts.size();
//				if (n > 0) {
//					Post post = (Post) this.allDelPosts.remove(0);
//					SLDocument doc = delPost2Semanlink(post, this.locale);
//					if (doc == null) {
//						continue;
//					} else {
//						out.flush();
//						out.write("<li>Importing: " + Integer.toString(this.allDelPostsSize - n) + " / " + Integer.toString(this.allDelPostsSize) + "</li>");
//						printOneDoc(doc, request, response, out);
//						this.importedList.add(doc);
//					}
//				} else { // list is empty: the end
//					out.flush();
//					out.write("<li>IMPORT COMPLETED! " + Integer.toString(this.allDelPostsSize) + " posts imported.</li>"); // Attention, cette chaine est utilis�e par delicious.js
//					this.importPostsCompleted = true;
//					// on dit que la derni�re date import�e est hier (pour si jamais on cr�e ds del de nouveayx docs aujourd'hui
//					String yesterday = (YearMonthDay.daysAgo(1)).getYearMonthDay("-");
//					saveInfoAboutSynchro.setLastDeliciousImportDate(yesterday);
//				}
//				break;
//			}
//			
//		} catch (Exception e) { // 2010-12
//			e.printStackTrace();
//			
//			
//			int n = delDatesAl.size();
//			if (n > 0) {
//				DeliciousDate delDate = (DeliciousDate) delDatesAl.remove(0);
//				importOneDate(delDate, request, response, out);
//				// save date, unless it is today (new posts can be created later today)
//				if (!today.equals(delDate.getDate())) {
//					saveInfoAboutSynchro.setLastDeliciousImportDate(delDate.getDate());
//				}
//			} else { // list is empty: the end
//				out.flush();
//				out.write("<li>IMPORT COMPLETED!</li>"); // Attention, cette chaine est utilis�e par delicious.js
//				this.importPostsCompleted = true;
//			}
//
//			
//		}
//	} else {
//		// not an import from zero, but from the last import date
//		int n = delDatesAl.size();
//		if (n > 0) {
//			DeliciousDate delDate = (DeliciousDate) delDatesAl.remove(0);
//			importOneDate(delDate, request, response, out);
//			// save date, unless it is today (new posts can be created later today)
//			if (!today.equals(delDate.getDate())) {
//				saveInfoAboutSynchro.setLastDeliciousImportDate(delDate.getDate());
//			}
//		} else { // list is empty: the end
//			out.flush();
//			out.write("<li>IMPORT COMPLETED!</li>"); // Attention, cette chaine est utilis�e par delicious.js
//			this.importPostsCompleted = true;
//		}
//	}
//}
//
///** doesn't update saveInfoAboutSynchro */
//private void importOneDate(DeliciousDate delDate, HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//		// importing
//		List docs = importOneDate(delDate);
//		// adding to list of imported
//		this.importedList.addAll(docs);
//		// displaying imported docs in the page
//		for (int ipost = 0; ipost < docs.size(); ipost++) {
//			SLDocument doc = (SLDocument) docs.get(ipost);
//			printOneDoc(doc, request, response, out);
//		}
//}
//
//private void printOneDoc(SLDocument doc, HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	DocumentFactory documentFactory = Manager_Document.getDocumentFactory();
//	request.setAttribute("net.semanlink.servlet.jsp.currentdoc", doc);
//	request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", doc.getKeywords());
//	out.flush();
//	String docLineJspName = documentFactory.getDocLineJspName(doc);
//	request.getRequestDispatcher(docLineJspName).include(request, response);
//}
//
//
///*
// * Ceci pourrait �tre consid�rablement optimis� en ne sauvant les fichiers qu'une seule fois !
// */
///** Imports the delicious document posted on delDate. Returns a List of SLDocuments.
// *  Documents that already exists in sl are not overwritten but leaved unchanged.
// *  They are not inlcuded in the returned list*/
//// (et c'ezst important qu'ils n'y soient pas pour le focntionnement de l asynchro:
//// on fait en effet, pour une date donn�e, d'abord l'import.
//// Puis on n'exporte les docs qui n'ont pas �t� import�s.
//// Or on veut mettre � jour delicious avec ce qu'il y a dans sl
//public List importOneDate(DeliciousDate delDate) throws Exception {
//		String delDateString = delDate.getDate();
//		Date date = DeliciousUtils.getDateFromUTCString(delDateString + "T01:00:00Z");
//		this.deliciousCaller.waitPlease();
//		System.out.println("importOneDate: " + date);
//		List postsAl = this.delicious.getPosts(null,date,null);
//		this.deliciousCaller.done();
//		int n = postsAl.size();
//		List x = new ArrayList(n);
//		for (int ipost = 0; ipost < n; ipost++) {
//			Post post = (Post) postsAl.get(ipost);
//			SLDocument doc = delPost2Semanlink(post, delDate, locale);
//			if (doc != null) x.add(doc);
//		}
//		return x;
//}
//
//
///** copy a delicious post to semanlink (only if it doesn't exist yet: does nothing in this case, and return null). */
//SLDocument delPost2Semanlink(Post post, Locale locale) throws Exception {
//	Date date = post.getTimeAsDate();
//	YearMonthDay yearMonthDay = new YearMonthDay(date);
//	return delPost2Semanlink(post, yearMonthDay.getYearMonthDay("-"), locale);
//}
//
///** copy a delicious post to semanlink (only if it doesn't exist yet: does nothing in this case, and return null). */
//private SLDocument delPost2Semanlink(Post post, DeliciousDate delDate, Locale locale) throws Exception {
//	return delPost2Semanlink(post, delDate.getDate(), locale);
//}
//
///** copy a delicious post to semanlink (only if it doesn't exist yet: does nothing if it already exists, and return null). */
//private SLDocument delPost2Semanlink(Post post, String slDate, Locale locale) throws Exception {
//	SLDocument doc = mod.getDocument(post.getHref());
//	if (!mod.existsAsSubject(doc)) {
//		mod.addDocProperty(doc, SLVocab.SL_CREATION_DATE_PROPERTY, slDate, null);
//		mod.setDocProperty(doc, SLVocab.SL_CREATION_TIME_PROPERTY, post.getTime(), null);
//		String s = post.getDescription();
//		if ((s != null)) {
//			s = s.trim();
//			if (!("".equals(s))) mod.addDocProperty(doc, SLVocab.TITLE_PROPERTY, s, null);
//		}
//		s = post.getExtended();
//		if ((s != null)) {
//			s = s.trim();
//			if (!("".equals(s))) mod.addDocProperty(doc, SLVocab.COMMENT_PROPERTY, s, null);
//		}
//		String[] delTagStrings = post.getTagsAsArray(" ");
//		for (int jtag = 0; jtag < delTagStrings.length; jtag++) {
//			SLKeyword kw = delTagString2SLKeyword(delTagStrings[jtag], locale);
//			mod.addKeyword(doc, kw); // CA SERAIT BIEN DE POUVOIR AJOUTER PLUSIEURS KWS A LA FOIS TODO
//		}
//		// System.out.println("delPost2Semanlink " + doc.getURI() + " / " + slDate + " / " + post.getTime());
//		return doc;
//	} else {
//		return null;
//	}
//}
//
///** Return the SLKeyword corresponding to a delicious tag, creating it if necessary. */
//SLKeyword delTagString2SLKeyword(String delTagString, Locale locale) throws Exception {
//	// String delTagUri = "http://del.icio.us/tag/" + delTagString;
//	delTagString = delTagString.replace("_", " ");
//	SLKeyword kw = mod.kwLabel2SLKw(delTagString, null, locale); // peut ne pas exister vraiment
//	String kwUri = kw.getURI();
//	if (!(mod.kwExists(kwUri))) {
//		kw = mod.doCreateKeyword(kwUri,delTagString,locale);
//	}
//	return kw;
//}
//
////
//// EXPORT
////
//
///** here the list to be exported is set to be the list of most recent documents.
// *  We could decide to set it in other ways.
// */
//public void initExport() throws Exception {
//	this.exportedList = new LinkedHashSet();
//	this.docListToBeExported = this.mod.getRecentDocs(nbofDaysForExport);
//}
//
//// exporte un seul doc � la fois. Fait pour fonctionner avec une liste de docs qui couvre plusieurs dates
///** called by an ajax script asking one more document at a time */
//public void exportNext(HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	boolean exportADefineListOfDocsMode = false;
//	if (exportADefineListOfDocsMode) {
//		// PREMIERE IMPLEMENTATION : LES DOCS DE "New Entries"
//		if (this.docListToBeExported == null) initExport();
//	
//		String lastExportDate = this.saveInfoAboutSynchro.getLastDeliciousExportDate();
//		if (lastExportDate == null) lastExportDate = LAST_EXPORT_DATE_IF_NEVER_EXPORTED;
//		
//		for (int idoc = this.docListToBeExported.size()-1; idoc > -1; idoc--) {
//			SLDocument doc = (SLDocument) this.docListToBeExported.get(idoc);
//			this.docListToBeExported.remove(idoc);
//	
//			String date = doc.getDate();
//			if (date != null) {
//				if (lastExportDate != null) {
//					if (date.compareTo(lastExportDate) <= 0) continue;
//				}
//			}
//	
//			boolean added = toDelicious(doc);
//			if (added) {
//				request.setAttribute("net.semanlink.servlet.jsp.currentdoc", doc);
//				request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", doc.getKeywords());
//				out.flush();
//				this.exportedList.add(doc);
//				String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(doc);
//				request.getRequestDispatcher(docLineJspName).include(request, response);
//				if (date != null) {
//					if (!date.equals(dateOfLastExportedDoc)) { // on a donc fini les doc de date dateOfLastExportedDoc
//						saveInfoAboutSynchro.setLastDeliciousExportDate(dateOfLastExportedDoc);
//						dateOfLastExportedDoc = date;
//					}
//				}
//				return;
//			}
//		} // for %>
//		out.flush();
//		out.write("<li>EXPORT COMPLETED!</li>"); // Attention, cette chaine est utilis�e par delicious.js
//		this.exportPostsCompleted = true;
//	} else {
//		synchroNext(request,response,out,true);
//	}
//}
//
//
///** Send one SLDocument to delicious.
//*  return false if doc is not to be added to delicious (bcause it is a local doc). */
//public boolean toDelicious(SLDocument doc) throws IOException, URISyntaxException {
//	String docUri = doc.getURI();
//	if (mod.isLocalDocument(docUri)) return false;
//	this.deliciousCaller.waitPlease();
//	StringBuffer tags = new StringBuffer();
//	List kws = doc.getKeywords();
//	for (int ikw = 0; ikw < kws.size(); ikw++) {
//		SLKeyword kw = (SLKeyword) kws.get(ikw);
//		tags.append(slKeyword2DelTag(kw));
//		tags.append(" ");
//	}
//	Date date = slCreationDate2JavaDate(doc);
//	if (date == null) date = new Date();
//	this.delicious.addPost(docUri, SLUtils.getLabel(doc), doc.getComment(), tags.toString(), date, true, true);
//	this.deliciousCaller.done();
//	return true;
//}
//
///** Send a list of SLDocuments to delicious, writing progress into a jsp
// *  This only sends documents that are not local. 
// * @throws Exception */
///*
//public void toDelicious(List docList, HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//for (int idoc = 0; idoc < docList.size(); idoc++) {
//	toDelicious((SLDocument) docList.get(idoc));
//}
//for (int idoc = docList.size()-1; idoc > -1; idoc--) {
//	SLDocument slDoc = (SLDocument) docList.get(idoc);
//	boolean added = toDelicious(slDoc);
//	if (added) {
//		request.setAttribute("net.semanlink.servlet.jsp.currentdoc", slDoc);
//		// ceci sert pour l'affichage des kws du doc
//		//List kwsOfDoc = beanDocList.getKeywordsToShow(idoc);
//		//if (kwsOfDoc != null) {
//		//	request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", kwsOfDoc);
//		//} else {
//		//	request.removeAttribute("net.semanlink.servlet.jsp.currentdoc.kws");
//		//}
//		out.flush();
//		String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(slDoc);
//		request.getRequestDispatcher(docLineJspName).include(request, response);
//	}
//} // for
//}*/
//
////
//// IMPORT BUNDLES
////
//
//public List getBundlesAsSLTagList() {
//	return this.bundlesAsSLTagList;
//}
//
//// OPTIM TODO : addParentChildLink �crit dans sle fichier, sans v�rifier si par hasard le lien n'existait pas d�j� (ce qui se produit souvent ici)
///** all bundles are imported at once. They are written one by one to the writer. */
//public void importBundles(HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	if (this.bundlesAsSLTagList == null) {
//		this.bundlesAsSLTagList = new ArrayList();
//		this.deliciousCaller.waitPlease();
//		List bundlesAl = this.delicious.getBundles();
//		this.deliciousCaller.done();
//		ArrayList al = new ArrayList(1);
//		for (int i = 0; i < bundlesAl.size(); i++) {
//			Bundle bundle = (Bundle) bundlesAl.get(i);
//			String name = bundle.getName();
//			String[] delTags = bundle.getTagsAsArray();
//			SLKeyword bundleAsSLTag = delTagString2SLKeyword(name, locale);
//			String bundleUri = bundleAsSLTag.getURI();
//			for (int j = 0; j < delTags.length; j++) {
//				SLKeyword child = delTagString2SLKeyword(delTags[j], locale);
//				// do not make it a child of bundle if it is equal to bundle
//				if (child.equals(bundleAsSLTag)) continue;
//				mod.addParentChildLink(bundleUri, child.getURI());
//			}
//			// livetreesons.jsp displays either the sons of a kw (use attribute "kw")
//			// or a list of kws (attribute livetreelist).
//			// We use here a list for each bundle
//			al.clear();
//			al.add(bundleAsSLTag);
//			this.bundlesAsSLTagList.add(bundleAsSLTag);
//			
//			if (out != null) {
//				out.write("<ul class=\"livetree\">");
//				request.setAttribute("livetreelist", al);
//				request.setAttribute("divid", Integer.toString(i));
//				out.flush();
//				request.getRequestDispatcher("/jsp/livetreesons.jsp").include(request, response);
//				out.write("</ul>");
//			}
//		}
//	}
//}
//
////
////
////
//
//class SaveInfoAboutSynchro {
//	private JFileModel jFileModel;
//	private String lastDeliciousExportDate = null;
//	private String lastDeliciousImportDate = null;
//	Resource res;  // c'est � elle qu'on affecte les props
//	SaveInfoAboutSynchro(String user) throws JenaException, IOException {
//		String userUrl = "http://del.icio.us/" + user;
//		
//		File file = new File(SLServlet.getConfigDir(), "delicious.rdf");
//		jFileModel = new JFileModel(file.getAbsolutePath(), SLServlet.getServletUrl());
//		Model saveInfoModel = jFileModel.getModel();
//		
//		Property deliciousAccount = saveInfoModel.getProperty(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA + "DeliciousAccount");
//		
//		/* ResIterator ite = saveInfoModel.listSubjectsWithProperty(RDF.type, deliciousAccount);
//		if (ite.hasNext()) {
//			String longAccount = ite.nextResource().getURI(); // une url longue de compte delicious
//		}*/
//		
//		this.res = saveInfoModel.getResource(userUrl);
//		StmtIterator it = saveInfoModel.listStatements(res, RDF.type, deliciousAccount);
//		if (!(it.hasNext())) {
//			saveInfoModel.add(res, RDF.type, deliciousAccount);
//		}
//		
//		Property lastDeliciousExportDateProp = saveInfoModel.getProperty(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA + "lastDeliciousExportDateProp");
//		Property lastDeliciousImportDateProp = saveInfoModel.getProperty(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA + "lastDeliciousImportDateProp");
//		Statement sta;
//		sta = res.getProperty(lastDeliciousExportDateProp);
//		if (sta != null) {
//			this.lastDeliciousExportDate = ((Literal) sta.getObject()).getString();
//		}
//		sta = res.getProperty(lastDeliciousImportDateProp);
//		if (sta != null) {
//			this.lastDeliciousImportDate = ((Literal) sta.getObject()).getString();
//		}
//	}
//	
//	/** can be null */
//	String getLastDeliciousExportDate() {
//		return this.lastDeliciousExportDate;
//	}
//	
//	
//	String getLastDeliciousImportDate() { return this.lastDeliciousImportDate; }
//	void setLastDeliciousExportDate(String s) throws JenaException, IOException, URISyntaxException {
//		this.lastDeliciousExportDate = s;
//		Model saveInfoModel = jFileModel.getModel();
//		if (s != null) {
//			Property prop = saveInfoModel.getProperty(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA + "lastDeliciousExportDateProp");
//			saveInfoModel.removeAll(this.res, prop, null);
//			Statement sta = saveInfoModel.createStatement(res, prop, saveInfoModel.createLiteral(s, null));
//			saveInfoModel.add(sta);
//			this.jFileModel.save();
//		}
//	}
//	void setLastDeliciousImportDate(String s) throws JenaException, IOException, URISyntaxException {
//		this.lastDeliciousImportDate = s;
//		Model saveInfoModel = jFileModel.getModel();
//		if (s != null) {
//			Property prop = saveInfoModel.getProperty(SemanlinkConfig.SEMANLINK_CONFIG_SCHEMA + "lastDeliciousImportDateProp");
//			saveInfoModel.removeAll(this.res, prop, null);
//			Statement sta = saveInfoModel.createStatement(res, prop, saveInfoModel.createLiteral(s, null));
//			saveInfoModel.add(sta);
//			this.jFileModel.save();
//		}
//	}
//} // class SaveInfoAboutSynchro
//
////
//// SYNCHRO
////
//
///*void synchroOneDelDate(DeliciousDate lastSynchroDelDate, DeliciousDate nextDelDate) throws Exception {
//	Date lastSynchroDate = DeliciousUtils.getDateFromUTCString(lastSynchroDelDate.getDate() + "T01:00:00Z");
//	Date nextDate = DeliciousUtils.getDateFromUTCString(nextDelDate.getDate() + "T01:00:00Z");
//	// export sl docs created between these 2 dates
//	Calendar calendar = Calendar.getInstance();
//	calendar.setTime(lastSynchroDate);
//	for(;;) {
//		calendar.add(Calendar.DAY_OF_MONTH, 1);
//		Date date = calendar.getTime();
//		if (date.before(nextDate)) {
//			// exporter les docs de ce jour
//			exportOneDate(date);
//		} else {
//			break;
//		}
//	}
//	// take note of sldocs created on nextDelDate to be exported 
//	List docsToBeExported = this.mod.geDocs(nextDate);
//	// import
//	importOneDate(nextDelDate);
//	// export
//	exportList(docsToBeExported);
//}
//
//private void exportOneDate(Date date) throws Exception {
//	exportList(this.mod.geDocs(date));
//}
//
//private void exportList(List docs) throws Exception {
//	for (int idoc = 0; idoc < docs.size(); idoc++) {
//		boolean added = toDelicious((SLDocument) docs.get(idoc));
//		if (added) {
//			// TODO : ECRIRE ICI DS LA PAGE, PARCE QUE CA DURE ENTRE 2
//		}
//	}
//}*/
//
//
//public void initSynchro() throws Exception {
//	this.synchroInited = true;
//	initImport();
//	this.exportedList = new LinkedHashSet();
//}
//// mod.geDocs(Date date)
//
//
///** called by an ajax script asking one date at a time */
//public void synchroNext(HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	synchroNext(request, response, out, false);
//}
//
///** used for synchro or export only, depending on exportOnly boolean. */
//private void synchroNext(HttpServletRequest request, HttpServletResponse response, JspWriter out, boolean exportOnly) throws Exception {
//	System.out.println("synchroNext exportOnly " + exportOnly);
//	if (!synchroInited) {
//		initSynchro();
//	}
//
//
//	boolean finished = false;
//	
//	// first, find the next date to be imported and exported
//	DeliciousDate nextDelDateToBeImported = null;
//	Date nextDateToBeImported = null;
//	int n = 0;
//	if (!exportOnly) {
//		n = delDatesAl.size();
//		if (n > 0) {
//			nextDelDateToBeImported = (DeliciousDate) delDatesAl.get(0);
//			nextDateToBeImported = DeliciousUtils.getDateFromUTCString(nextDelDateToBeImported.getDate() + "T01:00:00Z");
//		}
//	}
//
//	String lastExportDateString = this.saveInfoAboutSynchro.getLastDeliciousExportDate();
//	if (lastExportDateString == null) lastExportDateString = LAST_EXPORT_DATE_IF_NEVER_EXPORTED;
//	Date lastExportDate = DeliciousUtils.getDateFromUTCString(lastExportDateString + "T01:00:00Z");
//	Calendar calendar = Calendar.getInstance();
//	calendar.setTime(lastExportDate);
//	calendar.add(Calendar.DAY_OF_MONTH, 1);
//	Date nextDateToBeExported = calendar.getTime();
//	
//	// compare nextDateToBeImported and nextDateToBeExported
//
//	boolean doexportonly = false;
//	boolean doimportonly = false;
//	boolean doboth = false;
//	if (nextDateToBeImported == null) {
//		if (nextDateToBeExported != null) {
//			doexportonly = true;
//		} else {
//			// impossible
//			throw new RuntimeException("I thought this is impossible");
//		}
//	} else {
//		if (nextDateToBeExported != null) {
//			if (nextDateToBeImported.before(nextDateToBeExported)) {
//				doimportonly = true;
//			} else if (nextDateToBeExported.before(nextDateToBeImported)) {
//				doexportonly = true;
//			} else {
//				// next date to be exported and imported are the same
//				doboth = true;
//			}
//		} else {
//			// impossible
//			throw new RuntimeException("I thought this is impossible");
//		}		
//	}
//	
//	if (doboth) {
//		doboth = false;
//		doimportonly = true;
//		// le prochain coup, ce sera l'export -- ce qui implique qu'on v�rifie
//		// pour l'export de chaque doc qu'on ne vient pas de l'importer
//	}
//	
//	if (doimportonly) {
//		System.out.println("synchroNext importOneDate " + nextDelDateToBeImported);
//		out.write("IMPORTING FROM DELICIOUS: " + nextDelDateToBeImported);
//		out.flush();
//		importOneDate(nextDelDateToBeImported, request, response, out);
//		delDatesAl.remove(0);
//		String dateString = nextDelDateToBeImported.getDate();
//		if (!today.equals(dateString)) {
//			// save only if not today (we could create other docs today)
//			saveInfoAboutSynchro.setLastDeliciousImportDate(dateString);
//		} else {
//			// finished = true; NO! still export to be done!
//		}
//	} else if (doexportonly) {
//		System.out.println("synchroNext exportOneDate " + nextDateToBeExported);
//		// out.write("EXPORTING TO DELICIOUS: " + nextDateToBeExported);
//		// out.flush();
//		System.out.println("synchroNext exportOneDate " + nextDateToBeExported);
//		List docsToBeExported = mod.geDocs(nextDateToBeExported);
//		boolean nodoc = true;
//		if (docsToBeExported != null) {
//			// remove docs that have just been imported
//			for (int i = docsToBeExported.size() - 1; i > -1; i--) {
//				// TODO : remplacer importedList par un HashSet
//				if (this.importedList.contains(docsToBeExported.get(i))) {
//					docsToBeExported.remove(i);
//				}
//			}
//			if (docsToBeExported.size() > 0) {
//				out.write("EXPORTING TO DELICIOUS: " + nextDateToBeExported);
//				out.flush();
//				nodoc = false;
//				exportList(docsToBeExported, request, response, out);
//			}
//		}
//		/*if (nodoc) {
//			out.write("(no document on this day)");
//			out.flush();
//		}*/
//		String dateString = (new YearMonthDay(nextDateToBeExported)).getYearMonthDay("-");
//		if (!today.equals(dateString)) {
//			saveInfoAboutSynchro.setLastDeliciousExportDate(dateString);
//		} else {
//			finished = true;
//		}
//	} else if (doboth) {
//		// no more possible
//		// previously, I was trying following
//		// but problem with the GUI in ajax script
//		// (separate the import and export lists)
//		/*
//		// next date to be exported and imported are the same
//		// take note of sldocs created on nextDelDate to be exported 
//		List docsToBeExported = this.mod.geDocs(nextDateToBeExported);
//		// import
//		importOneDate(nextDelDateToBeImported, request, response, out);
//		// export
//		if ((docsToBeExported != null) && (docsToBeExported.size() > 0)) {
//			exportList(docsToBeExported, request, response, out);
//		}
//		String dateString = nextDelDateToBeImported.getDate();
//		if (!today.equals(dateString)) {
//			saveInfoAboutSynchro.setLastDeliciousImportDate(dateString);
//			saveInfoAboutSynchro.setLastDeliciousExportDate(dateString);
//		} else {
//			finished = true;
//		}
//		*/
//	}
//	if (finished) {
//		out.flush();
//		out.write("<li>SYNCHRO COMPLETED!</li>"); // Attention, cette chaine est utilis�e par delicious.js
//		this.synchroCompleted = true;
//		if (!exportOnly) this.importPostsCompleted = true;
//		this.exportPostsCompleted = true;
//	}
//}
//
//public void exportList(List list, HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception {
//	for (int idoc = list.size()-1; idoc > -1; idoc--) {
//		SLDocument doc = (SLDocument) list.get(idoc);
//		boolean added = toDelicious(doc);
//		if (added) {
//			request.setAttribute("net.semanlink.servlet.jsp.currentdoc", doc);
//			request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", doc.getKeywords());
//			out.print("[Exported:] ");
//			out.flush();
//			this.exportedList.add(doc);
//			String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(doc);
//			request.getRequestDispatcher(docLineJspName).include(request, response);
//			return;
//		}
//	} // for %>
//}
//
////
//// IMPORT ALL AT ONCE
////
//
//private void initAllDelPosts() {
//	if (this.allDelPosts == null) {
//		this.allDelPosts = computeAllDelPosts();
//		this.allDelPostsSize = this.allDelPosts.size();
//	}
//}
//
//private List computeAllDelPosts() {
//	List x;
//	this.deliciousCaller.waitPlease();
//	x = this.delicious.getAllPosts();
//	this.deliciousCaller.done();
//	return x;
//}
//public void importAll() throws Exception {
//	if (this.allDelPosts == null) initAllDelPosts();
//	List newDocs = delPostList2Semanlink(this.allDelPosts, this.locale);
//}
//
///** Import into Semanlink a list of delicious posts.
// *  Posts that already exist are not imported again.
// *  Return the list of new SLDocuments
// * @throws Exception 
// */
//List delPostList2Semanlink(List delPostList, Locale locale) throws Exception {
//	int n = delPostList.size();
//	List x = new ArrayList(n);
//	for (int ipost = 0; ipost < n; ipost++) {
//		SLDocument doc = delPost2Semanlink((Post) delPostList.get(ipost), locale);
//		if (doc != null) x.add(doc);
//	}
//	return x;
//}
//
//
//}
