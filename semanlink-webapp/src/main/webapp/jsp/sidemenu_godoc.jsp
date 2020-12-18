<!--sidemenu_godoc.jsp 2020-12-->
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
    boolean edit = (Boolean.TRUE.equals(session.getAttribute("net.semanlink.servlet.edit")));
%>

<div class="browser">
    <div class="title trigger"><%=jsp.i18l("sidemenu.godoc")%></div>
    <form id="sidemenu_godoc" method="get" action="<%=response.encodeURL(request.getContextPath()+"/godoc.do")%>">
        <p align="center"><input type="text" id="sidemenu_godoc_text" name="godoc_q" size="21" placeholder="url or phrase"/>
        </p>
    </form> 
</div> <%//browser%>
<!--/sidemenu_godoc.jsp-->