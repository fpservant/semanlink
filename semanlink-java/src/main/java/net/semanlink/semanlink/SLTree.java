package net.semanlink.semanlink;
import java.util.*;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.WalkListenerImpl;
/**
 * Arbre issu d'un kw
 * 
 * How to use: create an instance, create a SLTree.SLWalkListener (for instance extending SLTree.SLWalkListenerAdapter),
 * then call walk(SLWalkListener walkListener)
 */
public class SLTree extends GraphTraversal<Object> implements SLVocab {
static Integer UN = new Integer(1);
static Integer BONUS_POUR_PARENT = 2;
static Integer BONUS_POUR_RELATED = 2;
private SLKeyword root;
private String property;
/** null si pas de tri */
String sortProperty;
/** les docs de l'arbre. */
private LinkedHashSet hsDocs;
/** les kws de l'arbre (pas tous ceux liés aux docs !) 
 *  (un simple pointeur vers super.hs) */
// private HashSet hsKws;
/** les kws liés aux docs de l'arbre */
// private HashSet hsKwsOfDocs; // ceci, pour version sans calcul du nb d'occurrences des linkedkws
/** les kws liés aux docs de l'arbre. Clé kw lié au doc de l'arbre, data Integer nb d'occurences. 
 *  purgé des descendants de la racine une fois qu'on a appelé son getter. */
private HashMap hmKwsOfDocs;
/** Comment on accède aux voisins d'un kw : les fils ou les pères ? avec les docs?... */
VoisinsGetter getter;
private SLWalkListener slWalkListener;

//
//CONSTRUCTION
//

/**
* @param property defines the kind of links to be followed. SLVocab.HAS_PARENT_PROPERTY pour pere, autre chose : fils
*/
public SLTree(SLKeyword root, String property, String sortProperty, SLModel model) {
	super(new GraphImpl(seeds(root), voisinsGetter(property)));
	this.root = root;
	this.property = property;
	this.sortProperty = sortProperty;
	GraphImpl graph = (GraphImpl) getGraph();
	this.getter = graph.voisinsGetter;
}	

static private SLKeyword[] seeds(SLKeyword root) {
	SLKeyword[] seeds = new SLKeyword[1];
	seeds[0] = root;
	return seeds;	
}
static private VoisinsGetter voisinsGetter(String property) {
	VoisinsGetter getter = null;
	if (property.equals(SLVocab.HAS_PARENT_PROPERTY)) {
	getter = new ParentsGetter();
	} else {
	getter = new ChildrenGetter();
	}
	return getter;
}

static class GraphImpl implements Graph {
	private SLKeyword[] seeds;
	VoisinsGetter voisinsGetter;
	GraphImpl(SLKeyword[] seeds, VoisinsGetter voisinsGetter) {
		this.seeds = seeds;
		this.voisinsGetter = voisinsGetter;
	}
	public Iterator getNeighbors(Object node) throws Exception {
		List list = this.voisinsGetter.getKeywords((SLKeyword) node);
		return list.iterator();
	}
	
