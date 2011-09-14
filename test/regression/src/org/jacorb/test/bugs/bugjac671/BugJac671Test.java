package org.jacorb.test.bugs.bugjac671;

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
import junit.framework.TestCase;
import org.jacorb.test.common.CommonSetup;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * <code>BugJac671Test</code> verifies that we can start two ORB/POAs
 * on different ports and that they do not clash.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac671Test extends TestCase
{
    public void test_orb_port() throws Exception
    {
        Properties props1 = new Properties();
        props1.setProperty ("OAPort", "18000");
        org.omg.CORBA.ORB orb1 = ORB.init(new String[0], props1);
        POA poa1 = POAHelper.narrow(orb1.resolve_initial_references("RootPOA"));
        poa1.the_POAManager().activate();


        Properties props2 = new Properties();
        props2.putAll(CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks"));
        org.omg.CORBA.ORB orb2 = ORB.init(new String[0], props2);
        POA poa2 = POAHelper.narrow(orb2.resolve_initial_references("RootPOA"));
        poa2.the_POAManager().activate();


        orb2.shutdown(true);
        orb2 = null;

        orb1.shutdown(true);
        orb1 = null;
    }
}
