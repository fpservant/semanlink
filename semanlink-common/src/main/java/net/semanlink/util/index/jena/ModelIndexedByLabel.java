/* Created on 18 mars 2010 */
package net.semanlink.util.index.jena;

import java.util.*;

import org.apache.jena.rdf.model.*;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.MultiLabelIndex;
import net.semanlink.util.index.MultiLabelGetter;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

/**
 * Index of resources (indexed by label)
 * see  ModelIndexedByLabel2 which indexes (resource, label) pairs
 * @author fps
 */
public class ModelIndexedByLabel extends MultiLabelIndex<Resource> {
protected Model model;

/** Default: uses RDFS.label. Beware, only index the labels in the language given by the Locale*/
public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, Model model, Locale locale) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(locale.getLanguage()), model, locale);
}

/** Default: uses RDFS.label.*/
public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, Model model, Locale locale, boolean indexLabelInAnyLang) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(indexLabelInAnyLang ? null : locale.getLanguage()), model, locale);
}

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> labelGetter, Model model, Locale locale) {
	this(resToBeIndexedByLabel, labelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> labelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	this(labelGetter, iec, model, locale);
	addIterator(resToBeIndexedByLabel);
}

/** addIterator or addCollection must be called after that */
protected ModelIndexedByLabel(MultiLabelGetter<Resource> labelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	super();
	this.model = model;
	this.locale = locale;
	init(labelGetter, iec, locale) ;
}
}
