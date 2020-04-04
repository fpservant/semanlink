package net.semanlink.servlet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.ServletContext;

import net.semanlink.metadataextraction.MetadataExtractorManager;
import net.semanlink.semanlink.SLDataFolder;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLThesaurus;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.semanlink.WebServer;
import net.semanlink.semanlink.SLModel.LoadingMode;
import net.semanlink.skos.SKOS;
import net.semanlink.sljena.JModel;
import net.semanlink.sljena.modelcorrections.AliasCorrection;
import net.semanlink.sljena.modelcorrections.AliasToSkosAltLabelCorrection;
import net.semanlink.sljena.modelcorrections.CreationDateCorrection;
import net.semanlink.sljena.modelcorrections.ModelCorrector;
import net.semanlink.sljena.modelcorrections.NameSpaceCorrection;
import net.semanlink.sljena.modelcorrections.OldSLVocab2NewSLVocabTransformer;
import net.semanlink.sljena.modelcorrections.PropertyCopyCorrection;
import net.semanlink.sljena.modelcorrections.PropertyURICorrection;
import net.semanlink.sljena.modelcorrections.TagLabel2PrefLabelCorrection;
import net.semanlink.sljena.modelcorrections.ThesaurusUriCorrection;
import net.semanlink.sljena.modelcorrections.KeywordUriCorrection;
import net.semanlink.util.Util;
import net.semanlink.util.jena.JenaUtils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;



// TODO : check that webServer is not null (at least one mapping)
// create a default one if no mapping defined
// 



/** Modelizes the semanlink config file : models to be loaded, web server mappings, application parameters,... */
public class SemanlinkConfig { // extends JFileModel {
public static class LoadException extends RuntimeException {
	LoadException(String s) { super(s); }
	LoadException(Throwable e) {super(e); }
}
public static final String SEMANLINK_CONFIG_SCHEMA = "http://www.semanlink.net/2006/09/semanlink-config-schema#";
// property characterizing a SLModel to be loaded
public static final String SLMODEL_TYPE = SLVocab.SEMANLINK_SCHEMA + "SLModel";
public static final String THESAURUS_TYPE = SLVocab.SEMANLINK_SCHEMA + "Thesaurus";
public static final String DATA_FOLDER_TYPE = SLVocab.SEMANLINK_SCHEMA + "DataFolder";
//property characterizing a WebServerFileMapping
public static final String SL_WEBSERVER_MAPPING_TYPE = SEMANLINK_CONFIG_SCHEMA + "WebServerMapping";
//
public static final String SL_APPLICATION_PARAMS_TYPE = SEMANLINK_CONFIG_SCHEMA + "ApplicationParams";

/** slash terminated */
private String servletUrl;
private File configFile;
private File mainDataDir;
private WebServer webServer;
private ServletContext servletContext;
private ApplicationParams applicationParams;

public static final String SL_THESAURUS_PROP = SEMANLINK_CONFIG_SCHEMA + "thesaurus";
public static final String SL_DEFAULT_THESAURUS_PROP = SEMANLINK_CONFIG_SCHEMA + "defaultThesaurus";
public static final String SL_OLD_URI_PROP = SEMANLINK_CONFIG_SCHEMA + "oldUri";
public static final String SL_DATA_FOLDER_PROP = SEMANLINK_CONFIG_SCHEMA + "dataFolder";
public static final String SL_DEFAULT_DATA_FOLDER_SHORT_PROP = "defaultDataFolder";
public static final String SL_DEFAULT_DATA_FOLDER_PROP = SEMANLINK_CONFIG_SCHEMA + SL_DEFAULT_DATA_FOLDER_SHORT_PROP;
public static final String SL_BOOKMARK_FOLDER_PROP = SEMANLINK_CONFIG_SCHEMA + "bookmarkFolder";
public static final String SL_NOTES_FOLDER_PROP = SEMANLINK_CONFIG_SCHEMA + "notesFolder";
public static final String SL_FILE_PATH_PROP = SEMANLINK_CONFIG_SCHEMA + "filePath";
public static final String SL_PATH_RELATIVE_TO_MAIN_DATA_DIR_SHORT_PROP = "pathRelativeToDataDir";
public static final String SL_PATH_RELATIVE_TO_MAIN_DATA_DIR_PROP = SEMANLINK_CONFIG_SCHEMA + SL_PATH_RELATIVE_TO_MAIN_DATA_DIR_SHORT_PROP;
public static final String SL_FILE_URI_PROP = SEMANLINK_CONFIG_SCHEMA + "fileURI";
public static final String SL_LOADING_MODE_PROP = SEMANLINK_CONFIG_SCHEMA + "loadingMode";
public static final String SL_METADATA_EXTRACTION_BLACKLISTED = SEMANLINK_CONFIG_SCHEMA + "metadataExtractionBlackListed";
public static final String SL_MAIN_FRAME = SEMANLINK_CONFIG_SCHEMA + "mainFrame";
public static final String PUBLISH_PROP = SEMANLINK_CONFIG_SCHEMA + "publish";

public static final String SL_BASE_PROP = SEMANLINK_CONFIG_SCHEMA + "base";

public static final String SLC_USE_PROPERTY_PROP = SEMANLINK_CONFIG_SCHEMA + "useProperty";

private Model model;
private Property filePathProp;
private Property pathRelativeToMainDataDirProp;
private Property fileURIProp;
private Property thesaurusProp;
private Property loadFileModeProp;
/*private Property thesaurusFileProp;
private Property loadFileBaseProp;
private Property loadFileThesaurusProp;
private Property pathRelativToServletProp;*/
static class ConfigErrorHandler implements RDFErrorHandler {

