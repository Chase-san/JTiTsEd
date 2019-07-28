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
package org.csdgn.titsed.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.PlainDocument;

import org.csdgn.amf3.AmfFile;
import org.csdgn.amf3.AmfIO;
import org.csdgn.amf3.AmfObject;
import org.csdgn.amf3.AmfValue;
import org.csdgn.amf3.UnexpectedDataException;
import org.csdgn.maru.swing.DocumentAdapter;
import org.csdgn.maru.swing.NumberDocumentFilter;
import org.csdgn.maru.swing.TableLayout;
import org.csdgn.titsed.FileManager;
import org.csdgn.titsed.SavePath;
import org.csdgn.titsed.model.DataEntry;
import org.csdgn.titsed.model.DataModel;
import org.csdgn.titsed.model.SaveModel;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = -6707796242859221178L;
	
	
	protected static class EnumEntry<T> {
		protected final T id;
		protected final String text;
		
		protected EnumEntry(T id, String text) {
			this.id = id;
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
	}
	
	private JMenu fileSave;
	private JMenu fileOpen;
	private JMenuItem fileSaveFile;
	private JTabbedPane tabs;
	private FileManager files;
	private DataModel data;
	private SaveModel save;
	private JFileChooser chooser;
	
	public MainFrame(FileManager fm, DataModel dm) {
		this.files = fm;
		this.data = dm;
		
		setTitle(UIStrings.getString("UI.Title") + " " + UIStrings.getString("UI.Version"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		setJMenuBar(buildMenuBar());
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		
		tabs = new JTabbedPane();
		setContentPane(tabs);
		
		setSize(300, 500);
	}
	
	protected JComponent createEntry(DataEntry entry) {
		if(entry.type.startsWith(DataEntry.TYPE_TEXT_ENUM)) {
			return createTextEnumEntry(entry);
		} else if(entry.type.startsWith(DataEntry.TYPE_ENUM)) {
			return createEnumEntry(entry);
		} else if(entry.type.startsWith(DataEntry.TYPE_FLAGS)) {
			return createFlagsEntry(entry);
		} else switch(entry.type) {
		case DataEntry.TYPE_TITLE: {
			JLabel title = new JLabel(entry.value[0]);
			title.setFont(title.getFont().deriveFont(Font.BOLD));
			
			title.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createEmptyBorder(8, 0, 0, 0),
							BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK))
					);
			return title;
		}
		case DataEntry.TYPE_LABEL: {
			return new JLabel(entry.value[0]);
		}
		case DataEntry.TYPE_BOOLEAN: {
			return createBooleanEntry(entry);
		}
		case DataEntry.TYPE_STRING:
			return createStringEntry(entry);
		case DataEntry.TYPE_INTEGER:
		case DataEntry.TYPE_DECIMAL:
			return createNumberEntry(entry);
		}

		throw new RuntimeException("Unknown Data Type: " + entry.type);
	}
	
	protected JComponent createFlagsEntry(DataEntry entry) {
		ExpansionPanel panel = new ExpansionPanel();
		int cols = entry.span;
		if(cols > 2) {
			cols = (entry.span + 1) / 2;
		}
		panel.setLayout(new GridLayout(0, cols));
		
		
		String enumName = entry.type.substring(entry.type.indexOf(':')+1);
		Map<String, String> enumData = data.getEnum(enumName);
		
		panel.setTitle(enumName);
		panel.collapse();
		
		Set<Integer> flagSet = new HashSet<Integer>();
		for(String path : entry.value) {
			flagSet.addAll(save.getFlags(path));
		}
		
		final Map<JCheckBox, Integer> boxMap = new IdentityHashMap<JCheckBox, Integer>();  
		
		//we will be depleting the set in case
		//we have any unknown values that need display
		List<String> keys = sortIntegerKeySet(enumData, entry.sort);
		for(String key : keys) {
			String value = enumData.get(key);
			Integer nKey = Integer.parseInt(key);
			EnumEntry<Integer> ee = new EnumEntry<Integer>(nKey, value);
			
			//for each id
			JCheckBox box = new JCheckBox();
			box.setText(ee.text);
			if(flagSet.contains(nKey)) {
				box.setSelected(true);
			}
			flagSet.remove(nKey);
			boxMap.put(box, nKey);
			panel.add(box);
		}
		
		//handle all unknown flags that are set
		for(Integer unknownFlag : flagSet) {
			JCheckBox box = new JCheckBox();
			box.setText("Flag " + unknownFlag);
			box.setSelected(true);
			boxMap.put(box, unknownFlag);
			panel.add(box);
		}
		flagSet.clear();
		
		//setup callbacks
		for(final JCheckBox box : boxMap.keySet()) {
			box.addActionListener(e -> {
				int id = boxMap.get(box);
				for(String path : entry.value) {
					if(box.isSelected()) {
						//add flag
						save.addFlag(path, id);	
					} else {
						//remove flag
						save.removeFlag(path, id);
					}
				}
			});
		}
		
		return panel;
	}
	
	protected static List<String> sortIntegerKeySet(Map<String, String> enumData, String sort) {
		List<String> retKeys = new ArrayList<String>();
		
		retKeys.addAll(enumData.keySet());
		
		if(DataEntry.SORT_KEY.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				int aN = Integer.parseInt(a);
				int bN = Integer.parseInt(b);
				return Integer.compare(aN, bN);
			});
		} else if(DataEntry.SORT_KEY_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				int aN = Integer.parseInt(a);
				int bN = Integer.parseInt(b);
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if("None".equalsIgnoreCase(aV)) {
					aN = Integer.MIN_VALUE;
				}
				if("None".equalsIgnoreCase(bV)) {
					bN = Integer.MIN_VALUE;
				}
				return Integer.compare(aN, bN);
			});
		} else if(DataEntry.SORT_VALUE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if(DataEntry.SORT_VALUE_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if("None".equalsIgnoreCase(aV)) {
					aV = "";
				}
				if("None".equalsIgnoreCase(bV)) {
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
		
		if(DataEntry.SORT_KEY.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				return a.compareTo(b);
			});
		} else if(DataEntry.SORT_KEY_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				if("none".equalsIgnoreCase(a)) {
					a = "";
				}
				if("none".equalsIgnoreCase(b)) {
					b = "";
				}
				return a.compareTo(b);
			});
		} else if(DataEntry.SORT_VALUE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if(DataEntry.SORT_VALUE_NONE.equalsIgnoreCase(sort)) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				if("None".equalsIgnoreCase(aV)) {
					aV = "";
				}
				if("None".equalsIgnoreCase(bV)) {
					bV = "";
				}
				return aV.compareTo(bV);
			});
		}
		
		return retKeys;
	}
	
	protected JComboBox<EnumEntry<Integer>> createEnumEntry(DataEntry entry) {
		JComboBox<EnumEntry<Integer>> combo = new JComboBox<EnumEntry<Integer>>();
		combo.setPreferredSize(new Dimension(120, 18));
		
		//load data
		String enumName = entry.type.substring(entry.type.indexOf(':')+1);
		Map<String, String> enumData = data.getEnum(enumName);
		
		int gameValue = save.getInteger(entry.value[0]);
		EnumEntry<Integer> current = null;
		
		List<String> keys = sortIntegerKeySet(enumData, entry.sort);
		
		for(String key : keys) {
			String value = enumData.get(key);
			Integer nKey = Integer.parseInt(key);
			EnumEntry<Integer> ee = new EnumEntry<Integer>(nKey, value);
			combo.addItem(ee);
			
			if(nKey == gameValue) {
				current = ee;
			}
		}
		
		if(current == null) {
			//unknown value
			current = new EnumEntry<Integer>(gameValue, "Unknown Type " + gameValue);
			combo.addItem(current);
			combo.setSelectedItem(current);
		}
		
		combo.setSelectedItem(current);
		combo.addActionListener(e -> {
			for(String path : entry.value) {
				//save.setString(path, field.getText());
				Object obj = combo.getSelectedItem();
				if(obj != null) {
					@SuppressWarnings("unchecked")
					EnumEntry<Integer> tmp = (EnumEntry<Integer>)obj;
					save.setInteger(path, tmp.id);
				}
				
			}
		});
		
		return combo;
	}
	
	protected JComboBox<EnumEntry<String>> createTextEnumEntry(DataEntry entry) {
		
		JComboBox<EnumEntry<String>> combo = new JComboBox<EnumEntry<String>>();
		combo.setPreferredSize(new Dimension(120, 18));
		combo.setEditable(entry.enumTextEdit);
		
		boolean changeable = true;
		
		//load data
		String enumName = entry.type.substring(entry.type.indexOf(':')+1);
		Map<String, String> enumData = data.getEnum(enumName);
		
		String gameValue = save.getString(entry.value[0]);
		EnumEntry<String> current = null;
		
		List<String> keys = sortStringKeySet(enumData, entry.sort);
		
		for(String key : keys) {
			String value = enumData.get(key);
			EnumEntry<String> ee = new EnumEntry<String>(key, value);
			combo.addItem(ee);
			
			if(key.equals(gameValue)) {
				current = ee;
			}
		}
		
		if(current != null) {
			combo.setSelectedItem(current);
		} else {
			//TODO unknown value
			if(!entry.enumTextEdit) {
				//lock it down if it is not editable
				combo.setForeground(Color.RED);
				combo.setEnabled(false);
			}
			changeable = entry.enumTextEdit;
			
			//change the text to match whatever is stored (custom/unknown value)
			Component comp = combo.getEditor().getEditorComponent();
			if(comp instanceof JTextField) {
				JTextField field = (JTextField)comp;
				field.setText(gameValue);
			}
		}
		
		if(changeable) {
			combo.addActionListener(e -> {
				for(String path : entry.value) {
					//save.setString(path, field.getText());
					Object obj = combo.getSelectedItem();
					if(obj != null) {
						@SuppressWarnings("unchecked")
						EnumEntry<String> tmp = (EnumEntry<String>)obj;
						//set it to null if zero length
						String id = tmp.id;
						if(id.length() == 0) {
							id = "null";
						}
						save.setString(path, id);
						
					}
				}
			});
		}
		
		
		return combo;
	}
	
	protected JTextField createNumberEntry(DataEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(60, 18));
		
		//TODO number entry only
		field.setText(""+save.getInteger(entry.value[0]));
		PlainDocument doc = (PlainDocument)field.getDocument(); 
		doc.setDocumentFilter(new NumberDocumentFilter.Integer());
		doc.addDocumentListener(new DocumentAdapter(e -> {
			int value = 0;
			try {
				value = Integer.valueOf(field.getText());
			} catch(NumberFormatException ex) {
				return;
			}
			for(String path : entry.value) {
				save.setInteger(path, value);
			}
		}));
		
		return field;
	}
	
	protected JTextField createStringEntry(DataEntry entry) {
		JTextField field = new JTextField();
		field.setPreferredSize(new Dimension(120, 18));
		
		field.setText(save.getString(entry.value[0]));
		field.getDocument().addDocumentListener(new DocumentAdapter(e -> {
			for(String path : entry.value) {
				save.setString(path, field.getText());
			}
		}));
		
		return field;
	}
	
	protected JComboBox<EnumEntry<Boolean>> createBooleanEntry(DataEntry entry) {
		JComboBox<EnumEntry<Boolean>> combo = new JComboBox<EnumEntry<Boolean>>();
		combo.setPreferredSize(new Dimension(120, 18));
		
		EnumEntry<Boolean> eeTrue = new EnumEntry<Boolean>(true, "True");
		EnumEntry<Boolean> eeFalse = new EnumEntry<Boolean>(false, "False");
		
		combo.addItem(eeTrue);
		combo.addItem(eeFalse);
		
		if(save.getBoolean(entry.value[0])) {
			combo.setSelectedItem(eeTrue);	
		} else {
			combo.setSelectedItem(eeFalse);
		}
		
		combo.addActionListener(e -> {
			for(String path : entry.value) {
				Object obj = combo.getSelectedItem();
				if(obj != null) {
					@SuppressWarnings("unchecked")
					EnumEntry<Boolean> tmp = (EnumEntry<Boolean>)obj;
					save.setBoolean(path, tmp.id);
				}
			}
		});
		
		return combo;
	}
	
	
	public void buildLayout() {
		tabs.removeAll();
		
		int row = -1;
		int col = 0;
		JPanel panel = null;
		
		//wait for new tab
		for(DataEntry entry : data.getDataMap()) {
			switch(entry.type) {
			case DataEntry.TYPE_TAB: {
				panel = new JPanel(new TableLayout(4,4));
				panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				JScrollPane scroll = new JScrollPane(panel);
				tabs.addTab(entry.value[0], scroll);
				
				row = -1;
				col = 0;
				break;
			}
			case DataEntry.TYPE_ROW:
				col = 0;
				row = row + 1;
				break;
			default: try {
				panel.add(createEntry(entry), "x=" + col + ";y=" + row +";colspan="+entry.span);
			} catch(Exception e) {
				System.err.println("Error on building " + entry.type + " entry: " + entry.value[0]);
			}
				col = col + 1;
			}
		}
		
	}
	
	public JMenu buildFileMenu() {
		JMenu file = new JMenu(UIStrings.getString("UI.MenuFile"));
		file.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				files.updateSavePaths();
				buildOpenSubmenu();
				buildSaveSubmenu();
			}

			@Override
			public void menuDeselected(MenuEvent e) {}

			@Override
			public void menuCanceled(MenuEvent e) {}
			
		});
		file.setMnemonic('f');
		
		//Open ->
		fileOpen = new JMenu(UIStrings.getString("UI.MenuFileOpen"));
		fileOpen.setMnemonic('o');
		
		file.add(fileOpen);
		buildOpenSubmenu();
		
		//Open File
		JMenuItem fileOpenFile = new JMenuItem(UIStrings.getString("UI.MenuFileOpenFile"));
		fileOpenFile.setMnemonic('p');
		fileOpenFile.addActionListener(e -> {
			chooser.setDialogTitle("Import");
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				open(chooser.getSelectedFile());
			}
		});
		file.add(fileOpenFile);
		
		//-
		file.addSeparator();
		
		//Save ->
		fileSave = new JMenu(UIStrings.getString("UI.MenuFileSave"));
		fileSave.setEnabled(false);
		fileSave.setMnemonic('s');
		
		file.add(fileSave);
		
		//Save File
		fileSaveFile = new JMenuItem(UIStrings.getString("UI.MenuFileSaveFile"));
		//fileSaveFile.setEnabled(false);
		fileSaveFile.setMnemonic('a');
		fileSaveFile.addActionListener(e -> {
			chooser.setDialogTitle("Export");
			if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				saveSerial(chooser.getSelectedFile());
			}
		});
		file.add(fileSaveFile);
		
		//-
		file.addSeparator();
		
		//Exit
		JMenuItem fileExit = new JMenuItem(UIStrings.getString("UI.MenuFileExit"));
		fileExit.addActionListener(e -> {
			dispose();
		});
		fileExit.setMnemonic('x');
		file.add(fileExit);
		
		return file;
	}
	
	public JMenuBar buildMenuBar() {
		JMenuBar bar = new JMenuBar();
		bar.add(buildFileMenu());
		return bar;
	}
	
	public void buildOpenSubmenu() {
		//build menus
		fileOpen.removeAll();
		for(SavePath path : files.getSavePaths()) {
			JMenu fileOpenPath = new JMenu(path.name);
			fileOpen.add(fileOpenPath);
			
			//for each item in the save
			for(int index : path.saves.keySet()) {
				final File file = path.saves.get(index);
				String name = String.format("%d. TiTs %d", index, index);
				JMenuItem fileOpenPathItem = new JMenuItem(name);
				fileOpenPathItem.addActionListener(e -> {
					open(file);
				});
				fileOpenPath.add(fileOpenPathItem);
			}
			
			fileOpenPath.addSeparator();
			JMenuItem fileOpenPathOpenDir = new JMenuItem(UIStrings.getString("UI.MenuFileOpenDirectory"));
			fileOpenPath.add(fileOpenPathOpenDir);
			fileOpenPathOpenDir.addActionListener(e -> {
				try {
					Desktop.getDesktop().open(path.directory);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			
		}
	}
	
	public void open(File file) {
		if(file == null || !file.exists()) {
			return;
		}
		save = null;

		try {
			if(AmfIO.isAmfFile(file)) {
				AmfFile amfFile = AmfIO.readFile(file);
				if(amfFile != null) {
					save = new SaveModel(amfFile);
				}
			} else {
				AmfValue amfValue = AmfIO.read(file);
				if(amfValue != null) {
					save = new SaveModel(amfValue);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedDataException e) {
			e.printStackTrace();
		}
		if(save == null) { 
			return;
		}
		//fileSaveFile.setEnabled(true);
		fileSave.setEnabled(true);
		
		buildLayout();
		
		
	}
	
	public void saveSlot(File directory, int index) {
		String name = String.format("TiTs_%d", index);
		String filename = name + ".sol";
		
		File file = new File(directory, filename);
		
		try {
			AmfFile out = null;
			if(save.isFile) {
				out = save.srcFile;
			} else {
				//build a fake AmfFile for this
				out = new AmfFile();
				out.putAll(((AmfObject)save.srcValue).getDynamicMap());
			}
			out.setName(name);
			AmfIO.writeFile(out, file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedDataException e) {
			e.printStackTrace();
		}
	}
	
	public void saveSerial(File dst) {
		String filename = dst.getName();
		if(!filename.toLowerCase().endsWith(".tits")) {
			dst = new File(dst.getParentFile(), filename + ".tits");
		}
		
		try {
			AmfObject out = null;
			if(save.isFile) {
				//build a fake AmfObject for this
				out = new AmfObject();
				out.setDynamic(true);
				out.getDynamicMap().putAll(save.srcFile);
			} else {
				out = (AmfObject)save.srcValue;
			}
			AmfIO.write(out, dst);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedDataException e) {
			e.printStackTrace();
		}
	}
	
	public void buildSaveSubmenu() {
		fileSave.removeAll();
		//build menus
		for(SavePath path : files.getSavePaths()) {
			JMenu fileSavePath = new JMenu(path.name);
			fileSave.add(fileSavePath);
			
			//for each item in the save
			for(int i = 1; i <= 14; ++i) {
				final File file = path.saves.get(i);
				String name = String.format("%d. none", i); 
				if(file != null) {
					name = String.format("%d. TiTs %d", i, i);
				}
				final int index = i;
				JMenuItem fileSavePathItem = new JMenuItem(name);
				fileSavePathItem.addActionListener(e -> {
					saveSlot(path.directory, index);
				});
				fileSavePath.add(fileSavePathItem);
			}
			
			fileSavePath.addSeparator();
			JMenuItem fileSavePathOpenDir = new JMenuItem(UIStrings.getString("UI.MenuFileOpenDirectory"));
			fileSavePath.add(fileSavePathOpenDir);
			fileSavePathOpenDir.addActionListener(e -> {
				try {
					Desktop.getDesktop().open(path.directory);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			
		}
	}
}
