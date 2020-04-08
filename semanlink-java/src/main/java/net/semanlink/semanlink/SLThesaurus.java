package net.semanlink.semanlink;

import net.semanlink.util.index.LabelGetter;

/**
 * Un ensemble de Keywords.
 *  * CECI DOIT ETRE CHANGE VOIR SLThesaurusAdapter
 */
public interface SLThesaurus extends SLResource {
	
	//
	// WHAT WE HAD B4 2020-04
	//
	
	// stupid -- strictly related to the saving in files
	
	/**Fichier dans lequel, par défaut, on écrit les statements définissant les termes (SLKeywords) du thésaurus.*/
	public String getDefaultFile();
	public void setDefaultFile(String defaultFile);
	default public String getBase() {
		String uri = getURI();
		if (!uri.endsWith("/")) uri += "/";
		return uri;
	}
	
	//
	//
	//
	
	public LabelGetter<SLKeyword> getKwLabelGetter();
}