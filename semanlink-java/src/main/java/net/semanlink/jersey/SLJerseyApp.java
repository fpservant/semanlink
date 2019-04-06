/* Created on 4 août 2018 */
package net.semanlink.jersey;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;


// import io.swagger.jaxrs.config.BeanConfig;

public class SLJerseyApp extends ResourceConfig {
	private Date applicationStartTime;

	public SLJerseyApp(@Context ServletContext ctx) throws IOException { // , SlInitException {
		super(getResourceAndProviderClasses());

//		aldaServer = getAldaServer(); 
		applicationStartTime = new Date();
		
		// in order to be able to inject
		this.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(applicationStartTime).to(Date.class);
			}
		});

		/*// pour cette merde de swagger
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("0.0.1 (preview)");
		beanConfig.setTitle("ALDA API");
		beanConfig.setDescription("**ALDA** REST API\n\nNote to users: "
				+ "\n- Etags are returned along with data, so you may used them in caching resources, in order to save bandwidth and server resources."
				+ "\n- new fields may be added to the returned data without notice. Be warned.");
		beanConfig.setResourcePackage("com.renault.sicg.alda.web.resources");
		// ATTENTION, jusqu'à 3.0.6 au moins, si on met ca à false,
		// le basePath "/alda" ne semble pas pris en compte par swagger:
		// le "try it" tente de se connecter, par ex, à 127.0.0.1:7070/lexicons
		// au lieu de 127.0.0.1:7070/alda/lexicons
		beanConfig.setScan(true);
		*/

		if (ctx!=null) {
			Logger.getGlobal().log(Level.INFO, "SlJerseyApp will be installed at path "+ctx.getContextPath());
			// beanConfig.setBasePath(ctx.getContextPath());
		} else {	
			// beanConfig.setBasePath("/alda"); 
		}
		
		// beanConfig.setBasePath("alda");

		// Allow cross origin resource sharing
		register(CORSResponseFilter.class);
		
		// for a "file extension based content negotiation"
		register(MediaTypeFilter.class);

		// Delete IF-nono-match header if no-cache directive is in request header
		// register(NoCacheRequestFilter.class);

		// Allow reply compression
		EncodingFilter.enableFor(this, GZipEncoder.class);

		Logger.getGlobal().log(Level.INFO, "AldaApplication created");
	}

	private static Set<Class<?>> getResourceAndProviderClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		
		classes.add(DocumentJerseyResource.class);
		classes.add(TagJerseyResource.class);

		// classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
		// classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
		return classes;
	}

}
