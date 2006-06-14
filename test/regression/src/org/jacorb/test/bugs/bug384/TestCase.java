package org.jacorb.test.bugs.bug384;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import junit.framework.*;

import org.jacorb.test.common.*;
import org.omg.PortableServer.*;


/**
 * Tests that the _is_a() operation on object references gives correct
 * results for both local and non-local objects.
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class TestCase
    extends ClientServerTestCase
{
    private org.omg.CORBA.Object testObject;
    private org.omg.CORBA.Object localTestObject;
    private POA poa;
    private org.omg.CORBA.ORB orb;

    public TestCase( String name, ClientServerSetup setup )
    {
        super( name, setup );
    }

    public void setUp()
    {
        testObject = setup.getServerObject();
        orb = org.omg.CORBA.ORB.init( new String[0], null);
    try
    {
        poa = POAHelper.narrow( orb.resolve_initial_references("RootPOA"));

        poa.the_POAManager().activate();

        localTestObject =
                poa.servant_to_reference( new TestObjectImpl());
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite( "bug 384 wrong is_a results" );

        try
        {
            ClientServerSetup setup =
                new ClientServerSetup( suite,
                                       "org.jacorb.test.bugs.bug384.TestObjectImpl" );

            suite.addTest( new TestCase( "testNonLocalIsA", setup ));
            suite.addTest( new TestCase( "testLocalIsA", setup ));
            suite.addTest( new TestCase( "testMarshall", setup ));

            return setup;
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public void testNonLocalIsA()
    {
        assertTrue( "Is_a incorrectly returns false for non-local object",
                     testObject._is_a( "IDL:org/jacorb/test/bugs/bug384/TestObject:1.0") );

        assertFalse( "Is_a incorrectly returns true for non-local object",
                    testObject._is_a( "IDL:omg.org/CosNaming/NamingContext:1.0" ));
    }

    public void testLocalIsA()
    {
        assertTrue( "Is_a incorrectly returns false for non-local object",
                     localTestObject._is_a( "IDL:org/jacorb/test/bugs/bug384/TestObject:1.0") );

        assertFalse( "Is_a incorrectly returns true for non-local object",
                    localTestObject._is_a( "IDL:omg.org/CosNaming/NamingContext:1.0" ));
    }

    public void testMarshall()
    {
        TestObject serverObj = TestObjectHelper.narrow( testObject );
        try
        {
            A[] result = serverObj.testMarshall();
        }
        catch (Exception e)
        {
            fail( "Caught an exception " + e );
        }
    }


    public void tearDown() throws Exception
    {
        super.tearDown();
        orb.shutdown( true );
    }


    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( TestCase.class );
    }

}
