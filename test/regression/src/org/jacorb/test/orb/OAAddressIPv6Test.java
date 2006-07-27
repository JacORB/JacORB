package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2005  Gerald Brose.
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

import junit.framework.TestCase;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

/**
 * Tests the ORB's OAAddress property using an IPv6 address.
 *
 * @author Adam Mitz (mitza@ociweb.com)
 * @version $Id$
 */
public class OAAddressIPv6Test extends TestCase
{
    private static final String LISTEN_EP_V6 = "iiop://[::1]:45000";

    public void testOAAddress() throws org.omg.CORBA.UserException
    {
        Properties server_props = new Properties();
        server_props.setProperty("OAAddress", LISTEN_EP_V6);
        ORB myorb = ORB.init((String[])null, server_props);
        org.omg.CORBA.Object poa_obj =
          myorb.resolve_initial_references("RootPOA");
        POA root_poa = POAHelper.narrow(poa_obj);
        POAManager pm = root_poa.the_POAManager();
        SampleImpl servant = new SampleImpl();
        root_poa.activate_object(servant);
        pm.activate();
        myorb.destroy();
    }
}