	public void error(Exception e) {
		throw new LoadException(e);
	}

	public void fatalError(Exception e) {
		throw new LoadException(e);
	}

	public void warning(Exception e) {
		throw new LoadException(e);
	}
	
}
public SemanlinkConfig(File configFile, String servletUrl, ServletContext servletContext) throws JenaException, IOException {
	// super(configFile.getAbsolutePath(), slashEndedServletUrl(servletUrl));
	this.configFile = configFile;
	this.servletUrl = slashEndedServletUrl(servletUrl);
	this.servletContext = servletContext;
	this.model = ModelFactory.createDefaultModel();
	
	
	if (! (configFile.exists()) ) throw new IllegalArgumentException(configFile + " doesn't exist");
	/* on ne peut tolérer aucune erreur sur la lecture
	(for instance, we cannot tolerate a warning such as:
	WARN [Thread-1] (RDFDefaultErrorHandler.java:36) - unknown-source: {W107} Bad U
	RI: <?127.0.0.1:8080/semanlink/?> Error: 10/SCHEME_MUST_START_WITH_LETTER in slo
	t 2
	ERROR [Thread-1] (RDFDefaultErrorHandler.java:40) - 127.0.0.1:8080/semanlink/(li
	ne 14 column 27): {E214} Resolving against bad URI <127.0.0.1:8080/semanlink/>:
	
	Donc, ceci ne suffit pas :
	InputStream in = new BufferedInputStream(new FileInputStream(configFile));
	model.read(in,this.servletUrl); */
	RDFReader rdfReader = this.model.getReader();
	rdfReader.setErrorHandler(new ConfigErrorHandler());
	InputStream in = new BufferedInputStream(new FileInputStream(configFile));
	rdfReader.read(model, in, this.servletUrl);
	
	this.filePathProp = model.createProperty(SL_FILE_PATH_PROP);
	this.pathRelativeToMainDataDirProp = model.createProperty(SL_PATH_RELATIVE_TO_MAIN_DATA_DIR_PROP);
	this.mainDataDir = this.configFile.getParentFile(); // en afit dir "conf"
	if (this.mainDataDir == null) throw new LoadException(this.configFile + " has no parent: impossible to define a path relative to it.");
	this.mainDataDir = this.mainDataDir.getParentFile();
	if (this.mainDataDir == null) throw new LoadException(this.configFile.getParentFile() + " has no parent: impossible to define a path relative to it.");
	this.fileURIProp = model.createProperty(SL_FILE_URI_PROP);
	this.thesaurusProp = model.createProperty(SL_THESAURUS_PROP);
	this.loadFileModeProp = model.createProperty(SL_LOADING_MODE_PROP);
}


public ApplicationParams getApplicationParams() { return this.applicationParams; }

/** Retourne une liste de SLModel (aussi ajoutée au param, pour que la globale de slservlet soit documentée
 *  le plus vite possible, ce afin que slservlet.getSLMOdel ne se gauffre pas si on y fait référence. Je sais ça craint
 *  mais (cf update sicg), c'est parce que on a ref à slServlet.getSLModel dans ModelFileIOManager.writeModel */
public ArrayList<SLModel> load(ArrayList<SLModel> slModelList) throws IOException, URISyntaxException {
	ArrayList<SLModel> x = slModelList;
	ResIterator ite;
	
	// the application params
	ite = model.listSubjectsWithProperty(RDF.type, model.getProperty(SL_APPLICATION_PARAMS_TYPE));
	if (ite.hasNext()) {
		this.applicationParams = new ApplicationParams(ite.nextResource());
		if (ite.hasNext()) {
			throw new LoadException("More than one definition for ApplicationParams found in " + this.configFile);
		}
	} else {
		throw new LoadException("No definition for ApplicationParams found in " + this.configFile);
	}
	
	trace("SemanlinkConfig mainDataDir:"+ mainDataDir);

	// the web server
	ite = model.listSubjectsWithProperty(RDF.type, model.getProperty(SL_WEBSERVER_MAPPING_TYPE));
	// if (ite.hasNext()) webServer = new WebServer();
	webServer = new WebServer(); // 2019-03 : quoi, tjrs un webserver ???
	if (!ite.hasNext()) {
		// j'ajoute les mapping trivial de la servlet
		// (par sécurité, parce que je ne suis pas trop sûr que tout se passe bien
		// si aucun mapping n'est défini
		// TODO VIRER
		// webServer.addMapping(new URI(this.servletUrl + "/document"), getDirInMainDataDir("/documents"));
	} else {
		for (;ite.hasNext();) {
			Resource res = ite.nextResource();
			File dir = null;
			try {
				dir = getResDir(res, true);
			} catch (Exception e) {
				e.printStackTrace();
				throw new LoadException(SL_WEBSERVER_MAPPING_TYPE + " incorrect: " + e.toString());
			}

			String uri = res.getURI();
			if (!uri.endsWith("/")) uri += "/";
			webServer.addMapping(new URI(uri), dir);
		}			
	}

	ite.close();
	
	Property slModelTypeProp = model.getProperty(SLMODEL_TYPE);
	
	// the list of SLModel to be loaded
	ite = model.listSubjectsWithProperty(RDF.type, slModelTypeProp);
	for (;ite.hasNext();) {
		SLModel slModel = new JModel();
		// le faire le plus tôt possible, afin que
		// d'éventuels appels, par ex à SLServlet.getModel()
		// ne se viandent pas misérablement
		x.add(slModel);
		
		slModel.setWebServer(this.webServer);
		
		Resource slModelRes = ite.nextResource();	
		String slModelUri = slModelRes.getURI();
		slModel.setModelUrl(slModelUri);
		
		// Mettre un corrector si on en veut un
		ModelCorrector corrector = getCorrector(slModel);
		// ModelCorrector corrector = new ModelCorrector();
		slModel.setCorrector(corrector);

	 	StmtIterator site = null;
		RDFNode node = null;

		// THESAURUS
		// default thesaurus
		site = slModelRes.listProperties(model.getProperty(SL_DEFAULT_THESAURUS_PROP)); // TODO vérifier ce qui se passe si pas défini
		node = iterator2RDFNodeCheck(site, SL_DEFAULT_THESAURUS_PROP, true);
		SLThesaurus slThesaurus = loadThesaurus(slModel, (Resource) node, corrector);
		SLThesaurus defaultThesaurus = slThesaurus;
		slModel.setDefaultThesaurus(defaultThesaurus);
		
		// other thesaurus
		site = slModelRes.listProperties(model.getProperty(SEMANLINK_CONFIG_SCHEMA + "thesaurus"));
		for (;site.hasNext();) {
			node = site.nextStatement().getObject();
			if (!node.isResource() ) {
				throw new LoadException("error in " + configFile + " : thesaurus " + node + " should be a resource");
			}
			loadThesaurus(slModel, (Resource) node, corrector);
		}
		site.close();

		// DATA FOLDERS
		// default datafolder
		Resource defaultDataFolderRes = prop2ResourceCheck(model, slModelRes, SL_DEFAULT_DATA_FOLDER_PROP);
		if (defaultDataFolderRes == null) throw new LoadException("defaultFolder undefined");
		SLDataFolder defaultDataFolder = loadSLFile(slModel, defaultDataFolderRes, defaultThesaurus);
	 	slModel.setDefaultDataFolder(defaultDataFolder);
	 	webServer.setDefaultDocFolder(defaultDataFolder.getFile()); // @find CORS pb with markdown
	 	
	 	
	 	
	 	
//	 	// on ajoute systématiquement un (1/2) mapping de servlet/document vers le default data folder
//	 	// (cf pb CORS pour markdown file chez moi : le doc est servi par apache, on ne peut getter le fichier en ajax.
//	 	// Je veux donc donner la possibilité de linker vers 127.0.0.1:8080/semanlink/document/2015/10/UnFichier.md
//	 	// et il faut donc que cela envoie vers le fichier correspondant pour être servi par static file servlet
//	 	defaultDataFolder
//	 	webServer.addMapping(null, mainDataDir);
	 	
	 	
	 	
	 	

	 	// bookmark folder
		/*  // pas obligatoire, pour moi. 
		Resource bookmarkFolderRes = prop2ResourceCheck(model, slModelRes, SL_BOOKMARK_FOLDER_PROP);
		if (bookmarkFolderRes == null) throw new LoadException("bookmarkFolder undefined"); */
	 	Resource bookmarkFolderRes = null;
		site = slModelRes.listProperties(model.getProperty(SL_BOOKMARK_FOLDER_PROP));
		for (;site.hasNext();) {
			node = site.nextStatement().getObject();
			if (!node.isResource() ) {
				throw new LoadException("error in " + configFile + " : bookmark folder " + node + " should be a resource");
			}
			bookmarkFolderRes = (Resource) node;
			break;
		}
		site.close();
		if (bookmarkFolderRes != null) {
			// 2019-03 uris for bookmarks
			// SLDataFolder bookmarkDataFolder = loadSLFile(slModel, bookmarkFolderRes, defaultThesaurus, null, true);
			SLDataFolder bookmarkDataFolder = loadSLFile(slModel, bookmarkFolderRes, defaultThesaurus, bookmarkFolderRes.getURI(), true);
		 	slModel.setBookmarkFolder(bookmarkDataFolder);
		}

	 	// notes folder
		Resource notesFolderRes = prop2ResourceCheck(model, slModelRes, SL_NOTES_FOLDER_PROP);
		if (notesFolderRes == null) throw new LoadException("notesFolder undefined");
		// notesFolderRes is now an anonymous resource (we don't state anymore that it is "....NOTE_SERVLET_PATH")
		// File notesFolderFile = loadSLFile(slModel, notesFolderRes);
		String notesUri = null;
		if (notesFolderRes.isAnon()) {
			// notesUri = aSlashB(this.servletUrl,CoolUriServlet.DOC_SERVLET_PATH); // 2006/10
			notesUri = aSlashB(this.servletUrl,CoolUriServlet.NOTE_SERVLET_PATH);			
		} else {
			notesUri = notesFolderRes.getURI();
		}
		SLDataFolder notesDataFolder = loadSLFile(slModel, notesFolderRes, defaultThesaurus, notesUri, false);
	 	// notes are accessed through the servlet
	 	// we have to state that there is a web server association between the path
	 	// to the dir containing the notes and the servlet
		// done in loadSLFile : webServer.addMapping(new URI(notesUri  + "/"), notesFolderFile);
	 	slModel.setNotesFolder(notesDataFolder);

		// other data folders
	
	 	site = slModelRes.listProperties(model.getProperty(SL_DATA_FOLDER_PROP));
		for (;site.hasNext();) {
			node = site.nextStatement().getObject();
			if (!node.isResource() ) {
				throw new LoadException("error in " + configFile + " : " + node + " should be a resource");
			}
			try {
				loadSLFile(slModel, (Resource) node, defaultThesaurus);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR trying to load " + node + " : " + e);
			}
		}
		site.close();

		// attention, ce truc ne recharge pas le model :
  	// si on l'utilise, il faut donc quitter juste après et relancer
		// (la différence avec les corrections habituelles, c'est qu'on a besoin d'avoir
		// déjà chargé le slModel pour définir la correction
  	// ((JModel) slModel).correctOldKwUris(); // commenter (mais A GARDER!!!)
  	////////// JAMAIS FAIT ((JModel) slModel).correctAlias();
	  	
		// slModel.listenDocs();

		// x.add(slModel);
		
		// maintenant que les fichiers sont chargés, supprimer le corrector
		slModel.setCorrector(null);
	}
	// PrintWriter pw = new PrintWriter(new FileWriter(longFileName+"aaa"));
	// model.write(pw,"RDF/XML-ABBREV",null);

	return x;
}

/** Returns part1 / part2, en faisant gaffe de ne pas mettre un slash en trop s'il y en a un à la fin de part1 ou au début de part2.*/
private String aSlashB(String part1, String part2) {
	if (part1.endsWith("/")) {
		if (part2.startsWith("/")) {
			return part1 + part2.substring(1);
		} else {
			return part1 + part2;
		}
	} else {
		if (part2.startsWith("/")) {
			return part1 + part2;
		} else {
			return part1 + "/" + part2;
		}		
	}
}

/** modify corrector (supposed to be the corrector of slMod), if there is a property oldUri */
private SLThesaurus loadThesaurus(SLModel slMod, Resource thesaurusRes, ModelCorrector corrector) throws IOException, URISyntaxException {
	String uri = thesaurusRes.getURI();
	try {
		File dir = getResDir(thesaurusRes, true);
		// SL_OLD_URI_PROP
		String oldUri = prop2Uri(model, thesaurusRes, SL_OLD_URI_PROP);
		if (oldUri != null) {
			corrector.add(new ThesaurusUriCorrection(oldUri, uri));
		}
		
		trace("loadThesaurus:" + uri + " ; dir: " + dir + " oldUri: " + oldUri);
		return slMod.loadThesaurus(uri, dir);
	} catch (Exception e) {
		e.printStackTrace();		
		throw new LoadException("Impossible to access file corresponding to thesaurus " + thesaurusRes.getURI() + ":" + e.toString());
	}
}

/** Retourne fichier chargé. */
private SLDataFolder loadSLFile(SLModel slMod, Resource dataFolderRes, SLThesaurus defaultThesaurus) throws IOException, URISyntaxException {
	Resource base = (Resource) JenaUtils.firstObjectOfProperty(dataFolderRes, model.createProperty(SL_BASE_PROP)); // 2019-07
	if (base == null) base = dataFolderRes;
	return loadSLFile(slMod, dataFolderRes, defaultThesaurus, base.getURI(), false);
}

// 2007/02 isBookmarkDataFolder

/** ATTENTION, il faut absolument donner base parce que, si null, prend l'uri du thesaurus (ce qui ne convient qu'au cas du bookmarkFolder)
 * 	pour un dataFolder normal, passer dataFolderRes.getURI() 
 *  pour notesFolder, il faut l'avoir déterminé. */
private SLDataFolder loadSLFile(SLModel slMod, Resource dataFolderRes, SLThesaurus defaultThesaurus, String base, boolean isBookmarkDataFolder) throws IOException, URISyntaxException {
	Resource thesaurus = (Resource) this.iterator2RDFNode(dataFolderRes.listProperties(this.thesaurusProp));
	String thesaurusURI = null;
	if (thesaurus != null) {
		thesaurusURI = thesaurus.getURI();
	} else {
		thesaurusURI = defaultThesaurus.getURI();
	}

	String sLoadingMode = iterator2StringValue(dataFolderRes.listProperties(this.loadFileModeProp));
	LoadingMode loadingMode= new LoadingMode(sLoadingMode);
	
	// 2019-03 uris for bookmarks -> commented out
//	if (isBookmarkDataFolder) {
//		loadingMode.setBaseRelativeToFile(false); // pour utiliser thesaurus comme base (idem "absoluteBase" dans loadingmode - pour ne pas
//		// avoir à mettre "absoluteBase" dans le loadingmode du fichier xml
//	}

	File file = getResFile(dataFolderRes, true);
	// TODO VERIFY 2006/09
	trace("loadSLFile: " + file);
	
	// if ((isBookmarkDataFolder) || (base == null)) { // 2019-03 uris for bookmarks
	if (base == null) {
		base = thesaurusURI;
		// ATTENTION base must be slash terminated (ou # si on avait choisi l'autre option pour les uris de thesaurus ? // #thing)
		if (!base.endsWith("/")) base += "/";
	} else if (base.startsWith("http")) { // (pas si c'est une file-protocol url!)
		this.webServer.addMapping(new URI(base), file); // attention, absolument nécessaire, cf par ex StaticFileServlet
	}
	return slMod.loadSLDataFolder(file, base, thesaurusURI, loadingMode);
}

//
//
//

static String slashEndedServletUrl(String servletUrl) {
	if (!(servletUrl.endsWith("/"))) servletUrl += "/";
	return servletUrl;
}

String prop2StringValue(Model model, Resource subject, String propUri) {
	StmtIterator ite = subject.listProperties(model.getProperty(propUri));
	return iterator2StringValue(ite);
}

private Resource prop2ResourceCheck(Model model, Resource subject, String propUri) {
	StmtIterator ite = subject.listProperties(model.getProperty(propUri));	
	RDFNode node = iterator2RDFNodeCheck(ite, propUri, true);
	if (!(node instanceof Resource)) throw new IllegalArgumentException(propUri + " of " + subject + " should be a resource");
	return (Resource) node;
}

String prop2Uri(Model model, Resource subject, String propUri) {
	StmtIterator ite = subject.listProperties(model.getProperty(propUri));	
	try {
		RDFNode x = null;
		if (ite.hasNext()) {
			x = ite.nextStatement().getObject();
			if (ite.hasNext()) {
				throw new IllegalArgumentException("More than one value for " + propUri);
			}
		} else {
			return null;
		}
		if (!x.isResource() ) {
			throw new LoadException("Value for prop " + propUri + ": " + x + " should be a resource");
		}
		return ((Resource) x).getURI();
	} finally {
		ite.close();
	}
}

/** call load first */
public WebServer getWebServer() { return this.webServer; }


private static RDFNode iterator2RDFNode(StmtIterator ite) {
	RDFNode x = null;
	if (ite.hasNext()) {
		x = ite.nextStatement().getObject();
		ite.close();
	}
	return x;
}

/** check there is one and only one value.
 *  @param mustReturnAResource if true, also checks the return value is a resource */
private static RDFNode iterator2RDFNodeCheck(StmtIterator ite, String propName, boolean mustReturnAResource) {
	try {
		RDFNode x = null;
		if (ite.hasNext()) {
			x = ite.nextStatement().getObject();
			if (ite.hasNext()) {
				throw new IllegalArgumentException("More than one value for " + propName);
			}
		} else {
			throw new IllegalArgumentException(propName + " not defined.");		
		}
		if (mustReturnAResource) {
			if (!x.isResource() ) {
				throw new LoadException(propName + " " + x + " should be a resource");
			}
		}
		return x;
	} finally {
		ite.close();		
	}
}

private static String iterator2StringValue(StmtIterator ite) {
	RDFNode x = iterator2RDFNode(ite);
	if (x == null) return null;
	return x.toString();
}

// @find changing uris
private ModelCorrector getCorrector(SLModel slModel) {
	ModelCorrector x = new ModelCorrector();
	// try {
		// x.add(new KeywordUriCorrection("http://www.hypersolutions.fr/2001/00/vocab#mars_2004_mars_2004", "http://www.hypersolutions.fr/2001/00/vocab#mars_2004", Util.shortDate2Long("11/11/2004", Locale.FRANCE)));
		// x.add(new KeywordUriCorrection("http://www.semanlink.net/tag/berlusconi", "http://www.semanlink.net/tag/berlusCON", Util.shortDate2Long("25/12/2012", Locale.FRANCE)));
	// } catch (ParseException e) {
		// e.printStackTrace();
	// }
	// 
	
	// CORRECTIONS LIEES AU #thing
	// String oldVocab, newVocab;
	/*oldVocab = "http://www.hypersolutions.fr/2001/00/vocab#";
	String s = SLServlet.getServletUrl();
	if (!s.endsWith("/")) s+= "/";

	newVocab = s + "tag/";
	x.add(new ThesaurusUriCorrection(oldVocab, newVocab));*/
	
	/*
	new OldSLVocab2NewSLVocabTransformer(x);	
	oldVocab = "http://sicg.tpz.renault.fr/thesaurus#";
	newVocab = "http://sicg.tpz.renault.fr/thesaurus/";
	x.add(new NameSpaceCorrection(oldVocab, newVocab));
	*/
	
	/*
	// Exemple de changement d'une uri de thesaurus en une autre
	// ATTENTION, il ne faut pas oublier de faire les changeemnts dans semanlink-config.xml
	// (changements qui sont doubles : l'url du sl:Thesaurus, et dans les sl.dataFolder)
	oldVocab = "http://www.old.com/thesaurus";
	newVocab = "http://www.new.org/thesaurus";
	x.add(new ThesaurusUriCorrection(oldVocab, newVocab));
	*/

	// x.add(new CreationDateCorrection());
	
	/* pour recalculer les uris de kw à partir de leur label.
	 * Attention, il faut être en uri en "/", pas en "#"
	 * NE PEUT PAS S4APPELER ICI. VOIR SLServlet chercher correctOldKwUris
	 */
	 // NON ! KeywordUriCorrection.recomputeAllKwUrisFromLabel(x, slModel);
	
	// @find SKOSIFY
	// 2012-12 switching to skos
	// begin with just adding skos statements;
	// x.add(new PropertyCopyCorrection("http://www.semanlink.net/2001/00/semanlink-schema#hasParent", SKOS.broader.getURI(), true, false));

	
	x.add(new PropertyURICorrection("http://www.semanlink.net/2001/00/semanlink-schema#hasParent", SKOS.broader.getURI(), true, false));
	x.add(new PropertyURICorrection("http://www.semanlink.net/2001/00/semanlink-schema#related", SKOS.related.getURI(), true, false));
	x.add(new TagLabel2PrefLabelCorrection());
	x.add(new AliasToSkosAltLabelCorrection());
	x.add(new CreationDateCorrection()); // pour des pbs de creation date multiple ds les kws
	
	return x;
}







//
//
//

public class ApplicationParams {
	private String defaultSortProperty;
	private String defaultDateProperty;
	private boolean editorByDefault;
	// 2019-09
	// private String logonPage;
	private boolean useLogonPage;
	private boolean trace;
	private boolean isSemanlinkWebSite;
	private String[] metadataExtractionBlackList;
	private String mainFrame;
	private boolean useProxy;
	private String proxyHost;
	private int proxyPort;
	private String proxyUserName;
	private String proxyPassword;
	private boolean isProto;
	ApplicationParams(Resource res) {
		this.defaultSortProperty = prop2Uri(model, res, SEMANLINK_CONFIG_SCHEMA + "defaultSortProperty");
		if (this.defaultSortProperty == null) this.defaultSortProperty = SLVocab.SL_CREATION_DATE_PROPERTY;
		this.defaultDateProperty = prop2Uri(model, res, SEMANLINK_CONFIG_SCHEMA + "defaultDateProperty");
		if (this.defaultDateProperty == null) this.defaultDateProperty = SLVocab.SL_CREATION_DATE_PROPERTY;
		String s;
		s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "editorByDefault");
		if (s == null) {
			this.editorByDefault = true;
		} else {
			this.editorByDefault = Boolean.parseBoolean(s);
		}
		// 2019-09
//		if (!editorByDefault) {
//			this.logonPage = prop2Uri(model, res, SEMANLINK_CONFIG_SCHEMA + "logonPage");
//			// si pas de logonPage, alors pas de bouton edit. Sinon oui
//			// if (this.logonPage == null) throw new LoadException("No value found for " + logonPage);
//			if ((this.logonPage == null) || ("".equals(this.logonPage))) this.logonPage = null;
//		}
		// on n'utilise plus un logonPage, on veut juste savoir s'il faut mettre ou pas un bouton edit
		// Par paresse, je fait de quoi ne rien avoir à changer sur semanlink.net
		if (!editorByDefault) {
			s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "logonPage");
			if ((s == null) || ("".equals(s))) {
				useLogonPage = false;
			} else {
				useLogonPage = true;
			}
		} else {
			useLogonPage = false;
		}
		s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "trace");
		if (s == null) {
			this.trace = false;
		} else {
			this.trace = Boolean.parseBoolean(s);
		}
		
		s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "isSemanlinkWebSite");
		if (s == null) {
			this.isSemanlinkWebSite = false;
		} else {
			this.isSemanlinkWebSite = Boolean.parseBoolean(s);
		}
		
		Property metadataExtractionBlackListedProp = model.getProperty(SL_METADATA_EXTRACTION_BLACKLISTED);
		NodeIterator it = model.listObjectsOfProperty(res, metadataExtractionBlackListedProp);
		ArrayList al = new ArrayList(16);
		for (;it.hasNext();) {
			s = ((Literal)it.next()).getString().trim();
			if ("".equals(s)) continue;
			al.add(s);
		}
		this.metadataExtractionBlackList = new String[al.size()];
		al.toArray(this.metadataExtractionBlackList);
		MetadataExtractorManager.setMetadataExtractionBlackList(this.metadataExtractionBlackList);
		Statement sta = res.getProperty(model.getProperty(SL_MAIN_FRAME));
		if (sta != null) {
			RDFNode node = sta.getObject();
			this.mainFrame = ((Literal) node).getString();
			if ("".equals(this.mainFrame.trim())) this.mainFrame = null;
		}
		//
		s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "useProxy");
		if (s == null) {
			this.useProxy = false;
		} else {
			this.useProxy = Boolean.parseBoolean(s);
		}
		if (this.useProxy) {
			s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "proxyHost");
			if ((s != null) && (!"".equals(s))) {
				this.proxyHost = s;
			}
			s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "proxyPort");
			if ((s != null) && (!"".equals(s))) {
				this.proxyPort = Integer.parseInt(s);
			}
			s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "proxyUserName");
			if ((s != null) && (!"".equals(s))) {
				this.proxyUserName = s;
			}
			s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "proxyPassword");
			if ((s != null) && (!"".equals(s))) {
				this.proxyPassword = s;
			}
		} // useProxy
		s = prop2StringValue(model, res, SEMANLINK_CONFIG_SCHEMA + "proto");
		if (s == null) {
			this.isProto = false;
		} else {
			this.isProto = Boolean.parseBoolean(s);
		}
	}
	public String getDefaultDateProperty() {
		return defaultDateProperty;
	}
	public String getDefaultSortProperty() {
		return defaultSortProperty;
	}
	public boolean isEditorByDefault() {
		return editorByDefault;
	}
	
	// 2019-09
