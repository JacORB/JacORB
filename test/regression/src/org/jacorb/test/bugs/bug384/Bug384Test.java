package org.jacorb.test.bugs.bug384;

/*
 *        JacORB  - a free Java ORB
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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.PortableServer.POA;


/**
 * Tests that the _is_a() operation on object references gives correct
 * results for both local and non-local objects.
 *
 * @author Gerald Brose
 */

public class Bug384Test
    extends ClientServerTestCase
{
    private org.omg.CORBA.Object testObject;
    private org.omg.CORBA.Object localTestObject;

    public Bug384Test( String name, ClientServerSetup setup )
    {
        super( name, setup );
    }

    public void setUp() throws Exception
    {
        testObject = setup.getServerObject();

        POA poa = setup.getClientRootPOA();

        poa.the_POAManager().activate();

        localTestObject =
            poa.servant_to_reference( new TestObjectImpl());
    }

    public void tearDown() throws Exception
    {
        testObject = null;
        localTestObject._release();
        localTestObject = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "bug 384 wrong is_a results" );

        ClientServerSetup setup =
            new ClientServerSetup( suite,
            "org.jacorb.test.bugs.bug384.TestObjectImpl" );

        TestUtils.addToSuite(suite, setup, Bug384Test.class);

        return setup;
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
        A[] result = serverObj.testMarshall();
        assertNotNull(result);
    }
}
