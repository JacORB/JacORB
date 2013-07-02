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

package org.jacorb.test.ir;

import java.io.File;
import java.util.List;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Repository;
import org.omg.PortableServer.POA;

/**
 * base class for tests against the IR. this class will start a IR and feed the
 * contents of the specified IDL file(s) into it.
 * it will also configure the client ORB properly so that it can contact the IR.
 *
 * @author Alphonse Bendt
 */
public class AbstractIRServerTestCase extends TestCase
{
    private IFRServerSetup setup;
    protected Repository repository;

    public AbstractIRServerTestCase(String name, IFRServerSetup setup)
    {
        super(name);

        this.setup = setup;
    }

    protected final void setUp() throws Exception
    {
        repository = setup.getRepository();

        // need to wait a bit as the server first starts up and issues its IOR and
        // later fails during load of the idl classes
        Thread.sleep(1000);

        doSetUp();
    }

    protected void doSetUp() throws Exception
    {
        // empty to be overridden
    }

    protected final void tearDown() throws Exception
    {
        doTearDown();

        repository = null;
    }

    protected void doTearDown() throws Exception
    {
        // empty to be overridden
    }

    /**
     * this is the main suite method.
     *
     * @param idlFile file or directory containing the idl the ir server should work on. (String or File)
     * @param testClazz class containing the tests.
     * @param idlArgs additional arguments that should be passed to the idl compiler. (List or String[])
     * @param optionalIRServerProps additional properties that should be passed to the IFR Server (maybe null)
     * @return a TestSuite
     */
    private static Test _suite(Object idlFile, Class testClazz, Object idlArgs, Properties optionalIRServerProps)
    {
        TestSuite suite = new TestSuite();

        IFRServerSetup setup = new IFRServerSetup(suite, idlFile, idlArgs, optionalIRServerProps);

        TestUtils.addToSuite(suite, setup, testClazz);

        return setup;
    }

    protected POA getClientRootPOA()
    {
        return setup.getClientRootPOA();
    }

    /**
     * this is the main suite method.
     *
     * @param idlFile file or directory containing the idl the ir server should work on.
     * @param testClazz class containing the tests.
     * @param idlArgs additional arguments that should be passed to the idl compiler.
     * @param optionalIRServerProps additional properties that should be passed to the IFR Server
     * @return a TestSuite
     */
    protected static Test suite(File idlFile, Class testClazz, List idlArgs, Properties optionalIRServerProps)
    {
        return _suite(idlFile, testClazz, idlArgs, optionalIRServerProps);
    }

    /**
     * convenience method. will not use additional idl args.
     */
    protected static Test suite(File idlFile, Class testClazz)
    {
        return _suite(idlFile, testClazz, null, null);
    }

    /**
     * convenience method
     * @string it is assumed that the idl file is located in the dir test/regression/idl.
     */
    public static Test suite(String idlFile, Class testClazz)
    {
        return _suite(TestUtils.testHome() + "/idl/" + idlFile, testClazz, null, null);
    }

    /**
     * convenience method
     */
    public static Test suite(String idlFile, Class testClazz, String[] idlArgs, Properties props)
    {
        return _suite(idlFile, testClazz, idlArgs, props);
    }
}
