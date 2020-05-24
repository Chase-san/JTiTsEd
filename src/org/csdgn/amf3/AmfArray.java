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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Associated with the AMF true and false types. Unlike a standard array in
 * Java, an AmfArray has both a Dense (List like) portion and an Associative
 * (Map like) portion. This class handles both of these.
 * 
 * @author Robert Maupin
 *
 */
public class AmfArray implements AmfValue {
	private Map<String, AmfValue> associative;
	private List<AmfValue> dense;

	/**
	 * Constructs a new AmfArray.
	 */
	public AmfArray() {
		dense = new ArrayList<AmfValue>();
		associative = new LinkedHashMap<String, AmfValue>();
	}

	/**
	 * Adds the given value to the dense portion of this array.
	 * 
	 * @param value
	 *            Value.
	 */
	public void add(AmfValue value) {
		dense.add(value);
	}

	/**
	 * Removes all elements from both the dense and associative parts of this
	 * AmfArray.
	 */
	public void clear() {
		dense.clear();
		associative.clear();
	}
	
	@Override
	public boolean equals(AmfValue value) {
		if(value instanceof AmfArray) {
			AmfArray arr = (AmfArray) value;
			if(arr.dense.size() == dense.size() && arr.associative.size() == associative.size()) {
				return arr.dense.equals(dense) && arr.associative.equals(associative);
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

	/**
	 * Returns the element at the specified position of the dense part of this
	 * AmfArray.
	 * 
	 * @param index
	 *            index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, where index is less than 0 or index greater than or equal to
	 *             {@link #getDenseSize()}.
	 */
	public AmfValue get(int index) {
		return dense.get(index);
	}

	/**
	 * Returns the value from the associative part of this AmfArray to which the
	 * specified key is mapped, or null if it contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if the
	 *         associative part contains no mapping for the key
	 */
	public AmfValue get(String key) {
		return associative.get(key);
	}

	/**
	 * Returns map backing the associative part of this AmfArray.
	 * 
	 * @return The map backing associative part.
	 */
	public Map<String, AmfValue> getAssociative() {
		return associative;
	}

	/**
	 * Returns the size of the associative part of this AmfArray.
	 * 
	 * @return the associative size
	 */
	public int getAssociativeSize() {
		return associative.size();
	}

	/**
	 * Returns the list backing the dense part of this AmfArray.
	 * 
	 * @return The list backing the dense part.
	 */
	public List<AmfValue> getDense() {
		return dense;
	}

	/**
	 * Returns the size of the dense part of this AmfArray.
	 * 
	 * @return The dense size.
	 */
	public int getDenseSize() {
		return dense.size();
	}

	@Override
	public AmfType getType() {
		return AmfType.Array;
	}

	/**
	 * Returns the set of keys associated with the associative part of this
	 * AmfArray.
	 * 
	 * @return The associative keys.
	 */
	public Set<String> keySet() {
		return associative.keySet();
	}

	/**
	 * Associates the specified value with the specified key in the associated
	 * part of this AmfArray. If there was previously a mapping for the key, the
	 * old value is replaced by the specified value.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * 
	 * @param value
	 *            key with which the specified value is to be associated
	 * 
	 * @return the previous value associated with key, or null if there was no
	 *         mapping for the key.
	 */
	public AmfValue put(String key, AmfValue value) {
		return associative.put(key, value);
	}

	/**
	 * Removes the element at the specified index from the dense part of this
	 * AmfArray.
	 * 
	 * @param index
	 *            the index of the element to be removed
	 * @return the element previously at the specified position
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range where index is less than 0 or index is greater than or equal to 
	 *             {@link #getDenseSize()}.
	 */
	public AmfValue remove(int index) {
		return dense.remove(index);
	}

	/**
	 * Removes the mapping for a key from the associated part of this AmfArray
	 * if it is present. More formally, if this it contains a mapping from key k
	 * to value v such that (key==null ? k==null : key.equals(k)), that mapping
	 * is removed.
	 * 
	 * @param key
	 *            key whose mapping is to be removed from the map
	 * @return the previous value associated with key, or null if there was no
	 *         mapping for key.
	 */
	public AmfValue remove(String key) {
		return associative.remove(key);
	}

	/**
	 * Returns the size of the combined dense and associative parts of this
	 * AmfArray.
	 * 
	 * @return Size of this AmfArray
	 */
	public int size() {
		return dense.size() + associative.size();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Array{");
		boolean first = true;
		//associative first
		for(String key : associative.keySet()) {
			if(!first) {
				buf.append(",");
			}
			first = false;
			buf.append(key);
			buf.append("=");
			buf.append(associative.get(key));
		}
		//dense
		for(AmfValue value : dense) {
			if(!first) {
				buf.append(",");
			}
			first = false;
			buf.append(value);
		}
		
		buf.append("}");
		return buf.toString();
	}
}