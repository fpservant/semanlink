/* Created on 18 mars 2010 */
package net.semanlink.util.index.jena;

import java.util.Locale;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index.LabelIndex;
import net.semanlink.util.index.LabelIndex.Update;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

/**
 * Indexing (Resource, label) pairs.
 * <p>When compared to ModelIndexedByLabel (which indexes Resources, by several labels),
 * this allows to return the found label.
 * @author fps
 */
public class ModelIndexedByLabel extends LabelIndex<Resource> {
protected Model model;

/** Default: uses RDFS.label. Beware, only index the labels in the language given by the Locale
 * @throws Exception */
public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, Model model, Locale locale) throws Exception {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(locale.getLanguage()), model, locale);
}

/** Default: uses RDFS.label.
 * @throws Exception */
public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, Model model, Locale locale, boolean indexLabelInAnyLang) throws Exception {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(indexLabelInAnyLang ? null : locale.getLanguage()), model, locale);
}

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> multiLabelGetter, Model model, Locale locale) throws Exception {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> multiLabelGetter, IndexEntriesCalculator iec, Model model, Locale locale) throws Exception {
	this(multiLabelGetter, iec, model, locale);
	try (Update<Resource> up = new Update<>(this)) {
		up.addIterator(resToBeIndexedByLabel);
	}
}

/** addIterator or addCollection must be called after that */
protected ModelIndexedByLabel(LabelGetter<Resource> labelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	super(labelGetter, iec,locale);
	this.model = model;
}
}
