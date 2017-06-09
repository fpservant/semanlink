package net.semanlink.sljena;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import net.semanlink.util.Util;
import net.semanlink.util.YearMonthDay;

/**
 * Model backed by a file. 
 * 
 * How to use: <OL>
 * <LI>create a JFileModel</LI>
 * <LI>getModel() returns the corresponding Jena model</LI>
 * <LI>add statements to the Jena model</LI>
 * <LI>save the JFileModel</LI>
 * </OL>
 */
public class JFileModel {
private Model model;
private String longFileName;
private String base;

// CONSTRUCTION

/**
 * @base : pour un fichier de kws, passer l'url du vocabulaire.
 */
public JFileModel(String longFileName, String base) throws JenaException, IOException {
	this(loadModel(longFileName, base), longFileName, base);
}

/**
 * @base : pour un fichier de kws, passer l'url du vocabulaire.
 */
public JFileModel(Model model, String longFileName, String base) { // made public for SKOSIFY alias see JModel.aliasIt
	this.longFileName = longFileName;
	this.base = base;
	this.model = model;	
}

private static Model loadModel(String longFileName, String base) throws JenaException, IOException {
	Model model = ModelFactory.createDefaultModel();
	try {
		ModelFileIOManager.getInstance().readModel(model, longFileName, base);
	} catch (JenaException e) {
		System.err.println("JenaException reading " + longFileName + " : " + e.toString());
		throw e;
	}
	return model;
}


// GETTERS

/** Retourne le jena Model en question. */
public Model getModel() { return this.model; }

/** Retourne le fichier en question. */
public File getFile() {
	return new File(this.longFileName);
}

// IMPLEMENTATION

public void save() throws JenaException, IOException, URISyntaxException {
	//System.out.println("JFileModel.save " + longFileName + " base : " + base);
	File file = new File(this.longFileName);
	// on garde un backup par jour - le 1er de la journ�e (parce qu'il y a plein de maj
	// successives, il y aurait trop de risques d'�craser une svg ok par un fichier qui ne l'est pas.)
	String svgFilename = backupFilename(this.longFileName);
	File svgFile = new File(svgFilename);
	if (file.exists()) {
		if (!svgFile.exists()) {
			// suppression des anciennes sauvegardes
			String shortName = file.getName();
			String shortNameWithoutExtension = Util.getWithoutExtension(shortName);
			File dir = file.getParentFile();
			String[] list = dir.list();
			Arrays.sort(list);
			ArrayList al = new ArrayList();
			for (int i = 0; i < list.length; i++) {
				String sf = list[i];
				if (!(sf.startsWith(shortNameWithoutExtension))) continue;
				if (!(sf.endsWith(".rdf"))) continue;
				if (sf.equals(shortName)) continue;
				al.add(sf);
			}
			// on en garde 2
			for (int i = 0; i < al.size() - 1; i++) {
				String sf = (String) al.get(i);
				File f = new File(dir, sf);
				f.delete();
			}
			// boolean sucess = file.renameTo (svgFile);
			file.renameTo (svgFile);
		}
	} else {
		// make sure the directory exists, else writing will fail
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	boolean ok = false;
	try {
		this.model.setNsPrefix("sl","http://www.semanlink.net/2001/00/semanlink-schema#");
		ModelFileIOManager.getInstance().writeModel(this.model, this.longFileName, base);
		ok = true;
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		if (!ok) {
			// Comme je n'arrive pas pour l'instant � emp�cher la cr�ation de bad uri
			// (ex : un fichier dont le nom court contient un /)
			// qui fait qu'on a ici une exception,
			// je reprends ici la sauvegarde.
			// TODO : arriver � eviter ca.
			System.err.println("JFileModel.save : writeModel error, reverting to backupFile "+this.longFileName);
			if (file.exists()) file.delete();
			if (svgFile.exists()) svgFile.renameTo(file);
		}
	}
}

public static String backupFilename(String fileName) {
	String date = (new YearMonthDay()).getYearMonthDay("-");
	int dotIndex = fileName.lastIndexOf('.');
	if (dotIndex < 0) {
		return fileName + "-" + date;
	} else {
		return fileName.substring(0, dotIndex) + "-" + date + "." + fileName.substring(dotIndex + 1);
	}	
}

public boolean equals(Object o) {
	if (o == null) return false;
	return this.longFileName.equals(((JFileModel) o).longFileName);
}
}
