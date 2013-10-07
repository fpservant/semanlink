package net.semanlink.graph;
import java.util.Iterator;
public interface Graph<NODE> {
    /** at least one (and ideally only one) node of each connected component. */
    public NODE[] seeds();
    /** an iterator over the direct neighbors of a given node of the graph. */
    public Iterator<NODE> getNeighbors(NODE node) throws Exception ;
}
