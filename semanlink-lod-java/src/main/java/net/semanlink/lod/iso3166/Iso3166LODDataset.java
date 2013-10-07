/* Created on 7 oct. 08 */
package net.semanlink.lod.iso3166;

import com.hp.hpl.jena.rdf.model.Model;
import net.semanlink.lod.SimpleLODDataset;

public class Iso3166LODDataset extends SimpleLODDataset {
public Iso3166LODDataset(Model model, String base) {
		super(model, base);
}

public boolean owns(String uri) {
	return (uri.indexOf("iso3166") > -1);
}


}
