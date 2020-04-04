/* Created on Apr 4, 2020 */
package ahocorasick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.PayloadEmitHandler;
import org.ahocorasick.trie.handler.StatefulPayloadEmitHandler;
import org.ahocorasick.trie.Trie;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.semanlink.SLSchema;
import net.semanlink.skos.SKOS;
import net.semanlink.sljena.ModelFileIOManager;
import net.semanlink.util.index.MultiLabelGetter;
import net.semanlink.util.text.CharConverter;

// /semanlink/tag/aho_corasick_algorithm.html
// https://github.com/robert-bor/aho-corasick

public class AhoCorasicTest {

@BeforeClass
public static void setUpBeforeClass() throws Exception {
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
}

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

private String text = "Quel écosysteme ?"
		+ " est-ce qu'on trouve BERT.hml ?"
		+ " et BERT?"
		+ " et l'astronomie ?"
		+ "Knowledge graphs, are important resources for many artificial intelligence "
		+ "tasks but often suffer from incompleteness. In this work, we propose to use pre-trained language models for knowledge graph completion. "
		+ "We treat triples in knowledge graphs as textual sequences and propose a novel framework "
		+ "named Knowledge Graph Bidirectional Encoder Representations from Transformer (KG-BERT) "
		+ "to model these triples. Our method takes entity and relation descriptions of a triple as "
		+ "input and computes scoring function of the triple with the KG-BERT language model. "
		+ "Experimental results on multiple benchmark knowledge graphs show that our method can "
		+ "achieve state-of-the-art performance in triple classification, link prediction and relation prediction tasks."
		+ "BERTLOGIE GRAP";
@Test
public final void test() {
	Trie trie = Trie.builder()
			.ignoreOverlaps()
			.ignoreCase()
			.onlyWholeWords()
	    .addKeyword("Graph")
	    .addKeyword("Graphs")
	    .addKeyword("Knowledge Graph")
	    .addKeyword("Knowledge Graphs")
	    .addKeyword("Artificial Intelligence")
	    .addKeyword("Language Model")
	    .addKeyword("BERT")
	    .build();
	Collection<Emit> emits = trie.parseText(text);
	
	for (Emit emit : emits) {
		System.out.println(emit);
	}
}

static class KWord {
  private final String value;
  public KWord(String value) {
      this.value = value;
  }
  @Override public String toString() { return value; }
}

@Test
// PayloadTrie requires aho-corasick more than 0.4.0. ok with 0.6.0
public final void test2() {
	PayloadTrieBuilder<KWord> trieBuilder = PayloadTrie.builder();
	trieBuilder.ignoreOverlaps()
			.ignoreCase()
			.onlyWholeWords();
	
	for (String kw : getKws()) {
		trieBuilder.addKeyword(kw, new KWord(kw));
	}

	PayloadTrie trie = trieBuilder.build();
	Collection<PayloadEmit<KWord>> emits = trie.parseText(text);
	for (PayloadEmit<KWord> emit : emits) {
		System.out.println(emit.getKeyword() + " : " + emit.getPayload());
	}

}


List<String> getKws() {
	List<String> kws = new ArrayList<>();
	kws.add("Graph");
  kws.add("Graphs");
  kws.add("Knowledge Graph");
  kws.add("Knowledge Graphs");
  kws.add("Artificial Intelligence");
  kws.add("Language Model");
  kws.add("BERT");
  return kws;
}

private Model kwsModel; // use getter
Model getKWsModel() {
	if (kwsModel == null) {
		try {
			kwsModel = loadKwsModel();
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	return kwsModel;
}


//PayloadTrie requires aho-corasick more than 0.4.0. ok with 0.6.0
@Test public final void testWithRealKwsModel() {
	
	// we do not use ignoreCase, because we also want to handle diacritics.
	// So we store normalized (lowercase wo diacritics) labels in the Trie,
	// and we also normalize the text we want to scan.
	
	PayloadTrieBuilder<Resource> trieBuilder = PayloadTrie.builder();
	trieBuilder.ignoreOverlaps()
			// .ignoreCase()
			.onlyWholeWords();
	
	Model kwsModel = getKWsModel();
	KwResLabelGetter labelGetter = new KwResLabelGetter();
	CharConverter converter = new CharConverter(Locale.FRENCH);
	ResIterator kws = kwsModel.listSubjectsWithProperty(RDF.type, kwsModel.getResource(SLSchema.Tag.getURI()));
	
	int klab = 0, kkw = 0;
	for(;kws.hasNext();) {
		Resource kw = kws.next();
		kkw++;
		Iterator<String> labs = labelGetter.getLabels(kw);
		for (;labs.hasNext();) {
			String lab = labs.next();
			// convert the labels to a normalized form
			lab = converter.convert(lab);
			klab++;
			trieBuilder.addKeyword(lab, kw);
		}
	}
	
	System.out.println("Nb labels: " + klab + " nb kws: " + kkw);

	PayloadTrie<Resource> trie = trieBuilder.build();
	
 // convert the text to the normalized form
	text = converter.convert(text);
	Collection<PayloadEmit<Resource>> emits = trie.parseText(text);
	HashSet<Resource> tags = new HashSet<>();
	for (PayloadEmit<Resource> emit : emits) {
		// System.out.println(emit.getKeyword() + " : " + emit.getPayload() + " pos: " + emit.getStart() + "/" + emit.getEnd());
		tags.add(emit.getPayload());
	}
	ArrayList<Resource> tagsList = new ArrayList<>();
	tagsList.addAll(tags);
	for (Resource tag : tagsList) {
		System.out.println(tag);
	}
	
	
}

// variation on testWithRealKwsModel
// in the way we would use it in semanlink: extracting kws
// from a text
// Pas réussi à faire marcher un custom emit handler
// comme je voulais : ignoreOverlaps et onlyWHoleWOrds KO

//// DOES4NT WORK AS EXPECTED: ne prend pas en compte le ignoreOverlaps
//// et le onlyWholeWords
//@Test public final void testExtractKWsFromText() {
//	
//	// we do not use ignoreCase, because we also want to handle diacritics.
//	// So we store normalized (lowercase wo diacritics) labels in the Trie,
//	// and we also normalize the text we want to scan.
//	
//	PayloadTrieBuilder<Resource> trieBuilder = PayloadTrie.builder();
//	trieBuilder.ignoreOverlaps()
//			// .ignoreCase()
//			.onlyWholeWords();
//	
//	Model kwsModel = getKWsModel();
//	KwResLabelGetter labelGetter = new KwResLabelGetter();
//	CharConverter converter = new CharConverter(Locale.FRENCH);
//	ResIterator kws = kwsModel.listSubjectsWithProperty(RDF.type, kwsModel.getResource(SLSchema.Tag.getURI()));
//	
//	int klab = 0, kkw = 0;
//	for(;kws.hasNext();) {
//		Resource kw = kws.next();
//		kkw++;
//		Iterator<String> labs = labelGetter.getLabels(kw);
//		for (;labs.hasNext();) {
//			String lab = labs.next();
//			// convert the labels to a normalized form
//			lab = converter.convert(lab);
//			klab++;
//			trieBuilder.addKeyword(lab, kw);
//		}
//	}
//	
//	System.out.println("Nb labels: " + klab + " nb kws: " + kkw);
//
//	PayloadTrie<Resource> trie = trieBuilder.build();
//	
// // convert the text to the normalized form
//	text = converter.convert(text);
//	
//	HashSet<Resource> tags = new HashSet<>();
//	
//	StatefulPayloadEmitHandler<Resource> emitHandler  = new StatefulPayloadEmitHandler<Resource>() {
//    @Override
//    public boolean emit(PayloadEmit<Resource> emit) {
//    	tags.add(emit.getPayload());
//    	return true;
//    }
//
//		@Override
//		public List<PayloadEmit<Resource>> getEmits() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	};
//
//	Collection<PayloadEmit<Resource>> emits = trie.parseText(text, emitHandler);
//	trie.parseText(text, emitHandler);
//
//	for (Resource res : tags) {
//		System.out.println(res);
//	}
//}
//

//
//
//

Model loadKwsModel() throws JenaException, IOException {
	Model kwsModel = ModelFactory.createDefaultModel();
	String longFilename = "/Users/fps/Semanlink/semanlink-fps/tags/slkws.rdf";
	String base = "http://www.semanlink.net/tag/";
	ModelFileIOManager.readModel(kwsModel, longFilename, base);
	return kwsModel;
}

static class KwResLabelGetter implements MultiLabelGetter<Resource> {
	public Iterator<String> getLabels(Resource kwres) {
		Model m = kwres.getModel();
		ExtendedIterator<RDFNode> x;
		x = m.listObjectsOfProperty(kwres, SKOS.prefLabel);
		x = x.andThen(m.listObjectsOfProperty(kwres, SKOS.altLabel));
		ArrayList<String> al = new ArrayList<String>();
		for(;x.hasNext();) {
			RDFNode n = x.next();
			if (n instanceof Literal) { // shouldn't be otherwise, but better to be sure
				al.add(((Literal) n).getString());
			}
		}
		return al.iterator();
	}
}


}
