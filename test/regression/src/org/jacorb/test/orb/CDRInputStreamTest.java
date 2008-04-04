package org.jacorb.test.orb;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2008 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.test.common.ORBTestCase;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:russell.gold@oracle.com">Russell Gold</a>
 */
public class CDRInputStreamTest extends ORBTestCase
{

    public static TestSuite suite()
    {
        return new TestSuite( CDRInputStreamTest.class );
    }


    /**
     * Verifies that the default encoding (ISO8859_1) works for char, char arrays, and strings. Reading the string
     * forces alignment of the 4-byte length, and ignores any null terminator.
     */
    public void testDefaultEncodingChar() throws Exception
    {
        byte[] codedText = {'a', 's', 'd', 'f', 'x', 'y', 'z', '*',
                0, 0, 0, 5, 'C', 'A', 'F', 'E', 0};
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        assertEquals( "char 1", 'a', stream.read_char() );
        assertEquals( "char 2", 's', stream.read_char() );
        assertEquals( "char 3", 'd', stream.read_char() );
        assertEquals( "char 4", 'f', stream.read_char() );

        char[] buffer = new char[4];
        buffer[0] = 'w';
        stream.read_char_array( buffer, 1, 3 );
        assertEquals( "char array", "wxyz", new String( buffer ) );

        assertEquals( "string value", "CAFE", stream.read_string() );
    }


    /**
     * Verifies that the default encoding (UTF-16) works for wchar, wchar arrays, and wstrings
     * with no byte-order-marker. Reading the wstring
     * forces alignment of the 4-byte length.
     */
    public void testDefaultEncodingWChar() throws Exception
    {
        byte[] codedText = {2, 0x5, (byte) (0xD0 & 0xff),  // Hebrew letter aleph
                2, 0x30, 0x51,                 // Hiragana syllable ha
                2, 0x30, 0x74,                 // Hiragana syllable pi
                2,                             // Hebrew letter beis: length byte
                (byte) (0xFE & 0xff),       //   Big-endian indicator
                (byte) (0xFF & 0xff),
                0x5, (byte) (0xD1 & 0xff),  //   UTF16 encoding (big-endian)
                2,                             // Hebrew letter gimmel: length byte
                (byte) (0xFF & 0xff),       //   Little-endian indicator
                (byte) (0xFE & 0xff),
                (byte) (0xD2 & 0xff), 0x5,  //   UTF16 encoding (little-endian)
                2, 0x5, (byte) (0xD3 & 0xff),  // Hebrew letter dalet
                45, 23,                        // bytes ignored by 'long' alignment
                0, 0, 0, 8,                    // string length in bytes, not chars
                0x30, (byte) (0xDF & 0xff),    // Mitsubishi, in Katakana
                0x30, (byte) (0xC4 & 0xff),
                0x30, (byte) (0xFA & 0xff),
                0x30, (byte) (0xB7 & 0xff),
        };
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        assertEquals( "wchar 1", '\u05D0', stream.read_wchar() );
        assertEquals( "wchar 2", '\u3051', stream.read_wchar() );
        assertEquals( "wchar 3", '\u3074', stream.read_wchar() );

        char[] buffer = new char[4];
        buffer[0] = '\u05D0';
        stream.read_wchar_array( buffer, 1, 3 );
        assertEquals( "wchar array", "\u05D0\u05D1\u05D2\u05D3", new String( buffer ) );

        assertEquals( "wstring value", "\u30DF\u30C4\u30FA\u30B7", stream.read_wstring() );
    }


