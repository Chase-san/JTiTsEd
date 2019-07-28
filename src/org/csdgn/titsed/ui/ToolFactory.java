package org.csdgn.titsed.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

import org.csdgn.maru.swing.DocumentAdapter;
import org.csdgn.maru.swing.NumberDocumentFilter;
import org.csdgn.titsed.model.ControlEntry;
import org.csdgn.titsed.ui.MainFrame.EnumEntry;

public class ToolFactory {

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

	private ProgramState state;
	private int prefHeight;
	private int prefWidth;

	public ToolFactory(ProgramState state) {
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
