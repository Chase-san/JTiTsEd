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
package org.csdgn.titsed.model;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.csdgn.amf3.AmfArray;
import org.csdgn.amf3.AmfBoolean;
import org.csdgn.amf3.AmfDouble;
import org.csdgn.amf3.AmfFile;
import org.csdgn.amf3.AmfIO;
import org.csdgn.amf3.AmfInteger;
import org.csdgn.amf3.AmfString;
import org.csdgn.amf3.AmfType;
import org.csdgn.amf3.AmfUtils;
import org.csdgn.amf3.AmfValue;

/**
 * Save model for interfacing with the Amf Data.
 * 
 * @author Robert Maupin
 */
public class SaveModel {
	/**
	 * Short method for getting only the amf file name and nothing else.
	 * 
	 * @param file
	 * @return
	 */
	public static final String getSaveInfo(File file) {
		try {
			String name = "???";
			int days = 0;
			int hours = 0;
			int minutes = 0;

			AmfFile amfFile = AmfIO.readFile(file);
			AmfValue raw = AmfUtils.resolve(amfFile, "saveName");
			if (raw.getType() == AmfType.String) {
				name = ((AmfString) raw).getValue();
			}
			raw = AmfUtils.resolve(amfFile, "daysPassed");
			if (raw.getType() == AmfType.Integer) {
				days = ((AmfInteger) raw).getValue();
			}
			raw = AmfUtils.resolve(amfFile, "currentHours");
			if (raw.getType() == AmfType.Integer) {
				hours = ((AmfInteger) raw).getValue();
			}
			raw = AmfUtils.resolve(amfFile, "currentMinutes");
			if (raw.getType() == AmfType.Integer) {
				minutes = ((AmfInteger) raw).getValue();
			}

			return String.format("%s (D%d %02d:%02d)", name, days, hours, minutes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "??? (D? ??:??)";
	}

	public final boolean isFile;
	public final AmfFile srcFile;
	public final AmfValue srcValue;

	/**
	 * Constructs a save model from the given AmfFile.
	 * 
	 * @param file the file to use for the model
	 */
	public SaveModel(AmfFile file) {
		this.srcFile = file;
		this.srcValue = null;
		this.isFile = true;
	}

	/**
	 * Constructs a save model from the given AmfValue.
	 * 
	 * @param value the value to use for the model
	 */
	public SaveModel(AmfValue value) {
		this.srcFile = null;
		this.srcValue = value;
		this.isFile = false;
	}

	/**
	 * Gets the specific value from the loaded model (regardless of type).
	 * 
	 * @param ident Identity to resolve.
	 * @return the amf value or null if it did not exist.
	 */
	public AmfValue find(String ident) {
		if (isFile) {
			return AmfUtils.resolve(srcFile, ident);
		}
		return AmfUtils.resolve(srcValue, ident);
	}

	public Integer getInteger(String ident) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return null;
		}
		if (raw.getType() == AmfType.Double) {
			Double value = ((AmfDouble) raw).getValue();
			return value.intValue();
		} else if (raw.getType() == AmfType.Integer) {
			Integer value = ((AmfInteger) raw).getValue();
			return value.intValue();
		}
		return null;
	}

	public void setInteger(String ident, int value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.Double) {
			((AmfDouble) raw).setValue((double) value);
		} else if (raw.getType() == AmfType.Integer) {
			((AmfInteger) raw).setValue(value);
		}
	}

	public Double getDecimal(String ident) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return null;
		}
		if (raw.getType() == AmfType.Double) {
			Double value = ((AmfDouble) raw).getValue();
			return value.doubleValue();
		} else if (raw.getType() == AmfType.Integer) {
			Integer value = ((AmfInteger) raw).getValue();
			return value.doubleValue();
		}
		return null;
	}

	public void setDecimal(String ident, double value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.Double) {
			((AmfDouble) raw).setValue(value);
		} else if (raw.getType() == AmfType.Integer) {
			((AmfInteger) raw).setValue((int) value);
		}
	}

	public boolean getBoolean(String ident) {
		AmfValue raw = find(ident);
		if (raw.getType() == AmfType.True || raw.getType() == AmfType.False) {
			return ((AmfBoolean) raw).getValue();
		}
		return false;
	}

	public void setBoolean(String ident, boolean value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.True || raw.getType() == AmfType.False) {
			((AmfBoolean) raw).setValue(value);
		}
	}

	public String getString(String ident) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return null;
		}
		if (raw.getType() == AmfType.String) {
			return ((AmfString) raw).getValue();
		}
		return "";
	}

	public void setString(String ident, String value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.String) {
			((AmfString) raw).setValue(value);
		}
	}

	public Set<Integer> getFlags(String ident) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return Collections.emptySet();
		}
		// should be an array of ints
		// since these are flags, we store them in a set
		if (raw.getType() == AmfType.Array) {
			Set<Integer> data = new HashSet<Integer>();
			for (AmfValue value : ((AmfArray) raw).getDense()) {
				if (value instanceof AmfInteger) {
					data.add(((AmfInteger) value).getValue());
				} else {
					return Collections.emptySet();
				}
			}
			return data;
		}
		return Collections.emptySet();
	}

	public void addFlag(String ident, Integer value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.Array) {
			AmfArray arr = (AmfArray) raw;
			arr.add(new AmfInteger(value));
		}
	}

	public void removeFlag(String ident, Integer value) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.Array) {
			AmfArray arr = (AmfArray) raw;
			// arr.add(new AmfInteger(value));
			for (int index = 0; index < arr.getDenseSize(); ++index) {
				AmfValue val = arr.get(index);
				if (val instanceof AmfInteger && ((AmfInteger) val).getValue() == value) {
					arr.remove(index);
					break;
				}
			}
		}
	}

	public void setFlags(String ident, Collection<Integer> values) {
		AmfValue raw = find(ident);
		if (raw == null) {
			return;
		}
		if (raw.getType() == AmfType.Array) {
			AmfArray arr = (AmfArray) raw;
			arr.clear();
			for (Integer value : values) {
				arr.add(new AmfInteger(value));
			}
		}
	}
}
