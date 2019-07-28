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
import java.awt.Desktop;
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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.csdgn.amf3.AmfFile;
import org.csdgn.amf3.AmfIO;
import org.csdgn.amf3.AmfObject;
import org.csdgn.amf3.AmfValue;
import org.csdgn.amf3.UnexpectedDataException;
import org.csdgn.maru.swing.TableLayout;
import org.csdgn.titsed.FileManager;
import org.csdgn.titsed.SavePath;
import org.csdgn.titsed.model.ControlEntry;
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
	private ProgramState state;
	private ToolFactory tools;
	private JFileChooser chooser;

	public MainFrame(FileManager fm, DataModel dm) {
		state = new ProgramState();
		tools = new ToolFactory(state);
		this.files = fm;
		state.data = dm;

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

	protected JComponent createEntry(ControlEntry entry) {
		if (entry.type.startsWith(ControlEntry.TYPE_TEXT_ENUM)) {
			return tools.createTextEnumEntry(entry);
		} else if (entry.type.startsWith(ControlEntry.TYPE_ENUM)) {
			return tools.createEnumEntry(entry);
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
				return tools.createBooleanEntry(entry);
			}
			case ControlEntry.TYPE_STRING:
				return tools.createStringEntry(entry);
			case ControlEntry.TYPE_INTEGER:
			case ControlEntry.TYPE_DECIMAL:
				return tools.createNumberEntry(entry);
			}

		throw new RuntimeException("Unknown Data Type: " + entry.type);
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
		List<String> keys = ToolFactory.sortIntegerKeySet(enumData, entry.sort);
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

	public void buildLayout() {
		tabs.removeAll();

		int row = -1;
		int col = 0;
		JPanel panel = null;
		List<JScrollPane> panes = new ArrayList<JScrollPane>();

		// wait for new tab
		for (ControlEntry entry : state.data.getDataMap()) {
			switch (entry.type) {
			case ControlEntry.TYPE_TAB: {
				panel = new JPanel(new TableLayout(4, 4));
				panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				JScrollPane scroll = new JScrollPane(panel);
				tabs.addTab(entry.value[0], scroll);
				panes.add(scroll);

				row = -1;
				col = 0;
				break;
			}
			case ControlEntry.TYPE_ROW: {
				col = 0;
				row = row + 1;
				break;
			}
			default:
				try {
					panel.add(createEntry(entry), "x=" + col + ";y=" + row + ";colspan=" + entry.span);
				} catch (Exception e) {
					System.err.println("Error on building " + entry.type + " entry: " + entry.value[0]);
				}
				col = col + 1;
			}
		}

		for (JScrollPane scroll : panes) {
			int h = scroll.getViewport().getView().getPreferredSize().height;
			int unit = Math.max(1, (int) (h / 50.0));
			scroll.getVerticalScrollBar().setUnitIncrement(unit);
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
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
		file.setMnemonic('f');

		// Open ->
		fileOpen = new JMenu(UIStrings.getString("UI.MenuFileOpen"));
		fileOpen.setMnemonic('o');

		file.add(fileOpen);
		buildOpenSubmenu();

		// Open File
		JMenuItem fileOpenFile = new JMenuItem(UIStrings.getString("UI.MenuFileOpenFile"));
		fileOpenFile.setMnemonic('p');
		fileOpenFile.addActionListener(e -> {
			chooser.setDialogTitle("Import");
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				open(chooser.getSelectedFile());
			}
		});
		file.add(fileOpenFile);

		// -
		file.addSeparator();

		// Save ->
		fileSave = new JMenu(UIStrings.getString("UI.MenuFileSave"));
		fileSave.setEnabled(false);
		fileSave.setMnemonic('s');

		file.add(fileSave);

		// Save File
		fileSaveFile = new JMenuItem(UIStrings.getString("UI.MenuFileSaveFile"));
		// fileSaveFile.setEnabled(false);
		fileSaveFile.setMnemonic('a');
		fileSaveFile.addActionListener(e -> {
			chooser.setDialogTitle("Export");
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				saveSerial(chooser.getSelectedFile());
			}
		});
		file.add(fileSaveFile);

		// -
		file.addSeparator();

		// Exit
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
		// build menus
		fileOpen.removeAll();
		for (SavePath path : files.getSavePaths()) {
			JMenu fileOpenPath = new JMenu(path.name);
			fileOpen.add(fileOpenPath);

			// for each item in the save
			for (int index : path.saves.keySet()) {
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
		if (file == null || !file.exists()) {
			return;
		}
		state.save = null;

		try {
			if (AmfIO.isAmfFile(file)) {
				AmfFile amfFile = AmfIO.readFile(file);
				if (amfFile != null) {
					state.save = new SaveModel(amfFile);
				}
			} else {
				AmfValue amfValue = AmfIO.read(file);
				if (amfValue != null) {
					state.save = new SaveModel(amfValue);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedDataException e) {
			e.printStackTrace();
		}
		if (state.save == null) {
			return;
		}
		// fileSaveFile.setEnabled(true);
		fileSave.setEnabled(true);

		buildLayout();

	}

	public void saveSlot(File directory, int index) {
		String name = String.format("TiTs_%d", index);
		String filename = name + ".sol";

		File file = new File(directory, filename);

		try {
			AmfFile out = null;
			if (state.save.isFile) {
				out = state.save.srcFile;
			} else {
				// build a fake AmfFile for this
				out = new AmfFile();
				out.putAll(((AmfObject) state.save.srcValue).getDynamicMap());
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
		if (!filename.toLowerCase().endsWith(".tits")) {
			dst = new File(dst.getParentFile(), filename + ".tits");
		}

		try {
			AmfObject out = null;
			if (state.save.isFile) {
				// build a fake AmfObject for this
				out = new AmfObject();
				out.setDynamic(true);
				out.getDynamicMap().putAll(state.save.srcFile);
			} else {
				out = (AmfObject) state.save.srcValue;
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
		// build menus
		for (SavePath path : files.getSavePaths()) {
			JMenu fileSavePath = new JMenu(path.name);
			fileSave.add(fileSavePath);

			// for each item in the save
			for (int i = 1; i <= 14; ++i) {
				final File file = path.saves.get(i);
				String name = String.format("%d. none", i);
				if (file != null) {
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
