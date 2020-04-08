/* Created on 18 mars 2010 */
package net.semanlink.util.index_B4_2020_04.jena;

import java.util.Locale;

import net.semanlink.util.index_B4_2020_04.I18nFriendlyIndexEntries;
import net.semanlink.util.index_B4_2020_04.IndexEntriesCalculator;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index_B4_2020_04.MultiLabelIndex2;
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
public class ModelIndexedByLabel2 extends MultiLabelIndex2<Resource> {
protected Model model;

/** Default: uses RDFS.label. Beware, only index the labels in the language given by the Locale*/
public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, Model model, Locale locale) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(locale.getLanguage()), model, locale);
}

/** Default: uses RDFS.label.*/
public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, Model model, Locale locale, boolean indexLabelInAnyLang) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(indexLabelInAnyLang ? null : locale.getLanguage()), model, locale);
}

public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> multiLabelGetter, Model model, Locale locale) {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, LabelGetter<Resource> multiLabelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	this(iec, model, locale);
	addResIterator(resToBeIndexedByLabel, multiLabelGetter);
}

/** addIterator or addCollection must be called after that */
protected ModelIndexedByLabel2(IndexEntriesCalculator iec, Model model, Locale locale) {
	super(iec,locale);
	this.model = model;
}
}
