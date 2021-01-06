package net.semanlink.graph;
/** Listener receiving the events generated during a GraphTraversal */
public interface WalkListener<NODE> {
	/** Gets called when beginning a seed. */
  default void startSeed(NODE seed) throws Exception {}
	/** Gets called when beginning to handle the list of neighbors of node. 
	 *  The list is not empty. If it is empty, noNeighborList gets called instead. */
  default void startNeighborList(NODE node) throws Exception {}
	/** Gets called if node has no neighbors. */
  default void noNeighborList(NODE node) throws Exception {}
	/** Gets called when beginning a node (other than a seed) that has not been seen before. */
  default void startNode(NODE node) throws Exception {}
	/** Gets called when ending a node (other than a seed) that has not been seen before. */
  default void endNode(NODE node) throws Exception {}
	/** Gets called when node has already been traversed. */
  default void repeatNode(NODE node) throws Exception {}
	/** Gets called at the end of the handling of the list of neighbors of node. 
	 *  Not called when this list is empty. */
  default void endNeighborList(NODE node) throws Exception {}
	/** Gets called when ending a seed. */
  default void endSeed(NODE seed) throws Exception {}
}
