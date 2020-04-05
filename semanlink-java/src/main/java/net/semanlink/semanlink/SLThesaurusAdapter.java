package net.semanlink.semanlink;

import net.semanlink.util.index.MultiLabelGetter;

/**
 * Un ensemble de Keywords, associé a une url
 * 
 * CECI DOIT ETRE CHANGE : N'A PAS ETE MODIFIE DEPUIS QU'ON INDIQUE UNE DIR (ET NON
 * PLUS LE FICHIER slkws DANS semanlinkconfig.xml
 * (on continue d'arriver ici avec le fichier slkws
 * voir SLThesaurus loadThesaurus(String defaultThesaurusURI, File defaultThesaurusDir) ds SLModel
 */
public class SLThesaurusAdapter extends SLResourceAdapter implements SLThesaurus {
/** Fichier kws utilisé par défaut. */
private String defaultFile;
private MultiLabelGetter<SLKeyword> kwLabelGetter;

public String getDefaultFile() { return this.defaultFile; }
public void setDefaultFile(String defaultFile) { this.defaultFile = defaultFile; }

public SLThesaurusAdapter(String uri) {
	super(noSlashAtEnd(uri));
}
public SLThesaurusAdapter(String uri, String defaultFile, MultiLabelGetter<SLKeyword> kwLabelGetter) {
	super(noSlashAtEnd(uri));
	this.defaultFile = defaultFile;
	this.kwLabelGetter = kwLabelGetter;
}

static private String noSlashAtEnd(String uri) {
	if (uri.endsWith("/")) return uri.substring(0, uri.length() - 1);
	return uri;
}

@Override public String toString() {
	return this.uri; // + " default file:" + this.defaultFile;
}

@Override public MultiLabelGetter<SLKeyword> getKwLabelGetter() {
	return kwLabelGetter;
}

} // class
