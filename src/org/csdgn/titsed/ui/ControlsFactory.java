package org.csdgn.titsed.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

import org.csdgn.maru.swing.DocumentAdapter;
import org.csdgn.maru.swing.NumberDocumentFilter;
import org.csdgn.titsed.model.ControlEntry;
import org.csdgn.titsed.ui.MainFrame.EnumEntry;

public class ControlsFactory {

	protected static List<String> sortIntegerKeySet(Map<String, String> enumData, String sort) {
		List<String> retKeys = new ArrayList<String>();

		retKeys.addAll(enumData.keySet());

		if (ControlEntry.SORT_KEY.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				int aN = Integer.parseInt(a);
				int bN = Integer.parseInt(b);
				return Integer.compare(aN, bN);
			});
		} else if (ControlEntry.SORT_KEY_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				int aN = Integer.parseInt(a);
				int bN = Integer.parseInt(b);
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if ("None".equalsIgnoreCase(aV)) {
					aN = Integer.MIN_VALUE;
				}
				if ("None".equalsIgnoreCase(bV)) {
					bN = Integer.MIN_VALUE;
				}
				return Integer.compare(aN, bN);
			});
		} else if (ControlEntry.SORT_VALUE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if (ControlEntry.SORT_VALUE_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if ("None".equalsIgnoreCase(aV)) {
					aV = "";
				}
				if ("None".equalsIgnoreCase(bV)) {
					bV = "";
				}
				return aV.compareTo(bV);
			});
		}

		return retKeys;
	}

	protected static List<String> sortStringKeySet(Map<String, String> enumData, String sort) {
		List<String> retKeys = new ArrayList<String>();

		retKeys.addAll(enumData.keySet());

		if (ControlEntry.SORT_KEY.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				return a.compareTo(b);
			});
		} else if (ControlEntry.SORT_KEY_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				if ("none".equalsIgnoreCase(a)) {
					a = "";
				}
				if ("none".equalsIgnoreCase(b)) {
					b = "";
				}
				return a.compareTo(b);
			});
		} else if (ControlEntry.SORT_VALUE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if (ControlEntry.SORT_VALUE_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if ("None".equalsIgnoreCase(aV)) {
					aV = "";
				}
				if ("None".equalsIgnoreCase(bV)) {
					bV = "";
				}
				return aV.compareTo(bV);
			});
		}

		return retKeys;
	}

	private int prefHeight;
	private int prefWidth;
	private ProgramState state;

	public ControlsFactory(ProgramState state) {
		this.state = state;
		prefHeight = 24;
		prefWidth = 120;
	}

	protected JComboBox<EnumEntry<Boolean>> createBooleanEntry(ControlEntry entry) {
		JComboBox<EnumEntry<Boolean>> combo = new JComboBox<EnumEntry<Boolean>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));

		EnumEntry<Boolean> eeTrue = new EnumEntry<Boolean>(true, "True");
		EnumEntry<Boolean> eeFalse = new EnumEntry<Boolean>(false, "False");

		combo.addItem(eeTrue);
		combo.addItem(eeFalse);

		if (state.save.getBoolean(entry.value[0])) {
			combo.setSelectedItem(eeTrue);
		} else {
			combo.setSelectedItem(eeFalse);
		}

		combo.addActionListener(e -> {
			for (String path : entry.value) {
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

	protected JComponent createEntry(ControlEntry entry) {
		if (entry.type.startsWith(ControlEntry.TYPE_TEXT_ENUM)) {
			return createTextEnumEntry(entry);
		} else if (entry.type.startsWith(ControlEntry.TYPE_ENUM)) {
			return createEnumEntry(entry);
		} else if (entry.type.startsWith(ControlEntry.TYPE_FLAGS)) {
			return createFlagsEntry(entry);
		} else
			switch (entry.type) {
			case ControlEntry.TYPE_TITLE: {
				JLabel title = new JLabel(entry.value[0]);
				title.setFont(title.getFont().deriveFont(Font.BOLD));

				title.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0),
						BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK)));
				return title;
			}
			case ControlEntry.TYPE_LABEL: {
				return new JLabel(entry.value[0]);
			}
			case ControlEntry.TYPE_BOOLEAN: {
				return createBooleanEntry(entry);
			}
			case ControlEntry.TYPE_STRING:
				return createStringEntry(entry);
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				return createNumberEntry(entry);
			}

		throw new RuntimeException("Unknown Data Type: " + entry.type);
	}

	protected JComboBox<EnumEntry<Integer>> createEnumEntry(ControlEntry entry) {
		JComboBox<EnumEntry<Integer>> combo = new JComboBox<EnumEntry<Integer>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));

		// load data
		String enumName = entry.type.substring(entry.type.indexOf(':') + 1);
		Map<String, String> enumData = state.data.getEnum(enumName);

		int gameValue = state.save.getInteger(entry.value[0]);
		EnumEntry<Integer> current = null;

		List<String> keys = sortIntegerKeySet(enumData, entry.sort);

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
			combo.setSelectedItem(current);
		}

		combo.setSelectedItem(current);
		combo.addActionListener(e -> {
			for (String path : entry.value) {
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

		String enumName = entry.type.substring(entry.type.indexOf(':') + 1);
		Map<String, String> enumData = state.data.getEnum(enumName);

		panel.setTitle(enumName);
		panel.collapse();

		Set<Integer> flagSet = new HashSet<Integer>();
		for (String path : entry.value) {
			flagSet.addAll(state.save.getFlags(path));
		}

		final Map<JCheckBox, Integer> boxMap = new IdentityHashMap<JCheckBox, Integer>();

		// we will be depleting the set in case
		// we have any unknown values that need display
		List<String> keys = ControlsFactory.sortIntegerKeySet(enumData, entry.sort);
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
				for (String path : entry.value) {
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

	protected JTextField createNumberEntry(ControlEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(prefWidth / 2, prefHeight));

		// TODO number entry only
		field.setText("" + state.save.getInteger(entry.value[0]));
		PlainDocument doc = (PlainDocument) field.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter.Integer());
		doc.addDocumentListener(new DocumentAdapter(e -> {
			int value = 0;
			try {
				value = Integer.valueOf(field.getText());
			} catch (NumberFormatException ex) {
				return;
			}
			for (String path : entry.value) {
				state.save.setInteger(path, value);
			}
		}));

		return field;
	}

	protected JTextField createStringEntry(ControlEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(120, prefHeight));

		field.setText(state.save.getString(entry.value[0]));
		field.getDocument().addDocumentListener(new DocumentAdapter(e -> {
			for (String path : entry.value) {
				state.save.setString(path, field.getText());
			}
		}));

		return field;
	}

	protected JComboBox<EnumEntry<String>> createTextEnumEntry(ControlEntry entry) {

		JComboBox<EnumEntry<String>> combo = new JComboBox<EnumEntry<String>>();
		combo.setPreferredSize(new Dimension(prefWidth, prefHeight));
		combo.setEditable(entry.enumTextEdit);

		boolean changeable = true;

		// load data
		String enumName = entry.type.substring(entry.type.indexOf(':') + 1);
		Map<String, String> enumData = state.data.getEnum(enumName);

		String gameValue = state.save.getString(entry.value[0]);
		EnumEntry<String> current = null;

		List<String> keys = sortStringKeySet(enumData, entry.sort);

		for (String key : keys) {
			String value = enumData.get(key);
			EnumEntry<String> ee = new EnumEntry<String>(key, value);
			combo.addItem(ee);

			if (key.equals(gameValue)) {
				current = ee;
			}
		}

		if (current != null) {
			combo.setSelectedItem(current);
		} else {
			// TODO unknown value
			if (!entry.enumTextEdit) {
				// lock it down if it is not editable
				combo.setForeground(Color.RED);
				combo.setEnabled(false);
			}
			changeable = entry.enumTextEdit;

			// change the text to match whatever is stored (custom/unknown value)
			Component comp = combo.getEditor().getEditorComponent();
			if (comp instanceof JTextField) {
				JTextField field = (JTextField) comp;
				field.setText(gameValue);
			}
		}

		if (changeable) {
			combo.addActionListener(e -> {
				for (String path : entry.value) {
					// save.setString(path, field.getText());
					Object obj = combo.getSelectedItem();
					if (obj != null) {
						@SuppressWarnings("unchecked")
						EnumEntry<String> tmp = (EnumEntry<String>) obj;
						// set it to null if zero length
						String id = tmp.id;
						if (id.length() == 0) {
							id = "null";
						}
						state.save.setString(path, id);

					}
				}
			});
		}

		return combo;
	}
}
