package net.semanlink.lod;
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.update.*;

public class SPARQLUpdateEndPoint {
protected Dataset dataset;
/** The key used to store the result model into the execution context. */
public static Symbol RESULTMODEL_KEY = Symbol.create("net.semanlink.lod.ResultModel");

public SPARQLUpdateEndPoint(Model model) {
	dataset = DatasetFactory.create(model);
}
public SPARQLUpdateEndPoint(Dataset dataSource) {
	this.dataset = dataSource ;
}

public void exec(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	String query = req.getParameter("query");
	if (query == null) throw new RuntimeException("No query param in the request");
	// the form should be a post form -> no decode ? // TODO
	// query = java.net.URLDecoder.decode(query,"UTF-8");
	
	// It looks like there is a bug in Jena (2008/07/18):
	// UpdateAction.parseExecute(query,model);
	// with a simple insert s p o
	// gives a npe in BindingUtils.asBinding(QuerySolution qSolution)
	// Here's the workaround:
	
	// 2013-09 update to jena 2.10: this doesn't compile anymore
	// UpdateAction.parseExecute(query,dataset, new QuerySolutionMap());
	UpdateAction.parseExecute(query,dataset); // never tested
}

}
