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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Defines custom data capable of being stored in the Action Message Format
 * (AMF).
 * 
 * @author Robert Maupin
 */
public interface Externalizable {
	/**
	 * Reads the data for this externalizable from the given data input source.
	 * 
	 * @param in
	 *            data input source
	 * @throws IOException
	 *             thrown when an I/O error occurs.
	 * @throws UnexpectedDataException
	 *             thrown when there is a read failure due to unexpected data.
	 */
	void readExternal(DataInput in) throws IOException, UnexpectedDataException;

	/**
	 * Writes the data for this externalizable to the given data output target.
	 * 
	 * @param out
	 *            data output target
	 * @throws IOException
	 *             thrown when an I/O error occurs.
	 */
	void writeExternal(DataOutput out) throws IOException;
}
