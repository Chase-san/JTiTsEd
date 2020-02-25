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

import org.csdgn.titsed.model.ItemEntry;
import org.csdgn.titsed.model.DataModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Sax handler for items file.
 */
public class ItemHandler extends DefaultHandler {
    private DataModel dm;
    private StringBuilder buffer;
    private ItemEntry entry;
    private boolean read;

    public ItemHandler(DataModel dm) {
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
        if ("item".equals(qName)) {
            entry.finish();
            dm.getItemList().add(entry);
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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