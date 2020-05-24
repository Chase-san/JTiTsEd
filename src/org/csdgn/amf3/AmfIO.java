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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is an IO class to handle reading and writing from .SOL files and
 * singularly serialized AmfValues.
 * 
 * @author Robert Maupin
 */
public class AmfIO {
	/**
	 * Specifies an Entry in an AMF file.
	 * 
	 * @author Robert Maupin
	 */
	private static interface AmfEntry {
		/**
		 * The key associated with the entry.
		 * 
		 * @return The key or null if there is no associated key, which will only occur
		 *         if the stream does not represent a file.
		 */
		public String key();

		/**
		 * The value associated with the entry.
		 * 
		 * @return The value.
		 */
		public AmfValue value();
	}

	/**
	 * Contains all Amf Input methods and logic.
	 * 
	 * @author Robert Maupin
	 */
	private static class AmfInput implements Closeable, AutoCloseable {
		private List<ExternalizableFactory> factories;
		private boolean file;
		private boolean headerRead;
		private DataInputStream in;
		private String name;
		private List<AmfValue> referenceTable;
		private List<String> stringTable;
		private List<Trait> traitTable;

		/**
		 * Creates an AmfInputStream with the given InputStream as input.
		 * 
		 * @param in
		 *            the InputStream to read from.
		 * @param file
		 *            if the stream is reading from a SOL formatted file.
		 */
		protected AmfInput(InputStream in, boolean file) {
			if (!(in instanceof BufferedInputStream)) {
				in = new BufferedInputStream(in);
			}
			this.in = new DataInputStream(in);
			this.stringTable = new ArrayList<String>();
			this.referenceTable = new ArrayList<AmfValue>();
			this.traitTable = new ArrayList<Trait>();
			this.factories = new ArrayList<ExternalizableFactory>();
			this.headerRead = false;
			this.name = null;
			this.file = file;
		}

		private AmfXml _readXml(boolean isDocument) throws IOException {
			// Stored by ref?
			Header h = readHeader();
			if (h.isReference) {
				return (AmfXml) referenceTable.get(h.countIndexLength);
			}

			// Stored by value
			AmfXml result = new AmfXml(isDocument);
			result.setValue(readUTF8(h.countIndexLength));
			referenceTable.add(result);
			return result;
		}

		/**
		 * Associates the specified ExternalizableFactory with this AmfInputStream.
		 * Every ExternalizableFactory is called in the order they were added in attempt
		 * to find one that will provide a proper Externalizable for use.
		 * 
		 * @param factory
		 *            the ExternalizableFactory to add
		 */
		protected void addExternalizableFactory(ExternalizableFactory factory) {
			if (Objects.isNull(factory)) {
				throw new IllegalArgumentException("The factory provided cannot be null.");
			}
			factories.add(factory);
		}

		@Override
		public void close() throws IOException {
			in.close();
		}

		/**
		 * Gets the name stored in the amf file.
		 * 
		 * @return the file name, or null if the stream does not represent a file.
		 * @throws UnexpectedDataException
		 *             the stream data was not in an expected format.
		 * @throws IOException
		 *             the stream has been closed and the contained input stream does
		 *             not support reading after close, or another I/O error occurs.
		 */
		protected String getName() throws IOException, UnexpectedDataException {
			if (file && !headerRead) {
				readFileHeader();
			}
			return name;
		}

		/**
		 * Determines if this input stream has another entry in it.
		 * 
		 * @return true if there is another entry, false otherwise.
		 * @throws IOException
		 *             the stream has been closed and the contained input stream does
		 *             not support reading after close, or another I/O error occurs.
		 * @throws UnexpectedDataException
		 *             the stream data was not in an expected format.
		 */
		protected boolean hasNext() throws IOException, UnexpectedDataException {
			if (file && !headerRead) {
				readFileHeader();
			}
			// using the absolute simplest method at the moment.
			// this is basically only a isEOF check.
			in.mark(8);
			if (in.read() == -1) {
				return false;
			}
			in.reset();
			return true;
		}

