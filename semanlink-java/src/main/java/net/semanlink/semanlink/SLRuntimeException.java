package net.semanlink.semanlink;
import java.io.*;
import java.net.URISyntaxException;
/**
 * Generic RuntimeException thrown by SemanLink methods.
 */
public class SLRuntimeException extends RuntimeException {
public static final long serialVersionUID = 1;
private Throwable t;
// 2025-0
/** si on veut décider du http error code à retourner au client
 500 par défaut
*/
private int http_error_code;

public SLRuntimeException(Throwable t) {
	this(t, 500);
}

public SLRuntimeException(Throwable t, int http_error) { // 2025-01
	super();
	this.t = t;
	this.http_error_code = http_error;
}

public Throwable getOrigine() {
	if (this.t != null) {
		Throwable cause = this.t.getCause();
		if (cause != null) {
			return cause;
		}
	}
	return this.t;
}

public int toHttpErrorCode() { // 2025-01
	return this.http_error_code;
}

public SLRuntimeException(String s) {
	super(s);
}

public SLRuntimeException(String s, Throwable t) {
	super(s, t);
}

public String toString() { // 2025-01
	Throwable cause = getOrigine();
	if (cause != null) {
		return getClass().getName() + ": " + cause.toString();
	}
	return super.toString();
}

public void printStackTrace() {
	if (t != null) {
		System.err.println(this.getMessage());
	  t.printStackTrace();
	} else {
	  super.printStackTrace();
	}
}

public void printStackTrace(PrintStream s) {
	if (t != null) {
	  t.printStackTrace(s);
	} else {
	  super.printStackTrace(s);
	}
}

public void printStackTrace(PrintWriter s) {
  	if (t != null) {
	  t.printStackTrace(s);
	} else {
	  super.printStackTrace(s);
	}
}
}
