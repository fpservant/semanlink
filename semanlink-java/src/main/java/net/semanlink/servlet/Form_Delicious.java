package net.semanlink.servlet;
import org.apache.struts.action.ActionForm;

public class Form_Delicious extends ActionForm {
private String user;
private String password;
public void reset() {
	user = null;
	password = null;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getUser() {
	return user;
}
public void setUser(String user) {
	this.user = user;
}
}
