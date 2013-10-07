package net.semanlink.servlet;
import net.semanlink.util.html.HTMLPageDownload;

import org.apache.struts.action.ActionForm;

public class Form_Bookmark extends ActionForm {
public static final long serialVersionUID = 1;
private String docuri;
private String downloadfromuri;
private String cancelBtn;
private String bookmarkBtn;
private String bookmarkWithCopyBtn;
private String localDocBtn;
private String nir;
// private HTMLPageDownload download;
private String title;
private String lang;
private String via;
private String comment;
public void reset() {
	title = null;
	comment = null;
	downloadfromuri = null;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
public String getCancelBtn() { return this.cancelBtn; }
public void setCancelBtn(String s) {this.cancelBtn = s; }


public String getBookmarkBtn() {
	return bookmarkBtn;
}
public void setBookmarkBtn(String bookmarkBtn) {
	this.bookmarkBtn = bookmarkBtn;
}
public String getBookmarkWithCopyBtn() {
	return bookmarkWithCopyBtn;
}
public void setBookmarkWithCopyBtn(String bookmarkWithCopyBtn) {
	this.bookmarkWithCopyBtn = bookmarkWithCopyBtn;
}
public String getDocuri() {
	return docuri;
}
public void setDocuri(String docuri) {
	this.docuri = docuri;
}
public String getLocalDocBtn() {
	return localDocBtn;
}
public void setLocalDocBtn(String localDocBtn) {
	this.localDocBtn = localDocBtn;
}
/*public HTMLPageDownload getDownload() {
	return download;
}
public void setDownload(HTMLPageDownload download) {
	this.download = download;
}*/
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
public String getVia() {
	return via;
}
public void setVia(String via) {
	this.via = via;
}
public String getDownloadfromuri() {
	return downloadfromuri;
}
public void setDownloadfromuri(String downloadfromuri) {
	this.downloadfromuri = downloadfromuri;
}
// getNonInformationResourceUri
public String getNir() {
	return nir;
}
public void setNir(String nir) {
	this.nir = nir;
}
}
