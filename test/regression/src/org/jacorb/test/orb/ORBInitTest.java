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

import junit.framework.*;
import junit.extensions.TestSetup;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.orb.ParsedIOR;
import org.omg.IIOP.ProfileBody_1_1;


/**
 * <code>ORBInitTest</code> tests ORBInit parsing
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class ORBInitTest extends TestCase
{
    /**
     * <code>ORBInitTest</code> constructor - for JUnit.
     *
     * @param name a <code>String</code> value
     */
    public ORBInitTest (String name)
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
        TestSuite suite = new TestSuite ("ORBInit Test");

        suite.addTest (new ORBInitTest ("testParse1"));
        suite.addTest (new ORBInitTest ("testParse2"));

        return suite;
    }


    /**
     * <code>testParse1</code>
     */
    public void testParse1 ()
    {
        String args[] = new String[3];
        args[0] = "-ORBInitRef";
        args[1] = "NameService";
        args[2] = "foo.ior";

        try
        {
            org.omg.CORBA.ORB orbtest = org.omg.CORBA.ORB.init( args, null );
        }
        catch (org.omg.CORBA.BAD_PARAM e )
        {
            // Correct exception
            return;
        }
        catch (Exception e )
        {
            fail( "Incorrect exception " + e);
        }
        fail( "No exception");
    }


    /**
     * <code>testParse2</code>
     */
    public void testParse2 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        try
        {
            org.omg.CORBA.ORB orbtest = org.omg.CORBA.ORB.init( args, null );
        }
        catch (Exception e )
        {
            fail( "Incorrect exception " + e);
        }
    }
}
