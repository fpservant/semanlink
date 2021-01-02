/* Created on 24 sept. 03 */
package net.semanlink.semanlink;
import java.util.*;

/**
 * @author fps
 */
public interface SLResource {
	String getURI();
	String getComment();
	default LabelLN getCommentLN() { // 2021-01
		throw new UnsupportedOperationException();
	}
	String getDate();
	String getMarkdownUri(String lang); // not used actually. Culd be set again find DISPLAY_MARKDOWN_OF_PROPERTY
	/** Retourne une HashMap de cle les Property, et de data une PropertyValues */
	HashMap getProperties();
	HashMap getPropertiesAsStrings();
	/** Retourne une liste des objets de la ppte pptyUri.
	 * ? sous quelle forme ?
	 */
	PropertyValues getProperty(String pptyUri);
	List getPropertyAsStrings(String pptyUri);
	/** Ne retourne jamais qu'une seule valeur (même s'il y en a plus).	 */
	// public String getValue(String pptyUri);
}
