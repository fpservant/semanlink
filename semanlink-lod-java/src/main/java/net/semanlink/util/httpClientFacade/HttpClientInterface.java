/* Created on 6 avr. 2012 */
package net.semanlink.util.httpClientFacade;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpClientInterface {
public void setUserAgent(String userAgent);
/**
 * Sends a HEAD request and returns the content type. 
 * To be used when you want to know the content type of url without actually downloading it.
 * Beware, may return null.
 * @throws IOException 
 * @throws HttpException */
public String getContentType(String url, String acceptHeader, boolean exceptionOnFail) throws Exception;

/** Writes the content received in a get to an OutputStream. */
// public void output(String url, String acceptHeader, OutputStream out) throws IOException;
public void output(String url, HttpServletRequest req, HttpServletResponse res) throws ServletException;


}
