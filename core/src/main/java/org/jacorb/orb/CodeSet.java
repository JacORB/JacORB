/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 */

package org.jacorb.orb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.omg.CORBA.MARSHAL;
/**
 * @author Gerald Brose
 */
public class CodeSet
{
    static final String CODESET_PREFIX = "0x00000000";

    /**
     * <code>ASCII</code> represents the base 7-bits of ISO8859_1
     */
    static final CodeSet ASCII_CODESET = new AsciiCodeSet();

    /**
     * <code>ISO8859_1</code> represents the default 8-bit codeset.
     * It is ISO 8859-1:1987; Latin Alphabet No. 1
     */
    static final CodeSet ISO8859_1_CODESET = new Iso8859_1CodeSet();

    /**
     * <code>ISO8859_15</code> represents Latin Alphabet No. 9
     */
    static final CodeSet ISO8859_15_CODESET = new Iso8859_15CodeSet();

    /**
     * <code>UTF8</code> represents UTF8 1-6 bytes for every character
     * X/Open UTF-8; UCS Transformation Format 8 (UTF-8)
     */
    static final CodeSet UTF8_CODESET = new Utf8CodeSet();

    /**
     * <code>UTF16</code> represents extended UCS2, 2 or 4 bytes for every char
     * ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form
     */
    static final CodeSet UTF16_CODESET = new Utf16CodeSet();

    /**
     * <code>UCS2</code> represents UCS2, 2bytes for every char
     * ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form
     */
    static final CodeSet UCS2_CODESET = new Ucs2CodeSet();

    static final CodeSet MAC_ROMAN_CODESET = new MacRomanCodeSet();

    /**
     * All of the encodings supported by Jacorb. These should be listed in order of preference.
     */
    static final CodeSet[] KNOWN_ENCODINGS = { ISO8859_1_CODESET, ISO8859_15_CODESET, UTF16_CODESET, UTF8_CODESET, UCS2_CODESET, MAC_ROMAN_CODESET, ASCII_CODESET };

    /**
     * The default JVM platform encoding.
     */
    static final String DEFAULT_PLATFORM_ENCODING;

    /**
     * A 'null object' code set instance, used when no matching codeset is found.
     */
    static final CodeSet NULL_CODE_SET = new CodeSet( -1, "NO SUCH CODESET" );



    static
    {
        // See http://java.sun.com/j2se/1.4.1/docs/guide/intl/encoding.doc.html for
        // a list of encodings and their canonical names.
        //
        // http://developer.java.sun.com/developer/bugParade/bugs/4772857.html
        //
        // This allows me to get the actual canonical name of the encoding as the
        // System property may differ depending upon locale and OS.
        OutputStreamWriter defaultStream = new OutputStreamWriter( new ByteArrayOutputStream() );
        DEFAULT_PLATFORM_ENCODING = defaultStream.getEncoding();
        try
        {
            defaultStream.close();
        }
        catch( IOException e )
        {
        }
    }


    /** The standard CORBA identifier associated with this code set; used during negotiation. */
    private int id;

    /** The canonical name of this code set. */
    private String name;

    /** Identify this codeset as a local alias of some shared codeset and thus not to be added to the IOR */
    boolean isAlias;

