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
package org.csdgn.amf3;

/**
 * Associated with the AMF true and false types. This class simply stores a Java
 * Boolean internally.
 * 
 * @author Robert Maupin
 */
public class AmfBoolean extends AmfPrimitive<Boolean> {

	/**
	 * Constructs this AmfBoolean with the default value of false.
	 */
	public AmfBoolean() {
		super(false);
	}

	/**
	 * Constructs this AmfBoolean with the specified value.
	 * 
	 * @param value
	 *            The value.
	 */
	public AmfBoolean(boolean value) {
		super(value);
	}

	@Override
	public AmfType getType() {
		if(getValue()) {
			// TRUE
			return AmfType.True;
		}
		// FALSE
		return AmfType.False;
	}
	
	@Override
	public String toString() {
		if(getValue()) {
			return "Boolean[True]";
		}
		return "Boolean[False]";
	}
}
