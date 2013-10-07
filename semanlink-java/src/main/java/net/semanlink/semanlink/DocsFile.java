/* Created on 11 nov. 2005 */
package net.semanlink.semanlink;

import net.semanlink.semanlink.SLModel.LoadingMode;

/** Represents a "sl.rdf" file or a "sl:DataFolder". 
 *  A base is needed to load such a file, and a SLThesaurus when it's about adding data to it.
 *  When it is a sl:DataFolder, there's also the loadingMode
 * 
 *  Classe associant à un fichier de documents un thesaurus à utiliser par défaut.*/
public class DocsFile extends SLFile {
	private SLThesaurus defaultThesaurus;
	private LoadingMode loadingMode;
	public DocsFile(String filename, String base, SLThesaurus defaultThesaurus) {
		super(filename, base);
		// if (defaultThesaurus == null) throw new IllegalArgumentException("DocsFile.NEW : no defaultThesaurus");
		this.defaultThesaurus = defaultThesaurus;
	}
	/** utilisé seulement pour loadingList, où on met abusivement des DocsFile,
	 * et où on veut garder aussi s'il s'agit d'un truc où on ne descend pas en dessous des years/month
	 * @param filename
	 * @param base
	 * @param defaultThesaurus
	 * @param loadingMode
	 */
	public DocsFile(String filename, String base, SLThesaurus defaultThesaurus, LoadingMode loadingMode) {
		this(filename, base, defaultThesaurus);
		this.loadingMode = loadingMode;
	}
	/** Thesaurus utilisé par défaut pour qualifier les documents de cette documentation. */
	public SLThesaurus getDefaultThesaurus() { return this.defaultThesaurus; }
	public void setDefaultThesaurus(SLThesaurus thesaurus) { this.defaultThesaurus = thesaurus; }
	public boolean equals(Object o) {
		return (this.filename.equals(((DocsFile) o).getFilename()));
	}
	public String toString() {
		return this.filename + " base : " + base + " thesaurus: " + defaultThesaurus.getURI();
		// return this.filename; // + " ? " + getDocsFileNameWithLoadingList(new File(this.getFilename()), null);
	}
	LoadingMode getLoadingMode() { return loadingMode; }
}
