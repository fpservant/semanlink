/* Created on 5 juin 2004 */
package net.semanlink.sljena;
import net.semanlink.semanlink.*;
import org.apache.jena.rdf.model.*;
import java.util.*;
/**
 * BOF //TODO
 */
public class JPropertyValues implements PropertyValues {
private ArrayList al;
public JPropertyValues() {
	al = new ArrayList(3);
}
/** o doit être un RDFNode */
public void add(Object o) { this.al.add(o); }
public int size() { return this.al.size(); }

//
// implements PropertyValues
//
public String getString(int index) {
	return al.get(index).toString();
}

/** Retourne l'uri de la indexième property, ou null s'il ne s'agit pas d'une resource */
public String getUri(int index) {
	Object o = al.get(index);
	if (o instanceof Resource) return ((Resource) o).getURI() ;
	return null;
}
/** Si la indexième property n'est pas une resource, retourne son literal */
public String getLiteral(int index) {
	Object o = al.get(index);
	if (o instanceof Literal) return ((Literal) o).getString() ;
	return null;
}
/** Si la indexième property n'est pas une resource, retourne son "lang" */
public String getLang(int index) {
	Object o = al.get(index);
	if (o instanceof Literal) return ((Literal) o).getLanguage() ;
	return null;
}
/** Retourne la 1ere des valeurs sous forme de String. */
public String getFirstAsString() {
	if (al.size() > 0) {
		return al.get(0).toString() ;
	}
	return null;
}

//
//
//

}