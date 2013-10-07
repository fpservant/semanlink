package net.semanlink.lod;
import net.semanlink.servlet.SLServlet;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

public class SLSPARQLEndPoint extends SPARQLEndPoint {
public SLSPARQLEndPoint(Model kwsModel, Model docsModel) {
	super(createDataSource(kwsModel, docsModel));
}

private static Dataset createDataSource(Model kwsModel, Model docsModel) {
	// Dataset dataSource = DatasetFactory.create() ;
	// dataSource.setDefaultModel(kwsModel) ;
	Dataset dataSource = DatasetFactory.create(kwsModel) ;
	dataSource.addNamedModel(SLServlet.getServletUrl() + "/tags/", kwsModel);
	dataSource.addNamedModel(SLServlet.getServletUrl() + "/docs/", docsModel);
	return dataSource;
}
}
