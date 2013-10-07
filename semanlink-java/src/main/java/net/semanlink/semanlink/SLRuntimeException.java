package net.semanlink.semanlink;
import java.io.*;
/**
 * Generic RuntimeException thrown by SemanLink methods.
 */
public class SLRuntimeException extends RuntimeException {
	public static final long serialVersionUID = 1;
	private Throwable t;
	public SLRuntimeException(Throwable t) {
		super();
	this.t = t;
}

public SLRuntimeException(String s) {
	super(s);
}

public void printStackTrace() {
	if (t != null) {
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