    /**
     * Verifies that the default encoding (UTF-16) works for wstrings in giop 1.1, which uses the length
     * indicator to specify the number of characters rather than bytes and require a two-byte null terminator.
     * Wide characters in 1.1 do not take width bytes
     */
    public void testDefaultEncodingWCharGiop1_1() throws Exception
    {
        byte[] codedText = {0, 0, 0, 5,                    // string length in bytes, not chars
                0x30, (byte) (0xDF & 0xff),    // Mitsubishi, in Katakana
                0x30, (byte) (0xC4 & 0xff),
                0x30, (byte) (0xFA & 0xff),
                0x30, (byte) (0xB7 & 0xff),
                0, 0,                       // two-byte null terminator
                0x5, (byte) (0xD1 & 0xff),  // Hebrew letter beis
        };
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        stream.setGIOPMinor( 1 );
        assertEquals( "wstring value", "\u30DF\u30C4\u30FA\u30B7", stream.read_wstring() );

        assertEquals( "wchar 1", '\u05D1', stream.read_wchar() );
    }


    /**
     * Verifies that the UTF-8 encoding works for strings in giop 1.1.
     */
    public void testUTF8EncodingCharGiop1_1() throws Exception
    {
        byte[] codedText = {0, 0, 0, 5,                    // string length in bytes, including null pointer
                'a', 's', 'd', 'f', 0,         // one-byte null terminator
                'x'
        };
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        selectCodeSets( stream, "UTF8", "UTF8" );
        stream.setGIOPMinor( 1 );
        assertEquals( "sstring value", "asdf", stream.read_string() );

        assertEquals( "char 1", 'x', stream.read_char() );
    }


    /**
     * Verifies that the UTF-8 works for wchar, wchar arrays, and wstrings. Reading the wstring
     * forces alignment of the 4-byte length. Note that byte-ordering is fixed by the encoding.
     */
    public void testUTF8EncodingWChar() throws Exception
    {
        byte[] codedText = {1, 'x',                                                              // Latin-l lowercase x
                2, (byte) (0xD7 & 0xff), (byte) (0x90 & 0xff),                       // Hebrew letter aleph
                3, (byte) (0xE3 & 0xff), (byte) (0x81 & 0xff), (byte) (0x91 & 0xff), // Hiragana syllable ha
                3, (byte) (0xE3 & 0xff), (byte) (0x81 & 0xff), (byte) (0xB4 & 0xff), // Hiragana syllable pi
                2, (byte) (0xD7 & 0xff), (byte) (0x91 & 0xff),                       // Hebrew letter beis
                2, (byte) (0xD7 & 0xff), (byte) (0x92 & 0xff),                       // Hebrew letter gimmel
                2, (byte) (0xD7 & 0xff), (byte) (0x93 & 0xff),                       // Hebrew letter dalet
                45, 73,                                                              // bytes ignored by 'long' alignment
                0, 0, 0, 12,                                                         // string length in bytes, not chars
                (byte) (0xE3 & 0xff), (byte) (0x83 & 0xff), (byte) (0x9F & 0xff),    // Mitsubishi, in Katakana
                (byte) (0xE3 & 0xff), (byte) (0x83 & 0xff), (byte) (0x84 & 0xff),
                (byte) (0xE3 & 0xff), (byte) (0x83 & 0xff), (byte) (0xBA & 0xff),
                (byte) (0xE3 & 0xff), (byte) (0x82 & 0xff), (byte) (0xB7 & 0xff),
        };
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        selectCodeSets( stream, "ISO8859_1", "UTF8" );
        assertEquals( "wchar 1", 'x', stream.read_wchar() );
        assertEquals( "wchar 2", '\u05D0', stream.read_wchar() );
        assertEquals( "wchar 3", '\u3051', stream.read_wchar() );
        assertEquals( "wchar 4", '\u3074', stream.read_wchar() );

        char[] buffer = new char[4];
        buffer[0] = '\u05D0';
        stream.read_wchar_array( buffer, 1, 3 );
        assertEquals( "wchar array", "\u05D0\u05D1\u05D2\u05D3", new String( buffer ) );

        assertEquals( "wstring value", "\u30DF\u30C4\u30FA\u30B7", stream.read_wstring() );
    }


    private void selectCodeSets( CDRInputStream stream, String charCodeSet, String wideCharCodeSet )
    {
        stream.setCodeSet( CodeSet.getCodeSet( charCodeSet ), CodeSet.getCodeSet( wideCharCodeSet ) );
    }
}
