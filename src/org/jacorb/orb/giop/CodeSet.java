/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.giop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Properties;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetContext;
import org.omg.CONV_FRAME.CodeSetContextHelper;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.omg.CORBA.MARSHAL;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_CODE_SETS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Gerald Brose
 */
public class CodeSet
{
    /**
     * <code>ISO8859_1</code> represents standard ASCII.
     * It is ISO 8859-1:1987; Latin Alphabet No. 1
     */
    private static final CodeSet ISO8859_1_CODESET = new Iso8859_1CodeSet();

    /**
     * <code>ISO8859_15</code> represents Latin Alphabet No. 9
     */
    private static final CodeSet ISO8859_15_CODESET = new Iso8859_15CodeSet();

    /**
     * <code>UTF8</code> represents UTF8 1-6 bytes for every character
     * X/Open UTF-8; UCS Transformation Format 8 (UTF-8)
     */
    private static final CodeSet UTF8_CODESET = new Utf8CodeSet();

    /**
     * <code>UTF16</code> represents extended UCS2, 2 or 4 bytes for every char
     * ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form
     */
    private static final CodeSet UTF16_CODESET = new Utf16CodeSet();

    /**
     * <code>UCS2</code> represents UCS2, 2bytes for every char
     * ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form
     */
    private static final CodeSet UCS2_CODESET = new Ucs2CodeSet();

    /**
     * A 'null object' code set instance, used when no matching codeset is found.
     */
    private static final CodeSet NULL_CODE_SET = new CodeSet( -1, "NO SUCH CODESET" );

    /**
     * All of the encodings supported by Jacorb. These should be listed in order of preference.
     */
    private static CodeSet[] KNOWN_ENCODINGS = { ISO8859_1_CODESET, ISO8859_15_CODESET, UTF16_CODESET, UTF8_CODESET , UCS2_CODESET };


    /**
     * <code>logger</code> is the static logger for Codeset.
     */
    private static Logger logger = LoggerFactory.getLogger("jacorb.codeset");

    /** static flag that keeps track of the configuration status. */
    private static boolean isConfigured = false;

    /** The native code set for character data. */
    private static CodeSet nativeCodeSetChar = null; // will select from platform default;

    /** The native code set for wide character data. */
    private static CodeSet nativeCodeSetWchar = UTF16_CODESET;

    /** The definition of locally supported code sets provided to clients. */
    private volatile static CodeSetComponentInfo localCodeSetComponentInfo;

    /** The standard CORBA identifier associated with this code set; used during negotiation. */
    private int id;

    /** The canonical name of this code set. */

    private String name;


