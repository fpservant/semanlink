<!--delicious.jsp--><%@ page language="java" session="true" import="net.semanlink.semanlink.*,net.semanlink.servlet.*, java.util.*, net.semanlink.delicious.*"%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%Jsp_Page jsp = (Jsp_Page) request.getAttribute("net.semanlink.servlet.jsp");
DeliciousSynchro deliciousSynchro = (DeliciousSynchro) session.getAttribute("net.semanlink.delicious.DeliciousSynchro");%><script language="JavaScript" type="text/JavaScript">
	function deliciousSynchroNextAction() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),request.getContextPath() + "/deliciousajax.do?what=synchronext")).toString())%>";
	}
	
	function deliciousImportNextAction() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),request.getContextPath() + "/deliciousajax.do?what=importnext")).toString())%>";
	}
	
	function deliciousExportNextAction() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),request.getContextPath() + "/deliciousajax.do?what=exportnext")).toString())%>";
	}
	
	function deliciousImportBundlesAction() {
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),request.getContextPath() + "/delicious.do?what=importbundles")).toString())%>";
	}
	
	function deliciousAction() { // url of this page
		return "<%= response.encodeURL((new java.net.URL(new java.net.URL(request.getRequestURL().toString()),request.getContextPath() + "/delicious.do")).toString())%>";
	}
	
	var whatReq = false;
	var synchroNextReq = false;
	var importNextReq = false;
	var exportNextReq = false;
	var importBundlesReq = false;

	var synchrostopped = false;
	var importstopped = false;
	var exportstopped = false;

	function deliciousaction(what) {
		// alert("deliciousaction " + what);
		url = "";
		processStateChange = false;
		ajax = true;
		if (what == "synchronext") {
			url = deliciousSynchroNextAction();
			processStateChange = synchroNextProcessStateChange;
			stopsynchrobtn = document.getElementById("stopsynchrobtn");
			with (stopsynchrobtn.style) {
					display="block";
			}
			synchrobtn = document.getElementById("synchrobtn");
			with (synchrobtn.style) {
					display="none";
			}
			synchrostopped = false;
			
		} else if (what == "stopsynchro") {
			ajax = false;
			synchrostopped = true;
			waitstopsynchro = document.getElementById("waitstopsynchro");
			with (waitstopsynchro.style) {
					display="block";
			}
			stopsynchrobtn = document.getElementById("stopsynchrobtn");
			with (stopsynchrobtn.style) {
					display="none";
			}

		} else if (what == "importnext") {
			url = deliciousImportNextAction();
			processStateChange = importNextProcessStateChange;
			stopbtn = document.getElementById("stopbtn");
			with (stopbtn.style) {
					display="block";
			}
			importpostbtn = document.getElementById("importpostbtn");
			with (importpostbtn.style) {
					display="none";
			}
			importstopped = false;
			
		} else if (what == "stop") {
			ajax = false;
			importstopped = true;
			importpostbtn = document.getElementById("importpostbtn");
			with (importpostbtn.style) {
					display="block";
			}
			stopbtn = document.getElementById("stopbtn");
			with (stopbtn.style) {
					display="none";
			}

		} else if (what == "importbundles") {
			// url = deliciousImportBundlesActionAjax();
			// processStateChange = importBundlesProcessStateChange;
			ajax = false;
			url = deliciousImportBundlesAction();
			location.href = url;
			

		} else if (what == "exportnext") {
			url = deliciousExportNextAction();
			processStateChange = exportNextProcessStateChange;
			stopbtn = document.getElementById("stopexportbtn");
			with (stopexportbtn.style) {
					display="block";
			}
			exportpostbtn = document.getElementById("exportpostbtn");
			with (exportpostbtn.style) {
					display="none";
			}
			exportstopped = false;

		} else if (what == "stopexport") {
			ajax = false;
			exportstopped = true;
			stopexportbtn = document.getElementById("stopexportbtn");
			with (stopexportbtn.style) {
					display="none";
			}
			waitstopexport = document.getElementById("waitstopexport");
			with (waitstopexport.style) {
					display="block";
			}

		}
		if (ajax == true) {
			if (window.XMLHttpRequest) { // Non-IE browsers
				whatReq = new XMLHttpRequest();
				whatReq.onreadystatechange = processStateChange;
				try {
					whatReq.open("GET", url, true);
				} catch (e) {
					alert(e);
				}
				whatReq.send(null);
			} else if (window.ActiveXObject) { // IE
				isIE = true;
				whatReq = new ActiveXObject("Microsoft.XMLHTTP");
				if (whatReq) {
					whatReq.onreadystatechange = processStateChange;
					whatReq.open("GET", url, true);
					whatReq.send();
				}
			}
		}
		if (what == "synchronext") {
			synchroNextReq = whatReq;
		} else if (what == "importnext") {
			importNextReq = whatReq;
		/*} else if (what == "importbundles") {
			importBundlesReq = whatReq;*/
		} else if (what == "exportnext") {
			exportNextReq = whatReq;
		}
	}
	
	/*function deliciousimportnext() {
		url = deliciousImportNextAction();
		if (window.XMLHttpRequest) { // Non-IE browsers
			importReq = new XMLHttpRequest();
			importReq.onreadystatechange = importProcessStateChange;
			try {
				importReq.open("GET", url, true);
			} catch (e) {
				alert(e);
			}
			importReq.send(null);
		} else if (window.ActiveXObject) { // IE
			isIE = true;
			importReq = new ActiveXObject("Microsoft.XMLHTTP");
			if (importReq) {
				importReq.onreadystatechange = importProcessStateChange;
				importReq.open("GET", url, true);
				importReq.send();
			}
		}
	}*/
	
	<%
	// Problem if you leave the page, go back and click again on deliciousimport:
	// very fast loop appending the latest response. Why???
	// hence a hack with var latestImportNextResponseText
	%>
	var latestImportNextResponseText = "";
	function importNextProcessStateChange() {
		if (importNextReq.readyState == 4) { // Complete
			if (importNextReq.status == 200) { // OK response
				elem = document.getElementById("deliciousposts");
				// THE HACK : if we get same thing twice in row, there's a problem,
				// and we try to solve it.
				if (latestImportNextResponseText == importNextReq.responseText) {
					latestImportNextResponseText = "";
					location.href = deliciousAction();
					deliciousimportnext();
				} else {
					latestImportNextResponseText = importNextReq.responseText;
					elem.innerHTML = importNextReq.responseText;
					if (importNextReq.responseText.indexOf("IMPORT COMPLETED") < 0) {
						importNextReq = false;
						if (!importstopped) {
							deliciousaction("importnext");
						}
					} else { // IMPORT COMPLETED
						location.href = deliciousAction();
					}
				}
			} else {
				alert("Problem (importNextProcessStateChange): " + importNextReq.statusText);
			}
		}
	}
	
	var latestExportNextResponseText = "";
	function exportNextProcessStateChange() {
		if (exportNextReq.readyState == 4) { // Complete
			if (exportNextReq.status == 200) { // OK response
				elem = document.getElementById("deliciousexport");
				// THE HACK : if we get same thing twice in row, there's a problem,
				// and we try to solve it.
				if (latestExportNextResponseText == exportNextReq.responseText) {
					latestExportNextResponseText = "";
					location.href = deliciousAction();
					deliciousexportnext();
				} else {
					latestExportNextResponseText = exportNextReq.responseText;
					elem.innerHTML = exportNextReq.responseText;
					if (exportNextReq.responseText.indexOf("EXPORT COMPLETED") < 0) {
						exportNextReq = false;
						if (!exportstopped) {
							deliciousaction("exportnext");
						} else {
							exportpostbtn = document.getElementById("exportpostbtn");
							with (exportpostbtn.style) {
								display="block";
							}
							waitstopexport = document.getElementById("waitstopexport");
							with (waitstopexport.style) {
								display="none";
							}					
						}
					} else { // EXPORT COMPLETED
						location.href = deliciousAction();
					}
				}
			} else {
				alert("Problem (exportNextProcessStateChange): " + exportReq.statusText);
			}
		}
	}
	
	var latestSynchroNextResponseText = "";
	function synchroNextProcessStateChange() {
		if (synchroNextReq.readyState == 4) { // Complete
			if (synchroNextReq.status == 200) { // OK response
				// alert(synchroNextReq.responseText);
				elem = document.getElementById("delicioussynchro");
				/*// THE HACK : if we get same thing twice in row, there's a problem,
				// and we try to solve it.
				if (latestSynchroNextResponseText == synchroNextReq.responseText) {
					latestSynchroNextResponseText = "";
					location.href = deliciousAction();
					delicioussynchronext();
				} else {*/
					latestSynchroNextResponseText = synchroNextReq.responseText;
					elem.innerHTML = synchroNextReq.responseText;
					if (synchroNextReq.responseText.indexOf("SYNCHRO COMPLETED") < 0) {
						synchroNextReq = false;
						if (!synchrostopped) {
							deliciousaction("synchronext");
						} else {
							synchrobtn = document.getElementById("synchrobtn");
							with (synchrobtn.style) {
								display="block";
							}				
							waitstopsynchro = document.getElementById("waitstopsynchro");
							with (waitstopsynchro.style) {
								display="none";
							}
						}
					} else { // SYNCHRO COMPLETED
						location.href = deliciousAction();
					}
				//}
			} else {
				alert("Problem (synchroNextProcessStateChange): " + synchroReq.statusText);
			}
		}
	}
	
