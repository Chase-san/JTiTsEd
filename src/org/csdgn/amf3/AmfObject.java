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

/**
 * Associated with the AMF object type. This handles ActionScript Objects and
 * custom user classes. Traits are used to describe the attributes of a custom
 * user class.
 * 
 * @author Robert Maupin
 *
 */
public class AmfObject implements AmfValue {
	private Externalizable customData;
	private Map<String, AmfValue> dynamicMap;
	private boolean isDynamic;
	private boolean isExternalizable;
	private Map<String, AmfValue> sealedMap;
	private String traitName;

	/**
	 * Constructs a AmfObject. By default, the object is not dynamic, is not
	 * externalizable (and the custom data is null) and has no trait name, and
	 * has an empty sealed and dynamic map.
	 */
	public AmfObject() {
		isDynamic = false;
		isExternalizable = false;
		traitName = "";
		sealedMap = new LinkedHashMap<String, AmfValue>();
		dynamicMap = new LinkedHashMap<String, AmfValue>();
		customData = null;
	}
	
	@Override
	public boolean equals(AmfValue value) {
		if(value instanceof AmfObject) {
			AmfObject obj = (AmfObject) value;
			if(obj.isDynamic != isDynamic && obj.isExternalizable != isExternalizable && obj.customData != customData
					&& !traitName.equals(traitName)) {
				return false;
			}
			return obj.sealedMap.equals(sealedMap) && obj.dynamicMap.equals(dynamicMap);
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
	 * Gets the dynamic map associated with this object. If the object is not
	 * dynamic, the map will be empty. Unless {@link #isDynamic()} is set
	 * however the values stored in this map will not be saved into an AMF
	 * object.
	 * 
	 * @return The map associated with the dynamic portion of this object.
	 */
	public Map<String, AmfValue> getDynamicMap() {
		return dynamicMap;
	}

	/**
	 * Gets the {@link Externalizable} associated with this AmfObject.
	 * 
	 * @return The externalizable object associated with this AmfObject.
	 */
	public Externalizable getExternalizableObject() {
		return this.customData;
	}

	/**
	 * Gets the sealed map associated with this object. Adding to or removing
	 * from this map will likewise add or remove properties from the Objects
	 * Trait.
	 * 
	 * @return The map associated with the sealed portion of this object.
	 */
	public Map<String, AmfValue> getSealedMap() {
		return sealedMap;
	}

	/**
	 * The trait generated is backed by this object and changes in this object
	 * will be reflected in the trait.
	 * 
	 * @return the trait associated with this map.
	 */
	public Trait getTrait() {
		return new Trait() {
			public boolean equals(Object obj) {
				if(!(obj instanceof Trait)) {
					return false;
				}
				Trait trait = (Trait)obj;
				
				if(!traitName.equals(trait.getName())) {
					return false;
				}
				
				if(isDynamic != trait.isDynamic()) {
					return false;
				}
				
				if(isExternalizable != trait.isExternalizable()) {
					return false;
				}
				
				return getProperties().equals(trait.getProperties());
			}

			@Override
			public String getName() {
				return traitName;
			}

			@Override
			public List<String> getProperties() {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(sealedMap.keySet());
				return list;
			}

			@Override
			public boolean isDynamic() {
				return isDynamic;
			}
			
			@Override
			public boolean isExternalizable() {
				return isExternalizable;
			}
		};
	}

	/**
	 * Gets the trait name associated with this object.
	 * 
	 * @return The trait name.
	 */
	public String getTraitName() {
		return traitName;
	}

	@Override
	public AmfType getType() {
		return AmfType.Object;
	}

	/**
	 * This is a convenience method that is exactly the same as returned by
	 * {@link Trait#isDynamic()} method returned in the {@link #getTrait()}
	 * method of this class. It indicates if this object is considered dynamic
	 * or not, which determines if the dynamic portion of this AmfObject will be
	 * stored.
	 * 
	 * @return If the object is dynamic.
	 */
	public boolean isDynamic() {
		return isDynamic;
	}

	/**
	 * This is a convenience method that is exactly the same as returned by
	 * {@link Trait#isExternalizable()} method returned in the
	 * {@link #getTrait()} method of this class. It indicates if this object is
	 * considered externalizable or not, which determines if the externalizable
	 * object of this AmfObject will be stored.
	 * 
	 * @return If the object is dynamic.
	 */
	public boolean isExternalizable() {
		return isExternalizable;
	}

	/**
	 * Determines if this object is dynamic and if the dynamic section will be
	 * stored on writing.
	 * 
	 * @param isDynamic true if the object has a dynamic section, false otherwise.
	 */
	public void setDynamic(boolean isDynamic) {
		this.isDynamic = isDynamic;
	}

	/**
	 * Determines if this object has externalizable data and if the
	 * externalizable data will be written when writing to an AMF. If the
	 * ExternalizableObject is null this value will be treated as false when
	 * writing, regardless of its actual value.
	 * 
	 * @param isExternalizable
	 *            true to write externalizable data, false otherwise
	 */
	public void setExternalizable(boolean isExternalizable) {
		this.isExternalizable = isExternalizable;
	}

	/**
	 * Sets the Externalizable that this AmfObject will store if
	 * {@link #isExternalizable()} is set.
	 * 
	 * @param ext
	 *            The Externalizable object.
	 */
	public void setExternalizableObject(Externalizable ext) {
		this.customData = ext;
	}

	/**
	 * Sets the name of the trait that will be returned from
	 * {@link Trait#getName()} that is gotten from this classes
	 * {@link #getTrait()} method.
	 * 
	 * @param traitName
	 *            The name of the trait.
	 */
	public void setTraitName(String traitName) {
		if(traitName == null) {
			throw new IllegalArgumentException("Trait Name cannot be null.");
		}
		this.traitName = traitName;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Object:");
		buf.append(traitName);
		buf.append("{");
		boolean first = true;
		//sealed
		for(String key : sealedMap.keySet()) {
			if(!first) {
				buf.append(",");
			}
			first = false;
			buf.append(key);
			buf.append("=");
			buf.append(sealedMap.get(key));
		}
		//dynamic
		for(String key : dynamicMap.keySet()) {
			if(!first) {
				buf.append(",");
			}
			first = false;
			buf.append(key);
			buf.append("=");
			buf.append(dynamicMap.get(key));
		}
		//don't even try to print custom data
		buf.append("}");
		return buf.toString();
	}
}