    public static String csName(int cs)
    {
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            if (cs == KNOWN_ENCODINGS[i].getId()) return KNOWN_ENCODINGS[i].getName();
        }
        return "Unknown TCS: 0x" + Integer.toHexString(cs);
    }


    /**
     * <code>configure</code> configures the logger and codesets. It is
     * synchronized as the configuration parameters are static and therefore
     * we do not want to 'collide' with another init.
     *
     * This class does not implement configurable which ideally it should. However
     * as this method is static it would conflict with it.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public synchronized static void configure (Configuration config) throws ConfigurationException
    {
        // Only do this once per JVM.
        if (!isConfigured)
        {
            String ncsc = config.getAttribute("jacorb.native_char_codeset", "");
            String ncsw = config.getAttribute("jacorb.native_wchar_codeset", "");

            if (ncsc != null && ! ("".equals (ncsc)))
            {
                CodeSet codeset = getCodeSet(ncsc);
                if (codeset != NULL_CODE_SET)
                {
                    nativeCodeSetChar = codeset;
                }
                else if (logger.isErrorEnabled())
                {
                    logger.error("Cannot set default NCSC to " + ncsc);
                }
                logger.info ("Set default native char codeset to " + codeset);
            }

            if (ncsw != null && ! ("".equals (ncsw)))
            {
                CodeSet codeset = getCodeSet(ncsw);
                if (codeset != NULL_CODE_SET)
                {
                    nativeCodeSetWchar = codeset;
                }
                else if (logger.isErrorEnabled())
                {
                    logger.error("Cannot set default NCSW to " + ncsw);
                }
                logger.info ("Set default native wchar codeset to " + codeset);
          }

            logger = config.getLogger("jacorb.codeset");
            isConfigured = true;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                    ("CodeSet is already configured; further attempts to reconfigure will be ignored!");
            }

        }
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
     * @ return the matching code set or NULL_CODE_SET if there are no matches.
     */
    public static CodeSet getCodeSet( int id ) {
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            CodeSet codeset = KNOWN_ENCODINGS[i];
            if (id == codeset.id) return codeset;
        }
        return NULL_CODE_SET;
    }


    public static CodeSet getTCSDefault()
    {
        if (nativeCodeSetChar != null) return nativeCodeSetChar;

        String sysenc = getDefaultEncoding();
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            CodeSet codeset = KNOWN_ENCODINGS[i];
            if (codeset.supportsCharacterData( /* wide */ false ) && sysenc.equals( codeset.getName() ))
            {
                return setNativeCodeSetChar( codeset );
            }
        }

        // didn't match any supported char encodings, default to iso 8859-1

        if (logger.isWarnEnabled())
        {
            logger.warn( "Warning - unknown codeset (" + sysenc + ") - defaulting to ISO-8859-1" );
        }
        return setNativeCodeSetChar( ISO8859_1_CODESET );
    }


    private static CodeSet setNativeCodeSetChar( CodeSet codeset )
    {
        nativeCodeSetChar = codeset;
        if( logger.isDebugEnabled() )
        {
            logger.debug("TCS set to " + codeset.getName() );
        }
        return codeset;
    }


    private static String getDefaultEncoding()
    {
        // See http://java.sun.com/j2se/1.4.1/docs/guide/intl/encoding.doc.html for
        // a list of encodings and their canonical names.
        //
        // http://developer.java.sun.com/developer/bugParade/bugs/4772857.html
        //
        // This allows me to get the actual canonical name of the encoding as the
        // System property may differ depending upon locale and OS.
        OutputStreamWriter defaultStream = new OutputStreamWriter( new ByteArrayOutputStream() );
        String sysenc = defaultStream.getEncoding();
        try
            {
                defaultStream.close();
            }
            catch( IOException e ) {}
        return sysenc;
    }


    public static CodeSet getTCSWDefault()
    {
        return nativeCodeSetWchar;
    }

    public static CodeSetContext getCodeSetContext( ServiceContext[] contexts )
    {
        for( int i = 0; i < contexts.length; i++ )
        {
            if( contexts[i].context_id == TAG_CODE_SETS.value )
            {
                // TAG_CODE_SETS found, demarshall
                CDRInputStream is = new CDRInputStream( contexts[i].context_data );
                is.openEncapsulatedArray();

                return CodeSetContextHelper.read( is );
            }
        }

        return null;
    }


    public static CodeSetComponentInfo getLocalCodeSetComponentInfo()
    {
        if (localCodeSetComponentInfo == null)
        {
            synchronized (CodeSet.class)
            {
               if (localCodeSetComponentInfo == null)
               {
                  localCodeSetComponentInfo = new CodeSetComponentInfo();
                  localCodeSetComponentInfo.ForCharData = createCodeSetComponent( /* wide */ false, getTCSDefault() );
                  localCodeSetComponentInfo.ForWcharData = createCodeSetComponent( /* wide */ true, getTCSWDefault() );
               }
            }
        }
        return localCodeSetComponentInfo;
    }


    private static CodeSetComponent createCodeSetComponent( boolean wide, CodeSet nativeCodeSet )
    {
        ArrayList<CodeSet> codeSets = new ArrayList<CodeSet>();
        codeSets.add( nativeCodeSet );
        for (int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            if (KNOWN_ENCODINGS[i].supportsCharacterData( wide ) && !codeSets.contains( KNOWN_ENCODINGS[i] ))
            {
                codeSets.add( KNOWN_ENCODINGS[i] );
            }
        }
        int nativeSet = ((CodeSet) codeSets.remove( 0 )).getId();
        int[] conversionSets = new int[codeSets.size()];
        for (int i = 0; i < conversionSets.length; i++)
        {
            conversionSets[i] = ((CodeSet) codeSets.get(i)).getId();
        }
        return new CodeSetComponent( nativeSet, conversionSets );
    }


    public static CodeSet getNegotiatedCodeSet( CodeSetComponentInfo serverCodeSetInfo, boolean wide )
    {
        return getMatchingCodeSet( getSelectedComponent( getLocalCodeSetComponentInfo(), wide ),
                                   getSelectedComponent( serverCodeSetInfo, wide ),
                                   wide );
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


    private static final String CODESET_PREFIX = "0x00000000";
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
    }


    public CodeSet( int id, String name )
    {
        this.id = id;
        this.name = name;
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
     * @param giop_minor      the low-order byte of the giop version (must be >= 2)
     * @return a string possibly containing wide characters.
     */
    final protected String readGiop12WString( InputBuffer buffer, int size, int giop_minor )
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
        protected Iso8859_1CodeSet(int i, String name)
        {
            super( i, name);
        }


        /**
         * Returns true if 'wide' is not specified.
         */
        public boolean supportsCharacterData( boolean wide )
        {
            return !wide;
        }


        public void write_char( OutputBuffer buffer, char c, boolean write_bom, boolean write_length_indicator, int giop_minor )
        {
            buffer.write_byte( (byte) c );
        }
    }


    static private class Iso8859_15CodeSet extends Iso8859_1CodeSet {

        private Iso8859_15CodeSet()
        {
            super( 0x0001000F, "ISO8859_15" );
        }

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
        public boolean supportsCharacterData( boolean wide )
        {
            return true;
        }


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


        public String read_wstring( InputBuffer source, int lengthIndicator, int giop_minor, boolean little_endian )
        {
            if (giop_minor < 2) throw new MARSHAL( "Bad wide char codeSet: " + getName() );
            return readGiop12WString( source, lengthIndicator, giop_minor );
        }


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


        public int get_wstring_size( String s, int startPos, int currentPos )
        {
            return currentPos - startPos - 4;
        }
    }


    static abstract private class TwoByteCodeSet extends CodeSet
    {

        protected TwoByteCodeSet( int id, String name )
        {
            super( id, name );
        }


        /**
         * Returns true if the character type specified is 'wide'.
         */
        public boolean supportsCharacterData( boolean wide )
        {
            return wide;
        }


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
        public boolean write_bom( boolean configuredForBom )
        {
            return false;
        }
    }


    static private class Ucs2CodeSet extends TwoByteCodeSet
    {

        private Ucs2CodeSet()
        {
            super( 0x00010100, "UCS2" );
        }


        /** Returns true to force a BOM at the start of every text stream. */
        public boolean write_bom( boolean configuredForBom )
        {
            return true;
        }
    }


    /*
     * This is useful for debugging to print out Operating System details and
     * the encoding of that system.
     *
     * Currently this prints the following information:
     *    Operating System
     *    OS Version
     *    OS Architecture
     *    User Region
     *    Java Version
     *    JacORB Version
     *    System File Encoding
     *    Cannonical Encoding
     * If we are running on a Unix system and have used the command line argument
     * '-a' then it also runs the commands:
     *    locale
     *    locale -a
     *
     * Remember the precendence levels of LC_ALL, LANG, LC_CTYPE etc. Preferred
     * way to override for *all* categories is to set LC_ALL. If you just set LANG
     * then if any other LC_* categories are set then these will take precedence.
     * See http://publib16.boulder.ibm.com/pseries/en_US/aixprggd/nlsgdrf/locale_env.htm
     */
    public static void main (String args[])
    {
        if (args != null && args.length > 0 &&
            ! (args[0].equals("-h") || args[0].equals("-a") || args[0].equals("-l")))
        {
            System.out.println("Usage: org.jacorb.orb.connection.CodeSet [-a | -l <codeset>] ");
            System.exit(1);
        }

        Properties props = System.getProperties ();

        String osName = (String)props.get ("os.name");

        System.out.println ("Operating system name: " + osName);
        System.out.println ("Operating system version: " + props.get ("os.version"));
        System.out.println ("Operating system architecture: " + props.get ("os.arch"));
        System.out.println ("User region: " + System.getProperty( "user.region"));
        System.out.println ("JVM: " + props.get ("java.vm.version"));
        System.out.println ("JacORB: " + org.jacorb.util.Version.longVersion);

        System.out.println("System file encoding property: " + System.getProperty( "file.encoding" ));

        String defaultIOEncoding = (new OutputStreamWriter(new ByteArrayOutputStream ())).getEncoding();
        System.out.println("Cannonical encoding: " + defaultIOEncoding);
        System.out.println("Default WChar encoding: " + nativeCodeSetWchar.getName() );

        // If we're not using Windows do some extra debug, printing out the locale information.
        if ((osName.toLowerCase ()).indexOf ("windows") == -1)
        {
            System.out.println("Locale is:");
            try
            {
                Process locale = Runtime.getRuntime().exec ("locale");

                BufferedReader buffer = new BufferedReader
                    (new InputStreamReader (locale.getInputStream()));

                while (true)
                {
                    String line = buffer.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    System.out.println("    " + line);
                }
                buffer.close();
            }
            catch (IOException e)
            {
                System.err.println("Caught exception " + e);
            }


            if (args != null &&
                args.length == 1 &&
                args[0].equals ("-a"))
            {
                System.out.println("All available locales are:");
                try
                {
                    Process locale = Runtime.getRuntime().exec ("locale -a");

                    BufferedReader buffer = new BufferedReader
                        (new InputStreamReader (locale.getInputStream()));

                    while (true)
                    {
                        String line = buffer.readLine();
                        if (line == null)
                        {
                            break;
                        }
                        System.out.println("        " + line);
                    }
                    buffer.close();
                }
                catch (IOException e)
                {
                    System.err.println("Caught exception " + e);
                }
            }
            else if (args != null &&
                     args.length == 2 &&
                     args[0].equals ("-l"))
            {
                CodeSet c = getCodeSet(args[1]);
                System.out.println ("Codeset " + args[1] + " is " + c.getName());
            }
        }
    }
}
