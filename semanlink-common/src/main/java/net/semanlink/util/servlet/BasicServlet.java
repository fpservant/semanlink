package net.semanlink.util.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import net.semanlink.util.*;

/**
 * DEVELOPER, TAKE CARE: When overriding doGet or doPost,
 * don't forget at the very beginning of the method: 
 * req.setCharacterEncoding("UTF-8");
 */
public class BasicServlet extends HttpServlet {
/* The folder inside the web app folder that contains the JSP. */
// protected String JSPFolder = "/jsp/";

/**
 * Handles a "home page" 
 * 
 * and act as an example of what has to be done at the very beginning
 * of doGet (namely setting the character encoding to UTF-8).
 * 
 * The handling of the "home page" requires in web.xml something such as:
 * <pre>
	<servlet-mapping>
		<servlet-name>RDCServlet</servlet-name>
		<url-pattern>/home</url-pattern>
	</servlet-mapping>

	<!--  The welcome file list is defined in such a way 
	that the base URL for the web application will be served by the servlet, without redirection.
	Needs a corresponding servlet-mapping ("/home")
	<welcome-file-list>
		<welcome-file>home</welcome-file>
	</welcome-file-list>
 </pre>
 */
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    req.setCharacterEncoding("UTF-8");
    // don't do that here (furthermore, you may want to set it to something else!)
  	// res.setContentType("text/html; charset=UTF-8  ");
    String servletPath = req.getServletPath();
    if ("/home".equals(servletPath)) { // @find welcome-file-list (in web.xml)
    	forward2Jsp(req, res, homePage(req, res));
    } else {
      throw new RuntimeException("Unexpected servletPath in GET: " + servletPath);
    }
}

/**
 * Example of a doPost method. 
 * 
 * Example of what should be done at the very beginning
 * of doPost (namely setting the character encoding to UTF-8)
 */
public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    req.setCharacterEncoding("UTF-8");
    // don't do that here (furthermore, you may want to set it to something else!)
  	// res.setContentType("text/html; charset=UTF-8  ");
    String servletPath = req.getServletPath();
    throw new RuntimeException("Unexpected servletPath in POST: " + servletPath);
}

//
// HOME PAGE
//

protected Jsp_Page homePage(HttpServletRequest req, HttpServletResponse res) {
	return new Jsp_Page(req, res);
}

//
// PARSING OF REQUEST RELATED
//

/** 
 * To correct a problem with HttpServletRequest.getPathInfo. 
 * for http://127.0.0.1:8080/semanlink/tag/afrique
 * getPathInfo(req) returns /afrique
 * but  when we requested for http://127.0.0.1:8080/semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * (that is to say http://127.0.0.1:8080/semanlink/tag/Λεωνίδας.html)
 * returns /Î?ÎµÏ?Î?Î¯Î´Î±Ï?.html 
 * instead of /%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
 * Hence this patch.
 */
public static String getPathInfo_Patched(HttpServletRequest req) {
	/*
	 * req.getContextPath(): /semanlink
	 * req.getServletPath(): /tag
	 * req.getRequestURI(): /semanlink/tag/%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82.html
	 */
	int n = req.getContextPath().length() + req.getServletPath().length();
	return req.getRequestURI().substring(n);
}

/**
 * Context URL
 * for instance http://127.0.0.1:8080/semanlink or http://www.semanlink.net
 */
public static String getContextURL(HttpServletRequest req) throws MalformedURLException {
	return Util.getContextURL(req);
}

//
// INIT RELATED
//

/** Dealing with init parameters: first try to get the param in context, if not found, look in config. 
 *  There are two kinds of init parameters in a web application:<ul>
 *  <li>those that can or should be defined by the person who deploys the web app</li>
 *  <li>those that are set by the developer of web application (included in the web.xml)</li>
 *  </ul>
 *  (In the general case, web app are deployed as war, and there is no way for
 *  the person deploying the application to change parameters in the web.xml. That's why
 *  parameters that must be set when deploying the app are to be defined in its context)
 */
public String getInitParameter(String paramName) {
    ServletConfig servletConfig = getServletConfig();
    ServletContext servletContext = servletConfig.getServletContext();
    String s;
    s = servletContext.getInitParameter(paramName);
    if ((s == null) || ("".equals(s))) s = servletConfig.getInitParameter(paramName);
    // if ((s == null) || ("".equals(s))) throw new RuntimeException("InitParameter " +  paramName + " not documented");
    if ("".equals(s)) s = null;
    return s;
}

//
// DEBUG RELATED
//

