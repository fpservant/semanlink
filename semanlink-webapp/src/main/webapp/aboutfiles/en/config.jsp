<%@ page
    contentType="text/html;charset=UTF-8" 
    pageEncoding="UTF-8"
%><h1>Semanlink Configuration</h1><div class="graybox">

<!--<p><a href="../../statix/semanlink-config-with-comments.xml">RDF configuration file example</a></p>
 --><h2>Where is located configuration information?</h2><p>The <strong>semanlink-config.xml</strong> file is the main configuration file of the semanlink  servlet and the semanlink &quot;datastore&quot;.</p><p>(This file is located in the directory defined by the &quot;semanlinkDataDir&quot; parameter  of the &quot;context&quot; of web application, see [distributionRootDirectory]/READ-ME.htm)</p><p>All semanlink configuration is included in the <strong>semanlink-config.xml    file</strong>,    EXCEPT the part included in the definition of the &quot;context&quot; of    the web application<br>  (cf [distributionRootDirectory]/READ-ME.htm), that is to say:</p><ul>  <li>    the url of the servlet  </li>  <li>    and the path to the parent directory of this file (the &quot;semanlinkDataDir&quot;)  </li></ul>
<h2>Main purpose of <strong>semanlink-config.xml</strong> </h2><p>The main purpose of semanlink-config.xml file is to declare:</p><ul>  <li>    the thesauri        (set of tags)  </li>  <li>    and the &quot;data folders&quot; (the directories containing    documents and their metadata).  </li></ul>
<h2>When and how this file is loaded</h2><ul>  <li>    This file is read        by the servlet during its init.  </li>  <li>    Important note: when this file gets loaded a base equal      to the slash terminated url of servlet is used. Uris relative to the servlet      can    therefore be used in the file.  </li></ul>

<h2>TO BE COMPLETED</h2>
 // TO DO</div>