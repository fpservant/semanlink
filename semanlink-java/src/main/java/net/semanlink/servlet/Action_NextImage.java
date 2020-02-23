package net.semanlink.servlet;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import net.semanlink.semanlink.*;
/**
 * Action demandant de l'affichage d'une image au sein de la page en cours.
 * Cette image est definie par son uri
 * S'il y a un param "prev", retourne la précédente plutôt que la suivante
 */
public class Action_NextImage extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	ActionForward x = null;
	try {
		Jsp_Page jsp = null;
		
		// BOF BOF BOF si ca marche c'est vraiment du hasard
		if ((jsp = handleKwUriParam(request)) != null) {
		} else if ((jsp = handlePptyUriParam(request)) != null) {
		} else if ((jsp = handleKwUriParam(request)) != null) {
		} else if ((jsp = handleDocUriParam(request)) != null) {
		} else if ((jsp = handleKwUrisParam(request)) != null) {
		} else {
			throw new SLRuntimeException("Neither keyword nor document in request.");
		} 
		// jsp != null :
		x = mapping.findForward("continue");

		// prochaine image a afficher
		int nextImageIndex = -1;
		// - quelle est la liste des images ?
		SLModel mod = SLServlet.getSLModel();
		Bean_DocList docList = jsp.getDocList();
		boolean prevInsteadOfNext = (request.getParameter("prev") != null);
		String s = null;
		int currentImageIndex = -1;
		s = request.getParameter("currentimageindex"); // index de l'image qui est affichee
		if (s != null) {
			currentImageIndex = Integer.parseInt(s);
		}
		if (currentImageIndex < 0) { // index de l'image en cours non documente (parce que a ete affiche suite
			// a un clic dans la liste, et pas sur le btn next)
			s = request.getParameter("currentimage"); // l'image qui est affichee
			if (s != null) {
				// !!!!!
				// JE NE COMPRENDS PAS POURQUOI MAIS,
				// alors que currentImageUri a ete encode, il ne faut pas ici le decoder.
				// VOIR AUSSI ds Action_ShowDocument
				// String currentImageUri = java.net.URLDecoder.decode(s,"UTF-8");
				String currentImageUri = s;
				SLDocument currentImage = mod.getDocument(currentImageUri);
				// nextImageIndex = docList.getNextImageIndex(currentImage); // passer l'uri devrait suffir`
				nextImageIndex = indexToDisplay(prevInsteadOfNext, currentImage, docList);
			} else {
				// pas encore d'image : prendre la 1ere -- ne peut pas arriver
				nextImageIndex = docList.getFirstImageIndex();
			}
		} else { // currentImageIndex documente
			//nextImageIndex = docList.getNextImageIndex(currentImageIndex);
			nextImageIndex = indexToDisplay(prevInsteadOfNext, currentImageIndex, docList);
		}
		SLDocument nextImage;
		if (nextImageIndex < 0) {
			nextImage = null;
		} else {
			nextImage = docList.getDoc(nextImageIndex);
		}
		jsp.setImageToBeDisplayed(nextImage, nextImageIndex);

	} catch (Exception e) {
	    return error(mapping, request, e );
	}
	return x;
} // end execute

protected int indexToDisplay(boolean prevInsteadOfNext, SLDocument currentImage, Bean_DocList docList) {
	if (prevInsteadOfNext) {
		return docList.getPrevImageIndex(currentImage); // passer l'uri devrait suffir
	} else {
		return docList.getNextImageIndex(currentImage); // passer l'uri devrait suffir
	}
}
protected int indexToDisplay(boolean prevInsteadOfNext, int currentImageIndex, Bean_DocList docList) {
	if (prevInsteadOfNext) {
		return docList.getPrevImageIndex(currentImageIndex);
	} else {
		return docList.getNextImageIndex(currentImageIndex);
	}
}


} // end Action
