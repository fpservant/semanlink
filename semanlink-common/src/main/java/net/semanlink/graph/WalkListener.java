package net.semanlink.graph;
public interface WalkListener<NODE> {
	/** Gets called when beginning a seed. */
  public void startSeed(NODE seed) throws Exception;
	/** Gets called when beginning to handle the list of neighbors of node. 
	 *  The list is not empty. If it is empty, noNeighborList gets called instead. */
  public void startNeighborList(NODE node) throws Exception;
	/** Gets called if node has no neighbors. */
  public void noNeighborList(NODE node) throws Exception;
	/** Gets called when beginning a node (other than a seed) that has not been seen before. */
  public void startNode(NODE node) throws Exception;
	/** Gets called when ending a node (other than a seed) that has not been seen before. */
  public void endNode(NODE node) throws Exception;
	/** Gets called when node has already been traversed. */
  public void repeatNode(NODE node) throws Exception;
	/** Gets called at the end of the handling of the list of neighbors of node. 
	 *  Not called when this list is empty. */
  public void endNeighborList(NODE node) throws Exception;
	/** Gets called when ending a seed. */
  public void endSeed(NODE seed) throws Exception;
}
