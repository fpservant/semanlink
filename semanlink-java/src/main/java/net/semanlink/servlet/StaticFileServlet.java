package net.semanlink.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.semanlink.util.servlet.BasicServlet;

// 2006/10 file outside dataFolders : this should not stay, as it is a high security risk
// (we allow to serve any file from the disk)

// AND WHAT'S ABOUT THE MIMETYPE ??? // TODO
// see http://www.javaworld.com/javaworld/javatips/jw-javatip94.html
// res.setContentType( "application/pdf" );  

/** Servlet used to send static files. */
public class StaticFileServlet extends HttpServlet {
public static final String PATH = "/document";
public static final String PATH_FOR_FILES_OUTSIDE_DATAFOLDERS = "/file"; 	// 2006/10 file outside dataFolders
public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	File f;
	boolean b404 = false;
	try {
		// We could serve any file with this servlet (including files outside the dataFolder)
		// (I implemented it with the "/file" servlet path)
		// we cannot do it using the url of the file as a parameter (cf .../?uri=...)
		// because it would break relative links inside an html file
		// But it can be done with something such as semanlink/file/rawpath/part/of/uri/of/file
		
		// WITHOUT CARE, THIS IS A SECURITY RISK THAT WE CANNOT TAKE
		// (would allow to serve any file from the disk!)
		// -> We accept serving the file if and only if it is
		// inside a DataFolder
		
		// -> we serve only files in a declared DataFolder
		// (ceci suppose pour marcher qu'on a bien le m�canisme du "webserver" activ� pour les datafolder: chaque datafolder ajoute aux webserver mappings
		
		/*
		if (req.getServletPath().equals("/file")) {
			// fonctionne, et safe, mais � supprimer (ainsi que le mapping file ds web.xml
			String debut = req.getContextPath() + req.getServletPath(); // /semanlink/file
			String s = req.getRequestURI(); // / semanlink/file/xxx
			s = s.substring(debut.length() + 1); // xxx

			// URI uribad = new URI ("file", "", s, null); // ceci induirait-il pas un quotage superflu (et m�me nuisible)
			// System.out.println("StaticFileServlet uribad " + uribad);
			int nbDeSlashAuDebut = 0;
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) != '/') break;
				nbDeSlashAuDebut++;
			}
			if (nbDeSlashAuDebut == 0) {
				s = "file:///" + s;
			} else if (nbDeSlashAuDebut == 1) {
				s = "file://" + s;
			} if (nbDeSlashAuDebut == 2) {
				s = "file:/" + s;
			}
			URI uri = new URI(s);

			f = new File(uri.getPath());
			
			if (!f.exists()) throw new RuntimeException("file " + f + " doesn't exist.");
			// est-il servi par notre webServer (cad est-il ds un DataFolder d�clar�)
			String suri = SLServlet.getWebServer().getURI(f);
			if (suri == null) throw new RuntimeException("Access to file " + f + " not allowed.");

		} else { */
			f = SLServlet.getWebServer().getFile(req.getRequestURL().toString());
			if (f == null) {
				// @find CORS pb with markdown:
				// defautDataFolder servi (aussi) en /document (parce que on est obligé de servir les .md via la servlet sl pour appels en ajax)
				// (2017-09 : ce qui est maintenant utilisé plus largement, me semble-t-il, en cas de webserver apache)
				File defaultDir = SLServlet.getWebServer().getDefaultDocFolder();
				if (defaultDir != null) {
					String s = defaultDir.getAbsolutePath();
					if (s.endsWith("/")) {
						s += req.getPathInfo().substring(1);
					} else {
						s += req.getPathInfo();
					}
					f = new File(s);
				}
			}
	  	
			if (f == null) {
				b404 = true;
				throw new RuntimeException("no such file, or access not allowed");
			}
			if (!f.exists()) {
				b404 = true;
				throw new RuntimeException("file " + f + " doesn't exist, or access not allowed.");
			}
		// }
		if (f.isDirectory()) {
			b404 = true; // TODO CHANGE
			throw new RuntimeException("file " + f + " is a directory -- not supported, sorry.");
			/*SLModel mod = SLServlet.getSLModel();
			SLDocument doc = mod.getDocument(mod.fileToUri(f));
			Jsp*/
		}
		BasicServlet.writeFile2ServletResponse(f, res);
	} catch (Exception e) {
		if (b404) {
			res.sendError(404);
		} else {
			res.sendError(500);
		}
	}
}


}
