package net.semanlink.semanlink;
import java.util.*;
/**
 * @author fps
 */
public class SLKeywordAdapter extends SLResourceAdapter implements SLKeyword {
public static final long serialVersionUID = 1;

// CONSTRUCTION

public SLKeywordAdapter(String uri) { super(uri); }

// IMPLEMENTS SLKeyword

public List getParents() { return new ArrayList(); }
public List getChildren() { return new ArrayList(); }
public List getFriends() { return new ArrayList(); }
public List getDocuments() { return new ArrayList(); }
public boolean hasChild() { return false; }
public boolean hasDocument() { return false; }
/** @deprecated use SLUtils.getLinkedKeywords(this) */
public SLKeyword[] getLinkedKeywords() {
	return SLUtils.getLinkedKeywords(this);
}
public String getHomePageURI() { return null; }
}
