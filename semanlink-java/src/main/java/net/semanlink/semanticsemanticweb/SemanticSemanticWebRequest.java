/* Created on 20 mai 07 */
package net.semanlink.semanticsemanticweb;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.*;

import net.semanlink.semanlink.SLModel;
import net.semanlink.servlet.SLServlet;
import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;

public class SemanticSemanticWebRequest {
public static String PROBABLY_SAME_AS_PROP_URI = "http://www.semanlink.net/semanticsemanticweb#probablySameAs";
public static String DELICIOUS_TAG_PAGE_PROP_URI = "http://www.semanlink.net/semanticsemanticweb#deliciousTagPage";
static public CharConverter converter = new CharConverter(Locale.getDefault());
private static DBPedia DBPEDIA = new DBPedia();
private Model mod;
private Resource subject;
private Property probablySameAsProp;
private Property deliciousTagPageProp;
private HttpServletRequest req;
private HttpServletResponse res;
private String tagLabel;
private String lang;
/** the words in tagLabel. */
private ArrayList words;
private Locale locale;
SemanticSemanticWebRequest(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
	this.req = req;
	this.res = res;
	// THERE IS A PROBLEM WHEN HANDLING GET FORMS (at least until tomcat 5.5 included)
	// With a GET form, typing "Antiquité grecque" in the input field
	// the url in the browser looks like:
	// http://127.0.0.1:9080/semanlink/sl/semanticsemanticweb?s=antiquit%C3%A9+grecque
	this.tagLabel = req.getParameter("s"); // antiquité grecque
	// System.out.println("req.getParameter(s): " + tagLabel);
	// NOTE: I had this problem with semanlink, and I had found a workaround:
	// sending the form with a javascript such as:
	/*
		function semanticSemanticWebSubmit() {
			var url = "<%= request.getContextPath() + "/sl/semanticsemanticweb"%>";
			url = url + "?s=" + encodeURIComponent(encodeURIComponent(document.forms.semanticsemanticweb.s.value));
			window.location = url;
			return false;
		}
	*/
	// This is not OK here: I want to have the good uri generated by the form
	// (as we will use it as subject)
	// And I should change what I did in Semanlink with what I do here
	// (if it is actually possible: it is maybe not the case
	// for GET generated by the bookmarklet)
	if (this.tagLabel != null) {
		// this.tagLabel = URLDecoder.decode(this.tagLabel,"UTF-8"); // antiquité grecque
		this.tagLabel = URLUTF8Encoder.decode(this.tagLabel); // antiquité grecque
	}

	this.lang = req.getParameter("lang");
	if (lang != null) {
		if (lang.length() == 2) {
			lang = lang.toLowerCase();
			this.locale = new Locale(lang);
		} else {
			lang = null;
		}
	}
	if (this.locale == null)	this.locale = Locale.getDefault();
	
	WordsInString wordsInString = new WordsInString(false, true);
	this.words = wordsInString.words(this.tagLabel, this.locale);
	
	this.mod = ModelFactory.createDefaultModel();
	
	// SUBJECT URI : more or less, the uri of the request
	this.subject = this.mod.createResource(getSubjectURI(req));
	this.probablySameAsProp = this.mod.createProperty(PROBABLY_SAME_AS_PROP_URI);
	this.mod.add(this.subject, this.mod.createProperty(RDFS.label.getURI()), tagLabel, lang);
	this.deliciousTagPageProp = this.mod.createProperty(DELICIOUS_TAG_PAGE_PROP_URI);
}

Model getModel() { return this.mod; }

/**
 * Calculates the uri for the subject - more or less, the uri of request.
 * @throws UnsupportedEncodingException 
 */
private String getSubjectURI(HttpServletRequest request) throws UnsupportedEncodingException {
	// In the 2 following solutions, we get the "okBtn=OK" param from the form that we actually do not want
	// and also a useless "lang=" if no lang is specified.
	// String x = req.getRequestURL() + "?" + req.getQueryString(); // http://127.0.0.1:9080/semanlink/sl/semanticsemanticweb?s=antiquit%C3%A9+grecque&lang=&okBtn=OK
	// String x = getRequestURI(req); // http://127.0.0.1:9080/semanlink/sl/semanticsemanticweb?okBtn=OK&s=antiquit%C3%A9+grecque&lang=
	StringBuffer sb = new StringBuffer(request.getRequestURL());
	sb.append("?s=");
	sb.append(encode(this.tagLabel));
	if (this.lang != null) {
		sb.append("&lang=");
		sb.append(this.lang);
	}
	return sb.toString();
}

/* Pour une uri venant d'une get form */
private static String getRequestURI(HttpServletRequest request) throws UnsupportedEncodingException {
	StringBuffer sb = new StringBuffer(request.getRequestURL());
	Enumeration e = request.getParameterNames();
	if (e.hasMoreElements()) {
		sb.append("?");
		String param = (String) e.nextElement() ;
		String[] vals = request.getParameterValues(param);
		sb.append(param);
		sb.append("=");
		sb.append(vals[0]);
		for (int i = 1; i < vals.length; i++) {
			sb.append("&");
			sb.append(param);
			sb.append("=");
			// sb.append(vals[i]);
			sb.append(decodeEncode(vals[i]));
		}
		for (;e.hasMoreElements();) {
			param = (String) e.nextElement() ;
			vals = request.getParameterValues(param);
			for (int i = 0; i < vals.length; i++) {
				sb.append("&");
				sb.append(param);
				sb.append("=");
				// sb.append(vals[i]);
				sb.append(decodeEncode(vals[i]));
			}
		}
	}
	return sb.toString();
}

/** 
 * @param paramVal parameter value from a get form for instance // antiquité grecque */
private static String decodeEncode(String paramVal) throws UnsupportedEncodingException {
	// paramVal = // antiquité grecque
	String x = URLUTF8Encoder.decode(paramVal); // antiquit� grecque
	return encode(x); // 
}

/** @param s string (if read from a parameter value of get form, must be decoded first). For insta,ce "Antiquit� grecque"
 * @throws UnsupportedEncodingException */
private static String encode(String s) throws UnsupportedEncodingException {
	// return URLUTF8Encoder.encode(x); // space -> %20 : antiquit%C3%A9%20grecque
	return URLEncoder.encode(s,"UTF-8"); // space -> + : antiquit%C3%A9+grecque
}

void handleSemanlink() {
	SLModel slMod = SLServlet.getSLModel();
	// String shortTagUri = slMod.kwLabel2ShortUri(tagLabel, locale);
	String tagUri = slMod.kwLabel2ExistingKwUri(tagLabel, locale); // uri of an existing tag, or null
	if (tagUri != null) {
		this.mod.add(this.subject, this.probablySameAsProp, this.mod.createResource(tagUri));
	}
}


void handleDelicious() {
	String ns = "http://del.icio.us/tag/";
	String tagUri;
	int nbWords = this.words.size();
	if (nbWords == 1) {
		tagUri = ns + converter.urlConvert((String) this.words.get(0));
		this.mod.add(this.subject, this.deliciousTagPageProp, this.mod.createResource(tagUri));
	} else if (nbWords > 1) {
		String word = (String) this.words.get(0);
		word = converter.urlConvert(word);
		StringBuffer sb1 = new StringBuffer(word);
		StringBuffer sb2 = new StringBuffer(word);
		for (int i = 1 ; i < nbWords; i++) {
			word = (String) this.words.get(i);
			word = converter.urlConvert(word);
			sb1.append("_");
			sb1.append(word);
			sb2.append(word);
		}
		tagUri = ns + sb1.toString();
		this.mod.add(this.subject, this.deliciousTagPageProp, this.mod.createResource(tagUri));
		tagUri = ns + sb2.toString();
		this.mod.add(this.subject, this.deliciousTagPageProp, this.mod.createResource(tagUri));
	} else {
		throw new IllegalArgumentException("No words in this.tagLabel");
	}
}

void handleDbPedia() {
	if (("en".equals(this.lang)) || (this.lang == null)) {
		String tagUri = DBPEDIA.getResourceURI(words, "en");
		this.mod.add(this.subject, this.probablySameAsProp, this.mod.createResource(tagUri));
	} else {
		 throw new RuntimeException(this.lang + " not supported yet");
	}
}

}
