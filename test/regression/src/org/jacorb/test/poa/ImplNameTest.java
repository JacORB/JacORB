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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.test.orb.BasicServerImpl;
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
 * @version $Id$
 */
public class ImplNameTest extends TestCase
{
    private final List orbs = new ArrayList();

    private ORB newORB(Properties props)
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
        orbs.add(orb);
        return (ORB) orb;
    }

    protected void tearDown() throws Exception
    {
        for (Iterator i = orbs.iterator(); i.hasNext();)
        {
            ORB orb = (ORB) i.next();
            orb.shutdown(true);
        }
        orbs.clear();
    }

    /**
     * <code>testNoImpl1</code> tests that we can create a transient POA
     * specifying an implname but the IOR still contains a random component
     */
    public void testNoImpl1 () throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.implname", "TEST_RANDOM_COMPONENT");

        final ORB orb = newORB(props);

        POA rootPoa =
            (POAHelper.narrow( orb.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        POA poa = rootPoa.create_POA
        (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[]
                           {
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR(orb, orb.object_to_string(obj));

        assertTrue
        (
                CorbaLoc.parseKey( pior.get_object_key()).indexOf
                (props.getProperty("jacorb.implname")) == -1
        );
    }


    /**
     * <code>testNoImpl2</code> tests that we can create a persistent POA
     * specifying an implname and the IOR contains that component
     */
    public void testNoImpl2 () throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.implname", "TEST_PERSISTENT_COMPONENT");
        props.setProperty("jacorb.use_imr", "off");

        ORB orb = newORB(props);

        POA rootPoa =
            (POAHelper.narrow( orb.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        POA poa = rootPoa.create_POA
        (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[]
                           {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR( orb, orb.object_to_string(obj));

        assertTrue
        (
                CorbaLoc.parseKey( pior.get_object_key()).indexOf
                (props.getProperty("jacorb.implname")) != -1
        );
    }


    /**
     * <code>testNoImpl3</code> tests that we can creating two transient objects
     * specifying the same impl and object id information name but the IOR will
     * still be different.
     */
    public void testNoImpl3 () throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.implname", "TEST_RANDOM_COMPONENT_TWO");
        props.setProperty("jacorb.logfile.append", "on");

        final ORB orb1 = newORB(props);

        POA rootPoa =
            (POAHelper.narrow( orb1.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        POA poa = rootPoa.create_POA
        (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[]
                           {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.TRANSIENT),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );

        poa.the_POAManager().activate();

        // create the object reference
        poa.activate_object_with_id("Object".getBytes(), new BasicServerImpl());
        org.omg.CORBA.Object obj = poa.id_to_reference( "Object".getBytes() );

        ParsedIOR pior = new ParsedIOR( orb1, orb1.object_to_string(obj));


        // Now create number two.

        final ORB orb2 = newORB(props);

        rootPoa = (POAHelper.narrow( orb2.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        poa = rootPoa.create_POA
        (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[]
                           {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.TRANSIENT),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
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
    public void testNoImpl4 () throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.implname", "");

        final org.omg.CORBA.ORB orb = newORB(props);

        try
        {
            POA rootPoa =
                (POAHelper.narrow( orb.resolve_initial_references( "RootPOA" )));

            // Create a child POA
            rootPoa.create_POA
            (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[]
                {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
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
    public void testNoImpl5 () throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.implname", "");

        final org.omg.CORBA.ORB orb = newORB(props);

        POA rootPoa =
            (POAHelper.narrow( orb.resolve_initial_references( "RootPOA" )));

        // Create a child POA
        rootPoa.create_POA
        (
                "TestServerPOA",
                rootPoa.the_POAManager(),
                new Policy[] {
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                           }
        );
    }
}
