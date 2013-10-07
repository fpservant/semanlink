package net.semanlink.util;

import java.io.File;

public class Directory {
	public static interface Action {
		public void handleFile(File f) throws Exception;
	}
	
	private File dir;
	public Directory(File dir) {
		this.dir = dir;
	}
	
	public void doIt(Action action, boolean includeSubfolders) throws Exception {
		doIt(this.dir, action, includeSubfolders);
	}

	public static void doIt(File fileOrFolder, Action action, boolean includeSubfolders) throws Exception {
		if (fileOrFolder.isDirectory()) {
			for (File f : fileOrFolder.listFiles()) {
				if (f.isDirectory()) {
					doIt(f, action, includeSubfolders);
				} else {
					action.handleFile(f);
				}
			}
		} else {
			action.handleFile(fileOrFolder);
		}
	}
}
