/* Created on 9 nov. 07 */
package net.semanlink.lod;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/** 
 * The bridge between a request for a uri, and the response the representation (we get
 * when dereferencing the uri) is written to.
 * Nothing more than a SimpleHttpClient. Should be replaced by SimpleHttpClient? Probably not: 
 * we will handle here the handling of http return code to avoid unnecessary roundtrips: in case there's a request for rdf
 * and we get a 303, we'll send the request for the redirected uri*/
public class URIDereferencer {
//private SimpleHttpClient httpClient;
//public URIDereferencer(SimpleHttpClient httpClient) {
//	this.httpClient = httpClient;
//}
//
///** Writes directly to res what is returned when dereferencing the URI, without any modification. 
// * @throws IOException 
// * @throws HttpException */
//public void output(String uri, HttpServletResponse res) throws HttpException, IOException {
//	// should'nt we set the acceptHeader?
//	// Here,we're confident (because we're being called by the LODServlet), that we want rdf,
//	// and that that's we'll get
//	// TODO : this is not good for a general purpose class - either hide this class
//	// (visible only to the LODServlet), or handle the acceptHeader with care
//	String acceptHeader = null;
//	this.httpClient.output(uri, acceptHeader, res);
//}
//
//public SimpleHttpClient getSimpleHttpClient() {
//	return this.httpClient;
//}
}
