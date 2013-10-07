<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
	language="java"
	import="net.semanlink.servlet.*"
%>
<%String contextUrl = net.semanlink.util.Util.getContextURL(request); %>
<h2>Γρήγορη εκκίνηση</h2>
<h3>Εγκατάσταση σελιδοδείκτη (bookmarklet)</h3>
<ul><li>Ο σελιδοδείκτης αποτελεί το Α και το Ω του λογισμικού μας. Κι αυτό επειδή μας επιτρέπει να καταχωρούμε πολύ εύκολα τις ιστοσελίδες που μας ενδιαφέρουν. Για να εγκαταστήσουμε το σελιδοδείκτη : αν ο φυλλομετρητής σας είναι Internet Explorer κάντε δεξί κλικ στον παρακάτω σύνδεσμο και επιλέξτε προσθήκη στα αγαπημένα. Αν χρησιμοποιείτε Mozilla Firefox σύρτε τον παρακάτω σύνδεσμο στη μπάρα του διαφυλλιστή σας. Πλέον κάθε φορά που επισκέπτεστε μια ιστοσελίδα που σας ενδιαφέρει δεν έχετε παρά να κάνετε κλικ στο <jsp:include page="/jsp/bookmarklet_short.jsp"/>.   
    <%if (false) { %>
		<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/bookmarklet_more.htm">More...</a>
	<%} %>
</li>
</ul>
<h3>Για περισσότερες πληροφορίες ανατρέξτε στο <a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/getting_started_tutorial.htm">εγχειρίδιο «ξεκινώντας»</a>"</h3>
<ul><li>Περισσότερη τεκμηρίωση θα βρείτε στην ενότητα "<a href="<%=contextUrl%><%=CoolUriServlet.ABOUT_SERVLET_PATH%>/help.htm">Βοήθεια</a>" της αρχικής μας λίστας…
</li></ul>
<h3>Αν θέλετε <a href="<%=contextUrl%>/sl/delicious">μπορείτε να εισάγετε τις δικές σας καταχωρήσεις…</a></h3>
<h3>Καλή διασκέδαση !</h3>
