/* Created on 23 juil. 07 */
package net.semanlink.graph;

import java.util.HashSet;
import java.util.Iterator;

public class Intersection<NODE> {
private HashSet<NODE> nodes1;
private Graph<NODE> g2;
private HashSet<NODE> intersectHS;
private HashSet<NODE> partialHS2; // won't contain all nodes from g2

/** Intersection between 2 graphs */
public Intersection(Graph<NODE> g1, Graph<NODE> g2) throws Exception {
	this((new GraphTraversal<NODE>(g1)).getNodes(), g2);
}

/** Intersection between a set of nodes and the nodes in a graph */
public Intersection(HashSet<NODE> nodes1, Graph<NODE> g2) {
	this.nodes1 = nodes1;
	this.g2 = g2;
}

/** if result has not been computed yet, makes a simpleWalk.
 *  (if you don't want computing, use this.hs) */
public HashSet<NODE> getNodes() throws Exception {
    if (this.intersectHS == null) doIt();
    return this.intersectHS ;
}

private void doIt() throws Exception {
	if (nodes1.isEmpty()) {
		this.intersectHS = new HashSet<NODE>(0);
	} else {
	  this.intersectHS = new HashSet<NODE>() ;
	  
	  this.partialHS2 = new HashSet<NODE>();
	  NODE[] seeds2 = this.g2.seeds();
	  for (int i = 0; i < seeds2.length; i++) {
	    NODE node2 = seeds2[i];
	    if (this.partialHS2.add(node2)) intersectWalk(node2) ; // if notVisitedYet...
	  }
	}
}

//node doit déjà avoir été mis ds this.hsTemp2
//node is from g2
protected void intersectWalk(NODE node2) throws Exception{
	if (this.nodes1.contains(node2)) {
		this.intersectHS.add(node2);
	} else {
	  for (Iterator<NODE> it = this.g2.getNeighbors(node2) ; it.hasNext() ;) {
	  	NODE neighbor = it.next() ;
      if (this.partialHS2.add(neighbor)) { // if notVisitedYet
      	intersectWalk(neighbor);
      }
	  }
	}
}

}
