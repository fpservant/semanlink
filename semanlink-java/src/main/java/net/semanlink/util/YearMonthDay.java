package net.semanlink.util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/** Utilitaire pour retourner des dates sous la forme aaaa-mm-jj, ou sous forme de dossier */
public class YearMonthDay {
private Calendar calendar;
/** A partir de la date du jour. */
public YearMonthDay() {
	this(new Date());
}
public YearMonthDay(Date date) {
	this.calendar = Calendar.getInstance();
	this.calendar.setTime(date);
}
public YearMonthDay(Calendar calendar) {
	this.calendar = calendar;
}
public static YearMonthDay oneMonthAgo() {
	Calendar cal = Calendar.getInstance();
	cal.setTime(new Date());
	cal.add(Calendar.MONTH, -1);
	return new YearMonthDay(cal);
}
public static YearMonthDay daysAgo(int n) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(new Date());
	cal.add(Calendar.DATE, -1*n);
	return new YearMonthDay(cal);
}
public String getYear() {
	return Integer.toString(calendar.get(Calendar.YEAR));
}
public String getMonth() {
	String s;
	s = Integer.toString(calendar.get(Calendar.MONTH) + 1);
	if (s.length() < 2) return "0" + s;
	return s;		
}
public String getDay() {
	String s;
	s = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
	if (s.length() < 2) return "0" + s;
	return s;		
}
public String getYearMonthDay(String delim) {
	return getYearMonth(delim) + delim + getDay();
}
public String getYearMonth(String delim) {
	StringBuffer sb = new StringBuffer();
	sb.append(getYear());
	sb.append(delim);
	sb.append(getMonth());
	return sb.toString();
}
public File yearMonthAsFolder(File dir) {
	File x = new File(dir, getYear());
	return new File(x, getMonth());
}
public File yearAsFolder(File dir) {
	return new File(dir, getYear());
}

public String getDateString() { return getYearMonthDay("-"); }

public String getHour() {
	String s;
	s = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
	if (s.length() < 2) return "0" + s;
	return s;		
}
public String getMinute() {
	String s;
	s = Integer.toString(calendar.get(Calendar.MINUTE));
	if (s.length() < 2) return "0" + s;
	return s;		
}
public String getSecond() {
	String s;
	s = Integer.toString(calendar.get(Calendar.SECOND));
	if (s.length() < 2) return "0" + s;
	return s;		
}


public String getTimeString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getYear());
		sb.append("-");
		sb.append(getMonth());
		sb.append("-");
		sb.append(getDay());
		sb.append("T");
		sb.append(getHour());
		sb.append(":");
		sb.append(getMinute());
		sb.append(":");
		sb.append(getSecond());
		sb.append("Z");
		return sb.toString();
}


}
