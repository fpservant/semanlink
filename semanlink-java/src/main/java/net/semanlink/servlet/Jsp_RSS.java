/* Created on 13 nov. 2005 */
package net.semanlink.servlet;

import java.util.List;

import net.semanlink.semanlink.SLDocument;

/**
 * @author fps
 */
public interface Jsp_RSS {
public String getAbout() throws Exception;
public String getTitle() throws Exception;
public String getDescription() throws Exception;
public String getLink()  throws Exception;
public List getDocs() throws Exception;
public String getDate(SLDocument doc) throws Exception;
public String getComment(SLDocument doc) throws Exception;
public String getLink(SLDocument doc) throws Exception;
}