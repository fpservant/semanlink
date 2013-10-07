/*
 *  POMPE de si cg.util.xml;
 */
package net.semanlink.util.xml;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Des utilitaires pour aider à écrire du XML. 
 * Part of this code comes from Vlad Patryshev's class com.myjavatools.xml.XMLWriter (public domain) 
 */
public class XMLWriterUtil {

	//
	// This code comes from Vlad Patryshev's class com.myjavatools.xml.XMLWriter (public domain)
	//
	
	/**
	 * escapeChars - characters that ought to be escaped in output Xml file
	 */
	static public final String escapedChars = "<>\'\"&]";

	/**
	 * okChars - characters that are okay to be kept intact in output Xml file
	 */
	static public final String okChars      = "\r\n\t";

	static protected boolean needsEscape(char c) {
		return ((c < '\u0020' && okChars.indexOf(c) < 0) ||
						escapedChars.indexOf(c) >= 0);
	}

	/**
	 * Converts a string to the form that is acceptable inside Xml files,
	 * escaping special characters.
	 *
	 * @param s the string to convert
	 * @return string with certain characters replaced with their SGML entity representations
	 */
	static public String xmlEscape(String s)
	{
		boolean bNeedEscape = false;

		for (int i = 0; i < s.length() && !bNeedEscape; i++) {
			bNeedEscape = needsEscape(s.charAt(i));
		}

		if (!bNeedEscape) return s;

		StringBuffer result = new StringBuffer(s.length() * 6 / 5);

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			String esc = toXML(c);
			if (esc == null) {
				result.append(c);
			} else {
				result.append(esc);
			} 
		}

		return result.toString();
	}

	/**
	 * Encodes an escape XML character by SGML rules
	 * It can be a hex representation
	 * @param c the character
	 * @return the string with the Predefined Entity or null if no entity could be found
	 *
	 * <br><br><b>Examples</b>:
	 * <li><code>sgmlEntity('<')</code> returns "&amp;lt;" (that is, Predefined Entity);</li>
	 * <li><code>sgmlEntity('&')</code> returns "&amp;lt;" (that is, Predefined Entity);</li>
	 * <li><code>sgmlEntity('X')</code> returns <b>null</b>";</li>
	 * <li><code>sgmlEntity('\n')</code> returns <b>null</b>".</li>
	 */
	public static String toXML(char c) {
		return (c == '<') ? "&lt;" :
					 (c == '>') ? "&gt;" :
					 (c == '\'') ? "&apos;" :
					 (c == '"') ? "&quot;" :
					 (c == '&') ? "&amp;" :
					 (c == ']') ? "&#93;" :
					 null;
	}

	//
	// End of Vlad Patryshev's code
	//

	/**
	 * Return the very first line of an XML file.
	 * Beware : the encoding used in xml is not the same as the one returned 
	 * by the java Writer's getEncoding methods. writeFirstLine take care of it
	 * @see EncodingMap.getXMLFromJava(String)
	 */
	public static String getFirstLine(String xmlEncoding) {
		StringBuffer sb = new StringBuffer(64);
		sb.append("<?xml version=\"1.0\" encoding=\"");
		sb.append(xmlEncoding);
		sb.append("\" standalone=\"yes\"?>");
		return sb.toString();
	}
	
	public static void writeFirstLine(OutputStreamWriter out) throws IOException {
		out.write(getFirstLine(EncodingMap.getXMLFromJava(out.getEncoding())));
	}
	
	/**
	 * @param tagName
	 * @param domain par ex "http://com.renault.c2g"
	 * @param schemaLocation par ex "http://com.renault.c2g c2greport.xsd"
	 * @param more to be used if you want to add more attributes
	 */
	public static String getRootTag(String tagName, String domain, String schemaLocation, String more, String lang) {
		StringBuffer sb = new StringBuffer(128);
		sb.append("<");
		sb.append(tagName);
		sb.append(" xmlns=\"");
		sb.append(domain);
		sb.append("\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"");
		sb.append(schemaLocation);
		sb.append("\"");
		if (more != null) {
			sb.append(" ");
			sb.append(more);
		}
		if (lang != null) {
			sb.append(" lang=\"");
			sb.append(lang);
			sb.append("\"");
		}
		sb.append(">");
		return sb.toString();
	}
	
}
