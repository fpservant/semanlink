/* Created on 11 oct. 2015 */
package net.semanlink.sljena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import net.semanlink.semanlink.SLResourceAdapter;
import net.semanlink.semanlink.SLVocab;

public class JResource extends SLResourceAdapter {
protected Resource res;
protected JModel jModel;
public JResource(JModel jModel, Resource res) {
  super(res.getURI());
  this.jModel = jModel;
  this.res = res;
}
  
@Override public String getMarkdownUri(String lang) {
	Model m = jModel.getDocsModel();
	ResIterator ite = m.listSubjectsWithProperty(m.createProperty(SLVocab.SL_MARKDOWN_OF_PROPERTY), res);
	String x = null;
  if (ite.hasNext()) {
  	x = (ite.next()).getURI();
  }
  ite.close();
  return x;
}
}
