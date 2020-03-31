package net.semanlink.sljena;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
/**
 * Un Model dont une partie des statements se retrouve dans un JFileModel.
 * @author fps
 */
class JFileBiModel {
private JFileModel jFileModel;
private Model small; // n'est autre que jFileModel.getModel() -- on garde les 2 par commodite
private Model big;

//
// CONSTRUCTION
//

JFileBiModel(Model big, JFileModel jFileModel) {
	this.big = big;
	this.small = jFileModel.getModel();
	this.jFileModel = jFileModel;
}

//
// MODIFICATIONS DU MODEL
//

/** ajoute un statement dont l'objet est un literal */
void add(String docUri, String propertyUri, String propertyValue, String lang) {
	JenaUtils.add(this.big, docUri, propertyUri, propertyValue, lang);
	JenaUtils.add(this.small, docUri, propertyUri, propertyValue, lang);
}

void add(String docUri, String propertyUri, String objectUri) {
	JenaUtils.add(this.big, docUri, propertyUri, objectUri);
	JenaUtils.add(this.small, docUri, propertyUri, objectUri);
}

void set(String docUri, String propertyUri, String objectUri) {
	if ((objectUri == null) || (objectUri.equals(""))) {
		JenaUtils.removeProperty(this.big, docUri, propertyUri);
		JenaUtils.removeProperty(this.small, docUri, propertyUri);
	} else {
		JenaUtils.setPropertyValue(this.big, docUri, propertyUri, objectUri);
		JenaUtils.setPropertyValue(this.small, docUri, propertyUri, objectUri);
	}
}

void set(String docUri, String propertyUri, String propertyValue, String lang) {
	if ((propertyValue == null) || (propertyValue.equals(""))) {
		JenaUtils.removeProperty(this.big, docUri, propertyUri);
		JenaUtils.removeProperty(this.small, docUri, propertyUri);
	} else {
		JenaUtils.setPropertyValue(this.big, docUri, propertyUri, this.big.createLiteral(propertyValue, lang));
		JenaUtils.setPropertyValue(this.small, docUri, propertyUri, this.small.createLiteral(propertyValue, lang));
	}
}

/** Attention, ne traite pas la date de création, juste le RDF.type et le SLVocab.PREF_LABEL_PROPERTY. */
void newKeyword(String uri, String label, String lang) {
	JenaUtils.newKeyword(this.big, uri, label, lang);
	JenaUtils.newKeyword(this.small, uri, label, lang);
}

void remove(String uri, String propertyUri, String objectUri) {
	JenaUtils.remove(this.big, uri, propertyUri, objectUri);
	JenaUtils.remove(this.small, uri, propertyUri, objectUri);
}

void delete(String uri) {
	// delete all statements involving uri
	JenaUtils.delete(this.big, uri);
	JenaUtils.delete(this.small, uri);
}

void changeObjects(String oldUri, String newUri) {
	JenaUtils.changeObjects(this.big, oldUri, newUri);
	JenaUtils.changeObjects(this.small, oldUri, newUri);
}


//
//
//

void save() throws JenaException, IOException, URISyntaxException {
	this.jFileModel.save();
	System.out.println("JFileBiModel.save " + jFileModel.getFile());
}

public boolean equals(Object o) {
	if (o == null) return false;
	return this.jFileModel.equals(((JFileBiModel) o).jFileModel);
}


} // class
