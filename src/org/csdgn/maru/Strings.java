package org.csdgn.maru;

public final class Strings {
	public static String toTitleCase(String input) {
		StringBuilder titleCase = new StringBuilder(input.length());
		boolean nextTitleCase = true;

		for (char ch : input.toCharArray()) {
			if (Character.isSpaceChar(ch) || Character.isWhitespace(ch) || ".,:;`()[]<>!?\"".indexOf(ch) >= 0) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				ch = Character.toTitleCase(ch);
				nextTitleCase = false;
			} else {
				ch = Character.toLowerCase(ch);
			}

			titleCase.append(ch);
		}

		return titleCase.toString();
	}
}
