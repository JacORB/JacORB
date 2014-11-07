package org.jacorb.test.bugs.bugpt319;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014  Gerald Brose.
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
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * <code>TestCase</code> is a test for the IORMutator plugin. It verifies that
 * the plugin can be activated and will intercept calls and that the transport
 * variable is updated correctly. It also verifies the plugin is not called when
 * it is turned off.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugPt319Test extends ClientServerTestCase
{
    /**
     * <code>DEMOIOR</code> is passed to the server.
     */
    public static final String DEMOIOR = "IOR:000000000000001B49444C3A64656D6F2F68656C6C6F2F476F6F644461793A312E300000000000020000000000000068000102000000000931302E312E302E340000803B00000015373036323632343836332F001437360C1137201036000000000000020000000000000008000000004A414300000000010000001C0000000000010001000000010501000100010109000000010501000100000001000000500000000000000002000000010000001C00000000000100010000000105010001000101090000000105010001000000010000001C00000000000100010000000105010001000101090000000105010001";

    /**
     * <code>IMRIOR</code> is used by the mutator to replace DEMOIOR.
     */
    public static final String IMRIOR = "IOR:000000000000003049444C3A6F72672F6A61636F72622F696D722F496D706C656D656E746174696F6E5265706F7369746F72793A312E3000000000010000000000000064000102000000000931302E312E302E3400008020000000127468655F496D522F496D52504F412F496D520000000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001";

    /**
     * <code>server</code> is the server object.
     *
     */
    private PT319 server;


    @Before
    public void setUp() throws Exception
    {
        MutatorImpl.totalIncomingObjects = 0;
        MutatorImpl.totalOutgoingObjects = 0;

        Properties props = new Properties();

        if (name.getMethodName().equals("test_mutate"))
        {
            props.put("jacorb.iormutator",
                      "org.jacorb.test.bugs.bugpt319.MutatorImpl");
        }

        setup = new ClientServerSetup
        (
            "org.jacorb.test.bugs.bugpt319.PT319Impl",
            props,
            props
        );
        server = PT319Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>tearDown</code> resets the fields in CDRStreams and the MutatorImpl
     * counters.
     *
     * @exception Exception if an error occurs
     */
    @After
    public void tearDown() throws Exception
    {
        MutatorImpl.totalIncomingObjects = 0;
        MutatorImpl.totalOutgoingObjects = 0;

        setup.tearDown();
        server._release();;
    }


    /**
     * <code>test_mutate</code> tests the mutator.
     *
     */
    @Test
    public void test_mutate()
    {
        org.omg.CORBA.Object obj = server.getObject
            (setup.getClientOrb().string_to_object(DEMOIOR));

        assertTrue ("Incoming objects should be one", (MutatorImpl.totalIncomingObjects == 1));
        assertTrue ("Outgoing objects should be one", (MutatorImpl.totalOutgoingObjects == 1));

        assertTrue
        (
            "Should return imr ior with mutate",
            IMRIOR.equals (setup.getClientOrb().object_to_string(obj))
        );
    }


    /**
     * <code>test_nomutate</code> tests the mutator is not called when
     * it is disabled.
     *
     */
    @Test
    public void test_nomutate()
    {
        org.omg.CORBA.Object obj = server.getObject
            (setup.getClientOrb().string_to_object(DEMOIOR));

        assertTrue ("Incoming objects should be zero", (MutatorImpl.totalIncomingObjects == 0));
        assertTrue ("Outgoing objects should be zero", (MutatorImpl.totalOutgoingObjects == 0));
        assertTrue
        (
            "Should return demo ior with no mutate",
            DEMOIOR.equals (setup.getClientOrb().object_to_string(obj))
        );
    }
}
