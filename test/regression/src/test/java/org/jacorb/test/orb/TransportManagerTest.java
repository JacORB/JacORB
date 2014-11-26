package org.jacorb.test.orb;

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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.util.Properties;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;

/**
 * Test invalid OAAddress.
 */
public class TransportManagerTest
{
    @Test (expected=BAD_PARAM.class)
    public void testOAAddress() throws Exception
    {
        Properties server_props = new Properties();
        server_props.setProperty("OAAddress", "badiiop://localhost:45000");

        ORB.init((String[])null, server_props);
    }
}
