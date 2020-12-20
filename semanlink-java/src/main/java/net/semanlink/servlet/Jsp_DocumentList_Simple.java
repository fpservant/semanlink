package net.semanlink.servlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLKeyword;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLTree;
import net.semanlink.semanlink.SLUtils;

// 2020-12
// c'est n'importe quoi ces Jsp_DocumentList // TODO

public class Jsp_DocumentList_Simple extends Jsp_DocumentList {
private String title;
//
// CONSTRUCTION
//

public Jsp_DocumentList_Simple(List<SLDocument> docs, HttpServletRequest request, String title) throws Exception {
	this(docs, null, request, title);
}

//le and of tags n'est ici en fait pas supporté
public Jsp_DocumentList_Simple(List<SLDocument> docs, String[] kwUris, HttpServletRequest request, String title) throws Exception {
	super(kwUris, request);
	this.docs = docs;
	this.title = title;
	if (this.kws != null) {
		filterDocsByKws(this.docs, kws, !getDisplayMode().isLongListOfDocs());
	}
}


@Override public void setDocs() throws Exception {
	throw new UnsupportedOperationException();
}

/** liste de SLDocument : la liste des docs, non filtrée (par l'éventuel AND de kws)*/
@Override protected List<SLDocument> computeDocs() throws Exception {
	throw new UnsupportedOperationException();
}

@Override public String getTitle() {
	return this.title ;
}

@Override public HTML_Link linkToThisAndKw(SLKeyword otherKw) throws IOException {
	// throw new UnsupportedOperationException();
	return HTML_Link.getHTML_Link(otherKw);
}

} // class
