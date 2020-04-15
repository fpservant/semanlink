/* Created on Mar 30, 2020 */
package net.semanlink.arxiv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ArxivEntry {
private String arxivNum;
private String id, updated, published, title, summary;
private ArrayList<String> authors;

static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
  return factory.newDocumentBuilder();  
}

/**
 * 
 * @param client ClientBuilder.newClient();
 * @param factory DocumentBuilderFactory.newInstance();  
 * @param arxivNum eg. 0807.4145
 * @return
 * @throws SAXException
 * @throws IOException
 * @throws ParserConfigurationException 
 */
static ArxivEntry newArxivEntry(String arxivNum, Client client, DocumentBuilderFactory factory) throws SAXException, IOException, ParserConfigurationException {
	String query = "http://export.arxiv.org/api/query?id_list=" + arxivNum;
	WebTarget webTarget = client.target(query);
	// Response res = webTarget.request(MediaType.WILDCARD_TYPE).get();
	InputStream in = webTarget.request(MediaType.WILDCARD_TYPE).get(InputStream.class);
	
	DocumentBuilder docBuilder = factory.newDocumentBuilder(); // could it be reused for several docs?
	Document doc = docBuilder.parse(in);
	
	NodeList entries = doc.getElementsByTagName("entry");
	if (entries == null) throw new RuntimeException("No entry");
	if (entries.getLength() == 0) throw new RuntimeException("No entry");
	if (entries.getLength() > 1) throw new RuntimeException("Unexpected: several entries");
			
	Element entry = (Element) entries.item(0);
	return new ArxivEntry(arxivNum, entry);
}

ArxivEntry(String arxivNum, Element entry) {
	this.arxivNum = arxivNum;
	this.id = getArxivMetadataValue(entry, "id");
	this.updated = getArxivMetadataValue(entry, "updated");
	this.published = getArxivMetadataValue(entry, "published");
	this.title = getArxivMetadataValue(entry, "title");
	this.summary = getArxivMetadataValue(entry, "summary", false);
	this.authors = getArxivMetadataValues(entry, "author");
}

/** for a metadata supposed to have only one text value */
static private String getArxivMetadataValue(Element entry, String prop) {
	return getArxivMetadataValue(entry, prop, true);
}

static private String getArxivMetadataValue(Element entry, String prop, boolean preferedAsOneLineOnlyContent) {
	NodeList nl = entry.getElementsByTagName(prop);
	if (nl == null) return null;
	Node node = nl.item(0);
	if (node == null) return null;
	String text = node.getTextContent();
	if (text == null) return null;
	return cleanTextContent(text, preferedAsOneLineOnlyContent);
}

static private ArrayList<String> getArxivMetadataValues(Element entry, String prop) {
	NodeList nl = entry.getElementsByTagName(prop);
	if (nl == null) return null;
	int n = nl.getLength();
	ArrayList<String> x = new ArrayList<>(n);
	for (int i = 0 ; i < n ; i++) {
		Node node = nl.item(i);
		if (node == null) continue;
		String text = node.getTextContent();
		if (text == null) continue;
		x.add(cleanTextContent(text));			
	}
	return x;
}

/**
 * The text content from the xml may contain extra spaces, linebreaks, etc.
 * Make it look good (and correct as markdown text: an empty line for line break.)
 * 
 * @param replaceLineBreakBySpace in case of "false new line" in text (new line that would not
 * be shown as new line in HTML), should we replace that \n by a space (yes in the case of the title)
 */
static String cleanTextContent(String text) {
	return cleanTextContent(text, true);
}

static String cleanTextContent(String text, boolean replaceLineBreakBySpace) {
	BufferedReader reader = new BufferedReader(new StringReader(text));
	StringBuilder sb = new StringBuilder();
	String line;
	// true once we have skipped all empty lines that may occur at the beginning
	// (used to skip empty lines at the beginning)
	boolean foundFirstRealLine = false;
	// true when last line is not finished, that is, not followed by a true new line
	boolean lastLineNotFinished = false;
	// need to add a true line break (that is, \n\n)
	boolean lineBreakAdded = true;
	
	try {
		while((line = reader.readLine()) != null) { // java sucks
			line = cleanSpaces(line);
			// first remove empty lines at the beginning
			if (!foundFirstRealLine) {
				if ("".equals(line)) {
					continue;
				} else {
					foundFirstRealLine = true;
				}
			}
			
			if ("".equals(line)) {
				if (!lineBreakAdded) {
					// sb.append("\n\n");
					lastLineNotFinished = false;
					// lineBreakAdded = true;
				}
			} else {
				if (lastLineNotFinished) {
					if (replaceLineBreakBySpace) {
						sb.append(" ");
					} else {
						sb.append("\n");
					}
				} else {
					if (!lineBreakAdded) {
						sb.append("\n\n");
						lineBreakAdded = true;
					}
				}
				sb.append(line);
				lastLineNotFinished = true;
				lineBreakAdded = false;
			}
		}
	} catch (IOException e) { throw new RuntimeException(e); }
	return sb.toString();
}

