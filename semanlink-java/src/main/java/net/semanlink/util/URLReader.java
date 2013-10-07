/* Created on 4 mars 2005 */
package net.semanlink.util;

import java.io.*;
import java.net.*;
/**
 * BOF BOF :pb de l'encoding. Mieux traité dans HTMLDownload
 */
public class URLReader {

public URLReader(URL url, File outFile) throws IOException {
    this(url, new BufferedOutputStream(new FileOutputStream(outFile)));
}

public URLReader(URL url, OutputStream out) throws IOException {
	URLConnection yc = url.openConnection();
    BufferedInputStream in = new BufferedInputStream(yc.getInputStream());
    writeIn2Out(in, out);
	in.close();
	
	// HTMLDocument htmlDocument = HTMLDocumentLoader.loadDocument(url);
}


public class URLConnectionReader {
}

public static void main(String[] args) throws Exception {
	String url = "http://www.blogmarks.net/my/edit.php?id=77436";
	new URLReader(new URL(url), new File("/Users/fps/_fps/2005/09/blogmarks.net_post_result.htm"));
}



public static void writeIn2Out(InputStream in, OutputStream out) throws IOException {
	writeIn2Out(in, out, new byte[1024]);
}
/** écrit in sur out
@param buffer sert à bufferiser le transfert. Passer un byte[] de dimension qui semble raisonnable. */
public static void writeIn2Out(InputStream in, OutputStream out, byte[] buffer) throws IOException {
	int c;
	int len = buffer.length;
	while ((c = in.read(buffer,0,len)) > -1) {
		out.write(buffer,0,c);
	}
}

}
