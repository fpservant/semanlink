package net.semanlink.servlet;
import javax.servlet.http.HttpSession;

import net.semanlink.semanlink.SLVocab;

import org.apache.struts.action.ActionForm;

/**
 * TODO voir doc multibox "warning" methode reset a ajouter 
 */
public class Form_Base extends ActionForm {
public static final long serialVersionUID = 1;
private String okBtn;
public String getOkBtn() { return this.okBtn; }
public void setOkBtn(String s) {this.okBtn = s; }

private String cancelBtn;
public String getCancelBtn() { return this.cancelBtn; }
public void setCancelBtn(String s) {this.cancelBtn = s; }

private String kw;
public String getKw() { return this.kw; }
public void setKw(String s) {this.kw = s; }

private String parent;
public String getParent() { return this.parent; }
public void setParent(String s) {this.parent = s; }

private String child;
public String getChild() { return this.child; }
public void setChild(String s) {this.child = s; }

private String uri;
public String getUri() { return this.uri; }
public void setUri(String s) {this.uri = s; }

private String docuri;
public String getDocuri() { return this.docuri; }
public void setDocuri(String s) {this.docuri = s; }

private String encodedDocUri;
public String getEncodedDocuri() { return this.encodedDocUri; }
public void setEncodedDocuri(String s) {this.encodedDocUri = s; }

private String folderuri;
public String getFolderuri() { return this.folderuri; }
public void setFolderuri(String s) {this.folderuri = s; }

private String kwuri;
public String getKwuri() { return this.kwuri; }
public void setKwuri(String s) {this.kwuri = s; }

private String property;
public String getProperty() {
	return this.property;
}
public void setProperty(String s) {this.property = s; }

private String selectProp;
public String getSelectProp() {
	return this.selectProp;
}
public void setSelectProp(String s) {this.selectProp = s; }

private String selectRdfType;
public String getSelectRdfType() {
	return this.selectRdfType;
}
public void setSelectRdfType(String s) {this.selectRdfType = s; }


private String lang;
public String getLang() {
	return this.lang;
}
public void setLang(String s) {this.lang = s; }

private String gopage;
public String getGopage() { return this.gopage; }
public void setGopage(String s) {this.gopage = s; }

private String value;
public String getValue() { return this.value; }
public void setValue(String s) {this.value = s; }

private String comment;
public String getComment() {
	return comment;
}
public void setComment(String comment) {
	this.comment = comment;
}

/** cf actionTreeorNot : tree ou descendants or children only */
/*private String mode = Jsp_Page.SHOW_DEFAULT_MODE; // ATTENTION, pour changer, changer aussi ds Jsp_Page.getDescendantsMode
public String getMode() { return this.mode; }
public void setMode(String s) {this.mode = s; }*/
private String childrenAs = DisplayMode.DEFAULT.getChildrenAs();
public String getChildrenAs() { return this.childrenAs; }
public void setChildrenAs(String s) { this.childrenAs = s; }

private boolean longListOfDocs = DisplayMode.DEFAULT.isLongListOfDocs();
public boolean getLongListOfDocs() { return this.longListOfDocs; }
public void setLongListOfDocs(boolean b) { this.longListOfDocs = b; }

private String imagesonly;
public String getImagesonly() { return this.imagesonly; }
public void setImagesonly(String s) {this.imagesonly = s; }
	
// TODO A VIRER
private String commonprop;
public String getCommonprop() { return this.commonprop; }
public void setCommonprop(String s) {this.commonprop = s; }

private String[] kwuris;
public String[] getKwuris() { return  this.kwuris; }
public void setKwuris(String[]s) { this.kwuris = s; }

private String attr;
public String getAttr() { return this.attr; }
public void setAttr(String s) { this.attr = s; }

private boolean editor;
public boolean getEditor() { return this.editor; }
public void setEditor(boolean b) { this.editor = b; }

private boolean edit;
public boolean getEdit() { return this.edit; }
public void setEdit(boolean b) { this.edit = b; }

//
//
//

/** il arrive qu'on modifie la sortProp de la session sans passer par les actions normales
 * -> on a alors besoin de mettre à jour le bean de la form pour que l'affichage soit correct.
 */
public static void sessionSortPropIntoForm_Base(HttpSession session) {
	// bordel qui fait chier avec la form des prefs : ce qui est sélectionné par défaut
	// (rq : j'ai dû ajouter ds struts config la form BaseForm à l'action showpref,
	// mais gaffe du coup, il suffit d'être allé sur la form, même sans valider
	// pour que sortProp du form beam soit doc)
	Form_Base fb = get(session);
	fb.setProperty(getSelectedSortPropInForm(session));
}

public static Form_Base get(HttpSession session) {
	Form_Base fb = (Form_Base) session.getAttribute("baseForm");
	if (fb == null) {
		fb = new Form_Base();
		session.setAttribute("baseForm", fb);
	}
	return fb;
}


public static String getSelectedSortPropInForm(HttpSession session) {
	String sortProperty = (String) session.getAttribute("net.semanlink.servlet.SortProperty");
	if (sortProperty == null) sortProperty = SLServlet.getJspParams().getDefaultSortProperty();
	String sortProp = null;
	for (int i = 0; i < SLVocab.COMMON_PROPERTIES.length; i++) {
		if (sortProperty.equals(SLVocab.COMMON_PROPERTIES[i].getUri())) {
			sortProp = SLVocab.COMMON_PROPERTIES[i].getName();
		}
	}
	if (sortProp == null) sortProp = "* Tags *";
	return sortProp;

}

private String docTitle;
public String getDocTitle() {
	return docTitle;
}
public void setDocTitle(String docTitle) {
	this.docTitle = docTitle;
}
}
