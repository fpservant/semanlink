/* Created on 7 janv. 08 */
package net.semanlink.graph.jena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/** Definition of a tree in a jena graph*/
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

/**
 * The TreeDefinition is based on the listing of the properties that must be considered as "parent-child", "child-parent" and "node-leaf"
 * @param mod jena model
 * @param schildProps the properties to be used as "parent - child" properties
 * @param sparentProps  the properties to be used as "child - parent" properties
 * @param sleafProps the properties that link a node of the tree to a leaf
 * @param sinvLeafProps the properties that link a leaf to a node
 */
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
