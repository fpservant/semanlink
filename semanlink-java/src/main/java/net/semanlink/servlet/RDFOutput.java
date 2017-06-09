package net.semanlink.servlet;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class RDFOutput {
protected Jsp_Page jsp;
/** use getter */
protected Model mod;
protected SLUris urisToUse;
/** use getter */
protected Tag2ResConverter tag2ResConverter;
/** main resource's uri (for instance, uri of tag) */
protected String aboutUrl;
/** resource corresponding to the rdf file returned (for instance tag.rdf) May be null*/
protected Resource rdfRes; // 2007/08/29buggy "&tag;richard_cygan" avec   <!ENTITY tag 'http://www.semanlink.net/tag/'>
/** main resource */
protected Resource aboutRes;
protected Resource keywordClass;
protected Property docTitleProperty;
protected Property tagProperty;
protected Property creationDateProperty;
protected Property commentProperty;
protected Property keywordPrefLabelProperty;

/** eg "rdf" or "n3" -- null is OK, just you don't have the statement linking the doc*/
protected String extension; 

public RDFOutput(Jsp_Page jsp, String extension) throws UnsupportedEncodingException, MalformedURLException {
	this.jsp = jsp;
	this.extension = extension;
}

/** To be called before getModel (that is to say before init), if we want to use skos vocab rather than sl. // @find skos rather than sl */
public void setUrisToUse(SLUris urisToUse) {
	this.urisToUse = urisToUse;
}

/** To use a Tag2ResConverter other than default one
 *  (what we may want to do if the SLModel has a thesaurus uri that is different from serletUrl/tag)
 */
void setTag2ResConverter(Tag2ResConverter tag2ResConverter) { this.tag2ResConverter = tag2ResConverter; }
Tag2ResConverter getTag2ResConverter() throws Exception {
	if (this.tag2ResConverter == null) this.tag2ResConverter = new SimpleTag2ResConverter(getModel());
	return this.tag2ResConverter;
}

// 2010-12 pour éviter conversion pour les tree généré en js en 127 vs semanlink.net
private boolean useTag2ResConversion = true;
void setTag2ResConversion(boolean useTag2ResConversion) { this.useTag2ResConversion = useTag2ResConversion; }

public Model getModel() throws Exception {
	if (this.mod == null) computeModel();
	return this.mod;
}

protected void init() throws UnsupportedEncodingException, MalformedURLException {
	this.mod = ModelFactory.createDefaultModel();
	this.docTitleProperty = this.mod.createProperty(DC.title.getURI());
	// @find skos rather than sl
	/*
	// To use skos vocab rather than sl, change this line:
	this.urisToUse = SLUris.getInstance();
	// to:
	// SLUris urisToUse = SLUrisAsSkos.getInstance();
	*/
	if (this.urisToUse == null) {
		this.urisToUse = SLUris.getInstance();
	}
	this.keywordClass = this.urisToUse.getKeywordClass(mod);
	this.tagProperty = this.urisToUse.getHasKeywordProperty(mod);
	this.creationDateProperty = this.urisToUse.getCreationDateProperty(mod);
	this.keywordPrefLabelProperty = this.urisToUse.getKeywordPrefLabel(mod);
	this.commentProperty = this.urisToUse.getCommentProperty(mod);
	//
	if (useTag2ResConversion) { // 2010-12
		SLModel slMod = SLServlet.getSLModel();
		if (!( slMod.getDefaultThesaurus().getURI().startsWith(slMod.getModelUrl())) ) {
			String oldNS = slMod.getDefaultThesaurus().getURI();
			if (!oldNS.endsWith("/")) oldNS += "/";
			String newNS = slMod.getModelUrl();
			if (!newNS.endsWith("/")) newNS += "/";
			newNS += "tag/";
	
			setTag2ResConverter(new Tag2ResConverter_ChangingNS(this.mod, oldNS, newNS));
		}
	}
}

protected void computeModel() throws Exception {
	init();
	String s;
	// NON : il y a .html - en tout cas, pas bon pour kw
	this.aboutUrl = getAboutURL();
	this.aboutRes = getTag2ResConverter().createResource(aboutUrl); // "http://127.0.0.1:8080/semanlink/tag/fps"

	this.mod.add(aboutRes, this.mod.createProperty(getTitlePropertyURI()), this.jsp.getTitle());
	s = this.jsp.getComment();
	if (s != null) this.mod.add(aboutRes, this.commentProperty, s);
	
	// link between the uri of non-information resource and the file returned when it is dereferenced
	if (extension != null) {
		this.rdfRes = this.mod.createResource(getRDFUri(extension));
		this.mod.add(aboutRes, RDFS.isDefinedBy, this.rdfRes);
	}

	Resource htmlRes = this.mod.createResource(getHTMLUri());
	if (!(htmlRes.equals(aboutRes))) { // happens in the case of search output (aboutRes not a NIR ?)
		Property foafPage = this.mod.createProperty("http://xmlns.com/foaf/0.1/page");
		this.mod.add(aboutRes, foafPage, htmlRes);
	}

	List docList = getDocList();
	if (docList != null) {
		for (int i = 0; i < docList.size(); i++) {
			SLDocument doc = (SLDocument) docList.get(i);
			addDoc(doc);
		}
	}
	
	// tag cloud
	/* SLKeywordNb[] tagCloud = jsp.getLinkedKeywordsWithNb();
	for (int i = 0; i < tagCloud.length; i++) {
		SLKeyword kw = tagCloud[i].getKw();
	}*/
}

/** probably to be overridden. */
public String getHTMLUri() throws Exception {
	return getAboutURL() + ".html";  // BOF BOF
}

/** @param extension eg "rdf" */
public String getRDFUri(String extension) throws Exception {
	return this.jsp.linkToRDF(extension);
}

//overridden par ex ds Jsp_Keyword
public String getTitlePropertyURI() {
	return DC.title.getURI();
}

// ATTENTION, si n'est pas overridé ds RDFOutput_keyword,
// le code ici retourne, pour un tag, quelque chose comme
// "http://127.0.0.1:8080/semanlink/tag/fps"
/** The main res of this model : for instance the uri of this kw. 
 * BEWARE, NO PARAMETERS INCLUDED -> not ok, for instance for search
 * @throws MalformedURLException 
 * @throws UnsupportedEncodingException */
protected String getAboutURL() throws Exception {
	String x = jsp.completePath();
	if (x.endsWith(".html")) x = x.substring(0, x.length()-5); // attention, pour un tag, donnerait 127.0.0.1:8080/semanlink/tag/atag instead of www.semanlink.net/tag/atag
	return x;
}

// PAS POUR KW: PB AVEC LE MODE.
// (à quoi sert getDocList ds keyword.jsp ???)
/** list de SLDocument à ajouter au model, ou null */
protected List getDocList() throws Exception {
	Bean_DocList bean_DocList = jsp.getDocList();
	if (bean_DocList != null) return bean_DocList.getList();
	return null;
}

/** pour ajouter un doc au model rdf de sortie
 *  (ajoute les kws liés au doc)
 * @throws Exception 
 */
private void addDoc(SLDocument doc) throws Exception {
	Resource docRes = this.mod.createResource(doc.getURI());
	String s;
	s = doc.getLabel();
	if (s != null) this.mod.add(docRes, docTitleProperty, s);
	
	s = doc.getDate();
	if (s != null) this.mod.add(docRes, this.creationDateProperty, s);
	
	s = doc.getComment();
	if (s != null) this.mod.add(docRes, this.commentProperty, s);
	
	// kws : on ne met pas les labels ici : ils le seront via le tagCloud
	List kws = doc.getKeywords();
	if (kws != null) {
		for (int i = 0; i < kws.size(); i++) {
			SLKeyword kw = (SLKeyword) kws.get(i);
			Resource kwRes = addKw(kw);
			// ATTENTION, LES KWS sont en www.semanlink.net/tag (pas en 127.0.0.1:8080/semanlink/tag)
			// ce qui poserait probablement un pb pour le déréférencement d'un kw cliqué à partir du html généré
			// (enfin, dépendra de ce que fat le js, qui de toute façon devra manipuler à partir de l'uri des kws
			// pour créer les liens à suivre, puisqu'il ne s'agira pas de l'uri du kw lui même,
			// mais un truc genre rdfjs?uri=?
			// (BTW : on aura intérêt à) mettre ça ds une fct js)
			// SEE linkToRDF ds Jsp_page (doit être homogène !!!)
			this.mod.add(docRes, tagProperty, kwRes);
		}
	}
}

protected Resource addKw(SLKeyword kw) throws Exception {
	///// C'EST ICI qu'il faut changer pour mettre 127.0.0.1:8080 au lieu de semanlink.net -- ce dernier étant ce qu'on veut généralement mettre 
	////// VOIR AUSSI + haut ET RDFOutput_kw
	// ET COMMENT ON FAIT POUR LE HTML
	Resource kwRes = getTag2ResConverter().createResource(kw.getURI());
	this.mod.add(kwRes, RDF.type, this.keywordClass);
	this.mod.add(kwRes, this.keywordPrefLabelProperty, kw.getLabel());
	return kwRes;
}

//
// All this, to have the possibility to convert from tag uri to a local uri
//

interface Tag2ResConverter {
	public Resource createResource(String tagUri);
}

/** To be used normaly: the resource is created directly with kw's uri. */
class SimpleTag2ResConverter implements Tag2ResConverter {
	private Model model;
	public SimpleTag2ResConverter(Model model) { this.model = model; }
	public Resource createResource(String tagUri) {
		return mod.createResource(tagUri);
	}
}

/** To be used normaly: the resource is created directly with kw's uri. */
class Tag2ResConverter_ChangingNS extends SimpleTag2ResConverter {
	private String oldNS;
	private String newNS;
	private int oldNSLength;
	/**
	 * 
	 * @param model
	 * @param oldNS the thesaurus uri for instance http://www.semanlink.net/tag/
	 * @param newNS for instance 127.0.0.1:8080/tag/
	 */
	public Tag2ResConverter_ChangingNS(Model model, String oldNS, String newNS) { 
		super(model);
		// System.out.println("RDFOutput Tag2ResConverter_ChangingNS oldNS: " + oldNS + " newNS: "+ newNS);
		this.oldNS = oldNS;
		oldNSLength = oldNS.length();
		this.newNS = newNS;
	}
	
	public Resource createResource(String uri) {
		if (uri.startsWith(oldNS)) uri = newNS + uri.substring(oldNSLength);
		return mod.createResource(uri);
	}

}


}
