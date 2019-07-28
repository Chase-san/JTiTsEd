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
 * This class handles XML objects associated in the AMF. The XmlDocument option
 * of this is references an older and less capable version of the XML format
 * that AMF supports, which included E4X support. This class simply treats both
 * of these as a string.
 * 
 * @author Robert Maupin
 */
public class AmfXml extends AmfString {
	private boolean isXmlDocument;

	/**
	 * Constructs this AmfXml as a standard AMF XML element. Meaning the
	 * isXmlDocument will default to false for these.
	 */
	public AmfXml() {
		this.isXmlDocument = false;
	}

	/**
	 * Constructs this AmfXml as a standard AMF XML element with the given
	 * option determining if this should use the older XmlDocument AMF type.
	 * 
	 * @param isXmlDocument
	 *            true if XmlDocument, false otherwise
	 */
	public AmfXml(boolean isXmlDocument) {
		this.isXmlDocument = isXmlDocument;
	}

	@Override
	public boolean equals(AmfValue value) {
		if(value instanceof AmfXml && value.getType() == getType()) {
			AmfXml xml = (AmfXml) value;
			return xml.getValue().equals(getValue());
		}
		return false;
	}

	@Override
	public AmfType getType() {
		if(isXmlDocument) {
			return AmfType.XmlDoc;
		}
		return AmfType.Xml;
	}

	/**
	 * Indicates if this is the older XmlDocument AMF type.
	 * 
	 * @return true if XmlDocument, false otherwise
	 */
	public boolean isXmlDocument() {
		return isXmlDocument;
	}

	/**
	 * Sets if this AmfXml object should be stored as the older XmlDocument or
	 * not.
	 * 
	 * @param isXmlDocument
	 *            true if it should be stored as an XmlDocument, false
	 *            otherwise.
	 */
	public void setXmlDocument(boolean isXmlDocument) {
		this.isXmlDocument = isXmlDocument;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if(isXmlDocument) {
			buf.append("XMLDoc[");
		} else {
			buf.append("XML[");
		}
		buf.append(getValue());
		buf.append("]");
		return buf.toString();
	}
}