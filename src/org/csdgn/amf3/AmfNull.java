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
 * Associated with the AMF null type.
 * @author Robert Maupin
 *
 */
public class AmfNull implements AmfValue {
	@Override
	public boolean equals(AmfValue value) {
		if(value.getType() == AmfType.Null) {
			return true;
		}
		return false;
	}

	/**
	 * Determines if the given object is an AmfValue and equals this value.
	 * Makes use of {@link #equals(AmfValue)} to determine equality.
	 * 
	 * @see #equals(AmfValue)
	 */
	public boolean equals(Object obj) {
		if(obj instanceof AmfValue) {
			return equals((AmfValue)obj);
		}
		return false;
	}
	
	@Override
	public AmfType getType() {
		return AmfType.Null;
	}
	
	@Override
	public String toString() {
		return "Null[]";
	}
}