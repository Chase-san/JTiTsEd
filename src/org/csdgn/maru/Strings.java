/**
 * Copyright (c) 2015-2019 Robert Maupin
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
package org.csdgn.maru;

public final class Strings {
	public static String toTitleCase(String input) {
		String delimiters = ".,:;`()[]<>!?\"";
		StringBuilder titleCase = new StringBuilder(input.length());
		boolean nextTitleCase = true;

		for (char ch : input.toCharArray()) {
			if (Character.isSpaceChar(ch) || Character.isWhitespace(ch) || delimiters.indexOf(ch) >= 0) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				if (Character.isLetter(ch)) {
					ch = Character.toTitleCase(ch);
					nextTitleCase = false;
				}
			} else {
				ch = Character.toLowerCase(ch);
			}

			titleCase.append(ch);
		}

		return titleCase.toString();
	}
}
