package net.semanlink.util;
/*
 * Voir http://jakarta.apache.org/commons/httpclient/features.html
 * AND quand on a un problème avec le proxy
 * http://wiki.apache.org/jakarta-httpclient/FrequentlyAskedApplicationDesignQuestions#head-4808398f4e6d318df33672d886d1f27bd2845277
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;

/*
 * TODO : on creation, check whether we're able to connect to proxyhost. Voir
 * http://jakarta.apache.org/httpcomponents/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpConnection.html#setConnectionTimeout(int)
 */

/**
 * Wraps some functionnalities of the HttpClient class 
 */
public class  SimpleHttpClient {

//
// ATTRIBUTES
//
	
HttpClient client;

/** To avoid 403 (forbidden) error when connecting to a google search, or wikipedia, we must set the User-Agent in the request header. 
 *  "" is OK with both Google and Wikipedia (2007-01) */
private String userAgent = "semanlink";

//
// CONSTRUCTORS
//

public SimpleHttpClient() {
	// see http://hc.apache.org/httpclient-3.x/threading.html
	MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	this.client = new HttpClient(connectionManager);
}

/** to be used for connection going through a proxy. */
public SimpleHttpClient(String proxyHost, int proxyPort, String proxyUserName, String proxyPassword) {
  this();
	/*String secProviderName = "com.sun.crypto.provider.SunJCE";
	java.security.Provider secProvider = (java.security.Provider)Class.forName(secProviderName).newInstance();
	Security.addProvider(secProvider);*/

  if (proxyHost != null) {
	  this.client.getHostConfiguration().setProxy(proxyHost,proxyPort); 
	  /*List authPrefs = new ArrayList(2);
	  authPrefs.add(AuthPolicy.BASIC);
	  authPrefs.add(AuthPolicy.DIGEST);
	  // This will exclude the NTLM authentication scheme
	  this.client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);*/

	  if (proxyUserName != null) {
		  client.getState().setProxyCredentials(
		      new AuthScope(proxyHost, proxyPort, AuthScope.ANY_REALM),
		      new UsernamePasswordCredentials(proxyUserName, proxyPassword)
		  );
	  }
  }
}

public void finalize() {
	if (this.client != null) {
		((MultiThreadedHttpConnectionManager) this.client.getHttpConnectionManager()).shutdown();
	}
	try {
		super.finalize();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

//
// GETS 'N SETS
//

public HttpClient getHttpClient() { return this.client; }

/** To avoid 403 (forbidden) error when connecting to a google search, or wikipedia, we must set the User-Agent in the request header. 
 *  As of 2007-01, "" is OK with both Google and Wikipedia */
public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

//
// HEAD REQUESTS AND CONTEXT TYPE
//

/** send a HEAD request and return the content type 
 * To be used when you want to know the content type of url without actually downloading it
 * @throws IOException 
 * @throws HttpException */
public String getContentType(String url, boolean exceptionOnFail) throws HttpException, IOException {
	return getContentType(url, null, exceptionOnFail);
}

/**
 * Sends a HEAD request and returns the content type. 
 * To be used when you want to know the content type of url without actually downloading it.
 * Beware, may return null.
 * @throws IOException 
 * @throws HttpException */
public String getContentType(String url, String acceptHeader, boolean exceptionOnFail) throws HttpException, IOException {
	HeadMethod method = new HeadMethod(url);
	// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia
  method.setRequestHeader("User-Agent",this.userAgent);
  if (acceptHeader != null) method.setRequestHeader("accept",acceptHeader);
  method.setDoAuthentication(true);
  method.setFollowRedirects(true);
  try {
		// System.out.println("HTTP HEAD " + url);
    int status = client.executeMethod(method);
    if (status != HttpStatus.SC_OK) {
    	if (exceptionOnFail) {
    		throw new RuntimeException("HTTP HEAD method for " + url + " failed: " + method.getStatusLine());
    	} else {
    		System.out.println(("HTTP HEAD method for " + url + " failed: " + method.getStatusLine()));
    		return null;
    	}
    }
    Header type = method.getResponseHeader("Content-Type");
    String x = null;
    if (type != null) x = type.getValue();
    // System.out.println("Content-Type: " + x);
    return x;
  } catch (NoHttpResponseException e) {
  	if (!exceptionOnFail) {
  		return null;
  	} else {
  		throw e;
  	}
	} finally {
		method.releaseConnection();
	}
}

//
// RETURNING A GetMethod
//

public GetMethod newGetMethod(String url, String acceptHeader) {
  GetMethod method = new GetMethod(url);
	// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia
  method.setRequestHeader("User-Agent",this.userAgent);
  if (acceptHeader != null) method.setRequestHeader("accept",acceptHeader);
  // Tell the GET method to automatically handle authentication. The
  // method will use any appropriate credentials to handle basic
  // authentication requests.  Setting this value to false will cause
  // any request for authentication to return with a status of 401.
  // It will then be up to the client to handle the authentication.
  method.setDoAuthentication(true);
  method.setFollowRedirects(true); // Hmm : to see // TODO
  return method;
}

//cf "bridge" servlet
/** GetMethod to url, using all HTTP Headers of req. */
public GetMethod newGetMethod(String url, HttpServletRequest req) {
  GetMethod method = new GetMethod(url);
  
  // pass all HTTP headers of req into the GetMethod
	Enumeration headerNames = req.getHeaderNames();
	for(;headerNames.hasMoreElements();) {
		String headerName =  (String) headerNames.nextElement();
		Enumeration headerValues = req.getHeaders(headerName);
		for(;headerValues.hasMoreElements();) {
			method.addRequestHeader(headerName, (String) headerValues.nextElement());
		}
	}
  
	// This line to try to avoid 403 (forbidden) error when connecting to a google search, or wikipedia, if no user-agent is set
  Header userAgent = method.getRequestHeader("User-Agent");
  if ( (userAgent == null) || ( userAgent.getValue() == null) ) {
  	method.setRequestHeader("User-Agent",this.userAgent);
  }

  // Tell the GET method to automatically handle authentication. The
  // method will use any appropriate credentials to handle basic
  // authentication requests.  Setting this value to false will cause
  // any request for authentication to return with a status of 401.
  // It will then be up to the client to handle the authentication.
  method.setDoAuthentication(true); // Hmm : to see // TODO
  method.setFollowRedirects(true); // Hmm : to see // TODO
  return method;
}

//
//
//

/**
 * Opens the connection, reads the answer and releases connection. 
 * I wanted to have it returning simply the GetMethod, but
 * once the connection is released, method.getResponseBody() returns null.
 * (And we have to be sure that releaseConnection is called!)
 * We therefore have to completely read and store the response, hence the Response class to store the result
 * @param url
 * @param acceptHeader for instance "text/html", or "application/xml+rdf"
 */
public Response doGet(String url, String acceptHeader) throws HttpException, IOException {
  GetMethod method = newGetMethod(url, acceptHeader);
  try {
      // execute the GET
  		// System.out.println("HTTP GET " + url + " acceptHeader " + acceptHeader);
      int status = client.executeMethod(method);
      if (status != HttpStatus.SC_OK) {
        throw new RuntimeException("HTTP GET method for " + url + " failed: " + method.getStatusLine());
      }

      // InputStream in = get.getResponseBodyAsStream();
      // byte[] responseBody = method.getResponseBody();
      // String charSet = method.getResponseCharSet();
      // String contentType = method.getResponseHeader("Content-Type");

      Header type = method.getResponseHeader("Content-Type");
      String contentType = null;
      if (type != null) contentType = type.getValue();
      // System.out.println("content type : " + contentType);
      Response x = new Response(method.getResponseBody(), method.getResponseCharSet(), contentType);
      return x;

  } finally {
  	method.releaseConnection();
  }
}

public static class Response {
	private byte[] responseBody;
	private String charSet;
	private String contentType;
	Response(byte[] responseBody, String charSet, String contentType) {
		this.responseBody = responseBody;
		this.charSet = charSet;
		this.contentType = contentType;
	}
	public String getCharSet() {
		return charSet;
	}
	public String getContentType() {
		return contentType;
	}
	public byte[] getResponseBody() {
		return responseBody;
	}
	public InputStream getResponseBodyAsStream() {
		return new ByteArrayInputStream(responseBody);
	}
}

//
//
//

/** Writes the content received in a get to a file */
public void save(String url, String acceptHeader, File saveAsFile) throws IOException {	
  	File dir = new File(saveAsFile.getParent());
  	if (!dir.exists()) dir.mkdirs();
    OutputStream out = new FileOutputStream(saveAsFile);
    output(url, acceptHeader, out);
  	out.close();
}

/** Writes the content received in a get to an OutputStream. */
public void output(String url, String acceptHeader, OutputStream out) throws IOException {	
  GetMethod method = newGetMethod(url, acceptHeader);
  try {
      // execute the GET
      int status = client.executeMethod( method );
      if (status != HttpStatus.SC_OK) {
        throw new RuntimeException("HTTP get method failed: " + method.getStatusLine());
      }
    	CopyFiles.writeIn2Out(method.getResponseBodyAsStream(), out, new byte[1024]);
  } finally {
  	method.releaseConnection();
  }
}

// TODO : http return code ???
/** Writes the content received in a get to a HttpServletResponse. */
public void output(String url, String acceptHeader, HttpServletResponse res) throws HttpException, IOException {
	output(newGetMethod(url, acceptHeader), res);
}

public void output(String url, HttpServletRequest req, HttpServletResponse res) throws HttpException, IOException {
	output(newGetMethod(url, req), res);
}

private void output(GetMethod method, HttpServletResponse res) throws HttpException, IOException {
  try {
    // execute the GET
    int status = client.executeMethod( method );
    if (status != HttpStatus.SC_OK) {
      throw new RuntimeException("HTTP get method failed: " + method.getStatusLine());
    }
    
    // it is important to pass the http headers
    // For instance, this will preserve cache information
    
    // String charSet = method.getResponseCharSet();
    // Header contentType = method.getResponseHeader("Content-Type");
    Header[] headers = method.getResponseHeaders();
    for (int i = 0; i < headers.length; i++) {
    	Header header = headers[i];
    	// System.out.println("header/value: " + header.getName() +"/"+ header.getValue());
    	/* // Some HTTP headers (such as the set-cookie header) have values that can be decomposed into multiple elements.
    	HeaderElement[] elts = headers[i].getElements();
    	for (int j = 0; j < elts.length; j++) {
    		HeaderElement elt = elts[j];
    		System.out.println("SimpleHttpClient adding header " + elt.getName() + " : " + elt.getValue());
    	}*/
  		res.addHeader(header.getName(), header.getValue());
    }
  	CopyFiles.writeIn2Out(method.getResponseBodyAsStream(), res.getOutputStream(), new byte[1024]);
	} finally {
		res.getOutputStream().flush(); // ???
		method.releaseConnection();
	}	
}

//
// SAFE WAY TO HANDLE releaseConnection
//

/**
 * Wrapper for the GetMethod class of the HttpCLient package.
 * 
 * <p>Problem is: the recommended way to get the body of a response is through getResponseBodyAsStream(),
 * but  we have to be sure that the caller releases the connection after handling the response.</p>
 * 
 * How to use: subclass, implement doWhatYouWant(GetMethod), and run the execute() method.
 */

public abstract class GetMethodWrapper {
	protected String url;
	protected String acceptHeader;
	public GetMethodWrapper(String url, String acceptHeader) {
		this.url = url;
		this.acceptHeader = acceptHeader;
	}
	/**
	 * Executes the GetMethod, and then releases connection.
	 * for instance use method.getResponseBodyAsStream()
	 * BEWARE, the connection being released after execution of this method, the ResponseBodyAsStream will be
	 * automatically closed: if you need it, you have to consume it during exec of this method, not after
	 * @return
	 */
	public abstract void doWhatYouWant(GetMethod method);
	/**
	 * Executes the GetMethod, and then releases connection.
	 */
	public void execute() throws HttpException, IOException {
		GetMethod method = newGetMethod(url, acceptHeader);
	  try {
	      // execute the GET
	  		// System.out.println("HTTP GET " + url + " acceptHeader " + acceptHeader);
	      int status = client.executeMethod(method);
	      if (status != HttpStatus.SC_OK) {
	        throw new RuntimeException("HTTP GET method for " + url + " failed: " + method.getStatusLine());
	      }

	      // InputStream in = get.getResponseBodyAsStream();
	      // byte[] responseBody = method.getResponseBody();
	      // String charSet = method.getResponseCharSet();
	      // String contentType = method.getResponseHeader("Content-Type");

	      // Header type = method.getResponseHeader("Content-Type");
	      // String contentType = null;
	      // if (type != null) contentType = type.getValue();
	      // System.out.println("content type : " + contentType);
	      // Response x = new Response(method.getResponseBody(), method.getResponseCharSet(), contentType);
	      // return x;
	      doWhatYouWant(method);

	  } finally {
	  	method.releaseConnection();
	  }
	}
}

}

