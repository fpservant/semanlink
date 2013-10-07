package net.semanlink.util.servlet;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** @see template.jsp */
public class Jsp_Page {
/** documented only when using the constructor that includes the ServletContext */
protected ServletContext servletContext;
protected HttpServletRequest request;
protected HttpServletResponse response;
protected String title;
private String moreHeadersJsp = null;
private List<String> moreHeadersJspList = null;
private Set<String> onLoadEvents = null;

public Jsp_Page(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
	this.servletContext = servletContext;
  this.request = request;
  this.response = response;
}
public Jsp_Page(HttpServletRequest request, HttpServletResponse response) {
	this(null, request, response);
}

//
// 3-BOX-MODEL RELATED
//
protected String templateJsp = "/jsp/template.jsp";

protected String topBoxJsp = "/jsp/topBox.jsp";
protected String leftBoxJsp = "/jsp/leftBox.jsp";
protected String rightBoxJsp = "/jsp/rightBox.jsp";
protected String centerBoxJsp = "/jsp/centerBox.jsp";

public String getTemplateJsp() { return this.templateJsp; }
public void setTemplateJsp(String templateJsp) { this.templateJsp = templateJsp; }

/** JSP to be included by default in the top box of the "3 box model" layout */
public String getTopBoxJsp() { return this.topBoxJsp; }
/** JSP to be included by default in the left box of the "3 box model" layout */
public String getLeftBoxJsp() { return this.leftBoxJsp; }
/** JSP to be included by default in the right box of the "3 box model" layout */
public String getRightBoxJsp() { return this.rightBoxJsp; }
/** JSP to be included by default in the center box of the "3 box model" layout */
public String getCenterBoxJsp() { return this.centerBoxJsp; }
public void setTopBoxJsp(String jsp) { this.topBoxJsp = jsp; }
public void setLeftBoxJsp(String jsp) { this.leftBoxJsp = jsp; }
public void setRightBoxJsp(String jsp) { this.rightBoxJsp = jsp; }
public void setCenterBoxJsp(String jsp) { this.centerBoxJsp = jsp; }

//
// THINGS THAT CAN BE CUSTOMIZED IN template.jsp
//

/** JSP used to include headers in addition to those already defined in template.jsp (@see template.jsp) */
public String getMoreHeadersJsp() { return this.moreHeadersJsp; }
// public void setMoreHeadersJsp(String x) { this.moreHeadersJsp = x; } // added to be used by about.jsp to display sparql results as html
public void setMoreHeadersJsp(String x) { // added to be used by about.jsp to display sparql results as html
	addMoreHeadersJsp(x);
}
public List<String> getMoreHeadersJspList() { return this.moreHeadersJspList; }
public void addMoreHeadersJsp(String jspName) {
	if (this.moreHeadersJspList == null) this.moreHeadersJspList = new ArrayList<String>(8);
	this.moreHeadersJspList.add(jspName);
}

/** return null to just have a simple "<body>" tag. */
public String getBodyTag() { return null; }
/** @see Jsp_RDF2HTMLPage 
 * @deprecated see RDFIntoDiv */
public String loadRDFScript() { return null; }
// 2010-06

/** To add "onLoad" events to the body of the generated html page.
 * BEWARE that the corresponding javascript methods must be defined somewhere. You can use addMoreHeadersJsp for this purpose. */
public void addOnLoadEvents(String onLoadEvent) {
	if (this.onLoadEvents == null) this.onLoadEvents = new HashSet<String>(8);
	onLoadEvents.add(onLoadEvent);
}

public Set<String> getOnLoadEvents() { return this.onLoadEvents; }

//
//
//

/** BEWARE, documented only if this was created by the constructor with the servletContext argument
 * @since 2010-07 */
public ServletContext getServletContext() { return this.servletContext; }

/**
 * To my surprise, the "request" we get in a jsp IS NOT the request originally passed to the doPost 
 * or doGet method of the controler servlet (and that the controler passes to this for contruction).
 * So, in the jsp corresponding to this, request is not this.request - they have 
 * the same parameters and all the attributes the servlet has created (but there may be other attributes)
 * BUT : the results of following methods are changed: <ul>
 *      <li>getPathInfo</li>
 *      <li>getRequestURI</li>
 *      <li>getServletPath</li>
 *      <li>etc.</li>
 * </ul>
 * Example: this.request:
    REQ: org.apache.catalina.connector.RequestFacade@509df8
    req.getContextPath(): /diagweb
    req.getPathInfo(): /9106
    req.getPathTranslated(): C:\_fps\_fps\_java_DEV\eclipse\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\webapps\diagweb\9106
    req.getRequestURI(): /diagweb/dtc/9106
    req.getServletPath(): /dtc
    req.getRequestURL(): http://127.0.0.1:8080/diagweb/dtc/9106
    Parameters:
        val : http://sicg.tpz.renault.fr/diag/2006/06/ontologies/refop.owl#DiagTestResult_DF021_a_no
    Attributes:
        (depending on when we output this traces:)
        com.renault.sicg.diag.servlet.jsp : com.renault.sicg.diag.servlet.Jsp_Bvm@1b83048
 * request in the jsp:
    REQ: org.apache.catalina.core.ApplicationHttpRequest@ee20fe
    req.getContextPath(): /diagweb
    req.getPathInfo(): null
    req.getPathTranslated(): C:\_fps\_fps\_java_DEV\eclipse\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\webapps\diagweb\9106
    req.getRequestURI(): /diagweb/jsp/bvm.jsp
    req.getServletPath(): /jsp/bvm.jsp
    req.getRequestURL(): http://127.0.0.1:8080/diagweb/jsp/bvm.jsp
    Parameters:
        val : http://sicg.tpz.renault.fr/diag/2006/06/ontologies/refop.owl#DiagTestResult_DF021_a_no
    Attributes:
        javax.servlet.forward.request_uri : /diagweb/dtc/9106
        javax.servlet.forward.context_path : /diagweb
        javax.servlet.forward.servlet_path : /dtc
        javax.servlet.forward.path_info : /9106
        javax.servlet.forward.query_string : val=http%3A%2F%2Fsicg.tpz.renault.fr%2Fdiag%2F2006%2F06%2Fontologies%2Frefop.owl%23DiagTestResult_DF021_a_no
        com.renault.sicg.diag.servlet.jsp : com.renault.sicg.diag.servlet.Jsp_Bvm@1b83048

 * Questions :
 * - is it allowed to use this.request after the forward to the jsp (can't it be reused and modified
 * by the servlet engine?)
 * - 
 */
public HttpServletRequest getRequest() { return this.request; }

/** http://127.0.0.1:8080/diagweb/dtc/9106 */
public StringBuffer getRequestURL() { return this.request.getRequestURL(); }

/** /diagweb/dtc/9106 */
public String getRequestURI() { return this.request.getRequestURI(); }

/** /diagweb */
public String getContextPath() { return this.request.getContextPath(); }

/** /dtc */
public String getServletPath() { return this.request.getServletPath(); }

/** /9106 */
public String getPathInfo() {
    // return this.request.getPathInfo();
    return BasicServlet.getPathInfo_Patched(this.request);
}

public String getContextURL() throws MalformedURLException {
	return BasicServlet.getContextURL(this.request);
}

//
//
//

public void setTitle(String s) { this.title = s; }
public String getTitle() throws Exception { 
    if (this.title != null) return this.title;
    return "Untitled";
}

//
//
//


}

