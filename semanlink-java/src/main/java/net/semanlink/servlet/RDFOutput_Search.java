package net.semanlink.servlet;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.util.URLUTF8Encoder;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

// http://127.0.0.1:9080/semanlink/sl/search.rdf?text=afric+musi

public class RDFOutput_Search extends RDFOutput {
private Jsp_Search jsp_search;
public RDFOutput_Search(Jsp_Search jsp, String extension) throws UnsupportedEncodingException, MalformedURLException {
	super(jsp, extension);
	this.jsp_search = jsp;
}

/* This to use www.semanlink.net/tag/atag instead of 127.0.0.1:8080/semanlink/tag/atag */
/*protected String getAboutURL() throws Exception {
	return this.jsp.getUri();
}*/

protected void computeModel() throws Exception {
	super.computeModel();
	List kws = this.jsp_search.beanKwList.getList();
	addKwList(this.aboutRes, this.urisToUse.getHasKeywordProperty(mod), kws); // Hmmm @TODO change that
}

private void addKwList(Resource mainRes, Property prop, List kws) throws Exception {
	for (int i = 0; i < kws.size(); i++) {
		SLKeyword kw = (SLKeyword) kws.get(i);
		Resource res = addKw(kw);
		this.mod.add(mainRes, prop, res);
	}
}

protected String getAboutURL() throws Exception {
	String x = this.jsp_search.getRequest().getRequestURL().toString();
	if (x.endsWith(".rdf")) x = x.substring(0, x.length()-4); 
	x = x + "?text=" + URLUTF8Encoder.encode(jsp_search.getSearchString());
	return x;
}

public String getHTMLUri() throws Exception {
	return getAboutURL();  // BOF BOF
}

}
