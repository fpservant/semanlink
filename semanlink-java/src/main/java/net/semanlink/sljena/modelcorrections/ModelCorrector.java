package net.semanlink.sljena.modelcorrections;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import net.semanlink.sljena.JFileModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;



// LA CORRECTION EST FAITE AU CHARGEMENT :
// VOIR SemanlinkConfig.loadThesaurus et
// JModel.readDocsModelFromFile

/**
 * Classe permettant de corriger un modele en intervenant au moment ou on charge un fichier. 
 * 
 * This class allows to apply a set of corrections to a file (there is one method for files of tags and one
 * for files of documents)
 * 
 * It does not update the SLModel if it is already loaded.
 * 
 * Utilisation : mettre ds un ModelCorrector les corrections (Correction) à apporter
 * via des appels à add, puis charger le SLModel (comme ca se passe au lancement de l'appli).
 */
public class ModelCorrector {
/** ArrayList de Correction */
private ArrayList<Correction> corrections;
public ModelCorrector() {
	this.corrections = new ArrayList<Correction>(10);
}
public void add(Correction correction) { this.corrections.add(correction); }

/** return true iff something changed */
public boolean correctDocsModel(String slFile, String base) throws JenaException, IOException, URISyntaxException {
	return correctModel(slFile, base, DOC_TYPE);
}

/** return true iff something changed */
public boolean correctKwsModel(String slFile, String base) throws JenaException, IOException, URISyntaxException {
	return correctModel(slFile, base, KW_TYPE);
}

private static int DOC_TYPE = 0;
private static int KW_TYPE = 1;

private boolean correctModel(String slFile, String base, int modelType) throws JenaException, IOException, URISyntaxException {
	File file = new File(slFile);

	long lastModified = file.lastModified();
	JFileModel jFileModel = null;
	boolean hasChanged = false;
	for (Correction correction : corrections) {
		if (lastModified < correction.getTime()) {
			if (jFileModel == null) jFileModel = new JFileModel(slFile, base);
			Model mod = jFileModel.getModel();
			if (modelType == DOC_TYPE) {
				hasChanged = correction.correctDocsModel(mod) || hasChanged;
			} else if (modelType == KW_TYPE) {
				hasChanged = correction.correctKwsModel(mod) || hasChanged;
			} else {
				throw new IllegalArgumentException("unsupported modelType");
			}
		}
	}
	if (hasChanged) jFileModel.save();
	return hasChanged;
}


}
