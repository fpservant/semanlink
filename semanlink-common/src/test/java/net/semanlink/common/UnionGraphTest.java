/* Created on 5 juil. 2016 */
package net.semanlink.common;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;

public class UnionGraphTest {

@Test public final void test() {
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

@Test public final void test2() {
	Dataset ds = TDBFactory.createDataset("/Users/fps/z-no-svg/Rasse-Data/bin/tdb");
	System.out.println("*************");
	Iterator it = ds.listNames();
	for (;it.hasNext();) {
		System.out.println(it.next());
	}
	System.out.println("*************");
	

	doIt2(ds, "urn:x-arq:UnionGraph");
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

private void doIt2(Dataset ds, String graphName) {
	// String queryString = "SELECT * WHERE {{ GRAPH <" + graphName + "> {?s ?p ?o} } UNION {?s ?p ?o}}";
	
	String queryString = "CONSTRUCT {<http://127.0.0.1:9080/rasse/resources/pr/1452/711010> ?p ?o} WHERE {{<http://www.renault.com/euro5/referentiel/pr/1452/711010> ?p ?o.}UNION{GRAPH <urn:x-arq:UnionGraph> {<http://www.renault.com/euro5/referentiel/pr/1452/711010> ?p ?o.}}}";
			
			
// String queryString = "CONSTRUCT {<http://other.com/s> ?p ?o} WHERE {{ GRAPH <" + graphName + "> {?s ?p ?o} } UNION {?s ?p ?o}}";
	Query q = QueryFactory.create(queryString) ;
	QueryExecution qexec = QueryExecutionFactory.create(q, ds);
	Model r = ModelFactory.createDefaultModel();
	qexec.execConstruct(r);
	System.out.println(queryString);
	r.write(System.out,"TURTLE");
	
}

}