	public Object[] seeds() {
		return this.seeds;
	}
}


/**
 * Definit la liste des voisins d'un keyword donne.
 * C'est en l'implementant qu'on a programme les comportements "arbre des fils"
 * et "arbre des parents." On fera ensuite des implementations pour les friends,
 * etc.
 */
static interface VoisinsGetter {
	List getKeywords(SLKeyword kw);
	List getDocuments(SLKeyword kw);
	List getInvKeywords(SLKeyword kw);
}

static class ChildrenGetter implements VoisinsGetter {
	public List getKeywords(SLKeyword kw) { return kw.getChildren(); }
	public List getDocuments(SLKeyword kw) { return kw.getDocuments(); }
	public List getInvKeywords(SLKeyword kw) { return kw.getParents(); }
}

static class ParentsGetter implements VoisinsGetter {
	public List getKeywords(SLKeyword kw) { return kw.getParents(); }
	public List getDocuments(SLKeyword kw) { return kw.getDocuments(); }
	public List getInvKeywords(SLKeyword kw) { return kw.getChildren(); }
}

//
//
//

public interface SLWalkListener {
	public void startSeed(SLKeyword kw) throws Exception;
	/** début "sous-menu" de kw (children et/ou docs) */
	public void startList(SLLabeledResource kw) throws Exception;
	/** emis si kw n'a pas de sous liste (cad si startList pas émis) */
	public void noList(SLResource kw) throws Exception;
	public void startKwList(SLLabeledResource kw) throws Exception;
	/** emis si kw n'a pas de sous liste de kws (mais en a une de doc : pas émis si on a noList émis) */
	public void noKwList(SLLabeledResource kw) throws Exception;
	public void startKeyword(SLKeyword kw) throws Exception;
	public void endKeyword(SLKeyword kw) throws Exception;
	/** emis si on est deja tombe sur kw precedemment. */
	public void repeatKeyword(SLKeyword kw) throws Exception;
	public void endKwList(SLLabeledResource kw) throws Exception;
	public void startDocList(SLKeyword kw) throws Exception;
	/** emis si kw n'a pas de sous liste de docs (mais en a une de kws : pas émis si on a noList émis) */
	public void noDocList(SLLabeledResource kw) throws Exception;
	public void printDocument(SLDocument doc, SLKeyword currentKw, List kwsOfDoc) throws Exception;
	public void endDocList(SLKeyword kw) throws Exception;
	public void endList(SLLabeledResource kw) throws Exception;
	public void endSeed(SLKeyword kw) throws Exception;
}
public static class SLWalkListenerAdapter implements SLWalkListener {
	public void startSeed(SLKeyword kw) throws Exception {}
	public void startList(SLLabeledResource res) throws Exception {}
	/** emis si kw n'a pas de sous liste */
	public void noList(SLResource kw) throws Exception {}
	public void startKwList(SLLabeledResource kw) throws Exception {}
	/** emis si kw n'a pas de sous liste de kws (mais en a une de doc : pas émis si on a noList émis) */
	public void noKwList(SLLabeledResource kw) throws Exception {}
	public void startKeyword(SLKeyword kw) throws Exception {}
	public void endKeyword(SLKeyword kw) throws Exception {}
	/** emis si on est deja tombe sur kw precedemment. */
	public void repeatKeyword(SLKeyword kw) throws Exception {}
	public void endKwList(SLLabeledResource kw) throws Exception {}
	public void startDocList(SLKeyword kw) throws Exception {}
	/** emis si kw n'a pas de sous liste de docs (mais en a une de kws : pas émis si on a noList émis) */
	public void noDocList(SLLabeledResource kw) throws Exception {}
	public void printDocument(SLDocument doc, SLKeyword currentKw, List kwsOfDoc) throws Exception {}
	public void endDocList(SLKeyword kw) throws Exception {}
	public void endList(SLLabeledResource res) throws Exception {}
	public void endSeed(SLKeyword kw) throws Exception {}
}



/*
 * 
 * SLtreeNew(Graph)
 * new GraphWalkListener(treePosition)
 * 
 */
/*
public void walk(SLWalkListener walkListener) throws Exception {
	// System.out.println("SLTree.walk " + root);
	setWalkListener(walkListener);
	// this.hmKws = new HashMap();
	this.hsKws = new HashSet();
	this.hsDocs = new LinkedHashSet();
	// this.hsKwsOfDocs = new HashSet();
	this.hmKwsOfDocs = new HashMap();
	if (this.property.equals(SLVocab.HAS_PARENT_PROPERTY)) {
		this.getter = new ParentsGetter();
	} else {
		this.getter = new ChildrenGetter();
	}
	Stack treePosition = new Stack();
	treePosition.add(new Integer(0));
	walkListener.startWalk(root);
	walk(root, treePosition, walkListener);
	walkListener.endWalk(root);
}*/


public void walk(SLWalkListener slWalkListener) throws Exception {
	walk(slWalkListener, new Stack());
}
public void walk(SLWalkListener slWalkListener, Stack treePosition) throws Exception {
	// this.hsKws = this.hs;
	this.hsDocs = new LinkedHashSet();
	// this.hsKwsOfDocs = new HashSet();
	this.hmKwsOfDocs = new HashMap();

  GraphWalkListener graphWalkListener = new GraphWalkListener(slWalkListener, treePosition);
  walk(graphWalkListener, null, treePosition);
}


class GraphWalkListener extends WalkListenerImpl {
	private SLWalkListener slWalkListener;
	private Stack treePosition;

	/** pour la gestion des docs ds le sous-arbre */
	private Stack hsDocsStack;

	GraphWalkListener(SLWalkListener slWalkListener, Stack treePosition) {
		this.slWalkListener = slWalkListener;
		this.treePosition = treePosition;
	}
  public void startSeed(Object seed) throws Exception {
  	SLKeyword seedKw = (SLKeyword) seed;
  	slWalkListener.startSeed(seedKw);
		// 2007-04
		if (getter instanceof ChildrenGetter) {
			// we add the related to the tag cloud
			List list = seedKw.getFriends();
			for (int ii = 0; ii < list.size(); ii++) {
				add2count(BONUS_POUR_RELATED, list.get(ii));
			}
		}
  }
  public void startNeighborList(Object node) throws Exception {
  	SLKeyword kw = (SLKeyword) node;
   	slWalkListener.startList(kw);
   	slWalkListener.startKwList(kw);
  }
  
