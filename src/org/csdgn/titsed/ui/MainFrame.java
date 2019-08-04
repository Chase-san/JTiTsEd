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

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import org.csdgn.maru.Updater;
import org.csdgn.maru.swing.TableLayout;
import org.csdgn.titsed.FileManager;
import org.csdgn.titsed.SavePath;
import org.csdgn.titsed.model.ControlEntry;
import org.csdgn.titsed.model.DataModel;
import org.csdgn.titsed.model.SaveModel;

public class MainFrame extends JFrame {
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

	private class TabUpdater implements Updater {
		String tabName;

		public TabUpdater(String tabName) {
			this.tabName = tabName;
		}

		@Override
		public void update() {
			rebuildTab(tabName);
		}
	}

	private static final int MAXIMUM_SAVE_COUNT = 14;
	private static final long serialVersionUID = -6707796242859221178L;
	private JFileChooser chooser;
	private ControlsFactory factory;
	private JMenu fileOpen;
	private FileManager files;
	private JMenu fileSave;
	private JMenuItem fileSaveFile;

	private ProgramState state;

	private JTabbedPane tabs;

	public MainFrame(FileManager fm, DataModel dm) {
		state = new ProgramState();
		factory = new ControlsFactory(state);
		this.files = fm;
		state.data = dm;

		setTitle(UIStrings.getString("UI.Title") + " " + UIStrings.getString("UI.Version"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationByPlatform(true);
		setIconImage(UIStrings.getImage("UI.Icon"));

		setJMenuBar(buildMenuBar());

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

		tabs = new JTabbedPane();
		setContentPane(tabs);

		setSize(300, 500);
	}

	public JMenu buildFileMenu() {
		JMenu file = new JMenu(UIStrings.getString("UI.MenuFile"));
		file.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuSelected(MenuEvent e) {
				files.updateSavePaths();
				buildOpenSubmenu();
				buildSaveSubmenu();
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

	private JScrollPane buildTabScroll(String tabName) {
		JComponent comp = buildTabLayout(tabName);
		JScrollPane scroll = new JScrollPane(comp);
		int h = scroll.getViewport().getView().getPreferredSize().height;
		int unit = Math.max(1, (int) (h / 50.0));
		scroll.getVerticalScrollBar().setUnitIncrement(unit);

		return scroll;
	}

	private void buildLayout() {
		tabs.removeAll();
		for (String tabName : state.data.getTabs()) {
			tabs.addTab(tabName, buildTabScroll(tabName));
		}
	}

	private JMenuBar buildMenuBar() {
		JMenuBar bar = new JMenuBar();
		bar.add(buildFileMenu());
		return bar;
	}

	private void buildOpenSubmenu() {
		// build menus
		fileOpen.removeAll();
		for (SavePath path : files.getSavePaths()) {
			JMenu fileOpenPath = new JMenu(path.name);
			fileOpen.add(fileOpenPath);

			// for each item in the save
			for (int index : path.saves.keySet()) {
				final File file = path.saves.get(index);
				final String info = path.info.get(index);
				String name = String.format("%d. %s", index, info);

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

	private void buildSaveSubmenu() {
		fileSave.removeAll();
		// build menus
		for (SavePath path : files.getSavePaths()) {
			JMenu fileSavePath = new JMenu(path.name);
			fileSave.add(fileSavePath);

			// for each item in the save
			for (int i = 1; i <= MAXIMUM_SAVE_COUNT; ++i) {
				final File file = path.saves.get(i);
				final String info = path.info.get(i);
				String name = String.format("%d. none", i);
				if (file != null) {
					name = String.format("%d. %s", i, info);
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

	private JComponent buildTabLayout(String tabName) {
		JPanel panel = new JPanel(new TableLayout(4, 4));
		TabUpdater tu = new TabUpdater(tabName);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		boolean showRow = true;
		int row = -1;
		int col = 0;

		for (ControlEntry entry : state.data.getTabDataMap(tabName)) {
			switch (entry.type) {
			case ControlEntry.TYPE_ROW: {
				showRow = true;
				if (entry.arrayRow && factory.arraySize == 0) {
					showRow = false;
				}
				if (showRow) {
					col = 0;
					row = row + 1;
				}

				break;
			}
			default:
				if (showRow) {
					try {
						panel.add(factory.createEntry(tu, entry), "x=" + col + ";y=" + row + ";colspan=" + entry.span);
					} catch (Exception e) {
						System.err.println("Error on building '" + entry.type + "' entry: " + entry.value[0]);
					}
					col = col + 1;
				}
			}
		}

		return panel;
	}

	private void open(File file) {
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
		fileSave.setEnabled(true);

		buildLayout();

	}

	private void rebuildTab(String tabName) {
		int index = tabs.indexOfTab(tabName);
		JScrollPane pane = (JScrollPane)tabs.getComponentAt(index);
		
		int vPos = pane.getVerticalScrollBar().getValue();
		int hPos = pane.getHorizontalScrollBar().getValue();
		
		pane.setVisible(false);
		pane.setViewportView(buildTabLayout(tabName));
		pane.setVisible(true);
		
		pane.getVerticalScrollBar().setValue(vPos);
		pane.getHorizontalScrollBar().setValue(hPos);
	}

	private void saveSerial(File dst) {
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

	private void saveSlot(File directory, int index) {
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
}
