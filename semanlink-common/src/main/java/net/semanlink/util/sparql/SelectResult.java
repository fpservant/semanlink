package net.semanlink.util.sparql;

import java.io.IOException;
import java.io.InputStream;

import nu.xom.*;

/**
 * Utility class to parse the XML returned by a select sparql query
 * @author fps
 */
public class SelectResult {
private String[] variables;
private Result[] results;
public SelectResult(InputStream sparqlSelectIS) throws ParsingException, IOException {
  Builder parser = new Builder();
  Document doc = parser.build(sparqlSelectIS);
  Element root = doc.getRootElement();
  if (!"sparql".equals(root.getQualifiedName())) throw new IllegalArgumentException("InputStream doesn't contain the result of a SPARQL SELECT query");
  
  String ns = "http://www.w3.org/2005/sparql-results#";
  
  // PARSING THE HEAD
  
  Elements headElements = root.getChildElements("head", ns);
  if (headElements.size() < 1) throw new RuntimeException("No head!");
  if (headElements.size() > 1) throw new RuntimeException("Too many heads for me!");
  Element headElement = headElements.get(0);
  
  Elements variableElements = headElement.getChildElements("variable", ns);
  int varNb = variableElements.size();
  variables = new String[varNb];
  for (int i = 0; i < varNb; i++) {
  	variables[i] = variableElements.get(i).getAttributeValue("name");
  }
  
  // PARSING RESULTS
  
  Elements resultsElements = root.getChildElements("results", ns);
  if (resultsElements.size() < 1) throw new RuntimeException("No results!");
  if (resultsElements.size() > 1) throw new RuntimeException("Too many result tags for me!");
  Element resultsElement = resultsElements.get(0);
  
  Elements resultElements = resultsElement.getChildElements("result", ns);
  int resNb = resultElements.size();
  results = new Result[resNb];
  for (int ires = 0; ires < resNb; ires++) {
  	Element resultElement = resultElements.get(ires);
  	Binding[] bindings = new Binding[varNb];
  	Elements bindingElements = resultElement.getChildElements("binding", ns);
  	// assert(bindingElements.size() == varNb); // probablement faux (si des var absentes du result) -- de fait
  	// for (int ibin = 0 ; ibin < varNb; ibin++) {
  	for (int ibin = 0 ; ibin < bindingElements.size(); ibin++) {
  		Element bindingElement = bindingElements.get(ibin);
  		String var =  bindingElement.getAttributeValue("name");
  		Elements uriElements = bindingElement.getChildElements("uri", ns);
  		assert(uriElements.size() < 2);
  		if (uriElements.size() == 1) {
  			assert(bindingElement.getChildElements("literal", ns).size() == 0);
  			String uri = uriElements.get(0).getValue();
  			bindings[ibin] = new Binding(var, uri);
  		} else {
  			Elements literalElements = bindingElement.getChildElements("literal", ns);
  	 		assert(literalElements.size() == 1);
  	 		Element literalElement = literalElements.get(0);
  	 		String lang = null;
  	 		Attribute langAttr = literalElement.getAttribute("xml:lang");
  	 		if (langAttr != null) lang = langAttr.getValue();
  			bindings[ibin] = new Binding(var, literalElement.getValue(), lang);
  		}
  	}
    results[ires] = new Result(bindings);
  }
}

public String[] getVariables() { return this.variables; }
public Result[] getResults() { return this.results; }
public int size() { return this.results.length; }

static public class Result {
	Binding[] bindings;
	Result(Binding[] bindings) { this.bindings = bindings; }
	public Binding[] getBindings() { return this.bindings; }
}

static public class Binding {
	private String var;
	private String val;
	private Type type;
	private String lang;
	public enum Type {
		URI,
		LITERAL
	};
	Binding(String var, String uri) {
		this.var = var;
		this.val = uri;
		this.type = Type.URI;
	}
	Binding(String var, String literal, String lang) {
		this.var = var;
		this.val = literal;
		this.type = Type.LITERAL;
		this.lang = lang;
	}
	
	public String getVariableName() { return this.var; }
	public boolean isUriType() { return this.type == Type.URI; }
	/** null if !isUriType() */
	public String getUri() {
		if (isUriType()) return this.val;
		return null;
	}
	public String getValue() { return this.val; }
	/** null if isUriType() */
	public String getLiteral() {
		if (!isUriType()) return this.val;
		return null;
	}
	/** null if isUriType() */
	public String getLang() {
		// if isUriType(), null by construction
		return this.lang;
	}
}


/*
<?xml version="1.0"?>
<sparql xmlns="http://www.w3.org/2005/sparql-results#">
  <head>
    <variable name="tag"/>
    <variable name="label"/>
  </head>
  <results>
    <result>
      <binding name="tag">
        <uri>http://www.semanlink.net/tag/niger</uri>
      </binding>
      <binding name="label">
        <literal xml:lang="fr">Niger</literal>
      </binding>
    </result>
    <result>
      <binding name="tag">
        <uri>http://www.semanlink.net/tag/mali</uri>
      </binding>
      <binding name="label">
        <literal xml:lang="fr">Mali</literal>
      </binding>
    </result>
  </results>
</sparql>
 */

}
