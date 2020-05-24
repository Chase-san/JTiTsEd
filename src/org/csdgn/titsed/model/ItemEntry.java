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

import org.csdgn.maru.Strings;
import org.csdgn.maru.swing.ToolTipProvider;

public class ItemEntry implements ToolTipProvider {
	public String editorName;
	public String id;
	public String longName;
	public String shortName;
	public int stackSize;
	public String type;

	public ItemEntry() {
		id = type = editorName = shortName = longName = null;
		stackSize = 1;
	}

	public ItemEntry(String id, String shortName) {
		this.id = id;
		this.shortName = shortName;
		this.type = "";
		stackSize = 1;
		finish();
	}

	public String shortId() {
		if(id == null) {
			return null;
		}
		int index = id.indexOf("::");
		if(index >= 0) {
			return id.substring(index + 2);
		}
		return id;
	}

	public void finish() {
		if (longName == null) {
			longName = shortName;
		}
		if (editorName == null) {
			editorName = Strings.toTitleCase(longName);
		}
	}

	public String getToolTip() {
		return shortName + "'" + longName + "'" + " [" + id + "]";
	}

	public String toString() {
		return editorName;
	}
}
