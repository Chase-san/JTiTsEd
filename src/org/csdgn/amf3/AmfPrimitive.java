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
 * Defines a single value that has a simple format. Such as a number or string.
 * 
 * @author Robert Maupin
 */
public abstract class AmfPrimitive<T> implements AmfValue {
	private T value;

	/**
	 * Creates a new primitive with the specified value.
	 * 
	 * @param value
	 *            The value of this primitive.
	 */
	protected AmfPrimitive(T value) {
		setValue(value);
	}

	/**
	 * Determines if the given object is an AmfValue and equals this value.
	 * Makes use of {@link #equals(AmfValue)} to determine equality.
	 * 
	 * @see #equals(AmfValue)
	 */
	public boolean equals(AmfValue val) {
		if(val instanceof AmfPrimitive && val.getType() == getType()) {
			return getValue() == ((AmfPrimitive<?>) val).getValue();
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
	
	/**
	 * Gets the value of this primitive.
	 * 
	 * @return The primitives value.
	 */
	public T getValue() {
		return value;
	}
	
	/**
	 * Sets the value of this primitive.
	 * 
	 * @param value
	 *            The value to set.
	 * @throws UnsupportedOperationException
	 *             If the value is null, AmfPrimitives do not support null
	 *             values.
	 */
	public void setValue(T value) {
		if(value == null) {
			throw new UnsupportedOperationException("A primitive value cannot be null.");
		}
		this.value = value;
	}
}
