/* Created on 16 mai 2005 */
package net.semanlink.semanlink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.semanlink.skos.SKOS;
import net.semanlink.sljena.JKeyword;
import net.semanlink.util.index_B4_2020_04.I18nFriendlyIndexEntries;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index_B4_2020_04.MultiLabelIndex;
import net.semanlink.util.text.WordsInString;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Index a thesaurus by text of words included in tags. 
 * 
 * Used in particular by the livesearch, and to extract tags from a text. 
 */

// TODO would be better to use MultiLabelIndex2, that returns both the found label and the kw
// Would indeed allow to display the found label, not the pref label of the found KW
// (would also optimize the returning of the "top matches" in search)
public class ThesaurusIndex_B4_2020_03 extends MultiLabelIndex<SLKeyword> {
private SLModel mod;

//
// CONSTRUCTION AND UPDATES
//

ThesaurusIndex_B4_2020_03(SLModel mod, Locale locale) {
	super();
	this.mod = mod;
	init(new KwLabelGetter(), new I18nFriendlyIndexEntries(new WordsInString(true, true), this.mod.converter), locale) ;
	addIterator(this.mod.getKWsInConceptsSpaceArrayList().iterator());
}

// TODO (?) use Literal (=String + lang) ?
class KwLabelGetter implements LabelGetter<SLKeyword> {
	public Iterator<String> getLabels(SLKeyword o) {
		JKeyword kw = (JKeyword) o;
		Resource res = kw.getRes();
		Model m = res.getModel();
		ExtendedIterator<RDFNode> x;
		x = m.listObjectsOfProperty(res, SKOS.prefLabel);
		x = x.andThen(m.listObjectsOfProperty(res, SKOS.altLabel));
		ArrayList<String> al = new ArrayList<String>();
		for(;x.hasNext();) {
			al.add(x.next().toString());
		}
		return al.iterator();
	}
}

/** BEWARE: only looks for the main label, doesn't take care of alias // to be changed when we'll switch to using several labels
 * instead of alias */
public void deleteKw(SLKeyword kw) {
	deleteItem(kw);
}

// TODO PROBLEME DE LOCALE
/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */
public Collection<SLKeyword> getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	Set<SLKeyword> hs = getKeywordsInText(text);
	
	if (thesaurusUri != null) {
		// supprimer les kws trouvés qui ne sont pas de ce thesaurus
		ArrayList<SLKeyword> al = new ArrayList<SLKeyword>(hs.size());
		for (Iterator<SLKeyword> it = hs.iterator(); it.hasNext();) {
			SLKeyword kw = it.next();
			if (kw.getURI().startsWith(thesaurusUri)) {
				al.add(kw);
			}
		}
		return al;
	} else {
		return hs;
	}
}

//
//
//

public SLKeyword[] label2Keyword(String kwLabel, Locale locale) {
	List<SLKeyword> alx = label2KeywordList(kwLabel, locale);
	SLKeyword[] x = new SLKeyword[alx.size()];
	alx.toArray(x);
	return x;
}


}
