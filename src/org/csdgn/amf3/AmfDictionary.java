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

import java.util.LinkedHashMap;

/**
 * <p>
 * Associated with the AMF dictionary type. A dictionary is a map of key-value
 * pairs, where both the key and value can be any AmfValue. Keys are matched
 * based on identity using a strict-equality comparison by means of
 * {@link #equals(AmfValue)}.
 * </p>
 * 
 * <p>
 * An ActionScript dictionary can be set to only maintain a weak reference to
 * keys on construction. A weak references allow objects to be garbage collected
 * if the only reference to them is from the Dictionary. An exception to this is
 * mentioned in the AS3 Language Reference in that String keys are always
 * strongly referenced.
 * </p>
 * 
 * <p>
 * Despite this option however, this class does not make use of weak keys except
 * as a storage option. All keys and values within this AmfDictionary will be
 * retained regardless of external references.
 * </p>
 * 
 * @author Robert Maupin
 *
 */
public class AmfDictionary extends LinkedHashMap<AmfValue, AmfValue> implements AmfValue {
	private static final long serialVersionUID = -8860197036790566309L;
	
	private boolean weakKeys;

	/**
	 * Defines a AmfDictionary that has strong keys.
	 */
	public AmfDictionary() {
		setWeakKeys(false);
	}

	/**
	 * Defines a AmfDictionary that has the specified weak keys value.
	 * 
	 * @param weakKeys
	 *            true if it should be stored as having weak keys, false
	 *            otherwise.
	 */
	public AmfDictionary(boolean weakKeys) {
		this();
		this.setWeakKeys(weakKeys);
	}
	
	@Override
	public boolean equals(AmfValue value) {
		if(value instanceof AmfDictionary) {
			AmfDictionary dict = (AmfDictionary) value;
			if(dict.hasWeakKeys() == hasWeakKeys()) {
				return super.equals(dict);
			}
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
		return AmfType.Dictionary;
	}

	/**
	 * Returns if this dictionary will be stored as having weak keys.
	 * 
	 * @return true if having weak keys, false otherwise.
	 */
	public boolean hasWeakKeys() {
		return weakKeys;
	}

	/**
	 * Sets if this map should be stored as having weak keys or not.
	 * 
	 * @param weakKeys
	 *            True if the map should be stored as having weak keys, false
	 *            otherwise.
	 */
	public void setWeakKeys(boolean weakKeys) {
		this.weakKeys = weakKeys;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Dictionary{");
		boolean first = true;
		for(AmfValue key : keySet()) {
			if(!first) {
				buf.append(",");
			}
			first = false;
			buf.append(key);
			buf.append("=");
			buf.append(get(key));
		}
		//don't even try to print custom data
		buf.append("}");
		return buf.toString();
	}
}