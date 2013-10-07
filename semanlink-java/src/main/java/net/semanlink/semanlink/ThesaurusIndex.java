/* Created on 16 mai 2005 */
package net.semanlink.semanlink;

import java.util.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import net.semanlink.skos.SKOS;
import net.semanlink.sljena.JKeyword;
import net.semanlink.util.index.Index;
import net.semanlink.util.index.IndexEntriesCalculator;
import net.semanlink.util.index.LabelGetter;
import net.semanlink.util.index.I18nFriendlyIndexEntries;
import net.semanlink.util.index.MultiLabelGetter;
import net.semanlink.util.index.MultiLabelIndex;
import net.semanlink.util.index.MultiLabelIndex2;
import net.semanlink.util.text.WordsInString;

/*
 * Contains both an HashMap (word -> kw list) and a sorted list of the words.
 * The sorted list of words allows to search for the beginning of a word
 * (When we say "word" here, we speak of a normalized text, as produced by CharConverter)
 */
/**
 * Index a thesaurus by text of words included in tags. 
 * 
 * Used in particular by the livesearch, and to extract tags from a text. 
 */
// public class ThesaurusIndex extends Index<SLKeyword> { // SKOSIFY 
public class ThesaurusIndex extends MultiLabelIndex<SLKeyword> {
private SLModel mod;
/**
 * clés : les mots présents ds les short uri de kws (par ex homo)
 * data : les kws les contenant (par ex .../homo_sapiens). Attention, les alias sont présents,
 * mais non résolus. (see comments de getHashMap)*/
// private HashMap word2tagsHM; // thesaurusIndex ds SLModel
/** List of words in tags (in normalzed form, cf CharConverter).
 *  Sorted by construction, used for binary search */
// private ArrayList words;
/*
 * Keys: label of tags (in normalized form)
 * Data: corresponding tag - but there could be more than one, so, it must be a list (or : either a tag if there is only one, a list if there is more than one?)
 */
// private HashMap label2tagsHM;

//
// CONSTRUCTION AND UPDATES
//

ThesaurusIndex(SLModel mod, Locale locale) {
	super();
	this.mod = mod;
	init(new KwLabelGetter(), new I18nFriendlyIndexEntries(new WordsInString(true, true), this.mod.converter), locale) ;
	addIterator(this.mod.getKWsInConceptsSpaceArrayList().iterator());
}

// TODO (?) use Literal (=String + lang) ?
class KwLabelGetter implements MultiLabelGetter<SLKeyword> {
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

///**
// * 
// * @param kw
// * @param updateWords if true, this.words is updated, else not. False is used during construction, in order to avoid
// * sorting each time a kw is added to the hashmap: words are sorted only once, at the end.
// */
//// TODO
//@Override protected void addItem(SLKeyword kw, boolean updateWords) {
//	// super.addItem(kw, updateWords);
//
//	String label = kw.getLabel();
//	Locale locale = Locale.getDefault();
//	boolean needToSortWords = addLabel(kw, label, locale, updateWords); // todo locale
//	
////	List aliasUriList = this.mod.getAliasUriList(kw);
////	if (aliasUriList != null) {
////		for (int ial = 0; ial < aliasUriList.size(); ial++) {
////			String aliasUri = (String) aliasUriList.get(ial);
////			SLKeyword alias = this.mod.getKeyword(aliasUri);
////			label = alias.getLabel();
////			boolean b = addLabel(alias, label, locale, updateWords); // todo locale
////			if (b) needToSortWords = true;
////		}
////	}
//	if (needToSortWords) Collections.sort(this.words);
//}

/*
public void addKw(SLKeyword kw, String label, Locale locale) {
	boolean needToSortWords = addLabel(kw, label, locale, true);
	if (needToSortWords) Collections.sort(this.words);
}
*/

/*
void addAlias(String aliasUri) {
	SLKeyword alias = this.mod.getKeyword(aliasUri); // ceci ne résoud pas
	add(aliasUri, alias, true);	
}
*/

/** Attention, s'il s'agit d'un alias, le kw doit être le pseudokw alias (non résolu) */
/*
void add(String uriText, SLKeyword kw) {
	add(uriText, kw, true);
}
*/

/** Attention, s'il s'agit d'un alias, le kw doit être le pseudokw alias (non résolu) */
/*private void add(String uriText, SLKeyword kw, boolean updateWords) {
	boolean needToSortWords = false;
	StringTokenizer st = kwUri2words(uriText);
	for (;st.hasMoreElements();) {
		String word = st.nextToken();
		boolean b = addWord(word, kw, updateWords);
		if (updateWords && b) needToSortWords = true;
	}
	if (needToSortWords) Collections.sort(this.words);
}*/

///**
// *  Search for tags containing all words in text, cf livesearch. 
// *  (search for the beginning of words: if text is "sem", returns "semanlink", "semantic web", etc.)
// *  Beware, the result list also contains aliases that are not resolved. Not sorted. */
//@Override public List<SLKeyword> search(String text) {
//	Set<SLKeyword> x = searchText(text);
//	ArrayList<SLKeyword> alx = new ArrayList<SLKeyword>(x.size());
//	alx.addAll(x);
//	return alx;
//}

/** BEWARE: only looks for the main label, doesn't take care of alias // to be changed when we'll switch to using several labels
 * instead of alias */
public void deleteKw(SLKeyword kw) {
	deleteItem(kw);
} // deleteItem

// ATTENTION
// DUPLIQUE DS Index -- à part le truc sur le thesaurusUri. Serait à rempalcer par un si il devait y avoir une évolution
//
/*
 * 		if (thesaurusUri != null) {
			// supprimer de la liste les kws trouvés qui ne sont pas de ce thesaurus
			// Ce qui implique de cloner la liste pour éviter les effets de bord
			ArrayList al = new ArrayList(kws.size());
			for (int j = 0; j < kws.size(); j++) {
				SLKeyword kw = (SLKeyword) kws.get(j);
				if (kw.getURI().startsWith(thesaurusUri)) {
					al.add(kw);
				}
			}
			kws = al;
		}

 */

// TODO PROBLEME DE LOCALE
/** 
 * Les keywords d'un texte.
 * Si thesaurusUri est non null, ne prend que des kws ds ce thesaurus
 * (TODO : ATTENTION ce filtre ne serait peut être pas être correct si on avait des alias
 * d'un vocab pointant vers un autre vocab)
 */
public Collection<SLKeyword> getKeywordsInText(String text, Locale locale, String thesaurusUri) {
	Set<SLKeyword> hs = getKeywordsInText(text);
//	// There can be some aliases in there: let's replace them by the main kw
//	ArrayList<SLKeyword> resolveds = null, aliases = null;
//	for (Iterator<SLKeyword> it = hs.iterator(); it.hasNext();) {
//		SLKeyword kw = it.next();
//		SLKeyword resolved = this.mod.resolveAlias(kw.getURI());
//		if (!resolved.equals(kw)) {
//			if (aliases == null) {
//				resolveds = new ArrayList<SLKeyword>();
//				aliases = new ArrayList<SLKeyword>();
//				resolveds.add(resolved);
//				aliases.add(kw);
//			}
//		}
//	}
//	if (aliases != null) {
//		hs.removeAll(aliases);
//		hs.addAll(resolveds);
//	}
	
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
	/* 
	// ne s'occupe pas des doubles ? // TODO
	HashSet hs = new HashSet();
	// word in text
	ArrayList wordsInText = (this.wordsInString).words(text, locale);
	int nbWords = wordsInText.size();
	ArrayList convertedWordsInText = new ArrayList(nbWords);
	for (int i = 0; i < nbWords ; i++) {
		convertedWordsInText.add(this.mod.converter.convert((String) wordsInText.get(i)));
	}
	wordsInText = convertedWordsInText;
	// We sort them, because we use it (search for "wordsInText needs to be sorted")
	Collections.sort(wordsInText);
	
	// Several different ways to decide whether a kw applies to the text:
	// (S3):
	// label of kw is in text
	// (S3 bis)
	// normalized label of kw is in normalized text
	// (S2):
	// hs.add(kw);
	// (S1)
	// all the words of kw are in wordsInText?
	// - (S1) all the words of kw are in wordsInText
	// - (S2) one of the words of kw is in wordsInText (NO : condition is to weak, at least to add, by default, this kw to the model
	// - (S3) label of kw is in text
	// - (S3 bis) normalized label of kw is in normalized text (don't sort wordsInText to do that)
	for (int i = 0; i < nbWords ; i++) {
		String word = (String) wordsInText.get(i);
		// les kws contenant word
		List kws = (List ) this.word2tagsHM.get(word);
		
		if (kws == null) continue;
		if (thesaurusUri != null) {
			// supprimer de la liste les kws trouvés qui ne sont pas de ce thesaurus
			// Ce qui implique de cloner la liste pour éviter les effets de bord
			ArrayList al = new ArrayList(kws.size());
			for (int j = 0; j < kws.size(); j++) {
				SLKeyword kw = (SLKeyword) kws.get(j);
				if (kw.getURI().startsWith(thesaurusUri)) {
					al.add(kw);
				}
			}
			kws = al;
		}
		
		// remarquons qu'un kw d'un seul mot est a priori bon à prendre,
		// mais attention, pour éviter de mettre à la fois, par ex pour le texte "C2G rend visite à Nissan",
		// à la fois c2g, nissan et c2g_nissan, on ne retiendra en définitive un tel kw que
		// si on ne retient pas de kw de plusieurs mots le contenant, d'où :
		SLKeyword oneTokenKw = null; // éventuel kw à un seul token (supposé unique ds kws : ne serait
		// pas le cas uniquement si même short kw ds 2 thesaurus différents)
		boolean addOneTokenKw = true; // a priori, si oneTokenKw != null, on le mettra, si on ne met pas de kw plus long
		for (int ikw = 0; ikw < kws.size(); ikw++) {
			SLKeyword kw = (SLKeyword) kws.get(ikw);
			// (S2):
			// hs.add(kw);
			// (S1)
			// all the words of kw are in wordsInText?

			// si un des éléments est un kw à un seul token, celui est a priori à prendre, (cf plus haut)
			// mais seulement si on ne met pas de kw à plusiers mots le contenant
			String[] normalizedWordsInKw = kw2words(kw, locale);
			int nbTokens = normalizedWordsInKw.length;
			if (nbTokens == 1) {
				// hs.add(kw);
				oneTokenKw = kw;
			} else { // kw composé de plusieurs mots
				// (S1): verify whether all the tokens composing kw are in wordsInText
				boolean addIt = true;
				for (int iToken = 0; iToken < nbTokens; iToken++) {
					String kwItem = normalizedWordsInKw[iToken];
					// is-it in wordsInText ?
					// if kwItem is word itself, then yes, of course, it is in wordsInText
					if (kwItem.equals(word)) continue;
					int k = Collections.binarySearch(wordsInText, kwItem); // "wordsInText needs to be sorted"
					if (k < 0) { // kwItem not in wordsInText
						addIt = false;
						break;
					}
				} // for iToken
				if (addIt) {
					// Beware, kw can be an alias
					// therefore, don't do:
					// hs.add(kw);
					// but:
					hs.add(this.mod.resolveAlias(kw.getURI()));
					addOneTokenKw = false;
				}
			} // if nbTokens
		} // for ikws
		if ((oneTokenKw != null) && (addOneTokenKw)) {
			// Beware, oneTokenKw can be an alias
			// therefore, don't do:
			// hs.add(kw);
			// but:
			hs.add(this.mod.resolveAlias(oneTokenKw.getURI()));
		}
	}
	SLKeyword[] x = new SLKeyword[hs.size()];
	hs.toArray(x);
	return x;
	*/
}

//
//
//

public SLKeyword[] label2Keyword(String kwLabel, Locale locale) {
	List alx = label2KeywordList(kwLabel, locale);
	SLKeyword[] x = new SLKeyword[alx.size()];
	alx.toArray(x);
	return x;
}


}