		/**
		 * Returns the next entry in the input stream.
		 * 
		 * @return the next entry, or null of the end of the stream has been reached.
		 * @throws IOException
		 *             the stream has been closed and the contained input stream does
		 *             not support reading after close, or another I/O error occurs.
		 * @throws UnexpectedDataException
		 *             the stream data was not in an expected format.
		 */
		protected AmfEntry next() throws IOException, UnexpectedDataException {
			if (file && !headerRead) {
				readFileHeader();
			}

			final String key;
			if (file) {
				key = readString();
			} else {
				key = null;
			}
			final AmfValue value = readValue();

			if (file) {
				// trailer, skip byte
				in.skipBytes(1);
			}

			return new AmfEntry() {
				@Override
				public String key() {
					return key;
				}

				@Override
				public AmfValue value() {
					return value;
				}
			};
		}

		private AmfArray readArray() throws IOException, UnexpectedDataException {
			// Stored by ref?
			Header h = readHeader();
			if (h.isReference) {
				return (AmfArray) referenceTable.get(h.countIndexLength);
			}

			// Stored by value
			AmfArray result = new AmfArray();
			referenceTable.add(result);

			// Associative part (key-value pairs)
			while (true) {
				String key = readString();
				if (key == "") {
					break;
				}

				AmfValue value = readValue();
				result.put(key, value);
			}

			// Dense part (consecutive indices >=0 and <count)
			for (int i = 0; i < h.countIndexLength; i++) {
				AmfValue value = readValue();
				result.add(value);
			}

			return result;
		}

		private AmfByteArray readByteArray() throws IOException {
			// Stored by ref?
			Header h = readHeader();
			if (h.isReference) {
				return (AmfByteArray) referenceTable.get(h.countIndexLength);
			}

			// Stored by value
			byte[] array = new byte[h.countIndexLength];
			in.readFully(array);

			AmfByteArray aba = new AmfByteArray();
			aba.push(array);
			referenceTable.add(aba);
			return aba;
		}

		private AmfDate readDate() throws IOException {
			// Stored by ref?
			Header h = readHeader();
			if (h.isReference) {
				return (AmfDate) referenceTable.get(h.countIndexLength);
			}

			// Stored by value
			double elapsed = in.readDouble();
			AmfDate date = new AmfDate(elapsed);
			referenceTable.add(date);
			return date;
		}

		private AmfDictionary readDictionary() throws IOException, UnexpectedDataException {
			// Stored by ref?
			Header h = readHeader();
			if (h.isReference) {
				return (AmfDictionary) referenceTable.get(h.countIndexLength);
			}

			// Stored by value
			boolean weakKeys = in.readBoolean();
			AmfDictionary result = new AmfDictionary(weakKeys);
			referenceTable.add(result);

			for (int j = 0; j < h.countIndexLength; ++j) {
				AmfValue key = readValue();
				AmfValue value = readValue();
				result.put(key, value);
			}

			return result;
		}

		private AmfDouble readDouble() throws IOException {
			return new AmfDouble(in.readDouble());
		}

		private void readFileHeader() throws IOException, UnexpectedDataException {
			if (in.readUnsignedByte() != 0x0) {
				throw new UnexpectedDataException("Unknown Endianness");
			}
			if (in.readUnsignedByte() != 0xBF) {
				throw new UnexpectedDataException("Unknown Endianness");
			}

			// Size
			int size = in.readInt();
			// TODO
			// if(size + 6 != fileSize) throw new
			// InvalidOperationException("Wrong file size");

			// Magic signature
			String magic = readString(4);
			if (!"TCSO".equals(magic)) {
				throw new UnexpectedDataException("Wrong file tag");
			}
			in.skipBytes(6);

			// Read name
			size = in.readUnsignedShort();
			name = readString(size);

			// Version
			int version = (int) in.readInt();
			if (version < 3) {
				throw new UnexpectedDataException("Wrong AMF version");
			}

			headerRead = true;
		}

		private Header readHeader() throws IOException {
			return new Header(readU29());
		}

		private AmfInteger readInteger() throws IOException {
			return new AmfInteger(readS29());
		}

