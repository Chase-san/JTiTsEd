package org.csdgn.maru.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;


public class ToolTipRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -1630964615761562837L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
         JComponent component = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
         String tip = null;
         if (value instanceof ToolTipProvider) {
             ToolTipProvider ttp = (ToolTipProvider) value;
             tip = ttp.getToolTip();
         }
         list.setToolTipText(tip);
         return component;
    }
}