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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.CharServer;
import org.jacorb.test.CharServerHelper;
import org.jacorb.test.CharServerPackage.DataFlavour;
import org.jacorb.test.CharServerPackage.DataFlavourHelper;
import org.jacorb.test.CharServerPackage.NameValuePair;
import org.jacorb.test.CharServerPackage.wcharSeqHolder;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.CharHolder;

public class CharTest extends ClientServerTestCase
{
    private CharServer server;

    // a few character constants for testing
    private static final char E_ACUTE   = '\u00e9'; // in Latin-1
    private static final char EURO_SIGN = '\u20AC'; // not in Latin-1

    public CharTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = CharServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Client/server char tests" );
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.CharServerImpl" );
        TestUtils.addToSuite(suite, setup, CharTest.class);

        return setup;
    }

    // char tests

    public void test_pass_in_char()
    {
        for (short c=0; c<255; c++)
        {
            short result = server.pass_in_char( (char)c );
            assertEquals( c, result );
        }
    }

    public void test_pass_in_char_illegal()
    {
        short result = -1;
        try
        {
            result = server.pass_in_char( (char)256 );
            fail( "exception expected for (char)256" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }

        try
        {
            result = server.pass_in_char( EURO_SIGN );
            fail( "exception expected for euro sign" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }

        try
        {
            result = server.pass_in_char( (char)0xffff );
            fail( "exception expected for (char)0xffff" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }

    public void test_pass_out_char()
    {
        CharHolder x = new CharHolder ( 'a' );
        server.pass_out_char( (short)'c', x );
        assertEquals( 'c', x.value );

        server.pass_out_char( (short)E_ACUTE, x );
        assertEquals( E_ACUTE, x.value );
    }

    public void test_pass_out_char_illegal()
    {
        CharHolder x = new CharHolder ( 'a' );
        try
        {
            server.pass_out_char( (short)EURO_SIGN, x );
            fail( "exception expected for euro sign" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }

        try
        {
            server.pass_out_char( (short)0x8fff, x );
            fail( "exception expected for (char)0x8fff" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }

    public void test_pass_inout_char()
    {
        CharHolder x = new CharHolder( 'a' );
        server.pass_inout_char( x );
        assertEquals( 'A', x.value );

        x.value = E_ACUTE;
        server.pass_inout_char( x );
        // expect capital E_ACUTE
        assertEquals( '\u00c9', x.value );
    }

    public void test_pass_inout_char_illegal()
    {
        CharHolder x = new CharHolder( EURO_SIGN );
        try
        {
            server.pass_inout_char( x );
            fail( "exception expected for euro sign" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }

    public void test_return_char()
    {
        for (short c = 0; c < 255; c++)
        {
            char result = server.return_char( c );
            assertEquals( (char)c, result );
        }
    }

    public void test_return_char_illegal()
    {
        try
        {
            char result = server.return_char( (short)EURO_SIGN );
            fail( "exception expected for euro sign" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION e ) { }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }

    }

    public void test_bounce_char()
    {
        char result = server.bounce_char( 'a' );
        assertEquals( 'a', result );
    }

    // wchar tests

    public void test_pass_in_wchar()
    {
        short result = server.pass_in_wchar( 'a' );
        assertEquals( (short)'a', result );

        result = server.pass_in_wchar( E_ACUTE );
        assertEquals( (short)E_ACUTE, result );

        result = server.pass_in_wchar( EURO_SIGN );
        assertEquals( (short)EURO_SIGN, result );

        result = server.pass_in_wchar( '\uA000' );
        assertEquals( (short)0xA000, result );
    }

    public void test_pass_out_wchar()
    {
        CharHolder x = new CharHolder( 'a' );
        server.pass_out_wchar( (short)'s', x );
        assertEquals( 's', x.value );

        server.pass_out_wchar( (short)E_ACUTE, x );
        assertEquals( E_ACUTE, x.value );

        server.pass_out_wchar( (short)EURO_SIGN, x );
        assertEquals( EURO_SIGN, x.value );

        server.pass_out_wchar( (short)0xA000, x );
        assertEquals( '\uA000', x.value );
    }

    public void test_pass_inout_wchar()
    {
        CharHolder x = new CharHolder( E_ACUTE );
        server.pass_inout_wchar( x );
        assertEquals ( '\u00c9', x.value );  // capital e acute
    }

    public void test_return_wchar()
    {
        char result = server.return_wchar( (short)'a' );
        assertEquals( 'a', result );

        result = server.return_wchar( (short)E_ACUTE );
        assertEquals( E_ACUTE, result );

        result = server.return_wchar( (short)EURO_SIGN );
        assertEquals( EURO_SIGN, result );

        result = server.return_wchar( (short)0xA000 );
        assertEquals( '\uA000', result );
    }

    public void test_bounce_wchar()
    {
        char result = server.bounce_wchar( EURO_SIGN );
        assertEquals( EURO_SIGN, result );
    }

    public void test_wchar_seq()
    {
        try
        {
            wcharSeqHolder argout = new wcharSeqHolder();
            wcharSeqHolder arginout = new wcharSeqHolder( new char[]{ 'a', 'a' } );
            wcharSeqHolder argin = new wcharSeqHolder( new char[]{ 'a', 'a' } );

            for( int i = 0; i < 1000; i++ )
            {
                //call remote op
                test( server.test_wchar_seq( new char[]{ 'a', 'a' },
                                             argout,
                                             arginout ));

                test( argout.value);
            }
        }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }


    public void test_return_dataflavour_inany()
    {
        try
        {
            DataFlavour flavour = new DataFlavour
                ("Test_Flavour", new NameValuePair[0]);

            org.omg.CORBA.Any data = server.return_dataflavour_inany( flavour );

            DataFlavour flavour2 = DataFlavourHelper.extract( data );

            assertEquals( flavour2.name, "Test_Flavour" );
        }
        catch( Exception e )
        {
            fail( "unexpected exception: " + e );
        }
    }

    private static void test( char[] arg )
    {
        assertEquals( arg[ 0 ], 'a' );
        assertEquals( arg[ 1 ], 'a' );
    }
}
