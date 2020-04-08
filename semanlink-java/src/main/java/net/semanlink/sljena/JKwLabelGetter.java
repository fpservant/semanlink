/* Created on Apr 5, 2020 */
package net.semanlink.sljena;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.skos.SKOS;
import net.semanlink.util.index.LabelGetter;

// pff, java doesn't want to implement LabelGetter<SLKeyword> and LabelGetter<Resource>
// at the same time!!! // java sucks

//TODO (?) use Literal (=String + lang) ?
public class JKwLabelGetter implements LabelGetter<SLKeyword> {
	@Override public Iterator<String> getLabels(SLKeyword o) {
		JKeyword kw = (JKeyword) o;
		Resource res = kw.getRes();
		return getLabels(res);
	}
	
	// Override
	public Iterator<String> getLabels(Resource res) {
		Model m = res.getModel();
		ExtendedIterator<RDFNode> x;
		x = m.listObjectsOfProperty(res, SKOS.prefLabel);
		x = x.andThen(m.listObjectsOfProperty(res, SKOS.altLabel));
		ArrayList<String> al = new ArrayList<String>();
		for(;x.hasNext();) {
			RDFNode n = x.next();
			if (n instanceof Literal) { // shouldn't be otherwise, but better to be sure
				al.add(((Literal) n).getString());
			}
		}
		return al.iterator();
	}

}
