package net.semanlink.sljena.modelcorrections;

import net.semanlink.semanlink.SLVocab;
import net.semanlink.skos.SKOS;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;


/**
 * To transform a SLModel using old SLVocab ("SLKeyword", "parent", ...)
 * to be using skos vocab.
 * To use, creates an instance, then getModelCorrector, and use this corrector
 */

// A VOIR :
// les xml/lang
// label

//public URICorrection(String oldUri, String newUri, boolean inKwsModel, boolean inDocsModel) {


/// DATE DE 2007 JAMAIS UTILISE !!!
/** @deprecated and not used */
public class OldSLVocab2SkosTransformer {
private static String SL = SLVocab.SEMANLINK_SCHEMA;
private ModelCorrector corrector;
public OldSLVocab2SkosTransformer(ModelCorrector corrector) {
	this.corrector = corrector;
	// kwsmodel
	corrector.add(new URICorrection(SL + "Keyword", SKOS.Concept.getURI(), true, false));
	corrector.add(new URICorrection(SL + "hasParent", SKOS.broader.getURI(), true, false));
	// docsmodel
	corrector.add(new URICorrection(SL + "hasKeyword", SKOS.subject.getURI(), false, true));
	corrector.add(new URICorrection(RDFS.label.getURI(), DC.title.getURI(), false, true));
	// both
	corrector.add(new URICorrection(SL + "slCreationDate", SL + "creationDate", true, true));
	corrector.add(new URICorrection(SL + "comment", RDFS.comment.getURI(), true, true));
}

public ModelCorrector getModelCorrector() { return this.corrector; }
}

