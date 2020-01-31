package net.semanlink.semanlink;
import java.util.*;
/**
* Arbre issu d'un kw
*/
public class SLFastTree implements SLVocab {
private SLKeyword root;
private String property;
/** les kws de l'arbre (pas tous ceux liés aux docs !) */
private HashSet<SLKeyword> hsKws;
/** Comment on accède aux voisins d'un kw : les fils ou les pères ? avec les docs?... */
private VoisinsGetter getter;

//
//RESULTS
//

public HashSet<SLKeyword> getKwsSet() throws Exception {
	if (this.hsKws == null) walk();
	return this.hsKws;
}

//
//CONSTRUCTION
//

/**
* @param property defines the kind of links to be followed. SLVocab.HAS_PARENT_PROPERTY pour pere, autre chose : fils
*/
public SLFastTree(SLKeyword root, String property, SLModel model) {
	this.root = root;
	this.property = property;
	if (this.property.equals(SLVocab.HAS_PARENT_PROPERTY)) {
		this.getter = new ParentsGetter();
	} else {
		this.getter = new ChildrenGetter();
	}
}

//
//WALK
//

public void walk() throws Exception {
	this.hsKws = new HashSet<SLKeyword>();
	walk(root);
}
protected void walk(SLKeyword kw) throws Exception{
	List<SLKeyword> kws = this.getter.getKeywords(kw);
	int nkws = kws.size();
	this.hsKws.add(kw);
	for (int i = 0; i < nkws; i++) {
		SLKeyword voisin = kws.get(i);
		if (!this.hsKws.contains(voisin)) {
			walk(voisin);
		} else {
			// walkListener.repeatKeyword(voisin, treePosition);
		}
	}	
}

/**
* Definit la liste des voisins d'un keyword donne.
* C'est en l'implementant qu'on a programme les comportements "arbre des fils"
* et "arbre des parents." On fera ensuite des implementations pour les friends,
* etc.
*/
interface VoisinsGetter {
	List<SLKeyword> getKeywords(SLKeyword kw);
	List<SLDocument> getDocuments(SLKeyword kw);
}

class ChildrenGetter implements VoisinsGetter {
	public List<SLKeyword> getKeywords(SLKeyword kw) {
		return kw.getChildren();
	}
	public List<SLDocument> getDocuments(SLKeyword kw) { return kw.getDocuments(); }
}

class ParentsGetter implements VoisinsGetter {
	public List<SLKeyword> getKeywords(SLKeyword kw) { return kw.getParents(); }
	public List<SLDocument> getDocuments(SLKeyword kw) { return kw.getDocuments(); }
}

//
//
//


} // class