		private AmfObject readObject() throws IOException, UnexpectedDataException {
			Header h = readHeader();
			if (h.isReference) {
				return (AmfObject) referenceTable.get(h.countIndexLength);
			}

			Trait trait = readTrait(h);
			AmfObject result = new AmfObject();
			result.setDynamic(trait.isDynamic());
			result.setExternalizable(trait.isExternalizable());
			result.setTraitName(trait.getName());

			// read sealed properties
			Map<String, AmfValue> map = result.getSealedMap();
			for (String property : trait.getProperties()) {
				map.put(property, readValue());
			}

			// read dynamic properties
			map = result.getDynamicMap();
			if (trait.isDynamic()) {
				while (true) {
					String key = readString();
					if (key.length() == 0) {
						break;
					}
					map.put(key, readValue());
				}
			}

			// read custom data
			if (trait.isExternalizable()) {
				Externalizable ex = null;
				for (ExternalizableFactory factory : factories) {
					ex = factory.create(trait.getName());
					if (ex != null) {
						break;
					}
				}
				if (ex == null) {
					throw new UnsupportedOperationException(
							"Externalizable factory does not support the externalizable data.");
				}
				try {
					ex.readExternal(in);
				} catch (UnexpectedDataException e) {
					throw new UnsupportedOperationException("Externalizable cannot read the externalizable data.");
				}
				result.setExternalizableObject(ex);
			}

			referenceTable.add(result);
			return result;
		}

		private int readS29() throws IOException {
			int result = readU29();
			int maxPositiveInclusive = (1 << 28) - 1;
			if (result <= maxPositiveInclusive) {
				return result; // Positive number
			}

			// Negative number. -x is stored as 2^29 - x
			int upperExclusiveBound = 1 << 29;
			return result - upperExclusiveBound;
		}

		private String readString() throws IOException {
			Header h = readHeader();

			// Stored by reference?
			if (h.isReference) {
				return stringTable.get(h.countIndexLength);
			}

			// Empty string (never stored by ref) ?
			if (h.countIndexLength == 0) {
				return "";
			}

			// Read the string
			String str = readUTF8(h.countIndexLength);
			stringTable.add(str);

			return str;
		}

		private String readString(int length) throws IOException {
			// UTF-8 support
			byte[] data = new byte[length];
			in.read(data);
			return new String(data, StandardCharsets.US_ASCII);
		}

		private Trait readTrait(Header h) throws IOException {
			boolean traitReference = h.readNextBit();
			if (!traitReference) {
				return (Trait) traitTable.get(h.countIndexLength);
			}

			boolean ext = h.readNextBit();
			boolean dyn = h.readNextBit();
			String name = readString();

			// read properties
			String[] props = new String[h.countIndexLength];
			for (int i = 0; i < props.length; ++i) {
				props[i] = readString();
			}

			Trait trait = new SimpleTrait(name, dyn, ext, props);
			traitTable.add(trait);

			return trait;
		}

		private int readU29() throws IOException {
			// Unsigned integer encoded on 8 to 32 bits, with 7 to 29 significant
			// bits.
			// The most significant bits are stored on the left (at the beginning).
			// The fourth byte always have 8 significant bits.
			// 7-7-7-8 or 7-7-7 or 7-7 or 7

			int numBytes = 0;
			int result = 0;
			while (true) {
				int b = in.readUnsignedByte();
				if (numBytes == 3) {
					return (result << 8) | b;
				}
				result = (result << 7) | (b & 0x7F);
				if ((b & 0x7F) == b) {
					return result;
				}
				++numBytes;
			}
		}

		private String readUTF8(int length) throws IOException {
			// UTF-8 support
			byte[] data = new byte[length];
			in.read(data);
			return new String(data, StandardCharsets.UTF_8);
		}

		private AmfValue readValue() throws IOException, UnexpectedDataException {
			int typeId = in.readUnsignedByte();
			AmfType type = AmfType.get(typeId);
			switch (type) {
			case Undefined:
				return new AmfUndefined();

			case Null:
				return new AmfNull();

			case True:
				return new AmfBoolean(true);

			case False:
				return new AmfBoolean(false);

			case Integer:
				return readInteger();

			case Double:
				return readDouble();

			case String:
				return new AmfString(readString());

			case Date:
				return readDate();

			case ByteArray:
				return readByteArray();

			case Array:
				return readArray();

			case Object:
				return readObject();

			case Dictionary:
				return readDictionary();

			case VectorInt:
				return readVectorInt();

			case VectorUInt:
				return readVectorUInt();

			case VectorDouble:
				return readVectorDouble();

			case VectorGeneric:
				return readVectorGeneric();

			case XmlDoc:
				return readXmlDoc();

			case Xml:
				return readXml();
			}

			throw new UnexpectedDataException(String.format("Unknown Value Type: 0x%x", typeId));
		}

