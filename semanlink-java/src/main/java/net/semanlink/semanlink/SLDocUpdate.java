/* Created on Mar 31, 2020 */
package net.semanlink.semanlink;
public abstract class SLDocUpdate implements AutoCloseable { // 2020-03
abstract public void addDocProperty(String propertyUri, String propertyValue, String lang);
abstract public void addDocProperty(String propertyUri, String objectUri);
abstract public void addDocProperty(String propertyUri, String[] objectUris);
abstract public void setDocProperty(String propertyUri, String propertyValue, String lang);
abstract public void setDocProperty(String propertyUri, String objectUri);

abstract public void removeStatement(String propertyUri, String propertyValue, String lang);
abstract public void removeStatement(String propertyUri, String objectUri);

public void addKeyword(SLKeyword kw) {
	addDocProperty(SLModel.HAS_KEYWORD_PROPERTY, kw.getURI());
}

public void addKeyword(SLKeyword[] kws) {
	String[] uris = new String[kws.length];
	for (int i = 0; i < uris.length; i++) {
		uris[i] = kws[i].getURI();
	}
	addDocProperty(SLModel.HAS_KEYWORD_PROPERTY, uris);
}
	
}
