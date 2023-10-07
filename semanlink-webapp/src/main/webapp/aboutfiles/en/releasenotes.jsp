<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
%>
<h1>Release notes</h1>
<div class="graybox">

<h2>0.7.6 2023-10-07</h2>
<ul>
<li>arxiv changed the URL of pdfs</li>
</ul>

<h2>0.7.6 2021-12-13</h2>
<ul>
<li>log4j update</li>
</ul>

<h2>0.7.5 2020-12-xx</h2>
<ul>
<li>Search Doc: by URL or phrase</li>
<li>Edit/Local copy: improvements</li>
<li>Editing labels: improvements</li>
</ul>

<h2>0.7.4 2020-11-xx</h2>
<ul>
<li>Copy-Paste of doc or tag uri into comment field</li>
<li>Getting a given property of a doc or tag</li>
<li>Doc mentioned in docs comment used to populate "Linked From" docs in doc's page</li>
<li>Doc page: list docs with similar tags</li>
<li>Edit/Search Tag: create tag button</li>
</ul>

<h2>0.7.3 2020-05-30</h2>
<ul>
<li>Drag'n drop from tag search list to comment markdown, to list of tags (hum: tag's page only. TODO: doc's page)</li>
<li>Comments: now use "tag:" and "doc:" for links to tags and docs in markdown</li>
<li>Trying to make it easier to link a bookmark to a local copy: "Auto-link" on new bookmark</li>
<li>Export: as 2 files</li>
<li>SPARQL page: queries to search text in doc metadata (eg. title). (No index is used - enough however
to find a doc by its title, which is useful)</li>
<li>Upload</li>
</ul>

<h2>0.7.2 2020-04-11</h2>
<ul>
<li>Arxiv docs, specific metadata</li>
<li>Bookmark form simplified</li>
<li>Use of <a href="https://github.com/robert-bor/aho-corasick">Aho-Corasick algo. implementation</a> to extract tags from a text</li>
<li>Refactoring of the indexing of thesaurus by words</li>
</ul>

<h2>0.7.1 2020-03-03</h2>
<ul>
<li>Exact or close matches first in search results</li>
<li>Avoid too big tag clouds</li>
<li>Tag(s) and tag <ul>
    <li>better URLs</li>
    <li>Tag search results on a tag page now link to an AND of both tags.
    (The and between tags was previously available only through the tag cloud.
    Even before the shrinking of the size of the tag cloud introduced with this version,
    this was not always enough, to search for the AND of two general tags (ex. NLP AND History).
    Of course, this is not as good when you just want to go the tag's page (not to "and" it with)
    current one): but you're only one click away from it (in the "parents")</li>
    </ul></li>
<li>Hide/Show right bar: click on logo. 0r with a param: ?rightbar=0 to hide, 1 to show</li>
<li>Some changes in the display (less grey rectangles)</li>
</ul>

<h2>0.7.0 2020-01-26</h2>
<ul>
<li>List of docs: no more a UL list -> better display</li>
<li>Bug corrected regarding numbering in table of content in markdown files</li>
<li>Bug corrected regarding delete key in live search</li>
<li>top bar: logo on the right</li>
</ul>

<h2>0.6.2 2020-01-01</h2>
<ul>
<li>Better handling of bookmarks on local files outside semanlink's datafolders</li>
</ul>

<h2>0.6.1 2019-10-26</h2>
<ul>
<li>Various small things and bug corrections related to the changes introduced with 0.6</li>
<li>Table of content in markdown files using markdown-it-toc-done-right</li>
</ul>

<h2>0.6.0 2019-05-20</h2>
<ul>
<li>Statements about bookmarks now have a proper URI served by the servlet (semanlink/doc/...) 
as subject (their subject is no more the bookmarked URL). This will allow many improvements (in 
particular the possibility to change the bookmarked URL without changing the URI of the bookmark)</li>
<li>When running locally (servlet on a desktop machine), possibility to directly open a local file (not only
a downloaded version of it)</li>
</ul>

<h2>0.5.5 2017-06-xx</h2>
<ul>
<li>Docs in expanded tree were not sorted by sortProperty (that is, date by default)</li>
<li>Clic on the cloud of tags always return docs in subtrees</li>
<li>Markdown for comments in lists of docs</li>
<li>Better handling of relative urls in markdown: have them point to the sl display of md</li>
<li>Label of tags created on "Create Bookmark" form doesn't have a lang anymore (Action_Bookmark.java: was default locale's lang) TODO improve</li>
<li>Added an "about" link for each doc in a list</li>
</ul>

