/* Created on Apr 21, 2020 */
package net.semanlink.sljena;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.JenaException;

import net.semanlink.semanlink.SLDocUpdate;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLRuntimeException;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.servlet.SLServlet;
import net.semanlink.util.YearMonthDay;

class JDocUpdate extends SLDocUpdate {
	JModel mod;
	SLDocument doc;
	JFileBiModel bi;
	JDocUpdate(JModel mod, SLDocument doc) {
		this.mod = mod;
		this.doc = doc;
		String docUri = doc.getURI();
		try {
			bi = mod.getJFileBiModel4Docs(docUri);
		} catch (Exception e) { throw new RuntimeException(e) ; }
	}

	@Override
	public void close() throws JenaException, IOException, URISyntaxException {
		
		//
		// Handling of the SL_CREATION_DATE_PROPERTY
		//
		
		// if it's a new doc, add creation date and time
		// unless...
		boolean addCreationDate;
		if (!mod.hasSLCreationDate(doc)) {
			// first,
			// if this is a destruction (no more statement about doc): 
			// we don't want to add a creation date!
			// second, 
			// we don't want something that is just a local copy of a doc to be considered a doc (with properties such as
			// creation date etc.), hence the following trick	
			Resource res = ((JDocument) doc).getRes();
			int nbSta = 0;
			StmtIterator ite = res.listProperties ();
			for (;ite.hasNext();) {
				nbSta++;
				if (nbSta > 1) {
					break;
				}
				ite.next();
			}
			ite.close();
			if (nbSta == 0) {
				addCreationDate = false;
						
			} else if (nbSta == 1) {
				// one one statement. Is this only statement involving SLVocab.SOURCE_PROPERTY?
				ite = res.listProperties(mod.sourceProperty);
				boolean justALocalCopy = (ite.hasNext());
				ite.close();
				// only one statement, and it is SLVocab.SOURCE_PROPERTY
				if (justALocalCopy) {
					addCreationDate = false;
				} else {
					addCreationDate = true;
				}
				
			} else {
				addCreationDate = true;
			}
		} else {
			addCreationDate = false;
		}
			
		if (addCreationDate) {
			newDocEvent();
		}
		
		//
		//
		//
		
		bi.setTagNS(mod.getDefaultThesaurus().getBase()); // @findTagNS
		bi.save();
	}
	
	private void newDocEvent() throws IOException, URISyntaxException {
		// it's better to set the SL_CREATION_DATE first,
		// in order to choose the right file to write new doc to
		// (cf case where propertyUri is SL-CREATION_DATE: this happens with import from delicious:
		// we want to set the creation date (which has to be the first prop to be set for the doc)
		String today = (new YearMonthDay()).getYearMonthDay("-");
		addDocProperty(SLVocab.SL_CREATION_DATE_PROPERTY, today, null);

		//autres trucs à faire au on new doc (à part la sl creation date). 
		addDocProperty(SLVocab.SL_CREATION_TIME_PROPERTY,(new YearMonthDay()).getTimeString(), null);
		if (SLVocab.DATE_PARUTION_PROPERTY.equals(SLServlet.getDefaultSortProperty())) { // 2019-09 for sicg
			addDocProperty(SLVocab.DATE_PARUTION_PROPERTY, today, null);		
		} else {
			//
			File file = mod.getFile(doc.getURI());
			YearMonthDay modifDay = null;
			if (file != null) {
				if (file.exists()) {
					long lastModified = file.lastModified();
					// peut s'optimiser en testant sur la date plutôt que sa représentation en string
					modifDay = new YearMonthDay(new Date(lastModified));
					String modifVal = modifDay.getYearMonthDay("-");
					// if (modifVal.compareTo("2005-11-01") > 0) {
					addDocProperty(SLVocab.DATE_PARUTION_PROPERTY, modifVal, null); // 2020-03 TODO CHECK
					// }
				}
			}
		}
	}

