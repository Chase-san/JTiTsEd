/**
 * Copyright (c) 2017-2019 Robert Maupin
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
import java.util.Comparator;
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

import org.csdgn.maru.Strings;
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

	private class ControlSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		ControlEntry entry;
		boolean read;

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
			if (entry != null) {
				entry.value = buffer.toString().split(",");
				controlMap.add(entry);
			}
			entry = null;
			read = false;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.equals("tab")) {
				controlMap.add(ControlEntry.createTab(attributes.getValue("name")));
			} else if (qName.equals("row")) {
				String isArray = attributes.getValue("array");
				controlMap.add(ControlEntry.createRow("true".equalsIgnoreCase(isArray)));
			} else if (qName.equals("control")) {
				entry = new ControlEntry(attributes.getValue("type"));
				try {
					entry.min = Integer.parseInt(attributes.getValue("min"));
				} catch (NumberFormatException e) {
				}
				try {
					entry.max = Integer.parseInt(attributes.getValue("max"));
				} catch (NumberFormatException e) {
				}
				try {
					entry.span = Integer.parseInt(attributes.getValue("span"));
				} catch (NumberFormatException e) {
				}
				entry.sort = Sort.fromString(attributes.getValue("sort"));
				entry.ref = attributes.getValue("ref");
				buffer.setLength(0);
				read = true;
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

	/**
	 * Sax handler for items file.
	 */
	private class ItemSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		ItemEntry entry;
		boolean read;

		private ItemSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (read) {
				buffer.append(ch, start, length);
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("item".equals(qName)) {
				entry.finish();
				itemList.add(entry);
				entry = null;
			} else if ("shortName".equals(qName)) {
				entry.shortName = buffer.toString();
			} else if ("longName".equals(qName)) {
				entry.longName = buffer.toString();
			} else if ("editorName".equals(qName)) {
				entry.editorName = buffer.toString();
			} else if ("type".equals(qName)) {
				entry.type = buffer.toString();
			} else if ("stack".equals(qName)) {
				try {
					entry.stackSize = Integer.parseInt(buffer.toString());
				} catch (NumberFormatException e) {
					// this is technically optional, and defaults to 1
				}

			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if ("item".equals(qName)) {
				read = false;
				entry = new ItemEntry();
				entry.id = attributes.getValue("id");
			} else if (!"items".equals(qName)) {
				read = true;
				buffer.setLength(0);
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
			// System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private List<ControlEntry> controlMap;

	private Map<String, LinkedHashMap<String, String>> enumMap;
	private Map<String, List<ControlEntry>> tabMap;
	private List<ItemEntry> itemList;

	private void generateTabMap() {
		tabMap = new LinkedHashMap<String, List<ControlEntry>>();
		List<ControlEntry> list = null;
		for (ControlEntry entry : getControlMap()) {
			if (entry.type == ControlEntry.Type.Tab) {
				tabMap.put(entry.value[0], list = new ArrayList<ControlEntry>());
			} else {
				list.add(entry);
			}
		}
	}

	public void resetArrayIndexes() {
		for (ControlEntry e : controlMap) {
			e.arrayIndex = 0;
		}
	}

	public List<ItemEntry> getItemList() {
		return itemList;
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
		loadItemMap();
		loadControlMap();
		generateTabMap();
	}

	private void loadControlMap() {
		controlMap = new ArrayList<ControlEntry>();
		URL xml = UIStrings.getResource("Model.Controls");
		URL xsd = UIStrings.getResource("Model.Controls.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new ControlSAXHandler());
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Controls"));
		}
	}

	private void loadItemMap() {
		itemList = new ArrayList<ItemEntry>();
		URL xml = UIStrings.getResource("Model.Items");
		URL xsd = UIStrings.getResource("Model.Items.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new ItemSAXHandler());
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Items"));
		}
		//sort items
		itemList.sort((a, b) -> {
			return a.editorName.compareToIgnoreCase(b.editorName);
		});
	}

	private void loadValueMap() {
		enumMap = new HashMap<String, LinkedHashMap<String, String>>();
		URL xml = UIStrings.getResource("Model.Values");
		URL xsd = UIStrings.getResource("Model.Values.Schema");
		if (validate(xsd, xml)) {
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
