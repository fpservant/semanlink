<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
%>
<!--docs/about/intro.htm--><p>Semanlink est un <strong>système de gestion de graphe de connaissance</strong> basé sur <strong>RDF</strong>. Semanlink
	vous permet de décrire
	vos fichiers, marque-pages et de courtes notes qu'on peut y saisir, à l'aide de <strong>mots-clés</strong> (ou "tags", selon la
	terminologie à la mode), et d'autres métadonnées au format RDF
	En <strong>organisant vos
	tags sous forme de graphe</strong>, vous pourrez définir de façon incrémentale le vocabulaire qui vous servira
	à annoter vos documents.</p><p>L'information la plus récente sur Semanlink se trouve sur le site <a href="http://www.semanlink.net">www.semanlink.net</a></p><h2>Premiers pas</h2><p>The only thing to do now is to add the bookmarklet  to the navigation bar of your browser</p><h2>Presentation</h2><p>A  <a href="http://www.semanlink.net/files/2006/02/jena_semanlink_paper.htm">presentation		paper</a> describes the day to day use of Semanlink, in particular	how you create new entries and how you mark them with metadata (what you	won't be able to see in the demo which only shows consultation, not editing).	Here is a brief introduction:</p><h3>A personal personal knowledge graph management system</h3><p>Semanlink lets add RDF metadata to files, bookmarks		and short text notes that it allows to write,	organize and	display.</p><h3>A tagging utility</h3><p>Basically, it is a tagging utility: you use it to add tags to your documents.</p><h3>A tool to build thesauri as graphs of tags</h3><p>But Semanlink	also provides	 a	simple	way	to	organize	your		tags	in a	graph:	each tag may have several &quot;parents&quot; and &quot;children&quot;,	as well as other RDF properties.	Tagging	the tags allows	you	to	incrementally		define the vocabulary you use to annotate documents. This way,		you model	your	own representation of concepts and their relations.		Semanlink provides a GUI to easily navigate through the graph of tags.		And	of course, the taxonomy you build is used when searching: for instance,		a	document	taggedwith &quot;RDF&quot; will be found when searching for &quot;Semantic Web&quot;.</p><h3>A	file based RDF store</h3><p>By default, metadata about  files is stored in small RDF files, that	are	saved	in the same directory as the files they describe (or in a parent	directory). This is an important feature, not only because no database is	needed	to run	Semanlink:	for instance, when you save a copy of one directory to a CD or when you move	it to another location, metadata about its files is also copied or moved,	and ready to be used without any modification, as relative URLs are used	to	identify	the	files.</p><p>Metadata about bookmarks, as well as the short notes created within the application,	are	stored	in a directory organized with a &quot;year/month&quot; structure.</p><p>The definition of tags is written separately from the metadata about files:	a vocabulary can therefore easily be reused in another context. Several graphs	of	tags,	each with its own URI, can be used to mark a set of documents.</p><p>All	these RDF files are loaded into memory at startup.</p><h3>Comparing Semanlink with other information management systems</h3><p>To tagging systems, Semanlink adds an organization of tags' space that they	generally lack, and which is the	basis	for &quot;concept	navigation&quot; among	tags and documents.</p><p>Like hierarchical file systems, Semanlink makes extensive use of tree traversal.	But it adds to these hierarchies the possibility to store each document	in	several &quot;directories&quot; at	the same time (aliases or shortcuts, by the way, are not sufficient: given a	file, you cannot know to which directories its shortcuts belong to). While pathnames	are used to store metadata about files, system-level addressing and classification	data	are	cleanly	separated in Semanlink.</p><p>Like ontologies, Semanlink taxonomies are graph based. But keeping things	simple, it allows for dynamic growth and frequent modification	of	hierarchies.</p><h3>Semanlink is based on	Jena</h3><p>Semanlink runs as a servlet and has been developed with <a href="http://www.hpl.hp.com/semweb/index.html">Jena</a>. It uses plain	vanilla RDF memory models, which give very fast response time for any operation,	at least on my models (more than 20,000 statements about 3000 documents and	involving 2000 tags, as of this writing).</p><h2>Contact</h2><p>I am Fran&ccedil;ois-Paul Servant. You can contact me at fps[at]semanlink[dot]net</p><!--END docs/about/intro.htm-->