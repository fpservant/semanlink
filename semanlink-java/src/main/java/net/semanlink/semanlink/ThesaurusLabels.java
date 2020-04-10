/* Created on 16 mai 2005 */
package net.semanlink.semanlink;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.semanlink.util.index.AhoCorasick;
import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.WordIndexInterface;
import net.semanlink.util.text.CharConverter;

/**
 * Structures used for the handling of tag labels
 * 
 * @since 0.7.2 (2020-04), we only had ThesaurusWordIndex (inverted index: words in tags -> (tag, label))
 * We now also use a Aho-Corasick Trie to search for tag labels in text.
 */
public class ThesaurusLabels implements WordIndexInterface<ObjectLabelPair<SLKeyword>> {
private ThesaurusWordIndex thIndex;
private AhoCorasick<SLKeyword> aho;

private SLModel mod;
private CharConverter converter;

//
// CONSTRUCTION AND UPDATES
//

ThesaurusLabels(SLModel mod, CharConverter converter, Locale locale) throws Exception {
	this.mod = mod;
	this.converter = converter;
	List<SLKeyword> kws = mod.getKWsInConceptsSpaceArrayList();
	thIndex = new ThesaurusWordIndex(kws.iterator(), mod.getKwLabelGetter(), locale);
	aho = new AhoCorasick<>(kws.iterator(), mod.getKwLabelGetter(), converter);
}

private AhoCorasick<SLKeyword> newAho() {
	return new AhoCorasick<>(mod.getKWsInConceptsSpaceArrayList().iterator(), mod.getKwLabelGetter(), converter);
}
public void deleteKw(SLKeyword kw) throws Exception {
	thIndex.deleteKw(kw);
	this.aho = newAho();
}

public void addKw(SLKeyword kw) {
	thIndex.addKw(kw);
	this.aho = newAho();
}

public void addKw(SLKeyword kw, String label, Locale locale) {
	thIndex.addKw(kw, label, locale);
	this.aho = newAho();
}

//
//
//

@Override // implements WordIndexInterface<ObjectLabelPair<SLKeyword>>
public Set<ObjectLabelPair<SLKeyword>> string2entities(String searchString) {
	return thIndex.string2entities(searchString);
}

//
// SEARCHING TAGS IN A TEXT
//

// TODO PROBLEME DE LOCALE

/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */

public Collection<SLKeyword> getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	return aho.tagList(text);
}

//
//
//

// 2020-03 : JUSTE FAIT POUR REIMPLEMENTER CE QUI EXISTE A FCT IDENTIQUE
// used by SLModel.kwLabel2KwCreatingItIfNecessary 
public SLKeyword[] label2Keyword(String kwLabel, Locale locale) {
	return thIndex.label2Keyword(kwLabel, locale);
}

// 2020-03
public int compareString(String s1, String s2) {
	return thIndex.compareString(s1, s2);
}
}
