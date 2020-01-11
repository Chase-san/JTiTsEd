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

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class UIStrings {

	private static final String BUNDLE_NAME = "org.csdgn.titsed.ui.strings";
	private static ArrayList<ResourceBundle> BUNDLES = new ArrayList<ResourceBundle>();
	private static Map<String, ImageIcon> ICON_CACHE = new HashMap<String, ImageIcon>();

	static {
		addBundle(BUNDLE_NAME);
	}

	protected static void addBundle(String name) {
		try {
			BUNDLES.add(0, ResourceBundle.getBundle(name));
		} catch (MissingResourceException e) {
		}
	}

	public static URL getResource(String key) {
		String res = getString(key);
		return UIStrings.class.getResource(res);
	}

	public static String getString(String key) {
		for (ResourceBundle bundle : BUNDLES) {
			if (bundle.containsKey(key)) {
				return bundle.getString(key);
			}
		}
		return '!' + key + '!';
	}

	public static List<Image> getImageList(String ... keys) {
		List<Image> list = new ArrayList<Image>();
		for(String key : keys) {
			list.add(getImage(key));
		}
		return list;
	}

	public static Image getImage(String key) {
		ImageIcon icon = ICON_CACHE.get(key);
		if (icon == null) {
			URL res = getResource(key);
			if (res != null) {
				icon = new ImageIcon(res);
				ICON_CACHE.put(key, icon);
			}
		}
		if (icon != null) {
			return icon.getImage();
		}
		return null;
	}

	public static ImageIcon getIcon(String key) {
		ImageIcon icon = ICON_CACHE.get(key);
		if (icon == null) {
			URL res = getResource(key);
			if (res != null) {
				icon = new ImageIcon(res);
				ICON_CACHE.put(key, icon);
			}
		}
		return icon;
	}
}
