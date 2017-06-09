package net.semanlink.sljena.modelcorrections;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.sljena.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/** Classe permettant de changer l'uri d'un keyword. 
 *  Une méthode statique pour recalculer toutes les uris à partir des labels MAIS ATTENTION, A APPELER DS SLServlet chercher correctOldKwUris */
public class KeywordUriCorrection extends AbstractCorrection {
String oldUri, newUri;
public KeywordUriCorrection(String oldUri, String newUri) {
	this.oldUri = oldUri;
	this.newUri = newUri;
}
public KeywordUriCorrection(String oldUri, String newUri, long time) {
	this(oldUri,newUri);
	this.time = time;
}

public boolean correctDocsModel(Model mod) {
	return changeKeywordURI(mod);
}

public boolean correctKwsModel(Model mod) {
	return changeKeywordURI(mod);
}

boolean changeKeywordURI(Model model) {
	Resource oldRes, newRes;
	oldRes = model.createResource(oldUri);
	newRes = model.createResource(newUri);
	boolean x = JenaUtils.changeSubjects(model, oldRes, newRes);
	x = JenaUtils.changeObjects(model, oldRes, newRes) || x;
	return x;
}

//
// POUR CHANGER EN BLOC TOUTES LES URIS DE KW (RECALCUL A PARTIR DE LEURS LABELS
//

/** ajoute toutes les corrections nécessaires à corrector pour remettre d'amplomb toutes les uri de kw à partir de leur labels. 
 * ATTENTION, A APPELER DS SLServlet chercher correctOldKwUris (le model doit avoir déjà été chargé
 * ??? A VOIR */
static public void recomputeAllKwUrisFromLabel(ModelCorrector corrector, SLModel slModel) { //
	List<SLKeyword> kwList = slModel.getKWsInConceptsSpaceArrayList();
	for (SLKeyword kw : kwList) {
		KeywordUriCorrection correction = kwUriCorrectionFromLabel(slModel, kw);
		if (correction != null) corrector.add(correction);
	}
	// restent les alias
	List<String> aliases = new ArrayList<String>();
	slModel.aliasesIntoCollectionOfUris(aliases);
	for (String aliasUri : aliases) {
		SLKeyword kw = slModel.getKeyword(aliasUri);
		KeywordUriCorrection correction = kwUriCorrectionFromLabel(slModel, kw);
		if (correction != null) corrector.add(correction);
	}
}

/** compute Correction corresponding to computing kw uri from its label.
 *  Returns null if no change
 */
static private KeywordUriCorrection kwUriCorrectionFromLabel(SLModel slModel, SLKeyword kw) {
	Locale locale = Locale.getDefault();
	String lib = kw.getLabel();
	String newShortUri = slModel.kwLabel2ShortUri(lib,locale); // pour bien faire, faudrait prendre la langue du label, s'il y en a une
	String oldUri = kw.getURI();
	// int k = oldUri.indexOf("#");
	int k = oldUri.lastIndexOf("/");
	if (k < 0) {
		System.err.println("uri zarbi : " + oldUri);
		return null;
	}
	String oldShortUri = oldUri.substring(k+1);
	if (!oldShortUri.equals(newShortUri)) {
		// String newUri = oldUri.substring(0,k) + "#" + newShortUri;
		String newUri = oldUri.substring(0,k) + "/" + newShortUri;
		System.out.println(oldUri + " : " + newUri);
		return new KeywordUriCorrection(oldUri, newUri);
	}
	return null;
}

} // class

