/* Created on Mar 28, 2020 */
package net.semanlink.arxiv;
public class Arxiv {
static final String ARXIV_NS = "https://arxiv.org/";
static final String ABS = ARXIV_NS + "abs/";
static final String PDF = ARXIV_NS + "pdf/";
private static final int ABS_LEN = ABS.length();
private static final int PDF_LEN = PDF.length();

/*
 * Note: "http://.../0807.4145v1" -> http://.../0807.4145.pdf (NO v1)
 * null if url is not the url of an arxiv doc
 */
static public String url2pdfUrl(String url) {
//	if (!url.startsWith(ARXIV_NS)) {
//		return null;
//	}
//	if (url.startsWith(ABS)) {
//		return url.replace(ABS, PDF) + ".pdf";
//	} else if (url.startsWith(PDF)) {
//		return url;
//	}
//	return null;
	String num = url2num(url);
	if (num == null) return null;
	return PDF + num + ".pdf";
}

/**
 * @return eg.0807.4145, null if url is not the url of an arxiv doc
 */
static public String url2num(String url) {
	if (!url.startsWith(ARXIV_NS)) {
		return null;
	}
	String x = null;
	if (url.startsWith(ABS)) {
		x = url.substring(ABS_LEN);
	} else if (url.startsWith(PDF)) {
		x = url.substring(PDF_LEN);
		x = x.substring(0, x.length()-4); // remove .pdf	
	} else {
		return null;
	}
	// question of the vxx cf. 2003.02320v1
	int k = x.indexOf("v");
	if (k>0) {
		x = x.substring(0,k);
	}
	return x;
}
}
