package net.semanlink.semanlink;
/**
 * Un ensemble de documents.
 */
public interface SLDocumentation {
	/**Fichier dans lequel, par défaut, on écrit les statements définissants les termes (SLDocument) de la documentation.*/
	public String getDefaultFile();
	public void setDefaultFile(String defaultFile);
	/** Thesaurus utilisé par défaut pour qualifier les documents de cette documentation. */
	public SLThesaurus getDefaultThesaurus();
	public void setDefaultThesaurus(SLThesaurus thesaurus);
}