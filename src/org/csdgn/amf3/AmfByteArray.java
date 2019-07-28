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

import java.util.Arrays;

/**
 * This class handles byte buffer objects associated in the AMF. Internally
 * makes use of an {@link java.util.ArrayDeque}. This class functions as a basic
 * byte stack.
 * 
 * @author Robert Maupin
 *
 */
public class AmfByteArray implements AmfValue {
	private byte[] data;
	private int size;

	/**
	 * Constructs a new byte array.
	 */
	public AmfByteArray() {
		data = new byte[8];
		size = 0;
	}

	/**
	 * Returns the current capacity of this AmfByteArray.
	 * 
	 * @return The capacity.
	 */
	public int capacity() {
		return data.length;
	}

	/**
	 * Resets the size of this byte array and clears data.
	 */
	public void clear() {
		size = 0;
	}
	
	@Override
	public boolean equals(AmfValue value) {
		if(value instanceof AmfByteArray) {
			AmfByteArray ba = (AmfByteArray) value;
			return Arrays.equals(ba.toArray(), toArray());
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
	 * Gets the internal data array of this byte array.
	 * @return the backing data byte array
	 */
	protected byte[] getBackingArray() {
		return data;
	}

	@Override
	public AmfType getType() {
		return AmfType.ByteArray;
	}

	/**
	 * Indicates if this byte array is empty.
	 * 
	 * @return true if the size of this byte array is zero, false otherwise.
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Removes and returns the last byte in this array and decrements the size
	 * by one.
	 * 
	 * Throws {@link IndexOutOfBoundsException} if the byte array is empty.
	 * 
	 * @return The byte popped from the array.
	 */
	public byte pop() {
		if(size == 0) {
			throw new IndexOutOfBoundsException("Cannot pop values from an empty array.");
		}
		return data[--size];
	}

	/**
	 * <p>
	 * Removes and returns the last <code>count</code> bytes in this array and
	 * decrements the byte array by this amount.
	 * </p>
	 * <p>
	 * This method is unlike multiple calls to {@link #pop()} as it will get the
	 * values in the order they are in the byte array, where as multiple calls
	 * to {@link #pop()} will return them in reverse order.
	 * </p>
	 * <p>
	 * Throws a {@link IndexOutOfBoundsException} if the <code>count</code> is
	 * larger than the size of this byte array.
	 * </p>
	 * 
	 * @param count
	 *            Number of bytes to remove.
	 * @return The bytes popped from the array.
	 */
	public byte[] pop(int count) {
		if(size < count) {
			throw new IndexOutOfBoundsException("Cannot pop more values from an array then are available.");
		}
		byte[] ret = Arrays.copyOfRange(data, size - count, size);
		size -= count;
		return ret;
	}

	/**
	 * <p>
	 * Removes the last <code>length</code> bytes in this arrayand stores them
	 * in <code>b</code> at <code>offset</code> and decrements the byte array by
	 * this amount.
	 * </p>
	 * <p>
	 * This method is unlike multiple calls to {@link #pop()} as it will get the
	 * values in the order they are in the byte array, where as multiple calls
	 * to {@link #pop()} will return them in reverse order.
	 * </p>
	 * <p>
	 * Throws a {@link IndexOutOfBoundsException} if the <code>length</code> is
	 * larger than the size of this byte array.
	 * </p>
	 * 
	 * @param b
	 *            The byte array to write to.
	 * @param offset
	 *            The offset in the array to start writing at.
	 * @param length
	 *            The number of bytes to pop.
	 */
	public void popTo(byte[] b, int offset, int length) {
		if(size < length) {
			throw new IndexOutOfBoundsException("Cannot pop more values from an array then are available.");
		}
		System.arraycopy(data, size - length, b, offset, length);
		size -= length;
	}

	/**
	 * Appends the given byte to the end of this byte array.
	 * 
	 * @param b
	 *            The byte to append.
	 */
	public void push(byte b) {
		data[size++] = b;

		if(size == data.length) {
			// resize and increase capacity by double
			data = Arrays.copyOf(data, data.length << 1);
		}
	}

	/**
	 * Appends the given bytes to the end of this byte array.
	 * 
	 * @param b
	 *            The bytes to append.
	 */
	public void push(byte[] b) {
		if(size + b.length >= data.length) {
			int nCap = (size + b.length) << 1;
			data = Arrays.copyOf(data, nCap);
		}
		System.arraycopy(b, 0, data, size, b.length);
		size += b.length;
	}

	/**
	 * <p>
	 * Appends <code>length</code> bytes from <code>b</code> starting at
	 * <code>offset</code> to the end of this byte array.
	 * </p>
	 * <p>
	 * Throws a {@link IndexOutOfBoundsException} if the <code>offset</code>
	 * plus the <code>length</code> is larger than the size of <code>b</code>.
	 * </p>
	 * 
	 * @param b
	 *            The array containing the bytes to append.
	 * @param offset
	 *            The starting offset in <code>b</code> to start reading from.
	 * @param length
	 *            The number of bytes from <code>b</code> to append.
	 * 
	 * 
	 */
	public void pushFrom(byte[] b, int offset, int length) {
		if(offset + length > b.length) {
			throw new IndexOutOfBoundsException("Offset and length exceeds the size of the source array.");
		}
		if(size + length >= data.length) {
			int nCap = (size + length) << 1;
			data = Arrays.copyOf(data, nCap);
		}
		System.arraycopy(b, offset, data, size, length);
		size += b.length;
	}

	/**
	 * Returns the current size of the data in this AmfByteArray.
	 * 
	 * @return The size.
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Gets the data of this AmfByteArray as a standard java byte array.
	 * 
	 * @return The byte array.
	 */
	public byte[] toArray() {
		return Arrays.copyOf(data, size);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("ByteArray{");
		for(int i = 0; i < size; ++i) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append(String.format("%02x", data[i]));
		}
		buf.append("}");
		return buf.toString();
	}

}