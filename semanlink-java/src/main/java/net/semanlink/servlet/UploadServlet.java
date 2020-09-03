/* Created on Sep 2, 2020 */
package net.semanlink.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import net.semanlink.util.CopyFiles;

// @WebServlet("sl/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      Part file = request.getPart("file");
      String filename = getFilename(file);
      InputStream filecontent = file.getInputStream();
      // ... Do your file saving job here.
      File dir = SLServlet.getSLModel().goodDirToSaveAFile();
      File f = new File(dir, filename);
      if (f.exists()) {
      	// throw new RuntimeException("file already exists, no overwrite"); // TODO
      }
      CopyFiles.writeIn2Out(filecontent, new BufferedOutputStream(new FileOutputStream(f)), new byte[5000]);
      
      System.out.println("filename " + filename + "sub_dir:" + request.getParameter("sub_dir"));
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write("File " + filename + " successfully uploaded");
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
}