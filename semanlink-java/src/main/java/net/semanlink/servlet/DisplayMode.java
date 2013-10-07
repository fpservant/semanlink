/* Created on 27 oct. 07 */
package net.semanlink.servlet;
/** What to show in the GUI */
public class DisplayMode {
	public static String DESCENDANTS_CHILDREN_AS_LIST = "0";
	public static String DESCENDANTS_TREE = "1";
	public static String DESCENDANTS_EXPANDED_TREE = "2";
	// public static DisplayMode DEFAULT = new DisplayMode(DESCENDANTS_TREE, true);
	public static DisplayMode DEFAULT = new DisplayMode(DESCENDANTS_TREE, false); // 2013-03
	private boolean longListOfDocs;
	private String childrenAs;
	DisplayMode(String childrenAs, boolean longListOfDocs) {
		if (childrenAs == null) this.childrenAs = DEFAULT.childrenAs;
		else this.childrenAs = childrenAs;
		this.longListOfDocs = longListOfDocs;
	}
	public boolean isLongListOfDocs() { return this.longListOfDocs; }
	public String getChildrenAs() { return this.childrenAs; }
	public boolean isChildrenAsList() { return DESCENDANTS_CHILDREN_AS_LIST.equals(this.childrenAs); }
	public boolean isChildrenAsTree() { return  DESCENDANTS_TREE.equals(this.childrenAs); }
	public boolean isChildrenAsExpandedTree() { return DESCENDANTS_EXPANDED_TREE.equals(this.childrenAs); }
}