  public void startNode(Object node) throws Exception {
   	SLKeyword kw = (SLKeyword) node;
  	slWalkListener.startKeyword(kw);

  	/*
  	HashSet subHsDocs = new HashSet();
  	hsDocsStack.push(subHsDocs);
  	subHsDocs.addAll(docs);
  	*/
  	
  	/// CHANGER ICI LE BONUS_POUR_PARENT PAR NB DESCENDANTS
  	// We put in the tag cloud the parents of the kws of the tree
		List list = getter.getInvKeywords(kw);
		for (int ii = 0; ii < list.size(); ii++) {
			add2count(BONUS_POUR_PARENT, list.get(ii));
		}

		// 2007-04
		if (getter instanceof ChildrenGetter) {
			// we add the related to the tag cloud
			list = kw.getFriends();
			for (int ii = 0; ii < list.size(); ii++) {
				add2count(BONUS_POUR_RELATED, list.get(ii));
			}
		}
  }
  
  /** add howmany to count for kw -- if kw not on this.hs */
  private void add2count(int howmany, Object kw) {
		if (hs.contains(kw)) return;
		Integer nb = (Integer) hmKwsOfDocs.get(kw);
		if (nb == null) {
			hmKwsOfDocs.put(kw, new Integer(howmany));
		} else {
			hmKwsOfDocs.put(kw, new Integer(nb.intValue()+howmany));
		}
  }
  
  public void endNode(Object node) throws Exception {
  	slWalkListener.endKeyword((SLKeyword) node);
  }
  
  public void repeatNode(Object node) throws Exception {
  	slWalkListener.repeatKeyword((SLKeyword) node);
  }
  public void noNeighborList(Object node) throws Exception {
  	SLKeyword kw = (SLKeyword) node;
  	List docs = getter.getDocuments(kw);
		if (docs.size() < 1) {
			slWalkListener.noList(kw);
		} else {
			slWalkListener.startList(kw);
			handleDocList(kw,docs);
			slWalkListener.endList(kw);
		}
  }
  public void endNeighborList(Object node) throws Exception {
  	SLKeyword kw = (SLKeyword) node;
		slWalkListener.endKwList(kw);
  	List docs = getter.getDocuments(kw);
		if (docs.size() < 1) {
			slWalkListener.noDocList(kw);
		} else {
			handleDocList(kw,docs);
		}
		slWalkListener.endList(kw);
  }
  public void endWalk(Object seed) throws Exception {
  	slWalkListener.endSeed((SLKeyword) seed);
  }	
  
  /** docs supposé non vide */
  private void handleDocList(SLKeyword kw, List docs) throws Exception {
		slWalkListener.startDocList(kw);
  	
  	/*HashSet subHsDocs = new HashSet();
  	hsDocsStack.push(subHsDocs);
  	subHsDocs.addAll(docs);*/
  	
  	if (sortProperty != null) {
  		if (SLVocab.HAS_KEYWORD_PROPERTY.equals(sortProperty)) {
  			SLKeyword[] k = new SLKeyword[1]; k[0] = kw;
  			SLUtils.sortDocsByKws(docs, k);
  		} else {
  			SLUtils.sortByProperty(docs, sortProperty);
  		}
  	}

		// int index = currentVoisins.size();
		int ndocs = docs.size();
		// for (int i = 0; i < ndocs; i++, index++) {
		for (int i = 0; i < ndocs; i++) {
			// treePosition.push(new Integer(index));
			SLDocument doc = (SLDocument) docs.get(i);
			List kwsOfDoc = doc.getKeywords();
			// documenter l'ensemble de tous les docs
			// Attention, on a déjà pu tomber sur ce doc :
			// il ne faut pas, dans ce cas, augmenter le compteur des linked kws
			if (!(hsDocs.contains(doc))) {
				hsDocs.add(doc);
				// documenter les "LinkedKeywords"
				for (int j = 0; j < kwsOfDoc.size(); j++) {
					// this.hsKwsOfDocs.add(kwsOfDoc.get(j));
					Object kwo = kwsOfDoc.get(j);
					Integer nb = (Integer) hmKwsOfDocs.get(kwo);
					if (nb == null) {
						hmKwsOfDocs.put(kwo, UN);
					} else {
						hmKwsOfDocs.put(kwo, new Integer(nb.intValue()+1));
					}
				}
			}
			// même s'il a déjà été affiché pour un autre kw, on réaffiche le doc
			// slWalkListener.printDocument(doc, treePosition, kw, kwsOfDoc);
			slWalkListener.printDocument(doc, kw, kwsOfDoc);
			// treePosition.pop();
		}

  	slWalkListener.endDocList(kw);
  }
}


//
// PPTIES
//

/** All docs linked to this tree */
public HashSet getDocsSet() throws Exception {
	if (this.hsDocs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListenerAdapter();
		}
		walk(this.slWalkListener);
	}
	return this.hsDocs;
}

