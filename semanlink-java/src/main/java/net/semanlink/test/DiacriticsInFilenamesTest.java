package net.semanlink.test;
import java.io.*;
import java.net.*;

/** Shows a problem when trying to access files containing certain sequences of non ASCII characters. 
 * 
 *  You can access "éé.html" using a filename using "composed accents" (such as you get with
 *  a string constant), but you can't access "ééé.html".
 *  
 *  To run the test, pass to main a directory containing one file called "éé.html" and another called "ééé.html".
 */
public class DiacriticsInFilenamesTest {

/** Pass as sole arg a dir containing one file called "éé.html" and another called "ééé.html". */
public static void main(String[] args) {
	new DiacriticsInFilenamesTest(args[0]);
}

/** Pass a dir containing one file called "éé.html" and another called "ééé.html". */
public DiacriticsInFilenamesTest(String dirPath) {
	try {
		test(dirPath);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

void test(String dirPath) throws UnsupportedEncodingException {
	File dir = new File(dirPath);
	String[] names = dir.list();
	for (int i = 0; i < names.length; i++) {
		testOne("Short name as returned by dir.list() (non composed accents)", dir, names[i], false);
	}
	testOne("Short name as a java constant String (composed accents)", dir, "éé.html", true);
	testOne("Short name as a java constant String (composed accents)", dir, "ééé.html", true);
}

void testOne(String mess, File dir, String shortName, boolean accentsAreComposed) throws UnsupportedEncodingException {
		File file = new File(dir, shortName);
		if (file.isHidden()) return;

		System.out.println("\n****** TESTING " + mess +": " + shortName);
		System.out.println("short name: " + shortName);
		System.out.println("short name as nums: " + toNums(shortName));
		String s = URLEncoder.encode(shortName,"UTF-8");
    System.out.println("URLEncoder.encode(shortName,UTF-8): "+s);
		System.out.println("filename: " + file.getAbsolutePath());
		boolean b = file.exists();
		if (b) {
			System.out.println("File.exists: " + b);
		} else {
			System.out.println("File.exists: *** " + b + "***");
		}
}

private String toNums(String s) {
	StringBuffer x = new StringBuffer();
  for (int j = 0; j < s.length(); j++) {
    x.append( (int) s.charAt(j) + " " );
  }
  return x.toString();
}
} // 


