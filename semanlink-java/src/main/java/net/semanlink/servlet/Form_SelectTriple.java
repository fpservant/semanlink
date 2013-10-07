package net.semanlink.servlet;
import org.apache.struts.action.ActionForm;

/**
 * TODO voir doc multibox "warning" methode reset a ajouter 
 */
public class Form_SelectTriple extends ActionForm {
private String okBtn, cancelBtn;
private String s,p,o,lang;
public String getOkBtn() { return this.okBtn; }
public void setOkBtn(String s) {this.okBtn = s; }
public String getCancelBtn() { return this.cancelBtn; }
public void setCancelBtn(String s) {this.cancelBtn = s; }

public void setS(String s) { this.s = s; }
public String getS() { return this.s; }
public String getO() { return o; }
public void setO(String o) { this.o = o; }
public String getP() { return p; }
public void setP(String p) { this.p = p; }
public String getLang() {
	return lang;
}
public void setLang(String lang) {
	this.lang = lang;
}
}
