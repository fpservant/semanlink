/* Created on 11 déc. 2013 */
package net.semanlink.common;

import static org.junit.Assert.*;

import java.util.Locale;

import net.semanlink.util.URLUTF8Encoder;
import net.semanlink.util.text.CharConverter;
import net.semanlink.util.text.SimpleCharConverter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CharConverterTest {

@BeforeClass
public static void setUpBeforeClass() throws Exception {
}

@AfterClass
public static void tearDownAfterClass() throws Exception {
}

@Before
public void setUp() throws Exception {
}

@After
public void tearDown() throws Exception {
}

@Test
public final void simpleConverter() {
	SimpleCharConverter simple = SimpleCharConverter.collatorBasedConverter(256*256, Locale.FRANCE);
	String s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,Æ,ü";
	String d = simple.convert(s);
	System.out.println(d);
	assertTrue(d.equals("e,e,e,c,a,u,a,u,o,a,a,œ,n,æ,u"));
}

@Test
public final void frConverter() {
	CharConverter con = new CharConverter(Locale.FRANCE, "_");
	String s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,Æ,ü";
	String d = con.convert(s);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_u"));
	
	s = "é_é";
	d = con.convert(s);
	assertTrue(d.equals("e_e"));
	
	s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,,,,Æ,æ---Ü,™,Λ,λ,Λεωνίδας";
	d = con.convert(s);
	System.out.println(d);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_ae_u_tm_Λ_λ_Λεωνίδας")); // 2021-07 no more then again
	// assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_ae_u_tm_λ_λ_λεωνίδας"));
	
	d = con.urlConvert(s);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_ae_u_tm_%CE%9B_%CE%BB_%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82"));
}

@Test
public final void elConverter() {
	CharConverter con = new CharConverter(new Locale("el"), "_");
	String s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,Æ,ü";
	String d = con.convert(s);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_u"));
	
	s = "é_é";
	d = con.convert(s);
	assertTrue(d.equals("e_e"));
	
	s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,,,,Æ,æ---Ü,™,Λ,λ,Λεωνίδας";
	d = con.convert(s);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_ae_u_tm_λ_λ_λεωνιδασ"));
	
	d = con.urlConvert(s);
	assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_ae_u_tm_%CE%9B_%CE%BB_%CE%9B%CE%B5%CF%89%CE%BD%CE%AF%CE%B4%CE%B1%CF%82"));
}

@Test
public final void danemarkConverter() {
	CharConverter con = new CharConverter(new Locale("da"), "_");
	String s = "abAB";
	String d = con.convert(s);
	String durl = con.urlConvert(s);
	System.out.println(s + " -> " + d + " /url " +durl );
	
	System.out.println(URLUTF8Encoder.encode("¾B"));
	
	
	s = "åb";
	d = con.convert(s);
	durl = con.urlConvert(s);
	System.out.println(s + " -> " + d + " /url " +durl );
	
	
	s = "é,è,ê,ç,à,ù,ä,ü,ö,á,Á,œ,ñ,Æ,ü";
	d = con.convert(s);
	// assertTrue(d.equals("e_e_e_c_a_u_a_u_o_a_a_oe_n_ae_u"));
	
	s = "Med Partikelfilter";
	d = con.convert(s);
	System.out.println(d);
	
	s = "a a A A e e E E é é";
	d = con.convert(s);
	System.out.println(d);
	
	s = "é_é";
	d = con.convert(s);
	System.out.println(d);
	assertTrue(d.equals("e_e")); // 2021-07 was : E_E

	d = con.urlConvert(s);
	System.out.println(d);
}
}