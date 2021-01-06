package net.semanlink.semanlink;
import java.util.*;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.graph.WalkListener;
/**
 * Arbre issu d'un kw
 * 
 * How to use: create an instance, create a SLTree.SLWalkListener,
 * then call walk(SLWalkListener walkListener)
 */
public class SLTree extends GraphTraversal<SLKeyword> implements SLVocab {
static Integer UN = new Integer(1);
static Integer BONUS_POUR_PARENT = 2;
static Integer BONUS_POUR_RELATED = 2;
private SLKeyword root;
private String property;
/** null si pas de tri */
String sortProperty;
/** les docs de l'arbre. */
private LinkedHashSet<SLDocument> hsDocs;
/** les kws de l'arbre (pas tous ceux liés aux docs !) 
 *  (un simple pointeur vers super.hs) */
// private HashSet hsKws;
/** les kws liés aux docs de l'arbre */
// private HashSet hsKwsOfDocs; // ceci, pour version sans calcul du nb d'occurrences des linkedkws
/** les kws liés aux docs de l'arbre. Clé kw lié au doc de l'arbre, data Integer nb d'occurences. 
 *  purgé des descendants de la racine une fois qu'on a appelé son getter. */
HashMap<SLKeyword, Integer> hmKwsOfDocs;
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

static class GraphImpl implements Graph<SLKeyword> {
	private SLKeyword[] seeds;
	VoisinsGetter voisinsGetter;
	GraphImpl(SLKeyword[] seeds, VoisinsGetter voisinsGetter) {
		this.seeds = seeds;
		this.voisinsGetter = voisinsGetter;
	}
	public Iterator<SLKeyword> getNeighbors(SLKeyword node) throws Exception {
		List<SLKeyword> list = this.voisinsGetter.getKeywords(node);
		return list.iterator();
	}
	
	public SLKeyword[] seeds() {
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
	List<SLKeyword> getKeywords(SLKeyword kw);
	List<SLDocument> getDocuments(SLKeyword kw);
	List<SLKeyword> getInvKeywords(SLKeyword kw);
}

static class ChildrenGetter implements VoisinsGetter {
	public List<SLKeyword> getKeywords(SLKeyword kw) { return kw.getChildren(); }
	public List<SLDocument> getDocuments(SLKeyword kw) { return kw.getDocuments(); }
	public List<SLKeyword> getInvKeywords(SLKeyword kw) { return kw.getParents(); }
}

static class ParentsGetter implements VoisinsGetter {
	public List<SLKeyword> getKeywords(SLKeyword kw) { return kw.getParents(); }
	public List<SLDocument> getDocuments(SLKeyword kw) { return kw.getDocuments(); }
	public List<SLKeyword> getInvKeywords(SLKeyword kw) { return kw.getChildren(); }
}

//
//
//

public interface SLWalkListener {
	default void startSeed(SLKeyword kw) throws Exception {}
	/** début "sous-menu" de kw (children et/ou docs) */
	default void startList(SLLabeledResource kw) throws Exception {}
	/** emis si kw n'a pas de sous liste (cad si startList pas émis) */
	default void noList(SLResource kw) throws Exception {}
	default void startKwList(SLLabeledResource kw) throws Exception {}
	/** emis si kw n'a pas de sous liste de kws (mais en a une de doc : pas émis si on a noList émis) */
	default void noKwList(SLLabeledResource kw) throws Exception {}
	default void startKeyword(SLKeyword kw) throws Exception {}
	default void endKeyword(SLKeyword kw) throws Exception {}
	/** emis si on est deja tombe sur kw precedemment. */
	default void repeatKeyword(SLKeyword kw) throws Exception {}
	default void endKwList(SLLabeledResource kw) throws Exception {}
	default void startDocList(SLKeyword kw) throws Exception {}
	/** emis si kw n'a pas de sous liste de docs (mais en a une de kws : pas émis si on a noList émis) */
	default void noDocList(SLLabeledResource kw) throws Exception {}
	default void printDocument(SLDocument doc, SLKeyword currentKw, List<SLKeyword> kwsOfDoc) throws Exception {}
	default void endDocList(SLKeyword kw) throws Exception {}
	default void endList(SLLabeledResource kw) throws Exception {}
	default void endSeed(SLKeyword kw) throws Exception {}
}

public void walk(SLWalkListener slWalkListener) throws Exception {
	walk(slWalkListener, new Stack<Integer>());
}
public void walk(SLWalkListener slWalkListener, Stack<Integer> treePosition) throws Exception {
	// this.hsKws = this.hs;
	this.hsDocs = new LinkedHashSet<>();
	// this.hsKwsOfDocs = new HashSet();
	this.hmKwsOfDocs = new HashMap<>();

  GraphWalkListener graphWalkListener = new GraphWalkListener(slWalkListener, treePosition);
  walk(graphWalkListener, null, treePosition);
}


class GraphWalkListener implements WalkListener<SLKeyword> {
	private SLWalkListener slWalkListener1;
	private Stack<Integer> treePosition;

