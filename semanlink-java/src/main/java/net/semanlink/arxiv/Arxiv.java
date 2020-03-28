/* Created on Mar 28, 2020 */
package net.semanlink.arxiv;
public class Arxiv {

// null if url is not the url of an document doc
static public String url2pdfUrl(String url) {
	if (!url.startsWith("https://arxiv.org/")) {
		return null;
	}
	if (url.startsWith("https://arxiv.org/abs/")) {
		return url.replace("https://arxiv.org/abs/", "https://arxiv.org/pdf/") + ".pdf";
	} else if (url.startsWith("https://arxiv.org/pdf/")) {
		return url;
	}
	return null;
}
}
