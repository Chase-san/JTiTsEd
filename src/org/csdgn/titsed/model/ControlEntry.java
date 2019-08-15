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
	public static final String TYPE_TAB = "tab";
	public static final String TYPE_ROW = "row";
	public static final String TYPE_LABEL = "label";
	public static final String TYPE_TITLE = "title";
	public static final String TYPE_ARRAY = "array";
	public static final String TYPE_STRING = "string";
	public static final String TYPE_INTEGER = "integer";
	public static final String TYPE_DECIMAL = "decimal";
	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_ITEM = "item";
	public static final String TYPE_ENUM = "enum";
	public static final String TYPE_TEXT_ENUM = "textenum";
	public static final String TYPE_FLAGS = "flags";

	public static final String SORT_NATURAL = "natural";
	public static final String SORT_KEY = "key";
	public static final String SORT_VALUE = "value";
	public static final String SORT_KEY_NONE = "keyNone";
	public static final String SORT_VALUE_NONE = "valueNone";

	public String type;
	public String[] value;
	public boolean enumTextEdit;
	public int span;
	public int index;
	public boolean arrayRow;
	public Integer min;
	public Integer max;
	public String sort;
	public String className;

	public ControlEntry(String type, String... value) {
		this.type = type;
		this.value = value;
		this.span = 1;
		this.min = null;
		this.max = null;
		this.enumTextEdit = false;
		this.sort = SORT_NATURAL;
		this.className = null;
		this.arrayRow = false;
		this.index = 0;
	}

	public ControlEntry(String type, int span, String... value) {
		this.type = type;
		this.value = value;
		this.span = span;
		this.min = null;
		this.max = null;
		this.enumTextEdit = false;
		this.sort = SORT_NATURAL;
		this.className = null;
		this.arrayRow = false;
		this.index = 0;
	}
}
