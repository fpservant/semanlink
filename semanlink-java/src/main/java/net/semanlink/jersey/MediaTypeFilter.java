/* Created on 2 nov. 2012 */
package net.semanlink.jersey;
import org.glassfish.jersey.server.filter.UriConnegFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;

/**
 * transforms "path.extension" to "path"
 * (must be declared in web.xml) // hum, 2018-08 no more: registered in SLJerseyApp
 */

@PreMatching // without this annotation: java.lang.IllegalStateException: Method could be called only in pre-matching request filter.
public class MediaTypeFilter implements ContainerRequestFilter {
    private UriConnegFilter filter;
    private static final Map<String, MediaType> mappedMediaTypes = new HashMap<String, MediaType>(4);

    static {
      mappedMediaTypes.put("html", MediaType.TEXT_HTML_TYPE);
      mappedMediaTypes.put("rdf", Constants.MEDIA_TYPE_4_RDF_XML);
      mappedMediaTypes.put("n3", Constants.MEDIA_TYPE_4_TURTLE);
      mappedMediaTypes.put("ttl", Constants.MEDIA_TYPE_4_TURTLE);
    }

    public MediaTypeFilter() {
        filter = new UriConnegFilter(mappedMediaTypes, null);
    }

    @Override public void filter(ContainerRequestContext requestContext) throws IOException {
        filter.filter(requestContext);
    }
}

/*
// BROKEN WITH 2.27 SEE
// https://stackoverflow.com/questions/36517620/jersey-uriconnegfilter-now-declared-final-breaks-old-code-how-to-fix-it


public class MediaTypeFilter extends UriConnegFilter {
private static final Map<String, MediaType> mappedMediaTypes = new HashMap<String, MediaType>(4);

static {
  mappedMediaTypes.put("html", MediaType.TEXT_HTML_TYPE);
  mappedMediaTypes.put("rdf", Constants.MEDIA_TYPE_4_RDF_XML);
  mappedMediaTypes.put("n3", Constants.MEDIA_TYPE_4_TURTLE);
  mappedMediaTypes.put("ttl", Constants.MEDIA_TYPE_4_TURTLE);
}

public MediaTypeFilter() {
  super(mappedMediaTypes);
}
}
*/

//     public final static MediaType APPLICATION_XML_TYPE = new MediaType("application","xml");
