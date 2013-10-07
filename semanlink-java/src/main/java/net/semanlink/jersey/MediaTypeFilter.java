/* Created on 2 nov. 2012 */
package net.semanlink.jersey;
import com.sun.jersey.api.container.filter.UriConnegFilter;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * transforms "path.extension" to "path"
 * (must be declared in web.xml)
 */
public class MediaTypeFilter extends UriConnegFilter {
private static final Map<String, MediaType> mappedMediaTypes = new HashMap<String, MediaType>(2);

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

//     public final static MediaType APPLICATION_XML_TYPE = new MediaType("application","xml");
