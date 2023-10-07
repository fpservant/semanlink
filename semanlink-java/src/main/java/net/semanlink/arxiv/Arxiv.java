/* Created on Mar 28, 2020 */
package net.semanlink.arxiv;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLDocumentStuff;
import net.semanlink.semanlink.SLModel;

/**
 * url of arxiv documents
 */
public class Arxiv {
static final String ARXIV_NS = "https://arxiv.org/";
static final String ARXIV_NS_OLD = "http://arxiv.org/";
static final String ABS = ARXIV_NS + "abs/";
static final String PDF = ARXIV_NS + "pdf/";
// Bordel, 2013-10 : le pdf est maintenant en https://browse.arxiv.org/pdf/2310.03025.pdf
static final String PDF_2 = "https://browse.arxiv.org/pdf/";
private static final int ABS_LEN = ABS.length();
private static final int PDF_LEN = PDF.length();



private String num;
public Arxiv(String num) {
	this.num = num;
}
public static Arxiv getByUrl(String url) {
	String num = url2num(url);
	if (num == null) return null;
	return new Arxiv(num);
}
public String getNum() { return this.num ; }
public String pdfUrl() {
	return num2pdfUrl(num);
}
public String absUrl() {
	return num2absUrl(num);
}


/**
 * if url is the URL of an arxiv document, return its arxiv num (eg.0807.4145)
 * else return null;
 * @param url the url of a web document (not a semanlink url!), eg. https://arxiv.org/abs/0807.4145
 * or https://arxiv.org/pdf/0807.4145
 * or (since 2023-10) https://browse.arxiv.org/pdf/2310.03025.pdf
 * @return eg.0807.4145, null if url is not the url of an arxiv doc
 */
static public String url2num(String url) {
	if (!url.startsWith(ARXIV_NS)) {
		if (!url.startsWith(ARXIV_NS_OLD)) {
			if (!url.startsWith(PDF_2)) {
				return null;
			}
		} else {
			url = "https:" + url.substring(5);
		}
	}
	String x = null;
	int k = url.indexOf("#");
	if (k > 0) url = url.substring(0,k);
	k = url.indexOf("?");
	if (k > 0) url = url.substring(0,k);
	
	if (url.startsWith(ABS)) {
		x = url.substring(ABS_LEN);
	} else if (url.startsWith(PDF)) {
		x = url.substring(PDF_LEN);
		if (x.endsWith(".pdf")) {
			x = x.substring(0, x.length()-4); // remove .pdf
		}
	} else if (url.startsWith(PDF_2)) { // 2023-10 new url for pdfs
		x = url.substring(PDF_2.length());
		if (x.endsWith(".pdf")) {
			x = x.substring(0, x.length()-4); // remove .pdf
		}
	} else {
		return null;
	}
	// question of the vxx cf. 2003.02320v1
	k = x.indexOf("v");
	if (k>0) {
		x = x.substring(0,k);
	}
	return x;
}

/*
 * @param url the url of a web document (not a semanlink url!), eg. https://arxiv.org/abs/0807.4145
 * or https://arxiv.org/pdf/0807.4145
 * @return the url of the pdf of an arxiv doc
 * null if url is not the url of an arxiv doc
 * Note: "http://.../0807.4145v1" -> http://.../0807.4145.pdf (NO v1)
 */
static public String url2pdfUrl(String url) {
	String num = url2num(url);
	if (num == null) return null;
	return num2pdfUrl(num);
}

private static String num2pdfUrl(String num) {
	// 2023-10 arxiv new pdf url
	// return PDF + num + ".pdf";
	return PDF_2 + num + ".pdf";
}

private static String num2absUrl(String num) {
	return ABS + num;
}

/** null if not an arxiv doc */
static public String sldoc2arxivNum(SLDocument doc, SLModel mod) {
	SLDocumentStuff docstuff = new SLDocumentStuff(doc, mod, null); // pour ce qu'on en a à faire ici, pas besoin du contexte
	try {
		return url2num(docstuff.getHref());
	} catch (Exception e) { throw new RuntimeException(e); }	
}

}