<h2>0.5.4 2017-06-09</h2>
<ul>
<li>refactoring (all jars with same version num, no more aggregator project)</li>
<li>Upgrade Jena to 3.3.0</li>
<li>some stuff related to markdown</li>
</ul>

<h2>0.5.3 2017-04-01</h2>
<ul>
<li>markdown<ul>
	<li>using markdown-it (instead of markdown-js)</li>
	<li>possibility to edit markdown files</li>
</ul></li>
<li>textarea now 100% of their contening div</li>
</ul>
<h2>0.5.2</h2>
<ul>
<li>Upgrade Jena to 3.0.0 -- Beware, <b>requires Java 8</b> (implied several changes because of conflicts with some old libs that were used here or there)</li>
<li>Delicious removed</li>
</ul>

<h2>0.5.1 2015-10-10</h2>
<ul>
<li>Bookmarklet was not working with https</li>
<li>Some of the examples on the sparql page were not working</li>
</ul>

<h2>0.5.0 2015-05-09</h2>
<ul>
<li>Upgrade Jena to 2.13 -- Beware, <b>requires Java 7</b></li>
<li>Corrects an exception that was thrown on a fresh install when changing the title of a document (thanks to Jarriel Perlman for reporting the problem)</li>
<li>https documents can now be downloaded</li>
</ul>

<h2>0.4.2 2013-09-30</h2>
<ul><li>now available on <a href="https://github.com/fpservant/semanlink">github</a></li></ul>

<h2>0.4.1 2013-08-26</h2>
<ul><li>some bugs corrected</li></ul>

<h2>0.4.1 2013-08-13</h2>
<ul>
<li>Some RDFa describing tags</li>
<li>scripts defined in files are now loaded after the body</li>
<li>Links of the tag cloud now uses rel="nofollow". Some other links too</li>
</ul>

<h2>0.4.0 2013-04-18</h2>
<ul>
<li>On a tag page, possibility to switch between showing the short list of docs and the long one</li>
<li>remove the "resolvealias=true" param that is no more needed</li>
</ul>

<h2>0.4.0 2013-04-06</h2>
<ul>
<li>Now uses SKOS properties (skos:broader, narrower, related, prefLabel, altLabel) instead of the properties that were defined by semanlink (sl:parent, related...).
Which means that Semanlink now produces skos data, and uses it as native format. (Be warned however that we do not attempt to enforce that "broader-narrower" and "related"
are disjoint (something that we think that it should not have been required by skos, BTW).
Tags still are of type sl:Tag - a subClass of skos:Concept.
Alias have been replaced using the skos:altLabel property. Old data is automatically converted to new ones on startup.</li>
<li>By default, displays now the short list of documents for a tag (that is, the list of docs tagged with that tag), no more the list of all documents tagged by the tag or by any of its descendants).
Old behavior can still be used (see the "preferences"). In any case however, the tag cloud now contains the list of tags corresponding to the "long list" of documents</li>
</ul>
<h2>0.3.0 2012-08-31</h2>
<ul>
	<li>RDF/JSON output (application/rdf+json, extension ".rj")</li>
	<li>JSON-LD output (application/ld+json, extension ".json"), using JSONLD-java, Copyright (c) 2012, Deutsche Forschungszentrum für Künstliche Intelligenz GmbH, All rights reserved.</li>
	<li>Now CORS friendly (cf <a href="http://lists.w3.org/Archives/Public/semantic-web/2010Oct/0226.html">this</a>)</li>
	<li>Now uses maven. Not without some side effects:
		<ul><li>upgrade to struts 1.3.10</li>
		</ul>
	</li>
	<li>Upgrade Jena</li>
</ul>

<h2>0.2.5 2012-04-25</h2>
<ul>
	<li>Correction of a bug in the bookmarklet</li>
</ul>
<h2>2012-06</h2>
<ul>
	<li>CoolUriServlet: if n3 requested in accept header, now returns n3</li>
</ul>

<h2>0.2.4 2012-03-01</h2>
<ul>
	<li>Corrections in the JSP for Tomcat 7</li>
</ul>
<h2>0.2.3 2011-01-23</h2>
<ul>
	<li>Pages also available as n3 (tag.n3)</li>
