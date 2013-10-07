/* Created on 26 août 2012 */
package net.semanlink.util.jsonld;

import java.io.IOException;
import java.io.OutputStream;

//import org.codehaus.jackson.JsonGenerator;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.ObjectWriter;

import com.hp.hpl.jena.rdf.model.Model;

// import de.dfki.km.json.jsonld.impl.JenaJSONLDSerializer;

import net.semanlink.lod.JsonLDSerializer;

public class JsonLDSerializerImpl implements JsonLDSerializer {
private boolean pretty;
public JsonLDSerializerImpl(boolean pretty) { this.pretty = pretty ; }

@Override
public void rdf2jsonld(Model model, OutputStream out) throws IOException {
	throw new RuntimeException("sorry, currently no more supported");
//	 // Create an instance of the Jena serializer
//	JenaJSONLDSerializer serializer = new JenaJSONLDSerializer();
//	// import the Jena Model
//	serializer.importModel(model);
//	// grab the resulting JSON-LD map
//	Object jsonld = serializer.asObject();	
//
//	
// ObjectMapper objectMapper = new ObjectMapper();
// objectMapper.getJsonFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
// if (pretty) {
// 	ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
// 	objectWriter.writeValue(out, jsonld);
// } else {
// 	objectMapper.writeValue(out, jsonld);
// }
}

}