//	/** if null, (and !editorByDefault()) no btn edit displayed in GUI */
//	public String getLogonPage() {
//		return logonPage;
//	}
	public boolean useLogonPage() { return useLogonPage; }
	public boolean isTrace() { return trace; }
	public boolean isSemanlinkWebSite() { return isSemanlinkWebSite; }
	public String[] getMetadataExtractionBlackList() { return this.metadataExtractionBlackList; }
	public String getMainFrame() { return this.mainFrame; }
	public String getProxyHost() {
		return proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public String getProxyUserName() {
		return proxyUserName;
	}
	public String getProxyPassword() {
		return proxyPassword;
	}
	public boolean isProto() { return isProto; }
}

//
//
//
/** Returns the File corresponding to Resource res. 
 *  This file can be defined by any of following properties of res
 *  (which are tested in this order - first found is used) 
 *  <ul>
 *  <li>"pathRelativeToMainDataDir" property (in this case - and this case only -, if the file doesn't exist, a dir 
 *  with that name is created)</li>
 *  <li>"filePath" property</li>
 *	<li>through an URI (cf SL_FILE_URI_SHORT_PROP = "fileURI")
 *	(normally, a file protocol URI, unless it's an uri served by this.webServer)</li>
 *	</ul>
 *	@param slMod the model to add statements to (beware, not this.model nor res.getModel() !)
 *	@param res a resource of this.model represtenting a DataFolder or a Thesaurus (folder)
 *	@throwException if true, throws a RuntimeException if no file defined, or if file doesn't exist or is not created
 *	(cf special behavior with pathRelativeToMainDataDir property)
 */
