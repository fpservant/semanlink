package net.semanlink.test;
import glguerin.io.AccentComposer;
import java.io.*;
import java.net.*;

/** Shows a problem when trying to create from a file a string uri fully compliant with RFC 2396 
 * (that is, limited to US ASCII), and then trying to get back to the file. 
 * 
 *  When using the composed form for "é" (e acute), such as returned by URI.toASCIIString
 *  (or simply from a java string constant):
 *  everything is allright with a file called "éé.html", but fails with "ééé.html" :
 *  
 *  Used in an HTML HREF tag, "%C3%A9%C3%A9.html" gives access to "éé.html", while
 *  %C3%A9%C3%A9%C3%A9.html doesn't give access to "ééé.html"
 *  (tested with Apache and Firefox with file protocl urls)
 *  You may also note that 
 *  
 *  Run the test and look at the results. When you see a line such as:
 *  "Back to file, file.exists(): false",
 *  the generated String uri cannot be used to get to the file 
 *  
 *  If you don't have glguerin.io.AccentComposer (just google to find it), you may remove corresponding calls
 *  (just used here to give more light about the problem)
 */
public class File2Uri2FileTest {

/** Pass as sole arg a dir containing one file called "éé.html" and another called "ééé.html". */
public static void main(String[] args) {
	new File2Uri2FileTest(args[0]);
}

/** Pass a dir containing one file called "éé.html" and another called "ééé.html". */
public File2Uri2FileTest(String dirPath) {
	try {
		testURI(dirPath);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

void testURI(String dirPath) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
	File dir = new File(dirPath);
	String[] names = dir.list();
	for (int i = 0; i < names.length; i++) {
		testOne("Short name as returned by dir.list()", dir, names[i], false);
	}
	testOne("Short name as a keyed String", dir, "éé.html", true);
	testOne("Short name as a keyed String", dir, "ééé.html", true);
}

void testOne(String mess, File dir, String shortName, boolean accentsAreComposed) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
		File file = new File(dir, shortName);
		if (file.isHidden()) return;

		System.out.println("\n****** TESTING " + mess +": " + shortName);
		System.out.println("short name: " + shortName);
		System.out.println("short name as nums: " + toNums(shortName));
		String s = URLEncoder.encode(shortName,"UTF-8");
    System.out.println("URLEncoder.encode(shortName,UTF-8): "+s);
		System.out.println("filename: " + file.getAbsolutePath());
		System.out.println("File.exists: " + file.exists());

		URI uri = file.toURI();
		System.out.println("\nuri: " + uri);
		File f = new File(uri);
		System.out.println("Back to file from uri.toString(), file.exists(): "+ f.exists());
		System.out.println("\nuri.toASCIIString(): " + uri.toASCIIString());
		f = new File(new URI(uri.toASCIIString()));
		System.out.println("Back to file from uri.toASCIIString(), file.exists(): "+ f.exists());
		
		if (!accentsAreComposed) {
			System.out.println("\nShort name converted to ASCII without composing accents (as URI.toASCIIString does)");
			s = URLEncoder.encode(shortName,"UTF-8");
			// there must be a better way to do that:
			URL url = new URL(uri.toURL(),s);
			uri = url.toURI();
			System.out.println("uri2: " + uri);
			f = new File(uri);
			System.out.println("Back to file from uri.toString(), file.exists(): "+ f.exists());
		}
		
		s = AccentComposer.composeAccents(shortName);
		System.out.println("\nShortname modified with AccentComposer");
		file = new File(dir, s);
		uri = file.toURI();
		System.out.println("uri3: " + uri);
		f = new File(uri);
		System.out.println("Back to file from uri.toString(), file.exists(): "+ f.exists());
}

private String toNums(String s) {
	StringBuffer x = new StringBuffer();
  for (int j = 0; j < s.length(); j++) {
    x.append( (int) s.charAt(j) + " " );
  }
  return x.toString();
}
} // 


