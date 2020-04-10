package net.semanlink.util.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;

import net.semanlink.util.text.CharConverter;

/**
 * Structure allowing to extract entities mentioned in a text.
 * 
 * @since v0.7.2
 */
public class AhoCorasick<E> { // 2020-04
private CharConverter converter;
private PayloadTrie<E> trie;

public AhoCorasick(Iterator<E> kws, LabelGetter<E> labelGetter, CharConverter converter) {
	this.converter = converter;
	
	// we do not use ignoreCase, because we also want to handle diacritics.
	// So we store normalized (lowercase wo diacritics) labels in the Trie,
	// and we also normalize the text we want to scan.

	PayloadTrieBuilder<E> trieBuilder = PayloadTrie.builder();
	trieBuilder.ignoreOverlaps()
			// .ignoreCase()
			.onlyWholeWords();
			// .onlyWholeWordsWhiteSpaceSeparated(); // no probably cause the converter we use
	
	// ResIterator kws = kwsModel.listSubjectsWithProperty(RDF.type, kwsModel.getResource(SLSchema.Tag.getURI()));	
	for(;kws.hasNext();) {
		E kw = kws.next();
		Iterator<String> labs = labelGetter.getLabels(kw);
		for (;labs.hasNext();) {
			String lab = labs.next();
			// convert the labels to a normalized form
			lab = converter.convert(lab);
			trieBuilder.addKeyword(lab, kw);
		}
	}
	trie = trieBuilder.build();
}

/** Extract the tags mentioned in a text. */
public ArrayList<E> tagList(String text) {
	 // convert the text to the normalized form
	text = converter.convert(text);
	Collection<PayloadEmit<E>> emits = trie.parseText(text);
	HashSet<E> tags = new HashSet<>();
	for (PayloadEmit<E> emit : emits) {
		// System.out.println(emit.getKeyword() + " : " + emit.getPayload() + " pos: " + emit.getStart() + "/" + emit.getEnd());
		tags.add(emit.getPayload());
	}
	ArrayList<E> x = new ArrayList<>();
	x.addAll(tags);
	return x;
}
} // AhoCorasick
