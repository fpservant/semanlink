package net.semanlink.semanlink;

import java.io.File;

/**
 * Un ensemble de Keywords, associé a une url
 * 
 * CECI DOIT ETRE CHANGE : N'A PAS ETE MODIFIE DEPUIS QU'ON INDIQUE UNE DIR (ET NON
 * PLUS LE FICHIER slkws DANS semanlinkconfig.xml
 * (on continue d'arriver ici avec le fichier slkws
 * voir SLThesaurus loadThesaurus(String defaultThesaurusURI, File defaultThesaurusDir) ds SLModel
 */
public class SLThesaurusAdapter extends SLResourceAdapter implements SLThesaurus {
public static final long serialVersionUID = 1;
/** Fichier kws utilisé par défaut. */
private String defaultFile;
public String getDefaultFile() { return this.defaultFile; }
public void setDefaultFile(String defaultFile) { this.defaultFile = defaultFile; }

public SLThesaurusAdapter(String uri) {
	super(noSlashAtEnd(uri));
}
public SLThesaurusAdapter(String uri, String defaultFile) {
	super(noSlashAtEnd(uri));
	this.defaultFile = defaultFile;
}

static private String noSlashAtEnd(String uri) {
	if (uri.endsWith("/")) return uri.substring(0, uri.length() - 1);
	return uri;
}

/** avec # ou / à la fin */ // #thing
public String getBase() {
	/*if (withSharpTagUrls) return this.uri + "#";
	else return this.uri + "/";*/
	return this.uri + "/";
}

/** serait à améliorer TODO */
public boolean equals(Object o) {
	return this.uri.equals(((SLThesaurus) o).getURI());
}

public String toString() {
	return this.uri; // + " default file:" + this.defaultFile;
}

} // class
