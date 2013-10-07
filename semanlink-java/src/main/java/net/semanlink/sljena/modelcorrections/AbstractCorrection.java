/* Created on 3 nov. 03 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;
import net.semanlink.sljena.JModel;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Correction qui ne fait rien. L'etendre et overrider correctDocsModel et/ou correctKwsModel
 */
public abstract class AbstractCorrection implements Correction {
protected long time = System.currentTimeMillis();

/** Cette definition fait que la correction est systematiquement appliquee
 *  (aux cas tordus pres de fichier qui aurait ete modifie apres maintenant - cf
 *  fausses dates de modif, ou mauvaise heure systeme.
 */
public long getTime() {
	return this.time;
}

/* (non-Javadoc)
 * @see net.semanlink.sljena.modelcorrections.Correction#correct(net.semanlink.semanlink.SLModel)
 */
public boolean correct(JModel mod) throws IOException {
	boolean x;
	x = correctDocsModel(mod.getDocsModel());
	x = correctKwsModel(mod.getKWsModel()) || x;
	return x;
}

/* (non-Javadoc)
 * @see net.semanlink.sljena.modelcorrections.Correction#correctDocsModel(com.hp.hpl.jena.rdf.model.Model)
 */
public boolean correctDocsModel(Model mod) throws IOException {
	return false;
}

public boolean correctKwsModel(Model mod) throws IOException {
	return false;
}

}
