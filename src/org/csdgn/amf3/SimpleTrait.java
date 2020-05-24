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

import java.util.Arrays;
import java.util.List;

/**
 * A simple trait implementation that does not allow modification of its values.
 * 
 * @author Robert Maupin
 *
 */
public class SimpleTrait implements Trait {
	protected boolean dynamic;
	protected boolean externalizable;
	protected String name;
	protected List<String> properties;

	/**
	 * Creates a trait with the given values.
	 * 
	 * @param name
	 *            the name of the trait.
	 * @param isDynamic
	 *            true if the trait is dynamic
	 * @param isExternalizable
	 *            true if the trait is dynamic
	 * @param properties
	 *            an array of the sealed properties
	 */
	public SimpleTrait(String name, boolean isDynamic, boolean isExternalizable, String[] properties) {
		this.name = name;
		this.dynamic = isDynamic;
		this.externalizable = isExternalizable;
		this.properties = Arrays.asList(properties);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getProperties() {
		return properties;
	}

	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	@Override
	public boolean isExternalizable() {
		return externalizable;
	}

}