/** 
 * With URIs containing encoded characters, req.getPathInfo() is not correct (at least, it's not
 * what we want) (tested with Tomcat 5.5). Hence we cannot use it to get, at least directly, the tag from the URL.
 *
 * http://127.0.0.1:9080/semanlink/tag/%CE%91%E1%BC%B4%CE%B1%CF%82.html;jsessionid=D24B65AEA4A9411101B99E483512CF0D?resolvealias=true
 * Safari or IE7 displays in the nav bar:
 * http://127.0.0.1:9080/semanlink/tag/Αἴας.html;jsessionid=D44F9A6ACCA4520B0524539870969849?resolvealias=true
 * 
 * output in Tomcat 5.5, called from Safari:
 * <pre>
 * req.getContentType(): null
 * req.getRequestURL(): http://127.0.0.1:9080/semanlink/tag/%CE%91%E1%BC%B4%CE%B1%CF%82.html
 * req.getRequestURI(): /semanlink/tag/%CE%91%E1%BC%B4%CE%B1%CF%82.html
 * req.getContextPath(): /semanlink
 * req.getServletPath(): /tag
 * req.getPathInfo(): /Î?á?´Î±Ï?.html // for "a%20b.html", we would get "/a b.html"
 * req.getPathTranslated(): /Users/fps/_fps/-JavaDev/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/webapps/semanlink//Î?á?´Î±Ï?.html
 * req.getQueryString: resolvealias=true
 * net.semanlink.util.Util.getContextURL(req): http://127.0.0.1:9080/semanlink
 * Parameters:
 * 		resolvealias : true
 * HTTP Headers:
 * 		accept : * / * // white spaces added to prevent ending java comment
 * 		accept-language : fr
 * 		accept-encoding : gzip, deflate
 * 		cookie : __utmz=96992031.1183238253.1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); s_pers=%20s_vsn_nytimesglobal_1%3D1418197714452%7C1485122889093%3B%20s_vnum1%3D1185646310250%257C5%7C1501265510250%3B; fbbb_=1555185893.1.1183410405653; __utma=96992031.282475249.1183238253.1188913573.1189890503.8
 * 		referer : http://127.0.0.1:9080/semanlink/sl/home;jsessionid=D24B65AEA4A9411101B99E483512CF0D
 * 		user-agent : Mozilla/5.0 (Macintosh; U; Intel Mac OS X; fr) AppleWebKit/419.3 (KHTML, like Gecko) Safari/419.3
 * 		if-modified-since : Mon, 15 Oct 2007 19:55:30 GMT
 * 		connection : keep-alive
 * 		host : 127.0.0.1:9080
 * 
 * </pre>
 * Note that the session id is NOT in the req.foo() (but it is in the referer)
 * 
 */
public static void printRequestInfo(HttpServletRequest req) {
	System.out.println("req.getMethod(): " + req.getMethod());
	System.out.println("req.getCharacterEncoding(): " + req.getCharacterEncoding());
	System.out.println("req.getContentType(): " + req.getContentType());
  System.out.println("req.getContextPath(): " + req.getContextPath());
  System.out.println("req.getPathInfo(): " + req.getPathInfo());
  System.out.println("req.getPathTranslated(): " + req.getPathTranslated());
  System.out.println("req.getQueryString: " + req.getQueryString());
  System.out.println("req.getRequestURI(): " + req.getRequestURI());
  System.out.println("req.getRequestURL(): " + req.getRequestURL());
  System.out.println("req.getServletPath(): " + req.getServletPath());
  // System.out.println("net.semanlink.util.Util.getContextURL(req): " + net.semanlink.util.Util.getContextURL(req));
  // System.out.println("CoolUriServlet SERVLET session id :" + req.getSession().getId());
  HttpSession ses = req.getSession();
	System.out.println("session: " + ses);
  /* if (ses != null) {
  	System.out.println("session edit ? " +req.getSession().getAttribute("net.semanlink.servlet.edit"));
  } */
  System.out.println("Parameters:");
  Enumeration<String> e = req.getParameterNames();
  for (;e.hasMoreElements();) {
      String s = e.nextElement() ;
      // System.out.println("\t"+s+" : " + req.getParameter(s));
      String[] vals = req.getParameterValues(s);
      if (vals.length == 1) {
      	System.out.println("\t"+s+" : " + vals[0]);
      } else {
      	System.out.println("\t"+s+" : ");
	      for(String val : vals) {
	      	System.out.println("\t\t" + val);
	      }
      }
  }
  System.out.println("HTTP Headers:");
  Enumeration<String> headers = req.getHeaderNames();
  for (;headers.hasMoreElements();) {
      String name = headers.nextElement();
      e = req.getHeaders(name);
      for (;e.hasMoreElements();) {
          System.out.println("\t"+name+" : " + e.nextElement());
      }
  }
}

public static String getReferer(HttpServletRequest req) { // 2025-01
	Enumeration<String> e = req.getHeaders("referer");
	if (e != null && e.hasMoreElements()) {
		return e.nextElement();
	} else {
		return "";
	}
}

//
// CACHE RELATED
//

public static void preventCaching(HttpServletResponse res) {
  // Set to expire far in the past.
  res.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");

  // Set standard HTTP/1.1 no-cache headers.
  res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

  // Set IE extended HTTP/1.1 no-cache headers
  res.addHeader("Cache-Control", "post-check=0, pre-check=0");

  // Set standard HTTP/1.0 no-cache header.
  res.setHeader("Pragma", "no-cache");
}

//
// LOOK OF PAGES RELATED
//

protected void forward2Jsp(HttpServletRequest req, HttpServletResponse res, Jsp_Page jsp) throws ServletException, IOException {
  req.setAttribute("jsp", jsp);
  RequestDispatcher requestDispatcher = req.getRequestDispatcher(jsp.getTemplateJsp());
  requestDispatcher.forward(req, res);
}

//
//
//

public static void writeFile2ServletResponse(File source, ServletResponse res) throws IOException {
	// we do not want to close the res OutputStream
	// CopyFiles.writeFile2OutputStream(source, res.getOutputStream(), new byte[res.getBufferSize()]);
	try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
		CopyFiles.writeIn2Out(in, res.getOutputStream(), new byte[res.getBufferSize()]);
	}
}

}
