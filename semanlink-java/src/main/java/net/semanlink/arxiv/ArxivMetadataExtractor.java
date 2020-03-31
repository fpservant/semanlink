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
import net.semanlink.semanlink.SLDocumentStuff;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLSchema;
import net.semanlink.semanlink.SLUtils;

public class ArxivMetadataExtractor extends Extractor {
	
final static String ARXIV_PROP_DEB = SLSchema.NS + "arxiv_";
/** Retourne true ssi cet extractor traite le document passé en argument. */
public boolean dealWith(ExtractorData data) {
	return (sldoc2arxivNum(data) != null);	
}

static private String sldoc2arxivNum(ExtractorData data) {
	SLDocumentStuff docstuff = new SLDocumentStuff(data.getSLDocument(), data.getSLModel(), null); // pour ce qu'on en a à faire ici, pas besoin du contexte
	try {
		return Arxiv.url2num(docstuff.getHref());
	} catch (Exception e) { throw new RuntimeException(e); }	
}

// mal branlé cette histoire de ExtractorData - ici, me conduit à calculer sldoc2arxivNum() une 2eme fois // TODO
@Override public boolean doIt(ExtractorData data) throws Exception {
	SLModel mod = data.getSLModel();
	SLDocument sldoc = data.getSLDocument();
	String num = sldoc2arxivNum(data);
	
	// is it worth keeping these 2 things and reuse them ?
	Client client = ClientBuilder.newClient();
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	ArxivEntry arxiv = ArxivEntry.newArxivEntry(num, client, factory);
	
	try (SLDocUpdate du = mod.newSLDocUpdate(sldoc)) {
		du.addDocProperty(RDF.type.getURI(), SLSchema.NS + "ArxivDoc");
		// don't overwrite title if it has been set
		if (sldoc.getPropertyAsStrings(SLModel.TITLE_PROPERTY) == null) {
			du.setDocProperty(SLModel.TITLE_PROPERTY, arxiv.getTitle(), "en");
		}
		du.setDocProperty(ARXIV_PROP_DEB + "num", num, "");
		du.setDocProperty(ARXIV_PROP_DEB + "title", arxiv.getTitle(), "en");
		du.setDocProperty(ARXIV_PROP_DEB + "summary", arxiv.getSummary(), "en");
		du.setDocProperty(ARXIV_PROP_DEB + "published", arxiv.getPublished(), "");
		du.setDocProperty(ARXIV_PROP_DEB + "updated", arxiv.getUpdated(), "");
		List<String> authors = arxiv.getAuthors();
		du.setDocProperty(ARXIV_PROP_DEB + "firstAuthor", authors.get(0), "");
		boolean first = true;
		for (String author : authors) {
			if (first) {
				first = false;
				du.setDocProperty(ARXIV_PROP_DEB + "author", author, "");		
			} else {
				du.addDocProperty(ARXIV_PROP_DEB + "author", author, "");		
			}	
		}
		return true;
	}
	
}

//
// POUR L'EXTRACTION DE KEYWORDS
//

@Override public String getText4kwExtraction(ExtractorData data) throws IOException, URISyntaxException {
	SLDocument doc = data.getSLDocument();
	String title = SLUtils.getLabel(doc);
	StringBuilder sb = new StringBuilder();
	sb.append(title);
	List vals = doc.getPropertyAsStrings(ARXIV_PROP_DEB + "title");
	if ((vals != null) & (vals.size() > 0)) {
		String arxiv_title = (String) vals.get(0);
		if (!(arxiv_title.equals(title))) {
			sb.append("\n" + arxiv_title);
		}
	}
	String comment = doc.getComment();
	if (comment != null) {
		sb.append("\n" + comment);
	}
	// too any tags with this
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
