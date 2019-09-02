package net.semanlink.servlet;
import org.apache.struts.action.ActionForm;

public class Form_Logon extends ActionForm {
public static final long serialVersionUID = 1;
private String username;
private String password;
public void reset() {
	username = null;
	password = null;
}
public String getUsername() {
	return username;
}
public void setUsername(String username) {
	this.username = username;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
}
