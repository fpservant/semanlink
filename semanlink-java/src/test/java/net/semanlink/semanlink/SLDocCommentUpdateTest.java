/* Created on Nov 27, 2020 */
package net.semanlink.semanlink;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class SLDocCommentUpdateTest {
private String contextUrl = "http://127.0.0.1:8080/semanlink";

@Test public final void mdShortUrl2UrlTest() {
	String mdLink = "doc:2020/10/representation_learning_of_know";
	String x = SLDocCommentUpdate.mdShortUrl2Url(mdLink, contextUrl);
	assertTrue("http://127.0.0.1:8080/semanlink/doc/2020/10/representation_learning_of_know".equals(x));
}

@Test public final void extractLinksTest() {
	String comment;
	List<String> links;
	comment = "BLA [[1204.xxxx] Xie et al.](doc:2020/10/representation_learning_of_know) BLA";
	links = SLDocCommentUpdate.extractLinks(comment, contextUrl);
	assertTrue(links.size() == 1);
	
	comment = "BLA [Xie et al. (2016)](doc:2020/10/representation_learning_of_know) BLA";
	links = SLDocCommentUpdate.extractLinks(comment, contextUrl);
	assertTrue(links.size() == 1);

	comment = "BLA [[1204.xxxx] Xie et al. (2016) yo](doc:2020/10/representation_learning_of_know) BLA";
	links = SLDocCommentUpdate.extractLinks(comment, contextUrl);
	assertTrue(links.size() == 1);
}

@Test public final void essaiRegexGroupNames() {
	String s = "TEST 123 azazaz 345";
	String regex = "(?<login>[a-zA-Z]+)\\s(?<id>\\d+)";
	// String regex = "(?<login>\\w+) (?<id>\\d+)";
	Pattern pat = Pattern.compile(regex);
	Matcher m = pat.matcher(s);
  while (m.find()) { 	
  	System.out.println("hello " + m.groupCount() + " login: " + m.group("login") + " id " + m.group("id"));
  	for (int i = 0 ; i <= m.groupCount() ; i++) {
  		System.out.println("\t " + i + " "  + m.group(i));
  	}
  }
	// System.out.println(m.group(1));
	// System.out.println(m.group("login"));

  
  // this catches [xxx]
  s = "bla [aaa] bla";
  // \[([^\]]+)\]
  // regex = "\\[([^\\]]+)\\]";
  // (?<link1>\[[^\]]+\])
  // regex = "(?<link1>\\[[^\\]]+\\])";
  // (?<link1>\[([^\]])+\])
  // (?<link1>\[[^\]]|\[zzz]+\])
  
  // \[([^\[]+)]\(([^)]+)\)

  regex = SLDocCommentUpdate.markdownLinkRegex();
  s = "bla [aaa xxx [zzz] zzz] bla";
  
  pat = Pattern.compile(regex);
  
  m = pat.matcher(s);
  while (m.find()) {  	
  	System.out.println("hello " + m.groupCount() + " link1 " + m.group("link1"));
  	for (int i = 0 ; i <= m.groupCount() ; i++) {
  		System.out.println("\t " + i + " "  + m.group(i));
  	}
  }
  
//(?<link1>\[[^\]|(?1)]+\])
}

//
//
//

//UN COMMENT avec des liens vers test files
/*
- [[2010.01057] LUKE: Deep Contextualized Entity Representations with Entity-aware Self-attention@en](doc:2020/11/2010_01057_luke_deep_context)
- [Un lien vers la page d'un doc](https://mgalkin.medium.com/knowledge-graphs-in-nlp-emnlp-2020-2f98ec527738#c2ae)
- [Lien vers un vieux doc](/doc/?uri=http%3A%2F%2Fwww.openculture.com%2F2012%2F08%2Fthe_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html)
- [Lien vers un vieux doc bis](doc:?uri=http%3A%2F%2Fwww.openculture.com%2F2012%2F08%2FBIS_the_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html)
- [Lien vers la page d'un vieux doc](https://gist.github.com/mommi84/07f7c044fa18aaaa7b5133230207d8d4)
*/

@Test public final void link2UriOfLinkedDocsTest_OldDoc() throws Exception {
	// doc/?uri=... including if we have more query params
	String link = "http://127.0.0.1:8080/semanlink/doc/?x=aa&uri=http%3A%2F%2Fwww.cosmovisions.com%2FChronoCroisades02.htm&pretty=bla";
	SLModel m = null;
	String contextUrl = "http://127.0.0.1:8080/semanlink";
	String s = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue("http://www.cosmovisions.com/ChronoCroisades02.htm".equals(s));
	
	// also with doc?uri=... (without the / in doc/?uri=)
	link = "http://127.0.0.1:8080/semanlink/doc?uri=http%3A%2F%2Fwww.cosmovisions.com%2FChronoCroisades02.htm&pretty=bla";
	s = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue("http://www.cosmovisions.com/ChronoCroisades02.htm".equals(s));
}

@Test public final void link2UriOfLinkedDocsTest_OldDocPage() throws Exception {
	SLModel m = DataLoader.testSLModel();
	String link = "https://gist.github.com/mommi84/07f7c044fa18aaaa7b5133230207d8d4";
	String contextUrl = "http://127.0.0.1:7080/semanlink";
	String s = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	System.out.println(s);
}


//
//
//

@Test public final void link2UriOfLinkedDocsTest() throws Exception {
	SLModel m = DataLoader.testSLModel();
	
	// OUAIS : le context qu'on passe dans plusieurs méthodes de SLModel,
	// il semble qu'il ne soit autre chose que SLModel.getModelUrl() !!!
	// TODO CHECK THAT, ça serait cool !
	
	String contextUrl = "http://127.0.0.1:7080/semanlink";
	System.out.println(m.getModelUrl() + "\n" + contextUrl);
	assertTrue(contextUrl.equals(m.getModelUrl()));
	
	// this doc has a comment with a variety of kind of links to docs
	String docUri = contextUrl + "/doc/2020/11/%C2%AB_diego_maradona_c%E2%80%99est_la_rais";
	SLDocument doc = 	m.getDocument(docUri);
	System.out.println(doc.getComment());
	linksInComment(doc.getComment(), m, contextUrl);
	
	String link, linkUri;
	
	// link to a post 2019 bookmark
	link = "http://127.0.0.1:7080/semanlink/doc/2020/11/2010_01057_luke_deep_context";
	linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue(linkUri.equals(link));
	
	// link to the page of a post 2019 bookmark
	link = "https://mgalkin.medium.com/knowledge-graphs-in-nlp-emnlp-2020-2f98ec527738#c2ae";
	linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue(linkUri.equals("http://127.0.0.1:7080/semanlink/doc/2020/11/knowledge_graphs_in_nlp_emnlp"));
	
	// link to an old doc
	link = "http://127.0.0.1:7080/semanlink/doc/?uri=http%3A%2F%2Fwww.openculture.com%2F2012%2F08%2Fthe_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html";
	linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue(linkUri.equals("http://www.openculture.com/2012/08/the_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html"));

	// link to an old doc - not the same as previous
	link = "http://127.0.0.1:7080/semanlink/doc?uri=http%3A%2F%2Fwww.openculture.com%2F2012%2F08%2FBIS_the_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html";
	linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue(linkUri.equals("http://www.openculture.com/2012/08/BIS_the_character_of_physical_law_richard_feynmans_legendary_lecture_series_at_cornell_1964.html"));

	// link to the page of an old doc
	link = "https://gist.github.com/mommi84/07f7c044fa18aaaa7b5133230207d8d4";
	linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
	assertTrue(linkUri.equals(link));
			
	// a link to a page that is NOT a doc
	docUri = "http://www.a.com";
	String s = SLDocCommentUpdate.link2UriOfLinkedDocs(docUri, m, contextUrl);
	assertTrue(s == null);

}

@Test public final void test_doc_comment() throws Exception {
	SLModel m = DataLoader.testSLModel();
	
	// OUAIS : le context qu'on passe dans plusieurs méthodes de SLModel,
	// il semble qu'il ne soit autre chose que SLModel.getModelUrl() !!!
	// TODO CHECK THAT, ça serait cool !
	
	String contextUrl = "http://127.0.0.1:7080/semanlink";
	System.out.println(m.getModelUrl() + "\n" + contextUrl);
	assertTrue(contextUrl.equals(m.getModelUrl()));
	
	// this doc has a comment with a variety of kind of links to docs
	String docUri = contextUrl + "/doc/2020/11/%C2%AB_diego_maradona_c%E2%80%99est_la_rais";
	SLDocument doc = 	m.getDocument(docUri);
	System.out.println(doc.getComment());
	linksInComment(doc.getComment(), m, contextUrl);
}

void linksInComment(String comment, SLModel m, String contextUrl) {
	List<String> links = SLDocCommentUpdate.extractLinks(comment, contextUrl);
	
	for (String link : links) {
		try {
			String linkUri = SLDocCommentUpdate.link2UriOfLinkedDocs(link, m, contextUrl);
			// if (linkUri == null) continue;
			System.out.println(link + " \n-> " + linkUri);
		} catch (Exception e) {
			System.err.println("Bad link: "+ link);
		}	
	}
}


}
