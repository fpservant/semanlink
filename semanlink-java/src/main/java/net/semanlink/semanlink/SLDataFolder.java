package net.semanlink.semanlink;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.semanlink.semanlink.SLModel.LoadingMode;
import net.semanlink.semanlink.SLModel.YYYYMM;
import net.semanlink.util.FileUriFormat;

/** Represents a sl:DataFolder (as used in semanLink-config.xml)
 *  that is a directory of metadata files loaded at once.
 */
public class SLDataFolder extends DocsFile {
private File file;
public SLDataFolder(File file, String base, SLThesaurus defaultThesaurus, LoadingMode loadingMode) {
	super(file.getAbsolutePath(), base, defaultThesaurus, loadingMode);
	this.file = file;
	if (!(file.exists())) throw new IllegalArgumentException("file " + file + " does not exist");
	if (!(file.isDirectory())) throw new IllegalArgumentException("file " + file + " is not a directory");
	if (base == null) throw new IllegalArgumentException("base must not be null");
	if (defaultThesaurus == null) throw new IllegalArgumentException("defaultThesaurus must not be null");
	if (loadingMode == null) throw new IllegalArgumentException("loadingMode must not be null");
}
public File getFile() { return this.file; }
//
//
//

/**
 * @param f a file inside this SLDataFolder
 */
public String getBase(File f) throws MalformedURLException, URISyntaxException {
	LoadingMode loadingMode = getLoadingMode();
	if (loadingMode.isBaseRelativeToFile()) {
		return getBase(f, getBase(), getFile());
	} else {
		return getBase();
	}
}

/** Base à utiliser pour le fichier sl.rdf file, sachant que la base rootBase correspond à rootFileCorrespondingToBase,
 * dans le cas où le loadingMode.isBaseRelativeToFile(). 
 * @throws URISyntaxException 
 * @throws MalformedURLException */
private static String getBase(File file, String rootBase, File rootFileCorrespondingToBase) throws MalformedURLException, URISyntaxException {
	String xBase = rootBase;
	if (rootFileCorrespondingToBase == null) throw new IllegalArgumentException("rootFileCorrespondingToBase " + rootBase + " null");
	/* ce qui était ds SLModel:
	String dirPath = filenameToUri(file.getParent());
	String basePath = filenameToUri(rootFileCorrespondingToBase.getPath());*/
	String dirPath = FileUriFormat.fileToUri(file.getParentFile());
	String basePath = FileUriFormat.fileToUri(rootFileCorrespondingToBase);
	if ((dirPath.startsWith(basePath)) && (!dirPath.equals(basePath))){ // si égaux ?
		String relPath = dirPath.substring(basePath.length());
		// 2005/12 :
		boolean slash = xBase.endsWith("/");
		// if (!slash) xBase += "/";
		if (!slash) {
			slash = relPath.startsWith("/");
			if (!slash) xBase += "/";
		}
		//System.out.print ("RELPATH"+ relPath);
		// if ((!slash) && (relPath.endsWith("/"))) relPath = relPath.substring(0, relPath.length()-1); // not tested
		xBase += relPath; // un slash à la fin - j'insiste :
		if (!(xBase.endsWith("/"))) xBase += "/";
		//System.out.println(" base "+ xBase);
	} else {
		//  je rajoute ça à cause de pb ds le cas Sites/test
		if (rootFileCorrespondingToBase.isDirectory()) {
			if (!(xBase.endsWith("/"))) xBase += "/";
		}
	}
	// System.out.println("getBase file "  + file + " rootbase " + rootBase + " rootFileCB " + rootFileCorrespondingToBase + " : " +xBase);
	return xBase;
}

}
