package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.test.CodesetServer;
import org.jacorb.test.CodesetServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;

/**
 * <code>CodesetTest</code> is the Junit client implementation to test
 * codeset translations.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class CodesetTest extends ClientServerTestCase
{
    /**
     * The <code>server</code> reference.
     */
    private CodesetServer server;
    /**
     * <code>configuration</code> denotes whether if
     * equal to one then codeset is disabled
     * equal to two then codeset is enabled
     */
    private int configuration;


    // These constants are used during testing.
    /**
     * <code>BASIC</code> is a ASCII character.
     */
    public static final char BASIC = '@';
    /**
     * <code>E_ACUTE</code> is a Latin-1 character.
     */
    public static final char E_ACUTE = '\u00e9';
    /**
     * <code>Y_UMLAUT</code> is a Latin-1 character with diaeresis.
     */
    public static final char Y_UMLAUT = '\u00FF';
    /**
     * <code>EURO_SIGN</code> is a UTF-8 character
     */
    public static final char EURO_SIGN = '\u20AC';
    /**
     * <code>A_E</code> is a UTF-8 character
     */
    public static final char A_E = '\u01EC';
    /**
     * <code>KATAKANA</code> is a Japanese character
     * (a KATAKANA-HIRAGANA PROLONGED SOUND MARK)
     */
    public static final char KATAKANA = '\u30FC';

    /**
     * <code>UNI1</code> is a String with UTF-8 characters within it.
     */
    public static final String UNI1 = "\u00e3\u0081\u00a4\u00e3\u0081\u008b\u00e3\u0082\u008a\u00e3\u0081\u00be\u00e3\u0081\u009b\u0E01";
    /**
     * <code>UNI2</code> is a String with UTF-8 characters within it.
     */
    public static final String UNI2 = "\u00A9\u07FF Unicode";
    /**
     * <code>BLANK</code> is a empty string.
     */
    public static final String BLANK   = "";
    /**
     * <code>CONTROL</code> is a String with ASCII characters.
     */
    public static final String CONTROL = "BASIC_STRING";
    /**
     * <code>HANGUL</code> is a String with Korean characters
     * (some HANGUL CHOSEONG PIEUP-SIOS-KIYEOK, HANGUL CHOSEONG PIEUP-SIOS-TIKEUT)
     */
    public static final String HANGUL = "\u1122 , \u1123";

    /**
     * <code>BASIC_ARRAY</code> is an array with ASCII characters.
     */
    public static final char[] BASIC_ARRAY  = new char[]{'@', 'B'};
    /**
     * <code>LATIN_ARRAY</code> is an array with Latin-1 characters.
     */
    public static final char[] LATIN_ARRAY  = new char[]{E_ACUTE, Y_UMLAUT};
    /**
     * <code>EURO_ARRAY</code> is an array with ASCII characters.
     */
    public static final char[] EURO_ARRAY  = new char[]{EURO_SIGN, '9'};
    /**
     * <code>HANGUL_ARRAY</code> is an array with Korean characters.
     */
    public static final char[] HANGUL_ARRAY = new char[]{'\u1122','A','\u1123'};


    /**
     * Creates a new <code>CodesetTest</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     * @param codeset a <code>boolean</code> value
     */
    public CodesetTest(String name, ClientServerSetup setup, int config)
    {
        super(name, setup);

        configuration = config;
    }

    /**
     * <code>setUp</code> is used by Junit for initialising the tests.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = CodesetServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Codeset tests");

        suite.addTest(suite(1));
        suite.addTest(suite(2));

        return suite;
    }

    /**
     * <code>suite</code> sets up the server/client tests.
     *
     * @param config a <code>int</code> value
     * @return a <code>Test</code> value
     */
    private static Test suite(int config)
    {
        TestSuite suite = new TestSuite( "Client/server codeset tests" );

        if (CodeSet.getTCSDefault() != CodeSet.UTF8)
        {
            System.err.println
                ("WARNING - TESTS ARE NOT RUNNING WITH UTF8 - THEY MAY NOT PASS.");
        }

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        if (config == 1)
        {
            client_props.setProperty ("jacorb.codeset", "off");
            server_props.setProperty ("jacorb.codeset", "off");
        }
        else if (config == 2)
        {
            client_props.setProperty ("jacorb.codeset", "on");
            server_props.setProperty ("jacorb.codeset", "on");
        }
        else
        {
            throw new IllegalArgumentException();
        }

        server_props.setProperty ("jacorb.logfile.append", "on");


        ClientServerSetup setup =
        new ClientServerSetup( suite,
                               "org.jacorb.test.orb.CodesetServerImpl",
                               client_props,
                               server_props);


        suite.addTest( new CodesetTest( "test_pass_in_char1", setup, config) );
        suite.addTest( new CodesetTest( "test_pass_in_char2", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_char3", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_char4", setup, config ) );

        suite.addTest( new CodesetTest( "test_pass_in_string1", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_string2", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_string3", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_string4", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_string5", setup, config ) );
        suite.addTest( new CodesetTest( "test_pass_in_string6", setup, config ) );

        suite.addTest( new CodesetTest( "test_pass_in_char_array1", setup, config) );
        suite.addTest( new CodesetTest( "test_pass_in_char_array2", setup, config) );
        suite.addTest( new CodesetTest( "test_pass_in_char_array3", setup, config) );
        suite.addTest( new CodesetTest( "test_pass_in_char_array4", setup, config) );
        suite.addTest( new CodesetTest( "test_pass_in_char_array5", setup, config) );

        return setup;
    }


    /**
     * <code>test_pass_in_char1</code> tests characters regardless of codeset
     * translation settings.
     */
    public void test_pass_in_char1()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_char( "Y_UMLAUT", Y_UMLAUT ));
    }



    /**
     * <code>test_pass_in_char2</code> tests characters regardless of codeset
     * translation settings.
     */
    public void test_pass_in_char2()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_char( "E_ACUTE", E_ACUTE ));
    }


    /**
     * <code>test_pass_in_char3</code> tests characters regardless of codeset
     * translation settings.
     */
    public void test_pass_in_char3()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_char( "BASIC", BASIC ));
    }


    /**
     * <code>test_pass_in_char4</code> tests characters regardless of codeset
     * translation settings.
     */
    public void test_pass_in_char4()
    {
        // This should always fail as it is out of range for char.
        try
        {
            server.pass_in_char( "KATAKANA", KATAKANA );
        }
        catch (org.omg.CORBA.DATA_CONVERSION e)
        {
            // Pass
            return;
        }
        catch( Exception e )
        {
            fail("Unexpected exception: " + e);
        }
        fail("No exception thrown");
    }


    /**
     * <code>test_pass_in_string1</code> tests strings with and without codeset
     * translation.
     */
    public void test_pass_in_string1()
    {
        if( configuration == 2)
        {
            assertTrue (server.pass_in_string( "UNI1", UNI1 ));
        }
        else
        {
            assertFalse (server.pass_in_string( "UNI1", UNI1 ));
        }
    }


    /**
     * <code>test_pass_in_string2</code> tests strings with and without codeset
     * translation.
     */
    public void test_pass_in_string2()
    {
        if( configuration == 2)
        {
            assertTrue (server.pass_in_string( "UNI2", UNI2 ));
        }
        else
        {
            assertFalse (server.pass_in_string( "UNI2", UNI2 ));
        }
    }


    /**
     * <code>test_pass_in_string3</code> tests strings regardless of codeset
     * translation settings.
     */
    public void test_pass_in_string3()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_string( "BLANK", BLANK ));
    }


    /**
     * <code>test_pass_in_string4</code> tests strings regardless of codeset
     * translation settings.
     */
    public void test_pass_in_string4()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_string( "CONTROL", CONTROL ));
    }


    /**
     * <code>test_pass_in_string5</code> tests strings with and without codeset
     * translation.
     */
    public void test_pass_in_string5()
    {
        if( configuration == 2)
        {
            assertTrue (server.pass_in_string( "HANGUL", HANGUL ));
        }
        else
        {
            assertFalse (server.pass_in_string( "HANGUL", HANGUL ));
        }
    }


    /**
     * <code>test_pass_in_string6</code> tests strings with and without codeset
     * translation invoking the CDRStream layer directly.
     */
    public void test_pass_in_string6()
    {
        org.omg.CORBA.ORB myorb = setup.getClientOrb();

        CDROutputStream t1 = new CDROutputStream(myorb);

        t1.write_string(UNI1);

        CDRInputStream t2 = new CDRInputStream (myorb, t1.getBufferCopy());

        String result = t2.read_string();

        if( configuration == 2)
        {
            assertTrue (UNI1.equals (result));
        }
        else
        {
            assertFalse (UNI1.equals (result));
        }
    }



    /**
     * <code>test_pass_in_char_array1</code> tests character arrays regardless
     * of codeset translation settings.
     */
    public void test_pass_in_char_array1()
    {
        // This should always pass whether or not codeset translation is on.
        assertTrue (server.pass_in_char_array( "BASIC_ARRAY", BASIC_ARRAY ));
    }



    /**
     * <code>test_pass_in_char_array2</code> tests character arrays regardless
     * of codeset translation settings.
     */
    public void test_pass_in_char_array2()
    {
        assertTrue (server.pass_in_char_array( "LATIN_ARRAY", LATIN_ARRAY ));
    }


    /**
     * <code>test_pass_in_char_array3</code> tests character arrays with and without
     * codeset translation.
     */
    public void test_pass_in_char_array3()
    {
        try
        {
            server.pass_in_char_array("EURO_ARRAY", EURO_ARRAY);
        }
        catch (org.omg.CORBA.DATA_CONVERSION e)
        {
            // Pass
            return;
        }
        catch( Exception e )
        {
            fail("Unexpected exception: " + e);
        }
        fail("No exception thrown");
    }



    /**
     * <code>test_pass_in_char_array4</code> tests character arrays with and without
     * codeset translation.
     */
    public void test_pass_in_char_array4()
    {
        try
        {
            server.pass_in_char_array("HANGUL_ARRAY", HANGUL_ARRAY);
        }
        catch (org.omg.CORBA.DATA_CONVERSION e)
        {
            // Pass
            return;
        }
        catch( Exception e )
        {
            fail("Unexpected exception: " + e);
        }
        fail("No exception thrown");
    }



    /**
     * <code>test_pass_in_char_array5</code> tests character arrays with and without
     * codeset translation.
     */
    public void test_pass_in_char_array5()
    {
        try
        {
            byte []temp = (new String(new char [] {E_ACUTE}).getBytes("UTF-8"));
            char []topass = new char[2];
            topass[0]=(char)(temp[0] & 0xFF);
            topass[1]=(char)(temp[1] & 0xFF);

            // Multibyte tag assumes we are doing e_acute. It should always pass.
            assertTrue (server.pass_in_char_array( "multibyte", topass ));
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new RuntimeException( "Internal error - encoding issue " + e);
        }
    }
}
