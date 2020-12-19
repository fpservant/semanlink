package net.semanlink.lod;
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.semanlink.util.jena.RDFServletWriterUtil;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * SPARQL endpoint. 
 * 
 * This is just some util methods around a "org.apache.jena.query.DataSource".
 * 
 * Includes methods <ul>
 * <li>to execute a query included in an HttpServletRequest, </li>
 * <li>to write the result to an HttpServletResponse.</li>
 * </ul>
 * 
 * Also provides a way to modify the result model of a describe or construct query
 * from within a "magic property" (PropertyFunction) @see @getResultModel(Context)
 */
public class SPARQLEndPoint {
protected Dataset dataset;
/** The key used to store the result model into the execution context. */
public final static Symbol RESULTMODEL_KEY = Symbol.create("net.semanlink.lod.ResultModel");

public SPARQLEndPoint(Model model) {
	this(DatasetFactory.create(model));
}
public SPARQLEndPoint(Dataset dataset) {
	this.dataset = dataset ;
	// prefixes();
}

public Dataset getDataset() { return this.dataset ; }

/** Return the model used to store the results of a describe or construct query. 
 *  Can be used by a PropertyFunction to add (or remove) statements during the computation of the result model
 *  @see VINPropertyFunction for an example of use. */
public static Model getResultModel(Context executionContext) {
	return (Model) executionContext.get(RESULTMODEL_KEY);
}

// was used in VINPropertyFunction, but no more useful, because we always add the resultModel to the execution context here
public static void setResultModel(Context executionContext, Model resultModel) {
	executionContext.set(RESULTMODEL_KEY, resultModel);
}

//
// EXEC FROM HttpServletRequest.
// FIRST, EXTRACT THE SPARQL STRING FROM THE HttpServletRequest
//

// Hmm: there should be different handling of the encoding,
// depending on request (GEt and POST)
/** Extract the query string included in the HttpServletRequest. 
 *  <a href="http://www.semanlink.net/doc/?uri=http%3A%2F%2Fwiki.apache.org%2Ftomcat%2FFAQ%2FCharacterEncoding">
 *  pb with the character encoding</a>
 */
public String getQueryString(HttpServletRequest req) {
	String queryString = req.getParameter("query");
	
	
	if (queryString == null) throw new RuntimeException("No query param in the request");
	
	// @find Tomcat "feature" wrt uri encoding
	// THIS SHOULD BE DONE ONLY ON TOMCAT?
	// @TODO mettre un attribut à this: tomcat or not tomat
	try {
		queryString = java.net.URLEncoder.encode(queryString, "ISO-8859-1");
		queryString = java.net.URLDecoder.decode(queryString, "UTF-8");
	} catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
	
	// @TODO HMM: verify that: decode again? related to "double encoding on client side"?
	// we suppose here that the form is a get form, with encoded query argument
	// return java.net.URLDecoder.decode(queryString,"UTF-8");	
	
	// System.out.println("SPARQLENdPoint " + queryString);
	return queryString;
}

/** Execute the query corresponding to a given HttpServletRequest, and write result to HttpServletResponse. */
public void exec(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	exec(createQuery(req), res);
}

/** Execute the Query, and write result to HttpServletResponse */
public void exec(Query query, HttpServletResponse res) throws IOException, ServletException {
	QueryExecution qexec = QueryExecutionFactory.create(query, this.dataset);
  
	try {
		if (query.isAskType()) {
			boolean b = qexec.execAsk();
			String s = Boolean.toString(b);
			res.getOutputStream().write(s.getBytes());
		} else if (query.isSelectType()) {
		  ResultSet results = qexec.execSelect() ;
		  /*for ( ; results.hasNext() ; ) {
		    QuerySolution soln = results.nextSolution() ;
		    OutputStream out = res.getOutputStream();
		    out.write(soln.toString().getBytes());
			  out.write("\r\n".getBytes());
		  }*/
		  ResultSetFormatter.outputAsXML(res.getOutputStream(), results);
		  // ResultSetFormatter.outputAsJSON(res.getOutputStream(), results);
		} else { // either query.isDescribeType() or query.isConstructType()
			Model resultModel = getResultModel(query, qexec);
			
			RDFServletWriterUtil.writeRDF(resultModel, res);
		}
	} finally { qexec.close() ; }
}

/** Create the Query corresponding to a given HttpServletRequest. 
 *  Doesn't execute the query: use getResultModel to execute it
 *  @see getResultModel(Query) */
public Query createQuery(HttpServletRequest req) {
	return createQuery(getQueryString(req));
}

/** Create the Query corresponding to a given String. 
 *  Doesn't execute the query: use getResultModel to execute it
 *  @see getResultModel(Query) */
public Query createQuery(String queryString) {
	System.out.println("EndPoint " + queryString);
	Query x = QueryFactory.create(queryString) ;
	// Query x = QueryFactory.create(queryString, Syntax.syntaxARQ) ; // property path test PBS with UNION (see message to Andy Seaborne)
	// x.setLimit(50);
	return x;
}

/** Execute the query and return the resultModel.
 *  Assumes that either query.isDescribeType() or query.isConstructType() */
public Model getResultModel(Query query) {
	QueryExecution qexec = QueryExecutionFactory.create(query, this.dataset);
	return getResultModel(query, qexec); 
}

/** Execute the query and return the resultModel.
 *  Assumes that either query.isDescribeType() or query.isConstructType() */
public Model getResultModel(Query query, QueryExecution qexec) { 
	/*// @see using a property function to add statements to the result model
	// we create a model and pass it to execDescribe.
	// This allows to add statements to it in a function property
	// (to achieve that, we add this model to the context)
	Model resultModel = ModelFactory.createDefaultModel();
	qexec.getContext().set(RESULTMODEL_KEY, resultModel);
	if (query.isDescribeType()) {
		resultModel = qexec.execDescribe(resultModel) ;
	} else { // (query.isConstructType())
		resultModel = qexec.execConstruct(resultModel) ;
	}
		qexec.close();
	return resultModel;
	 */
	return getResultModel(query, qexec, true);
}

public Model getResultModel(Query query, QueryExecution qexec, boolean close) { 
	// @see using a property function to add statements to the result model
	// we create a model and pass it to execDescribe.
	// This allows to add statements to it in a function property
	// (to achieve that, we add this model to the context)
	// System.out.println(query);
	Model resultModel = ModelFactory.createDefaultModel();
	qexec.getContext().set(RESULTMODEL_KEY, resultModel);
	if (query.isDescribeType()) {
		resultModel = qexec.execDescribe(resultModel) ;
	} else { // (query.isConstructType())
		resultModel = qexec.execConstruct(resultModel) ;
	}
	if (close) qexec.close();
	return resultModel;
}


//
// PREFIXES
//

//public void prefixes() {
//	// System.out.println("Prefixes defined in datasource 's defaultmodel");
//	Map prefixMap = this.dataset.getDefaultModel().getNsPrefixMap();
//	Set keySet = prefixMap.keySet();
//	Iterator keys = keySet.iterator();
//	for (;keys.hasNext();) {
//		String prefix = (String) keys.next();
//		String ns = (String) prefixMap.get(prefix);
//		// System.out.println("PREFIX " + prefix + " : " + ns);
//	}
//}

/** Get the URI bound to a specific prefix, null if there isn't one. */
public String getNsPrefixURI(String prefix) {
	return this.dataset.getDefaultModel().getNsPrefixURI(prefix);
}

    
}
