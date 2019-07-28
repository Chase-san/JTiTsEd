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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SavePath {
	public final String name;
	public final File directory;
	public final Map<Integer, File> saves;
	
	protected SavePath(String name, File dir) {
		this.saves = new HashMap<Integer, File>();
		this.name = name;
		this.directory = dir;
	}
	
	protected boolean findSaves() {
		saves.clear();
		if(!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		for(File file : directory.listFiles()) {
			String filename = file.getName();
			if(!Pattern.matches("^TiTs_[0-9]+.sol$", filename)) {
				continue;
			}
			
			String number = filename.substring(5, filename.length() - 4);
			try {
				int index = Integer.parseInt(number);
				saves.put(index, file);
			} catch(NumberFormatException ex) {
				//don't add it
			}
		}
		return true;
	}
}