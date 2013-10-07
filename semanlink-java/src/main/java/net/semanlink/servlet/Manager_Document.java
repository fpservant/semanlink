/* Created on 3 avr. 2005 */
package net.semanlink.servlet;
import java.util.ArrayList;

/**
 * Main class to deal with creation of Jsp_Document
 * 
 * This class maintains a list of DocFactory - classes specialized in making Jsp_Document
 * from one kind of document.
 * How to use :
 * First, add DocFactories to the list of DocFactoryies (typically done at startup)
 */
public class Manager_Document {
private static Manager_Document self;
private ArrayList factoryList;
private DocumentFactory defaultFactory = new DocumentFactory();
private Manager_Document() {
	this.factoryList = new ArrayList();
}
public void add(DocumentFactory fact) {
	this.factoryList.add(fact);
}
public static Manager_Document getInstance() {
	if (self == null) self = new Manager_Document();
	return self;
}
public static DocumentFactory getDocumentFactory() {
	// TODO
	return getInstance().defaultFactory;
}
public static void setDocumentFactory(DocumentFactory f) {
	getInstance().defaultFactory = f;
}
}

