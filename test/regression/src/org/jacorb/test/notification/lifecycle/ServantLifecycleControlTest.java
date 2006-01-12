/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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

package org.jacorb.test.notification.lifecycle;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.jacorb.notification.lifecycle.IServantLifecyle;
import org.jacorb.notification.lifecycle.ServantLifecyleControl;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

public class ServantLifecycleControlTest extends TestCase
{
    private MockControl lifecycleDelegateControl_;

    private IServantLifecyle lifecycleDelegateMock_;

    private ServantLifecyleControl objectUnderTest_;

    private MockControl poaControl_;

    private POA poaMock_;

    private MockControl referenceControl_;

    private org.omg.CORBA.Object referenceMock_;

    private Servant servantDummy_ = new Servant()
    {
        public String[] _all_interfaces(POA poa, byte[] objectID)
        {
            return null;
        }
    };
    
    private byte[] oidDummy_ = new byte[] {0};

    protected void setUp() throws Exception
    {
        referenceControl_ = MockControl.createControl(org.omg.CORBA.Object.class);
        referenceMock_ = (org.omg.CORBA.Object) referenceControl_.getMock();
        poaControl_ = MockControl.createControl(POA.class);
        poaMock_ = (POA) poaControl_.getMock();
        lifecycleDelegateControl_ = MockControl.createControl(IServantLifecyle.class);
        lifecycleDelegateMock_ = (IServantLifecyle) lifecycleDelegateControl_.getMock();
        objectUnderTest_ = new ServantLifecyleControl(lifecycleDelegateMock_);

        lifecycleDelegateMock_.getPOA();
        lifecycleDelegateControl_.setReturnValue(poaMock_, MockControl.ZERO_OR_MORE);
    }

    public void testActivate() throws Exception
    {
        lifecycleDelegateMock_.newServant();
        lifecycleDelegateControl_.setReturnValue(servantDummy_);

        poaMock_.servant_to_reference(servantDummy_);
        poaControl_.setReturnValue(referenceMock_);

        replayAll();

        assertEquals(referenceMock_, objectUnderTest_.activate());
        assertEquals(referenceMock_, objectUnderTest_.activate());

        verifyAll();
    }

    public void testDeactivate() throws Exception
    {
        lifecycleDelegateMock_.newServant();
        lifecycleDelegateControl_.setReturnValue(servantDummy_);

        poaMock_.servant_to_reference(servantDummy_);
        poaControl_.setReturnValue(referenceMock_);

        poaMock_.servant_to_id(servantDummy_);
        poaControl_.setReturnValue(oidDummy_);
        
        poaMock_.deactivate_object(oidDummy_);
        
        replayAll();

        objectUnderTest_.activate();
        objectUnderTest_.deactivate();
        objectUnderTest_.deactivate();
        
        verifyAll();
    }

    private void replayAll()
    {
        referenceControl_.replay();
        poaControl_.replay();
        lifecycleDelegateControl_.replay();
    }

    private void verifyAll()
    {
        referenceControl_.verify();
        poaControl_.verify();
        lifecycleDelegateControl_.verify();
    }
}
