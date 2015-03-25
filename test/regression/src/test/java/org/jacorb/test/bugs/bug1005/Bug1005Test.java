package org.jacorb.test.bugs.bug1005;

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

import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.test.harness.TestUtils;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.runner.RunWith;

/**
 * @author Nick Cross
 */
@RunWith(BMUnitRunner.class)
public class Bug1005Test
{
    static
    {
        // System.setProperty("org.jboss.byteman.verbose", "true");
        // System.setProperty("org.jboss.byteman.debug", "true");
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
    @BMRule(name = "null-from-network-interface",
                    targetClass = "NetworkInterface",
                    targetMethod = "getDisplayName()",
                    targetLocation = "AT ENTRY",
                    action = "RETURN null"
    )
    public void testNullWithNetworkInterface() throws Exception
    {
        IIOPAddress.getNetworkInetAddresses();
    }
}
