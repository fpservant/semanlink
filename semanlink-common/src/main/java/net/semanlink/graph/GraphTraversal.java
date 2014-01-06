package net.semanlink.graph;

import java.util.*;

/**
 * Implements the traversal of a Graph. 
 */
public class GraphTraversal<NODE> {
private Graph<NODE> graph;
/** the set of nodes.
 * Documented by traversal. */
protected HashSet<NODE> hs;

public GraphTraversal(Graph<NODE> graph) {
    this.graph = graph ;
}

/** The traversed graph. */
public Graph<NODE> getGraph() { return this.graph; }

/** A simple traversal of the graph that just computes its set of nodes */
public void simpleWalk() throws Exception {
    this.hs = new HashSet<NODE>() ;
    NODE[] seeds = this.graph.seeds();
    for (int i = 0; i < seeds.length; i++) {
        NODE node = seeds[i];
        if (this.hs.add(node)) simpleWalk(node) ; // if notVisitedYet...
    }
}

// node doit déjà voir été mis ds this.hs
protected void simpleWalk(NODE node) throws Exception{
    for (Iterator<NODE> it = this.graph.getNeighbors(node) ; it.hasNext() ;) {
        NODE neighbor = it.next() ;
        if (this.hs.add(neighbor)) { // if notVisitedYet
            simpleWalk(neighbor);
        }
    }
}

/** 
 * The set of nodes in the graph
 */
 // If result has not been computed yet, makes a simpleWalk.
 // (if you don't want computing, use this.hs)
public HashSet<NODE> getNodes() throws Exception {
    if (this.hs == null) simpleWalk();
    return this.hs ;
}

//
// WALK
//

/**
 * Depth-first traversal. 
 * 
 * <p>Il y a deux façons de faire pour la gestion des "seeds".</p>
 * <p>On peut les traiter comme si elles étaient les voisins d'un noeud "root"
 * (on a alors un vrai parcours en profondeur d'abord)
 * - c'est ce que fait cette méthode.</p>
 * <p>Ou bien on peut les privilégier de façon à ce que, au cas où une des seeds
 * serait en fait atteinte lors du parcours à partir d'une des autres, on ait la parcours pour cette seed
 * en 1er niveau (et le "repeatNode" lorsqu'elle est atteinte à partir d'ailleurs,
 * même si on passe en fait d'abord par là) (ça, c'est ce que fait walk)</p>
 * <p>La différence entre les 2, c'est si on commence par mettre les seeds
 * dans le HashSet des déjà vus, ou pas.</p>
 */
public void depthFirstWalk(WalkListener<NODE> walkListener) throws Exception {
    depthFirstWalk(walkListener, null, null);
}

/**
 * Depth-first traversal. 
 *
 * <p>HOW TO USE: implements WalkListener (for instance extending WalkListenerImpl),
 * and call this method. By storing a Stack stack or treePosition as attribute of
 * the class implementing  WalkListener, you can use their content in the implementing methods.</p>
 * @see class SimplePrinter as an example
 * @param walkListener
 * @param stack: if not null, the nodes handled during traversal are pushed / popped onto this stack
 * So, supposing <UL>
 * <li>that you passed an empty Stack stack when calling this method</li>
 * <li>that you are receiving one of the methods of the walkListener with arg node,</li> 
 * <li>and that stack contains "seed1,node3",<li>
 * then node is a neighbor of node3, which in turn is a neighbor of seed1.
 * A node is pushed on this stack at the moment we are about to process the list of its neighbors
 * (after sending startNeighborList() to walkListener) , and popped just before sending endNeighborList()
 * If a node doesn't have any neighbor, it is never pushed on stack.
 * Note that stack treePosition (if used) has one element more than stack.
 * @param treePosition: if not null, the positions of nodes handled during traversal 
 * are pushed / popped onto this stack.
 * So, supposing <UL>
 * <li>that you passed an empty Stack treePosition when calling this method</li>
 * <li>that you are receiving one of the methods of the walkListener with arg node,</li> 
 * <li>and that treePosition contains "1,3,2",<li>
 * then node is the second neighbor of third neighbor of first seed of graph.
 * Note that stack "stack" (if used) is one element shorter than treePosition.
 * @throws Exception
 */
public void depthFirstWalk(WalkListener<NODE> walkListener, Stack<NODE> stack, Stack<Integer> treePosition) throws Exception {
    this.hs = new HashSet<NODE>();
    NODE[] seeds = this.graph.seeds();
    for (int i = 0; i < seeds.length; i++) {
        NODE node = seeds[i];
        boolean notVisitedYet = this.hs.add(node);
        if (treePosition != null) treePosition.push(Integer.valueOf(i));
        walkListener.startSeed(node);
        if (notVisitedYet) {
            walk(node, walkListener, stack, treePosition);
         } else {
            walkListener.repeatNode(node);
        }
        walkListener.endSeed(node);
        if (treePosition != null) treePosition.pop();
    }
}
/**
 * Depth-first traversal. 
 * 
 * @see depthFirstWalk(WalkListener)
 */
public void walk(WalkListener<NODE> walkListener) throws Exception {
    walk(walkListener, null, null);
}

/**
 * Depth-first traversal. 
 * 
 * Duplicate seeds are removed. 
 * Seeds never receive "repeatNode". 
 * 
 * @see depthFirstWalk(WalkListener)
 * @see depthFirstWalk(WalkListener, Stack, Stack)
 */
public void walk(WalkListener<NODE> walkListener, Stack<NODE> stack, Stack<Integer> treePosition) throws Exception {
    this.hs = new HashSet<NODE>();
    NODE[] seeds = this.graph.seeds();
    boolean duplicateSeeds = false;
    for (int i = 0; i < seeds.length; i++) {
        NODE node = seeds[i];
        boolean notVisitedYet = this.hs.add(node) ;
        if (!notVisitedYet) duplicateSeeds = true;
    }
    // remove duplicates from seeds, if any
    int seedsNb = seeds.length;
    if (duplicateSeeds) {
    	seeds = this.hs.toArray(seeds); // attention, seeds array too big: last items will be null
    	seedsNb = hs.size();
    }
 
    for (int i = 0; i < seedsNb; i++) { // seedsNb, not seeds.length (there are null items at the end if duplicateSeeds)
    	NODE node = seeds[i];
      if (treePosition != null) treePosition.push(Integer.valueOf(i));
      walkListener.startSeed(node);
      walk(node, walkListener, stack, treePosition);
      walkListener.endSeed(node);
      if (treePosition != null) treePosition.pop();
    }
}

/** node doit déjà avoir été mis ds this.hs */
protected void walk(NODE node, WalkListener<NODE> walkListener, Stack<NODE> stack, Stack<Integer> treePosition) throws Exception{
    Iterator<NODE> it = this.graph.getNeighbors(node);
    boolean thereIsANeighbor = it.hasNext();
    if (!thereIsANeighbor) {
        walkListener.noNeighborList(node);
    } else {
        walkListener.startNeighborList(node);
        if (stack != null) stack.push(node);
        for (int i = 0 ; it.hasNext() ; i++) {
        		NODE neighbor = it.next();
            boolean notVisitedYet = this.hs.add(neighbor);
            if (treePosition != null) treePosition.push(Integer.valueOf(i));
            if (notVisitedYet) {
                walkListener.startNode(neighbor);
                walk(neighbor, walkListener, stack, treePosition);
                walkListener.endNode(neighbor);
            } else {
                walkListener.repeatNode(neighbor);
            }
            if (treePosition != null) treePosition.pop();
        }   
        if (stack != null) stack.pop();
        walkListener.endNeighborList(node);
    }
}

//
// EXAMPLE OF USE / PRINTING THE GRAPH AS A TREE
//

public void print() throws Exception {
    Stack<NODE> stack = new Stack<NODE>();
    Stack<Integer> treePosition = new Stack<Integer>();
    SimplePrinter<NODE> printer = new SimplePrinter<NODE>(stack, treePosition);
    depthFirstWalk(printer, stack, treePosition);
}

static public class SimplePrinter<NODE2> extends WalkListenerImpl<NODE2> {
    private Stack<NODE2> stack;
    private Stack<Integer> treePosition;
    public SimplePrinter(Stack<NODE2> stack, Stack<Integer> treePosition) {
        this.stack = stack;
        this.treePosition = treePosition;
    }
    public void startSeed(NODE2 node) throws Exception {
    		startNode(node);
    }
    public void startNode(NODE2 node) throws Exception {
        System.out.println(x(node));
    }
    public void repeatNode(NODE2 node) throws Exception {
        System.out.println(x(node) + " (already displayed above)");
    }
    private String x(NODE2 node) throws Exception {
        return indent() + position() + " " + node;
    }
    protected String indent() {
        int n = stack.size();
        if (n == 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append("\t");
        return sb.toString();
    }
    public String position() {
        int n = treePosition.size();
        StringBuilder sb = new StringBuilder(n*2);
        for (int i = 0; i < n; i++) {
            Integer pos = treePosition.get(i);
            sb.append(pos+1);
            sb.append(".");
        }
        return sb.toString();
    }
}

}
