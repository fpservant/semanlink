/* Created on 1 oct. 03 */
package net.semanlink.semanlink;

public interface SLLabeledResource extends SLResource {
	/**
	 * @see SLUtils.getLabel(SLDocument) to handle case where this.getLabel() returns null */
	String getLabel();
	/** Returns the label corresponding to language, if such a label exists.
	 *  If not, it's up to the application to decide whether it returns another label or null. */
	String getLabel(String language);
	
	/** @since 2021-01 */
	LabelLN getLabelLN();
}
