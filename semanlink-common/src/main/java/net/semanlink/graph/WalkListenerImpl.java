package net.semanlink.graph;
/** An implementation of WalkListener that doesn't do anything. */
public class WalkListenerImpl<NODE> implements WalkListener<NODE> {
  public void startSeed(NODE seed) throws Exception {}
  public void startNeighborList(NODE node) throws Exception {}
  public void noNeighborList(NODE node) throws Exception {}
  public void startNode(NODE node) throws Exception {}
  public void endNode(NODE node) throws Exception {}
  public void repeatNode(NODE node) throws Exception {}
  public void endNeighborList(NODE node) throws Exception {}
  public void endSeed(NODE seed) throws Exception {}
}
