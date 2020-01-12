/**
 * Copyright (c) 2011-2020 Robert Maupin
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */
package org.csdgn.maru;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * A number of useful static methods to make working with IO much easier.
 * 
 * Many of the methods purposely ignore exceptions and return a sentinel value
 * on failure.
 * 
 * @author Robert Maupin
 */
public class Streams {
	private static final int IO_BUFFER_SIZE = 16384;

	/**
	 * Get an every byte from an input stream and put it into a byte array.
	 * 
	 * @param stream
	 *            Input stream to read from
	 * @return bytes retrieved from stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public static byte[] get(InputStream stream) throws IOException {
		if(stream != null) {
			/*
			 * This is the fastest method I know for reading from an
			 * indeterminate stream. So fast in fact, that it pars fairly well
			 * with FileChannel stuff in test cases.
			 * 
			 * Small and simple enough the JIT can grab hold and optimize it,
			 * but with a large enough buffer to not make reading slow.
			 */
			final BufferedInputStream input = new BufferedInputStream(stream);
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream(); /* magic */
			final byte[] reader = new byte[IO_BUFFER_SIZE];
			int r = 0;
			while((r = input.read(reader, 0, IO_BUFFER_SIZE)) != -1) {
				buffer.write(reader, 0, r);
			}
			buffer.flush();
			return buffer.toByteArray();
		}
		return null;
	}

	/**
	 * Get an every byte from an input stream and put it into a byte array.
	 * 
	 * @param stream
	 *            Input stream to read from
	 * @return bytes retrieved from stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public static final String get(Reader reader) throws IOException {
		if(reader != null) {
			/*
			 * This is the fastest method I know for reading from an
			 * indeterminate reader. So fast in fact, that it pars fairly well
			 * with FileChannel stuff in test cases.
			 * 
			 * Small and simple enough the JIT can grab hold and optimize it,
			 * but with a large enough buffer to not make reading slow.
			 */

			final BufferedReader input = new BufferedReader(reader);
			final StringBuilder buffer = new StringBuilder();

			final char[] read = new char[IO_BUFFER_SIZE];
			int r = 0;
			while((r = input.read(read, 0, IO_BUFFER_SIZE)) != -1) {
				buffer.append(read, 0, r);
			}

			return buffer.toString();
		}
		return null;
	}

	/**
	 * Get an every byte from an input stream and put it into a byte array. Then
	 * it also closes the stream after it is done.
	 * 
	 * @param stream
	 *            Input stream to read from
	 * @return bytes retrieved from stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public static byte[] getAndClose(InputStream stream) throws IOException {
		if(stream != null) {
			try {
				/*
				 * This is the fastest method I know for reading from an
				 * indeterminate stream. So fast in fact, that it pars fairly
				 * well with FileChannel stuff in test cases.
				 * 
				 * Small and simple enough the JIT can grab hold and optimize
				 * it, but with a large enough buffer to not make reading slow.
				 */
				final BufferedInputStream input = new BufferedInputStream(stream);
				final ByteArrayOutputStream buffer = new ByteArrayOutputStream(); /* magic */
				final byte[] reader = new byte[IO_BUFFER_SIZE];
				int r = 0;
				while((r = input.read(reader, 0, IO_BUFFER_SIZE)) != -1) {
					buffer.write(reader, 0, r);
				}
				buffer.flush();
				return buffer.toByteArray();
			} finally {
				stream.close();
			}
		}
		return null;
	}

	/**
	 * Get an every character from a reader and put it into a string. Then it
	 * also closes the reader after it is done.
	 * 
	 * @param reader
	 *            Reader to read from
	 * @return String retrieved from stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public static final String getAndClose(Reader reader) throws IOException {
		if(reader != null) {
			try {
				/*
				 * This is the fastest method I know for reading from an
				 * indeterminate reader. So fast in fact, that it pars fairly
				 * well with FileChannel stuff in test cases.
				 * 
				 * Small and simple enough the JIT can grab hold and optimize
				 * it, but with a large enough buffer to not make reading slow.
				 */
				final BufferedReader input = new BufferedReader(reader);
				final StringBuilder buffer = new StringBuilder();

				final char[] read = new char[IO_BUFFER_SIZE];
				int r = 0;
				while((r = input.read(read, 0, IO_BUFFER_SIZE)) != -1) {
					buffer.append(read, 0, r);
				}

				return buffer.toString();
			} finally {
				reader.close();
			}
		}
		return null;
	}

	/**
	 * Writes to the output as it gets data from the input.
	 * 
	 * @param istream
	 *            Input stream to read from
	 * @param ostream
	 *            Output stream to write to
	 * @throws IOException
	 *             if an error occurs
	 */
	public static void pipe(InputStream istream, OutputStream ostream) throws IOException {
		if(ostream != null && istream != null) {
			/*
			 * This is the fastest method I know for reading from an
			 * indeterminate stream. So fast in fact, that it pars fairly well
			 * with FileChannel stuff in test cases.
			 * 
			 * Small and simple enough the JIT can grab hold and optimize it,
			 * but with a large enough buffer to not make reading slow.
			 */
			final BufferedInputStream input = new BufferedInputStream(istream);
			final BufferedOutputStream output = new BufferedOutputStream(ostream);
			// final FileOutputStream buffer = new FileOutputStream(file);
			final byte[] reader = new byte[IO_BUFFER_SIZE];
			int r = 0;
			while((r = input.read(reader, 0, IO_BUFFER_SIZE)) != -1) {
				output.write(reader, 0, r);
			}
			output.flush();
		}
	}

	/**
	 * Writes to the output as it gets data from the input. Finally it closes
	 * both.
	 * 
	 * @param istream
	 *            Input stream to read from
	 * @param ostream
	 *            Output stream to write to
	 * @throws IOException
	 *             if an error occurs
	 */
	public static void pipeAndClose(InputStream istream, OutputStream ostream) throws IOException {
		if(ostream != null && istream != null) {
			try {
				/*
				 * This is the fastest method I know for reading from an
				 * indeterminate stream. So fast in fact, that it pars fairly
				 * well with FileChannel stuff in test cases.
				 * 
				 * Small and simple enough the JIT can grab hold and optimize
				 * it, but with a large enough buffer to not make reading slow.
				 */
				final BufferedInputStream input = new BufferedInputStream(istream);
				final BufferedOutputStream output = new BufferedOutputStream(ostream);
				final byte[] reader = new byte[IO_BUFFER_SIZE];
				int r = 0;
				while((r = input.read(reader, 0, IO_BUFFER_SIZE)) != -1) {
					output.write(reader, 0, r);
				}
				output.flush();

				input.close();
				output.close();
			} finally {
				istream.close();
				ostream.close();
			}
		}
	}

	private Streams() {
	}
}
