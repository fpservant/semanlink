 /* Created on 2 oct. 06 */
package net.semanlink.servlet;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.semanlink.semanlink.Exporter;
import net.semanlink.semanlink.SLDataFolder;
import net.semanlink.semanlink.SLModel;
import net.semanlink.semanlink.SLModel.LoadingMode;
import net.semanlink.util.Util;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Action_Export extends BaseAction {
		public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
			ActionForward x = null;
			try {
		  	// Jsp_Page jsp = new Jsp_Page(request); // 2012-03
				Jsp_Welcome jsp = new Jsp_Welcome(request);
		  	String title;
		  	title = "Export";
				request.setAttribute("net.semanlink.servlet.jsp", jsp);
				// jsp.setContent("/jsp/export.jsp"); // 2012-03
				jsp.setContent("/jsp/welcome.jsp");
				jsp.setTitle(title);
				
				SLModel slMod = SLServlet.getSLModel();
				File exportDir = new File(SLServlet.getMainDataDir(),"export");
				String base = slMod.getDefaultThesaurus().getURI();
				if (!base.endsWith("/")) base += "/";
				SLDataFolder dataFolder = new SLDataFolder(exportDir, base, slMod.getDefaultThesaurus(), new LoadingMode("yearMonth absoluteBase"));
				Exporter exporter = new Exporter(slMod, dataFolder);
				
				int nbOfDays = -1;
				String s = request.getParameter("days");
				if (s != null) {
					try {
						nbOfDays = Integer.parseInt(s);
					} catch (NumberFormatException e) {}
				}
				exporter.export(nbOfDays);
				
				x = mapping.findForward("continue");
			} catch (Exception e) {
				return error(mapping, request, e );
			}
			return x;
		} // end execute
} // end Action
