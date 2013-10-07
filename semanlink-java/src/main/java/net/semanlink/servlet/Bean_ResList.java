/* Created on 24 sept. 03 */
package net.semanlink.servlet;
import java.util.List;
/**
 * Classe utilitaire pour l'affichage des JSP presentant des listes de SLKeyword
 * (telles les enfants et parents d'un kw, ou les kws d'un doc).
 */
public class Bean_ResList {
public Bean_ResList() {}

public int size() {
	if (list == null) return 0;
	else return this.list.size();
}
/** La liste a proprement parler */
private List list;
public void setList(List list) { this.list = list; }
public List getList() { return this.list; }

/** La Resource a laquelle la liste se rapporte, eventuellement.
 *  (par exemple, le kw dont cette liste contient les parents).
 */
private String uri;
public void setUri(String s) { this.uri = s; }
public String getUri() { return this.uri; }

/** titre éventuel. */
private String title;
public void setTitle(String s) { this.title = s; }
public String getTitle() { return this.title; }

/** for instance "parents","children","friends","tags" */
private String field;
public void setField(String s) { this.field = s; }
public String getField() { return this.field; }


// POUR DETERMINER LES STYLES CSS

private String containerAttr;
/** permet de fixer le style a utiliser pour le container contenant la liste. */
public void setContainerAttr(String s) { this.containerAttr = s; }
/** le style a utiliser pour le container contenant la liste. */
public String getContainerAttr() {
	if (this.containerAttr != null) return this.containerAttr;
	return "id = \"verticalkwscontainer\"";
}

// A CHANGER AVANT D'utiliser
private String listStyleId;
/** le style a utiliser pour le tag <ul>. */
public void setListStyleId(String s) { this.listStyleId = s; }
/** permet de fixer le style a utiliser pour le tag <ul>. */
public String getListStyleId() { return this.listStyleId; }

/** identifiant éventuel (utilisé par kwblock.jsp pour le show/hide). */
private String identifier;
public void setIdentifier(String s) { this.identifier = s; }
public String getIdentifier() { return this.identifier; }

} // class