private File getResFile(Resource res, boolean throwException) throws IOException, URISyntaxException {
	String pathRelativeToMainDataDir = iterator2StringValue(res.listProperties(pathRelativeToMainDataDirProp));
	if (pathRelativeToMainDataDir != null) return getDirInMainDataDir(pathRelativeToMainDataDir);

	String filename = iterator2StringValue(res.listProperties(filePathProp));
	if (filename != null) {	
		File f = getFile(filename, false);
		if (f == null) {
			if (throwException) throw new LoadException("File " + filename + " for " + res + " doesn't exists.");
		}
		return f;
	}
	
	// through an URI (cf SL_FILE_URI_SHORT_PROP = "fileURI")
	// (normally, a file protocol URI, unless it's an uri served by this.webServer
	// But in this case, why would you have defined it as the uri of the folder itself ?)
	Resource fileRes = (Resource) this.iterator2RDFNode(res.listProperties(this.fileURIProp));
	String fileURI = null;
	if (fileRes != null) {
		fileURI = fileRes.getURI();
		File f = SLModel.getFile(fileURI, this.webServer);
		if (f == null) throw new LoadException(SL_FILE_URI_PROP + " of " + res + " doesn't resolve to a file. Maybe a missing mapping in " + SL_WEBSERVER_MAPPING_TYPE + "?");
		return f;
	}

	// the dataFolder URI itself 
	// supposed to be either a file protocol uri, or an uri served by this.webServer
	File f = null;
	if (!res.isAnon()) {
		String uri = res.getURI();
		f = SLModel.getFile(uri, this.webServer);
		if (f == null) throw new LoadException("uri of " + res + " doesn't resolve to a file. Maybe a missing mapping in " + SL_WEBSERVER_MAPPING_TYPE + "? Or define its " + SL_FILE_URI_PROP + " or its " + SL_FILE_PATH_PROP + " property.");
	}
	return null;
}

