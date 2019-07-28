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
package org.csdgn.titsed.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interface for our own home grown data sheet.
 * 
 * @author chase
 *
 */
public class DataModel {

	/**
	 * Sax handler for values file.
	 */
	private class ValueSAXHandler extends DefaultHandler {
		LinkedHashMap<String, String> map;
		StringBuilder buffer;
		boolean read;
		String name;
		String id;

		private ValueSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if ("enum".equals(qName)) {
				map = new LinkedHashMap<String, String>();
				name = attributes.getValue("name");
			} else if ("value".equals(qName)) {
				read = true;
				buffer.setLength(0);
				id = attributes.getValue("id");
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("enum".equals(qName)) {
				valueMap.put(name, map);
				map = null;
			} else if ("value".equals(qName)) {
				read = false;
				String value = buffer.toString();
				if (null == id) {
					id = value;
				}
				map.put(id, value);
			}
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (read) {
				buffer.append(ch, start, length);
			}
		}
	}

	/**
	 * Sax handler for controls file.
	 */
	private class ControlSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		String enumType;
		int span = 1;
		boolean read;
		boolean enumTextEdit;
		String sort;

		private ControlSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			enumType = null;
			enumTextEdit = false;
			sort = null;
			span = 1;

			switch (qName) {
			case ControlEntry.TYPE_TAB:
				controlMap.add(new ControlEntry(qName, attributes.getValue("name")));
				break;
			case ControlEntry.TYPE_ROW:
				controlMap.add(new ControlEntry(qName));
				break;
			case ControlEntry.TYPE_FLAGS:
			case ControlEntry.TYPE_ENUM:
			case ControlEntry.TYPE_TEXT_ENUM:
				enumType = attributes.getValue("type");
				String sEdit = attributes.getValue("edit");
				enumTextEdit = "true".equalsIgnoreCase(sEdit);
				String vSort = attributes.getValue("sort");
				if (vSort != null) {
					sort = vSort;
				}

			case ControlEntry.TYPE_LABEL:
			case ControlEntry.TYPE_TITLE:
			case ControlEntry.TYPE_STRING:
			case ControlEntry.TYPE_BOOLEAN:
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				String sSpan = attributes.getValue("span");
				if (sSpan != null) {
					span = Integer.parseInt(sSpan);
				}
				buffer.setLength(0);
				read = true;
				break;
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			read = false;
			String type = qName;
			ControlEntry entry = null;
			switch (qName) {
			case ControlEntry.TYPE_FLAGS:
			case ControlEntry.TYPE_ENUM:
			case ControlEntry.TYPE_TEXT_ENUM:
				type = qName + ":" + enumType;
			case ControlEntry.TYPE_BOOLEAN:
			case ControlEntry.TYPE_STRING:
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				String data = buffer.toString().trim();
				controlMap.add(entry = new ControlEntry(type, span, data.split(",")));
				if (ControlEntry.TYPE_TEXT_ENUM.equals(qName)) {
					entry.enumTextEdit = enumTextEdit;
				}
				if (ControlEntry.TYPE_ENUM.equals(qName) || ControlEntry.TYPE_TEXT_ENUM.equals(qName)
						|| ControlEntry.TYPE_FLAGS.equals(qName)) {
					entry.sort = sort;
				}
				break;
			case ControlEntry.TYPE_LABEL:
			case ControlEntry.TYPE_TITLE:
				controlMap.add(new ControlEntry(type, span, buffer.toString().trim()));
				break;
			}

		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (read) {
				buffer.append(ch, start, length);
			}
		}
	}

	private Map<String, LinkedHashMap<String, String>> valueMap;
	private List<ControlEntry> controlMap;

	public void load() {
		loadValueMap();
		loadControlMap();
	}

	public LinkedHashMap<String, String> getEnum(String name) {
		return valueMap.get(name);
	}

	public List<ControlEntry> getDataMap() {
		return controlMap;
	}

	private File getFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			file = new File("jtitsed" + name);
		}
		if (!file.exists()) {
			file = new File("jtitsed_" + name);
		}
		if (!file.exists()) {
			file = new File("jtitsed-" + name);
		}
		if (!file.exists()) {
			file = new File("jtitsed." + name);
		}
		if (!file.exists()) {
			file = new File("data", name);
		}
		if (!file.exists()) {
			file = new File("data", "jtitsed" + name);
		}
		if (!file.exists()) {
			file = new File("data", "jtitsed_" + name);
		}
		if (!file.exists()) {
			file = new File("data", "jtitsed-" + name);
		}
		if (!file.exists()) {
			file = new File("data", "jtitsed." + name);
		}
		return file;
	}

	private void loadControlMap() {
		controlMap = new ArrayList<ControlEntry>();
		parseFile(getFile("controls.xml"), new ControlSAXHandler());
	}

	private void loadValueMap() {
		valueMap = new HashMap<String, LinkedHashMap<String, String>>();
		parseFile(getFile("values.xml"), new ValueSAXHandler());
	}

	private void parseFile(File file, DefaultHandler dh) {
		try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(input, dh);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
