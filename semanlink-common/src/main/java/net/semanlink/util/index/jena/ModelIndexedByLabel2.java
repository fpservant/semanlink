/* Created on 18 mars 2010 */
package net.semanlink.util.index.jena;

import java.util.Locale;

import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.MultiLabelGetter;
import net.semanlink.util.index.MultiLabelIndex2;
import net.semanlink.util.index.RDFSLabelGetter;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.WordsInString;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Indexing (Resource, label) pairs.
 * <p>When compared with ModelIndexedByLabel (which indexes Resources, on several labels),
 * this has the advantage of allowing to return the found label.
 * @author fps
 */
public class ModelIndexedByLabel2 extends MultiLabelIndex2<Resource> {
protected Model model;

/** Default: uses RDFS.label */
public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, Model model, Locale locale) {
	this(resToBeIndexedByLabel, new RDFSLabelGetter(locale.getLanguage()), model, locale);
}

public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter, Model model, Locale locale) {
	this(resToBeIndexedByLabel, multiLabelGetter, new I18nFriendlyIndexEntries(new WordsInString(true, true), new CharConverter(locale, "_")), model, locale);
}

public ModelIndexedByLabel2(ResIterator resToBeIndexedByLabel, MultiLabelGetter<Resource> multiLabelGetter, IndexEntriesCalculator iec, Model model, Locale locale) {
	this(iec, model, locale);
	addResIterator(resToBeIndexedByLabel, multiLabelGetter);
}

/** addIterator or addCollection must be called after that */
protected ModelIndexedByLabel2(IndexEntriesCalculator iec, Model model, Locale locale) {
	super(iec,locale);
	this.model = model;
}
}
