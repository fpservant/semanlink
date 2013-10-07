/* Created on 15 août 2004 */
package net.semanlink.util;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Représente des arborescences de dossiers de type year/month/day
 * @author fps
 */
public class YearsMonthesDir {
/*public static int SORT_UP = 1;
public static int SORT_DOWN = -1;*/
private Dir rootDir;
//
//


/** ne fait rien : faire un copier coller de ce code là où on a besoin.
 *  Il y a un bon exemple ds CrRddIndexMaker.constructHTMLList */
public static void loop(YearsMonthesDir ymDir) {
	YearsMonthesDir.Dir[] years = ymDir.getYears();
	for (int iyear = 0; iyear < years.length; iyear++) {
		YearsMonthesDir.Dir year = years[iyear];
		YearsMonthesDir.Dir[] monthes = year.getChildren();
		for (int imonth = 0; imonth < monthes.length; imonth++ ) {
			YearsMonthesDir.Dir month = monthes[imonth];
			YearsMonthesDir.Dir[] days = month.getChildren();
			for (int iday = 0; iday < days.length; iday++ ) {
				YearsMonthesDir.Dir day = days[iday];
				// String subPath = ((YearsMonthesDir.DayDir) day).getSubPath();
				File[] files = day.listFiles();
				for (int i = 0; i < files.length; i++) {
					// File file = files[i];
					// do something here
				}
			}
		}
	}
}

/** Contructeur.
 * @param root la dir parent des /200x/MM/JJ
 * IL Y A ICI LA LECTURE DES /200x/MM/JJ
 */
public YearsMonthesDir(File root) throws Exception {
	this.rootDir = new Dir(root.getParentFile(), root.getName());
	String[] years = root.list();
	for (int i = 0; i < years.length; i++) {
		File yearDirFile = new File(root, years[i]);
		if (!yearDirFile.isDirectory()) continue;
		int iYear = 0;
		try {
			iYear = Integer.parseInt(years[i]);
			if ((iYear < 1000) || (iYear > 3000)) continue;
		} catch (NumberFormatException e) {
			continue;
		}
		YearDir yearDir = new YearDir(root, years[i], iYear);
		rootDir.addChild(yearDir);
		
		String[] monthes = yearDirFile.list();
		for (int j = 0; j < monthes.length; j++) {
			File monthDirFile = new File(yearDirFile, monthes[j]);
			if (!monthDirFile.isDirectory()) continue;
			int iMonth = 0;
			try {
				iMonth = Integer.parseInt(monthes[j]);
				if ((iMonth < 0) || (iMonth > 99)) continue;
			} catch (NumberFormatException e) {
				continue;
			}
			MonthDir monthDir = new MonthDir(yearDirFile, monthes[j], iMonth);
			yearDir.addChild(monthDir);
			
			String[] days = monthDirFile.list();
			for (int k = 0; k < days.length; k++) {
				File dayDirFile = new File(monthDirFile, days[k]);
				if (!dayDirFile.isDirectory()) continue;
				int iDay = 0;
				try {
					iDay = Integer.parseInt(days[k]);
					if ((iDay < 0) || (iDay > 99)) continue;
				} catch (NumberFormatException e) {
					continue;
				}
				DayDir dayDir = new DayDir(monthDirFile, days[k], iDay);
				monthDir.addChild(dayDir);
			}	
		}
	}
}

public Dir getLastDay() {
	Dir lastYear = this.rootDir.getLastChild();
	Dir lastMonth = lastYear.getLastChild();
	return lastMonth.getLastChild();
}

//
//
//
public Dir[] getYears() {
	return this.rootDir.getChildren();
}
//
//

public class Dir implements Comparable {
	protected Dir parent;
	protected File folder;
	protected String sFilename;
	File file;
	/** sera, selon le cas, constitué de MonthDir, ...*/
	private ArrayList contentAl = new ArrayList(16);
	Dir(File folder, String sFilename) {
		this.folder = folder;
		this.sFilename = sFilename;
	}
	void addChild(Dir child) {
		this.contentAl.add(child);
		child.setParent(this);
	}
	void setParent(Dir parent) { this.parent = parent; }
	public Dir getParent() { return this.parent; }
	public String getShortFilename() { return this.sFilename; }
	public File getFile() {
		if (this.file == null) this.file = new File(this.folder, this.sFilename);
		return this.file;
	}
	/** Uniquement les years, ou les month, etc... selon le cas */
	public Dir[] getChildren() {
		Dir[] x = new Dir[contentAl.size()];
		contentAl.toArray(x);
		Arrays.sort(x);
		return x;
	}
	public File[] listFiles() {
		return getFile().listFiles();
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return (this.sFilename.compareTo(((Dir) o).sFilename));
	}
	Dir getLastChild() {
		Dir[] children = getChildren();
		if ((children == null) || (children.length == 0)) return null;
		return children[children.length - 1];
	}
	public String toString() { return getFile().toString() ; }
}

/** une dir à laquelle est associée un entier - typiquement son short filename */
public class IntDir extends Dir implements Comparable {
	protected int nn;
	IntDir(File folder, String shortFilename, int nn) {
		super(folder, shortFilename);
		this.nn = nn;
	}
	public int compareTo(Object o) {
		IntDir intDir = (IntDir) o;
		return (this.nn - intDir.nn);
	}
}
public class YearDir extends IntDir {
	YearDir(File folder, String yyyy, int nn) { super(folder, yyyy, nn); }
	public String getYYYY() { return this.sFilename; }
}

public class MonthDir extends IntDir {
	MonthDir(File folder, String mm, int nn) { super(folder, mm, nn); }
	public String getMM() { return this.sFilename; }
}

public class DayDir extends IntDir {
	// String subPath = years[i] + "/" + monthes[j] + "/" + days[k] + "/";
	private String subPath;
	DayDir(File folder, String dd, int nn) { super(folder, dd, nn); }
	public String getDD() { return this.sFilename; }
	public String getSubPath() {
		if (this.subPath == null) {
			Dir m = getParent();
			Dir y = m.getParent();
			this.subPath = y.getShortFilename() + "/" + m.getShortFilename() + "/" + getShortFilename() + "/";
		}
		return this.subPath;
	}
	public GregorianCalendar getCalendar() {
		Dir m = getParent();
		Dir y = m.getParent();
		return new GregorianCalendar(Integer.parseInt(y.getShortFilename()),
				Integer.parseInt(m.getShortFilename()) - 1,
				Integer.parseInt(getShortFilename()));

	}
	/** yyyy-mm-dd */
	public String getYMD() {
		Dir m = getParent();
		Dir y = m.getParent();
		return y.getShortFilename() +"-"+ m.getShortFilename() +"-"+ getShortFilename();
		
	}
	public String getLongDate() {
		return getDate(DateFormat.LONG);
	}
	public String getShortDate() {
		return getDate(DateFormat.SHORT);
	}
	private String getDate(int dateFormatCste) {
		Date date = getCalendar().getTime();
		DateFormat dateFormat = DateFormat.getDateInstance(dateFormatCste);
		return dateFormat.format(date);
	}
}

}
