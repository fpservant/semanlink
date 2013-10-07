package net.semanlink.metadataextraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.servlet.SLServlet;

/**
 * Main class to deal with metadata extraction.
 * 
 * This class maintains a list of Extractors - classes specialized in extracting metadata
 * from one kind of document. When doIt(SLDocument slDoc, SLModel mod) is called to 
 * add the corresponding statements to the model, we loop through the extractors until the dealWith
 * method of one returns true. The doIt() method of this extractor is then called.
 * If no one is able to deal with this doc, HTMLDefaultExtractor's doIt() method id called.
 * 
 * Also deals with keywords. Override 
 * 
 * How to use :
 * First, add Extractor to the list of Extractors (typically done at startup) -- some
 * are added here at creation (extractors used by fps : Le Monde, ...)
 *
 * Call doIt(SLDocument slDoc, SLModel mod) to add the statements to the model
 * (Note: doIt does explicitly save the model, depends on slModel.addProperty)
 */
public class MetadataExtractorManager {
private ArrayList extractors;
private Extractor defaultExtractor = new HTMLDefaultExtractor();
// private SimpleHttpClient client;
/** kws à ne pas ajouter automatiquement */
public static String[] DONT_ADD_THESE_KWS;
public MetadataExtractorManager() {
	// this.client = client;
	this.extractors = new ArrayList();
	this.extractors.add(new LeMonde());
	this.extractors.add(new BBC());
	// this.extractors.add(new HTMLDefaultExtractor()); // bob : ceci est fait par défaut si tout a raté
}
public void add(Extractor extractor) {
	this.extractors.add(extractor);
}
public static void setMetadataExtractionBlackList(String[] metadataExtractionBlackList) {DONT_ADD_THESE_KWS = metadataExtractionBlackList;}

/** retourne si modelHasChanged. 
 * @throws Exception*/
public boolean doIt(SLDocument slDoc, SLModel mod) throws Exception {
	ExtractorData extractorData = new ExtractorData(slDoc, mod, SLServlet.getSimpleHttpClient());
	boolean done = false;
	boolean modelHasChanged = false;
	Extractor extractor = null;
	for (int i = 0; i < this.extractors.size(); i++) {
		extractor = (Extractor) this.extractors.get(i);
		if (extractor.dealWith(extractorData)) {
			modelHasChanged = (extractor.doIt(extractorData) || modelHasChanged);
			// on s'arrête au premier extracteur pertinent
			done = true;
			break;
		}
	}
	if (!done) {
		extractor =  defaultExtractor;
		if (isHTML(slDoc)) {
			modelHasChanged = extractor.doIt(extractorData);
			done = true;
		}
	}
	
	modelHasChanged = (extractKWs(extractor.getText4kwExtraction(extractorData), extractor.getLocale(extractorData), slDoc, mod) || modelHasChanged);
	return modelHasChanged;
}

public static boolean isHTML(SLDocument slDoc) {
	String uri = slDoc.getURI();
	return ( uri.endsWith(".htm") || uri.endsWith(".html") );
}

/** Return true si a effectivement trouvé des kws. */
public static boolean extractKWs(String text, Locale locale, SLDocument doc, SLModel mod) throws Exception {
	boolean x = false;
	if (text != null) {

		String thesaurusUri = mod.getThesaurus(doc).getURI();
		Collection<SLKeyword> kws = mod.getKeywordsInText(text, locale, thesaurusUri);
		for (SLKeyword kw : kws) {
			String kwUri = kw.getURI();
			// pour ne pas ajouter automatiquement "fps" et autres blacklisted elements
			boolean doit = true;
			if (DONT_ADD_THESE_KWS != null) {
				for (int j = 0; j < DONT_ADD_THESE_KWS.length; j++) { // #thing
					if (kwUri.endsWith("/" + DONT_ADD_THESE_KWS[j])) {
						doit = false;
						break;
					}
					if (kwUri.endsWith("#" + DONT_ADD_THESE_KWS[j])) {
						doit = false;
						break;
					}
				}
			}
			if (doit) {
				mod.addKeyword(doc, kw);
				x = true;
			}
		}
	}
	return x;
}


}