//HUM, aurait surement pu faire replaceAll("\\h+", " ")
/** trim, and replace all successions of white chars (including tabs, non breaking ones) by one white space */
public static String cleanSpaces(String s) {
	// return s.replaceAll("(\\h*)", " "); // +/g
	String x = space2space(s);
	return x.trim().replaceAll(" +", " ");
}
/**
 * trim that trims also the non breaking spaces
 * https://stackoverflow.com/questions/28295504/how-to-trim-no-break-space-in-java
 */
public static String betterTrim(String s) {
	s = s.replaceAll("(^\\h*)|(\\h*$)","");
	return s;
}

/**
 * remplace les faux blancs (blancs insécables, tabs, etc) en whitespace
 */
public static String space2space(String s) {
	return s.replaceAll("\\h", " ");
}

//
//
//

public String getArxivNum() {
	return arxivNum;
}

public String getId() {
	return id;
}

public String getUpdated() {
	return updated;
}

public String getPublished() {
	return published;
}

public String getTitle() {
	return title;
}

public String getSummary() {
	return summary;
}

public ArrayList<String> getAuthors() {
	return authors;
}
}


/*
<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <link href="http://arxiv.org/api/query?search_query%3D%26id_list%3D2003.02320%26start%3D0%26max_results%3D10" rel="self" type="application/atom+xml"/>
  <title type="html">ArXiv Query: search_query=&amp;id_list=2003.02320&amp;start=0&amp;max_results=10</title>
  <id>http://arxiv.org/api/ys/RxwZY/EyUF3cT0w5CYLEP60k</id>
  <updated>2020-03-22T00:00:00-04:00</updated>
  <opensearch:totalResults xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">1</opensearch:totalResults>
  <opensearch:startIndex xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">0</opensearch:startIndex>
  <opensearch:itemsPerPage xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">10</opensearch:itemsPerPage>
  <entry>
    <id>http://arxiv.org/abs/2003.02320v1</id>
    <updated>2020-03-04T20:20:32Z</updated>
    <published>2020-03-04T20:20:32Z</published>
    <title>Knowledge Graphs</title>
    <summary>  In this paper we provide a comprehensive introduction to knowledge graphs,
which have recently garnered significant attention from both industry and
academia in scenarios that require exploiting diverse, dynamic, large-scale
collections of data. After a general introduction, we motivate and contrast
various graph-based data models and query languages that are used for knowledge
graphs. We discuss the roles of schema, identity, and context in knowledge
graphs. We explain how knowledge can be represented and extracted using a
combination of deductive and inductive techniques. We summarise methods for the
creation, enrichment, quality assessment, refinement, and publication of
knowledge graphs. We provide an overview of prominent open knowledge graphs and
enterprise knowledge graphs, their applications, and how they use the
aforementioned techniques. We concludnon, suremente with high-level future research
directions for knowledge graphs.
</summary>
    <author>
      <name>Aidan Hogan</name>
    </author>
    <author>
      <name>Eva Blomqvist</name>
    </author>
    <author>
      <name>Michael Cochez</name>
    </author>
    <author>
      <name>Claudia d'Amato</name>
    </author>
    <author>
      <name>Gerard de Melo</name>
    </author>
    <author>
      <name>Claudio Gutierrez</name>
    </author>
    <author>
      <name>José Emilio Labra Gayo</name>
    </author>
    <author>
      <name>Sabrina Kirrane</name>
    </author>
    <author>
      <name>Sebastian Neumaier</name>
    </author>
    <author>
      <name>Axel Polleres</name>
    </author>
    <author>
      <name>Roberto Navigli</name>
    </author>
    <author>
      <name>Axel-Cyrille Ngonga Ngomo</name>
    </author>
    <author>
      <name>Sabbir M. Rashid</name>
    </author>
    <author>
      <name>Anisa Rula</name>
    </author>
    <author>
      <name>Lukas Schmelzeisen</name>
    </author>
    <author>
      <name>Juan Sequeda</name>
    </author>
    <author>
      <name>Steffen Staab</name>
    </author>
    <author>
      <name>Antoine Zimmermann</name>
    </author>
    <arxiv:comment xmlns:arxiv="http://arxiv.org/schemas/atom">130 pages</arxiv:comment>
    <link href="http://arxiv.org/abs/2003.02320v1" rel="alternate" type="text/html"/>
    <link title="pdf" href="http://arxiv.org/pdf/2003.02320v1" rel="related" type="application/pdf"/>
    <arxiv:primary_category xmlns:arxiv="http://arxiv.org/schemas/atom" term="cs.AI" scheme="http://arxiv.org/schemas/atom"/>
    <category term="cs.AI" scheme="http://arxiv.org/schemas/atom"/>
    <category term="cs.DB" scheme="http://arxiv.org/schemas/atom"/>
    <category term="cs.LG" scheme="http://arxiv.org/schemas/atom"/>
  </entry>
</feed>
*/