		private AmfVector.Double readVectorDouble() throws IOException {
			Header h = readHeader();
			if (h.isReference) {
				return (AmfVector.Double) referenceTable.get(h.countIndexLength);
			}
			// Stored by value
			boolean fixedLength = in.readBoolean();
			AmfVector.Double result = new AmfVector.Double();
			result.setFixedLength(fixedLength);
			result.setCapacity(h.countIndexLength);
			for (int i = 0; i < h.countIndexLength; ++i) {
				result.add(new AmfDouble(in.readDouble()));
			}
			referenceTable.add(result);
			return result;
		}

		private AmfVector.Generic readVectorGeneric() throws IOException, UnexpectedDataException {
			Header h = readHeader();
			if (h.isReference) {
				return (AmfVector.Generic) referenceTable.get(h.countIndexLength);
			}
			// Stored by value
			boolean fixedLength = in.readBoolean();
			String type = readString();
			AmfVector.Generic result = new AmfVector.Generic(type);
			result.setFixedLength(fixedLength);
			result.setCapacity(h.countIndexLength);
			for (int i = 0; i < h.countIndexLength; ++i) {
				result.add(readValue());
			}
			referenceTable.add(result);
			return result;
		}

		private AmfVector.Integer readVectorInt() throws IOException {
			Header h = readHeader();
			if (h.isReference) {
				return (AmfVector.Integer) referenceTable.get(h.countIndexLength);
			}
			// Stored by value
			boolean fixedLength = in.readBoolean();
			AmfVector.Integer result = new AmfVector.Integer();
			result.setFixedLength(fixedLength);
			result.setCapacity(h.countIndexLength);
			for (int i = 0; i < h.countIndexLength; ++i) {
				result.add(new AmfInteger(in.readInt()));
			}
			referenceTable.add(result);
			return result;
		}

		private AmfVector.UnsignedInteger readVectorUInt() throws IOException {
			Header h = readHeader();
			if (h.isReference) {
				return (AmfVector.UnsignedInteger) referenceTable.get(h.countIndexLength);
			}
			// Stored by value
			boolean fixedLength = in.readBoolean();
			AmfVector.UnsignedInteger result = new AmfVector.UnsignedInteger();
			result.setFixedLength(fixedLength);
			result.setCapacity(h.countIndexLength);
			for (int i = 0; i < h.countIndexLength; ++i) {
				result.add(new AmfInteger(in.readInt() & 0xFFFFFFFF));
			}
			referenceTable.add(result);
			return result;
		}

		private AmfXml readXml() throws IOException {
			return _readXml(false);
		}

		private AmfXml readXmlDoc() throws IOException {
			return _readXml(true);
		}
	}

	/**
	 * Contains all Amf Output methods and logic.
	 * 
	 * @author Robert Maupin
	 */
	private static class AmfOutput implements Closeable, AutoCloseable {
		private ByteArrayOutputStream buffer;
		private List<ExternalizableFactory> factories;
		private OutputStream fileOut;
		private boolean headerWritten;
		private boolean isFile;
		private String name;
		private DataOutputStream out;
		private List<AmfValue> referenceTable;
		private List<String> stringTable;
		private List<Trait> traitTable;

		public AmfOutput(OutputStream out, boolean file) {
			if (!(out instanceof BufferedOutputStream)) {
				out = new BufferedOutputStream(out);
			}
			this.fileOut = out;
			this.buffer = new ByteArrayOutputStream();
			this.out = new DataOutputStream(this.buffer);
			this.stringTable = new ArrayList<String>();
			this.referenceTable = new ArrayList<AmfValue>();
			this.traitTable = new ArrayList<Trait>();
			this.factories = new ArrayList<ExternalizableFactory>();
			this.headerWritten = false;
			this.name = null;
			this.isFile = file;
		}

