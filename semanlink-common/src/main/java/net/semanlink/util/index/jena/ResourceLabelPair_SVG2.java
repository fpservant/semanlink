/* Created on 12 juil. 2010 */
package net.semanlink.util.index.jena;

import net.semanlink.util.index.ObjectLabelPair;

import com.hp.hpl.jena.rdf.model.Resource;

/** Represents a pair (resource, label). 
 *  The label is just a String: doesn't contain lang information. */
public class ResourceLabelPair_SVG2 extends ObjectLabelPair<Resource> { // implements Comparable<ResourceLabelPair> {
public ResourceLabelPair_SVG2(Resource res, String label) {
	super(res, label);
}
public Resource getResource() { return getObject(); }
}
