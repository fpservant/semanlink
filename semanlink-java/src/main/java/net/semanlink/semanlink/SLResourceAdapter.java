package net.semanlink.semanlink;

import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.text.*;


/**
 * @author fps
 */
public class SLResourceAdapter implements SLLabeledResource, SLVocab, Serializable {
public static final long serialVersionUID = 1;
static public Collator collator = initCollator();
private static Collator initCollator() {
	Collator collator = Collator.getInstance(Locale.FRANCE);
	collator.setStrength(Collator.PRIMARY);
	return collator;
}
protected String uri;
/** For ordering. Must be accessed through its getter */
private CollationKey collationKey;

// CONSTRUCTION

public SLResourceAdapter(String uri) { this.uri = uri; }
private HashMap properties, propertiesAsStrings;

// IMPLEMENTS SLLabeledResource

public String getURI() { return this.uri; }
public String getLabel() { return getURI(); }
public String getLabel(String language) { return getLabel(); }
public String getComment() { return null; }
public String getDate() { return null; }
public HashMap getProperties() { 
	if (this.properties == null) this.properties = new HashMap();
	return this.properties;
}
public HashMap getPropertiesAsStrings()  { 
	if (this.propertiesAsStrings == null) this.propertiesAsStrings = new HashMap();
	return this.propertiesAsStrings;
}
public PropertyValues getProperty(String pptyUri) {
	return (PropertyValues) getProperties().get(pptyUri);
}
public List getPropertyAsStrings(String pptyUri) {
	return (List) getPropertiesAsStrings().get(pptyUri);
}

//IMPLEMENTS Comparable

public boolean equals(Object o) {
	if (o instanceof SLResource) return this.uri.equals(((SLResource) o).getURI());
	return false;
}

public int hashCode() { return this.uri.hashCode(); }
public CollationKey getCollationKey() {
	if (this.collationKey == null) this.collationKey = collator.getCollationKey(toString());
	return this.collationKey;
}
public int compareTo(Object o) { return getCollationKey().compareTo(((SLResourceAdapter) o).getCollationKey()); }

//

public String toString() {
	return getLabel();
}

//IMPLEMENTS Serializable : cf clipboard // Not tested

private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	 out.writeObject(this.uri);
}
private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	 this.uri = ((String) in.readObject());
}


}