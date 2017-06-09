/* Created on 3 janv. 2011 */
package net.semanlink.sparql;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.skos.SKOS;
import net.semanlink.util.jena.JenaUtils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.describe.*;
import org.apache.jena.sparql.util.Context;

/**
 * Used to modify the content returned by DESCRIBE queries for tags
 * must be added to the DescribeHandlerRegistry.
 * @author fps
 */
public class TagDescribeHandler implements DescribeHandler {
Model acc ;
Context qContext;

public void start(Model accumulateResultModel, Context qContext) {
  acc = accumulateResultModel ;
  this.qContext = qContext;
}

public void describe(Resource resource) {
	// System.out.println("describing " + resource);
	if (JenaUtils.hasRDFType(resource, SLSchema.Tag)) {
		Model mod = resource.getModel();
		/*// to have labels for parents: not OK for the display: subjects are no more alwways "results" of the query)
		NodeIterator parents = mod.listObjectsOfProperty(resource, mod.createProperty(SLVocab.HAS_PARENT_PROPERTY));
		for(;parents.hasNext();) {
			StmtIterator sit = mod.listStatements((Resource) parents.next(), RDFS.label, (RDFNode) null);
			acc.add(sit);			
		}*/

		// to include "hasChild" statements
		// si on fait ça, pose pb ds les display de sparql results: on autre chose que des sujets ds les res
		// StmtIterator it = resource.getModel().listStatements((Resource) null, SLSchema.hasParent, resource);
		// acc.add(it);
		// ResIterator it = mod.listSubjectsWithProperty(SLSchema.hasParent, resource); // @find SKOSIFY
		ResIterator it = mod.listSubjectsWithProperty(mod.createProperty(SLVocab.HAS_PARENT_PROPERTY), resource);
		// Property sonProp = mod.createProperty(SLSchema.NS + "hasChild"); // @find SKOSIFY
		Property sonProp = SKOS.narrower;
		Resource resInRes = resource.inModel(acc);
		for(;it.hasNext();) {
			acc.add(resInRes, sonProp, it.nextResource());
		}
		
	}
}

public void finish() {
}


}
