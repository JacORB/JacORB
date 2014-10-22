package org.jacorb.test.bugs.bug969;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.CodeSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class is responsible for testing the CodeSet.write_string method with different strings: 1 byte, 2, byte, 3 byte, 4 byte and mixed strings.
 * See
 * http://www.utf8-zeichentabelle.de/unicode-utf8-table.pl
 * http://www.fileformat.info/info/unicode/index.htm
 * http://www.charbase.com/
 * for UTF-8 characters
 *
 * @author gotthardwitsch
 */
public class CodesetWriteStringTest {

	@Test
	public void testStringWith1Byte() {
		CodeSet codeSet = CodeSet.getCodeSet("utf8");

		CDROutputStream cdrOutputStream = new CDROutputStream();
		byte[] expected = new byte[] {(byte)0x61};
		String string = "a";

		codeSet.write_string(cdrOutputStream, string, false, false, 2);
		byte[] bufferCopy = cdrOutputStream.getBufferCopy();
		Assert.assertArrayEquals(expected, bufferCopy);
	}

	@Test
	public void testStringWith2Byte() {
		CodeSet codeSet = CodeSet.getCodeSet("utf8");

		CDROutputStream cdrOutputStream = new CDROutputStream();
		byte[] expected = new byte[] {(byte)0xc2, (byte)0xae};
		String string = "®";

		codeSet.write_string(cdrOutputStream, string, false, false, 2);
		byte[] bufferCopy = cdrOutputStream.getBufferCopy();
		Assert.assertArrayEquals(expected, bufferCopy);
	}

	@Test
	public void testStringWith3Byte() {
		CodeSet codeSet = CodeSet.getCodeSet("utf8");

		CDROutputStream cdrOutputStream = new CDROutputStream();
		byte[] expected = new byte[] {(byte)0xe0, (byte)0xa4, (byte)0xbf};
		String string = "ि";

		codeSet.write_string(cdrOutputStream, string, false, false, 2);
		byte[] bufferCopy = cdrOutputStream.getBufferCopy();
		Assert.assertArrayEquals(expected, bufferCopy);
	}

	@Test
	public void testStringWith4Byte() {
		CodeSet codeSet = CodeSet.getCodeSet("utf8");

		CDROutputStream cdrOutputStream = new CDROutputStream();
		byte[] expected = new byte[] {(byte)0xf0, (byte)0x9f, (byte)0x98, (byte)0x8e};
		String string = "😎";

		codeSet.write_string(cdrOutputStream, string, false, false, 2);
		byte[] bufferCopy = cdrOutputStream.getBufferCopy();
		Assert.assertArrayEquals(expected, bufferCopy);
	}

	@Test
	public void testMixedString() {
		CodeSet codeSet = CodeSet.getCodeSet("utf8");

		CDROutputStream cdrOutputStream = new CDROutputStream();
		byte[] expected = new byte[] {
				(byte)0x61,											// a
				(byte)0x73,											// s
				(byte)0x64,											// d
				(byte)0x66,											// f
				(byte)0x20,											// space
				(byte)0xe0, (byte)0xa4, (byte)0x91,					// DEVANAGARI LETTER CANDRA O
				(byte)0x20,											// space
				(byte)0xe1, (byte)0x85, (byte)0x93,					// HANGUL CHOSEONG CHIEUCH-HIEUH
				(byte)0x20,											// space
				(byte)0xf0, (byte)0x90, (byte)0x90, (byte)0xa7, 	// DESERET CAPITAL LETTER EW
				(byte)0x20,											// space
				(byte)0xf0, (byte)0x90, (byte)0x91, (byte)0x89,		// DESERET SMALL LETTER ER
				(byte)0x20,											// space
				(byte)0xf0, (byte)0x9d, (byte)0x96, (byte)0x84,		// MATHEMATICAL BOLD FRAKTUR CAPITAL Y
				(byte)0x20,											// space
				(byte)0xf0, (byte)0x9f, (byte)0x88, (byte)0xb3,		// SQUARED CJK UNIFIED IDEOGRAPH-7A7A
				(byte)0x20,											// space
				(byte)0xcd, (byte)0xb2,								// GREEK CAPITAL LETTER ARCHAIC SAMPI
				(byte)0xd1, (byte)0xbe								// CYRILLIC CAPITAL LETTER OT
				};
		String string = "asdf ऑ ᅓ 𐐧 𐑉 𝖄 🈳 ͲѾ";

		codeSet.write_string(cdrOutputStream, string, false, false, 2);
		byte[] bufferCopy = cdrOutputStream.getBufferCopy();
		Assert.assertArrayEquals(expected, bufferCopy);
	}
}
