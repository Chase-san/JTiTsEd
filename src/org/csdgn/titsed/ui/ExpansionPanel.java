/**
 * Copyright (c) 2018 Robert Maupin
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
 */package org.csdgn.titsed.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Robert Maupin
 */
public class ExpansionPanel extends JComponent {
	private static final long serialVersionUID = -4684336379404585379L;

	private JButton button;
	private JLabel buttonIcon;
	private JLabel buttonText;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public ExpansionPanel() {
		super.setLayout(new BorderLayout(0, 0));
		
		button = new JButton();
		button.setFocusPainted(false);
		buttonIcon = new JLabel();
		buttonText = new JLabel("", SwingConstants.CENTER);
		
		button.setLayout(new BorderLayout());
		button.add(buttonIcon, BorderLayout.WEST);
		button.add(buttonText, BorderLayout.CENTER);
		
		super.add(button, BorderLayout.NORTH);
		button.addActionListener(e -> {
			if(isExpanded()) {
				collapse();
			} else {
				expand();
			}
		});
		
		panel = new JPanel();
		super.add(panel, BorderLayout.CENTER);
		
		expand();
	}

	@Override
	public Component add(Component component) {
		return panel.add(component);
	}
	
	public JButton getButton() {
		return button;
	}
	
	public LayoutManager getLayout() {
		return panel.getLayout();
	}
	
	public void setLayout(LayoutManager mgr) {
		panel.setLayout(mgr);
	}
	
	public void setTitle(String text) {
		buttonText.setText(text);
	}
	
	public boolean isExpanded() {
		return panel.isVisible();
	}
	
	public void collapse() {
		panel.setVisible(false);
		buttonIcon.setIcon(UIStrings.getIcon("UI.EXPCloseIcon"));
	}
	
	public void expand() {
		panel.setVisible(true);
		buttonIcon.setIcon(UIStrings.getIcon("UI.EXPOpenIcon"));
	}
}
