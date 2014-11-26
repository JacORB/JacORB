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

package org.jacorb.test.ir;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.omg.CORBA.Repository;

/**
 * base class for tests against the IR. this class will start a IR and feed the
 * contents of the specified IDL file(s) into it.
 * it will also configure the client ORB properly so that it can contact the IR.
 *
 * @author Alphonse Bendt
 */
public class AbstractIRServerTestCase
{
    protected static IFRServerSetup setup;
    protected Repository repository;

    @Before
    public void setUp() throws Exception
    {
        repository = setup.getRepository();

        // need to wait a bit as the server first starts up and issues its IOR and
        // later fails during load of the idl classes
        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws Exception
    {
        repository._release();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        setup.tearDown();
    }
}
