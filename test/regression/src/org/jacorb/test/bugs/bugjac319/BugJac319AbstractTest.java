package org.jacorb.test.bugs.bugjac319;

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

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * BugJac319*Test are tests for the IORMutator plugin. They verifies that
 * the plugin can be activated and will intercept calls and that the transport
 * variable is updated correctly. It also verifies the plugin is not called when
 * it is turned off.
 *
 * @author Nick Cross
 * @version $Id$
 */
public abstract class BugJac319AbstractTest extends ClientServerTestCase
{
    /**
     * <code>DEMOIOR</code> is passed to the server.
     */
    public static final String DEMOIOR = "IOR:000000000000001B49444C3A64656D6F2F68656C6C6F2F476F6F644461793A312E300000000000020000000000000068000102000000000931302E312E302E340000803B00000015373036323632343836332F001437360C1137201036000000000000020000000000000008000000004A414300000000010000001C0000000000010001000000010501000100010109000000010501000100000001000000500000000000000002000000010000001C00000000000100010000000105010001000101090000000105010001000000010000001C00000000000100010000000105010001000101090000000105010001";

    /**
     * <code>IMRIOR</code> is used by the mutator to replace DEMOIOR.
     */
    public static final String IMRIOR = "IOR:000000000000003049444C3A6F72672F6A61636F72622F696D722F496D706C656D656E746174696F6E5265706F7369746F72793A312E3000000000010000000000000064000102000000000931302E312E302E3400008020000000127468655F496D522F496D52504F412F496D520000000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001";

    protected JAC319 server;

    public BugJac319AbstractTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = JAC319Helper.narrow( setup.getServerObject() );
        MutatorImpl.reset();
    }

    public static Test suite(boolean doMutate, Class clazz)
    {
        Properties props = new Properties();

        if (doMutate)
        {
            props.put("jacorb.iormutator",
                      MutatorImpl.class.getName());
        }

        TestSuite suite = new TestSuite( "IORMutator tests (" + clazz.getName() + ")");

        ClientServerSetup setup = new ClientServerSetup
        (
            suite,
            JAC319Impl.class.getName(),
            props,
            props
        );

        TestUtils.addToSuite(suite, setup, clazz);

        return setup;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(BugJac319MutatorTest.suite());
        suite.addTest(BugJac319NoMutatorTest.suite());

        return suite;
    }
}
