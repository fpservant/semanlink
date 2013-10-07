package net.semanlink.servlet;
import org.apache.struts.action.ActionForm;

/**
 * TODO voir doc multibox "warning" methode reset a ajouter 
 */
public class Form_AddAlias extends ActionForm {
public static final long serialVersionUID = 1;
private String okBtn, cancelBtn;
private String lang;
private String aliasLabel;
// pour le remove :
private String[] aliasuris;
public String[] getAliasuris() { return  this.aliasuris; }
public void setAliasuris(String[]s) { this.aliasuris = s; }

public String getAliasLabel() {
	return aliasLabel;
}
public void setAliasLabel(String aliasLabel) {
	this.aliasLabel = aliasLabel;
}
public String getOkBtn() { return this.okBtn; }
public void setOkBtn(String s) {this.okBtn = s; }
public String getCancelBtn() { return this.cancelBtn; }
public void setCancelBtn(String s) {this.cancelBtn = s; }

public String getLang() {
	return lang;
}
public void setLang(String lang) {
	this.lang = lang;
}
}
