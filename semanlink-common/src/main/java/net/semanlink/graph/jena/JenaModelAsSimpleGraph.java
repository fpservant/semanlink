/* Created on Sep 12, 2006 */
package net.semanlink.graph.jena;

import java.util.ArrayList;
import java.util.Iterator;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Allows for using a Jena Model (and one of its properties) as a net.semanlink.semanlink.graph.Graph. 
 * 
 * @see JenaModelAsGraph if you need to use more than one property to define the graph. 
 */
public class JenaModelAsSimpleGraph implements Graph<RDFNode> {
private static final Iterator<RDFNode> nada = (new ArrayList<RDFNode>(0)).iterator();
protected Model mod;
protected Resource[] seeds;
protected Property neighborProp;
public JenaModelAsSimpleGraph(Model mod, Resource[] seeds, Property neighborProp) {
    this.mod = mod;
    this.seeds = seeds;
    this.neighborProp = neighborProp;
}
@Override public Iterator<RDFNode> getNeighbors(RDFNode node) throws Exception {
	if (!(node instanceof Resource)) return nada;
  Resource res = (Resource) node;
  return mod.listObjectsOfProperty(res, this.neighborProp);
}
@Override public Resource[] seeds() { return this.seeds; }
public Model getModel() { return this.mod; }
public void print() throws Exception {
    (new GraphTraversal<RDFNode>(this)).print();
}
}