/** All docs linked to this tree
 *  liste parallele au parcours - à ceci près que les éléments ne sont pas dupliqués. */
public SLDocument[] getDocs() throws Exception {
	return (SLDocument[]) getDocsSet().toArray(new SLDocument[0]);
}

/** les kws de l'arbre (pas tous ceux liés aux docs !) 
 *  Si un parcours a déjà, été fait, identique à super.getNodes().
 *  Si pas encore fait, n'a pas le même résultat que (puisque l'appel ds super fait
 *  un super.simpleWalk) Donc si on n'a pas besoin des docs, faire super.getNodes()  */
public HashSet getNodes() throws Exception {
	if (this.hs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListenerAdapter();
		}
		walk(this.slWalkListener);
	}
	return this.hs;
}

/** Tous les kws liés aux docs.
 *  Des "descendants" de la racine peuvent donc être présents (alors qu'ils sont purgés dans getLinkedKws)
 *  Version sans nb d'occurences
 **/
public SLKeyword[] getKwsOfDocs() throws Exception {
	if (this.hmKwsOfDocs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListenerAdapter();
		}
		walk(this.slWalkListener);
	}
	SLKeyword[] x = (SLKeyword[]) this.hmKwsOfDocs.keySet().toArray(new SLKeyword[0]);
	Arrays.sort(x);
	return x;
}

/** Les descendants de la racine en sont purgés. List de SLKeyword */
public List getLinkedKws() throws Exception {
	SLKeyword[] allKws = getKwsOfDocs();
	ArrayList x = new ArrayList(allKws.length);
	// purge des descendants de la racine
	for (int i = 0; i < allKws.length; i++) {
		SLKeyword kw = allKws[i];
		if (!this.hs.contains(kw)) x.add(kw);
	}
	Collections.sort(x);
	return x;
}

/** Les descendants de la racine en sont purgés. */
// public HashMap getLinkedKeywordsWithNb() throws Exception {
public HashMap getLinkedKeywords2NbHashMap() throws Exception {
	if (this.hmKwsOfDocs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListenerAdapter();
		}
		walk(this.slWalkListener);
	}
	// purge des descendants de la racine
	// on procède différemment selon les taille respectives de hsKws et hmKwsOfDocs
	// puisqu'il s'agit de purger l'un avec l'autre.
	int n_hsKws = this.hs.size() ;
	int n_hmKwsOfDocs = this.hmKwsOfDocs.size();
	if (n_hsKws < 3*n_hsKws) { // pourquoi * 3 ? parce qu'il y a plus d'opérations ds l'autre façon
		Iterator it = this.hs.iterator() ;
		for (int i = 0; i < n_hsKws; i++) {
			Object kwo = it.next();
			this.hmKwsOfDocs.remove(kwo);
		}
	} else {
		SLKeyword[] linked = (SLKeyword[]) this.hmKwsOfDocs.keySet().toArray(new SLKeyword[n_hmKwsOfDocs]);
		for (int i = 0; i < n_hmKwsOfDocs; i++) {
			Object kwo = linked[i];
			if (this.hs.contains(kwo)) this.hmKwsOfDocs.remove(kwo);
		}
	}
	
	return this.hmKwsOfDocs;
}

//
// WALK
//

public void setWalkListener(SLWalkListener slWalkListener) {
	this.slWalkListener = slWalkListener;
	// this.listDocs = null; 
}

// ya des trucs a virer qui ne servent pas pour jsp

