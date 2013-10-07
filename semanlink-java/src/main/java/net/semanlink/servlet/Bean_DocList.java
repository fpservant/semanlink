/* Created on 30 nov. 03 */
package net.semanlink.servlet;
import net.semanlink.semanlink.*;
import net.semanlink.util.*;
import java.util.*;

// Ca aurait été plus malin de contenir un SLDocument[] qu'une List
/**
 * A list of documents to be displayed.
 */
public class Bean_DocList extends Bean_ResList {

//
// ATTRIBUTS
//

/** liste de kws à ne pas afficher avec le doc.
 *  (par ex, quand on affiche la liste des docs d'un kw, on ne veut pas afficher
 *  ce kw ds la liste des autres kws du doc.
 */
private SLKeyword[] dontShowTheseKws;

//
//
//

public SLDocument getDoc(int index) { return (SLDocument) this.getList().get(index);}

//
// IMAGES
//

public List getImages() {
	List list = this.getList();
	int n = list.size();
	List x = new ArrayList(n);
	for (int i = 0; i < n; i++) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			x.add(doc);
		}
	}
	return x;
}

/**
 * Retourne l'index, dans la liste des docs, de l'image suivant celle passée en argument ou -1
 */
int getNextImageIndex(SLDocument currentImage) {
	/* on reparcourt toute la liste des documents pour trouver currentImage.
	 * Dans le cas ou on ne fait que des "gonext",
	 * il faudrait optimiser ce qu'on fait et passer la position de l'image en cours
	 * a la jsp qu'elle nous la retransmette. 
	 * Ou alors faire une map uri -> image : non, pas ca*/
	return getNextImageIndex(getDocIndex(currentImage));
}
/**
 * Retourne l'index, dans la liste des docs, de l'image précédent celle passée en argument ou -1
 */
int getPrevImageIndex(SLDocument currentImage) {
	return getPrevImageIndex(getDocIndex(currentImage));
}
/**
 * Retourne l'index dans la liste des docs de l'image suivant celle d'index passe en argument, ou -1
 * @param index index dans la liste des docs (pas ds celle des images!)
 */
int getNextImageIndex(int index) {
	List list = this.getList();
	int n = list.size();
	index++;
	for (int i = index; i < n; i++) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			return i;
		}
	}
	for (int i = 0; i < index; i++) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			return i;
		}
	}
	return -1;
}
/**
 * Retourne l'index dans la liste des docs de l'image précédant celle d'index passée en argument, ou -1
 * @param index index dans la liste des docs (pas ds celle des images!)
 */
int getPrevImageIndex(int index) {
	List list = this.getList();
	int n = list.size();
	index--;
	for (int i = index; i > -1; i--) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			return i;
		}
	}
	for (int i = n-1; i > index; i--) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			return i;
		}
	}
	return -1;
}
/** retourne l'index dans la liste des docs de la 1ere image. */
int getFirstImageIndex() {
	List list = this.getList();
	for (int i = 0; i < list.size(); i++) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			return i;
		}
	}
	return -1;
}

private int getDocIndex(SLDocument doc) {
	String uri = doc.getURI();
	List list = this.getList();
	for (int i = 0; i < list.size(); i++) {
		SLDocument d = (SLDocument) list.get(i);
		String ur = d.getURI();
		if (uri.equals(ur)) return i;
	}
	return -1;
}


/** attention, recalcule la liste presque complete. OPTIM to do ?
 *  suppose lastindice > firstindice
 */
public List getImages(int firstIndice, int lastindice) {
	List list = this.getList();
	int n = list.size();
	List x = new ArrayList(lastindice - firstIndice);
	int iIm = 0;
	for (int i = 0; i < n; i++) {
		SLDocument doc = (SLDocument) list.get(i);
		String uri = doc.getURI();
		if (Util.isImage(uri)) {
			if (iIm >= firstIndice) {
				// suppose lastindice > firstindice
				x.add(doc);
				if (iIm >= lastindice) {
					break;
				}
			}
			iIm++;
		}
	}
	return x;
}

//
// AFFICHAGE OU PAS DES KWS ASSOCIES AUX DOCS
//

/** Pour dire qu'il faut afficher les kws du doc. */
public void setShowKwsOfDocs(boolean showKwsOfDocs, SLKeyword[] dontShowTheseKws) {
	// this.showKwsOfDocs = showKwsOfDocs;
	this.dontShowTheseKws = dontShowTheseKws;
}

/** Attention, ne pas modifier la liste retournée */
public List getKeywordsToShow(int index) {
	SLDocument doc = getDoc(index);
	List kws = doc.getKeywords();
	if (this.dontShowTheseKws == null) return kws;
	int n = kws.size();
	List x = new ArrayList(n);
	for (int i = 0; i < n; i++) {
		Object kw = kws.get(i); 
		if (!dontShow(kw)) x.add(kw);
	}
	return x;
}

private boolean dontShow(Object kw) {
	for (int j = 0; j < this.dontShowTheseKws.length; j++) {
		if (this.dontShowTheseKws[j].equals(kw)) {
			return true;
		}
	}
	return false;
}

}
