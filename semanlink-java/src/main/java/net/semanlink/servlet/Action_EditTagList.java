/* Created on 7 mai 2006 */
package net.semanlink.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLThesaurus;
import net.semanlink.semanlink.SLModel.Label2KeywordMatching;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


// voir Action_GoKeyword pourquoi les 2 ????? // TODO

public class Action_EditTagList extends BaseAction {
public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
  try {
  	if (!getSessionEditState(request)) {
  		throw new RuntimeException("Action forbidden when not in edit mode");
  	} else {
	    SLModel mod = SLServlet.getSLModel();
	    // add, remove, replace
	    String btn = request.getParameter("btn");
	    if (request.getParameter("cut") != null) {
	  		btn = "cut";
	    } else if (request.getParameter("remove") != null) {
	    		btn = "remove";
	    } else if (request.getParameter("add") != null) {
	    		btn = "add";
	    } else if (request.getParameter("paste") != null) {
	  		btn = "paste";
	    } else {
				throw new RuntimeException("Unexpected null btn");    	
	    }
	    // children, parents, kws
	    String field = request.getParameter("field");
	
	    // the target of the action : one of kw or doc 
	    SLKeyword kw = null;
	    SLDocument doc = null;
	    // documenté si c'est un kw qu'on modifie:
	    String kwuri = request.getParameter("kwuri");
	    if (kwuri != null) {
	    		kw = mod.getKeyword(kwuri);
	    }
	    // documenté si c'est un doc qu'on modifie:
	    String docuri = request.getParameter("docuri"); // le nom était encodedDocUri, avec une question: pourquoi ? -- si on ne decode pas plus bas ? strange voir ailleurs
	    if (docuri != null) {
	    		doc = mod.getDocument(docuri); // de qui ont retire
	    }
	    
	    if ((kw == null) && (doc == null)) {
	    		throw new RuntimeException("No doc nor tag documented in request");
	    }
	
	    if ( (btn.equals("remove")) || (btn.equals("cut")) ) {
		    // les retirés:
				String[] kws = request.getParameterValues("kwuris");
				if ((kws != null) && (kws.length > 0)) {
					SLKeyword[] slKws = new SLKeyword[kws.length];
					for (int i = 0; i < kws.length; i++) {
						slKws[i] = mod.getKeyword(java.net.URLDecoder.decode(kws[i],"UTF-8"));
					}
					if (doc != null) {
						mod.removeKeywords(doc, slKws);
					} else {
						if (field.equals("children")) {
							mod.removeChildren(kw, slKws);
						} else if (field.equals("parents")) {
							mod.removeParents(kw, slKws);
						} else if (field.equals("friends")) {
							mod.removeFriends(kw, slKws);
						} else {
							throw new RuntimeException("Unexpected field: " + field);
						}
					}
					if (btn.equals("cut")) {
						request.getSession().setAttribute("net.semanlink.servlet.ClipboardKeyword", slKws);					
					}
				}
				
	    } else if (btn.equals("add")) {
			  String kwLabel = request.getParameter("kwlabel");
	    	/*System.out.println("Action_EditTagList " + kwLabel);
	    	for (int i = 0; i < kwLabel.length(); i++) {
	    		System.out.println(kwLabel.charAt(i) + " " + (int) kwLabel.charAt(i));
	    	}
	    	System.out.println();*/
				if (doc != null) {
				    mod.addKeyword(doc, kwLabel, null); // find 2020-04 label2kwcreation
				} else {
					// 2020-04: there may be more than one KW associated to a label
					// SLKeyword addedKw = kwLabel2Kw(kwLabel, mod, mod.kwUri2Thesaurus(kw.getURI()));
					SLKeyword[] addedKws = kwLabel2Kw(kwLabel, mod, mod.kwUri2Thesaurus(kw.getURI()));
					for (SLKeyword addedKw : addedKws) {
						if (field.equals("children")) {
							mod.addChild(kw, addedKw);
						} else if (field.equals("parents")) {
							mod.addParent(kw, addedKw);
						} else if (field.equals("friends")) {
							mod.addFriend(kw, addedKw);
						} else {
							throw new RuntimeException("Unexpected field: " + field);
						}
					}
				}
				
	    } else if (btn.equals("paste")) {
	    	String pasteToUri = kwuri;
	    	if (pasteToUri == null) pasteToUri = docuri;
	    	doPaste(pasteToUri, field, request.getSession(), mod);
	    	
	    } else {
				throw new RuntimeException("Unexpected btn: " + btn);    	
	    }
  	}
    // POST REDIRECT
    /*
		if (kw != null) {
			request.setAttribute("net.semanlink.servlet.jsp", new Jsp_Keyword(kw, request));
		} else { // (doc != null) already tested
			getJsp_Document(doc, request);
		}
    return mapping.findForward("continue");
    */
		// The modified resource is the referer
		// note: referer contains sessionId, if cookies are off (and a session maintained)
		// If we use it in a sendRedirect, we won't have to add it
		String referer = request.getHeader("referer");
   	response.sendRedirect(response.encodeRedirectURL(referer));
   	return null;

  } catch (Exception e) {
    return error(mapping, request, e );
  }
} // end execute

 /* (Rq : pour l'ajout d'un kw à un doc, la méthode est dans SLModel.)*/

/** Retourne le kw pour le label passé en argument, dans le thesaurus auquel appartient kw.
 *  Crée le kw au besoin.
 */
protected SLKeyword[] kwLabel2Kw(String label, SLModel mod, SLThesaurus thesaurus) throws Exception {
	Label2KeywordMatching match = mod.label2KeywordMatch(label, mod.getDefaultThesaurus().getURI(), null);
	return  match.getKwsCreatingIfNecessary();
}

static void doPaste(String pasteToUri, String field, HttpSession session, SLModel mod) throws Exception {
	// SLKeyword pastedKeyword = (SLKeyword) request.getSession().getAttribute("net.semanlink.servlet.ClipboardKeyword");
	SLKeyword[] clipboardKeywords = (SLKeyword[]) session.getAttribute("net.semanlink.servlet.ClipboardKeyword");
		
	if ("tags".equals(field)) {
		SLDocument doc = mod.getDocument(pasteToUri);
		mod.addKeyword( doc, clipboardKeywords);
	} else {
		// paste to a keyword
		if ("friends".equals(field)) {
			SLKeyword kw = mod.getKeyword(pasteToUri);
			for (int i = 0; i < clipboardKeywords.length;i++) {
				mod.addFriend(kw, clipboardKeywords[i]);
			}
		} else {
			String[] pastedUris = new String[clipboardKeywords.length];
			for (int i = 0; i < clipboardKeywords.length;i++) {
				pastedUris[i] = clipboardKeywords[i].getURI();
			}
			if ("children".equals(field)) {
				mod.addParentChildLink(pasteToUri, pastedUris);
			} else if ("parents".equals(field)) {
				mod.addParentChildLink(pastedUris, pasteToUri);
			} else {
				throw new IllegalArgumentException("Unexpected field value");
			}						
		}
	}
}
} // end Action
