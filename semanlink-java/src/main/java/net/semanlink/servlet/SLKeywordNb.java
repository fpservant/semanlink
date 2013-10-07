/* Created on 24 juil. 2005 */
package net.semanlink.servlet;

import net.semanlink.semanlink.SLKeyword;

/** Pour compter les linked keywords : un keyword, avec son nb d'occurrences. */
public class SLKeywordNb implements Comparable {
	public SLKeyword kw; private int nb;
	SLKeywordNb(SLKeyword kw, int nb) {
		this.kw = kw;
		this.nb = nb;
	}
	public SLKeyword getKw() { return this.kw; }
	public int getNb() { return this.nb; }
	void plusPlus() { nb++; }
	void plus(int n) { nb += n; }
	public boolean equals(Object o) {
		return this.kw.equals(((SLKeywordNb)o).kw);
	}
	public int hashCode() { return this.kw.hashCode(); }
	public int compareTo(Object o) {
		return this.kw.compareTo(((SLKeywordNb)o).kw);
	}
	
}
