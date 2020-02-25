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

import org.csdgn.titsed.model.ControlEntry;
import org.csdgn.titsed.model.DataModel;
import org.csdgn.titsed.model.Sort;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ControlHandler extends DefaultHandler {
    private DataModel dm;
    private StringBuilder buffer;
    private ControlEntry entry;
    private boolean read;

    public ControlHandler(DataModel dm) {
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
        if (entry != null) {
            entry.value = buffer.toString().split(",");
            dm.getControlMap().add(entry);
        }
        entry = null;
        read = false;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("tab")) {
            dm.getControlMap().add(ControlEntry.createTab(attributes.getValue("name")));
        } else if (qName.equals("row")) {
            String isArray = attributes.getValue("array");
            dm.getControlMap().add(ControlEntry.createRow("true".equalsIgnoreCase(isArray)));
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