</ul>
<h2>0.2.2 2011-01-01</h2>
<ul>
	<li>SPARQL<ul>
		<li>now possible to query using the SKOS narrower, narrowerTransitive, broader and broaderTransitive properties (based of ARQ's property paths) Example queries using these properties
		Note that: aTag skos:narrowerTransitive aTag. (same with skos:broaderTransitive): otherwise implementing in SPARQL the intersection of 2 hierarchies becomes ugly</li>
		<li>results of queries DESCRIBING tags: now contain "sl:hasChild" statements ("sl:hasChild" property is not used in the triple store, which only uses sl:hasParent)</li>
		<li>now possible to use "hasChild" in a query </li>
		<li>init of a generic mecanism to pass javascript handlers dedicated to the display of given rdf:types</li>
	</ul></li>
	<li>Upgraded Jena to 2.6.4</li>
</ul>

<h2>0.2.1 2010-12-29</h2>
<ul>
	<li>SPARQL<ul>
		<li>Link to page to enter SPARQL queries now in top menu.</li>
		<li>Various modifications in the SPARQL page, and in the display of RDF returned from SPARQL queries</li>
		<li>Searching tags by text example</li>
	</ul></li>
	<li>Switch from XHTML 1.0 Transitional to XHTML+RDFa 1.0</li>
	<li>Export: the publish property can be used for tags</li>
</ul>
	
<h2>0.2.0 2010-07-26</h2>
<ul>
	<li>Page to enter SPARQL queries (at sl/sparql).<ul>
		<li>Result of describe queries can be output as HTML, using the Tabulator javascript RDF parser. NOTE that the code for this output is therefore completely
		distinct from the rest of old semanlink code (this code demonstrates simple production of HTML from RDF on the client in javascript. Its integration in semanlink
		is kind of a quick hack, just to see.) </li>
		<li>"sl:hasDescendant" and "sl:hasAncestor" (implemented as Jena property path) (Note that aTag hasDescendant aTag. (same thing with ancestor))</li>
	</ul></li>
	<li>sources now included in the war.</li>
</ul>

<h2>0.1.4 2008-09-02</h2>
<ul>
	<li>Corrects a bug that could lead to creation of tags without being in edit mode (this was not possible for a human user using the GUI, but it was for Googlebot,
	cf <a href="http://googlewebmastercentral.blogspot.com/2008/04/crawling-through-html-forms.html">Crawling through HTML forms</a></li>
</ul>
<h2>0.1.3 2008-06-21</h2>
<ul>
	<li>Edit tag page now display a special form to add rdf:type values for the the tag (A Tag can be for instance a foaf:person)</li>
	<li>List of domains that have been bookmarked (from welcome page). Just as with new entries, during the last month by default: add a "days=xx" param to change that.</li>
	<li>When the sl:describedBy property of a tag is defined, (case of tag created from bookmark form), it is now shown in the title bar of the tag</li>
	<li>Logo</li>
</ul><h2>0.1.2 2008-02-25</h2>
<ul>
	<li>Tag search's results now available as RDF. Dereferencing for instance <a href="http://www.semanlink.net/sl/search?text=afric+musi">http://www.semanlink.net/sl/search?text=afric+musi</a>
	returns html or rdf (the rdf is also available at <a href="http://www.semanlink.net/sl/search.rdf?text=afric+musi">http://www.semanlink.net/sl/search.rdf?text=afric+musi</a>)</li>
	<li>New "Create Tag" button on the bookmark form: to create a tag as the thing or concept defined by the bookmarked HTML page (new property sl:describedBy)</li>
	<li>When clicking the bookmarklet on a page that is the homepage of a tag, or that "describes" a tag (that is, a page such as
	tag,sl:describedBy,page), you are now redirected to tag's page.</li>
	<li>New tags now listed on the "new entries" page</li>
	<li>New "lang" menu in the "Preferences" Panel</li>
	<li>Greek documentation more complete</li>
</ul>
<h2>0.1.1 2007-12-17</h2>
<ul>
	<li>Application now available in Greek, thanks to Dimitris Delevegos.</li>
	<li>
	Now possible to change the language of the application adding a "lang=xx" param to a request. Only "en", "fr" and "el" available (French, only partially)
	</li>
	<li>
	Live search now working with IE7. Bug was due to the fact that the javascript test 
	(window.XMLHttpRequest) returns true with ie7 (it was not the case with ie6). But contrary to non ie browsers,
	ie7 requires a call to new XMLHttpRequest for each ajax call. It looks like microsoft tried to break scripts
	with this move. Didn't they?
	</li>
</ul>
<h2>0.1.0 2007-11-26</h2>
<ul>
	<li>Tagging using non-latin characters is now supposed to work<ul>
		<li>but it has been tested only in modern Greek</li>
		<li>net.semanlink.util.text.CharConverter: characters that are above 'z' when converted by a SimpleCharConverter are no more converted to the weirdCharSubstitution.
		They are now converted in the same way as by java.net.URLEncoder.encode(s,"UTF-8").</li>
		<li>net.semanlink.servlet.CoolUriServlet: patch the behaviour of HttpServletRequest.getPathInfo() when used for a request containing UTF8-encoded characters</li>
		</ul>
	</li>
	<li>Changes in the way tags are indexed by their labels. Corrects problems that existed when the label of a tag was changed.
	This will allow to use several labels for a tag (and index all of them), without the workaround based on aliases.</li>
	<li>Changes in the tag page and Display preferences. By default, a tag page now displays both the tree of descendants, and the list of all documents tagged with the tag or any of its descendants.
	Display preferences: two select menus instead of one: one for the display of descendants, one for the list of documents.</li>
	<li>Localisation : Strings of the application (labels of buttons,...) can now be stored in XML files, in order to allow the localisation of the application. Actual
	replacing of the strings in the application is a work in progress. The class net.semanlink.servlet.I18l contains a main and some tools to produce XML properties file ready
	to be translated.
	New lang parameter in context file. For now, the localisation concerns only the application as a whole (not on a web user basis).</li>
	<li>Thesaurus index is now loaded during init. Until now, it was loaded only at the time of the first request.</li>
	<li>Added a small piece of RDFa in the HTML page of a tag, to state that this page has a foaf:primaryTopic which is the tag (a non-information resource). This is a follow-up
	of the thread <a href="http://simile.mit.edu/mail/ReadMsg?listName=Linking%20Open%20Data&msgId=21256">"How to get the uri of a non-information resource from the corresponding HTML page?"</a> on the LOD mailing list.</li>
	<li>Added a "slc:publish" property for documents (expected values: "true" or "false"° Defaults to true for bookmarks,
	to false for local files.</li>
	<li>"AND" search for tags now lists the common descendants (and not only documents) (new Intersection class in net.semanlink.graph package)</li>
	<li>Added "Cut" and "Paste" buttons in "edit tag list" forms</li>
	<li>New conf/props.rdf file to list the properties available in the select menus in edit mode</li>
	<li>"Saving a copy" now works on dannyayers.com ("Saving a copy" first issues a "HTTP HEAD" to learn the content
	type of the file. If it is HTML, was HTTP GETting with an accept-header set to "text/html". But dannyayers.com in this case
	returns a 404. Now the accept header is set to "text/html, text/*, */*")</li>
	<li>The link to document on bookmark form was not working with FireFox. Now it does (at least with FF 2.0)</li>
	<li>Change in the css to have let Safari 3 displaying aqua buttons</li>
	<li>Added a link from a tag to its foaf:homepage</li>
</ul>
<h2>This is release 0.0.0 2007-03-26</h2>
<ul>
<li>Another count is 1789.07.14. Anyway, 0.0.0 seems better for the first release available as a free download.</li>
</ul>
<h2>2007-01-05</h2>
<ul>
<li>redirect after post now implemented</li>
</ul>
<h2>2007-01-01</h2>
<ul>
<li>"http-range14", with "303 redirect", now implemented</li>
</ul>
<h2>2006-10-15</h2>
<ul>
<li>Delicious synchronization</li>
</ul>
<h2>2006-07-11</h2>
<ul>
<li>Many changes in the way the set of semanlink files is defined and loaded at init</li>
<li>Tags now have a slash uri (no more a # one)</li>
</ul>
<h2>2006-05-10</h2>
<ul>
<li>Presentation at the Jena User Conference</li>
</ul>
<h2>2005-08-31</h2>
<ul>
<li>First release used at Renault in "Intelligence Artificielle Appliqu&eacute;e - SICG" department</li>
</ul>
<h2>2005-07-14</h2>
<ul>
<li>Live search and live tree, implemented using Ajax</li>
</ul>
<h2>2004-03-04</h2>
<ul>
<li>Creation date now automatically added. Used to decide in which file bookmarks are stored (no more in one big file)</li>
</ul>
<p></p></div>