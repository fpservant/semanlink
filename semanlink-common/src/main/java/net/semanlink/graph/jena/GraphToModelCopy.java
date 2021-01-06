/* Created on Sep 12, 2006 */
package net.semanlink.graph.jena;

import java.util.Stack;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.WalkListener;

import org.apache.jena.rdf.model.*;

/** Implements the copy of a Graph into a Jena Model. 
 * 
 *  <p>Assumes that the situation is as follows: a Graph is defined through statements of a given model,
 *  (possibly using several properties for the parent-child link)
 *  and you want to create a new model representing this graph, with just the needed statements,
 *  and using a given property for the parent-child link.</p>
 *  <p>Assumes that the source graph is made of Resource.</p>
 */
public class GraphToModelCopy implements WalkListener<Resource> {
	
public static final String SEPARATOR_IN_POSITION = ".";
protected Graph<Resource> sourceGraph;
/** the Model to be constructed. */
protected Model mod;
protected Property hasNeighborProp;
/** used and modified by graphTraversal.depthFirstWalk. */
protected Stack<Resource> stack;
/** not null to create "position statements" */
protected Property positionProp;
/** used and modified by graphTraversal.depthFirstWalk, if we want to add "position statements" (else null) */
protected Stack<Integer> treePosition; 

public GraphToModelCopy(Graph<Resource> sourceGraph, Model destModel, String neighborPropInDestModel) {
	this.sourceGraph = sourceGraph;
	this.mod = destModel;
	this.hasNeighborProp = destModel.createProperty(neighborPropInDestModel);
  this.stack = new Stack<Resource>();
}

/** idée d'ajouter une info de position sous forme de statements. Not used */
GraphToModelCopy(Graph<Resource> sourceGraph, Model destModel, String neighborPropInDestModel, String positionPropInDestModel) {
	this(sourceGraph, destModel, neighborPropInDestModel);
	if (positionPropInDestModel != null) {
		this.positionProp = destModel.createProperty(positionPropInDestModel);
		this.treePosition = new Stack<Integer>();
	}
}



/**
 * Each link of sourceGraph is converted to a statement using a given property of the dest model. 
 * 
 * Adds to destModel statements for each link in sourceGraph.
 * @param sourceGraph the source graph supposed to be made of jena Resources
 * @param destModel the destination model
 * @param neighborPropInDestModel uri of the property for the links in the destination model
 * @throws Exception
 */
public void doCopy() throws Exception {
  GraphTraversal<Resource> graphTraversal = new GraphTraversal<Resource>(sourceGraph);
  graphTraversal.depthFirstWalk(this, stack, treePosition);
 // UTILISER HashSet graphTraversal.getNodes() pour ajouter des statements au sujet des res
 // (voire pour créer tous les statements)
}


//
// GraphTraversal.WalkListenerAdapter
//

public void startSeed(Resource node) throws Exception { handleNode(node); }
public void startNode(Resource node) throws Exception { handleNode(node); }
public void repeatNode(Resource node) throws Exception { handleNode(node); } // aussi pour repeat, parce que le parent est différent
protected void handleNode(Object node) throws Exception {
		Resource res = mod.createResource(((Resource) node).getURI());
		handleRes(res);
}
/**
 * @param res the currently handled resource, in destination model.
 * @throws Exception
 */
protected void handleRes(Resource res) throws Exception {
	if(stack.size() > 0) {
    Resource parent = this.stack.lastElement();
    // the parent - child statement
    mod.add(mod.createResource(parent.getURI()), hasNeighborProp, res);
	}
	if (treePosition != null) {
    mod.add(res, positionProp, positionToString());			
	}
}



protected String positionToString() {
  int n = treePosition.size();
  StringBuilder sb = new StringBuilder(n*2);
  for (int i = 0; i < n; i++) {
      sb.append(treePosition.get(i));
      sb.append(SEPARATOR_IN_POSITION);
  }
  return sb.toString();
}


}






