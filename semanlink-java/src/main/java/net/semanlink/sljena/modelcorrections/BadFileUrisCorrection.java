package net.semanlink.sljena.modelcorrections;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import net.semanlink.semanlink.SLModel;
import net.semanlink.sljena.JenaUtils;
import net.semanlink.util.Util;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/** Corrige dans le fichier slFile les uri de protocol file qui ne seraient pas correctes.
 *  Retourne s'il y a eu des changements
 * 
 *  // TODO voir si il n'est plus possible de creer de nouveaux statements ds un model
 *  qui comprtent une mauvaise uri. ATTENTION c'est important puisqu'on ne fait la correction qu'une seule
 *  fois sur un fichier.
 */
public class BadFileUrisCorrection extends AbstractCorrection {
private SLModel slModel;

public BadFileUrisCorrection(SLModel slModel) {
	this.slModel = slModel;
	try {
		this.time = Util.shortDate2Long("2/12/2003", Locale.FRANCE);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

public boolean correctDocsModel(Model mod) {
	ResIterator resIte = mod.listSubjects();
	// pour ne pas risquer des pbs en modifiant le model en cours d'iteration :
	ArrayList al = new ArrayList();
	for (;resIte.hasNext();) {
		al.add(resIte.next());
	}
	boolean hasChanged = false;
	for (Iterator ite = al.iterator(); ite.hasNext();) {
		Resource res = (Resource) ite.next();
		String uri = res.getURI();
		try {
			new URI(uri);
		} catch (URISyntaxException e1) {
			try {
				URL url = new URL(uri);
				if ("file".equals(url.getProtocol())) {
					String filename = url.getFile();
					String newUri = this.slModel.filenameToUri(filename);
					if (!newUri.equals(uri)) {
						System.out.println("BadFileUrisCorrection ");
						System.out.println("    olduri : " +uri);
						System.out.println("    newuri : " +newUri);
						JenaUtils.changeResURI(mod, uri, newUri);
						hasChanged = true;
					}
				}
			} catch(MalformedURLException e) {
				// TODO : essayer autre chose pour modifier la res - ou la supprimer
				System.err.println("BadFileUrisCorrection.correctDocsModel " + uri + " : " + e);
			} catch (URISyntaxException e) {
				// TODO : essayer autre chose pour modifier la res - ou la supprimer
				System.err.println("BadFileUrisCorrection.correctDocsModel " + uri + " : " + e);
				e.printStackTrace();
			}
		}
	}
	// System.out.println("BadFileUrisCorrection returns " + hasChanged);
	return hasChanged;
}
}
