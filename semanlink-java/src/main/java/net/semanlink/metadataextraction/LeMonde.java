package net.semanlink.metadataextraction;
import java.io.*;
import java.util.*;
import java.text.*;
import net.semanlink.semanlink.SLVocab;
import net.semanlink.util.*;

public class LeMonde extends Extractor {
	public boolean dealWith(ExtractorData data) {
		if (data.getUri().indexOf("/www.lemonde.fr/") > -1) return true;
//		try {
//			String text = data.getText();
//			if (text.lastIndexOf("©Le Monde") > -1) return true; // après mars 2005
//			if (text.lastIndexOf("© Le Monde") > -1) return true; // avant mars 2005
//			if (text.indexOf("LE MONDE |") > -1) return true; // à partir de mars 2005
//			return false;
//		} catch (Exception e) { 
//			return false;
//		}
		return false;
	}

	/*public String getSource(ExtractorData data) { // viré, pour cause double emploi
		return "Le Monde";
	}*/
	
//	public String getDateParution(ExtractorData data) throws IOException, ParseException {
//		String t = data.getText();
//		if (t == null) return null;
//		String marc = "Article paru dans l'édition du";  // à partir de mars 2005
//		int n = t.indexOf(marc);
//		if (n < 0) {
//			marc = "ARTICLE PARU DANS L'EDITION DU"; // avant mars 2005
//			n = t.indexOf(marc);
//			if (n < 0) {
//				marc = "LEMONDE.FR |"; // avant mars 2005
//				n = t.indexOf(marc);
//			}
//		}
//		if (n < 0) return null;
//		n = n + marc.length() +1;
//		t = t.substring(n);
//		LineNumberReader lineReader = new LineNumberReader(new StringReader(t));
//		String x = lineReader.readLine();
//		Calendar cal = dateInLeMonde2Calendar(x,Locale.FRANCE);
//		if (cal != null) {
//			return (new YearMonthDay(cal)).getYearMonthDay(SLVocab.DATE_DELIM);
//		} else {
//			return null;
//		}
//	}
	
//	public String getCreator(ExtractorData data) {
//		String t = data.getText();
//		if (t== null) return null;
//		if (t.indexOf("Eric Le Boucher") > -1) {
//			return "Eric Le Boucher";
//		}
//		return null;
//	}
	
	/** Parse une date telle que apparaît ds un article ("paru ds l'édition du ...")
	 * Attention, problème avec les dates genre "09.02.04".Palié en supposant
	 * que si l'an est < 50, il s'agit de 20aa, sinon 19aa
	 * @param sDate : fin de ligne contenant la date
	 * @author fps
	 */
	private Calendar dateInLeMonde2Calendar(String sDate, Locale locale) throws ParseException {
		// ParseException e = null;
		Date date = null;
		try {
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
			date = new Date(dateFormat.parse(sDate).getTime());
		} catch (ParseException t) {
			// e = t;
		}
		
		if (date == null) {
			try {
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
				date = new Date(dateFormat.parse(sDate).getTime());
				Calendar x = Calendar.getInstance();
				x.setTime(date);
				return x;
			} catch (ParseException t) {}
		}
		
		if (date != null) {
			Calendar x = Calendar.getInstance();
			x.setTime(date);
			return x;
		}
		
		StringTokenizer st = new StringTokenizer(sDate,".");
		try {
			if (st.countTokens() > 2) {
				String j = st.nextToken();
				String m = st.nextToken();
				String a = st.nextToken().substring(0,2);
				if (Integer.parseInt(a) < 50) {
					a = "20"+a;
				} else {
					a = "19"+a;
				}
				
				GregorianCalendar x = new GregorianCalendar(Integer.parseInt(a),
						Integer.parseInt(m) - 1,
						Integer.parseInt(j));
				return x;
			}
		} catch (Throwable t) {}
		// throw e;
		return null;
	}

	public Locale getLocale(ExtractorData data) throws Exception {
		return Locale.FRANCE;
	}
} // class LeMonde
