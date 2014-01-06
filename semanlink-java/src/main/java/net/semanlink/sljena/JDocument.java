package net.semanlink.sljena;
import net.semanlink.semanlink.SLDocumentAdapter;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.semanlink.SLRuntimeException;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

public class JDocument extends SLDocumentAdapter {
private Resource res;
private JModel slModel;
private boolean dateComputed = false;
private String date;

// CONSTRUCTION

/** @param res est une resource de rdfs:Class SLKeyword. */
public JDocument(JModel slModel, Resource res) {
  super(res.getURI());
  this.slModel = slModel;
  this.res = res;
  // System.out.println("JKeyword.NEW : " + res);
}

// GETS

public Resource getRes() { return this.res; }
public String getURI() { return this.res.getURI(); } // ca va pas 

//IMPLEMENTS Comparable

public int hashCode() { return this.res.hashCode(); }

public List<SLKeyword> computeKeywords() {
  try {
	return this.slModel.getKeywordsList(this.res);
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
	Model model = this.slModel.getDocsModel();
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
	x = getLabel(this.slModel.getDocsModel().getProperty(SLVocab.SL_CREATION_DATE_PROPERTY));
	//if (x != null) return x;
	//x = getLabel(SLVocab.DATE_PARUTION_PROPERTY);
	return x;
}
public String toString() {
  try {
    return getLabel();
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
}
