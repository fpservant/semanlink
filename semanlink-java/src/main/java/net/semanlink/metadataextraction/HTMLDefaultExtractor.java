package net.semanlink.metadataextraction;
/** Parse un fichier HTML et retourne une instance de classe adaptée à l'extraction de ses metadata. 
 * 
 *  Cette classe est une factory de HTMLMetadataExtractor (call getHTMLMetadataExtractor)
 */
public class HTMLDefaultExtractor extends Extractor {
//
// AUTRES PUBLICATIONS
//
public String getSource(ExtractorData data) {
	if (isLiberation(data)) return "Libération";
	if (isNationalGeographic(data)) return "National Geographic";
	if (isScientificAmerican(data)) return "Scientific American";
	return null;
}

public static boolean isNationalGeographic(ExtractorData data) {
	try {
		return (data.getText().lastIndexOf("National Geographic Society. All rights reserved.") > -1);
	} catch (Exception e) { return false; }
}

public static boolean isScientificAmerican(ExtractorData data) {
	try {
		return (data.getText().lastIndexOf("Scientific American, Inc. All rights reserved.") > -1);
	} catch (Exception e) { return false; }
}

public static boolean isLiberation(ExtractorData data) {
	if (data.getUri().indexOf("/www.liberation.fr/") > -1) return true;
	try {
		String text = data.getText();// data.getText()
		return (text.lastIndexOf("©Libération") > -1);
	} catch (Exception e) { return false; }
}
	

} // class HTMLAnalysis
