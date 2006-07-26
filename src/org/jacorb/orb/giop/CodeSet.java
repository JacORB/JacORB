/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import java.util.Properties;

import org.omg.CONV_FRAME.CodeSetContext;
import org.omg.CONV_FRAME.CodeSetContextHelper;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_CODE_SETS;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.NullLogger;

import org.jacorb.config.Configuration;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class CodeSet
{
    /**
     * <code>ISO8859_1</code> represents standard ASCII.
     * It is ISO 8859-1:1987; Latin Alphabet No. 1
     */
    public static final int ISO8859_1 = 0x00010001;

    /**
     * <code>UTF8</code> represents UTF8 1-6 bytes for every character
     * X/Open UTF-8; UCS Transformation Format 8 (UTF-8)
     */
    public static final int UTF8 = 0x05010001;

    /**
     * <code>UTF16</code> represents extended UCS2, 2 or 4 bytes for every char
     * ISO/IEC 10646-1:1993; UTF-16, UCS Transformation Format 16-bit form
     */
    public static final int UTF16 = 0x00010109;


    /**
     * <code>ISO8859_STR</code> represents the canonical string form of ISO8859_1.
     */
    public static final String ISO8859_STR = "ISO8859_1";


    /**
     * <code>UTF8_STR</code> represents the canonical string form of UTF8.
     */
    public static final String UTF8_STR = "UTF8";


    /**
     * <code>UTF16_STR</code> represents the canonical string form of UTF16
     */
    public static final String UTF16_STR = "UTF16";


    /**
     * <code>logger</code> is the static logger for Codeset.
     */
    private static org.apache.avalon.framework.logger.Logger logger = new NullLogger();

    /**
     * static flag that keeps track of the configuration status.
     */
    private static boolean isConfigured = false;

    /**
     * Describe variable <code>nativeCodeSetChar</code> here.
     *
     */
    private static int nativeCodeSetChar = -1; //ISO8859_1;
    private static int nativeCodeSetWchar = UTF16;

    public static String csName(int cs)
    {
        switch(cs)
        {
            case ISO8859_1: return ISO8859_STR;
            case UTF16: return  UTF16_STR;
            case UTF8: return  UTF8_STR;
        }
        return "Unknown TCS: " + Integer.toHexString(cs);
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
                int value = csInt(ncsc);
                if (value != -1)
                {
                    nativeCodeSetChar = value;
                }
                else if (logger.isErrorEnabled())
                {
                    logger.error("Cannot set default NCSC to " + ncsc);
                }
            }

            if (ncsw != null && ! ("".equals (ncsw)))
            {
                int value = csInt(ncsw);
                if (value != -1)
                {
                    nativeCodeSetWchar = value;
                }
                else if (logger.isErrorEnabled())
                {
                    logger.error("Cannot set default NCSW to " + ncsw);
                }
            }

            logger = config.getNamedLogger("org.jacorb.orb.codeset");
            isConfigured = true;
        }
        else
        {
            logger.warn("CodeSet is configured already. Further attempts to configure CodeSet will be ignored!");
        }
    }


    public static int csInt(String name)
    {
        try
        {
            return Integer.parseInt(name,16);
        }
        catch (NumberFormatException ex)
        {
            // no problem, go on to match literal strings
        }
        String ucName = name.toUpperCase();
        if (ucName.equals(ISO8859_STR))
        {
            return ISO8859_1;
        }
        else if (ucName.equals(UTF8_STR))
        {
            return UTF8;
        }
        else if (ucName.equals(UTF16_STR))
        {
            return UTF16;
        }
        else
        {
            return -1;
        }
    }


    public static int getTCSDefault()
    {
        if( nativeCodeSetChar == -1 )
        {
            // See http://java.sun.com/j2se/1.4.1/docs/guide/intl/encoding.doc.html for
            // a list of encodings and their canonical names.
            //
            // http://developer.java.sun.com/developer/bugParade/bugs/4772857.html
            //
            // This allows me to get the actual canonical name of the encoding as the
            // System property may differ depending upon locale and OS.
            OutputStreamWriter defaultStream = new OutputStreamWriter
                ( new ByteArrayOutputStream () );
            String sysenc = defaultStream.getEncoding();
            try
            {
                defaultStream.close();
            }
            catch( IOException e ) {}
            if (sysenc.equals( ISO8859_STR ) )
            {
                nativeCodeSetChar = ISO8859_1;
            }
            else if ( sysenc.equals( UTF8_STR ) )
            {
                nativeCodeSetChar = UTF8;
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn
                    (
                        "Warning - unknown codeset (" +
                        sysenc +
                        ") - defaulting to ISO-8859-1"
                    );
                }
                nativeCodeSetChar = ISO8859_1;
            }
            if( logger.isDebugEnabled() )
            {
                logger.debug("TCS set to " + csName( nativeCodeSetChar));
            }
        }
        return nativeCodeSetChar;
    }

    public static int getTCSWDefault()
    {
        return nativeCodeSetWchar;
    }

    // at some point additional codeset alternatives are likely to be
    // added in which case this single conversion default will not be
    // sufficient.
    public static int getConversionDefault()
    {
        return UTF8;
    }

    /**
     * This method compares the codesets in the component with our
     * native codeset.
     */
    public static int selectTCS( CodeSetComponentInfo cs_info )
    {
        int with_native = selectCodeSet( cs_info.ForCharData,
                                         getTCSDefault() );

        if( with_native == -1 )
        {
            //no match with native codeset, so try with conversion
            //codeset

            return selectCodeSet( cs_info.ForCharData, getConversionDefault() );
        }
        else
        {
            return with_native;
        }
    }

    /**
     * This method compares the wide codesets in the component with our
     * native wide codeset.
     */
    public static int selectTCSW( CodeSetComponentInfo cs_info )
    {
        int with_native = selectCodeSet( cs_info.ForWcharData,
                                         getTCSWDefault() );

        if( with_native == -1 )
        {
            //no match with native codeset, so try with conversion
            //codeset

            return selectCodeSet( cs_info.ForWcharData,
                                  getConversionDefault() );
        }
        else
        {
            return with_native;
        }
    }

    private static int selectCodeSet( CodeSetComponent cs_component,
                                      int native_cs )
    {
        // check if we support server's native sets
        if( cs_component.native_code_set == native_cs )
        {
            return native_cs;
        }

        // is our native CS supported at server ?
        for( int i = 0; i < cs_component.conversion_code_sets.length; i++ )
        {
            if( cs_component.conversion_code_sets[i] == native_cs )
            {
                return native_cs;
            }
        }

        // can't find supported set ..
        return -1;
    }

    public static ServiceContext createCodesetContext( int tcs, int tcsw )
    {
        // encapsulate context
        final CDROutputStream os = new CDROutputStream();
        try
        {
            os.beginEncapsulatedArray();
            CodeSetContextHelper.write( os, new CodeSetContext( tcs, tcsw ));

            return new ServiceContext( TAG_CODE_SETS.value,
                    os.getBufferCopy() );
        }
        finally
        {
            os.close();
        }
    }

    public static CodeSetContext getCodeSetContext( ServiceContext[] contexts )
    {
        for( int i = 0; i < contexts.length; i++ )
        {
            if( contexts[i].context_id == TAG_CODE_SETS.value )
            {
                // TAG_CODE_SETS found, demarshall
                CDRInputStream is =
                new CDRInputStream( (org.omg.CORBA.ORB) null,
                                    contexts[i].context_data );
                is.openEncapsulatedArray();

                return CodeSetContextHelper.read( is );
            }
        }

        return null;
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
            (args[0].equals("-h") || ! args[0].equals("-a")))
        {
            System.out.println("Usage: org.jacorb.orb.connection.CodeSet [-a]");
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
        System.out.println("Default WChar encoding: " + csName(nativeCodeSetWchar));

        // If we're not using Windows do some extra debug, printing out the locale information.
        if ((osName.toLowerCase ()).indexOf ("windows") == -1 &&
            args != null &&
            args.length == 1 &&
            args[0].equals ("-a"))
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
    }
}
