/* Created on 19 févr. 2009 */
package net.semanlink.lod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Jsp_NIRPage extends Jsp_RDF2HTMLPage {
private LODDataset lodDataset;
public Jsp_NIRPage(HttpServletRequest request, HttpServletResponse response, LODDataset lodDataset, String nirUri) {
	super(request, response, "centercontent", lodDataset.nir2rdfURI(nirUri), nirUri, false);
	this.lodDataset = lodDataset;
}

}
