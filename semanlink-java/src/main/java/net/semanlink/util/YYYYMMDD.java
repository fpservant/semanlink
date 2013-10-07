/* Created on 17 avr. 2005 */
package net.semanlink.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import net.semanlink.semanlink.SLDocument;

public class YYYYMMDD {
	private int yyyy,mm,dd;
	/** use getter */
	private YearMonthDay yearMonthDay;
	private YYYYMMDD() {};
	private Locale locale;
	/** tous les params au format humain habituel (mm de 1 à 31 !!! - cf java) */
	public YYYYMMDD(int yyyy, int mm, int dd) {
		this.yyyy = yyyy;
		this.mm = mm;
		this.dd = dd;
	}
	/** suppose que l'uri de slDoc se termine par /yyyy/mm/dd (et éventuellement un /). */
	public YYYYMMDD(SLDocument slDoc) {
		this();
		String uri = slDoc.getURI() ;
		String path = uri;
		if (path.endsWith("/")) path = path.substring(0, path.length()-1);
		int k = path.lastIndexOf("/");
		if (k < 0) throwNotYYYYMMDD();
		String s = path.substring(k+1);
		try {
			this.dd = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throwNotYYYYMMDD();
		}
		path = path.substring(0,k);
		k = path.lastIndexOf("/");
		if (k < 0) throwNotYYYYMMDD();
		s = path.substring(k+1);
		try {
			this.mm = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throwNotYYYYMMDD();
		}
		path = path.substring(0,k);
		k = path.lastIndexOf("/");
		if (k < 0) throwNotYYYYMMDD();
		s = path.substring(k+1);
		try {
			this.yyyy = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throwNotYYYYMMDD();
		}
		if ((this.yyyy<1000)||(this.yyyy>9999)) {
			throwNotYYYYMMDD();
		}
	}
	private static void throwNotYYYYMMDD() {
		throw new IllegalArgumentException("Uri not ending with aaaa/mm/dd");
	}
	public int yyyy() { return this.yyyy; }
	public int mm() { return this.mm; }
	public int dd() { return this.dd; }
	public GregorianCalendar getCalendar() {
		return new GregorianCalendar(this.yyyy, this.mm - 1, this.dd);
	}
	public YearMonthDay getYearMonthDay() {
		if (this.yearMonthDay == null) {
			this.yearMonthDay = new YearMonthDay(getCalendar());
		}
		return this.yearMonthDay;
	}
	/** yyyy-mm-dd */
	public String getYMD() {
		return getYearMonthDay().getYearMonthDay("-");
	}
	public String getYM() {
		return getYearMonthDay().getYearMonth("-");
	}
	public String getLongDate() {
		return getDate(DateFormat.LONG);
	}
	public String getShortDate() {
		return getDate(DateFormat.SHORT);
	}
	private String getDate(int dateFormatCste) {
		Date date = getCalendar().getTime();
		DateFormat dateFormat = DateFormat.getDateInstance(dateFormatCste, getLocale());
		return dateFormat.format(date);
	}
	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		if (this.locale == null) return Locale.getDefault();
		return this.locale;
	}
	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
