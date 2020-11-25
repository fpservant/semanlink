/* Created on 24 fevr. 2004 */
package net.semanlink.semanlink;

import java.util.HashMap;
import java.util.List;

/**
 * @author fps
 * @deprecated (because unused!)
 */
public class Document_DELETED implements SLDocument {
private SLDocument slDoc;
public Document_DELETED(SLDocument slDoc) {
	this.slDoc = slDoc;
}

public SLDocument getSLDocument() { return this.slDoc; }
public List getKeywords() { return this.slDoc.getKeywords(); }
public String getLabel() { return this.slDoc.getLabel(); }
public String getLabel(String lang) { return this.slDoc.getLabel(lang); }
public String getURI() { return this.slDoc.getURI(); }
public String getComment() { return this.slDoc.getComment(); }
public String getDate() { return this.slDoc.getDate(); }
public HashMap getProperties() { return this.slDoc.getProperties(); }
public HashMap getPropertiesAsStrings() { return this.slDoc.getPropertiesAsStrings(); }
public PropertyValues getProperty(String pptyUri) { return this.slDoc.getProperty(pptyUri); }
public List getPropertyAsStrings(String pptyUri) { return this.slDoc.getPropertyAsStrings( pptyUri); }
public String getPropertyAsString(String pptyUri) { 
	List lis = this.slDoc.getPropertyAsStrings( pptyUri);
	if (lis == null) return null;
	return (String) lis.get(0);
}
public int compareTo(Object arg0) { return this.slDoc.compareTo(arg0); }

//
//
//
@Override public String getMarkdownUri(String lang) {
	return null;
}

@Override public List<SLDocument> mainDocOf() {
	// TODO Auto-generated method stub
	return null;
}

@Override public List<SLDocument> similarlyTaggedDocs() { // 2020-11
	// TODO Auto-generated method stub
	return null;
}

@Override public List<SLDocument> relatedDocs(boolean linkTo, boolean linkFrom){ // 2020-11
	// TODO Auto-generated method stub
	return null;
}
} // class
