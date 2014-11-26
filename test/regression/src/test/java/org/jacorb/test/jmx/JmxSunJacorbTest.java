/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
 *
 */

package org.jacorb.test.jmx;

import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class JmxSunJacorbTest extends AbstractJMXTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass",
                   "com.sun.corba.se.impl.orb.ORBImpl");
        props.put ("org.omg.CORBA.ORBSingletonClass",
                   "com.sun.corba.se.impl.orb.ORBSingleton");
        jmxSetup = new JMXClientServerSetup( props, null);
    }

    @Ignore
    @Test
    public void testAccessRemoteMBean() throws Exception
    {
        super.testAccessRemoteMBean();;
    }
}
