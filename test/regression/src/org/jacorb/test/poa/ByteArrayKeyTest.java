package org.jacorb.test.poa;

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

import junit.framework.*;
import junit.extensions.TestSetup;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.poa.util.ByteArrayKey;


/**
 * <code>ByteArrayKeyTest</code> tests JacORB ByteArrayKey class.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class ByteArrayKeyTest extends TestCase
{
    /**
     * <code>orb</code> is used to obtain the root poa.
     */
    private static org.omg.CORBA.ORB orb = null;



    /**
     * <code>ByteArrayKeyTest</code> constructor - for JUnit.
     *
     * @param name a <code>String</code> value
     */
    public ByteArrayKeyTest (String name)
    {
        super (name);
    }


    /**
     * <code>suite</code> lists the tests for Junit to run.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        TestSuite suite = new TestSuite ("ByteArrayKey Test");
        Setup setup = new Setup( suite );
        ORBSetup osetup = new ORBSetup( setup );

        suite.addTest (new ByteArrayKeyTest ("testKey1"));
        suite.addTest (new ByteArrayKeyTest ("testKey2"));

        return osetup;
    }


    /**
     * <code>testKey1</code> tests that JacORB can handle a null key.
     */
    public void testKey1 ()
    {
        ByteArrayKey bk = new ByteArrayKey( (byte[])null );

        try
        {
            bk.toString();
            bk.getBytes();
            bk.hashCode();

            if( System.identityHashCode( bk.toString() ) !=
                System.identityHashCode( bk.toString() )  )
            {
                fail( "Different Strings returned on toString" );
            }
        }
        catch( Exception e )
        {
            fail( "Caught exception processing ByteArrayKey" );
        }
    }


    /**
     * <code>testKey2</code> does some basic tests.
     */
    public void testKey2 ()
    {
        ByteArrayKey bk = new ByteArrayKey( ( "bytearraykeytest" ).getBytes() );

        try
        {
            bk.toString();
            bk.getBytes();
            bk.hashCode();

            if( System.identityHashCode( bk.toString() ) !=
                System.identityHashCode( bk.toString() )  )
            {
                fail( "Different Strings returned on toString" );
            }
        }
        catch( Exception e )
        {
            fail( "Caught exception processing ByteArrayKey" );
        }
    }


    /**
     * <code>Setup</code> is an inner class to initialize the ORB.
     */
    private static class Setup extends TestSetup
    {
        /**
         * Creates a new <code>Setup</code> instance.
         *
         * @param test a <code>Test</code> value
         */
        public Setup (Test test)
        {
            super (test);
        }

        /**
         * <code>setUp</code> sets the orb variable.
         */
        protected void setUp ()
        {
            org.omg.CORBA.Object obj = null;

            orb = ORBSetup.getORB ();
        }

        /**
         * <code>tearDown</code> does nothing for this test.
         */
        protected void tearDown ()
        {
        }
    }
}
