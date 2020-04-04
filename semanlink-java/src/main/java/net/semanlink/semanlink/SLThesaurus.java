package net.semanlink.semanlink;
/**
 * Un ensemble de Keywords.
 *  * CECI DOIT ETRE CHANGE VOIR SLThesaurusAdapter
 */
public interface SLThesaurus extends SLResource {
	/**Fichier dans lequel, par défaut, on écrit les statements définissants les termes (SLKeywords) du thésaurus.*/
	public String getDefaultFile();
	public void setDefaultFile(String defaultFile);
	/** avec / : en fait, this.uri+"/"*/ // #thing todo
	public String getBase();
}