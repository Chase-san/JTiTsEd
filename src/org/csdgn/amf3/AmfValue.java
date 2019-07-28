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
 * This class is the origin point for all Action Message Format (AMF) value
 * types.
 * 
 * @author Robert Maupin
 *
 */
public interface AmfValue {
	/**
	 * This determines if the given AmfValue equals another AmfValue exactly.
	 * The identities of the two objects do not need to match. This method is
	 * used in building the string and object reference tables.
	 * 
	 * @param value
	 *            The value to check this values equality against.
	 * @return true if this and the supplied value are equal, false otherwise.
	 */
	public boolean equals(AmfValue value);

	/**
	 * This is used to get the value type of the AMF value type.
	 * 
	 * @return the type id of the value type.
	 */
	public AmfType getType();
}
