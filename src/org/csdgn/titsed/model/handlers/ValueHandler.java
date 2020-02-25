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
package org.csdgn.titsed.model.handlers;

import org.csdgn.titsed.model.DataModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.LinkedHashMap;

/**
 * Sax handler for values file.
 */
public class ValueHandler extends DefaultHandler {
    private DataModel dm;
    private StringBuilder buffer;
    private String id;
    private LinkedHashMap<String, String> map;
    private String name;
    private boolean read;

    public ValueHandler(DataModel dm) {
        this.dm = dm;
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
            dm.getEnumMap().put(name, map);
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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