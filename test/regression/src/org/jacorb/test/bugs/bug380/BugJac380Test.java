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

package org.jacorb.test.bugs.bug380;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.PrintIOR;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * @author Alphonse Bendt
 */
public class BugJac380Test extends ORBTestCase
{
    public void testSetIORProxyHostToIP() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.ior_proxy_host", "192.168.1.1");
        final String profileDetails = getProfileDetails(props);
        assertTrue(profileDetails, profileDetails.indexOf("192.168.1.1") >= 0);
    }

    public void testSetIORProxy_DNSEnabled() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.ior_proxy_host", "localhost");
        props.setProperty("jacorb.dns.enable", "on");
        final String profileDetails = getProfileDetails(props);
        
        // Really this should be returning "localhost" in the IOR but Windows appears
        // not to do that and always returns "127.0.0.1".
        assertTrue(profileDetails, 
            (TestUtils.isWindows() ? profileDetails.indexOf("127.0.0.1") >= 0 : profileDetails.indexOf("localhost") >= 0));
    }

    public void testSetIORProxy_DNSDisabled() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.ior_proxy_host", "localhost");
        props.setProperty("jacorb.dns.enable", "off");
        final String profileDetails = getProfileDetails(props);
        assertTrue(profileDetails, profileDetails.indexOf("127.0.0.1") >= 0);
    }

    /**
     * create a reference and return its details as string printed using dior.
     */
    private String getProfileDetails(Properties props) throws Exception, InvalidPolicy, AdapterAlreadyExists, WrongPolicy
    {
        ORBSetup setup = new ORBSetup(this, props);
        try
        {
            setup.setUp();

            POA rootPOA = setup.getRootPOA();
            POA testPOA = rootPOA.create_POA("MyPOA", rootPOA.the_POAManager(), new Policy[] {rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID)});
            byte[] key = new byte[] {1, 2, 3, 4};
            org.omg.CORBA.Object object = testPOA.create_reference_with_id(key, BasicServerHelper.id());

            ObjectImpl objectImpl = (ObjectImpl) object;
            Delegate delegate = (Delegate) objectImpl._get_delegate();

            ParsedIOR parsedIOR = delegate.getParsedIOR();

            StringWriter out = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(out);
            PrintIOR.printIOR(orb, parsedIOR, printWriter);
            printWriter.close();

            return out.toString();
        }
        finally
        {
            setup.tearDown();
        }
    }
}
