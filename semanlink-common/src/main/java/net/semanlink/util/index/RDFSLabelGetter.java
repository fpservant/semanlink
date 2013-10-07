/* Created on 12 juil. 2010 */
package net.semanlink.util.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RDFSLabelGetter implements MultiLabelGetter<Resource> {
private String lang;
public RDFSLabelGetter(String lang) { this.lang = lang; }
public Iterator<String> getLabels(Resource res) {
	ArrayList<String> al = new ArrayList<String>();
	NodeIterator it = res.getModel().listObjectsOfProperty(res, RDFS.label);
	nodeIteratorIntoListOfLabels(it, al);
	return al.iterator();
}
private void nodeIteratorIntoListOfLabels(NodeIterator it, List<String> al) {
	for(;it.hasNext();) {
		Object node = it.next();
		if (node instanceof Literal) {
			Literal lit = (Literal) node;
			if (lang != null) {
				if (lang.equals(lit.getLanguage())) {
					al.add(lit.getString());
				}
			} else {
				al.add(lit.getString());
			}
		}
	}
}
}