		/**
		 * Associates the specified ExternalizableFactory with this AmfInputStream.
		 * Every ExternalizableFactory is called in the order they were added in attempt
		 * to find one that will provide a proper Externalizable for use.
		 * 
		 * @param factory
		 *            the ExternalizableFactory to add
		 */
		protected void addExternalizableFactory(ExternalizableFactory factory) {
			if (Objects.isNull(factory)) {
				throw new IllegalArgumentException("The factory provided cannot be null.");
			}
			factories.add(factory);
		}

		@Override
		public void close() throws IOException {
			out.close();

			byte[] data = buffer.toByteArray();
			buffer.close();

			// update header length
			if (isFile) {
				int size = data.length - 6;

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dos.writeInt(size);

				byte[] sizeBytes = baos.toByteArray();
				System.arraycopy(sizeBytes, 0, data, 2, 4);
			}

			// write to actual output
			fileOut.write(data);
			fileOut.flush();
			fileOut.close();
		}

		/**
		 * Set the name to be stored in the SOL file.
		 * 
		 * @param name
		 *            the file name to store in the SOL file.
		 * @throws UnexpectedDataException
		 *             the stream data was not in an expected format.
		 * @throws IOException
		 *             the stream has been closed and the contained input stream does
		 *             not support reading after close, or another I/O error occurs.
		 */
		protected void setName(String name) {
			this.name = name;
		}

		private void writeArray(AmfArray arr) throws IOException {
			if (!writeRef(arr)) {
				writeU29Flag(arr.getDenseSize(), true);
				// out.write(arr.getBackingArray(), 0, arr.size());
				// write associative data (key-value pairs)
				Map<String, AmfValue> ass = arr.getAssociative();
				for (String key : ass.keySet()) {
					writeString(key);
					writeValue(ass.get(key));
				}
				writeString("");

				// write dense data (list line 0 to count)
				for (AmfValue val : arr.getDense()) {
					writeValue(val);
				}
			}

		}

		private void writeByteArray(AmfByteArray arr) throws IOException {
			if (!writeRef(arr)) {
				writeU29Flag(arr.size(), true);
				out.write(arr.getBackingArray(), 0, arr.size());
			}
		}

		private void writeDate(AmfDate date) throws IOException {
			if (!writeRef(date)) {
				writeU29Flag(0, true);
				out.writeDouble(date.getValue());
			}
		}

		private void writeDictionary(AmfDictionary dict) throws IOException {
			if (!writeRef(dict)) {
				writeU29Flag(dict.size(), true);

				// out.write
				out.writeBoolean(dict.hasWeakKeys());

				for (AmfValue key : dict.keySet()) {
					writeValue(key);
					writeValue(dict.get(key));
				}
			}
		}

		/**
		 * Writes the given name/value to a SOL. If writing to an serialized AMFObject,
		 * call this only once. Name will be ignored. Calling this more than once
		 * outside of a file may produce undefined behavior.
		 * 
		 * If writing to an SOL file and the header has not been written, it will
		 * automatically be written.
		 * 
		 * @param name
		 *            The name of the entry
		 * @param value
		 *            The value of the entry
		 * 
		 * @throws IOException
		 *             if an I/O exception occured during the write.
		 */
		protected void writeEntry(String name, AmfValue value) throws IOException {
			writeFileHeader();

			writeString(name);
			writeValue(value);

			// append trailing zero if a file
			if (isFile) {
				out.writeByte(0);
			}
		}

		private void writeFileHeader() throws IOException {
			if (!isFile || headerWritten) {
				return;
			}
			// write BOM
			out.writeByte(0);
			out.writeByte(0xBF);

			// file size placeholder
			out.writeInt(0); // 4 bytes @ index 2

			// write magic header
			out.writeByte('T');
			out.writeByte('C');
			out.writeByte('S');
			out.writeByte('O');

			// write some values??
			// not sure what these values are actually
			out.write(new byte[] { 0, 4, 0, 0, 0, 0 });

			// write name
			byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
			out.writeShort(nameBytes.length & 0xFFFF);
			out.write(nameBytes);

			// write version
			out.writeInt(3);

			headerWritten = true;
		}

