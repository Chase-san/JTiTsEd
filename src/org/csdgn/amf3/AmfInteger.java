/**
 * Copyright (c) 2017-2020 Robert Maupin
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
 * Associated with the AMF integer type. This class simply stores a Java Integer
 * internally.
 * 
 * @author Robert Maupin
 */
public class AmfInteger extends AmfPrimitive<Integer> {
	/**
	 * Defines an AmfInteger with a value of zero.
	 */
	public AmfInteger() {
		super(0);
	}

	/**
	 * Defines an AmfInteger with the specified value.
	 * 
	 * @param value
	 *            The integer value.
	 */
	public AmfInteger(Integer value) {
		super(value);
	}

	@Override
	public AmfType getType() {
		return AmfType.Integer;
	}

	/**
	 * Gets the unsigned value of this integer.
	 * 
	 * @return The unsigned integer value.
	 */
	public long getUnsignedValue() {
		return getValue() & 0xFFFFFFFFL;
	}

	/**
	 * Sets the value of this integer from an unsigned value.
	 * 
	 * @param value
	 *            The unsigned value. All values will be truncated by ANDing
	 *            with the value 0xFFFFFFFF.
	 */
	public void setUnsignedValue(long value) {
		setValue((int) (value & 0xFFFFFFFFL));
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Int[");
		buf.append(getValue());
		buf.append("]");
		return buf.toString();
	}
}