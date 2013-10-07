package net.semanlink.servlet;
import net.semanlink.semanlink.*;

import java.util.*;
import java.io.IOException;
import javax.servlet.http.*;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Permet de faire des pages affichant une liste de docs qui supporte, en plus,
 * le AND avec des mots clés. 
 * 
 * Utilisation :
 * Overrider computeDocs pour définir comment on dresse la liste des docs - sans tenir compte
 * du AND éventuel avec des keywords.
 * Appeler setDocs à la fin de la construction (ou juste après) pour <OL>
 * 	<LI>induire l'appel à computeDocs</LI>
 * 	<LI>faire le AND éventuel</LI>
 * </OL>
 * @author fps
 */
public abstract class Jsp_DocumentList extends Jsp_Page {
/** au cas où on fait un and avec des kws */
protected SLKeyword[] kws;
// use getter
protected List docs; // finalement éventuellement filtré par les kws

//
// CONSTRUCTION
//

/**
 * @params kwUris pour faire un AND des SLDocuments de docs (déterminé par computeDocs) 
 * avec les kws en question
 * APPELER ensuite setDocs (ceci est fait ainsi pour qu'on puisse, ds le constructeur, avant cet appel à setDocs,
 * et donc à computeDocs et filtrage par les  kws, setter des choses qui influent le computeDocs
 */
public Jsp_DocumentList(String[] kwUris, HttpServletRequest request) throws Exception {
	super(request);
	if (kwUris != null) {
		this.kws = new SLKeyword[kwUris.length];
		SLModel mod = SLServlet.getSLModel();
		for (int i = 0; i < kwUris.length; i++) {
			this.kws[i] = mod.getKeyword(kwUris[i]);
		}
	}
}

/** doit être appelée pour compléter la construction */
public final void setDocs() throws Exception {
	this.docs = computeDocs();
	if (this.kws != null) {
		filterDocsByKws(this.docs, kws, !getDisplayMode().isLongListOfDocs());
	}
}

/** liste de SLDocument : la liste des docs, non filtrée (par l'éventuel AND de kws)*/
protected abstract List computeDocs() throws Exception;

//
//
//

// A mettre ds SLModel ?
/** modifie docs. Fait un AND avec les kws. */
public static void filterDocsByKws(List docs, SLKeyword[] kws, boolean childrenOnly) throws Exception {
	for (int i = 0; i < kws.length ;i++) {
		HashSet set = null;
		if (childrenOnly) {
			set = new HashSet(kws[i].getDocuments());
		} else {
			SLTree tree = new SLTree(kws[i], "children", null, SLServlet.getSLModel());
			set = tree.getDocsSet();
		}
		for (int j = docs.size()-1; j > -1; j--) {
			Object oDoc = docs.get(j);
			if (!(set.contains(oDoc))) {
				docs.remove(j);
			}
		}
	}
}

public abstract String getTitle() throws Exception;

/** non nécessairement triée */
public List getDocs() throws Exception { return this.docs; }

/** triée */
public Bean_DocList getDocList() throws Exception {
	Bean_DocList x = new Bean_DocList();
	List docList = getDocs();
	sort(docList);
	x.setList(docList);
	x.setShowKwsOfDocs(true, null);
	return x;
}

/*public abstract String getLinkToThis(String action) throws UnsupportedEncodingException;
public abstract String getLinkToThis() throws UnsupportedEncodingException;*/

/** la liste des mots clés liés, cad ayant un doc en commun */
public SLKeyword[] getLinkedKeywords() throws Exception {
	List docs = getDocs();
	HashSet hs = SLUtils.getKeywords(docs);
	SLKeyword[] x = (SLKeyword[]) hs.toArray(new SLKeyword[0]);
	Arrays.sort(x);
	return x;
}

public HashMap getLinkedKeywords2NbHashMap() throws Exception {
	List docs = getDocs();
	return SLUtils.getLinkedKeywords2NbHashMap(docs);
}

/** "lien vers un mot clé lié" cad vers un AND de this et du mot clé 
 * On utilisera pour l'implémentation la la méthode andOtherKw.
 * POURQOUI NE PAS FAIRE DS L'AUTRE SENS : CHERCHER LE ET DE KWS, PUIS FAIRE AND CECI ??? */
public abstract HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException;

/** retourne this.kws augmenté de otherKw. */
protected SLKeyword[] andOtherKw(SLKeyword otherKw) {
	SLKeyword[] x = null;
	if (this.kws != null) {
		x = new SLKeyword[this.kws.length+1];
		for (int i = 0; i < kws.length; i++) {
			x[i] = kws[i];
		}
		x[kws.length] = otherKw;
	} else {
		x = new SLKeyword[1];
		x[0] = otherKw;
	}
	return x;
}

//
//
//

public String getContent() throws Exception {
	return "/jsp/documentlist.jsp";
}

public String aboutList() throws Exception {
	return this.getDocs().size() + " documents";
}

//
//
//

} // class
