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
package org.csdgn.maru.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentAdapter implements DocumentListener {
	public interface SimpleDocumentListener {
		public void update(DocumentEvent e);
	}
	
	private SimpleDocumentListener sdl;
	
	public DocumentAdapter() {
		this.sdl = null;
	}
	
	public DocumentAdapter(SimpleDocumentListener sdl) {
		this.sdl = sdl;
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		if(null != sdl) {
			sdl.update(e);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if(null != sdl) {
			sdl.update(e);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if(null != sdl) {
			sdl.update(e);
		}
	}

}
