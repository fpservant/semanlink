package net.semanlink.servlet;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import net.semanlink.semanlink.SLKeyword;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class RDFOutput_Keyword extends RDFOutput {
private SLKeyword thisKw;
private Resource thisKwRes;
public RDFOutput_Keyword(Jsp_Keyword jsp, String extension) throws UnsupportedEncodingException, MalformedURLException {
	super(jsp, extension);
	this.thisKw = jsp.getSLKeyword();
}

public String getTitlePropertyURI() {
	return this.urisToUse.getKeywordPrefLabelURI();
}

/* This to use www.semanlink.net/tag/atag instead of 127.0.0.1:8080/semanlink/tag/atag */
protected String getAboutURL() throws Exception {
	return this.thisKw.getURI();
}

protected void computeModel() throws Exception {
	super.computeModel();
	this.thisKwRes = addKw(this.thisKw);
	// parents
	addKwList(this.thisKw.getParents(), this.urisToUse.getHasParentProperty(mod));
	addKwList(this.thisKw.getFriends(), this.urisToUse.getHasFriendProperty(mod));
	// CECI pourrait être bien pour SKOS :
	// addKwList(this.thisKw.getChildren(), this.urisToUse.getHasChildrenProperty(mod));
	addChildren();
}

private void addKwList(List list, Property prop) throws Exception {
	for (int i = 0; i < list.size(); i++) {
		SLKeyword kw = (SLKeyword) list.get(i);
		Resource res = addKw(kw);
		this.mod.add(this.thisKwRes, prop, res);
		//System.out.println("rdf_output " + thisKwRes.getURI() + " -> " + res.getURI());
	}
}

private void addChildren() throws Exception {
	List list = this.thisKw.getChildren();
	Property parentProp = this.urisToUse.getHasParentProperty(mod);
	for (int i = 0; i < list.size(); i++) {
		SLKeyword kw = (SLKeyword) list.get(i);
		Resource res = addKw(kw);
		this.mod.add(res, parentProp, this.thisKwRes);
		//System.out.println("rdf_output " + res.getURI() + " -> " + thisKwRes.getURI());
	}
}


}
