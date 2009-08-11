package org.jacorb.test.orb.giop;
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
import junit.framework.TestSuite;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CORBA.CODESET_INCOMPATIBLE;

/**
 * @author <a href="mailto:russell.gold@oracle.com">Russell Gold</a>
 */
public class CodeSetTest extends ORBTestCase
{

    private static final int ISO8859_1_ID = 0x00010001;
    private static final int UTF8_ID      = 0x05010001;
    private static final int UTF16_ID     = 0x00010109;
    private static final int UCS2         = 0x00010100;
    private static final int UNKNOWN_ID = -1;


    public static TestSuite suite()
    {
        return new TestSuite( CodeSetTest.class );
    }


    /**
     * Verifies the ability to recognize the supported codeset names.
     */
    public void testNameRecognition() throws Exception
    {
        assertEquals( "Latin-1 set id", ISO8859_1_ID, CodeSet.getCodeSet( "iso8859_1" ).getId() );
        assertEquals( "UTF-8 set id",   UTF8_ID,      CodeSet.getCodeSet( "utf8" ).getId() );
        assertEquals( "UTF-16 set id",  UTF16_ID,     CodeSet.getCodeSet( "Utf16" ).getId() );
        assertEquals( "Unknown set id", UNKNOWN_ID,   CodeSet.getCodeSet( "JUNK" ).getId() );
        assertEquals( "Literal id",     ISO8859_1_ID, CodeSet.getCodeSet( "00010001" ).getId() );
        assertEquals( "Uknown id",      UNKNOWN_ID,   CodeSet.getCodeSet( "01010101" ).getId() );
    }


    /**
     * Verifies the ability to convert an id to a name.
     */
    public void testNameSelection() throws Exception
    {
        assertEquals( "Latin-1 set name", "ISO8859_1",               CodeSet.csName( ISO8859_1_ID ) );
        assertEquals( "UTF-8 set name",   "UTF8",                    CodeSet.csName( UTF8_ID ) );
        assertEquals( "UTF-16 set name",  "UTF16",                   CodeSet.csName( UTF16_ID ) );
        assertEquals( "Unknown set name", "Unknown TCS: 0xbabe",     CodeSet.csName( 0x0000BABE ) );
    }


    /**
     * Verifies detection of the platform standard encoding for char and string.
     */
    public void testDefaultCharEncoding() throws Exception
    {
        int encoding = CodeSet.getTCSDefault().getId();
        if (encoding != ISO8859_1_ID && encoding != UTF8_ID) fail( "Default codeset must be iso8859-1 or UTF-8" );
    }


    /**
     * Verifies failure to match native code sets.
     */
    public void testCodeSetNativeSetsDoNotMatch() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( 1, new int[0] );
        CodeSetComponent remote = new CodeSetComponent( 0x21, new int[0] );
        try
        {
            CodeSet.getMatchingCodeSet( local, remote, /* wide */ true );
            fail( "Should have reported failure to match" );
        } catch (CODESET_INCOMPATIBLE e)
        {
            assertEquals( "exception message", "No matching wide code set found. Client knows {0x00000001}. Server offered {0x00000021}", e.getMessage() );
        }
    }


    /**
     * Verifies success in matching native code sets.
     */
    public void testCodeSetNativeSetsMatch() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( UTF8_ID, new int[0] );
        CodeSetComponent remote = new CodeSetComponent( UTF8_ID, new int[0] );
        assertEquals( "Matched code set", UTF8_ID, CodeSet.getMatchingCodeSet( local, remote, /* wide */ false ).getId() );
    }


    /**
     * Verifies success in matching local native code set to remote conversion set.
     */
    public void testCodeSetNativeSetMatchesRemoteConversion() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( UTF8_ID, new int[]{UTF16_ID} );
        CodeSetComponent remote = new CodeSetComponent( UTF16_ID, new int[]{UTF8_ID} );
        assertEquals( "Matched code set", UTF8_ID, CodeSet.getMatchingCodeSet( local, remote, /* wide */ false ).getId() );
    }


    /**
     * Verifies success in matching local conversion code set to remote native set.
     */
    public void testConversionCodeSetMatchesRemoteNative() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( ISO8859_1_ID, new int[]{UTF16_ID} );
        CodeSetComponent remote = new CodeSetComponent( UTF16_ID, new int[]{UTF16_ID} );
        assertEquals( "Matched code set", UTF16_ID, CodeSet.getMatchingCodeSet( local, remote, /* wide */ false ).getId() );
    }


    /**
     * Verifies success in matching conversion code sets.
     */
    public void testConversionCodeSetsMatch() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( 12, new int[]{UTF16_ID} );
        CodeSetComponent remote = new CodeSetComponent( 5, new int[]{UTF16_ID} );
        assertEquals( "Matched code set", UTF16_ID, CodeSet.getMatchingCodeSet( local, remote, /* wide */ false ).getId() );
    }


    /**
     * Verifies failure to match any code sets.
     */
    public void testCodeSetsDoNotMatch() throws Exception
    {
        CodeSetComponent local = new CodeSetComponent( 1, new int[]{0x123, 0x345} );
        CodeSetComponent remote = new CodeSetComponent( 0x21, new int[]{0x34, 0x567, 0x890} );
        try
        {
            CodeSet.getMatchingCodeSet( local, remote, /* wide */ true );
            fail( "Should have reported failure to match" );
        } catch (CODESET_INCOMPATIBLE e)
        {
            assertEquals( "exception message", "No matching wide code set found. Client knows {0x00000001,0x00000123,0x00000345}. Server offered {0x00000021,0x00000034,0x00000567,0x00000890}", e.getMessage() );
        }
    }


    /**
     * Verify that the local code set components offer the appropriate code sets.
     */
    public void testLocalCodeSets() throws Exception
    {
        CodeSetComponentInfo info = CodeSet.getLocalCodeSetComponentInfo();
        assertNotNull( "iso 8859-1 not supported for char", CodeSet.getCodeSetIfMatched( ISO8859_1_ID, info.ForCharData ) );
        assertNotNull( "utf-8 not supported for char", CodeSet.getCodeSetIfMatched( UTF8_ID, info.ForCharData ) );
        assertNotNull( "utf-8 not supported for wchar", CodeSet.getCodeSetIfMatched( UTF8_ID, info.ForWcharData ) );
        assertNotNull( "utf-16 not supported for wchar", CodeSet.getCodeSetIfMatched( UTF16_ID, info.ForWcharData ) );
        assertNotNull( "ucs-2 not supported for wchar", CodeSet.getCodeSetIfMatched( UCS2, info.ForWcharData ) );
    }

}
