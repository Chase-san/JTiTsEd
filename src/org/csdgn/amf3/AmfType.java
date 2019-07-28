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
 * List of types associated with the the Amf Objects. These are mostly used for
 * storage.
 * 
 * @author Robert Maupin
 *
 */
public enum AmfType {
	/** @see AmfArray */
	Array(0x09),
	/** @see AmfByteArray */
	ByteArray(0x0C),
	/** @see AmfDate */
	Date(0x08),
	/** @see AmfDictionary */
	Dictionary(0x11),
	/** @see AmfDouble */
	Double(0x05),
	/** @see AmfBoolean */
	False(0x02),
	/** @see AmfInteger */
	Integer(0x04),
	/** @see AmfNull */
	Null(0x01),
	/** @see AmfObject */
	Object(0x0A),
	/** @see AmfString */
	String(0x06),
	/** @see AmfBoolean */
	True(0x03),
	/** @see AmfUndefined */
	Undefined(0x00),
	/** @see AmfVector.Double */
	VectorDouble(0x0F),
	/** @see AmfVector.Generic */
	VectorGeneric(0x10),
	/** @see AmfVector.Integer */
	VectorInt(0x0D),
	/** @see AmfVector.UnsignedInteger */
	VectorUInt(0x0E),
	/** @see AmfXml */
	Xml(0x0B),
	/** @see AmfXml */
	XmlDoc(0x07);

	/**
	 * Gets the AmfType associated with the provided type marker id.
	 * 
	 * @param id
	 *            The type marker identifier.
	 * @return The AmfType associated with the id, or null if there is no
	 *         associated type.
	 */
	public static AmfType get(int id) {
		for(AmfType type : values()) {
			if(type.id == id) {
				return type;
			}
		}
		return null;
	}

	/**
	 * The id of the type marker for the AmfType.
	 */
	public final int id;

	private AmfType(int id) {
		this.id = id;
	}
}
