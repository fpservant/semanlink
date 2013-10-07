package net.semanlink.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

// import javax.servlet.ServletResponse;

/**
 * utile quand on a affaire à des dossiers et des fichiers.
 *
 * @author hyperFP
 * @version 0.2
 */
public class CopyFiles {
	
/**
 * 	
 * @param src
 * @param dst
 * @throws IOException 
 */
public static void osCopy(final File src,final File dst) throws IOException {
	if (src==null) {
		throw new IllegalArgumentException("copy: src undefined");		
	}
	if (dst==null) {
		throw new IllegalArgumentException("copy: src undefined");
	}
	if (src.canRead()) {
		
	}
	else {
		throw new IllegalArgumentException("copy: "+src.getAbsolutePath()+" is not readable");
	}	
	
	final String osName = System.getProperty("os.name" );
	final String[] cmd = new String[3];
	if (osName.equals("Windows NT") || osName.equals("Windows XP")) {		
		cmd[0] = "cmd.exe"; 
		cmd[1] = "/C"; 
		cmd[2] = "copy "+src.getAbsolutePath()+" "+dst.getAbsolutePath(); 
	}
	else {
		cmd[0] = "sh"; 
		cmd[1] = "-c"; 
		cmd[2] = "cp "+src.getAbsolutePath()+" "+dst.getAbsolutePath(); 
	}
	Runtime.getRuntime().exec(cmd); 	
}
	
//
//	 COPIER UN FICHIER
//
/** copie un fichier dans un autre. */
public static void copyFile(File source, File dest) throws IOException {
	copyFile(source, dest, new byte[1024]);
}
/**
 * 
 * @param source
 * @param destDir créée si elle n'existe pas
 * @param overwrite true to overwrite file if already exists
 * @return the dest file
 * @throws IOException
 */
public static File copyFile2Dir(File source, File destDir, boolean overwrite) throws IOException {
	if (!destDir.exists()) {
		destDir.mkdirs();
	}
	String sn = source.getName();
	File x = new File(destDir, sn);
	if (!overwrite) {
		if (x.exists()) {
			String dotExtension = Util.getExtension(sn);
			String woExtension = null;
			if ("".equals(dotExtension)) {
				woExtension = sn;
			} else {
				woExtension = Util.getWithoutExtension(sn);
			}
			for(int i = 2;;i++) {
				x = new File(destDir, woExtension + "-" + Integer.toString(i) + dotExtension);
				if (!(x.exists())) break;
			}
		}
	}
	copyFile(source, x);
	return x;
}

/**  copie un fichier dans un autre.
@param buffer sert à bufferiser le transfert. */
public static void copyFile(File source, File dest, byte[] buffer) throws IOException {
	writeFile2OutputStream(source, new FileOutputStream(dest), buffer);
}

/** lit un fichier et l'écrit sur un OutputStream, (via un BufferedOutputStream).
@param buffer sert à bufferiser le transfert. Passer un byte[] de dimension qui vous semble raisonnable. */
public static void writeFile2OutputStream(File source, OutputStream os, byte[] buffer) throws IOException {
	BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
	BufferedOutputStream out = new BufferedOutputStream(os);
	writeIn2Out(in, out, buffer);
	out.close();
	in.close();
}

/*// moved to BasicServlet, to avoid to have to include servlet stuff
public static void writeFile2ServletResponse(File source, ServletResponse res) throws IOException {
	// we do not want to close the res OutputStream
	// CopyFiles.writeFile2OutputStream(source, res.getOutputStream(), new byte[res.getBufferSize()]);
	BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
	writeIn2Out(in, res.getOutputStream(), new byte[res.getBufferSize()]);
	in.close();
}
*/



/** write in to out
@param buffer to bufferize transfer. */
public static void writeIn2Out(InputStream in, OutputStream out, byte[] buffer) throws IOException {
	int c;
	int len = buffer.length;
	while ((c = in.read(buffer,0,len)) > -1) {
		out.write(buffer,0,c);
		// System.out.print(new String(buffer));
	}
	out.flush();
}


//
//	 CHARGER UN FICHIER DS UN TABLEAU DE BYTES
//


public static byte[] stream2bytes(InputStream is) throws IOException {
	return stream2bytes(is, 1024);
}
public static byte[] stream2bytes(InputStream is, int buffSize) throws IOException {
	InputStream in;
	if (! (is instanceof BufferedInputStream)) {
		in = new BufferedInputStream(is);
	} else {
		in = is;
	}
	
	int c;
	byte[] buffer0 = new byte[buffSize];
	byte[] buffer = buffer0;
	ArrayList v = new ArrayList();
	ArrayList vSizes = new ArrayList();
	int nx = 0;
	while ((c = in.read(buffer,0,buffSize)) > -1) {
		v.add(buffer);
		vSizes.add(new Integer(c));
		nx += c;
		buffer = new byte[buffSize];
	}
	byte[] x = new byte[nx];
	int xpos = 0;
	int k = 0;
	for (Iterator it = v.iterator(); it.hasNext();) {
		buffer = (byte[]) it.next();
		nx = ((Integer) (vSizes.get(k))).intValue();
		System.arraycopy(buffer,0,x,xpos,nx);
		xpos += nx;
		k++;
	}
	return x;
}

public static char[] reader2chars(Reader reader) throws IOException {
	return reader2chars(reader, 1024);
}
public static char[] reader2chars(Reader reader, int buffSize) throws IOException {
	Reader in;
	if (! (reader instanceof BufferedReader)) {
		in = new BufferedReader(reader);
	} else {
		in = reader;
	}
	
	int c;
	char[] buffer0 = new char[buffSize];
	char[] buffer = buffer0;
	ArrayList v = new ArrayList();
	ArrayList vSizes = new ArrayList();
	int nx = 0;
	while ((c = in.read(buffer,0,buffSize)) > -1) {
		v.add(buffer);
		vSizes.add(new Integer(c));
		nx += c;
		buffer = new char[buffSize];
	}
	char[] x = new char[nx];
	int xpos = 0;
	int k = 0;
	for (Iterator it = v.iterator(); it.hasNext();) {
		buffer = (char[]) it.next();
		nx = ((Integer) (vSizes.get(k))).intValue();
		System.arraycopy(buffer,0,x,xpos,nx);
		xpos += nx;
		k++;
	}
	return x;
}


		
//
//	 DETRUIRE UN DOSSIER
//


/** détruire un dossier et tous les fichiers qu'il contient.
(la méthode java.io.File.delete() exige qu'une directory soit vide pour la détruire). 
return true iff the dir is successfully deleted */
public static boolean deleteFolder(File dir) throws SecurityException {
	if (!dir.exists()) return true;
	if (!(dir.isDirectory())) throw new IllegalArgumentException("not a dir");
	String[] liste;
	String name;
	File f;
	liste = dir.list();
	int n = liste.length;
	for (int i = 0; i<n; i++) {
		name = liste[i];
		f = new File(dir,name);
		boolean ok;
		if (f.isDirectory()) {
			ok = deleteFolder(f);
		} else {
			ok = f.delete();
		}
		if (!ok) return false;
	}
	return dir.delete();
}
}