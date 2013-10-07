<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
%><h1>About the bookmarklet</h1><div class="graybox">
	<h2>Using the bookmarklet</h2>
	<ul>	<li>If you click the boorkmarklet on a page that is already stored in
		semanlink, you will get the metadata about
		that page (a second bookmark won't be created).</li>
	<li>Text selected in the page beeing marked is automatically added to the comment field of the form</li>
	<li>clicking the back of the browser deson't bring you back to the page from where you come (but to the one you visited before).
	To go back to the page where you clicked the bookmarklet, click the "document's uri" link in the form .</li>
	</ul>
	<h2>Tip</h2>
	<p>Paste the link defining the bookmarklet into	an HTML page to link it to	its	metadata.</p>
	<h2>About the implementation</h2>
	<ul>
		<li>based on an idea described at <a href="http://codinginparadise.org/weblog/2005/08/ajax-creating-huge-bookmarklets.html">http://codinginparadise.org/weblog/2005/08/ajax-creating-huge-bookmarklets.html</a></li>
		<li>pdf can be marked with  Firefox 1.5, but not with IE</li>
		<li>Known problems : <ul>
			<li>browser's back button doesn't work (use "document's uri" hyperlink in the form as a workaround)</li>
			<li>doesn't work on W3C's home page</li>
			<li>on a given page displaying a pdf file, (at least with Safari), works only once (you have to reload page to have it working again)</li>
			</ul>
		</li>
	</ul>