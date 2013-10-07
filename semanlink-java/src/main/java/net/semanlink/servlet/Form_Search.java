/* Created on 17 mai 2005 */
package net.semanlink.servlet;

import org.apache.struts.action.ActionForm;

/**
 * @author fps
 */
public class Form_Search extends ActionForm {
private String text;
private String okBtn;
public String getOkBtn() { return this.okBtn; }
public void setOkBtn(String s) {this.okBtn = s; }

public String getText() {
	return text;
}
public void setText(String text) {
	this.text = text;
}
}
