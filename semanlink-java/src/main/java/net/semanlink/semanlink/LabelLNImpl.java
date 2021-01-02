/* Created on Jan 2, 2021 */
package net.semanlink.semanlink;
public class LabelLNImpl implements LabelLN, Comparable<LabelLNImpl> {
private String label, lang;
public LabelLNImpl(String label, String lang) {
	this.label = label;
	this.lang = lang;
}

public String getLabel() {
	return this.label;
}

public String getLang()  {
	return this.lang;
}

@Override public String toString() {
	String la = getLang();
	if (la == null) return getLabel();
	return getLabel() + "@" + la;
}

@Override public int hashCode() {
	return toString().hashCode(); // hum, not good
}

@Override public boolean equals(Object o) {
	if (!(o instanceof LabelLNImpl)) return false;
	LabelLNImpl ll = (LabelLNImpl) o;
	if (ll.lang == null) {
		if (lang != null) return false;
	} else {
		if (!lang.equals(ll.lang)) return false;
	}
	return ll.label.equals(label);
}

@Override
public int compareTo(LabelLNImpl ll) {
	return toString().compareTo(ll.toString()); // hum, not good
}

}