		private void writeI29(int value) throws IOException {
			final int upperExclusiveBound = 1 << 29;
			if (value < 0) {
				writeU29(value + upperExclusiveBound); // -x is stored as 2^29 - x
			} else {
				writeU29(value);
			}
		}

		private void writeObject(AmfObject obj) throws IOException {
			if (!writeRef(obj)) {
				writeTrait(obj.getTrait());

				// sealed properties
				Map<String, AmfValue> map = obj.getSealedMap();
				for (String key : map.keySet()) {
					AmfValue val = map.get(key);
					writeValue(val);
				}

				// dynamic properties
				if (obj.isDynamic()) {
					map = obj.getDynamicMap();
					for (String key : map.keySet()) {
						writeString(key);
						writeValue(map.get(key));
					}
					writeString("");
				}

				// externalizable properties
				if (obj.isExternalizable()) {
					Externalizable ext = obj.getExternalizableObject();
					if (ext != null) {
						ext.writeExternal(out);
					}
				}
			}
		}

		private void writePlainString(String str) throws IOException {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			writeU29Flag(data.length, true);
			out.write(data);
		}

		// Most object types are stored by reference so that they are only serialized
		// once. After that only their reference index is stored.
		/**
		 * Write new reference or get reference
		 * 
		 * @param obj
		 *            the object to write
		 * @return true if reference exists, false otherwise.
		 */
		private boolean writeRef(AmfValue obj) throws IOException {
			//no reference table when writing to a serialized reference
			if(!isFile) {
				return false;
			}
			
			int index;

			// My system works way better than ActionScript does at determining
			// if two objects are equal, so I had to forcefully block things
			// so that it is equally bad at determining if some things are equal
			boolean beLessGoodPlox = obj instanceof AmfObject || obj instanceof AmfArray;
			if (!beLessGoodPlox && (index = referenceTable.indexOf(obj)) != -1) {
				writeU29Flag(index, false);
				return true;
			}

			referenceTable.add(obj);
			return false;
		}

		private void writeString(String str) throws IOException {
			int index = -1;
			if (str.length() == 0) {
				// empty string
				writeU29Flag(0, true);
			} else if (isFile && (index = stringTable.indexOf(str)) != -1) {
				// reference
				writeU29Flag(index, false);
			} else {
				// plain string
				writePlainString(str);
				//no string table when not writing to a file
				if(isFile) {
					stringTable.add(str);
				}
			}
		}

		void writeTrait(Trait trait) throws IOException {
			int index = -1;
			if(isFile) {
				index = traitTable.indexOf(trait);
				if (index != -1) {
					writeU29((index << 2) | 1);
					return;
				}
				traitTable.add(trait);	
			}
			index = 3;
			if (trait.isExternalizable()) {
				index |= 4;
			}
			if (trait.isDynamic()) {
				index |= 8;
			}
			List<String> props = trait.getProperties();
			index |= (props.size() << 4);
			writeU29(index);

			writeString(trait.getName());
			for (String name : props) {
				writeString(name);
			}
		}

		private void writeU29(long value) throws IOException {
			int iVal = (int) (value & 0x3FFFFFFF);

			// much faster (and smaller!) than some complicated loop
			if (value < 0x80) {
				// 7 bits
				out.writeByte(iVal & 0x7F);
			} else if (value < 0x4000) {
				// 14 bits
				out.writeByte(0x80 | ((iVal >> 7) & 0x7F));
				out.writeByte((iVal & 0x7F));
			} else if (value < 0x200000) {
				// 21 bits
				out.writeByte(0x80 | ((iVal >> 14) & 0x7F));
				out.writeByte(0x80 | ((iVal >> 7) & 0x7F));
				out.writeByte((iVal & 0x7F));
			} else {
				// 29 bits, this one doesn't follow the above pattern
				out.writeByte(0x80 | ((iVal >> 22) & 0x7F));
				out.writeByte(0x80 | ((iVal >> 15) & 0x7F));
				out.writeByte(0x80 | ((iVal >> 8) & 0x7F));
				out.writeByte((iVal & 0xFF));
			}

		}

