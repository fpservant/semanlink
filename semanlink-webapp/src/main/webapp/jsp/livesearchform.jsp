<!--livesearchform.jsp-->
<%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.net.*, java.util.*, glguerin.io.*"%>
<%
/**
 * @see livesearch.js, qui documente le hidden input avec le href de la ligne s�lectionn�e (highlighted)
 * (dans une premi�re version, simple, 
 * liveSearchSubmit "cliquait" sur la ligne s�lectionn�e (ou la 1ere) : l'action n'�tait invoqu�e que
 * lorsque la recherche avait �t� infructueuse)
 */
%>
<%
    Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
    Jsp_Resource jsp_r = null;
    if (jsp instanceof Jsp_Resource) jsp_r = (Jsp_Resource) jsp;
    Jsp_Document jsp_doc = null;
    Jsp_Keyword jsp_kw = null;
    String uri = null;
    // this is used in the searchform to pass back the page we are on when the user will do a search
    // (this is necessary, as the searchform runs in ajax, for instance to make the AND)
    // for instance, we are on the page kw1 andTargetUri will contain kw1, and we want 
    // to display results which link to kw1 AND search result
    // Here, we put it in the form (as hidden)
    // see livesearch.js liveSearchDoSearch() to see how it is used (in tree.js to)
    String andTargetUri = null; // 2020-02 tagAndTag
    if (jsp instanceof Jsp_Document)  {
        jsp_doc = (Jsp_Document) jsp;
        uri = jsp_r.getUriFormValue();
    } else if (jsp instanceof Jsp_Keyword) {
        jsp_kw = (Jsp_Keyword) jsp;
        uri = jsp_r.getUriFormValue();
        // hum pb uri de tags -> url de tag (cf en semanlink.net vs 127.0.0.1/semanlink)
        // andTargetUri = uri;
        
        // 2020-03 sauf que ce dont on a besoin pour faire le and ensuite, c'est le kw, pas son url
        // andTargetUri = HTML_Link.getTagURL(SLServlet.getServletUrl(), uri, resolveAlias, null);
        // OUI, CA CRAINT, C'EST VILAIN
        
        andTargetUri = jsp_kw.getSLKeyword().getURI();
        
    } else if (jsp instanceof Jsp_AndKws) { // TagAndTags   	
    	andTargetUri = ((Jsp_AndKws) jsp).getHref();
    
    }
    if ((jsp_doc != null) || (jsp_kw != null)) uri = jsp_r.getUriFormValue();
    boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
    String method, liveSearchSubmitFunction;
    int size;
    String align;
    if (edit) {
        method = "post";
        liveSearchSubmitFunction = "liveSearchSubmit4Post";
        
        size = 16; // pour avoir "New" button sur la m�me ligne
        align = "center";
    } else {
        method = "get";
        liveSearchSubmitFunction = "liveSearchSubmit4Get";
        
        size = 21;
        align = "center";
    }
%>

<div class="browser">
    <div class="title trigger"><%=jsp.i18l("livesearchform.search")%></div>
    <form id="searchform" method="<%=method%>" onsubmit="return <%=liveSearchSubmitFunction%>();" action="<%=response.encodeURL(request.getContextPath()+"/gokeyword.do")%>">
        <p align="<%=align%>"><input type="text" id="livesearch" name="q" onkeypress="liveSearchStart()" size="<%=size%>" tabindex="1" placeholder="<%=jsp.i18l("livesearchform.search.placeholder")%>"/>
        <input type="hidden" name="kwhref" value="" /><%//kwhref is documented with the href of the hghlighted line by liveSearchSubmitFunction%> 
        <%if (andTargetUri != null) { // 2020-02
            if (!edit) {%>
                <input type="hidden" name="targeturi" value="<%=andTargetUri%>" />
        <%}} // if uri != null%>
        <%if (edit) {
            if (uri != null) {
                %>
                 <!-- BEWARE, by default, button is type submit. Must not be here -->
                 <button type="button" onclick="if (askConfirmationCreateTag()) {document.getElementById('searchform').submit()}; return false;">New</button>
                 </p><p align="<%=align%>" style="padding-left:4px;padding-top:0px;padding-bottom:4px;padding-right:0px;margin:0">
                 <input type="hidden" name="targeturi" value="<%=uri%>" /><%//uri of this page (doc or tag)%>
                    <select name="actionprop">
                        <%if (jsp_doc != null) {%>
                            <option value="add2doc"><%=jsp.i18l("livesearchform.addToTags")%></option>
                        <%} else if (jsp_kw != null) { %>
                            <option value="add2parents"><%=jsp.i18l("livesearchform.addToParents")%></option>
                            <option value="add2friends"><%=jsp.i18l("livesearchform.addToFriends")%></option>
                            <option value="add2children"><%=jsp.i18l("livesearchform.addToChildren")%></option>
                        <%}%>
                        <option value="go"><%=jsp.i18l("livesearchform.go")%></option>
                    </select>
            <%} // if uri != null%>
            
        <%} // if edit %>
        </p>
    </form> 
    <div id="LSResult" style="display: none;">
        <div id="LSShadow" style="padding-left:4px">
        </div>
    </div>
</div> <%//browser%>
<!--/livesearchform.jsp-->