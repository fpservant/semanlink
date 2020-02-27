<!--livesearchform.jsp-->
<%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*,net.semanlink.util.*, java.net.*, java.util.*, glguerin.io.*"%>
<%
/**
 * @see livesearch.js, qui documente le hidden input avec le href de la ligne sélectionnée (highlighted)
 * (dans une première version, simple, 
 * liveSearchSubmit "cliquait" sur la ligne sélectionnée (ou la 1ere) : l'action n'était invoquée que
 * lorsque la recherche avait été infructueuse)
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
        boolean resolveAlias = false;
        andTargetUri = HTML_Link.getTagURL(SLServlet.getServletUrl(), uri, resolveAlias, null);
    } else if (jsp instanceof Jsp_AndKws) { 
    	andTargetUri = ((Jsp_AndKws) jsp).getHref();
    }
    if ((jsp_doc != null) || (jsp_kw != null)) uri = jsp_r.getUriFormValue();
    boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
    String method, liveSearchSubmitFunction;
    if (edit) {
        method = "post";
        liveSearchSubmitFunction = "liveSearchSubmit4Post";
    } else {
        method = "get";
        liveSearchSubmitFunction = "liveSearchSubmit4Get";
    }
    System.out.println("livesearchform.jsp andTargetUri " + andTargetUri);
%>

<div class="browser">
    <div class="title trigger"><%=jsp.i18l("livesearchform.search")%></div>
    <form id="searchform" method="<%=method%>" onsubmit="return <%=liveSearchSubmitFunction%>();" action="<%=response.encodeURL(request.getContextPath()+"/gokeyword.do")%>">
        <p align="center"><input type="text" id="livesearch" name="q" onkeypress="liveSearchStart()" size="17" tabindex="1" />
        <input type="hidden" name="kwhref" value="" /><%//kwhref is documented with the href of the hghlighted line by liveSearchSubmitFunction%> 
        <%if (andTargetUri != null) { // 2020-02
            if (!edit) {%>
                <input type="hidden" name="targeturi" value="<%=andTargetUri%>" />
        <%}} // if uri != null%>
        <%if (edit) {
            if (uri != null) {%>
                <input type="hidden" name="targeturi" value="<%=uri%>" /><%//uri of this page (doc or tag)%>
                </p><p align="center" style="padding-left:4px;padding-top:0px;padding-bottom:4px;padding-right:0px;margin:0">
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
        <%} // i fedit %>
        </p>
    </form> 
    <div id="LSResult" style="display: none;">
        <div id="LSShadow" style="padding-left:4px">
        </div>
    </div>
</div> <%//browser%>
<!--/livesearchform.jsp-->