		private void writeU29Flag(long value, boolean flag) throws IOException {
			value <<= 1;
			if (flag) {
				value |= 1;
			}
			writeU29(value);
		}

		/**
		 * Writes a given value to the stream.
		 * 
		 * @param value
		 *            the value to write.
		 * @throws IOException
		 */
		protected void writeValue(AmfValue value) throws IOException {
			AmfType type = value.getType();
			out.write(type.id);
			switch (type) {
			case Array:
				writeArray((AmfArray) value);
				break;
			case ByteArray:
				writeByteArray((AmfByteArray) value);
				break;
			case Date:
				writeDate((AmfDate) value);
				break;
			case Dictionary:
				writeDictionary((AmfDictionary) value);
				break;
			case Double:
				out.writeDouble(((AmfDouble) value).getValue());
				break;
			case Integer:
				writeI29(((AmfInteger) value).getValue());
				break;
			case Object:
				writeObject((AmfObject) value);
				break;
			case String:
				writeString(((AmfString) value).getValue());
				break;
			case VectorDouble:
				writeVector((AmfVector.Double) value);
				break;
			case VectorGeneric:
				writeVector((AmfVector.Generic) value);
				break;
			case VectorInt:
				writeVector((AmfVector.Integer) value);
				break;
			case VectorUInt:
				writeVector((AmfVector.UnsignedInteger) value);
				break;
			case Xml:
			case XmlDoc:
				writeXml((AmfXml) value);
				break;

			case Null:
			case False:
			case True:
			case Undefined:
				// nothing more required
				break;
			default:
				// WTF is this shit?
				break;
			}
		}

		private void writeVector(AmfVector.Double vec) throws IOException {
			if (!writeRef(vec)) {
				writeU29Flag(vec.size(), true);
				out.writeBoolean(vec.isFixedLength());
				for (AmfDouble val : vec) {
					out.writeDouble(val.getValue());
				}
			}
		}

		private void writeVector(AmfVector.Generic vec) throws IOException {
			if (!writeRef(vec)) {
				writeU29Flag(vec.size(), true);
				out.writeBoolean(vec.isFixedLength());
				writeString(vec.getTypeName());
				for (AmfValue val : vec) {
					writeValue(val);
				}
			}
		}

		private void writeVector(AmfVector.Integer vec) throws IOException {
			if (!writeRef(vec)) {
				writeU29Flag(vec.size(), true);
				out.writeBoolean(vec.isFixedLength());
				for (AmfInteger val : vec) {
					out.writeInt(val.getValue());
				}
			}
		}

		private void writeVector(AmfVector.UnsignedInteger vec) throws IOException {
			if (!writeRef(vec)) {
				writeU29Flag(vec.size(), true);
				out.writeBoolean(vec.isFixedLength());
				for (AmfInteger val : vec) {
					out.writeInt((int) val.getUnsignedValue());
				}
			}
		}

		private void writeXml(AmfXml xml) throws IOException {
			if (!writeRef(xml)) {
				writePlainString(xml.getValue());
			}
		}

	}

	private static class Header {
		protected int countIndexLength;
		protected boolean isReference;

		protected Header(int u29) {
			this.countIndexLength = u29;
			this.isReference = !readNextBit();
		}

		/**
		 * Reads the next bit and reduces the countIndexLength by a bit.
		 * 
		 * @return
		 */
		protected boolean readNextBit() {
			boolean result = (countIndexLength & 1) == 1;
			countIndexLength >>= 1;
			return result;
		}
	}
	
