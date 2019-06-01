package net.semanlink.sljena;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.semanlink.SLRuntimeException;

import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

public class JDocument extends JResource implements SLDocument {
private boolean dateComputed = false;
private String date;
private List<SLKeyword> keywords;

// CONSTRUCTION

public JDocument(JModel jModel, Resource res) {
  super(jModel, res);
}

// GETS

public Resource getRes() { return this.res; }
public String getURI() { return this.res.getURI(); } // ca va pas 

//IMPLEMENTS Comparable

public int hashCode() { return this.res.hashCode(); }

public List<SLKeyword> getKeywords() {
	if (this.keywords == null) this.keywords = computeKeywords();
	return this.keywords;
}

public List<SLKeyword> computeKeywords() {
  try {
	return this.jModel.getKeywordsList(this.res);
  } catch (Exception ex) { throw new SLRuntimeException(ex); }
}

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
	// la ligne suivante fonctionnait quand on n'avait qu'un seul modèle ds JModel.
	// On y trouvaut en effet ce qui décrivait le KW.
	// Or maintenant, le KW a pû être créé avec une res kw provenant d'un docsModel
	// (créé parce que un doc hasKayword un kw). On ne trouve alors pas
	// les infos sur le kw ds ce modèle.
	/*
	StmtIterator stmtIt = this.res.listProperties(RDFS.label);
	for (;stmtIt.hasNext();) { // normalement yen a qu'un - et on n'en prend qu'un
	Statement sta = stmtIt.next();
	return sta.getObject().toString();
	}
	*/
	Model model = this.jModel.getDocsModel();
	NodeIterator ite = model.listObjectsOfProperty(res, prop);
	// est-ce que ça va marcher ? Et bien oui,
	// ce qui n'était pas sûr et est intéressant quand on le compare
	// au code commenté plus haut. Prouesse de la POO.
	// QUESTION est-ce + lent ?
	// Dépend de l'implémentation de ModelMem. Peut l'être. Optim à voir.
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

public List<SLDocument> mainDocOf() {
	return this.jModel.mainDocOf(this.res);
}

}
