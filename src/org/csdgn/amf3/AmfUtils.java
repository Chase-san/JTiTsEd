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
package org.csdgn.amf3;

import java.util.ArrayList;

public class AmfUtils {
	/**
	 * Gets the data assocaited with the string, this will be a series of identifiers connected by periods.
	 * This method will only resolve through the sparse sections of arrays, string keys of dictionaries and objects.
	 * @param file file to resolve
	 * @param ident the identifier
	 * @return the resolved value, or null if not found.
	 */
	public static AmfValue resolve(AmfFile file, String ident) {
		String[] data = split(ident, '.');
		if(data.length == 0) {
			return null;
		}
		return subresolve(file.get(data[0]), data, 1);
	}
	
	/**
	 * Gets the data assocaited with the string, this will be a series of identifiers connected by periods.
	 * This method will only resolve through the sparse sections of arrays, string keys of dictionaries and objects.
	 * @param amf AmfValue to resolve from.
	 * @param ident the identifier
	 * @return the resolved value, or null if not found.
	 */
	public static AmfValue resolve(AmfValue amf, String ident) {
		String[] data = split(ident, '.');
		if(data.length == 0) {
			return null;
		}
		return subresolve(amf, data, 0);
	}
	
	/**
	 * Smart Fast Split. Removes null sections (length 0). "ab..cd" would
	 * produce [ab][cd] with '.'
	 */
	private static final String[] split(String src, char delim) {
		ArrayList<String> output = new ArrayList<String>();
		String tmp = "";
		int index = 0;
		int lindex = 0;
		while ((index = src.indexOf(delim, lindex)) != -1) {
			tmp = src.substring(lindex, index);
			if (tmp.length() > 0) {
				output.add(tmp);
			}
			lindex = index + 1;
		}
		tmp = src.substring(lindex);
		if (tmp.length() > 0) {
			output.add(tmp);
		}
		return output.toArray(new String[output.size()]);
	}
	
	protected static AmfValue subresolve(AmfValue value, String[] idents, int identIndex) {
		if(value == null) {
			return null;
		}
		if(idents.length == identIndex) {
			return value;
		}
		String ident = idents[identIndex];
		switch(value.getType()) {
		case Array:
			AmfArray array = (AmfArray)value;
			value = array.getAssociative().get(ident);
			break;
		case Dictionary:
			AmfDictionary dict = (AmfDictionary)value;
			value = dict.get(new AmfString(ident));
			break;
		case Object:
			AmfObject obj = (AmfObject)value;
			value = null;
			if(obj.getSealedMap().containsKey(ident)) {
				value = obj.getSealedMap().get(ident);
			} else if(obj.getDynamicMap().containsKey(ident)) {
				value = obj.getDynamicMap().get(ident);
			}
			break;
		default: //unsupported type
			return null;
		}
		return subresolve(value, idents, identIndex + 1);
	}
}
