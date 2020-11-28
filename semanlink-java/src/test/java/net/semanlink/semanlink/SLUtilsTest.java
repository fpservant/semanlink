/* Created on Nov 28, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.util.FileUriFormat;

public class SLUtilsTest {
@Test public final void title2shortFilenameTest() throws Exception {
	System.out.println((int)'«');
	String s = "« ça va allé ô j'aime à+ alors";
	String sfn = SLUtils.title2shortFilename(s);
	System.out.println(sfn);
	System.out.println(URLEncoder.encode(sfn,"UTF-8"));
	
	// from NewBookmarkCreationData:
	// bkmUri = mod.fileToUri(new File(bkmDir, shortFilename));
	// return FileUriFormat.filenameToUri(f.getPath());
	
	String bkmDir = "/Users/fps/Sites/fps/2020/11";
	System.out.println(FileUriFormat.filenameToUri(bkmDir + sfn)); // manque l'histoire du webserver
	
	
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	String bkmDirUrl = contextUrl + "/doc/2020/11/";
	
	URI uri = new URI (bkmDirUrl + sfn);
	String x = uri.toASCIIString();

	System.out.println(x); // that's good
	
	String title = "« ça va allé ô j'aime à+ alors";
	x = SLUtils.title2bookmarkUri(title, bkmDirUrl);
	assertTrue(x.equals("http://127.0.0.1:8080/semanlink/doc/2020/11/%C2%AB_ca_va_alle_o_j_aime_a_alors"));
}


}