</script><div class="graybox">		<div class="what"><%=jsp.i18l("delicious.account")%></div>	<html:form action="delicious.do" method="post">		<%=jsp.i18l("delicious.username")%> <html:text property="user"/> <%=jsp.i18l("delicious.password")%> <html:password property="password"/> <html:submit property="okbtn">OK</html:submit>	</html:form>		<%	if (deliciousSynchro != null) {
		%>
		<p>Note: Delicious requires a delay of 2 seconds between requests: don't expect export and synchronization to be very fast. But you can suspend
		execution and resume later</p>
		<p>If you never have imported your delicious posts, import them before using synchronization.</p>
		<p>Delicous posts are imported in semanlink with a creation date equal to their creation date in delicious: don't be surprised
		if you don't see them in the "New entries" tab.</p>
		<p>It could be wise to save your account before exporting or synchronizing.
		<%

		//
		// SYNCHRO
		//
				%>		<div class="what"><span id="synchrobtn" style="display:block"><a href="#synchro" onclick="deliciousaction('synchronext');return false;">Synchronize with delicious</a></span>
		<span id="stopsynchrobtn" style="display:none"><a href="#stopsynchro" onclick="deliciousaction('stopsynchro');return false;">Suspend synchro</a></span><span id="waitstopsynchro" style="display:none">PLEASE WAIT...</span>
		</div>
		<%
		if (deliciousSynchro.isSynchroCompleted()) {
				%><p>Synchro completed!</p><%
		}
		%>
		<ul class="maindoclist" id="delicioussynchro">
		</ul>

		<%
		//
		// IMPORT
		//
		 %>
		 
		<div class="what"><span id="importpostbtn" style="display:block"><a href="#import" onclick="deliciousaction('importnext');return false;">Import posts from delicious</a></span>
		<span id="stopbtn" style="display:none"><a href="#stop" onclick="deliciousaction('stop');return false;">Suspend import</a></span><span id="waitstop" style="display:none">PLEASE WAIT...</span>
		</div>
		<%
		boolean isImportPostsCompleted = deliciousSynchro.isImportPostsCompleted();
		List importedAl = deliciousSynchro.getImportedList();
		if ((importedAl == null) || (importedAl.size() == 0) || (isImportPostsCompleted)) {
			String lastDate = deliciousSynchro.getLastDeliciousImportDate();
			if (lastDate != null) {
				%><p>Date of latest imported delicious posts: <%=lastDate%></p><%
			}
			if (isImportPostsCompleted) {
				if ((importedAl == null) || (importedAl.size() == 0)) {
					%><p>IMPORT COMPLETED! (there was no new post to import)</p><%
				} else {
					%><p>IMPORT COMPLETED! <%=importedAl.size()%> posts imported</p><%
				}
			}
		}

		if (false) {
			int nn = 0;
			int n0 = 0;
			if (importedAl != null) {
					//on en affiche 20 au max
					nn = importedAl.size();
					if (nn > 20) {
						n0 = nn - 20;
						%><p>20 most recent imported documents:</p><%
					}
			}
		}
		// Attention ci-dessous : le ul sert même si aucun elts dedans (ajax script ecrit dedans)
		%>
		<ul class="maindoclist" id="deliciousposts">
			<%
			if (importedAl != null) {
				int kk = 0;
				for (int i = importedAl.size()-1; i < -1; i--) {
					SLDocument doc = (SLDocument) importedAl.get(i);
					request.setAttribute("net.semanlink.servlet.jsp.currentdoc", doc);
					request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", doc.getKeywords());
					String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(doc);
					%>
					<jsp:include flush="true" page="<%=docLineJspName%>"></jsp:include>
					<%
					kk++;
					if (kk > 19) {
						%><li>[Only the most recent imports are listed]</li><%
						break;
					}
				}
			} // if (importedAl != null)
			%>				</ul>

		<%
		//
		// IMPORT BUNDLES
		//
		 %>
			<div class="what"><a href="#importbundles" onclick="deliciousaction('importbundles');return false;">Import bundles from delicious</a></div>
		<ul class="maindoclist" id="deliciousbundles">			<%
			List bundlesAsSLTagList = deliciousSynchro.getBundlesAsSLTagList();
			if (bundlesAsSLTagList != null) {
				request.setAttribute("livetreelist", bundlesAsSLTagList);
				%>
					<ul class="livetree">
						<jsp:include flush="true" page="/jsp/livetreesons.jsp"></jsp:include>
					</ul>
				<%
			}
			%>
		</ul>		<%if (bundlesAsSLTagList != null) {
			%><p>IMPORT OF BUNDLES COMPLETED!</p><%
		}	}
	String errorMess = (String) request.getAttribute("errorMess");	
	if (errorMess != null) {%><%=errorMess%><%}

	//
	// EXPORT
	//

	if (deliciousSynchro != null) {
		%>
		<div class="what"><span id="exportpostbtn" style="display:block"><a href="#export" onclick="deliciousaction('exportnext');return false;">Export documents to delicious</a></span>
		<span id="stopexportbtn" style="display:none"><a href="#stopexport" onclick="deliciousaction('stopexport');return false;">Suspend export</a></span><span id="waitstopexport" style="display:none">PLEASE WAIT...</span>
		</div>

		<%
		List exportedAl = deliciousSynchro.getExportedList();

		boolean isExportPostsCompleted = deliciousSynchro.isExportPostsCompleted();
		if ((exportedAl == null) || (exportedAl.size() == 0) || (isExportPostsCompleted)) {
			String lastExportDate = deliciousSynchro.getLastDeliciousExportDate();
			if (lastExportDate != null) {
				%><p>Creation date of latest document exported to delicious: <%=lastExportDate%></p><%
			}
			if (isExportPostsCompleted) {
				if ((exportedAl == null) || (exportedAl.size() == 0)) {
					%><p>EXPORT COMPLETED! (there was no new document to be exported)<%
				} else {
					%><p>EXPORT COMPLETED! <%=exportedAl.size()%> documents exported</p><%
				}
			}
		}

		if (false) {
			int nn = 0;
			int n0 = 0;
			if (exportedAl != null) {
					//on en affiche 20 au max
					nn = exportedAl.size();
					if (nn > 20) {
						n0 = nn - 20;
						%><p>20 most recent exported documents:</p><%
					}
			}
		}
		// Attention ci-dessous : le ul sert même si aucun elts dedans (ajax script ecrit dedans)
		%>
		<ul class="maindoclist" id="deliciousexport">
			<%
			// if (false) {
				if (exportedAl != null) {
					int kk = 0;
					for (int i = exportedAl.size()-1; i > -1; i--) {
						SLDocument doc = (SLDocument) exportedAl.get(i);
						request.setAttribute("net.semanlink.servlet.jsp.currentdoc", doc);
						request.setAttribute("net.semanlink.servlet.jsp.currentdoc.kws", doc.getKeywords());
						String docLineJspName = Manager_Document.getDocumentFactory().getDocLineJspName(doc);
						%>
						<jsp:include flush="true" page="<%=docLineJspName%>"></jsp:include>
						<%
						kk++;
						if (kk > 19) {
							%><li>[Only the most recent exported documents are listed]</li><%
							break;
						}
					}
				} // if (exportedAl != null)
			// } // if false
			%>		
		</ul>
		<%
	}
	String errorExportMess = (String) request.getAttribute("errorExportMess");	
	if (errorExportMess != null) {%><%=errorExportMess%><%}
	%>
	</div>

<!-- /delicious.jsp-->