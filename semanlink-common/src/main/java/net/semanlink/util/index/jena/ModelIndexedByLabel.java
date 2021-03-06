/* Created on 18 mars 2010 */
package net.semanlink.util.index.jena;

import java.util.Locale;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index.WordIndex;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

/**
 * Indexing (Resource, label) pairs.
 * <p>When compared to ModelIndexedByLabel (which indexes Resources, by several labels),
 * this allows to return the found label.
 */
public class ModelIndexedByLabel extends WordIndex<Resource> {
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

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> labelGetter, Model model, Locale locale) throws Exception {
	this(resToBeIndexedByLabel, labelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> labelGetter, IndexEntriesCalculator iec, Model model, Locale locale) throws Exception {
	this(labelGetter, iec, model, locale);
	try (Update<Resource> up = newUpdate(true)) {
		up.addIterator(resToBeIndexedByLabel);
	}
}

protected ModelIndexedByLabel(LabelGetter<Resource> labelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	super(labelGetter, iec,locale);
	this.model = model;
}
}
