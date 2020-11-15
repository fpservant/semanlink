package net.semanlink.servlet;
import net.semanlink.semanlink.SLDocUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.Util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class Action_SetComment extends Action_SetOrAddProperty {
protected String getPropUri(HttpServletRequest request) {
	return SLVocab.COMMENT_PROPERTY;
}
protected String getPropValue(HttpServletRequest request) {
	return request.getParameter("comment");
}


// propertyUri is the comment prop
@Override protected void setDocProperty(HttpServletRequest request, SLModel mod, SLDocument doc, String propertyUri, String propertyValue, String lang) { // 2020-11
	String contextUrl;
	try {
		contextUrl = Util.getContextURL(request);
	} catch (MalformedURLException e) { throw new RuntimeException(e); }
		
	// 
	String oldComment = doc.getComment();
	String newComment = propertyValue;
	
	List<String> oldLinks = extractLinks(oldComment, contextUrl);
	List<String> newLinks = extractLinks(newComment, contextUrl);
	
	try {
		for (String link : newLinks) {
			if ((link.startsWith("http://"))||(link.startsWith("https://"))) {
				if (link.startsWith(contextUrl)) {
					
				} else {
					// une url du web (hum : ou file // TODO)
					// System.out.println(link + " : " + mod.smarterGetDocument(link).getURI());
					SLDocument d = mod.bookmarkUrl2Doc(link);
					if (d != null) {
						System.out.println(link + " : " + d.getURI());
					} else {
						System.out.println(link + " : not a known bookmark");					
					}
				}
			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
	
	
	List<SLDocument> oldRelatedDocs = doc.relatedDocs();
	
	List<String> toBeAdded = new ArrayList<>();
	for (String link : newLinks) {
		try {
			link = link2slUri(link, mod, contextUrl);
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
	
	
	
	
	
	try (SLDocUpdate up = mod.newSLDocUpdate(doc)) {
		// mod.setDocProperty(doc, propertyUri, propertyValue, lang);
		up.setDocProperty(propertyUri, newComment, lang);
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

private boolean isIn(String link, List<SLDocument> docs) {
	for (SLDocument doc : docs) {
		if (link.equals(doc.getURI())) return true;
	}
	return false;
}

List<String> extractLinks(String comment, String contextUrl) {
	List<String> x = new ArrayList<>();
	if (comment == null) return x;
	String regex = markdownLinkRegex();
	Pattern pat = Pattern.compile(regex);
	Matcher m = pat.matcher(comment);
  while (m.find()) {
    // Capturing groups are indexed from left to right, starting at one. 
    // Group zero denotes the entire pattern, 
    // so the expression m.group(0) is equivalent to m.group()
//    System.out.printf("Match count: %s, Group Zero Text: '%s'%n", matchCount, m.group());
//    for (int i = 1; i <= m.groupCount(); i++) {
//        System.out.printf("Capture Group Number: %s, Captured Text: '%s'%n", i, m.group(i));
//    }
    // String mdLinkText = m.group(1);
    String mdLink = m.group(2);
    String url = mdLink2Url(mdLink, contextUrl);
    if (url != null) x.add(url);
  }
  return x;
}

/** to transform url of a web page to url of slbookmark */
private String link2slUri(String link, SLModel mod, String contextUrl) throws Exception {
	if ((link.startsWith("http://"))||(link.startsWith("https://"))) {
		if (link.startsWith(contextUrl)) {
			return link;
		} else {
			// une url du web (hum : ou file // TODO)
			// System.out.println(link + " : " + mod.smarterGetDocument(link).getURI());
			SLDocument d = mod.bookmarkUrl2Doc(link);
			if (d != null) {
				return d.getURI();
			} else {
				// not a known bookmark");
				return link;
			}
		}
	}
	return link;
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

/..../g to catch all occurrences
*/
public static String markdownLinkRegex() {
	return "\\[([^\\]]+)\\]\\(([^)]+)\\)";
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
 * Basically the same thing in markdown-sl.js (much more complicated over there)
 */
private String mdLink2Url(String mdLink, String contextUrl) {
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
