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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.csdgn.titsed.ui.UIStrings;
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
	 * Sax handler for controls file.
	 */
	private class ControlSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		String className;
		boolean enumTextEdit;
		String enumType;
		Integer max;
		Integer min;
		boolean read;
		String sort;
		int span = 1;

		private ControlSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (read) {
				buffer.append(ch, start, length);
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
			case ControlEntry.TYPE_ARRAY:
			case ControlEntry.TYPE_BOOLEAN:
			case ControlEntry.TYPE_STRING:
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				String data = buffer.toString().trim();
				entry = new ControlEntry(type, span, data.split(","));
				controlMap.add(entry);
				if (ControlEntry.TYPE_TEXT_ENUM.equals(qName)) {
					entry.enumTextEdit = enumTextEdit;
				}
				if (ControlEntry.TYPE_ENUM.equals(qName) || ControlEntry.TYPE_TEXT_ENUM.equals(qName)
						|| ControlEntry.TYPE_FLAGS.equals(qName)) {
					entry.sort = sort;
				}
				if (ControlEntry.TYPE_ARRAY.equals(qName)) {
					entry.className = className;
				}
				if (ControlEntry.TYPE_ARRAY.equals(qName) || ControlEntry.TYPE_INTEGER.equals(qName)
						|| ControlEntry.TYPE_DECIMAL.equals(qName)) {
					entry.min = min;
					entry.max = max;
				}
				break;
			case ControlEntry.TYPE_LABEL:
			case ControlEntry.TYPE_TITLE:
				controlMap.add(new ControlEntry(type, span, buffer.toString().trim()));
				break;
			}

		}

		private void parseSpan(Attributes attributes) {
			String sSpan = attributes.getValue("span");
			if (sSpan != null) {
				span = Integer.parseInt(sSpan);
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			enumType = null;
			enumTextEdit = false;
			sort = null;
			min = null;
			max = null;
			className = null;
			span = 1;

			switch (qName) {
			case ControlEntry.TYPE_TAB:
				controlMap.add(new ControlEntry(qName, attributes.getValue("name")));
				break;
			case ControlEntry.TYPE_ROW: {
				ControlEntry row = new ControlEntry(qName);
				String arr = attributes.getValue("array");
				if (arr != null && arr.equalsIgnoreCase("true")) {
					row.arrayRow = true;
				}
				controlMap.add(row);
				break;
			}
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
				parseSpan(attributes);
				buffer.setLength(0);
				read = true;
				break;

			case ControlEntry.TYPE_ARRAY:
				className = attributes.getValue("class");
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				String sMin = attributes.getValue("min");
				if (sMin != null) {
					min = Integer.parseInt(sMin);
				}
				String sMax = attributes.getValue("max");
				if (sMax != null) {
					min = Integer.parseInt(sMax);
				}

			case ControlEntry.TYPE_LABEL:
			case ControlEntry.TYPE_TITLE:
			case ControlEntry.TYPE_STRING:
			case ControlEntry.TYPE_BOOLEAN:
				parseSpan(attributes);
				buffer.setLength(0);
				read = true;
				break;
			}
		}
	}

	/**
	 * Sax handler for values file.
	 */
	private class ValueSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		String id;
		LinkedHashMap<String, String> map;
		String name;
		boolean read;

		private ValueSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (read) {
				buffer.append(ch, start, length);
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("enum".equals(qName)) {
				enumMap.put(name, map);
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
	}

	private static BufferedInputStream getUrlStream(URL url) throws IOException {
		URLConnection uc = url.openConnection();
		uc.connect();
		return new BufferedInputStream(uc.getInputStream());
	}
	
	private static boolean validate(URL xsd, URL xml) {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = factory.newSchema(xsd);
			Validator validator = schema.newValidator();
			try (InputStream is = getUrlStream(xml)) {
				validator.validate(new StreamSource(is));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} catch (SAXException e) {
			//System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private List<ControlEntry> controlMap;

	private Map<String, LinkedHashMap<String, String>> enumMap;

	private Map<String, List<ControlEntry>> tabMap;

	private void generateTabMap() {
		tabMap = new LinkedHashMap<String, List<ControlEntry>>();
		List<ControlEntry> list = null;
		for (ControlEntry entry : getControlMap()) {
			switch (entry.type) {
			case ControlEntry.TYPE_TAB:
				tabMap.put(entry.value[0], list = new ArrayList<ControlEntry>());
				break;
			default:
				list.add(entry);
				break;
			}
		}
	}

	public List<ControlEntry> getControlMap() {
		return controlMap;
	}

	public LinkedHashMap<String, String> getEnum(String name) {
		return enumMap.get(name);
	}

	public List<ControlEntry> getTabDataMap(String tab) {
		return tabMap.get(tab);
	}

	public Set<String> getTabs() {
		return tabMap.keySet();
	}

	public void load() {
		loadValueMap();
		loadControlMap();
		generateTabMap();
	}

	private void loadControlMap() {
		controlMap = new ArrayList<ControlEntry>();
		URL xml = UIStrings.getResource("Model.Controls");
		URL xsd = UIStrings.getResource("Model.Controls.Schema");
		if(validate(xsd, xml)) {
			parseURL(xml, new ControlSAXHandler());
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Controls"));
		}
	}

	private void loadValueMap() {
		enumMap = new HashMap<String, LinkedHashMap<String, String>>();
		URL xml = UIStrings.getResource("Model.Values");
		URL xsd = UIStrings.getResource("Model.Values.Schema");
		if(validate(xsd, xml)) {
			parseURL(xml, new ValueSAXHandler());
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Values"));
		}
		
	}

	private void parseURL(URL url, DefaultHandler dh) {
		try {
			try (InputStream input = getUrlStream(url)) {
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(input, dh);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
