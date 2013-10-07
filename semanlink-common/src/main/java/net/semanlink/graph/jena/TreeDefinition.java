/* Created on 7 janv. 08 */
package net.semanlink.graph.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

public class TreeDefinition {
// je le garde à la fois sous forme de string et de props, parce qu'on a besoin
// des 1eres ds js, et des secondes en java
private String[] schildProps;
private String[] sparentProps;
private String[] sleafProps;
private String[] sinvLeafProps;
private Property[] childProps;
private Property[] parentProps;
private Property[] leafProps;
private Property[] invLeafProps;


public TreeDefinition(Model mod, String[] schildProps, String[] sparentProps, String[] sleafProps, String[] sinvLeafProps) {
	this.schildProps = schildProps;
	this.sparentProps = sparentProps;
	this.sleafProps = sleafProps;
	this.sinvLeafProps = sinvLeafProps;

	childProps = s2p(schildProps, mod);
	parentProps = s2p(sparentProps, mod);
	leafProps = s2p(sleafProps, mod);
	invLeafProps = s2p(sinvLeafProps, mod);
}

static private Property[] s2p(String[] s, Model mod) {
	if (s != null) {
		Property[] x = new Property[s.length];
		for (int i = 0 ; i < s.length; i++) {
			x[i] = mod.createProperty(s[i]);
		}
		return x;
	} else {
		return null;
	}
}
/*
public TreeDefinition(Property[] childProps, Property[] parentProps, Property[] leafProps, Property[] invLeafProps) {
	this.childProps = childProps;
	this.parentProps = parentProps;
	this.leafProps = leafProps;
	this.invLeafProps = invLeafProps;
}
*/

public Property[] getChildProps() {
	return childProps;
}

public Property[] getParentProps() {
	return parentProps;
}

public Property[] getLeafProps() {
	return leafProps;
}

public Property[] getInvLeafProps() {
	return invLeafProps;
}

public String[] getChildPropsAsString() {
	return schildProps;
}

public String[] getParentPropsAsString() {
	return sparentProps;
}

public String[] getLeafPropsAsString() {
	return sleafProps;
}

public String[] getInvLeafPropsAsString() {
	return sinvLeafProps;
}

}
