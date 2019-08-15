package org.csdgn.titsed.model;

public class ItemEntry {
	String id;
	String type;
	String editorName;
	String shortName;
	String longName;
	int stackSize;
	
	public ItemEntry() {
		id = type = editorName = shortName = longName = null;
		stackSize = 1;
	}
	
	public void finish() {
		if(longName == null) {
			longName = shortName;
		}
		if(editorName == null) {
			editorName = longName;
		}
	}
}
