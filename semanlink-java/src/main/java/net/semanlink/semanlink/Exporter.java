/* Created on 3 déc. 06 */
package net.semanlink.semanlink;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import net.semanlink.graph.Graph;
import net.semanlink.graph.GraphTraversal;
import net.semanlink.servlet.SLServlet;
import net.semanlink.servlet.SemanlinkConfig;
import net.semanlink.sljena.JDocument;
import net.semanlink.sljena.JFileModel;
import net.semanlink.sljena.JKeyword;
import net.semanlink.sljena.JModel;
import net.semanlink.util.CopyFiles;
import net.semanlink.util.Util;
import net.semanlink.util.YearMonthDay;
import net.semanlink.util.jena.JenaUtils;

public class Exporter {
public static final String PUBLISH_PROP = SemanlinkConfig.PUBLISH_PROP;
private SLModel slMod;
private SLDataFolder dataFolder;
private String contextURL;

private static final String DOCS_SUB_FOLDER = "documents";
private static final String BOOKMARKS_SUB_FOLDER = "bookmarks";
private static final String DOC_SUB_PATH = "doc";

/**
 * @param slMod model to be exported
 * @param dataFolder the dir that will contain the exported files (a dir tags and a dir bookmarks will be created inside)
 * @param SLServletContextURL par ex http://127.0.0.1:8080/semanlink ou http://www.semanlink.net
 * BEWARE: remplace les éventuelles dir "tags" et "bookmarks" existant à l'intérieur vont être remplacées
 * @throws Exception
 */
public Exporter(SLModel slMod, SLDataFolder dataFolder, String contextURL) throws Exception {
	this.slMod = slMod;
	this.dataFolder = dataFolder;
	this.contextURL = contextURL;
	File dir = this.dataFolder.getFile();
	if (dir.exists()) {
		boolean ok;
		File tagsDir = new File(dir, "tags");
		if (tagsDir.exists()) {
			ok = Util.deleteDir(tagsDir);
			if (!ok) throw new RuntimeException("Impossible to delete " + tagsDir);
		}
		File bookmarksDir = new File(dir, BOOKMARKS_SUB_FOLDER);
		if (bookmarksDir.exists()) {
			ok = Util.deleteDir(bookmarksDir);
			if (!ok) throw new RuntimeException("Impossible to delete " + bookmarksDir);
		}
		File notesDir = new File(dir, "notes");
		if (notesDir.exists()) {
			ok = Util.deleteDir(notesDir);
			if (!ok) throw new RuntimeException("Impossible to delete " + notesDir);
		}
		File docsDir = new File(dir, DOCS_SUB_FOLDER);
		if (docsDir.exists()) {
			ok = Util.deleteDir(docsDir);
			if (!ok) throw new RuntimeException("Impossible to delete " + docsDir);
		}
	}
}

/**
 * Export an SLModel into a given dir
 * 
 * Ne prend que les bookmarks (docs qui ne sont pas locaux)
 * @param nbOfDays if < 0, all things after 2000-00-00
 */
public void export(int nbOfDays) throws Exception {
	Model kwsModel = ((JModel) this.slMod).getKWsModel();
	HashSet tagHS = new HashSet();
	
	//
	// export the docs, keeping track of the tags used
	//
	
	exportDocuments(nbOfDays, tagHS);
	
	//
	// maintenant, on a dans tagHS tous les kws publiables affectés aux docs
	//
	
	// ce qui suit pourrait être considérablement optimisé.
	// Ici, pour les parents, on fait un graphe de SLKeywords
	// qu'on parcourt simplement (simpleWalk)
	// puis on fait un model jena à partir du résultat
	// ALORS QUE on pourrait très bien
	// - prendre directement des resources pour le graphe
	// lors du parcours, documenter le model résultat
	SLKeyword[] seeds = new SLKeyword[tagHS.size()];
	tagHS.toArray(seeds);
	ParentTagsGraph graph = new ParentTagsGraph(seeds);
	GraphTraversal trav = new GraphTraversal(graph);
	tagHS = trav.getNodes();
	// tagHS est maintenant le set de tous les tags affectés de docs + leurs parents
	// Faudrait virer les non publiables. Va être fait plus bas (ds l'iterator)
	
	File tagFile = new File(this.dataFolder.getFile(), "tags/slkws.rdf");
	String base = this.slMod.getDefaultThesaurus().getBase();
	JFileModel jFileModel = new JFileModel(tagFile.getPath(), base);
	Model tagMod = jFileModel.getModel();
	Iterator it = tagHS.iterator();
	for(;it.hasNext();) {
		JKeyword tag = (JKeyword) it.next();
		
		if (!(publish(tag,true))) continue;
		Resource res = tag.getRes();
		// res = kwsModel.getResource(res.getURI());
		StmtIterator ite = res.listProperties ();
		/*
		Here, I was adding all statements,
		but there is a problem with related:
		do not add them if they are not in tagHS
		(in this case indeed, they do not have docs and
		they would end up without label)
		if (ite.hasNext()) {
			tagMod.add(ite);
		}
		*/
		for (;ite.hasNext();) {
			Statement sta = ite.nextStatement();
			// ne pas ajouter le statement si l'objet est non publiable
			RDFNode val = sta.getObject();
			if (val instanceof Resource) {
				if (!publish((Resource) val, true)) continue; // HUM
			}

			if (sta.getPredicate().getURI().equals(SLVocab.HAS_FRIEND_PROPERTY)) {
				// System.out.println("*********" + sta.getSubject() + " / " + sta.getPredicate().getURI() + " / " + sta.getObject());
				// Devrait être amélioré (éviter de passer par le slkeyword : avoir des uri dans tagHS)
				String relatedUri = ((Resource) sta.getObject()).getURI();
				SLKeyword relatedKw = slMod.getKeyword(relatedUri);
				if (!(tagHS.contains(relatedKw))) {
					continue;
				}
			}
						
			// System.out.println(sta.getPredicate().getURI());
			tagMod.add(sta);
		}
		ite.close();
		// il faut encore ajouter les libellés des alias
		List aliasUriList = this.slMod.getAliasUriList(tag);
		if (aliasUriList != null) {
			for (int i = 0; i < aliasUriList.size(); i++) {
				res = kwsModel.getResource((String) aliasUriList.get(i));
				// System.out.println(res.getURI());
				StmtIterator iteAliasProps = res.listProperties ();
				if (iteAliasProps.hasNext()) {
					tagMod.add(iteAliasProps);
				}
				iteAliasProps.close();
			}
		}
	}
	jFileModel.save();
}

class ParentTagsGraph implements Graph {
	SLKeyword[] seeds;
	ParentTagsGraph(SLKeyword[] seeds) {
		this.seeds= seeds;
	}
	
