/**
 * Copyright (c) 2017-2018 Robert Maupin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.csdgn.titsed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FileManager {
	private List<SavePath> paths;

	/**
	 * Creates a new file manager
	 */
	public FileManager() {
		paths = buildSavePaths();
	}

	/**
	 * Refinds all files in each of the previously found paths.
	 */
	public void updateSavePaths() {
		// refinds the files in each path
		for (SavePath path : paths) {
			path.findSaves();
		}
	}

	/**
	 * Rebuilds all save paths.
	 */
	public void rebuildSavePaths() {
		paths = buildSavePaths();
	}

	/**
	 * Gets a list of the save paths in this file manager.
	 * 
	 * @return list of save paths.
	 */
	public List<SavePath> getSavePaths() {
		return Collections.unmodifiableList(paths);
	}

	private List<File> getSharedObjectPaths(String pathname) {
		File path = new File(pathname);
		if (!path.exists()) {
			return Collections.emptyList();
		}
		List<File> files = new ArrayList<File>();
		for (File file : path.listFiles()) {
			String filename = file.getName();
			if (file.isDirectory() && Pattern.matches("^[0-9A-Z]+$", filename)) {
				file = new File(file, "localhost");
				if (!file.exists()) {
					file.mkdir();
				}
				files.add(file);
			}
		}
		return files;
	}

	private List<SavePath> buildSavePaths(String pathname, String format, String multiFormat) {
		ArrayList<SavePath> paths = new ArrayList<SavePath>();
		List<File> files = getSharedObjectPaths(pathname);
		boolean multi = files.size() > 1;
		for (File file : files) {
			String name = format;
			if (multi) {
				name = String.format(multiFormat, file.getName());
			}
			SavePath savePath = new SavePath(name, file);
			if (savePath.findSaves()) {
				paths.add(savePath);
			}
		}
		return paths;
	}

	/**
	 * Gets a list of all the save directories to search.
	 * 
	 * @return list of SavePaths, empty if none were found
	 */
	protected List<SavePath> buildSavePaths() {
		ArrayList<SavePath> paths = new ArrayList<SavePath>();

		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("windows")) {
			paths.addAll(buildSavePaths(System.getenv("APPDATA") + "\\Macromedia\\Flash Player\\#SharedObjects\\",
					"Local (Standard)", "Local (Standard %s)"));

		} else {
			// linux
			paths.addAll(buildSavePaths(System.getenv("HOME") + "/.macromedia/Flash_Player/#SharedObjects/", "Global",
					"Global %s"));
			paths.addAll(buildSavePaths(
					System.getenv("HOME")
							+ "/.config/google-chrome/Default/Pepper Data/Shockwave Flash/WritableRoot/#SharedObjects/",
					"Chrome", "Chrome %s"));
			paths.addAll(buildSavePaths(
					System.getenv("HOME")
							+ "/.config/chromium/Default/Pepper Data/Shockwave Flash/WritableRoot/#SharedObjects/",
					"Chromium", "Chromium %s"));
		}

		return paths;
	}
}
