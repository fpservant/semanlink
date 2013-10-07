/* Created on 13 sept. 2004 */
package net.semanlink.semanlink;
import glguerin.io.AccentComposer;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author fps
 */
public class SLFolder {
private File dir;
private String uri;
private SLModel mod;
private ArrayList docList;
/**
 * A directory (in  the meaning of the OS), seen as a list of SLDocuments. 
 * 
 * Main purpose of this class is to handle the problems of character encoding in uris.
 * @param dir supposed to be a directory (may howver be served by the web server)
 */
public SLFolder(File dir, String uri, SLModel mod) {
	this.dir = dir;
	this.uri = uri;
	// 2006-01
	if (!uri.endsWith("/")) this.uri += "/";
	this.mod = mod;
}

/** Content of this directory, as a List of SLDocument */
public List getDocList() throws URISyntaxException {
	if (this.docList == null) {
		this.docList = computeDocList();
	}
	return this.docList;
}

private ArrayList computeDocList() throws URISyntaxException {
	boolean fileProtocol = (this.uri.startsWith("file:"));
	String[] list = dir.list();
	ArrayList al = new ArrayList();
	// String encodedUri = URLUTF8Encoder.encode(uri); // TODO RECREER CA : encodage une seule fois de la dir
	for (int i = 0; i < list.length; i++) {
		String sf = list[i];
		// if (sf.equals("sl.rdf")) continue;
		// if (sf.equals("slkws.rdf")) continue;
		// if (sf.endsWith(".rdf")) continue;
		File f = new File(dir, sf);
		if (f.isHidden()) continue;
		try {
			SLDocument doc;
			if (fileProtocol) {
				doc = mod.getDocument(mod.filenameToUri(f.toString()));
			} else {
				if (f.isDirectory()) {
					if (!(sf.endsWith("/"))) sf += "/";
				}
				// doc = mod.getDocument( FileUriFormat.fileToUri(this.uri, AccentComposer.composeAccents(sf))); // AccentComposer 2004-08
				// La situation : uri est correcte, �ventuellement avec des car quot�s, (genre %20)
				// Mais sf n'a pas ses car quot�s. Pb : quoter ces car.
				// Ceci ne marche pas si uri contient, par ex, des %20 (cad si c'est une uri correcte, mais avec des car quot�s)
				// � cause d'un pb ds laxistRelativPath2Uri sur ces cas (voir laxistRelativPath2Uri)
				// doc = mod.getDocument( SLUtils.laxistRelativPath2Uri(this.uri, AccentComposer.composeAccents(sf))); // AccentComposer 2004-08
				// d'o� (2006/01) ce hack pour quoter les car de sf : utiliser SLUtils.laxistRelativPath2Uri avec une uri sans car quot�s
				/* String s = SLUtils.laxistRelativPath2Uri("http://www.semanlink.net/", AccentComposer.composeAccents(sf)); // AccentComposer 2004-08
				s = s.substring("http://www.semanlink.net/".length());
				// je me suis assur� � la construction que uri est "/" terminated
				s = this.uri + s; */
				String s = SLUtils.notQuotedToQuotedUriRelativPath(AccentComposer.composeAccents(sf)); // AccentComposer 2004-08
				// je me suis assur� � la construction que uri est "/" terminated
				s = this.uri + s;
				doc = mod.getDocument(s);
			}
			al.add(doc);
		} catch (Exception e) {
			e.printStackTrace();
			// System.err.println("BAD URI : " + sf);
		}
	}
	return al;
}
}
