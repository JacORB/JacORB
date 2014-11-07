package org.jacorb.test.poa;

/*
 *        JacORB  - a free Java ORB
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Test;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.InvalidPolicy;

/**
 * <code>ImplNameTest</code> tests creating transient and persistent
 * POAs when using the implname property.
 *
 * @author Nick Cross
 * @author Alphonse Bendt
 */
public class ImplNameTest extends ORBTestCase
{
    /**
     * We utilise testName so each local ORB can get a different set of properties for each test
     * @see org.jacorb.test.harness.ORBTestCase#patchORBProperties(java.util.Properties)
     */
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        if (name.getMethodName().equals("testNoImpl1"))
        {
            props.setProperty("jacorb.implname", "TEST_RANDOM_COMPONENT");
        }
        else if (name.getMethodName().equals("testNoImpl2"))
        {
            props.setProperty("jacorb.implname", "TEST_PERSISTENT_COMPONENT");
            props.setProperty("jacorb.use_imr", "off");
        }
        else if (name.getMethodName().equals("testNoImpl3"))
        {
            props.setProperty("jacorb.implname", "TEST_RANDOM_COMPONENT_TWO");
            props.setProperty("jacorb.logfile.append", "on");
        }
        else if (name.getMethodName().equals("testNoImpl4"))
        {
            props.setProperty("jacorb.implname", "");
        }
        else if (name.getMethodName().equals("testNoImpl5"))
        {
            props.setProperty("jacorb.implname", "");
        }
        else
        {
            fail ("Unknown name");
        }
    }


    /**
     * <code>testNoImpl1</code> tests that we can create a transient POA
     * specifying an implname but the IOR still contains a random component
     */
    @Test
    public void testNoImpl1 () throws Exception
    {
        // Create a child POA
        POA poa = rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                           {
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR(getORB(), orb.object_to_string(obj));

        assertTrue
        (
                CorbaLoc.parseKey( pior.get_object_key()).indexOf
                (orbProps.getProperty("jacorb.implname")) == -1
        );
    }


    /**
     * <code>testNoImpl2</code> tests that we can create a persistent POA
     * specifying an implname and the IOR contains that component
     */
    @Test
    public void testNoImpl2 () throws Exception
    {
        // Create a child POA
        POA poa = rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                           {
                    rootPOA.create_lifespan_policy( LifespanPolicyValue.PERSISTENT),
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR(getORB(), orb.object_to_string(obj));

        assertTrue
        (
                CorbaLoc.parseKey( pior.get_object_key()).indexOf
                (orbProps.getProperty("jacorb.implname")) != -1
        );
    }


    /**
     * <code>testNoImpl3</code> tests that we can creating two transient objects
     * specifying the same impl and object id information name but the IOR will
     * still be different.
     */
    @Test
    public void testNoImpl3 () throws Exception
    {
        // Create a child POA
        POA poa = rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                           {
                    rootPOA.create_lifespan_policy( LifespanPolicyValue.TRANSIENT),
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR( getORB(), orb.object_to_string(obj));


        // Now create number two.
        final ORB orb2 = (org.jacorb.orb.ORB)getAnotherORB(orbProps);

        rootPOA = (POAHelper.narrow( orb2.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        poa = rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                           {
                    rootPOA.create_lifespan_policy( LifespanPolicyValue.TRANSIENT),
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior2 = new ParsedIOR( orb2, orb2.object_to_string(obj));

        assertTrue
        (
                ! (CorbaLoc.parseKey( pior.get_object_key()).equals
                        (CorbaLoc.parseKey( pior2.get_object_key())))
        );
    }



    /**
     * <code>testNoImpl4</code> tests that we cannot create a persistent POA without
     * specifying an implname.
     */
    @Test
    public void testNoImpl4 () throws Exception
    {
        try
        {
            // Create a child POA
            rootPOA.create_POA
            (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                {
                    rootPOA.create_lifespan_policy( LifespanPolicyValue.PERSISTENT),
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                }
            );
            fail();
        }
        catch (InvalidPolicy e )
        {
            // expected
        }
    }


    /**
     * <code>testNoImpl5</code> tests that we can create a transient POA without
     * specifying an implname.
     */
    @Test
    public void testNoImpl5 () throws Exception
    {
        // Create a child POA
        rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[] {
                    rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                }
        );
    }
}
