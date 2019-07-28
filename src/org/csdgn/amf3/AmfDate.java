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

import java.time.Instant;

/**
 * This class handles date objects associated in the AMF. Makes use of the newer
 * Instant class instead of the legacy Date class.
 * 
 * @author Robert Maupin
 */
public class AmfDate extends AmfDouble {
	/**
	 * Creates a AmfDate at the epoch of 1970-01-01T00:00:00Z.
	 */
	public AmfDate() {
		super(0.0);
	}

	/**
	 * Creates an AmfDate with the given double value, which will be truncated
	 * to determine the number of milliseconds since the epoch of
	 * 1970-01-01T00:00:00Z.
	 * 
	 * @param value the amf date value
	 */
	public AmfDate(double value) {
		super(value);
	}

	/**
	 * Gets the date as an Instant.
	 * 
	 * @return An Instant of this AmfDate.
	 */
	public Instant getInstant() {
		return Instant.ofEpochMilli((long) (double) getValue());
	}

	@Override
	public AmfType getType() {
		return AmfType.Date;
	}

	/**
	 * Sets this AmfDate by means of the specified Instant.
	 * 
	 * @param instant
	 *            The instant to use to set this value. Cannot be null.
	 * @throws UnsupportedOperationException
	 *             If the provided value is null.
	 */
	public void setInstant(Instant instant) {
		if(instant == null) {
			throw new UnsupportedOperationException("Instant cannot be null.");
		}
		setValue((double) instant.toEpochMilli());
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Date[");
		buf.append(getInstant().toString());
		buf.append("]");
		return buf.toString();
	}
}
