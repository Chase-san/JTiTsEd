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

public class ControlEntry {

	public enum Type {
		Array, ArrayRow, Boolean, CustomTextEnum, Decimal, Enum, Flags, Integer, Item, Label, Row, String, Tab,
		TextEnum, Title;

		public static Type fromString(String typeName) {
			for (Type type : Type.values()) {
				if (type.name().equalsIgnoreCase(typeName)) {
					return type;
				}
			}
			return null;
		}
	}

	public static ControlEntry createTab(String name) {
		ControlEntry entry = new ControlEntry(Type.Tab);
		entry.value = new String[] { name };
		return entry;
	}

	public static ControlEntry createRow(boolean isArray) {
		if (isArray) {
			return new ControlEntry(Type.ArrayRow);
		}
		return new ControlEntry(Type.Row);
	}

	public final Type type;
	public Integer max;
	public Integer min;
	public String ref;
	public String[] value;
	public Sort sort;
	public int span;
	public int arrayIndex;

	public ControlEntry(Type type) {
		this.type = type;
		min = max = null;
		ref = null;
		sort = Sort.Natural;
		value = null;
		span = 1;
		arrayIndex = 0;
	}

	public ControlEntry(String typeName) {
		this(Type.fromString(typeName));
	}
}
