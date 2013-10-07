/* Created on 1 oct. 03 */
package net.semanlink.semanlink;
/*
 * 
 * TODO : getLabel ds kw et doc doit etre impl?mnt? ds model : 
 * @author fps
 */
public interface SLLabeledResource extends SLResource {
	/**
	 * @see SLUtils.getLabel(SLDocument) to handle case where this.getLabel() returns null */
	public String getLabel();
	/** Returns the label corresponding to language, if such a label exists.
	 *  If not, it's up to the application to decide whether it returns another label or null. */
	public String getLabel(String language);
}
