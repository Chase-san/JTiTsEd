/**
 * Copyright (c) 2019 Robert Maupin
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stand alone executable for parsing the items directly from TiTs source files.
 *
 */
public class ItemParser {
	private static Map<String, String> map;

	private static String getValue(String line) {
		return getValue(line, "=", ";");
	}

	private static String getValue(String line, String sDelim, String eDelim) {
		int start = line.indexOf(sDelim);
		int end = line.lastIndexOf(eDelim);
		if (start != -1 && end != -1) {
			start += sDelim.length();
			String output = line.substring(start, end).trim();
			if (output.charAt(0) == '"') {
				output = output.substring(1, output.length() - 1);
			}
			return output;
		}
		return line;
	}

	private static void parseFolder(File folder) {
		StringBuilder buf = new StringBuilder();
		
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				parseFolder(file);
				continue;
			} else if (!file.getName().endsWith(".as")) {
				continue;
			}
			try {
				String packageName = "!";
				String className = "!";
				String stack = "!";
				String shortName = "!";
				String longName = "!";
				String type = "!";
				for (String line : Files.readAllLines(file.toPath())) {
					if (line.contains("if(") || line.contains("if (") || line.contains("output(")
							|| line.contains("output (")) {
						continue;
					}

					if (line.contains("package classes")) {
						packageName = line.substring(line.indexOf("package") + 8).trim();
						if (packageName.endsWith("{")) {
							packageName = packageName.substring(0, packageName.length() - 1).trim();
						}
					}
					if (line.contains("public class") && line.contains("extends")) {
						className = getValue(line, "class", "extends");
					}
					if (line.contains("{") || line.contains("}")) {
						continue;
					}
					if (line.contains("stackSize") && line.contains("=")) {
						stack = getValue(line);
					}
					if (line.contains("type") && line.contains("=") && line.contains("GLOBAL")
							&& !line.contains("!=")) {
						type = getValue(line, "GLOBAL.", ";");
					}
					if (!line.contains("TooltipManager")) {

						if (line.contains("shortName") && line.contains("=")) {
							shortName = getValue(line);
						}
						if (line.contains("longName") && line.contains("=")) {
							//for some reason some of these lack the end ;
							longName = getValue(line, "\"", "\"");
						}
					}

				}

				if (type.equals("!")) {
					continue;
				}

				if (className.contains("CornyTShirt")) {
					longName = className;
					shortName = className;
					if (className.equals("CornyTShirt")) {
						shortName = "CornyTShirtV0";
						longName = "CornyTShirtV0";
					}
				}
				
				shortName = shortName.replaceAll("&", "&#38;").replaceAll("<", "&#60;").replaceAll(">", "&#62;");
				longName = longName.replaceAll("&", "&#38;").replaceAll("<", "&#60;").replaceAll(">", "&#62;");

				buf.setLength(0);
				buf.append("<item id=\"");
				buf.append(packageName);
				buf.append("::");
				buf.append(className);
				buf.append("\"><shortName>");
				buf.append(shortName);
				buf.append("</shortName>");
				if(!shortName.equals(longName)) {
					buf.append("<longName>");
					buf.append(longName);
					buf.append("</longName>");
				}
				buf.append("<stack>");
				buf.append(stack);
				buf.append("</stack><type>");
				buf.append(type);
				buf.append("</type></item>");

				map.put(shortName, buf.toString());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		String path = System.getProperty("user.home") + "/git/TiTS-Public-master/classes/Items";
		String output = System.getProperty("user.home") + "/git/TiTS-Public-master/classes/Items/items.xml";

		map = new TreeMap<String, String>();

		File itemsPath = new File(path);
		if (!itemsPath.exists()) {
			System.out.println("Path not found: " + path);
			return;
		}
		for (File folder : itemsPath.listFiles()) {
			if (folder.isDirectory()) {
				parseFolder(folder);
			}
		}

		ArrayList<String> lines = new ArrayList<String>();
		lines.add("<items>");
		for (String line : map.values()) {
			lines.add(line);
		}
		lines.add("</items>");

		try {
			Files.write(new File(output).toPath(), lines);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
