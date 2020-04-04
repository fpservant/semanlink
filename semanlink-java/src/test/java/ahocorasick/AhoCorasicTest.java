/* Created on Apr 4, 2020 */
package ahocorasick;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

private String text = "Knowledge graphs, are important resources for many artificial intelligence "
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


}
