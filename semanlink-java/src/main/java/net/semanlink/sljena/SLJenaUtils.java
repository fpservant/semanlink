package net.semanlink.sljena;
import net.semanlink.semanlink.*;
import java.util.*;
import java.lang.reflect.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
public class SLJenaUtils {
/** parce qu'il n'y a que des methodes statiques dans cette classe. */
private SLJenaUtils() {}

public static Statement newStatement(Model model, String docUri, String propertyUri, String propertyValueUri) {
	Resource subject = model.createResource(docUri);
	Property ppty = model.createProperty(propertyUri);
	return model.createStatement(subject, ppty, model.createResource(propertyValueUri));
}

public static Statement newStatement(Model model, String docUri, String propertyUri, String propertyValue, String lang) {
	Resource subject = model.createResource(docUri);
	Property ppty = model.createProperty(propertyUri);
	return model.createStatement(subject, ppty, model.createLiteral(propertyValue, lang));
}

//
// UTILS - iterator and collections related
//

/**
 * Crée des JKeyword et les ajoute à la Collection passée en argument.
 * Suppose que le ResIterator retourne des resources propres à servir de kw
 * (telles des res de rdfs:Class JKeyword.)
 */
static void keywordIteratorIntoCollection(ResIterator ite, Collection collection, JModel slModel) throws JenaException {
    // resIteratorIntoCollection(ite, collection, "net.semanlink.semanlink.sljena.JKeyword");
    // on aurait pu faire plus simplement (voire plus efficacement) :
    for (;ite.hasNext();) {
			Resource res = ite.nextResource(); // une resource de rdfs:Class JKeyword
			JKeyword kw = new JKeyword(slModel, res);
			collection.add(kw);
    }
    ite.close();
}

/**
 * Ajoute à une Collection des objets de classe elementClassName construits à partir
 * des éléments retournés par un ResIterator.
 // NE MARCHE PLUS DEPUIS QU4IL FAUT PASSER LE SLMODEL POUR CONSTRUIRE UN KW OU UN DOC
 */
static void resIteratorIntoCollection(ResIterator ite, Collection collection, String elementClassName) throws JenaException {
    try {
	  Class eltClass = Class.forName(elementClassName);
	  Class[] constructorParameterTypes = new Class[1];
	  constructorParameterTypes[0] = Class.forName("com.hp.hpl.mesa.rdf.jena.model.Resource");
	  Constructor constructor = eltClass.getConstructor(constructorParameterTypes);
	  Object[] args = new Object[1];
	  for (;ite.hasNext();) {
		  // Resource res = ite.next();
		  args[0] = ite.next();
		  Object o = constructor.newInstance(args);
		  collection.add(o);
	  }
    } catch (Exception ex) {throw new SLRuntimeException (ex);}
    finally {ite.close();}
}

static void iteratorIntoCollection(Iterator ite, Collection collection) {
    for (;ite.hasNext();) {collection.add(ite.next());}
}

static void resIteratorIntoCollection(ResIterator ite, Collection collection) throws JenaException {
    for (;ite.hasNext();) {collection.add(ite.next());}
	ite.close();
}

//
// UTILS - toString related
//

static String EOL = System.getProperty("line.separator");

static String iterator2String(Iterator ite) {
  StringBuffer sb = new StringBuffer();
  for (;ite.hasNext();) {
	sb.append(ite.next().toString());
	sb.append(EOL);
  }
  return sb.toString();
}

static String resIterator2String(ResIterator ite) throws JenaException {
  StringBuffer sb = new StringBuffer();
  for (;ite.hasNext();) {
	sb.append(ite.next().toString());
	sb.append(EOL);
  }
  ite.close();
  return sb.toString();
}

public static String collection2String(Collection collection) {
  return iterator2String(collection.iterator());
}

static String collection2String(String titre, Collection collection) {
  return titre + EOL + collection2String(collection);
}

//
// BASE RELATED
//

/**
 * remove chars after the last /
 * except when base is just something like http://www.a.com
 */
static String niceBase(String base) {
	System.out.print("niceBase de " + base + " returns ");
  if ((base != null)  && (base.length() > 3)) {
    int indexLastSlash = base.lastIndexOf("/");
    if (indexLastSlash != base.length() - 1) { // last char is not a slash
      int indexFirstSlash = base.indexOf("/");
      if (indexLastSlash != indexFirstSlash+1) { // not the slash from protocol://
        base = base.substring(0, indexLastSlash+1);
      }
    }
  }
	System.out.println(base);
  return base;
}

//

static public LabelLN getLabelLN(Model model, Resource res, Property labelProp) { // 2021-01
  NodeIterator ite = model.listObjectsOfProperty(res, labelProp);
  try {
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (!(node instanceof Literal)) continue;
			Literal lit = (Literal) node;
			String s = lit.getString().trim();
			if (s.length() > 0) {
				return new LabelLNImpl(s, lit.getLanguage());
			}
		}
  } finally {
  	ite.close();
  }
	return new LabelLNImpl(res.getURI(), null);
}

static public List<LabelLN> getLabelLNs(Model model, Resource res, Property labelProp) { // 2021-01
	List<LabelLN> x = new ArrayList<>();
  NodeIterator ite = model.listObjectsOfProperty(res, labelProp);
  try {
		for (;ite.hasNext();) {
			RDFNode node = ite.nextNode();
			if (!(node instanceof Literal)) continue;
			Literal lit = (Literal) node;
			try {
				String s = lit.getString().trim();
				if (s.length() > 0) {
					x.add(new LabelLNImpl(s, lit.getLanguage()));
				}
			} catch (Exception e) {
				x.add(new LabelLNImpl(lit.toString(), null));
			}
		}
  } finally {
  	ite.close();
  }
	return x;
}




}
