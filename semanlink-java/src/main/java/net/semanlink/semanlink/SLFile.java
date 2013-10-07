/* Created on 11 nov. 2005 */
package net.semanlink.semanlink;
/** Un nom de fichier de metadata et la base qui va avec. */
class SLFile implements Comparable {
	protected String filename;
	protected String base;
	public SLFile(String filename, String base) {
		this.filename = filename;
		this.base = base;
	}
	public String getFilename() { return this.filename; }
	public String getBase() { return this.base; }
	public String toString() { return this.filename; }

	public int compareTo(Object o) {
		return this.filename.compareTo(((SLFile)o).filename);
	} 
}

