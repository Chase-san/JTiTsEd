/**
 * Copyright (c) 2017-2019 Robert Maupin
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
package org.csdgn.titsed.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.PlainDocument;

import org.csdgn.amf3.AmfArray;
import org.csdgn.amf3.AmfInteger;
import org.csdgn.amf3.AmfObject;
import org.csdgn.amf3.AmfString;
import org.csdgn.maru.Updater;
import org.csdgn.maru.swing.DocumentAdapter;
import org.csdgn.maru.swing.NumberDocumentFilter;
import org.csdgn.maru.swing.ToolTipRenderer;
import org.csdgn.titsed.model.ControlEntry;
import org.csdgn.titsed.model.ItemEntry;
import org.csdgn.titsed.model.Sort;
import org.csdgn.titsed.ui.MainFrame.EnumEntry;

public class ControlsFactory {
	private String arrayPath;
	protected int arraySize;
	private int prefHeight;
	private int prefWidth;

	private ProgramState state;

	public ControlsFactory(ProgramState state) {
		this.state = state;
		arrayPath = null;
		prefHeight = 24;
		prefWidth = 120;
	}

	protected JPanel createArrayEntry(Updater tabUpdater, ControlEntry entry) {
		// need index, add, and remove
		// min is minimum number of entries
		// max is maximum number of entries
		// TODO allow multiple array entries
		// TODO handle array within arrays
		arrayPath = entry.value[0] + "." + entry.arrayIndex;

		// get maximum size
		AmfArray arr = (AmfArray) state.save.find(entry.value[0]);
		arraySize = arr.getDenseSize();

		JButton prev = new JButton("<");
		prev.setEnabled(entry.arrayIndex > 0);

		JButton next = new JButton(">");
		next.setEnabled(entry.arrayIndex < arraySize - 1);

		String lbl = String.format("%d of %d", entry.arrayIndex + 1, arraySize);
		if (arraySize == 0) {
			lbl = "none";
		}

		JLabel label = new JLabel(lbl);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setHorizontalTextPosition(SwingConstants.CENTER);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(prefWidth, prefHeight));
		panel.add(prev, BorderLayout.WEST);
		panel.add(next, BorderLayout.EAST);

		JPanel innerPanel = new JPanel(new BorderLayout());

		JButton sub = new JButton("-");
		innerPanel.add(sub, BorderLayout.WEST);

		JButton add = new JButton("+");
		add.setEnabled(false);
		innerPanel.add(add, BorderLayout.EAST);

		innerPanel.add(label, BorderLayout.CENTER);
		panel.add(innerPanel, BorderLayout.CENTER);

		int min = 0;
		if (entry.min != null) {
			min = entry.min;
		}

		sub.setEnabled(arraySize > min);
		sub.addActionListener(e -> {
			// remove this entry from the array and update
			arr.getDense().remove(entry.arrayIndex);
			if (entry.arrayIndex > 0) {
				entry.arrayIndex = Math.max(entry.arrayIndex - 1, arraySize - 1);
			}

			tabUpdater.update();
		});

		// TODO find a better way to do this...
		if ("item".equals(entry.ref)) {
			createArrayItemSubEntry(tabUpdater, entry, arr, add, sub);
		} else if ("breasts".equals(entry.ref)) {
			// TODO
		} else if ("cocks".equals(entry.ref)) {
			// TODO
		} else if ("vaginas".equals(entry.ref)) {
			// TODO
		}

		prev.addActionListener(e -> {
			entry.arrayIndex--;
			tabUpdater.update();
		});

		next.addActionListener(e -> {
			entry.arrayIndex++;
			tabUpdater.update();
		});

		return panel;
	}

	private void createArrayItemSubEntry(Updater tabUpdater, ControlEntry entry, AmfArray arr, JButton add,
			JButton sub) {
		int max = Integer.MAX_VALUE;
		if (entry.max != null) {
			max = entry.max;
		}

		add.setEnabled(arraySize < max);

		add.addActionListener(e -> {
			ItemEntry item = state.data.getItemList().get(0);

			// add a new entry to the array, switch to it and update
			AmfObject obj = new AmfObject();
			obj.setDynamic(true);
			obj.getDynamicMap().put("shortName", new AmfString(item.shortName));
			obj.getDynamicMap().put("version", new AmfInteger(1));
			obj.getDynamicMap().put("classInstance", new AmfString(item.id));
			obj.getDynamicMap().put("quantity", new AmfInteger(1));

			arr.getDense().add(obj);
			entry.arrayIndex = arr.getDenseSize() - 1;

			tabUpdater.update();
		});
	}

	protected JComboBox<EnumEntry<Boolean>> createBooleanEntry(ControlEntry entry) {
		JComboBox<EnumEntry<Boolean>> combo = new JComboBox<EnumEntry<Boolean>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));

		EnumEntry<Boolean> eeTrue = new EnumEntry<Boolean>(true, "True");
		EnumEntry<Boolean> eeFalse = new EnumEntry<Boolean>(false, "False");

		combo.addItem(eeTrue);
		combo.addItem(eeFalse);

		final String[] paths = getSaveIdents(entry.value);

		if (state.save.getBoolean(paths[0])) {
			combo.setSelectedItem(eeTrue);
		} else {
			combo.setSelectedItem(eeFalse);
		}

		combo.addActionListener(e -> {
			for (String path : paths) {
				Object obj = combo.getSelectedItem();
				if (obj != null) {
					@SuppressWarnings("unchecked")
					EnumEntry<Boolean> tmp = (EnumEntry<Boolean>) obj;
					state.save.setBoolean(path, tmp.id);
				}
			}
		});

		return combo;
	}

	protected JComponent createEntry(Updater tabUpdater, ControlEntry entry) {
		switch (entry.type) {
		case Array:
			return createArrayEntry(tabUpdater, entry);
		case Boolean:
			return createBooleanEntry(entry);
		case CustomTextEnum:
		case TextEnum:
			return createTextEnumEntry(entry);
		case Decimal:
			return createDecimalEntry(entry);
		case Integer:
			return createIntegerEntry(entry);
		case Enum:
			return createEnumEntry(entry);
		case Flags:
			return createFlagsEntry(entry);
		case Item:
			return createItemEntry(entry);
		case Label:
			return new JLabel(entry.value[0]);
		case String:
			return createStringEntry(entry);
		case Title:
			return createTitleEntry(entry);
		default:
			break;
		}

		throw new RuntimeException("Unknown Data Type: " + entry.type);
	}

	protected JComboBox<EnumEntry<Integer>> createEnumEntry(ControlEntry entry) {
		JComboBox<EnumEntry<Integer>> combo = new JComboBox<EnumEntry<Integer>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));

		// load data
		Map<String, String> enumData = state.data.getEnum(entry.ref);

		final String[] paths = getSaveIdents(entry.value);

		int gameValue = state.save.getInteger(paths[0]);
		EnumEntry<Integer> current = null;

		List<String> keys = Sort.sortIntegerKeySet(enumData, entry.sort);

		for (String key : keys) {
			String value = enumData.get(key);
			Integer nKey = Integer.parseInt(key);
			EnumEntry<Integer> ee = new EnumEntry<Integer>(nKey, value);
			combo.addItem(ee);

			if (nKey == gameValue) {
				current = ee;
			}
		}

		if (current == null) {
			// unknown value
			current = new EnumEntry<Integer>(gameValue, "Unknown Type " + gameValue);
			combo.addItem(current);
		}

		combo.setSelectedItem(current);
		combo.addActionListener(e -> {
			for (String path : paths) {
				// save.setString(path, field.getText());
				Object obj = combo.getSelectedItem();
				if (obj != null) {
					@SuppressWarnings("unchecked")
					EnumEntry<Integer> tmp = (EnumEntry<Integer>) obj;
					state.save.setInteger(path, tmp.id);
				}

			}
		});

		return combo;
	}

	protected JComponent createFlagsEntry(ControlEntry entry) {
		ExpansionPanel panel = new ExpansionPanel();
		int cols = entry.span;
		if (cols > 2) {
			cols = (entry.span + 1) / 2;
		}
		panel.setLayout(new GridLayout(0, cols));

		Map<String, String> enumData = state.data.getEnum(entry.ref);

		panel.setTitle(entry.ref);
		panel.collapse();

		final String[] paths = getSaveIdents(entry.value);

		Set<Integer> flagSet = new HashSet<Integer>();
		for (String path : paths) {
			flagSet.addAll(state.save.getFlags(path));
		}

		final Map<JCheckBox, Integer> boxMap = new IdentityHashMap<JCheckBox, Integer>();

		// we will be depleting the set in case
		// we have any unknown values that need display
		List<String> keys = Sort.sortIntegerKeySet(enumData, entry.sort);
		for (String key : keys) {
			String value = enumData.get(key);
			Integer nKey = Integer.parseInt(key);
			EnumEntry<Integer> ee = new EnumEntry<Integer>(nKey, value);

			// for each id
			JCheckBox box = new JCheckBox();
			box.setText(ee.text);
			if (flagSet.contains(nKey)) {
				box.setSelected(true);
			}
			flagSet.remove(nKey);
			boxMap.put(box, nKey);
			panel.add(box);
		}

		// handle all unknown flags that are set
		for (Integer unknownFlag : flagSet) {
			JCheckBox box = new JCheckBox();
			box.setText("Flag " + unknownFlag);
			box.setSelected(true);
			boxMap.put(box, unknownFlag);
			panel.add(box);
		}
		flagSet.clear();

		// setup callbacks
		for (final JCheckBox box : boxMap.keySet()) {
			box.addActionListener(e -> {
				int id = boxMap.get(box);
				for (String path : paths) {
					if (box.isSelected()) {
						// add flag
						state.save.addFlag(path, id);
					} else {
						// remove flag
						state.save.removeFlag(path, id);
					}
				}
			});
		}

		return panel;
	}

	protected JButton createItemEntry(ControlEntry entry) {
		final String[] paths = getSaveIdents(entry.value);
		String itemClass = state.save.getString(paths[0] + ".classInstance");
		String itemName = state.save.getString(paths[0] + ".shortName");

		final String[] filter;
		if (entry.ref != null) {
			filter = entry.ref.split(",");
		} else {
			filter = new String[0];
		}

		ItemEntry current = null;
		for (ItemEntry item : state.data.getItemList()) {
			if (Objects.equals(itemClass, item.id)) {
				current = item;
				break;
			}
		}
		boolean unknown = false;
		if (current == null) {
			current = new ItemEntry(itemClass, itemName);
			unknown = true;
		}

		JButton btn = new JButton(current.editorName);
		btn.setPreferredSize(new Dimension(prefWidth, prefHeight));
		btn.addActionListener(e -> {
			ItemPanel panel = new ItemPanel(state.data, filter);
			ItemEntry selected = panel.selectItem(state.window);
			if (selected != null) {
				for (String path : paths) {
					state.save.setString(path + ".classInstance", selected.id);
					state.save.setString(path + ".shortName", selected.shortName);
					btn.setText(selected.editorName);
				}
			}
		});

		if (unknown) {
			btn.setForeground(Color.RED);
			btn.setToolTipText("This item is not in the database, if changed it cannot be recovered.");
		}

		return btn;
	}

	protected JComboBox<ItemEntry> createItemEntryOld(ControlEntry entry) {
		JComboBox<ItemEntry> combo = new JComboBox<ItemEntry>();
		combo.setRenderer(new ToolTipRenderer());
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));

		String[] filter = new String[0];
		if (entry.ref != null) {
			filter = entry.ref.split(",");
		}

		// load data
		List<ItemEntry> data = state.data.getItemList();

		final String[] paths = getSaveIdents(entry.value);

		String itemClass = state.save.getString(paths[0] + ".classInstance");
		String itemName = state.save.getString(paths[0] + ".shortName");

		ItemEntry current = null;

		// find existing data
		for (ItemEntry item : data) {
			boolean add = true;
			if (filter.length > 0) {
				add = false;
				for (String f : filter) {
					if (item.type.equalsIgnoreCase(f)) {
						add = true;
						break;
					}
				}
			}
			if (Objects.equals(itemClass, item.id)) {
				current = item;
				add = true;
				// TODO handle objects that share the id
				// currently only applies to hardlight equipped underwear.
			}
			if (add) {
				combo.addItem(item);
			}
		}

		if (current == null) {
			// unknown value
			current = new ItemEntry(itemClass, itemName);
			combo.addItem(current);
		}

		combo.setSelectedItem(current);
		combo.setToolTipText(current.getToolTip());

		combo.addActionListener(e -> {
			for (String path : paths) {
				Object obj = combo.getSelectedItem();
				if (obj != null) {
					ItemEntry tmp = (ItemEntry) obj;
					state.save.setString(path + ".classInstance", tmp.id);
					state.save.setString(path + ".shortName", tmp.shortName);
					combo.setToolTipText(tmp.getToolTip());
				}

			}
		});

		return combo;
	}

	protected JTextField createIntegerEntry(ControlEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(prefWidth / 2, prefHeight));

		final String[] paths = getSaveIdents(entry.value);

		Integer saveValue = state.save.getInteger(paths[0]);
		if (saveValue == null) {
			field.setEnabled(false);
			field.setForeground(Color.RED);
			field.setText("<Not Available>");
		} else {
			field.setText("" + saveValue);
		}
		PlainDocument doc = (PlainDocument) field.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter.Integer(entry.min, entry.max));
		doc.addDocumentListener(new DocumentAdapter(e -> {
			int value = 0;
			try {
				value = Integer.valueOf(field.getText());
				for (String path : paths) {
					state.save.setInteger(path, value);
				}
			} catch (NumberFormatException ex) {
				return;
			}

		}));

		return field;
	}

	protected JTextField createDecimalEntry(ControlEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(prefWidth / 2, prefHeight));

		final String[] paths = getSaveIdents(entry.value);

		Double saveValue = state.save.getDecimal(paths[0]);
		if (saveValue == null) {
			field.setEnabled(false);
			field.setForeground(Color.RED);
			field.setText("<Not Available>");
		} else {
			DecimalFormat df = new DecimalFormat("#.###");
			field.setText(df.format(saveValue));
		}

		PlainDocument doc = (PlainDocument) field.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter.Double(entry.min, entry.max));
		doc.addDocumentListener(new DocumentAdapter(e -> {
			double value = 0;
			try {
				value = Double.valueOf(field.getText());
				for (String path : paths) {
					state.save.setDecimal(path, value);
				}
			} catch (NumberFormatException ex) {
				return;
			}

		}));

		return field;
	}

	protected JTextField createStringEntry(ControlEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(120, prefHeight));

		final String[] paths = getSaveIdents(entry.value);
		field.setText(state.save.getString(paths[0]));
		field.getDocument().addDocumentListener(new DocumentAdapter(e -> {
			for (String path : paths) {
				state.save.setString(path, field.getText());
			}
		}));

		return field;
	}

	protected JComboBox<EnumEntry<String>> createTextEnumEntry(ControlEntry entry) {
		final boolean allowCustom = entry.type == ControlEntry.Type.CustomTextEnum;
		JComboBox<EnumEntry<String>> combo = new JComboBox<EnumEntry<String>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));
		combo.setEditable(allowCustom);

		Map<String, String> data = state.data.getEnum(entry.ref);

		final String[] paths = getSaveIdents(entry.value);
		String saveValue = state.save.getString(paths[0]);

		EnumEntry<String> current = null;

		for (String key : Sort.sortStringKeySet(data, entry.sort)) {
			String value = data.get(key);
			EnumEntry<String> comboEntry = new EnumEntry<String>(key, value);
			combo.addItem(comboEntry);

			if (key.equals(saveValue)) {
				current = comboEntry;
			}
		}

		if (current == null) {
			if (allowCustom) {
				// create a custom entry
				current = new EnumEntry<String>(saveValue, saveValue);
				combo.addItem(current);
			} else {
				combo.setEnabled(false);
			}
		}

		combo.setSelectedItem(current);
		combo.addActionListener(e -> {
			Object obj = combo.getSelectedItem();
			if (obj == null) {
				return;
			}
			String id = "null";
			if (obj instanceof String) {
				id = (String) obj;
			} else if (obj instanceof EnumEntry<?>) {
				@SuppressWarnings("unchecked")
				EnumEntry<String> tmp = (EnumEntry<String>) obj;
				id = tmp.id;
			}
			if (id.length() == 0) {
				id = "null";
			}
			for (String path : paths) {
				state.save.setString(path, id);
			}
		});

		return combo;
	}

	private JLabel createTitleEntry(ControlEntry entry) {
		JLabel title = new JLabel(entry.value[0]);
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0),
				BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK)));
		return title;
	}

	private String[] getSaveIdents(String[] paths) {
		if (arrayPath == null) {
			return paths;
		}
		String[] idents = new String[paths.length];
		for (int i = 0; i < paths.length; ++i) {
			idents[i] = paths[i].replace("{ARRAY_ENTRY}", arrayPath);
		}
		return idents;
	}
}
