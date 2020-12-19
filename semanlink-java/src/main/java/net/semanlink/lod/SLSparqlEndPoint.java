package net.semanlink.lod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathLib;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.ThesaurusLabels;
import net.semanlink.servlet.SLServlet;
import net.semanlink.skos.SKOS;
import net.semanlink.sljena.JKeyword;
import net.semanlink.sljena.JModel;
import net.semanlink.sparql.TagDescribeHandlerFactory;
import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.WordIndexInterface;
import net.semanlink.util.index.jena.TextMatchMagicProp;

public class SLSparqlEndPoint extends SPARQLEndPoint {
public SLSparqlEndPoint(JModel slModel) {
	super(createDataSource(slModel.getKWsModel(), slModel.getDocsModel()));
	sparqlInits(slModel);
}

private static Dataset createDataSource(Model kwsModel, Model docsModel) {
	Dataset dataSource = DatasetFactory.create(kwsModel) ;
	dataSource.addNamedModel(SLServlet.getServletUrl() + "/tags/", kwsModel);
	dataSource.addNamedModel(SLServlet.getServletUrl() + "/docs/", docsModel);
	return dataSource;
}

private static void sparqlInits(JModel slModel) {
	//
	// MAGIC PROP DESCENDANT AND ANCESTOR (attention, doit ici inclure la tag lui même)
	//

	PrefixMapping mapping = slModel.getKWsModel();
	String slNs = "http://www.semanlink.net/2001/00/semanlink-schema#"; // TODO
	mapping.setNsPrefix("sl", slNs);
	// @find SKOSIFY
	String skosNs = SKOS.NS;
	mapping.setNsPrefix("skos", skosNs);
	
	Path path; String uri;

//  // @find SKOSIFY
//	// usual semanlik use
//	// path = PathParser.parse("^sl:hasParent*", mapping) ; // @find SKOSIFY
//	path = PathParser.parse("^skos:broader*", mapping) ; // @find SKOSIFY
//	uri = slNs + "hasDescendant" ; // including given tag
//	PathLib.install(uri, path) ;
	
//  // @find SKOSIFY
//	// path =  PathParser.parse("sl:hasParent*", mapping) ; // @find SKOSIFY
//	path =  PathParser.parse("skos:broader*", mapping) ; // @find SKOSIFY
//	uri = slNs + "hasAncestor"; // including given tag
//	PathLib.install(uri, path) ;
	
//	// @find SKOSIFY
//	// to allow to search using hasChild
//	// path =  PathParser.parse("^sl:hasParent", mapping) ; // @find SKOSIFY
//	path =  PathParser.parse("^skos:broader", mapping) ; // @find SKOSIFY
//	uri = slNs + "hasChild";
//	PathLib.install(uri, path) ;

	// to use SKOS
	// @find SKOSIFY
//	// path =  PathParser.parse("sl:hasParent", mapping) ; // not OK, at least with jena 2.6.4
//	path =  PathParser.parse("sl:hasParent{1}", mapping) ;
//	uri = SKOS.broader.getURI();
//	PathLib.install(uri, path) ;
	
	// path =  PathParser.parse("^sl:hasParent", mapping) ; // @find SKOSIFY
	path =  PathParser.parse("^skos:broader", mapping) ; // @find SKOSIFY
	uri = SKOS.narrower.getURI();
	PathLib.install(uri, path) ;
	
	// path =  PathParser.parse("sl:hasParent+", mapping) ; // not including given tag: don't do that, because searching docs becomes ugly
	// @find SKOSIFY
	// path =  PathParser.parse("sl:hasParent*", mapping) ; // including given tag
	path =  PathParser.parse("skos:broader*", mapping) ; // including given tag
	uri = SKOS.NS + "broaderTransitive"; 
	PathLib.install(uri, path) ;

	// @find SKOSIFY
	// path =  PathParser.parse("^sl:hasParent+", mapping) ; // not including given tag: don't do that, because searching docs becomes ugly
	path =  PathParser.parse("^skos:broader+", mapping) ; // not including given tag: don't do that, because searching docs becomes ugly
	uri = SKOS.NS + "narrowerTransitive"; // including given tag
	PathLib.install(uri, path) ;

	// @find SKOSIFY
//	path =  PathParser.parse("sl:related{1}", mapping) ;
//	uri = SKOS.related.getURI();
//	PathLib.install(uri, path) ;
	
	path =  PathParser.parse("sl:Tag", mapping) ;
	uri = SKOS.Concept.getURI();
	PathLib.install(uri, path) ;
	

	//
	// 
	//
	
	uri = slNs + "tagText";
	PropertyFunctionRegistry.get().put(uri, TextMatchMagicProp.class);
	TextMatchMagicProp.setIndex(new AdaptedIndex(slModel.getThesaurusLabels()));
	
	//
	// to return the sons in the description of a Tag
	//
	
  DescribeHandlerRegistry.get().add(new TagDescribeHandlerFactory());
}

// that's a hack
private static class AdaptedIndex implements WordIndexInterface<Resource> {
	ThesaurusLabels th;
	AdaptedIndex(ThesaurusLabels thIndex) {
		this.th = thIndex;
	}
	public Collection<Resource> string2entities(String searchString) {
		// 2020-03
		// Set<SLKeyword> set = th.searchText(searchString);
		Set<ObjectLabelPair<SLKeyword>> set = th.string2entities(searchString);
		ArrayList<Resource> x = new ArrayList<Resource>(set.size());
		// Model model = ((JModel) SLServlet.getSLModel()).getKWsModel();
		for (ObjectLabelPair<SLKeyword> pair : set) {
			// x.add(model.createResource(kw.getURI()));
			x.add(((JKeyword) pair.getObject()).getRes());
		}
		return x;
	}
}
}
