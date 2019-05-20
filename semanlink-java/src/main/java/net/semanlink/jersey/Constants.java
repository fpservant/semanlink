/* Created on 2 nov. 2012 */
package net.semanlink.jersey;

import javax.ws.rs.core.MediaType;

public interface Constants {
public static final String TAG_PATH = "tag";
public static final String DOC_PATH = "doc/";

// these constants to define the media types that we use
// BEWARE, they are not supposed to identify a media type as such,
// but the media type that we use for a given purpose:
// maybe we're not sure, for instance, what the right media type is for, say, returning turtle.
// Hence the "4" in those names: MEDIA_TYPE_4_TURTLE means: the media type that we use for turtle.

// not OK to be used in annotations:
// public static final MediaType MEDIA_TYPE_4_TURTLE = new MediaType("text","turtle");
// public static final String MEDIA_TYPE_4_TURTLE_STRING = MEDIA_TYPE_4_TURTLE.toString();
// hence, declaration must have this form:
// public static final String MEDIA_TYPE_4_TURTLE_STRING = "text/turtle";
public static final String MEDIA_TYPE_4_TURTLE_STRING = "text/rdf+n3";
public static final MediaType MEDIA_TYPE_4_TURTLE = MediaType.valueOf(MEDIA_TYPE_4_TURTLE_STRING);

public static final String MEDIA_TYPE_4_RDF_XML_STRING = "application/rdf+xml";
public static final MediaType MEDIA_TYPE_4_RDF_XML = MediaType.valueOf(MEDIA_TYPE_4_RDF_XML_STRING);

public static final String MEDIA_TYPE_4_JSON_STRING = "application/json";
public static final MediaType MEDIA_TYPE_4_JSON = MediaType.valueOf(MEDIA_TYPE_4_JSON_STRING);

public static final String MEDIA_TYPE_4_JSONP_STRING = "application/x-javascript";
public static final MediaType MEDIA_TYPE_4_JSONP = MediaType.valueOf(MEDIA_TYPE_4_JSONP_STRING);

public static final String MEDIA_TYPE_4_HTML_STRING = "text/html";
public static final MediaType MEDIA_TYPE_4_HTML = MediaType.valueOf(MEDIA_TYPE_4_HTML_STRING);
}
