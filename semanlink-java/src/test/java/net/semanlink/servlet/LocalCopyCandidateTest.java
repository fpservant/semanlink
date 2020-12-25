/* Created on Dec 23, 2020 */
package net.semanlink.servlet;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.semanlink.util.Util;
import net.semanlink.util.text.WordsInString;

public class LocalCopyCandidateTest {

// verify there is a match between the title (from html) and
// the filename.
// Cannot be an exact match, eg. 
// <title>Covid-19 : après l’aval de l’Europe, le défi logistique du vaccin Pfizer-BioNTech en France</title>
// the ":" is a separator for path on macos. Hence when exporting / printing as pdf,
// we get filenames such as (safari export as pdf ; print as pdf) :
// Covid-19 / après l’aval de l’Europe, le défi logistique du vaccin Pfizer-BioNTech en France.pdf
// or (firefox):
// Covid-19 après l’aval de l’Europe, le défi logistique du vaccin Pfizer-BioNTech en France.html
// (would be different on win)

@Test
public final void test() {
	String htmlTitle = "Covid-19 : la saga du vaccin à ARN messager désormais dans le sprint final";
	
	boolean strict = true;
	File dir = new File("src/test/files/localCopyCandidates/strict");
	for (File f : dir.listFiles()) {
		assertTrue(Jsp_Document.titleFilenameMatch(htmlTitle, f, strict));
	}
	dir = new File("src/test/files/localCopyCandidates/notstrict");
	for (File f : dir.listFiles()) {
		assertFalse(Jsp_Document.titleFilenameMatch(htmlTitle, f, strict));
		assertTrue(Jsp_Document.titleFilenameMatch(htmlTitle, f, !strict));
	}
	dir = new File("src/test/files/localCopyCandidates/nomatch");
	for (File f : dir.listFiles()) {
		assertFalse(Jsp_Document.titleFilenameMatch(htmlTitle, f, true));
		assertFalse(Jsp_Document.titleFilenameMatch(htmlTitle, f, false));
	}
}
}
