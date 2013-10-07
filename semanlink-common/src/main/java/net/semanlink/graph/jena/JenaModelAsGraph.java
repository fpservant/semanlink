package net.semanlink.graph.jena;

import java.util.ArrayList;
import java.util.Iterator;

import net.semanlink.graph.Graph;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Allows for using a Jena Model as a net.semanlink.semanlink.graph.Graph based on a TreeDefinition. */
public class JenaModelAsGraph implements Graph<Resource> {
protected Resource root;
protected TreeDefinition treeDefinition;


public JenaModelAsGraph(Resource root, TreeDefinition treeDefinition) {
	this.root = root;
	this.treeDefinition = treeDefinition;
}

/*
public Model toModel() throws Exception {
	Model x = ModelFactory.createDefaultModel();
	//GraphToModelCopy.copy(this, x);
	(new GraphToModelCopy(this,x, RDCSchema.SON_PROP.getURI())).doCopy();
	return x;
}
*/

public Iterator<Resource> getNeighbors(Resource res) throws Exception {
	ArrayList<Resource> x = new ArrayList<Resource>();
	Model mod = root.getModel();
	for (Property childProp : this.treeDefinition.getChildProps()) {
		NodeIterator it = mod.listObjectsOfProperty(res, childProp);
		for (;it.hasNext();) {
			Object o = it.next();
			if (o instanceof Resource) x.add((Resource) o);
		}
	}
	for (Property parentProp : this.treeDefinition.getParentProps()) {
		ResIterator it = mod.listResourcesWithProperty(parentProp,res);
		for (;it.hasNext();) {
			x.add(it.nextResource());
		}
	}
	return x.iterator();
}

public Resource[] seeds() {
	Resource[] x = new Resource[1];
	x[0] = root;
	return x;
}

}
