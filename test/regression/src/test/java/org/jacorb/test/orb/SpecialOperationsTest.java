package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2015 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;

public class SpecialOperationsTest extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("ignoreXBootClasspath", "true");

        setup = new ClientServerSetup ("org.jacorb.test.orb.BasicServerImpl", null, props );
    }

    @Test( expected = INITIALIZE.class)
    public void test_get_interface()
    {
        server._get_interface();
    }

    @Test ( expected = BAD_OPERATION.class )
    public void test_get_component()
    {
        server._get_component();
    }

    @Test
    @Ignore
    public void test_get_repoid()
    {
        server._repository_id();
    }

    @Test
    @Ignore
    public void test_get_policy_override()
    {
        server._set_policy_overrides(new Policy[] {}, SetOverrideType.ADD_OVERRIDE);
    }
}