	public Iterator getNeighbors(Object node) throws Exception {
		SLKeyword tag = (SLKeyword) node;
		List list = tag.getParents();
		return list.iterator();
	}

	public Object[] seeds() {
		return this.seeds;
	}
	
}

private boolean isLocal(String docUri) {
		if (docUri.startsWith("file:/")) return true;
		if (docUri.startsWith("http://127.0.0.1")) return true;
		if (docUri.startsWith("http://localhost")) return true;
		if (!isSicg()) {
			if (docUri.contains("renault")) {
				if (docUri.contains("rplug.renault.com")) {
					return false;
				}
				return true;
			}
//		} else { // ne pas exporter les cr de rdd
//			if (docUri.contains("/cr/")) {
//				return true;
//			} else if (docUri.contains("sicg.tpz.renault.fr/journal")){
//				return true;
//			} else {
//				return false;
//			}
		}
		return false;
}

// TODO REMOVE
private boolean isSicg() {
	return SLServlet.getServletUrl().contains("sicg.tpz.renault.fr");
}

/**
 * @param tagHS si non null, y met tous les tags associés aux docs.
 * @param nbOfDays if < 0, all things after 2000-00-00
 */
private void exportDocuments(int nbOfDays, HashSet tagHS) throws Exception {
	
	if (isSicg()) {
		purgeDates();
	}
	
	
	
	// pourrait être optimisé : ici, on ajoute jour par jour
	// les docs au fichier du mois (30 lectures écriture du fichier, là où il pourrait
	// n'y en avoir qu'une.

	
	
	
	String dateLimite = "2000-00-00"; // 2013-09
	if (nbOfDays < 0) nbOfDays = 100000; // 2013-09
	for (int i = 0; i < nbOfDays+1; i++) {
		String dateString = (YearMonthDay.daysAgo(i)).getYearMonthDay("-");
		if (dateString.compareTo(dateLimite) < 0) break; // 2013-09
		List docsOfDayList = this.slMod.getDocumentsList(SLVocab.SL_CREATION_DATE_PROPERTY, dateString, null);
		

		JFileModel bookmarksJFileModel = null;
		Model bookmarksMod = null; // les bookmarks pour ce jour		
		{
			File slDotRdfFile = getSLDotRdfFileUsingCreationMonth(this.dataFolder, BOOKMARKS_SUB_FOLDER, dateString);
			String base = this.dataFolder.getBase(slDotRdfFile);
			// 2019-07 parce que les statements sont en /doc/, pas /bookmarks/
			// (pour les docs, on réécrit les statements, ce qui fait que ça va,
			// mais pas ici)
			base = base.replace("/" + BOOKMARKS_SUB_FOLDER + "/", "/" + DOC_SUB_PATH + "/");
			bookmarksJFileModel = new JFileModel(slDotRdfFile.getPath(), base);
			// System.out.println("Exporter base bookmarksJFileModel:" + base);
			bookmarksMod = bookmarksJFileModel.getModel(); // les docs pour ce jour
		}
		
		JFileModel notesJFileModel = null;
		Model notesMod = null; // les notes pour ce jour
		{
			File slDotRdfFile = getSLDotRdfFileUsingCreationMonth(this.dataFolder, "notes", dateString);
			String base = this.dataFolder.getBase(slDotRdfFile);
			notesJFileModel = new JFileModel(slDotRdfFile.getPath(), base);
			// System.out.println("Exporter base notesJFileModel:" + base);
			notesMod = notesJFileModel.getModel(); // les docs pour ce jour
		}
		
		JFileModel docsJFileModel = null;
		Model docsMod = null; // les docs pour ce jour
		File destDir4Docs = null;
		String base4docs = null;
		{
			File slDotRdfFile = getSLDotRdfFileUsingCreationMonth(this.dataFolder, DOCS_SUB_FOLDER, dateString);
			destDir4Docs = slDotRdfFile.getParentFile();
			base4docs = this.dataFolder.getBase(slDotRdfFile);
			docsJFileModel = new JFileModel(slDotRdfFile.getPath(), base4docs);
			// System.out.println("Exporter base docsJFileModel:" + base4docs);
			docsMod = docsJFileModel.getModel(); // les docs pour ce jour
		}
		
		
		boolean emptyBookmarksModel = true;
		boolean emptyNotesModel = true;
		boolean emptyDocsModel = true;
		for (int idoc = 0; idoc < docsOfDayList.size(); idoc++) {
			JDocument doc = (JDocument) docsOfDayList.get(idoc);
			
			String publish = null;
			PropertyValues vals = doc.getProperty(PUBLISH_PROP);
			if (vals != null) publish = vals.getFirstAsString().trim().toLowerCase();
			Resource res = doc.getRes();
//			System.out.println("Exporter res: " + res + " dateString: " + dateString);
			boolean addToBookmarks = false;
			boolean addToNotes = false;
			boolean addToDocs = false;
			String docUri = res.getURI();
			// this doesn't include docs such as http://127.0.0.1:8080/otherservlet or http://127.0.0.1/toto
			// if (slMod.isLocalDocument(res.getURI())) continue;
			
			boolean local;
			// uris for bookmarks
			SLDocumentStuff stuff = new SLDocumentStuff(doc, slMod, contextURL);
			String bookmarkOf = stuff.getBookmarkOf();
			if (bookmarkOf != null) {
				local = isLocal(bookmarkOf);
			} else {
				 local = isLocal(res.getURI());
			}
			
			if (local) {
				// document local.
				// Par défaut on ne publie pas
				// publié ssi son publish est true
				if (publish == null) {
					continue;
				} else {
					// 2019-03: hack to avoid problem if the prop value contains a lang
					// if (!("true".equals(publish.trim()))) {
					if (!(publish.trim().startsWith("true"))) {
						continue;
					}
					// cas des notes
					if (Note.isNote(docUri)) {
						addToNotes = true;
					} else {
						addToDocs = true;
					}
				}
				
			} else {
				// par défaut, on publie (exporte) les bookmarks
				if (publish != null) {
					// 2019-03: hack to avoid problem if the prop value contains a lang
					// if ("false".equals(publish.trim())) {
					if (publish.trim().startsWith("false")) {
						continue;
					}
				}
				addToBookmarks = true;
			}
			
			List<SLKeyword> kws = doc.getKeywords();
			Set<String> dontPublishKws = null; // pour ne pas mettre les triplets doc hasKW un kw non publiable 2013-03
			// 2010-12: PUBLISH_PROP can now be used for tags 
			// if (tagHS != null) tagHS.addAll(kws);
			if (tagHS != null) {
				for (SLKeyword kw : kws) {
					if (publish(kw, true)) { // 2013-03 hmm: ne regarde que le kw lui-même, pas ses ancêtres
						tagHS.add(kw);
					} else {
						if (dontPublishKws == null) dontPublishKws = new HashSet<String>();
						dontPublishKws.add(kw.getURI());
					}
				}
			}
			

			StmtIterator ite = res.listProperties ();
			if (ite.hasNext()) {
				if (addToBookmarks) {
					if (dontPublishKws == null) {
						emptyBookmarksModel = false;
						bookmarksMod.add(ite);
					} else {
						// only add statements that are not of the form res p aKWThatMustNotBePublished
						for (;ite.hasNext();) {
							Statement sta = ite.nextStatement();
							RDFNode obj = sta.getObject();
							if (obj instanceof Resource) {
								if (dontPublishKws.contains(((Resource) obj).getURI())) continue;
							}								
							bookmarksMod.add(sta);
							emptyBookmarksModel = false;
						}
					}
						
						
				} else if (addToNotes) {
						if (dontPublishKws == null) {
							emptyNotesModel = false;
							notesMod.add(ite);
						} else {
							for (;ite.hasNext();) {
								Statement sta = ite.nextStatement();
								RDFNode obj = sta.getObject();
								if (obj instanceof Resource) {
									if (dontPublishKws.contains(((Resource) obj).getURI())) continue;
								}								
								notesMod.add(sta);
								emptyNotesModel = false;
							}
						}
				
				
				
				} else if (addToDocs) {
					File source = this.slMod.getFile(docUri);
					if (source == null) {
						System.err.println("Exporter: no source for " + docUri);
					} else {
						// ATTENTION ne suporte pas la publication de dirs
						File destFile = CopyFiles.copyFile2Dir(source, destDir4Docs, false); // ATTENTION SI ON CHANGE QLQ CHOSE, VOIR ATTENTION PLUS BAS
						// String newDocUri = slMod.fileToUri(destFile); NON donne une uri file
					  // ATTENTION : seulement si base4docs est bien un truc relatif !:
						String newDocUri = base4docs + destFile.getName(); // ATTENTION suppose que le nouveau fichier n'est pas ds un sous-dossier de destDir4Docs 
						Resource newDocRes = docsMod.getResource(newDocUri);
						for (;ite.hasNext();) {
							Statement sta = ite.nextStatement();
							Property prop = docsMod.getProperty(sta.getPredicate().getURI());
							RDFNode obj = sta.getObject().inModel(docsMod);
							if (dontPublishKws != null) {
								if (obj instanceof Resource) {
									if (dontPublishKws.contains(((Resource) obj).getURI())) continue;
								}								
							}
							// BUG ICI dans le cas où obj est un document local
							docsMod.add(newDocRes, prop, obj);
							emptyDocsModel = false;
						}
					}
				} 
			}
			ite.close();
			
			
			
			
			
			
		}		
		if (!emptyBookmarksModel) bookmarksJFileModel.save();
		if (!emptyNotesModel) notesJFileModel.save();
		if (!emptyDocsModel) docsJFileModel.save();
	}
	// tagHS contient tous les kws directement attaché à un doc.
	// On va maintenant ramasser tous leurs parents, (et leurs related (?) ?)
}

private boolean publish(SLResource slRes, boolean defaut) {
	PropertyValues vals = slRes.getProperty(PUBLISH_PROP);
	if (vals == null) return defaut;
	return publish(vals.getFirstAsString(), defaut);
}

private boolean publish(Resource res, boolean defaut) {
	Literal lit = JenaUtils.firstLiteralOfProperty(res, res.getModel().createProperty(PUBLISH_PROP));
	if (lit == null) return defaut;
	return publish (lit.getString(), defaut);
}

private boolean publish(String publish, boolean defaut) {
	if (publish == null) return defaut;
	publish = publish.trim().toLowerCase();
	// 2019-03: hack to avoid problem if the prop value contains a lang
	// if ("true".equals(publish)) return true;
	// if ("false".equals(publish)) return false;
	if (publish.startsWith("true")) return true;
	if (publish.startsWith("false")) return false;
	return defaut;
}


/** @param subDir par ex "bookmarks" */
private File getSLDotRdfFileUsingCreationMonth(SLDataFolder dataFolder, String subDir, String creationDateString) {
	File x = dataFolder.getFile();
	x = new File(x, subDir);
	String yyyy = creationDateString.substring(0, 4);
	String mm = creationDateString.substring(5,7);
	x = new File(x,yyyy);
	x = new File(x,mm);		
	return new File(x,"sl.rdf");
}

//
// Pour export lors du changement semanlink-sicg 2019-08
// Les fichiers (articles, en particuliers) n'ont pas tous une sl:creationDate (amis souvent une dc:date)
// On aimerait que (pour les articles), ils se retrouvent ds un dossier correspondant à la date de la rdd
// Par ailleurs, certains ont plusieurs sl:creationDate

private void purgeDates() {
	System.out.println("**************** purgeDates *****************");
	
	JModel m = (JModel) slMod;
	Model docsModel = m.getDocsModel();
	
	Property dcDateProp = docsModel.getProperty(SLVocab.DATE_PARUTION_PROPERTY);
	Property slCreationDateProp = docsModel.getProperty(SLVocab.SL_CREATION_DATE_PROPERTY);
	Property slCreationTimeProp = docsModel.getProperty(SLVocab.SL_CREATION_TIME_PROPERTY);

	// il y a des docs avec 2 sl:creationDate.
	// Réglons ça
	ResIterator ite = docsModel.listSubjectsWithProperty(slCreationDateProp);
	List<Resource> docs = ite.toList();
	for (Resource doc : docs) {
		StmtIterator sit = doc.listProperties(slCreationDateProp);
		// faudrait ne garder que la 1ere, mais c pénible : on garde une au hasard
//		List<Statement> dates = sit.toList();
//		if (dates.size() > 1) {
//		}
		// faudrait aussi s'occuper des sl:creationTime. pfff
		
		if (sit.hasNext()) sit.next(); // on passe la 1ere
		// on vire les suivantes
		if (sit.hasNext()) {
			docsModel.remove(sit);
		}
	}
	
	
	
	// l'idée était de lister les docs avec ça
	// MAIS les cr rdd n'ont pas de tags
	// Par ailleurs, plus lojn on ne prend que ceux qui ont un dc:title
	// Autant prendre ici tou ce qui a un dc:title
	
	// ResIterator ite = docsModel.listSubjectsWithProperty(docsModel.getProperty(SLVocab.HAS_KEYWORD_PROPERTY));
	ite = docsModel.listSubjectsWithProperty(dcDateProp);
	docs = ite.toList();
	for (Resource doc : docs) {
		
		// SEULEMENT POUR LES CR RDD
		System.out.println(doc.getURI());
		
		boolean doit = (doc.getURI().contains("/journal")) 
				|| (doc.getURI().contains("/cr/"))
				|| (doc.getURI().contains("/article"));

		System.out.println("doit " + doit + " : " + doc.getURI() + " doit");

		if (!doit) {
			continue;
		}
		
		
		Literal dcDate = JenaUtils.firstLiteralOfProperty(doc, dcDateProp);
		if (dcDate == null) {
			// continue; // peut pas arriver, vu que plus haut, on n'a pris que les docs qui ont une dc:date
			throw new RuntimeException("WTF???");
		}
		
		// ATTENTION, IL FAUT dcDate non null (et correct)

		// virer les formes pre-existantes de slCreationDate et time
		StmtIterator sit = doc.listProperties(slCreationDateProp);
		docsModel.remove(sit);
		sit = doc.listProperties(slCreationTimeProp);
		docsModel.remove(sit);
		
		// Mettre une date de creation à la date de publication
		docsModel.addLiteral(doc, slCreationDateProp, dcDate);
	}
}

}
