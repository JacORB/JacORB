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

package org.jacorb.test.bugs.bug943;

import java.util.Properties;
import org.jacorb.test.harness.CommonSetup;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.INITIALIZE;
import org.omg.PortableServer.POAHelper;

/**
 * @author Nick Cross
 */
public class Bug943Test extends ORBTestCase
{
    @Test(expected=INITIALIZE.class)
    public void test_poa_ssl_port() throws Exception
    {
        Properties props = new Properties();
        props.putAll(CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks"));
        props.setProperty ("OASSLPort", "7777");

        org.omg.CORBA.ORB orb1 = getAnotherORB(props);
        POAHelper.narrow(orb1.resolve_initial_references("RootPOA"));

        org.omg.CORBA.ORB orb2 = getAnotherORB(props);
        POAHelper.narrow(orb2.resolve_initial_references("RootPOA"));
    }
}