	@Override
	public void addDocProperty(String propertyUri, String propertyValue, String lang) {
		propertyValue = mod.safe(propertyValue);
		lang = mod.safe(lang);
		String docUri = doc.getURI();
		if ((propertyValue != null) && (!("".equals(propertyValue)))) {
			try {			
//				if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propertyUri)) {
//					creationDateCase(propertyValue);
//					return;
//				}
				bi.add(docUri, propertyUri, propertyValue, lang);
			} catch (Exception e) { throw new SLRuntimeException(e); }
		}
	}

	@Override
	public void addDocProperty(String propertyUri, String objectUri) {
		String docUri = doc.getURI();
		if ((objectUri != null) && (!("".equals(objectUri)))) {
			if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propertyUri)) throw new IllegalArgumentException("Impossible to modify to such a thing " + SLVocab.SL_CREATION_DATE_PROPERTY);
			bi.add(docUri, propertyUri, objectUri);
		}
	}

	@Override
	public void addDocProperty(String propertyUri, String[] objectUris) {
		String docUri = doc.getURI();
		if (objectUris != null) {
			if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propertyUri)) throw new IllegalArgumentException("Impossible to modify to such a thing" + SLVocab.SL_CREATION_DATE_PROPERTY);
			for (int i = 0; i < objectUris.length;i++) {
				String objectUri = objectUris[i];
				if ((objectUri != null) && (!("".equals(objectUri)))) {
					bi.add(docUri, propertyUri, objectUri);
				}
			}
		}
	}

	@Override
	public void setDocProperty(String propertyUri, String objectUri) {
		if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propertyUri)) throw new IllegalArgumentException("Impossible to modify to such a thing" + SLVocab.SL_CREATION_DATE_PROPERTY);
		bi.set(doc.getURI(), propertyUri, objectUri);
	}
	
	@Override
	public void setDocProperty(String propertyUri, String propertyValue, String lang) {
		propertyValue = mod.safe(propertyValue);
		lang = mod.safe(lang);
		try {
//			if (SLVocab.SL_CREATION_DATE_PROPERTY.equals(propertyUri)) {
//				creationDateCase(propertyValue);
//				return;
//			}
			bi.set(doc.getURI(), propertyUri, propertyValue, lang);
		} catch (Exception e) { throw new SLRuntimeException(e); }
	}

	@Override public void removeStatement(String propertyUri, String propertyValue, String lang) {
		bi.remove(doc.getURI(), propertyUri, propertyValue, lang);
	}
	
	@Override public void removeStatement(String propertyUri, String objectUri) {
		bi.remove(doc.getURI(), propertyUri, objectUri);
	}

	// 2020-03 
//	private void creationDateCase(String yyyy_mm_dd) throws JenaException, IOException, URISyntaxException {
//		if (existsAsSubject(doc)) {
//			// it's forbidden to change sl:creationDate
//			// -- except if its a local document ???
//			// (as we use its uri to locate it and its corresponding sl.rdf file) 
//			if (!isLocalDocument(doc.getURI())) throw new IllegalArgumentException("Impossible to modify " + SLVocab.SL_CREATION_DATE_PROPERTY);
//		}
//		String docUri = doc.getURI();
//		// we have to set the SL_CREATION_DATE first,
//		// in order to choose the right file to write new doc to
//		// (cf case where propertyUri is SL-CREATION_DATE: this happens with import from delicious:
//		// we want to set the creation date (which has to be the first prop to be set for the doc)
//		JenaUtils.add(mod.getDocsModel(), docUri, SL_CREATION_DATE_PROPERTY, yyyy_mm_dd, null);
//		// now it's ok to find the file where to save statement
//		JFileModel smallJFileModel = getJFileModel4Docs(docUri);
//		JenaUtils.add(smallJFileModel.getModel(), docUri, SL_CREATION_DATE_PROPERTY, yyyy_mm_dd, null);
//		// others things to do on newDocEvent:
//		bi = new JFileBiModel(mod.getDocsModel(), smallJFileModel);
//		moreOnDocCreation(docUri, bi);
//	}
}
