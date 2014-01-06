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

/** The nodes in the intersection. 
 * if fast, computes a "light" intersection: children of its elements NOT necessarily included in this result. 
 * (for instance, if we have one graph with "music", the other with "africa", we find that "african music" is in the intersection,
 * but we do not need to explicitly return makossa, congolese rumba and music of niger */
public HashSet<NODE> getNodes(boolean fast) throws Exception {
    doIt(fast);
    return this.intersectHS ;
}

private void doIt(boolean fast) throws Exception {
	if (nodes1.isEmpty()) {
		this.intersectHS = new HashSet<NODE>(0);
	} else {
	  this.intersectHS = new HashSet<NODE>() ;
	  
	  this.partialHS2 = new HashSet<NODE>();
	  NODE[] seeds2 = this.g2.seeds();
	  for (int i = 0; i < seeds2.length; i++) {
	    NODE node2 = seeds2[i];
	    if (this.partialHS2.add(node2)) intersectWalk(node2, fast) ; // if notVisitedYet...
	  }	  	
	}
}

private void intersectWalk(NODE node2, boolean fast) throws Exception{
	if (this.nodes1.contains(node2)) {
		this.intersectHS.add(node2);
		if (fast) {
			 // we do not explore the neighbors of node2 
			// (more or less assuming that they are in the intersection, but that we don't need to return them explicitly in the intersection)
			return;
		}
	}

  for (Iterator<NODE> it = this.g2.getNeighbors(node2) ; it.hasNext() ;) {
  	NODE neighbor = it.next() ;
    if (this.partialHS2.add(neighbor)) { // if notVisitedYet
    	intersectWalk(neighbor, fast);
    }
  }
}


}