	/** pour la gestion des docs ds le sous-arbre */
	// private Stack hsDocsStack;

	GraphWalkListener(SLWalkListener slWalkListener, Stack<Integer> treePosition) {
		this.slWalkListener1 = slWalkListener;
		this.treePosition = treePosition;
	}
  public void startSeed(SLKeyword seed) throws Exception {
  	slWalkListener1.startSeed(seed);
  	// 2020-01 on ne le fait plus (déjà qu'on en a trop)
//		// 2007-04
//		if (getter instanceof ChildrenGetter) {
//			// we add the related to the tag cloud
//			List<SLKeyword> list = seed.getFriends();
//			for (int ii = 0; ii < list.size(); ii++) {
//				add2count(BONUS_POUR_RELATED, list.get(ii));
//			}
//		}
  }
  public void startNeighborList(SLKeyword kw) throws Exception {
   	slWalkListener1.startList(kw);
   	slWalkListener1.startKwList(kw);
  }
  
  public void startNode(SLKeyword kw) throws Exception {
  	slWalkListener1.startKeyword(kw);

  	/// CHANGER ICI LE BONUS_POUR_PARENT PAR NB DESCENDANTS
  	// We put in the tag cloud the parents of the kws of the tree
		List<SLKeyword> list = getter.getInvKeywords(kw);
		for (int ii = 0; ii < list.size(); ii++) {
			add2count(BONUS_POUR_PARENT, list.get(ii));
		}

  	// 2020-01 on ne le fait plus (déjà qu'on en a trop)
//		// 2007-04
//		if (getter instanceof ChildrenGetter) {
//			// we add the related to the tag cloud
//			list = kw.getFriends();
//			for (int ii = 0; ii < list.size(); ii++) {
//				add2count(BONUS_POUR_RELATED, list.get(ii));
//			}
//		}
  }
  
  /** add howmany to count for kw -- if kw not on this.hs */
  private void add2count(int howmany, SLKeyword kw) {
		if (hs.contains(kw)) return;
		Integer nb = hmKwsOfDocs.get(kw);
		if (nb == null) {
			hmKwsOfDocs.put(kw, new Integer(howmany));
		} else {
			hmKwsOfDocs.put(kw, new Integer(nb.intValue()+howmany));
		}
  }
  
  public void endNode(SLKeyword node) throws Exception {
  	slWalkListener1.endKeyword(node);
  }
  
  public void repeatNode(SLKeyword node) throws Exception {
  	slWalkListener1.repeatKeyword(node);
  }
  public void noNeighborList(SLKeyword kw) throws Exception {
  	List<SLDocument> docs = getter.getDocuments(kw);
		if (docs.size() < 1) {
			slWalkListener1.noList(kw);
		} else {
			slWalkListener1.startList(kw);
			handleDocList(kw,docs);
			slWalkListener1.endList(kw);
		}
  }
  public void endNeighborList(SLKeyword kw) throws Exception {
		slWalkListener1.endKwList(kw);
  	List<SLDocument> docs = getter.getDocuments(kw);
		if (docs.size() < 1) {
			slWalkListener1.noDocList(kw);
		} else {
			handleDocList(kw,docs);
		}
		slWalkListener1.endList(kw);
  }
  public void endWalk(Object seed) throws Exception {
  	slWalkListener1.endSeed((SLKeyword) seed);
  }	
  
