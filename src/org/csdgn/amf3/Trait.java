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

import java.util.List;

/**
 * This defines a Trait. Traits are never stored and are created dynamically and
 * are only generally used when reading or writing to disk.
 * 
 * @author Robert Maupin
 *
 */
public interface Trait {
	/**
	 * Gets the name of this trait.
	 * 
	 * @return the trait name
	 */
	public String getName();

	/**
	 * Gets the list of sealed properties associated with this trait.
	 * 
	 * @return List of trait properties.
	 */
	public List<String> getProperties();

	/**
	 * Indicates if this trait is dynamic or not.
	 * 
	 * @return true if the trait is dynamic, false otherwise
	 */
	public boolean isDynamic();

	/**
	 * Indicates if this trait is externalizable or not.
	 * 
	 * @return true if the trait is externalizable, false otherwise.
	 */
	public boolean isExternalizable();
}
