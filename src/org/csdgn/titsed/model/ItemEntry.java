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

	public void finish() {
		if (longName == null) {
			longName = shortName;
		}
		if (editorName == null) {
			editorName = Strings.toTitleCase(longName);
		}
	}

	public String getToolTip() {
		return "[" + id + "] " + shortName;
	}

	public String toString() {
		return editorName;
	}
}
