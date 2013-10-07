/* Created on 22 oct. 03 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;

import net.semanlink.sljena.JModel;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Interface a implementer pour definir une correction a apporter a un model.
 * @see ModelCorrector
 * @author fps
 */
interface Correction {
/** Retourne le temps (depuis "l'epoque") de "mise en oeuvre" de cette correction.
 *  Permet de ne tenter une correction que sur les fichiers qui
 *  n'ont pas ete modifie depuis (s'il est permis de supposer que tout fichier
 *  modifie depuis a effectivement ete corrige).
 */
public long getTime();
/** Corrige le JModel mod
 *  Convenience method qu'il suffit de definir à l'aide des 2 autres methodes de cette interface.
 *  Retourne true ssi il y a eu un changement.
 */
public boolean correct(JModel mod) throws IOException;
/** Corrige le docsModel mod.
 *  Retourne true ssi il y a eu un changement.
 */
public boolean correctDocsModel(Model mod) throws IOException;
/** Corrige le kwsModel mod.
 *  Retourne true ssi il y a eu un changement.
 */
public boolean correctKwsModel(Model mod) throws IOException;
}

