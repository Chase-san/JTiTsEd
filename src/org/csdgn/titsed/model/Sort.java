/**
 * Copyright (c) 2020 Robert Maupin
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
package org.csdgn.titsed.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum Sort {
	Key, KeyNone, Natural, Value, ValueNone;

	public static Sort fromString(String sortName) {
		for (Sort sort : Sort.values()) {
			if (sort.name().equalsIgnoreCase(sortName)) {
				return sort;
			}
		}
		return null;
	}

	public static List<String> sortIntegerKeySet(Map<String, String> enumData, Sort sort) {
		List<String> retKeys = new ArrayList<String>();

		retKeys.addAll(enumData.keySet());

		if (Sort.Key == sort) {
			retKeys.sort((a, b) -> {
				int aN = Integer.parseInt(a);
				int bN = Integer.parseInt(b);
				return Integer.compare(aN, bN);
			});
		} else if (Sort.KeyNone == sort) {
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
		} else if (Sort.Value == sort) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if (Sort.ValueNone == sort) {
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

	public static List<String> sortStringKeySet(Map<String, String> enumData, Sort sort) {
		List<String> retKeys = new ArrayList<String>();

		retKeys.addAll(enumData.keySet());

		if (Sort.Key == sort) {
			retKeys.sort((a, b) -> {
				return a.compareTo(b);
			});
		} else if (Sort.KeyNone == sort) {
			retKeys.sort((a, b) -> {
				if ("none".equalsIgnoreCase(a)) {
					a = "";
				}
				if ("none".equalsIgnoreCase(b)) {
					b = "";
				}
				return a.compareTo(b);
			});
		} else if (Sort.Value == sort) {
			retKeys.sort((a, b) -> {
				String aV = enumData.get(a);
				String bV = enumData.get(b);
				return aV.compareTo(bV);
			});
		} else if (Sort.ValueNone == sort) {
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

}
