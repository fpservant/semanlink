package net.semanlink.semanlink;
/**
 *  TODO extends SLResource
 */
public class SLDocumentationAdapter implements SLDocumentation {
private String defaultFile;
private SLThesaurus defaultThesaurus;
public SLDocumentationAdapter(String defaultFile, SLThesaurus defaultThesaurus) {
	setDefaultFile(defaultFile);
	setDefaultThesaurus(defaultThesaurus);
}
/**Fichier dans lequel, par défaut, on écrit les statements définissants les termes (SLDocument) de la documentation.*/
public String getDefaultFile() { return this.defaultFile; }
public void setDefaultFile(String defaultFile) { this.defaultFile = defaultFile; }
/** Thesaurus utilisé par défaut pour qualifier les documents de cette documentation. */
public SLThesaurus getDefaultThesaurus() { return this.defaultThesaurus; }
public void setDefaultThesaurus(SLThesaurus thesaurus) { this.defaultThesaurus = thesaurus; }
	
}