	/**
	 * Determines if the given file is a shared object, or if it is a serialized value.
	 * @param file file to check
	 * @return true if shared object, false otherwise.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static final boolean isAmfFile(File file) throws FileNotFoundException, IOException {
		//starts with 0xA (.tits file)
		//starts with 0x0 (.sol file)
		try(FileInputStream fis = new FileInputStream(file)) {
			if(fis.read() != 0x0) {
				return false;
			}
			if(fis.read() != 0xBF) {
				return false;
			}
			fis.skip(4);
			if(fis.read() != 'T') {
				return false;
			}
			if(fis.read() != 'C') {
				return false;
			}
			if(fis.read() != 'S') {
				return false;
			}
			if(fis.read() != 'O') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads a serialized AmfValue from the given file.
	 * 
	 * @param file
	 *            The file to read from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @return The AmfValue read.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final AmfValue read(File file, ExternalizableFactory... ext)
			throws FileNotFoundException, IOException, UnexpectedDataException {
		return read(new FileInputStream(file), ext);
	}

	/**
	 * Reads a serialized AmfValue from the given input stream.
	 * 
	 * @param input
	 *            The input stream to read from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @return The AmfValue read.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final AmfValue read(InputStream input, ExternalizableFactory... ext)
			throws IOException, UnexpectedDataException {
		AmfValue value = null;
		try (AmfInput in = new AmfInput(input, false)) {
			for (ExternalizableFactory factory : ext) {
				in.addExternalizableFactory(factory);
			}
			value = in.next().value();
		}
		return value;
	}

	/**
	 * Reads AMF from the given SOL file.
	 * 
	 * @param file
	 *            The file to read from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @return The AmfFile read.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final AmfFile readFile(File file, ExternalizableFactory... ext)
			throws FileNotFoundException, IOException, UnexpectedDataException {
		return readFile(new FileInputStream(file), ext);
	}

	/**
	 * Reads AMF from the given input stream designating an SOL file.
	 * 
	 * @param input
	 *            The input stream to read from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @return The AmfFile read.
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final AmfFile readFile(InputStream input, ExternalizableFactory... ext)
			throws IOException, UnexpectedDataException {
		AmfFile file = null;
		try (AmfInput in = new AmfInput(input, true)) {
			for (ExternalizableFactory factory : ext) {
				in.addExternalizableFactory(factory);
			}
			file = new AmfFile();
			file.setName(in.getName());
			while (in.hasNext()) {
				AmfEntry e = in.next();
				file.put(e.key(), e.value());
			}
		}
		return file;
	}

	/**
	 * Writes a serialized AmfValue to the given file.
	 * 
	 * @param amf
	 *            The AmfValue to write.
	 * @param file
	 *            The file to write to.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final void write(AmfValue amf, File file, ExternalizableFactory... ext)
			throws FileNotFoundException, IOException, UnexpectedDataException {
		write(amf, new FileOutputStream(file), ext);
	}

	/**
	 * Writes a serialized AmfValue to the given output stream.
	 * 
	 * @param amf
	 *            The AmfValue to write.
	 * @param output
	 *            The output stream to write to.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during reading.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the read, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final void write(AmfValue amf, OutputStream output, ExternalizableFactory... ext)
			throws IOException, UnexpectedDataException {
		try (AmfOutput out = new AmfOutput(output, false)) {
			for (ExternalizableFactory factory : ext) {
				out.addExternalizableFactory(factory);
			}
			out.writeValue(amf);
		}
	}

	/**
	 * Writes AMF to the given SOL file.
	 * 
	 * @param amf
	 *            The AMFFile to write.
	 * @param file
	 *            The file to write from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during writeing.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the write, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final void writeFile(AmfFile amf, File file, ExternalizableFactory... ext)
			throws FileNotFoundException, IOException, UnexpectedDataException {
		writeFile(amf, new FileOutputStream(file), ext);
	}

	/**
	 * Writes an AMFFile to the given output stream.
	 * 
	 * @param amf
	 *            The AMFFile to write.
	 * @param isFile
	 *            The file to write from.
	 * @param ext
	 *            The ExternalizableFactorys to use, if any.
	 * @throws FileNotFoundException
	 *             if the file was not found
	 * @throws IOException
	 *             if the program encountered an I/O error during writeing.
	 * @throws UnexpectedDataException
	 *             if invalid data was found during the write, often occurs with an
	 *             invalid or unsupported format.
	 */
	public static final void writeFile(AmfFile amf, OutputStream output, ExternalizableFactory... ext)
			throws IOException, UnexpectedDataException {
		try (AmfOutput out = new AmfOutput(output, true)) {
			for (ExternalizableFactory factory : ext) {
				out.addExternalizableFactory(factory);
			}
			out.setName(amf.getName());
			for (String key : amf.keySet()) {
				AmfValue val = amf.get(key);
				out.writeEntry(key, val);
			}
		}
	}
}
