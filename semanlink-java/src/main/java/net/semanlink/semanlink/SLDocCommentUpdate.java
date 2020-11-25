/* Created on Nov 16, 2020 */
package net.semanlink.semanlink;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SLDocCommentUpdate { // 2020-11

// set doc's comment to a newComment, and based on this new comment
	// (and the previous value), computes and updates the sl:relatedDocs
// return true if updated, false if no change done (and this is not the same as oldComment == newComment !)
static public boolean changeComment(SLModel mod, SLDocument doc, String newComment, String lang, String contextUrl) { // 2020-11
	String oldComment = doc.getComment();
	
	List<String> oldLinks = extractLinks(oldComment, contextUrl);
	List<String> newLinks = extractLinks(newComment, contextUrl);
	
	List<SLDocument> oldRelatedDocs = doc.relatedDocs(true, false);
	
	List<String> toBeAdded = new ArrayList<>();
	for (String link : newLinks) {
		try {
			link = link2slUri(link, mod, contextUrl);
			if (link == null) continue;
			if (!isIn(link, oldRelatedDocs)) {
				// verify it isn't in oldLinks? Hum non, en tout cas pas tant qu'on n'a pas récupéré l'existant
				toBeAdded.add(link);
			}
		} catch (Exception e) {
			System.err.println("Bad link: "+ link);
		}	
	}
	
	// But we will only remove the link from this doc (maybe the reverse link is OK)
	List<String> toBeRemoved = new ArrayList<>();
	for (String link : oldLinks) {
		try {
			if (!newLinks.contains(link)) {
				toBeRemoved.add(link);
				String linkInSl = link2slUri(link, mod, contextUrl);
				if (!linkInSl.equals(link)) {
					if (!newLinks.contains(link)) {
						toBeRemoved.add(link);
					}				
				}
			}
		} catch (Exception e) {
			System.err.println("Bad link: "+ link);
		}	
	}
	
	// pour les links vers des http/// : vérifier si le doc existe dans sl
	// PB : si on a un lien vers http://, qu'on afiche le doc
	// il est en ?docuri=
	// et là, si on fait new doc en ajoutant un tag, 
	// c cette uri là qui est prise
	// (pas de création d'un bookmark nouveau) - ca risque d'être chiant à faire
	
	// not done : tag:xxx (oui, mais faire quoi ?)
	
	
	boolean somethingChanged = ((!newComment.equals(oldComment))
			|| (toBeAdded.size() > 0)
			|| (toBeRemoved.size() > 0));

	if (somethingChanged) {
		try (SLDocUpdate up = mod.newSLDocUpdate(doc)) {
			up.setDocProperty(SLVocab.COMMENT_PROPERTY, newComment, lang);
			// links in comment
			if (toBeAdded.size() > 0) {
				String[] objectUris = new String[toBeAdded.size()];
				objectUris = toBeAdded.toArray(objectUris);
				up.addDocProperty(SLVocab.SL_RELATED_DOC_PROPERTY, objectUris);
			}
			for (String link : toBeRemoved) {
				up.removeStatement(SLVocab.SL_RELATED_DOC_PROPERTY, link);
			}
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	return somethingChanged;
}

static private boolean isIn(String link, List<SLDocument> docs) {
	for (SLDocument doc : docs) {
		if (link.equals(doc.getURI())) return true;
	}
	return false;
}

public static List<String> extractLinks(String comment, String contextUrl) {
	List<String> x = new ArrayList<>();
	if (comment == null) return x;
	String regex = markdownLinkRegex();
	Pattern pat = Pattern.compile(regex);
	Matcher m = pat.matcher(comment);
  while (m.find()) {
    // Capturing groups are indexed from left to right, starting at one. 
    // Group zero denotes the entire pattern, 
    // so the expression m.group(0) is equivalent to m.group()
//	    System.out.printf("Match count: %s, Group Zero Text: '%s'%n", matchCount, m.group());
//	    for (int i = 1; i <= m.groupCount(); i++) {
//	        System.out.printf("Capture Group Number: %s, Captured Text: '%s'%n", i, m.group(i));
//	    }
    // String mdLinkText = m.group(1);
    String mdLink = m.group(2);
    String url = mdLink2Url(mdLink, contextUrl);
    if (url != null) x.add(url);
  }
  return x;
}

/** to transform url of a web page to url of slbookmark (it if exists) */
static public String link2slUri(String link, SLModel mod, String contextUrl) throws Exception {
	if ((link.startsWith("http://"))||(link.startsWith("https://"))) {
		if (link.startsWith(contextUrl)) {
			if (link.indexOf("?") > 0) { // TODO
				return null;
			} else {
				return link;
			}
		} else {
			// une url du web (hum : ou file // TODO)
			// System.out.println(link + " : " + mod.smarterGetDocument(link).getURI());
			SLDocument d = mod.bookmarkUrl2Doc(link);
			if (d != null) {
				return d.getURI();
			} else {
				// not a known bookmark
				
				// WE COULD TAKE THE link AS IS (a link to the outside world)
				// could be interesting (for instance to know whta do clink to a given page of the web
				// eg. if it is a github page)
				// BUT
				// pb if creation of doc from linked page (in ?docuri=...)
				// return link;
				return null;
			}
		}
	} else {
		return null;
	}
}

/**
Regex to extract links from markdown [xxx](yyy)

Here to test and get explanation about a regex:
https://www.regexpal.com/

catching things between parenthesis (yyy)
https://stackoverflow.com/questions/17779744/regular-expression-to-get-a-string-between-parentheses-in-javascript :
\(([^)]+)\) pour trouver (yyy)

    \( : match an opening parentheses
    ( : begin capturing group
    [^)]+: match one or more non ) characters
    ) : end capturing group
    \) : match closing parentheses


likewise:
\[([^\]]+)\]
to catch [xxx]

and this for [xxx](yyy)
\[([^\]]+)\]\(([^)]+)\)

// OUAIS SAUF QUE CA NE MARCHE PAS pour [[aaa] bla bla](http://xxx)

This is better :

\[([^()]*)\]\(([^()]*)\)

but not perfect: doesn't match [aaa (bbb) ccc](http://xxx) 

// TODO

/..../g to catch all occurrences
*/
public static String markdownLinkRegex() {
	// return "\\[([^\\]]+)\\]\\(([^)]+)\\)";
	return "\\[([^()]*)\\]\\(([^()]*)\\)";
}

/**
 * mdLink: can be:
 * - a long url
 * - doc:xxx
 * - /doc/xxx (in old stuff)
 * - /doc?docuri=xxx (in very old stuff ???)
 * - any thing ?
 * 
 * we handle here http:// https:// doc:xxx /doc/xxx (return null otherwise)
 * 
 * Similar thing in markdown-sl.js (much more complicated over there)
 */
static private String mdLink2Url(String mdLink, String contextUrl) {
	if (mdLink.startsWith("doc:")) {	
		return contextUrl + "/doc/" + mdLink.substring(4);
	} else if (mdLink.startsWith("/doc/")) {
		return contextUrl + "/doc/" + mdLink.substring(5);		
	} else if (mdLink.startsWith("http://")) {
		return mdLink;
	} else if (mdLink.startsWith("https://")) {
		return mdLink;
	} else {
		return null;
	}
}

// SLVocab.SL_RELATED_DOC_PROPERTY
/*
Match count: 1, Group Zero Text: '[Xie et al. (2016)](doc:2020/10/representation_learning_of_know)'
Capture Group Number: 1, Captured Text: 'Xie et al. (2016)'
Capture Group Number: 2, Captured Text: 'doc:2020/10/representation_learning_of_know'
Match count: 2, Group Zero Text: '[DKRL](tag:dkrl)'
Capture Group Number: 1, Captured Text: 'DKRL'
Capture Group Number: 2, Captured Text: 'tag:dkrl'
 */

}
