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
