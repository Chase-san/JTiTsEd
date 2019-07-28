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

public class DataModel {
	
	/**
	 * Sax handler for enum values.
	 */
	private class EnumSAXHandler extends DefaultHandler {
		LinkedHashMap<String, String> map;
		StringBuilder buffer;
		boolean read;
		String name;
		String id;
		
		
		private EnumSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if("enum".equals(qName)) {
				map = new LinkedHashMap<String, String>();
				name = attributes.getValue("name");
			} else if("value".equals(qName)) {
				read = true;
				buffer.setLength(0);
				id = attributes.getValue("id");
			}
		}
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("enum".equals(qName)) {
				enumMap.put(name, map);
				map = null;
			} else if("value".equals(qName)) {
				read = false;
				String value = buffer.toString();
				if(null == id) {
					id = value;
				}
				map.put(id, value);
			}
		}
		public void characters(char ch[], int start, int length) throws SAXException {
			if(read) {
				buffer.append(ch, start, length);
			}
		}
	}
	
	
	
	/**
	 * Sax handler for data entries.
	 */
	private class DataSAXHandler extends DefaultHandler {
		StringBuilder buffer;
		String enumType;
		int span = 1;
		boolean read;
		boolean enumTextEdit;
		String sort;
		
		private DataSAXHandler() {
			buffer = new StringBuilder();
			read = false;
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			enumType = null;
			enumTextEdit = false;
			sort = null;
			span = 1;
			
			switch(qName) {
			case DataEntry.TYPE_TAB:
				dataMap.add(new DataEntry(qName, attributes.getValue("name")));
				break;
			case DataEntry.TYPE_ROW:
				dataMap.add(new DataEntry(qName));
				break;
			case DataEntry.TYPE_FLAGS:
			case DataEntry.TYPE_ENUM:
			case DataEntry.TYPE_TEXT_ENUM:
				enumType = attributes.getValue("type");
				String sEdit = attributes.getValue("edit");
				enumTextEdit = "true".equalsIgnoreCase(sEdit);
				String vSort = attributes.getValue("sort");
				if(vSort != null) {
					sort = vSort;
				}
			
			case DataEntry.TYPE_LABEL:
			case DataEntry.TYPE_TITLE:
			case DataEntry.TYPE_STRING:
			case DataEntry.TYPE_BOOLEAN:
			case DataEntry.TYPE_INTEGER:
			case DataEntry.TYPE_DECIMAL:
				String sSpan = attributes.getValue("span");
				if(sSpan != null) {
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
			DataEntry entry = null;
			switch(qName) {
			case DataEntry.TYPE_FLAGS:
			case DataEntry.TYPE_ENUM:
			case DataEntry.TYPE_TEXT_ENUM:
				type = qName + ":" + enumType;
			case DataEntry.TYPE_BOOLEAN:
			case DataEntry.TYPE_STRING:
			case DataEntry.TYPE_INTEGER:
			case DataEntry.TYPE_DECIMAL:
				String data = buffer.toString().trim();
				dataMap.add(entry = new DataEntry(type, span, data.split(",")));
				if(DataEntry.TYPE_TEXT_ENUM.equals(qName)) {
					entry.enumTextEdit = enumTextEdit;
				}
				if(DataEntry.TYPE_ENUM.equals(qName)
				|| DataEntry.TYPE_TEXT_ENUM.equals(qName)
				|| DataEntry.TYPE_FLAGS.equals(qName)) {
					entry.sort = sort;
				}
				break;
			case DataEntry.TYPE_LABEL:
			case DataEntry.TYPE_TITLE:
				dataMap.add(new DataEntry(type, span, buffer.toString().trim()));
				break;
			}
			
		}
		public void characters(char ch[], int start, int length) throws SAXException {
			if(read) {
				buffer.append(ch, start, length);
			}
		}
	}
	
	
	private Map<String, LinkedHashMap<String, String>> enumMap;
	private List<DataEntry> dataMap;
	
	public void load() {
		loadEnumMap();
		loadDataMap();
	}
	
	public LinkedHashMap<String, String> getEnum(String name) {
		return enumMap.get(name);
	}
	
	public List<DataEntry> getDataMap() {
		return dataMap;
	}
	
	private File getFile(String name) {
		File file = new File(name);
		if(!file.exists()) {
			file = new File("jtitsed"+name);
		}
		if(!file.exists()) {
			file = new File("jtitsed_"+name);
		}
		if(!file.exists()) {
			file = new File("jtitsed-"+name);
		}
		if(!file.exists()) {
			file = new File("jtitsed."+name);
		}
		if(!file.exists()) {
			file = new File("data", name);
		}
		if(!file.exists()) {
			file = new File("data", "jtitsed"+name);
		}
		if(!file.exists()) {
			file = new File("data", "jtitsed_"+name);
		}
		if(!file.exists()) {
			file = new File("data", "jtitsed-"+name);
		}
		if(!file.exists()) {
			file = new File("data", "jtitsed."+name);
		}
		return file;
	}
	
	private void loadDataMap() {
		dataMap = new ArrayList<DataEntry>();
		parseFile(getFile("data.xml"), new DataSAXHandler());
	}
	
	private void loadEnumMap() {
		enumMap = new HashMap<String, LinkedHashMap<String, String>>();
		parseFile(getFile("enum.xml"), new EnumSAXHandler());
	}
	
	private void parseFile(File file, DefaultHandler dh) {
		try(InputStream input = new BufferedInputStream(new FileInputStream(file))) {
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
