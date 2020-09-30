/* 2020-09 */
package net.semanlink.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import net.semanlink.util.CopyFiles;

// @WebServlet("sl/upload")

/**
 * either upload of a file, or creation of a subdir 
 */
@MultipartConfig
public class UploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	try {
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
  		
      // dir to upload to. Either a param of request, or goodDirToSaveAFile
      File dir = toDir(request);

      // subdir creation: if createSubDir param (supposed to contain name of subdir to be created)
      String createSubDir = request.getParameter("createSubDir");
      if (createSubDir != null) {
      	File f = new File (dir, createSubDir);
      	if (f.exists()) {
      		throw new RuntimeException("Already exixts: " + f.getName());
      	} else {
        	boolean done = f.mkdir();
        	if (done) {
            response.getWriter().write("Dir " + f.getName() + " successfully created");
        	} else {
        		response.setStatus(500);
        		response.getWriter().write("Dir " + f.getName() + " not created");      		
        	}
      	}
      } else {
      	// file upload
    		Part file = request.getPart("file");
        if (file == null) {      	
          	throw new RuntimeException("No 'file' part"); // TODO     
        }
        String filename = getFilename(file);
        InputStream filecontent = file.getInputStream();
        
        File f = new File(dir, filename);
        if (f.exists()) {
        	throw new RuntimeException("file already exists, no overwrite"); // TODO
        }
      	
      	// real upload
        CopyFiles.writeIn2Out(filecontent, new BufferedOutputStream(new FileOutputStream(f)), new byte[5000]);
        
        // System.out.println("filename " + filename + "sub_dir:" + request.getParameter("sub_dir")); // 2020-09 TODO
        response.getWriter().write("File " + filename + " successfully uploaded");
      }
   	} catch (Exception e) {
      // response.sendError(400, "Failed upload: " + e.getMessage());
      response.setStatus(400); // TODO
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write("Failed upload: " + e.getMessage());  		
  	}
  }

  private static String getFilename(Part part) {
      for (String cd : part.getHeader("content-disposition").split(";")) {
          if (cd.trim().startsWith("filename")) {
              String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
              return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
          }
      }
      return null;
  }
  
  // dir to upload to. Either a param of request, or goodDirToSaveAFile
  private File toDir(HttpServletRequest request) throws IOException {
  	File dir = null;
    String uploadDirUri = request.getParameter("uploadDirUri");
    if ((uploadDirUri != null) && (!"".equals(uploadDirUri))) {
    	try {
				dir = SLServlet.getSLModel().getFile(uploadDirUri);
				if (!dir.exists()) {
					throw new RuntimeException("no such dir " + uploadDirUri);
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
    }
    if (dir == null) {
    	dir = SLServlet.getSLModel().goodDirToSaveAFile();
    }
    return dir;
  }
}