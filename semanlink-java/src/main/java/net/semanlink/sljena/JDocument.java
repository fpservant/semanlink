package net.semanlink.sljena;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.servlet.SLServlet;
import net.semanlink.semanlink.SLRuntimeException;
import net.semanlink.semanlink.SLTree;

import java.util.*;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

public class JDocument extends JResource implements SLDocument {
private boolean dateComputed = false;
private String date;

// 2020-11: all cached attributes must be rest when updating (cf. onCloseUpdate)
private List<SLKeyword> keywords; // use getter // 2020-04 not updated - but generally we don't need updates...

// CONSTRUCTION

public JDocument(JModel jModel, Resource res) {
  super(jModel, res);
}

/** reset cached attributes when updating */
void onCloseUpdate() { // 2020-11
	this.keywords = null;
}

// GETS

public Resource getRes() { return this.res; }
public String getURI() { return this.res.getURI(); } // ca va pas 

// IMPLEMENTS Comparable

public int hashCode() { return this.res.hashCode(); }

public List<SLKeyword> getKeywords() {
	// 2020-11
////	if (this.keywords == null) this.keywords = computeKeywords();
////	return this.keywords;
//  try {
//  	return this.jModel.getKeywordsList(this.res);
//  } catch (Exception ex) { throw new SLRuntimeException(ex); }
	if (this.keywords == null) {
		try {
			this.keywords = this.jModel.getKeywordsList(this.res);
		} catch (Exception ex) { throw new SLRuntimeException(ex); }
	}
	return this.keywords;
}

//public List<SLKeyword> computeKeywords() {
//  try {
//  	return this.jModel.getKeywordsList(this.res);
//  } catch (Exception ex) { throw new SLRuntimeException(ex); }
//}

public String getLabel() {
	String x = null;
	
	x = getLabel(RDFS.label);
	if (x != null) return x;
	
	x = getLabel(DC.title);
	/* if (x != null) return x;
	
	// ce qui suit est envoyé ds Jsp_Page.getLabel(SLDocument) 
	// par defaut, on prend la fin de l'uri
	x = getURI();
	if ( (x.startsWith("file:")) && !(x.endsWith("/")) ) {
		int n = x.lastIndexOf('/');
		x = x.substring(n+1);
	} 
	// comme uri est encodee, il faut la decoder pour l'afficher sous une forme "human compliant"
	try {
		x = URLDecoder.decode(x, "UTF-8");
	} catch (UnsupportedEncodingException e) { throw new SLRuntimeException(e); }
	*/
	return x;
}

private String getLabel(Property prop) {
	Model model = this.jModel.getDocsModel();
	NodeIterator ite = model.listObjectsOfProperty(res, prop);
	for (;ite.hasNext();) { // normalement yen a qu'un - et on n'en prend qu'un
		RDFNode node = ite.nextNode();
		ite.close();
		if (node instanceof Literal) {
			Literal literal = (Literal) node;
			try {
				return literal.getString();
			} catch (Exception e) {
				return node.toString(); 
			}
		} else {
			return node.toString(); 
		}
	}
	return null;
}
public String getComment() {
  return JenaUtils.getComment(this.res);
}
public String getDate() {
	if (!this.dateComputed) {
		this.date = computeDate();
	}
	return this.date;
}
private String computeDate() {
	String x = null;
	x = getLabel(this.jModel.getDocsModel().getProperty(SLVocab.SL_CREATION_DATE_PROPERTY));
	//if (x != null) return x;
	//x = getLabel(SLVocab.DATE_PARUTION_PROPERTY);
	return x;
}
public String toString() {
  try {
    String x = getLabel();
    if (x != null) return x;
    return this.getURI();
  } catch (Throwable t) { return t.toString(); }
}

// TODO 

public HashMap getPropertiesAsStrings() {
	return JenaUtils.getPropertiesAsStrings(this.res);
}


/* (non-Javadoc)
 * @see net.semanlink.semanlink.SLDocument#getProperties()
 */
public HashMap getProperties() {
	return JenaUtils.getProperties(this.res);
}

@Override public List<SLDocument> mainDocOf() {
	return this.jModel.mainDocOf(this.res);
}

//
//
//

/** docs with similar tags */
@Override public List<SLDocument> relatedDocs() { // 2020-11
	HashMap<SLDocument, Integer> m = new HashMap<>();
	List<SLKeyword> kws = getKeywords(); // only exact match
	int max = 0;
	for (SLKeyword kw : kws) {
		// List<SLDocument> docs = kw.getDocuments(); // only exact match
		List<SLDocument> docs = longDocs(kw); // match on descendants
		for (SLDocument doc : docs) {
			if (doc.getURI().equals(this.getURI())) continue;
			Integer ii = m.get(doc);
			if (ii == null) {
				ii = new Integer(1);
				// m.put(doc, ii);
			} else {
				ii += 1;
			}
			m.put(doc, ii);
			if (ii > max) max = ii.intValue();
		}
	}
	List<SLDocument> x = new ArrayList<>();
	for (Entry<SLDocument, Integer> d_i : m.entrySet()) {
		if (isSimilar(d_i.getValue().intValue(), max)) {
			x.add(d_i.getKey());
		}
	}
	return x;
}

private List<SLDocument> longDocs(SLKeyword kw) {
	SLTree tree = new SLTree(kw, "children", null, SLServlet.getSLModel());
	try {
		return Arrays.asList(tree.getDocs());
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

/*
 new SLTree(this.slKw, "children", sortProperty, SLServlet.getSLModel())
 */
private boolean isSimilar(int i, int max) {
	if (i > 2) return true;
	if (i == max) return true;
	return false;
}

}
