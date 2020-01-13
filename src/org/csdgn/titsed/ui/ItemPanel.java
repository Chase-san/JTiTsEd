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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.csdgn.titsed.model.DataModel;
import org.csdgn.titsed.model.ItemEntry;

public class ItemPanel extends JPanel {
    private static final int maxItemTitleLength = 24;
    private static final long serialVersionUID = 6072760133338826691L;

    //
    private ItemEntry selectedItem;
    private JDialog dialog;
    private Map<String, List<ItemEntry>> map = new HashMap<String, List<ItemEntry>>();
    private ButtonGroup group;

    private void buildMapping(DataModel model) {
        map = new HashMap<String, List<ItemEntry>>();
        for (ItemEntry item : model.getItemList()) {
            List<ItemEntry> list = map.get(item.type);
            if (list == null) {
                list = new ArrayList<ItemEntry>();
                map.put(item.type, list);
            }
            list.add(item);
        }
    }

    private ExpansionPanel createCategoryPanel(String category) {

        ExpansionPanel panel = new ExpansionPanel();
        panel.setTitle(category.replace("_", " "));
        Font f = panel.getButtonTextLabel().getFont();
        panel.getButtonTextLabel().setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        panel.setLayout(new GridLayout(0, 3, 2, 2));

        List<ItemEntry> list = map.get(category);
        list.sort(new Comparator<ItemEntry>() {
            @Override
            public int compare(ItemEntry o1, ItemEntry o2) {
                return o1.editorName.compareToIgnoreCase(o2.editorName);
            }
        });

        for (ItemEntry item : list) {
            JButton btn = new JButton();
            String text = item.editorName;
            if (text.length() > maxItemTitleLength) {
                text = text.substring(0, maxItemTitleLength - 1) + "…";
            }
            btn.setText(text);
            if (text.equalsIgnoreCase(item.editorName)) {
                btn.setToolTipText("<html>" + item.longName + "<br/>&lt;" + item.id + "&gt;");
            } else {
                btn.setToolTipText(
                        "<html>" + item.editorName + "<br/>" + item.longName + "<br/>&lt;" + item.id + "&gt;");
            }

            btn.addActionListener(e -> {
                selectedItem = item;
                dialog.dispose();
            });
            panel.add(btn);
        }

        return panel;
    }

    public ItemPanel(DataModel model, String... category) {
        group = new ButtonGroup();

        buildMapping(model);

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        List<String> catFilter = Arrays.asList(category);

        List<String> cats = new ArrayList<String>();
        cats.addAll(map.keySet());
        cats.sort(null);
        for (String cat : cats) {
            ExpansionPanel panel = createCategoryPanel(cat);
            if (catFilter.size() == 0) {
                panel.collapse();
                add(panel);
            } else if (catFilter.contains(cat)) {
                add(panel);
            }
        }

        selectedItem = null;
    }

    public ItemEntry selectItem(Window owner) {
        selectedItem = null;
        dialog = new JDialog(owner, "Item Finder", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.add(new JScrollPane(this));
        dialog.pack();
        dialog.setSize(new Dimension(640, 640));
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
        return selectedItem;
    }
}