  /** docs supposé non vide */
  private void handleDocList(SLKeyword kw, List<SLDocument> docs) throws Exception {
		slWalkListener1.startDocList(kw);
  	
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
			SLDocument doc = docs.get(i);
			List<SLKeyword> kwsOfDoc = doc.getKeywords();
			// documenter l'ensemble de tous les docs
			// Attention, on a déjà pu tomber sur ce doc :
			// il ne faut pas, dans ce cas, augmenter le compteur des linked kws
			if (!(hsDocs.contains(doc))) {
				hsDocs.add(doc);
				// documenter les "LinkedKeywords"
				for (int j = 0; j < kwsOfDoc.size(); j++) {
					// this.hsKwsOfDocs.add(kwsOfDoc.get(j));
					SLKeyword kwo = kwsOfDoc.get(j);
					Integer nb = hmKwsOfDocs.get(kwo);
					if (nb == null) {
						hmKwsOfDocs.put(kwo, UN);
					} else {
						hmKwsOfDocs.put(kwo, new Integer(nb.intValue()+1));
					}
				}
			}
			// même s'il a déjà été affiché pour un autre kw, on réaffiche le doc
			// slWalkListener.printDocument(doc, treePosition, kw, kwsOfDoc);
			slWalkListener1.printDocument(doc, kw, kwsOfDoc);
			// treePosition.pop();
		}

  	slWalkListener1.endDocList(kw);
  }
}


//
// PPTIES
//

/** All docs linked to this tree */
public HashSet<SLDocument> getDocsSet() throws Exception {
	if (this.hsDocs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListener() {
			};
		}
		walk(this.slWalkListener);
	}
	return this.hsDocs;
}

/** All docs linked to this tree
 *  liste parallele au parcours - à ceci près que les éléments ne sont pas dupliqués. */
public SLDocument[] getDocs() throws Exception {
	return getDocsSet().toArray(new SLDocument[0]);
}

/** les kws de l'arbre (pas tous ceux liés aux docs !) 
 *  Si un parcours a déjà, été fait, identique à super.getNodes().
 *  Si pas encore fait, n'a pas le même résultat que (puisque l'appel ds super fait
 *  un super.simpleWalk) Donc si on n'a pas besoin des docs, faire super.getNodes()  */
public HashSet<SLKeyword> getNodes() throws Exception {
	if (this.hs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListener() {
			};
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
			this.slWalkListener = new SLWalkListener() {
			};
		}
		walk(this.slWalkListener);
	}
	SLKeyword[] x = this.hmKwsOfDocs.keySet().toArray(new SLKeyword[0]);
	Arrays.sort(x);
	return x;
}

/** Les descendants de la racine en sont purgés. List de SLKeyword */
public List<SLKeyword> getLinkedKws() throws Exception {
	SLKeyword[] allKws = getKwsOfDocs();
	ArrayList<SLKeyword> x = new ArrayList<>(allKws.length);
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
public HashMap<SLKeyword, Integer> getLinkedKeywords2NbHashMap() throws Exception {
	if (this.hmKwsOfDocs == null) {
		if (this.slWalkListener == null) {
			this.slWalkListener = new SLWalkListener() {
			};
		}
		walk(this.slWalkListener);
	}
	// purge des descendants de la racine
	// on procède différemment selon les taille respectives de hsKws et hmKwsOfDocs
	// puisqu'il s'agit de purger l'un avec l'autre.
	int n_hsKws = this.hs.size() ;
	int n_hmKwsOfDocs = this.hmKwsOfDocs.size();
	if (n_hsKws < 3*n_hsKws) { // pourquoi * 3 ? parce qu'il y a plus d'opérations ds l'autre façon
		Iterator<SLKeyword> it = this.hs.iterator() ;
		for (int i = 0; i < n_hsKws; i++) {
			Object kwo = it.next();
			this.hmKwsOfDocs.remove(kwo);
		}
	} else {
		SLKeyword[] linked = this.hmKwsOfDocs.keySet().toArray(new SLKeyword[n_hmKwsOfDocs]);
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

