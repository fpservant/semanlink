/* Created on 12 juil. 2010 */
package net.semanlink.util.index.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.semanlink.util.index.MultiLabelGetter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

public class RDFSLabelGetter implements MultiLabelGetter<Resource> {
private String lang;

public RDFSLabelGetter(String lang) {
	this.lang = lang;
}

public Iterator<String> getLabels(Resource res) {
	ArrayList<String> al = new ArrayList<String>();
	NodeIterator it = res.getModel().listObjectsOfProperty(res, RDFS.label);
	nodeIteratorIntoListOfLabels(it, al);
	return al.iterator();
}

private void nodeIteratorIntoListOfLabels(NodeIterator it, List<String> al) {
	if (lang == null) {
		for(;it.hasNext();) {
			Object node = it.next();
			if (node instanceof Literal) {
				Literal lit = (Literal) node;
				al.add(lit.getString());
			}
		}
	} else {
		for(;it.hasNext();) {
			Object node = it.next();
			if (node instanceof Literal) {
				Literal lit = (Literal) node;
				if (lang.equals(lit.getLanguage())) {
					al.add(lit.getString());
				}
			}
		}
	}
}
}
