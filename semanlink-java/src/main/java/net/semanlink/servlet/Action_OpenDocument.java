/* Created on 19 oct. 2004 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


// ça marche ????

/**
 * Pour être utilisé ds la barre de sicg, pour passer d'un doc vu ds sl au doc lui même (lien retour de metadata)
 * On ne peut passer ds le javascript que l'url ds sl, il faut en retirer l'url du doc
 */
public class Action_OpenDocument extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  ActionForward x = null;
  try {
    String sluri = request.getParameter("sluri"); // l'url du doc ds sl
	
	// Had in Action-ShowDocument : JE NE COMPRENDS PAS POURQUOI MAIS,
	// alors que docuri a ete encode, il ne faut pas ici le decoder.
	// VOIR AUSSI ici + bas et ds Action_NextImage et Action_ShowKeyword
	
	// docuri = URLUTF8Encoder.decode(docuri);
	// docuri = URLDecoder.decode(docuri, "UTF-8");

    // request.setAttribute("openrealdoc", "true");
	x = new ActionForward(sluri);
  } catch (Exception e) {
	    return error(mapping, request, e );
  }
  return x;
} // end execute
} // end Action