/** retourne le nb de doc dans l'arbre */
// attention, ne doit pas être exact : un doc listé plusieurs fois 
// doit être compté plusieurs fois
// Pas très grave (et même pas mal) : sert pour le poids des kws
/*
protected int walk(SLKeyword kw, Stack treePosition, SLWalkListener walkListener) throws Exception{
	List kws = this.getter.getKeywords(kw);
	int nkws = kws.size();
	// List kwsVoisins = new ArrayList(nkws);
	List docs = this.getter.getDocuments(kw);
	
	if (this.sortProperty != null) {
		if (SLVocab.HAS_KEYWORD_PROPERTY.equals(this.sortProperty)) {
			SLKeyword[] k = new SLKeyword[1]; k[0] = kw;
			SLUtils.sortDocsByKws(docs, k);
		} else {
			SLUtils.sortByProperty(docs, this.sortProperty);
		}
	}
	int ndocs = docs.size();
	// we compute the number of docs in the tree
	int x = ndocs;
	// Node node = new Node(kw, kwsVoisins, docs);		
	// this.hmKws.put(kw, node);
	this.hsKws.add(kw);
	if ((nkws < 1) && (ndocs < 1)) {
		walkListener.noList(kw, treePosition);
	} else {
		walkListener.startList(kw, treePosition);
				
		// the keywords
		if (nkws < 1) {
			walkListener.noKwList(kw, treePosition);
		} else {
			walkListener.startKwList(kw, treePosition);
			for (int i = 0; i < nkws; i++) {
				treePosition.push(new Integer(i));
				SLKeyword voisin = (SLKeyword) kws.get(i);
				if (!this.hsKws.contains(voisin)) {
					walkListener.startKeyword(voisin, treePosition);
					int xsub = walk(voisin, treePosition, walkListener);
					walkListener.endKeyword(voisin, treePosition);
					x += xsub;
					// we augment the count of docs of parents
					// C'est un moyen de donner du poids à un kw
					// dont les fils ont des docs associés.
					// (c'est à un seul niveau : n'augmente que le poids des pères,
					// pas des grands parents)
					List invKws = this.getter.getInvKeywords(voisin);
					for (int iinv = 0; iinv < invKws.size(); iinv ++) {
						Object invKw = invKws.get(iinv);
						if (invKw.equals(kw)) continue;
						Integer nb = (Integer) this.hmKwsOfDocs.get(invKw);
						if (nb == null) {
							this.hmKwsOfDocs.put(invKw, new Integer(xsub));
						} else {
							this.hmKwsOfDocs.put(invKw, new Integer(nb.intValue()+xsub));
						}
						
					}
				} else {
					walkListener.repeatKeyword(voisin, treePosition);
				}
				treePosition.pop();
			}	
			walkListener.endKwList(kw, treePosition);
		}
		
		// the docs 
		if (ndocs < 1) {
			walkListener.noDocList(kw, treePosition);
		} else {
			walkListener.startDocList(kw, treePosition);
			int index = nkws;
			for (int i = 0; i < ndocs; i++, index++) {
				treePosition.push(new Integer(index));
				SLDocument doc = (SLDocument) docs.get(i);
				List kwsOfDoc = doc.getKeywords();
				// documenter l'ensemble de tous les docs
				// Attention, on a déjà pu tomber sur ce doc :
				// il ne faut pas, dans ce cas, augmenter le compteur des linked kws
				if (!(this.hsDocs.contains(doc))) {
					this.hsDocs.add(doc);
					// documenter les "LinkedKeywords"
					for (int j = 0; j < kwsOfDoc.size(); j++) {
						// this.hsKwsOfDocs.add(kwsOfDoc.get(j));
						Object kwo = kwsOfDoc.get(j);
						Integer nb = (Integer) this.hmKwsOfDocs.get(kwo);
						if (nb == null) {
							this.hmKwsOfDocs.put(kwo, UN);
						} else {
							this.hmKwsOfDocs.put(kwo, new Integer(nb.intValue()+1));
						}
					}
				}
				// même s'il a déjà été affiché pour un autre kw, on réaffiche le doc
				walkListener.printDocument(doc, treePosition, kw, kwsOfDoc);
				treePosition.pop();
			}
			walkListener.endDocList(kw, treePosition);
		}

		walkListener.endList(kw, treePosition);
	}
	return x;
}
*/


//
// GETTERS
//

public String getSortProperty() { return this.sortProperty; }
/**
 * Retourne true ssi cet arbre a été créé avec sortProperty, 
 * cad si on n'a pas besoin de le recalculer.
 * @param sortProperty : si null, tjrs true : en effet, peu importe la façon
 * dont this est trié.
 */
public boolean isValid(String sortProperty) {
	if (sortProperty == null) return true;
	return (sortProperty.equals(this.sortProperty));
}

} // class

