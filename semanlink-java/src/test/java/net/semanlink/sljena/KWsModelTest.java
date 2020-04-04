/* Created on Apr 4, 2020 */
package net.semanlink.sljena;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.junit.Test;

public class KWsModelTest {
	
public Model loadKwsModel() throws JenaException, IOException {
	Model kwsModel = ModelFactory.createDefaultModel();
	String longFilename = "/Users/fps/Semanlink/semanlink-fps/tags/slkws.rdf";
	String base = "http://www.semanlink.net/tag/";
	ModelFileIOManager.readModel(kwsModel, longFilename, base);
	return kwsModel;
}

}
