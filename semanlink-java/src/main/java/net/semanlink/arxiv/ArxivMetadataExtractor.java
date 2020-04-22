/* Created on Mar 31, 2020 */
package net.semanlink.arxiv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.vocabulary.RDF;

import net.semanlink.metadataextraction.Extractor;
import net.semanlink.metadataextraction.ExtractorData;
import net.semanlink.semanlink.SLDocUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLSchema;

public class ArxivMetadataExtractor extends Extractor {
	
final static String ARXIV_PROP_DEB = SLSchema.NS + "arxiv_";
/** Retourne true ssi cet extractor traite le document passé en argument. */
@Override public boolean dealWith(ExtractorData data) {
	return (Arxiv.sldoc2arxivNum(data.getSLDocument(), data.getSLModel()) != null);	
}

/** creating it if necessary */
private SLKeyword arxivDocTag(SLModel mod) throws Exception {
	String label = "Arxiv Doc";
	String uri = mod.kwLabel2UriQuick(label, mod.getDefaultThesaurus().getURI(), Locale.ENGLISH);	
	return mod.getKeywordCreatingItIfNecessary(uri, label, Locale.ENGLISH);
}

// mal branlé cette histoire de ExtractorData - ici, me conduit à calculer sldoc2arxivNum() une 2eme fois // TODO
@Override public boolean doIt(ExtractorData data) throws Exception {
	SLModel mod = data.getSLModel();
	SLDocument sldoc = data.getSLDocument();
	String num = Arxiv.sldoc2arxivNum(data.getSLDocument(), data.getSLModel());
	
	// is it worth keeping these 2 things and reuse them ?
	Client client = ClientBuilder.newClient();
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	ArxivEntry arxiv = ArxivEntry.newArxivEntry(num, client, factory);
	
	try (SLDocUpdate du = mod.newSLDocUpdate(sldoc)) {
		// du.addDocProperty(RDF.type.getURI(), SLSchema.NS + "ArxivDoc");
		SLKeyword arxivDocTag = arxivDocTag(mod);
		du.addKeyword(arxivDocTag);
		
		du.setDocProperty(ARXIV_PROP_DEB + "num", num, "");
		du.setDocProperty(ARXIV_PROP_DEB + "title", arxiv.getTitle(), "en");
		du.setDocProperty(ARXIV_PROP_DEB + "summary", arxiv.getSummary(), "en");
		du.setDocProperty(ARXIV_PROP_DEB + "published", arxiv.getPublished(), "");
		du.setDocProperty(ARXIV_PROP_DEB + "updated", arxiv.getUpdated(), "");
		List<String> authors = arxiv.getAuthors();
		String firstAuthor = authors.get(0);
		du.setDocProperty(ARXIV_PROP_DEB + "firstAuthor", firstAuthor, "");
		boolean first = true;
		for (String author : authors) {
			if (first) {
				first = false;
				du.setDocProperty(ARXIV_PROP_DEB + "author", author, "");		
			} else {
				du.addDocProperty(ARXIV_PROP_DEB + "author", author, "");		
			}	
		}
		
		// Title
		
//		// don't overwrite title if it has been set
//		if (sldoc.getPropertyAsStrings(SLModel.TITLE_PROPERTY) == null) {
//			du.setDocProperty(SLModel.TITLE_PROPERTY, arxiv.getTitle(), "en");
//		}
		String year = arxiv.getPublished().substring(0, 4);
		// String title = "[" + lastWord(firstAuthor) + year + "] " + arxiv.getTitle() + " (Arxiv:" + num + ")";
		// String title = arxiv.getTitle() + " [" + firstAuthor + " " + year + "] "+ "(Arxiv:" + num + ")";
		String title =  "[" + num + "] " + arxiv.getTitle();
		// String title = "[" + lastWord(firstAuthor) + year + "] " + arxiv.getTitle() + "<br/>(Arxiv:" + num + ")";
		// String title = arxiv.getTitle() + " [" + firstAuthor + " " + year + " - " + num + "]";
		// String title =  "[" + num + "] " + arxiv.getTitle() + " (" + firstAuthor + " " + year + ")";
		// String title =  arxiv.getTitle() + " (" + year + ")";
		du.setDocProperty(SLModel.TITLE_PROPERTY, title, "en");
		
		
		return true;
	}
}

private String lastWord(String firstAuthor) {
	int k = firstAuthor.lastIndexOf(" ");
	if (k > 0) return firstAuthor.substring(k+1);
	return firstAuthor;
}

//
// POUR L'EXTRACTION DE KEYWORDS
//

@Override public String getText4kwExtraction(ExtractorData data) throws IOException, URISyntaxException {
	SLDocument doc = data.getSLDocument();
	StringBuilder sb = new StringBuilder();
	// no, use the real arxiv title (avoids having "arxiv" systematically added to kws)
//	String title = SLUtils.getLabel(doc);
//	sb.append(title);
	List vals = doc.getPropertyAsStrings(ARXIV_PROP_DEB + "title");
	if ((vals != null) & (vals.size() > 0)) {
		String arxiv_title = (String) vals.get(0);
		sb.append(arxiv_title);
	}
	String comment = doc.getComment();
	if (comment != null) {
		sb.append("\n" + comment);
	}
	// too many tags with this
//	vals = doc.getPropertyAsStrings(ARXIV_PROP_DEB + "summary");
//	if ((vals != null) & (vals.size() > 0)) {
//		String arxiv_summary = (String) vals.get(0);
//		if (!(arxiv_summary.equals(comment))) {
//			sb.append("\n" + arxiv_summary);
//		}
//	}
	return sb.toString();
}

@Override public Locale getLocale(ExtractorData data) {
	return Locale.ENGLISH;
}

}
