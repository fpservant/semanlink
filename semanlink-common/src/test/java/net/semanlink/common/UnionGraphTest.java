/* Created on 5 juil. 2016 */
package net.semanlink.common;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;

public class UnionGraphTest {

// @Test 
public final void test() {
	Model m = ModelFactory.createDefaultModel();
	m.add(
			m.createResource("http://ex.com/s"),
			m.createProperty("http://ex.com/p"),
			m.createResource("http://ex.com/o"));
	Dataset ds = TDBFactory.createDataset("/Users/fps/Desktop/tdbtest");
	ds.addNamedModel("http://ex.com/g", m);

	Model m0 = ds.getDefaultModel();
	m0.add(
			m0.createResource("http://ex.com/s"),
			m0.createProperty("http://ex.com/p"),
			m0.createResource("http://ex.com/o0"));
	
	// query using the named graph: return some results
	// doIt(ds, "http://ex.com/g");
	// same query, using urn:x-arq:UnionGraph, doens't return any result
	doIt(ds, "urn:x-arq:UnionGraph");
	ds.close();
}

private void doIt(Dataset ds, String graphName) {
	// String queryString = "SELECT * WHERE {{ GRAPH <" + graphName + "> {?s ?p ?o} } UNION {?s ?p ?o}}";
	
	String queryString = "CONSTRUCT {?s ?p ?o} WHERE {{?s ?p ?o.}UNION{GRAPH <urn:x-arq:UnionGraph> {?s ?p ?o.}}}";
			
			
// String queryString = "CONSTRUCT {<http://other.com/s> ?p ?o} WHERE {{ GRAPH <" + graphName + "> {?s ?p ?o} } UNION {?s ?p ?o}}";
	Query q = QueryFactory.create(queryString) ;
	QueryExecution qexec = QueryExecutionFactory.create(q, ds);
	Model r = ModelFactory.createDefaultModel();
	qexec.execConstruct(r);
	r.write(System.out,"TURTLE");
}

}
