package net.semanlink.semanlink;
import java.util.*;
/**
 * @author fps
 */
public class SLKeywordAdapter extends SLResourceAdapter implements SLKeyword {

// CONSTRUCTION

public SLKeywordAdapter(String uri) { super(uri); }

// IMPLEMENTS SLKeyword

public List<SLKeyword> getParents() { return new ArrayList<SLKeyword>(); }
public List<SLKeyword> getChildren() { return new ArrayList<SLKeyword>(); }
public List<SLKeyword> getFriends() { return new ArrayList<SLKeyword>(); }
public List<SLDocument> getDocuments() { return new ArrayList<SLDocument>(); }
public boolean hasChild() { return false; }
public boolean hasDocument() { return false; }
/** @deprecated use SLUtils.getLinkedKeywords(this) */
public SLKeyword[] getLinkedKeywords() {
	return SLUtils.getLinkedKeywords(this);
}
public String getHomePageURI() { return null; }
}
