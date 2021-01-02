package net.semanlink.sljena;
import java.util.HashMap;
import java.util.List;

import net.semanlink.semanlink.LabelLN;
import net.semanlink.semanlink.LabelLNImpl;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.skos.SKOS;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class JKeyword extends JResource implements SLKeyword {

// CONSTRUCTION

/*
 * NORMALEMENT, res devrait être une resource créée dans un KWsModel 
 * -- sauf que ca ne drvrait pas aller, par ex, pour getDocuments
 * (qui recherche ds le model des documents)
 * MANISFESTEMENT, il se trouve que Jena ne se préoccupe pas du model
 * auquel appartient la resource quand il fait ses recherches (doit faire une recherche
 * dans un map de clé l'uri) -- AU MOINS POUR LES MODEL MEM
 * (Ceci serait peut-être un problème avec d'autres implémentations)
 * (il faudrait voir en particulier node2JKeyword ds JModel)
 */

/** @param res est une resource de rdfs:Class JKeyword. */
public JKeyword(JModel jModel, Resource res) {
  super(jModel, res);
}

// GETS

public Resource getRes() { return this.res; }

//IMPLEMENTS Comparable

public int hashCode() { return this.res.hashCode(); }

//IMPLEMENTS SLKeyword

@Override public LabelLN getLabelLN() { // 2021-01
	return getLabelLN(this.jModel.getKWsModel(), this.res);
}

@Override public LabelLN getCommentLN() { // 2021-01
  return JenaUtils.getCommentLN(this.res);
}

static public LabelLN getLabelLN(Model model, Resource res) { // 2021-01
	return SLJenaUtils.getLabelLN(model, res, model.createProperty(SLVocab.PREF_LABEL_PROPERTY));
}

public String getLabel() {
	return getLabel(this.jModel.getKWsModel(), this.res);
}

/** essaye de trouver le libellé pour language, sinon un autre */
public String getLabel(String language) {
	return getLabel(this.jModel.getKWsModel(), this.res, language);
}

static public String getLabel(Model model, Resource res) {
	return getLabelLN(model, res).getLabel();
}

static public String getLabel(Model model, Resource res, String language) {
  NodeIterator ite = model.listObjectsOfProperty(res, model.createProperty(SLVocab.PREF_LABEL_PROPERTY));
  String x = null;
  try {
  	Literal literal = null;
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (!(node instanceof Literal)) continue;
			literal = (Literal) node;
			if (!(language.equals(literal.getLanguage()))) continue;
			x = literal.getString();
			x = x.trim();
			if (x.length() > 0) return x;
		}
		if (literal != null) {
			// on tente le coup : peut-être que literal est un rvai label pour un autre language
			x = (literal).getString();
			x = x.trim();
			if (x.length() > 0) return x;
			// mais le coup a peut-être raté : on a pu retomber sur un truc vide
			return getLabel(model, res);
		}
  } finally {
  	ite.close();
  }
	// return res2shortString(res);
	return res.getURI();
} // getLabel()


String res2shortString() {
	return res2shortString(this.res);
}
static String res2shortString(Resource res) {
	String s = res.toString();
	// int n = s.lastIndexOf("#"); // #thing ?
	int n = s.lastIndexOf("/"); // #thing ?
	return s.substring(n);
}


public List<SLKeyword> getParents() {
  return this.jModel.getParentsList(this.res);
}

public List<SLKeyword> getChildren() {
	return this.jModel.getChildrenList(this.res);
}

public List<SLKeyword> getFriends() {
	return this.jModel.getFriendsList(this.res);
}


public boolean hasChild() {
	return this.jModel.hasChild(this.res);
}

public List<SLDocument> getDocuments() {
  return this.jModel.getDocumentsList(this.res);
}

public boolean hasDocument() {
	return this.jModel.hasDocument(this.res);
}


public String getComment() {
	return JenaUtils.getComment(this.res);
}


// TODO 

/** Retourne une HashMap de cle les Property, et de data une ArrayList des RDFNode objects */
public HashMap getProperties() {
	return JenaUtils.getProperties(this.res);
}
public HashMap getPropertiesAsStrings() {
	return JenaUtils.getPropertiesAsStrings(this.res);
}

//
//
//

/*public String getHomePageURI() {
	Model kwsModel = this.jModel.getKWsModel();
	Property p = kwsModel.createProperty(SL_HOME_PAGE_PROPERTY); // todo optim
	NodeIterator it = kwsModel.listObjectsOfProperty(p);
	Resource homePage = null;
	for (;it.hasNext();) {
		Object o = it.next();
		if (o instanceof Resource) {
			homePage = (Resource) o;
			break;
		}
	}
	it.close();
	if (homePage != null) return homePage.getURI();
	else return null;
}*/

@Override
public List<LabelLN> getAltLabels() { // 2021-01
	return SLJenaUtils.getLabelLNs(this.jModel.getKWsModel(), this.res, SKOS.altLabel);
}

} // class