/** Returns a directory inside the main datadir, creating it first if necessary. */
private File getDirInMainDataDir(String pathRelativeToMainDataDir) {
	File f = new File(this.mainDataDir, pathRelativeToMainDataDir);
	if (!f.exists()) {
		boolean created = f.mkdirs();
		if (!created) throw new LoadException("Impossible to create directory " + f);
	} else {
		if (!(f.isDirectory())) throw new LoadException(f + " is not a directory");
	}
	return f;
}

/** Returns the dir File corresponding to Resource res. 
 *  @see getResFile(Resource, boolean) about how it is computed. */
private File getResDir(Resource res, boolean throwException) throws IOException, URISyntaxException {
	File f = getResFile(res, false);
	if (f == null) {
		if (throwException) throw new LoadException("Filepath for " + res.getURI() + " undefined.");
		return null;
	}
	if (!f.exists()) {
		if (throwException) throw new LoadException("File " + f + " for " + res.getURI() + " doesn't exist.");
		return null;		
	}
	if (!f.isDirectory()) {
		if (throwException) throw new LoadException("File " + f + " for " + res.getURI() + " is not a directory.");
		return null;
	}
	return f;
}

//

// VERIFIER, MAIS JE PENSE QU'ON A (ICI, PAS DS SLSERVLET)
// TOUT INTERET A VIRER LA POSSIBILITE SUR LE REALPATH
/** 
 * If file doesn't exist, returns null if !throwException, else throw an exception. 
 * first, tries the filename as it is. If this doesn't work,
 * tries as a path relative to servlet (based on "realpath").*/
private File getFile(String filename, boolean throwException) { 		// kattare // ???
	File f = new File(filename);
	if (!f.exists()) {
		String svg = filename;
		filename = this.servletContext.getRealPath(svg);
		f = new File(filename);
		if (!f.exists()) {
			String s = "File \"" + svg +"\" doesn't exist, neither as absolute path, nor as realPath: " + filename;
			if (throwException) {
				throw new LoadException(s);
			} else {
				return null;
			}
		}
	}
	return f;
}

private void trace(String s) {
	// if (applicationParams.isTrace()) System.out.println(s);
	if ((this.applicationParams != null) && (this.applicationParams.isTrace())) {
		System.out.println(s);
	}
}


}