    /**
     * Convert the CORBA standard id to a String name.
     *
     * @param cs
     * @return
     */
    public static String csName(int cs)
    {
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            if (cs == KNOWN_ENCODINGS[i].getId()) return KNOWN_ENCODINGS[i].getName();
        }
        return "Unknown TCS: 0x" + Integer.toHexString(cs);
    }


    /**
     * Returns the code set which matches the specified name, which should either be the canonical name of
     * a supported encoding or the hex representation of its ID.
     * @param name the string used to select a codeset.
     * @return the matching code set or NULL_CODE_SET if there are no matches.
     */
    public static CodeSet getCodeSet( String name ) {
        String ucName = name.toUpperCase();
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            CodeSet codeset = KNOWN_ENCODINGS[i];
            if (codeset.getName().equals( ucName )) return codeset;
        }

        try
        {
            int id = Integer.parseInt( name, 16 );
            for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
            {
                CodeSet codeset = KNOWN_ENCODINGS[i];
                if (id == codeset.getId()) return codeset;
            }
            return NULL_CODE_SET;
        }
        catch (NumberFormatException ex)
        {
            return NULL_CODE_SET;
        }
    }


    /**
     * Returns the code set which matches the specified ID.
     * @return the matching code set or NULL_CODE_SET if there are no matches.
     */
    public static CodeSet getCodeSet( int id ) {
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            CodeSet codeset = KNOWN_ENCODINGS[i];
            if (id == codeset.id) return codeset;
        }
        return NULL_CODE_SET;
    }


    public static CodeSet getNegotiatedCodeSet( ORB orb, CodeSetComponentInfo serverCodeSetInfo, boolean wide )
    {
        return getMatchingCodeSet( getSelectedComponent( orb.getLocalCodeSetComponentInfo(), wide ),
                                   getSelectedComponent( serverCodeSetInfo, wide ),
                                   wide );
    }


    static CodeSetComponent createCodeSetComponent( boolean wide, CodeSet nativeCodeSet )
    {
        ArrayList<CodeSet> codeSets = new ArrayList<CodeSet>();
        codeSets.add( nativeCodeSet );
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            if (KNOWN_ENCODINGS[i].supportsCharacterData( wide ) && !codeSets.contains( KNOWN_ENCODINGS[i] ))
            {
                if (!KNOWN_ENCODINGS[i].isAlias)
                    codeSets.add( KNOWN_ENCODINGS[i] );
            }
        }
        int nativeSet = codeSets.remove( 0 ).getId();
        int[] conversionSets = new int[codeSets.size()];
        for (int i = 0; i < conversionSets.length; i++)
        {
            conversionSets[i] = codeSets.get(i).getId();
        }
        return new CodeSetComponent( nativeSet, conversionSets );
    }


    public static CodeSet getMatchingCodeSet( CodeSetComponent local, CodeSetComponent remote, boolean wide )
    {
        CodeSet codeSet = getCodeSetIfMatched( local.native_code_set, remote );
        if (codeSet != null) return codeSet;

        for (int i = 0; i < local.conversion_code_sets.length; i++)
        {
            codeSet = getCodeSetIfMatched( local.conversion_code_sets[i], remote );
            if (codeSet != null) return codeSet;
        }

        return reportNegotiationFailure( local, remote, wide );
    }


    public static CodeSet getCodeSetIfMatched( int localCodeSetId, CodeSetComponent remote )
    {
        if (localCodeSetId == remote.native_code_set)
        {
            return getCodeSet( localCodeSetId );
        }
        else
        {
            for (int i = 0; i < remote.conversion_code_sets.length; i++)
            {
                if (localCodeSetId == remote.conversion_code_sets[i])
                {
                    return getCodeSet( localCodeSetId );
                }
            }
        }
        return null;
    }


    private static CodeSet reportNegotiationFailure( CodeSetComponent local, CodeSetComponent remote, boolean wide )
    {
        StringBuffer sb = new StringBuffer( "No matching ");
        if (wide) sb.append( "wide " );
        sb.append( "code set found. Client knows {" );
        appendCodeSetList( sb, local );
        sb.append( "}. Server offered {" );
        appendCodeSetList( sb, remote );
        sb.append( '}' );
        throw new CODESET_INCOMPATIBLE( sb.toString() );
    }


    private static void appendCodeSetList( StringBuffer sb, CodeSetComponent remote )
    {
        int code_set = remote.native_code_set;
        sb.append( toCodeSetString( code_set ) );
        for (int i = 0; i < remote.conversion_code_sets.length; i++) {
            sb.append( ',' ).append( toCodeSetString( remote.conversion_code_sets[i] ) );
        }
    }


    private static String toCodeSetString( int code_set )
    {
        String rawString = Integer.toHexString( code_set );
        return CODESET_PREFIX.substring( 0, CODESET_PREFIX.length() - rawString.length() ) + rawString;
    }


    private static CodeSetComponent getSelectedComponent( CodeSetComponentInfo info, boolean wide )
    {
        return wide ? info.ForWcharData : info.ForCharData;
    }


    /**
     * This interface represents a buffer from which character data can be read.
     */
    public static interface InputBuffer
    {

        /**  Reads the next byte from an in-memory buffer. */
        public byte readByte();

        /** Returns the current position in the buffer. */
        int get_pos();


        /**
         * Looks ahead in the buffer to see if a byte-order marker is present. If so, reads it from the buffer
         * and returns the result.
         * @return true if a marker indicating little-endian was read.
         */
        boolean readBOM();
    }


    /**
     * Represents a buffer to which character data may be written.
     */
    public static interface OutputBuffer
    {
        /**
         * Writes the specified byte to the buffer.
         * @param b the byte to write
         */
        void write_byte( byte b );

        /**
         * Forces short (2-byte) alignment and writes the specified value to the buffer.
         * @param value the value to write.
         */
        void write_short( short value );

        /**
         * Write an array of bytes to the buffer
         * @param b
         * @param offset
         * @param length
         */
        void write_octet_array(byte []b, int offset, int length);
    }


    public CodeSet( int id, String name )
    {
        this.id = id;
        this.name = name;
        this.isAlias = false;
    }


    /**
     * Returns true if this codeset supports the specified character type.
     * @param wide
     */
    public boolean supportsCharacterData( boolean wide )
    {
        return false;
    }


    /**
     * Returns true if this codeset supports multie-byte characters
     */
    public boolean supportsWideCharacterData()
    {
        return false;
    }


    /**
     * Returns the CORBA-standard id for this code set.
     */
    public int getId()
    {
        return id;
    }


    /**
     * Returns the canonical name of this code set.
     */
    public String getName()
    {
        return name;
    }

    @Override
    public String toString ()
    {
        return getName();
    }


    /**
     * Returns true if this code set requires byte-order-markers to be written to the beginning of a stream of text.
     * @param configuredForBom true if the orb has been configured to write byte-order-markers.
     */
    public boolean write_bom( boolean configuredForBom )
    {
        return false;
    }


    /**
     * Reads a wide character from the specified buffer.
     * @param buffer the buffer containing the data.
     * @param giop_minor the low-order byte of the giop version (1.x is assumed)
     * @param littleEndian true if the character is to be read low end first
     * @return the wide character.
     */
    public char read_wchar( InputBuffer buffer, int giop_minor, boolean littleEndian )
    {
        throw new MARSHAL( "Bad wide char codeSet: " + getName() );
    }


    /**
     * Reads a wide string from the buffer. The length indicator is presumed already to have been read.
     * @param buffer          the buffer from which to read the string
     * @param lengthIndicator the length indicator already read
     * @param giop_minor      the low-order byte of the giop version (1.x is assumed)
     * @param littleEndian    true if the characters are to be read low end first
     * @return a string possibly containing wide characters.
     */
    public String read_wstring( InputBuffer buffer, int lengthIndicator, int giop_minor, boolean littleEndian )
    {
        throw new MARSHAL( "Bad wide char codeSet: " + getName() );
    }


    /**
     * Writes a character to the buffer with the appropriate encoding.
     * @param buffer       the buffer to which the character is written
     * @param c            the character to write
     * @param write_bom    true if a byte-order-marker (indicating big-endian) should be written
     * @param write_length true if the length of the character should be written
     * @param giop_minor   the low-order byte of the giop version (1.x is assumed)
     */
    public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length, int giop_minor )
    {
        throw new CODESET_INCOMPATIBLE("Bad codeset: " + getName() );
    }

    /**
     * Writes a sting to the buffer with the appropriate encoding.
     * @param buffer       the buffer to which the string is written
     * @param s            the string to write
     * @param write_bom    true if a byte-order-marker (indicating big-endian) should be written
     * @param write_length true if the length of the character should be written
     * @param giop_minor   the low-order byte of the giop version (1.x is assumed)
     */
    public void write_string(OutputBuffer buffer, String s, boolean write_bom, boolean write_length, int giop_minor)
    {
        for (int i = 0; i < s.length(); i++)
        {
            this.write_char(buffer, s.charAt(i), write_bom, write_length, giop_minor);
        }
    }

    /**
     * Returns the length of the string just written to the buffer.
     * @param string     the string written
     * @param startPos   the starting position at which the string was written
     * @param currentPos the current buffer position
     */
    public int get_wstring_size( String string, int startPos, int currentPos )
    {
        return 0;
    }


    /**
     * Reads a wide string from the buffer according to GIOP 1.2. The length indicator is presumed already to have been read.
     * @param buffer          the buffer from which to read the string
     * @param size            the length indicator already read
     * @param giop_minor      the low-order byte of the giop version (must be &gt;= 2)
     * @return a string possibly containing wide characters.
     */
    final String readGiop12WString( InputBuffer buffer, int size, int giop_minor )
    {
        char buf[] = new char[ size ];
        int endPos = buffer.get_pos() + size;

        boolean wchar_litte_endian = buffer.readBOM();

        int i = 0;
        while( buffer.get_pos() < endPos )
        {
            buf[ i++ ] = read_wchar( buffer, giop_minor, wchar_litte_endian );
        }

        return new String( buf, 0, i );
    }


    static private class Iso8859_1CodeSet extends CodeSet {

        private Iso8859_1CodeSet()
        {
            super( 0x00010001, "ISO8859_1" );
        }


        /**
         * Only used for derived codesets
         */
        Iso8859_1CodeSet(int i, String name)
        {
            super( i, name);
        }


        /**
         * Returns true if 'wide' is not specified.
         */
        @Override
        public boolean supportsCharacterData( boolean wide )
        {
            return !wide;
        }


        @Override
        public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length_indicator, int giop_minor )
        {
            buffer.write_byte( (byte) c );
        }
    }

    static private class AsciiCodeSet extends Iso8859_1CodeSet {

        private AsciiCodeSet()
        {
            super( 0x00010001, "ASCII" );
            this.isAlias = true;
        }


        /**
         * Only used for derived codesets
         */
        AsciiCodeSet(int i, String name)
        {
            super( i, name);
            this.isAlias = true;
        }

    }

    static private class MacRomanCodeSet extends Iso8859_1CodeSet {

        private MacRomanCodeSet()
        {
            super( 0x00010001, "MacRoman" );
            this.isAlias = true;
        }


        /**
         * Only used for derived codesets
         */
        MacRomanCodeSet(int i, String name)
        {
            super( i, name);
            this.isAlias = true;
        }
    }

    static private class Iso8859_15CodeSet extends Iso8859_1CodeSet {

        private Iso8859_15CodeSet()
        {
            super( 0x0001000F, "ISO8859_15" );
        }

        @Override
        public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length_indicator, int giop_minor )
        {
            switch (c)
            {
                case '\u20AC':
                {
                    buffer.write_byte((byte) 0xA4);
                    break;
                }
                case '\u0160':
                {
                    buffer.write_byte((byte) 0xA6);
                    break;
                }
                case '\u0161':
                {
                    buffer.write_byte((byte) 0xA8);
                    break;
                }
                case '\u017D':
                {
                    buffer.write_byte((byte) 0xB4);
                    break;
                }
                case '\u017E':
                {
                    buffer.write_byte((byte) 0xB8);
                    break;
                }
                case '\u0152':
                {
                    buffer.write_byte((byte) 0xBC);
                    break;
                }
                case '\u0153':
                {
                    buffer.write_byte((byte) 0xBD);
                    break;
                }
                case '\u0178':
                {
                    buffer.write_byte((byte) 0xBE);
                    break;
                }
                default:
                {
                    super.write_char (buffer, c, write_bom, write_length_indicator, giop_minor);
                }
            }
        }
    }


    static private class Utf8CodeSet extends CodeSet {

        private Utf8CodeSet( )
        {
            super( 0x05010001, "UTF8" );
        }


        /**
         * Returns true for both wide and non-wide characters.
         */
        @Override
        public boolean supportsCharacterData( boolean wide )
        {
            return true;
        }


        @Override
        public char read_wchar( InputBuffer buffer, int giop_minor, boolean littleEndian )
        {
            if (giop_minor < 2)
            {
                throw new MARSHAL( "GIOP 1." + giop_minor +
                                   " only allows 2 Byte encodings for wchar, but the selected TCSW is UTF-8" );
            }

            short value = (short) (0xff & buffer.readByte());

            if ((value & 0x80) == 0)
            {
                return (char) value;
            }
            else if ((value & 0xe0) == 0xc0)
            {
                return (char) (((value & 0x1F) << 6) | (buffer.readByte() & 0x3F));
            }
            else
            {
                short b2 = (short) (0xff & buffer.readByte());
                return (char) (((value & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (buffer.readByte() & 0x3F));
            }
        }


        @Override
        public String read_wstring( InputBuffer source, int lengthIndicator, int giop_minor, boolean little_endian )
        {
            if (giop_minor < 2) throw new MARSHAL( "Bad wide char codeSet: " + getName() );
            return readGiop12WString( source, lengthIndicator, giop_minor );
        }


        @Override
        public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length_indicator, int giop_minor )
        {
            if( c <= 0x007F )
            {
                if( giop_minor == 2 && write_length_indicator )
                {
                    //the chars length in bytes
                    buffer.write_byte( (byte) 1);
                }

                buffer.write_byte( (byte) c );
            }
            else if( c > 0x07FF )
            {
                if( giop_minor == 2 && write_length_indicator )
                {
                    //the chars length in bytes
                    buffer.write_byte( (byte) 3 );
                }

                buffer.write_byte( (byte)(0xE0 | ((c >> 12) & 0x0F)) );
                buffer.write_byte( (byte)(0x80 | ((c >>  6) & 0x3F)) );
                buffer.write_byte( (byte)(0x80 | ((c >>  0) & 0x3F)) );
            }
            else
            {
                if( giop_minor == 2 && write_length_indicator )
                {
                    buffer.write_byte( (byte) 2 );   //the chars length in bytes
                }

                buffer.write_byte( (byte)(0xC0 | ((c >>  6) & 0x1F)) );
                buffer.write_byte( (byte)(0x80 | ((c >>  0) & 0x3F)) );
            }
        }

        @Override
        public void write_string( OutputBuffer buffer, String s, boolean write_bom, boolean write_length, int giop_minor )
        {
            try
            {
                byte[] bytes = s.getBytes(this.getName());
                buffer.write_octet_array(bytes, 0, bytes.length);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new CODESET_INCOMPATIBLE("Bad codeset: " + getName());
            }
        }

        @Override
        public int get_wstring_size( String s, int startPos, int currentPos )
        {
            return currentPos - startPos - 4;
        }
    }


    static abstract private class TwoByteCodeSet extends CodeSet
    {

        TwoByteCodeSet( int id, String name )
        {
            super( id, name );
        }


        /**
         * Returns true if the character type specified is 'wide'.
         */
        @Override
        public boolean supportsCharacterData( boolean wide )
        {
            return wide;
        }


        @Override
        public char read_wchar( InputBuffer buffer, int giop_minor, boolean littleEndian )
        {
            if (littleEndian)
            {
                return (char) ((buffer.readByte() & 0xFF) | (buffer.readByte() << 8));
            }
            else
            {
                return (char) ((buffer.readByte() << 8) | (buffer.readByte() & 0xFF));
            }
        }


        @Override
        public String read_wstring( InputBuffer source, int lengthIndicator, int giop_minor, boolean little_endian )
        {
            if( giop_minor == 2 )
            {
                return readGiop12WString( source, lengthIndicator, giop_minor );
            }
            else //GIOP 1.1 / 1.0 : length indicates number of 2-byte characters
            {
                char buf[] = new char[lengthIndicator];
                int endPos = source.get_pos() + 2* lengthIndicator;

                int i = 0;
                while( source.get_pos() < endPos )
                {
                    buf[ i++ ] = read_wchar( source, giop_minor, little_endian );
                }

                if( (i != 0) && (buf[ i - 1 ] == 0) ) //don't return terminating NUL
                {
                    return new String( buf, 0, i - 1 );
                }
                else   //doesn't have a terminating NUL. This is actually not allowed
                {
                    return new String( buf, 0, i );
                }
            }
        }


        @Override
        public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length_indicator, int giop_minor )
        {
            if (giop_minor < 2)
            {
                buffer.write_short( (short) c );   //UTF-16 char is treated as an ushort (write aligned)
            }
            else
            {
                if (write_length_indicator)  //the chars length in bytes
                {
                    buffer.write_byte( (byte) 2 );
                }

                if (write_bom) //big endian encoding
                {

                    buffer.write_byte( (byte) 0xFE );
                    buffer.write_byte( (byte) 0xFF );
                }

                //write unaligned
                buffer.write_byte( (byte)((c >> 8) & 0xFF) );
                buffer.write_byte( (byte) (c       & 0xFF) );
            }
        }

        @Override
        public int get_wstring_size( String s, int startPos, int currentPos )
        {
            return s.length() + 1;   // size in chars (+ NUL char)
        }
    }


    static private class Utf16CodeSet extends TwoByteCodeSet
    {

        private Utf16CodeSet()
        {
            super( 0x00010109, "UTF16" );
        }

        /** Returns the configured value to use BOMs only when specifically configured to do so. */
        @Override
        public boolean write_bom( boolean configuredForBom )
        {
            return false;
        }
    }


    /*
     * According to:
     * http://www.omg.org/issues/issue4008.txt
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4506930
     * UCS-2 should not write a BOM.
     */
    static private class Ucs2CodeSet extends TwoByteCodeSet
    {
        private Ucs2CodeSet()
        {
            super( 0x00010100, "UCS2" );
        }
    }
}
