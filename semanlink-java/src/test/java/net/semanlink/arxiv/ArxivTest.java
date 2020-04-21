/* Created on Mar 24, 2020 */
package net.semanlink.arxiv;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

// import javax.xml.Parsers.DocumentBuilder;

public class ArxivTest {

// @Test // TODO REMETTRE ?
public final void test() throws IOException, ParserConfigurationException, SAXException {
	Client client = ClientBuilder.newClient();
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	
	String arxivNum = "0807.4145";
	
	ArxivEntry arx = ArxivEntry.newArxivEntry(arxivNum, client, factory);
	assertTrue(arxivNum.equals(arx.getArxivNum()));
	// assertTrue(arxivNum.equals(arx.getId()));
	// assertTrue(arx.getTitle().startsWith()));
	System.out.println("Title: " + arx.getTitle());
	assertTrue(!arx.getTitle().contains("\n"));
	System.out.println("Summary:\n" + arx.getSummary());
	assertTrue(arx.getSummary().contains("\n"));
}

@Test public void cleanTextContentTest() throws IOException {
	String text = "Il neigeait, on était vaincu \n par sa conquête. \n   \n Pour la première fois l'Aigle baissait la tête.\n\n\n";
	text += "Sombres                  jours !\n";
	text += "L'empereur revenait lentement,\n\n";
	text += "Laissant derrière lui brûler Moscou fumant.";
	
	String good = "Il neigeait, on était vaincu par sa conquête.\n" + 
			"\n" + 
			"Pour la première fois l'Aigle baissait la tête.\n" + 
			"\n" + 
			"Sombres jours ! L'empereur revenait lentement,\n" + 
			"\n" + 
			"Laissant derrière lui brûler Moscou fumant.";
	
	String x = ArxivEntry.cleanTextContent(text);
	
	assertTrue(x.equals(good));
	
	text = "\n\nnom auteur\n\n";
	boolean replaceLineBreakBySpace = true;
	x = ArxivEntry.cleanTextContent(text, replaceLineBreakBySpace);
	assertTrue(x.equals("nom auteur"));

}

@Test public void url2numTest() {
	String num = "2003.02320";
	String url = "https://arxiv.org/abs/2003.02320";
	String x;
	x = Arxiv.url2num(url);
	assertTrue(num.contentEquals(x));
	
	url = "https://arxiv.org/pdf/2003.02320.pdf";
	x = Arxiv.url2num(url);
	assertTrue(num.contentEquals(x));
	
	url = "https://arxiv.org/abs/2003.02320v1";
	x = Arxiv.url2num(url);
	System.out.println(x);
	assertTrue(num.contentEquals(x));
	
	url = "http://arxiv.org/abs/0811.3701";
	x = Arxiv.url2num(url);
	System.out.println(x);
	assertTrue("0811.3701".contentEquals(x));

}

@Test public void foo() {
	String text = "\n\nnom auteur\n\n";
	boolean replaceLineBreakBySpace = true;
	String x = ArxivEntry.cleanTextContent(text, replaceLineBreakBySpace);
	System.out.println("X"+x+"X");
}

@Test public void numIfBookmarkWithHash() {
	 String url = "https://arxiv.org/abs/1912.08904#clicktoread";
	 String num = Arxiv.url2num(url);
	 assertTrue(num.equals("1912.08904"));
}

}
