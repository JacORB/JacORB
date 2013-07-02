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

package org.jacorb.test.bugs.bugjac542;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.test.common.MyNullLogger;

/**
 * @author Alphonse Bendt
 */
public class BugJac542Test extends TestCase
{
    public void testUnresolvableHostname() throws Exception
    {
        final String hostname = "does.not.exist";
        IIOPAddress address = new IIOPAddress(hostname, 2710);
        
        MockControl configControl = MockControl.createControl(Configuration.class);
        Configuration configMock = (Configuration) configControl.getMock();
        
        configControl.expectAndReturn(configMock.getLogger("jacorb.iiop.address"), new MyNullLogger());
        configControl.expectAndReturn(configMock.getAttributeAsBoolean("jacorb.dns.enable", false), true);
        configControl.expectAndReturn(configMock.getAttributeAsBoolean("jacorb.dns.force_lookup", true), true);
        configControl.expectAndReturn(configMock.getAttributeAsBoolean("jacorb.ipv6.hide_zoneid", true), true);
        configControl.expectAndReturn(configMock.getAttributeAsBoolean("jacorb.dns.eager_resolve", true), true);
        
        configControl.replay();
        
        address.configure(configMock);
        
        configControl.verify();
        
        assertEquals(address, address);
        assertEquals(address.hashCode(), address.hashCode());
    }
}
