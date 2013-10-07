/* Created on 3 janv. 2006 */
package net.semanlink.sljena.modelcorrections;

import java.io.IOException;

import net.semanlink.semanlink.SLVocab;
import net.semanlink.sljena.JenaUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A été utilisé pour réparer des conneries faites avec les dates de création
 */
public class CreationDateCorrection extends AbstractCorrection {
/** pour ne garder que la plus ancienne des dates de création d'un KW */
@Override public boolean correctKwsModel(Model mod) throws IOException {
	// System.out.println("CreationDateCorrection");
	Property prop1 = mod.createProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
	Property prop2 = mod.createProperty(SLVocab.SL_CREATION_TIME_PROPERTY);
	ResIterator ite = mod.listSubjectsWithProperty(RDF.type, mod.createProperty(SLVocab.KEYWORD_CLASS));
	boolean x = ite.hasNext();
	for (;ite.hasNext();) {
		Resource res = ite.nextResource();
		// System.out.println(res);
		updateDate(mod, res, prop1);
		updateDate(mod, res, prop2);
	}
	ite.close();
	return x;
}


private void updateDate(Model m, Resource kwRes, Property dateProp) {
	String oldest = null;
	NodeIterator it = m.listObjectsOfProperty(kwRes, dateProp);
	boolean change = false;
	for (;it.hasNext();) {
		String d = it.next().asLiteral().getString();
		if (oldest == null) { 
			oldest = d;
		} else {
			change = true;
			if (d.compareTo(oldest) < 0) oldest = d;
		}
	}
	if (change) {
		StmtIterator sit2 = m.listStatements(kwRes, dateProp,(RDFNode) null);
		m.remove(sit2);
		m.add(kwRes,dateProp,oldest);
	}
}

/* Pour supprimer le creationMonth */
	/*
public static final String SL_CREATION_MONTH_PROPERTY = SLVocab.SEMANLINK_SCHEMA + "slCreationMonth";
public boolean correctDocsModel(Model mod) throws IOException {
	Property prop = mod.getProperty(SL_CREATION_MONTH_PROPERTY);
	ResIterator ite = mod.listSubjectsWithProperty(prop);
	boolean x = ite.hasNext();
	for (;ite.hasNext();) {
		Resource res = ite.nextResource();
		JenaUtils.removeProperty(mod, res.getURI(), SL_CREATION_MONTH_PROPERTY);
	}
	ite.close();
	return x;
}
*/
/*
 * Pour réparer une connerie faite : des dates de création de 2005-12-13 mises à plein de fichiers
 *  (avec en plus une dc:date)
 */
/*
public boolean correctDocsModel(Model mod) throws IOException {
	Property creationDateProp = mod.getProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
	String propertyValue = "2005-12-13";
	String lang = null;
	ResIterator ite = mod.listSubjectsWithProperty(creationDateProp, mod.createLiteral(propertyValue, lang));
	boolean x = ite.hasNext();
	for (;ite.hasNext();) {
		Resource res = ite.nextResource();
		JenaUtils.removeProperty(mod, res.getURI(), creationDateProp.getURI());
		JenaUtils.removeProperty(mod, res.getURI(), SLVocab.DATE_PARUTION_PROPERTY);
	}
	ite.close();
	return x;
}
*/

}
