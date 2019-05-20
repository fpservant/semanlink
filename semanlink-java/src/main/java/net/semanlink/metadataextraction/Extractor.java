package net.semanlink.metadataextraction;

import java.util.Locale;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLUtils;
import net.semanlink.semanlink.SLVocab;

/** Extract metadata from a document. 
 *  
 *  Extends this class to describe the peculiarities of one kind of doc.
 *  Override dealWith to return true when doc is dealed with by this extractor
 *  Call doIt() to add properties to the model.
 *  Overrides the method like getSource(ExtractorData),... 
 *  To add a new prop to be analysed, define its getter here with default value,
 *  and modify doIt() to add the prop to the model.
 *  (this, if you want the prop for any HTMLMetadataExtractor, 
 *  else : modify the model in the doIt of the sub class)
 *  An inner class is created that can be used if the implementation of the different getters
 *  have to do calculus that can be shared (this avoid to do these calculus several times)
 */
public class Extractor implements SLVocab {
/** Retourne true ssi cet extractor traite le document pass� en argument. */
public boolean dealWith(ExtractorData data) {
	return false;
}
/** Returns true if model hasChanged. May modify data. */
public boolean doIt(ExtractorData data) throws Exception {
	ExtractionResult x = extract(data);
	SLDocument doc = data.getSLDocument();
	SLModel mod = data.getSLModel();
	String s;
	s = x.getSource();
	boolean hasChanged = false;
	if (s != null) {
		hasChanged = true;
		mod.addDocProperty(doc, SLVocab.SOURCE_PROPERTY, s, null); // add et pas set pour ne pas remplacer le lien vers url
	}
	s = x.getDateParution();
	if (s != null) {
		hasChanged = true;
		mod.setDocProperty(doc, SLVocab.DATE_PARUTION_PROPERTY, s, null);		
	}
	s = x.getCreator();
	if (s != null) {
		hasChanged = true;
		mod.setDocProperty(doc, SLVocab.CREATOR_PROPERTY, s, null);
	}
	return hasChanged;
}

public ExtractionResult extract(ExtractorData data) throws Exception {
	ExtractionResult x = new ExtractionResult(data);
	x.setSource(getSource(data));
	x.setDateParution(getDateParution(data));
	x.setCreator(getCreator(data));
	return x;
}

/** Achtung, should return a literal. Change that?*/
public String getSource(ExtractorData data) throws Exception { return null; }
/** Should return a date with format such as 2004-12-25 */
public String getDateParution(ExtractorData data) throws Exception { return null; }
public String getCreator(ExtractorData data) throws Exception { return null; }
public String getTitle(ExtractorData data) throws Exception { return null; }

public class ExtractionResult {
	ExtractorData data;
	private String source;
	private String dateParution;
	// DEVRAIT ETRE UN TABLEAU !!!
	private String creator;
	private String title;
	public ExtractionResult(ExtractorData data) {
		this.data = data;
	}
	/** Achtung, should return a literal. Change that?*/
	public String getSource() throws Exception { return this.source; }
	/** Should return a date with format such as 2004-12-25 */
	public String getDateParution() throws Exception { return this.dateParution; }
	public String getCreator() throws Exception { return this.creator; }
	public String getTitle() throws Exception { return this.title; }
	public void setDateParution(String dateParution) {
		this.dateParution = dateParution;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}

//
// POUR L'EXTRACTION DE KEYWORDS
//

public String getText4kwExtraction(ExtractorData data) throws Exception {
	SLDocument doc = data.getSLDocument();
	String x = SLUtils.getLabel(doc);
	String s = doc.getComment();
	if (s != null) x = x + " " + s;
	return x;
}
public Locale getLocale(ExtractorData data) throws Exception {
	return Locale.getDefault();
}
}
