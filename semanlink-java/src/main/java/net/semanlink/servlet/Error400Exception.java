/* Created on Mar 28, 2020 */
package net.semanlink.servlet;

/** an exception that should return a 400 error to client */
public class Error400Exception extends RuntimeException {
	public Error400Exception(String mess) {
		super(mess);
	}
}
