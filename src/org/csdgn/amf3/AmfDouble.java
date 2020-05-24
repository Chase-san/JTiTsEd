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
 * Associated with the AMF double type. This class simply stores a Java Double
 * internally.
 * 
 * @author Robert Maupin
 */
public class AmfDouble extends AmfPrimitive<Double> {
	/**
	 * Defines an AmfInteger with a value of 0.0.
	 */
	public AmfDouble() {
		super(0.0);
	}

	/**
	 * Defines an AmfDouble with the specified value.
	 * 
	 * @param value
	 *            The double value.
	 */
	public AmfDouble(double value) {
		super(value);
	}

	@Override
	public AmfType getType() {
		return AmfType.Double;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Double[");
		buf.append(getValue());
		buf.append("]");
		return buf.toString();
	}
}