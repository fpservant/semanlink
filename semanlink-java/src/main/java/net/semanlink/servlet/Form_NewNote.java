package net.semanlink.servlet;
import org.apache.struts.action.ActionForm;

public class Form_NewNote extends ActionForm {
public static final long serialVersionUID = 1;
private String title;
private String lang;
private String comment;
private String note;
public void reset() {
	title = null;
	comment = null;
	note = null;
}
public String getComment() {
	return comment;
}
public void setComment(String comment) {
	this.comment = comment;
}
public String getLang() {
	return lang;
}
public void setLang(String lang) {
	this.lang = lang;
}
public String getNote() {
	return note;
}
public void setNote(String note) {
	this.note = note;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
}
