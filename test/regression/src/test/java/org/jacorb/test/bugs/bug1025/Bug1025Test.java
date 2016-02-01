package org.jacorb.test.bugs.bug1025;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2016 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.harness.TestUtils;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.runner.RunWith;
import org.omg.CORBA.ORB;

import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * @author Nick Cross
 */
@RunWith(BMUnitRunner.class)
public class Bug1025Test
{
    static
    {
            //System.setProperty("org.jboss.byteman.verbose", "true");
            //System.setProperty("org.jboss.byteman.debug", "true");
    }

    @Rule
    public final StandardOutputStreamLog log = new StandardOutputStreamLog
            (
                    TestUtils.verbose ? LogMode.LOG_AND_WRITE_TO_STREAM : LogMode.LOG_ONLY
            );

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Test(timeout = 60000)
    @BMRules(rules = {
            // Force an UnknownHostException to provoke the null ptr
            @BMRule(name = "getByName",
                    targetClass = "InetAddress",
                    targetMethod = "getByName(String)",
                    targetLocation = "AT EXIT",
                    condition = "callerMatches(\".*IIOPAddress.init_host\",true,true)",
                    action = "throw new java.net.UnknownHostException()")
    })
    public void testIIOPAddressConstruction() throws Exception
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put("OAPort", 4714);
        props.put("OAAddress", "iiop://invalid.name:4711");

        ORB.init((String[]) null, props);
    }
}
