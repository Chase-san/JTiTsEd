/**
 * Copyright (c) 2020 Robert Maupin
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.csdgn.amf3.AmfArray;
import org.csdgn.amf3.AmfBoolean;
import org.csdgn.amf3.AmfDouble;
import org.csdgn.amf3.AmfInteger;
import org.csdgn.amf3.AmfObject;
import org.csdgn.amf3.AmfString;
import org.csdgn.maru.Pair;

public class StructEntry {

    public String id;
    public Map<String, Pair<String, String>> entry;

    public StructEntry() {
        entry = new LinkedHashMap<String, Pair<String, String>>();
    }

    public AmfObject createAmfObject() {
        AmfObject obj = new AmfObject();
        obj.setDynamic(true);
        for (String id : entry.keySet()) {
            Pair<String, String> data = entry.get(id);
            String type = data.left();
            String value = data.right();

            if (type.equals("string")) {
                obj.getDynamicMap().put(id, new AmfString(value));
            } else if (type.equals("int")) {
                obj.getDynamicMap().put(id, new AmfInteger(new Integer(value)));
            } else if (type.equals("double")) {
                obj.getDynamicMap().put(id, new AmfDouble(new Double(value)));
            } else if (type.equals("boolean")) {
                obj.getDynamicMap().put(id, new AmfBoolean(new Boolean(value)));
            } else if (type.equals("array")) {
                obj.getDynamicMap().put(id, new AmfArray());
            }
        }
        return obj;
    }
}