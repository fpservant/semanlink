/* Created on 26 août 2012 */
package net.semanlink.lod;

import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.rdf.model.Model;

public interface JsonLDSerializer {
public void rdf2jsonld(Model model, OutputStream out) throws IOException;
}
