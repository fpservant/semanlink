<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	import="net.semanlink.servlet.*"
%>
<%
String contextUrl = net.semanlink.util.Util.getContextURL(request);
%>
<h2>Άδεια</h2>
<ul>
<li>ο Semanlink είναι ένα λογισμικό ελεύθερης χρήσης (freeware). Ωστόσο είναι απαραίτητο σε περίπτωση που χρησιμοποιείται στο πλαίσιο μιας ιστοσελίδας να αναφέρετε το δημιουργό του François-Paul Servant, fps [at] semanlink.net, www.semanlink.net</li>
<li>Το χρησιόποιειτε με δικη σας ευθυνη</li>
<li>Το Semanlink χρησιμοποιεί αρκετά ελεύθερης χρήσης προγράμματα. <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/credits.htm">Ευχαριστίες...</a></li>

</ul>
