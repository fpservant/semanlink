/* Created on 16 mai 2005 */
package net.semanlink.semanlink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index.LabelIndex;
import net.semanlink.util.index.ObjectLabelPair;
import net.semanlink.util.index.LabelIndex.Update;

/**
 * Index a thesaurus by text of words included in tags. 
 * 
 * Used by the livesearch.
 * 
 * (Before 2020-04, was also used to extract tags from a text. 
 * Now replaced by the use of Aho-Corasick algorithm)
 */

public class ThesaurusIndex extends LabelIndex<SLKeyword> {
protected LabelGetter<SLKeyword> kwLabelGetter;

//
// CONSTRUCTION AND UPDATES
//

ThesaurusIndex(SLModel mod, Locale locale) throws Exception {
	this(mod.getKWsInConceptsSpaceArrayList().iterator(), mod.getKwLabelGetter(), locale);
}

ThesaurusIndex(Iterator<SLKeyword> resToBeIndexedByLabel, LabelGetter<SLKeyword> kwLabelGetter, Locale locale) throws Exception {
	super(resToBeIndexedByLabel, kwLabelGetter, LabelIndex.newI18nFriendlyIndexEntries(locale), locale);
	this.kwLabelGetter = kwLabelGetter;
}

public void deleteKw(SLKeyword kw) throws Exception {
	try (Update<SLKeyword> up = new Update<>(this, false)) {
		up.deleteItem(kw);
	}
}

public void addKw(SLKeyword kw) {
	try (Update<SLKeyword> up = new Update<>(this, false)) {
		up.addItem(kw);
	} catch (Exception e) { throw new RuntimeException(e) ; }
}

public void addKw(SLKeyword kw, String label, Locale locale) {
	try (Update<SLKeyword> up = new Update<>(this, false)) {
		up.addLabel(kw, label, locale);
	} catch (Exception e) { throw new RuntimeException(e) ; }
}


//
// SEARCHING TAGS IN A TEXT
//

// 2020-04: better to use Aho-Corasick algo !

// TODO PROBLEME DE LOCALE
/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */

public Collection<SLKeyword> getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	// Set<SLKeyword> hs = getKeywordsInText(text);
	Set<ObjectLabelPair<SLKeyword>> hs = getKeywordsInText(text);
	
	ArrayList<SLKeyword> al = new ArrayList<SLKeyword>(hs.size());
	if (thesaurusUri != null) {
		// supprimer les kws trouvés qui ne sont pas de ce thesaurus
		for (Iterator<ObjectLabelPair<SLKeyword>> it = hs.iterator(); it.hasNext();) {
			ObjectLabelPair<SLKeyword> pair = it.next();
			SLKeyword kw = pair.getObject();
			if (kw.getURI().startsWith(thesaurusUri)) {
				al.add(kw);
			}
		}
	} else {
		for (Iterator<ObjectLabelPair<SLKeyword>> it = hs.iterator(); it.hasNext();) {
			ObjectLabelPair<SLKeyword> pair = it.next();
			SLKeyword kw = pair.getObject();
			al.add(kw);
		}		
	}
	return al;
}

//
//
//

// 2020-03 : JUSTE FAIT POUR REIMPLEMENTER CE QUI EXISTE A FCT IDENTIQUE
/** @deprecated */
public SLKeyword[] label2Keyword(String kwLabel, Locale locale) {
	List<ObjectLabelPair<SLKeyword>> alx = label2KeywordList(kwLabel, locale);
	SLKeyword[] x = new SLKeyword[alx.size()];
	int i = 0;
	for (ObjectLabelPair<SLKeyword> pair : alx) {
		x[i] = pair.getObject();
	}
	return x;
}

// 2020-03
public int compareString(String s1, String s2) {
	return this.collator.compare(s1, s2);
}
}
