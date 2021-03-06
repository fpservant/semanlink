/* Created on 5 juin 2004 */
package net.semanlink.semanlink;
/**
 * Représente une liste de valeur de propriétés.
 * Bof //TODO REFAIRE CAR C'EST NULL
 */
public interface PropertyValues {
	int size();
	String getString(int index);
	/** Retourne l'uri de la indexième property, ou null s'il ne s'agit pas d'une resource */
	String getUri(int index);
	/** Si la indexième property n'est pas une resource, retourne son literal */
	String getLiteral(int index);
	/** Si la indexième property n'est pas une resource, retourne son "lang" */
	String getLang(int index);
	/** Retourne la 1ere des valeurs sous forme de String. */
	String getFirstAsString();
}
