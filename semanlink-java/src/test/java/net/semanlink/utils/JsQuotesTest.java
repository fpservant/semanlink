/* Created on Jan 3, 2021 */
package net.semanlink.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsQuotesTest {
	@Test
	public final void test() {
		String uri = "http://127.0.0.1:7080/semanlink/doc/2021/01/l'histoire%20de%20l'humanit%C3%A9.md";
		// String s = uri.replaceAll("'", "ZZZZZZZ");
		// s = uri.replaceAll("ZZZZZZZ", "\\" + "'");
		// System.out.println(s);
		String s = uri.replace("\'", "\\'");
		System.out.println(s);
	}

}
