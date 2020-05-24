/**
 * Copyright (c) 2017-2020 Robert Maupin
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

import org.csdgn.titsed.model.handlers.*;
import org.csdgn.titsed.ui.UIStrings;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interface for our own home grown data sheet.
 * 
 * @author chase
 *
 */
public class DataModel {
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
	private Map<String, StructEntry> structMap;
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

	public Map<String, LinkedHashMap<String, String>> getEnumMap() {
		return enumMap;
	}

	public List<ItemEntry> getItemList() {
		return itemList;
	}

	public List<ControlEntry> getControlMap() {
		return controlMap;
	}

	public Map<String, StructEntry> getStructMap() {
		return structMap;
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
		loadStructMap();
		loadControlMap();
		generateTabMap();
		
	}

	private void loadControlMap() {
		controlMap = new ArrayList<ControlEntry>();
		URL xml = UIStrings.getResource("Model.Controls");
		URL xsd = UIStrings.getResource("Model.Controls.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new ControlHandler(this));
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Controls"));
		}
	}

	private void loadItemMap() {
		itemList = new ArrayList<ItemEntry>();
		URL xml = UIStrings.getResource("Model.Items");
		URL xsd = UIStrings.getResource("Model.Items.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new ItemHandler(this));
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Items"));
		}
		//sort items
		itemList.sort((a, b) -> {
			return a.editorName.compareToIgnoreCase(b.editorName);
		});
	}

	private void loadStructMap() {
		structMap = new HashMap<String, StructEntry>();
		URL xml = UIStrings.getResource("Model.Structs");
		URL xsd = UIStrings.getResource("Model.Structs.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new StructHandler(this));
		} else {
			System.err.printf("Invalid `%s`!\n", UIStrings.getString("Model.Values"));
		}
	}

	private void loadValueMap() {
		enumMap = new HashMap<String, LinkedHashMap<String, String>>();
		URL xml = UIStrings.getResource("Model.Values");
		URL xsd = UIStrings.getResource("Model.Values.Schema");
		if (validate(xsd, xml)) {
			parseURL(xml, new ValueHandler(this));
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
