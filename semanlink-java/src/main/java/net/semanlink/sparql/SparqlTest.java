/* Created on 9 janv. 08 */
package net.semanlink.sparql;
import net.semanlink.servlet.SLServlet;
import net.semanlink.sljena.JModel;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.*;
public class SparqlTest {
public static void main(String[] args) {
	new SparqlTest();
}

/*
SparqlTest() {
		Model model = ((JModel) SLServlet.getSLModel()).getKWsModel();
		String queryString = " .... " ;
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		try {
		  ResultSet results = qexec.execSelect() ;
		  for ( ; results.hasNext() ; ) {
		    QuerySolution soln = results.nextSolution() ;
		    RDFNode x = soln.get("varName") ;       // Get a result variable by name.
		    Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
		    Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
		  }
		} finally { qexec.close() ; }
}
*/
SparqlTest() {
		String service = "http://dbpedia.org/sparql";
		// String queryString = "SELECT ?s ?p ?p2 WHERE {{ ?s ?p <http://en.wikipedia.org/wiki/Marco_Polo> } { ?s ?p2 <http://fr.wikipedia.org/wiki/Marco_Polo> }}" ;
		String queryString = "SELECT ?s ?p WHERE { ?s ?p <http://en.wikipedia.org/wiki/Tampopo> }" ;
		// String queryString = "SELECT ?s ?p WHERE { <http://fr.wikipedia.org/wiki/Marco_Polo> ?p ?s}" ;
		Query query = QueryFactory.create(queryString) ;
		String defaultGraph = "";
		QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query, defaultGraph);
		try {
		  ResultSet results = qexec.execSelect() ;
		  for ( ; results.hasNext() ; ) {
		    QuerySolution soln = results.nextSolution() ;
		    /*
		    RDFNode x = soln.get("varName") ;       // Get a result variable by name.
		    Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
		    Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
		    */
		    RDFNode x = soln.get("s") ;
		    System.out.println(soln.get("p") + " : " + x);
		  }
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally { qexec.close() ; }
}
}
