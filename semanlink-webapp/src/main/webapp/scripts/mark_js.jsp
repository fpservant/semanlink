<%@ page language="java" session="true" import="net.semanlink.servlet.*,net.semanlink.semanlink.*, java.util.*"%><%/** Returns the javascript used by bookmarklet - to be included within the hack defined at http://codinginparadise.org/weblog/2005/08/ajax-creating-huge-bookmarklets.html  * We don't use a static file, just because the script must include the address  * of the semanlink application.  * This solution has a trouble (which the version including this script directly in the bookmarklet didn't have :  * the back button doesn't bring you back to the bookmarked page. */%>s='';q='';l='';
t='';
<%
String mainFrame = SLServlet.getMainFrame();
if (mainFrame != null) { // case of an app with frames
%>if (parent.<%=mainFrame%>) {
	s=parent.<%=mainFrame%>.location;
} else {
	s=location.href;
}
<%
} else {
%>
s=location.href;
<%}%>if (window.getSelection) {    q=window.getSelection();} else if (document.getSelection) {    q=document.getSelection();} else if (document.selection) {	if (document.selection.createRange) {		q=document.selection.createRange().text;	}}
if (document.title) {
	t=document.title;
}

<%
// @find nir2tag
// look for a tag such as 
// <link rel="alternate" type="application/rdf+xml" href="http://dbpedia.org/data/William_of_Rubruck" title="RDF" />
// that would mean that this is an html page corresponding to a non information resource
%>
nir='';
el=document.getElementsByTagName('link');
if (el) {
	for(i=0;i < el.length;i++){		var rel = el[i].getAttribute('rel');		if (rel) {			var type = el[i].getAttribute('type');			if (type) {
				if (rel.indexOf('alternate')!=-1 && type.indexOf('application/rdf+xml')!=-1) nir=el[i].getAttribute('href');
			}		}
	}
}

s = encodeURIComponent(encodeURIComponent(s));
t = encodeURIComponent(encodeURIComponent(t));
q = encodeURIComponent(encodeURIComponent(q));
l = encodeURIComponent(l);
nir = encodeURIComponent(encodeURIComponent(nir));

<%if (false) {%>
r='';
if (document.referrer) r=document.referrer;
if (typeof(_ref)!='undefined') r=_ref;
r = encodeURIComponent(r);
location.href='<%=net.semanlink.util.Util.getContextURL(request)%>/bookmarkform.do?docuri='+s+'&title='+t+'&comment='+q+'&lang='+l+'&via='+r;
<%}%>

location.href='<%=net.semanlink.util.Util.getContextURL(request)%>/bookmarkform.do?docuri='+s+'&title='+t+'&comment='+q+'&lang='+l+'&nir='+nir;
