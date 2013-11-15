/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.notification.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.easymock.MockControl;
import org.jacorb.notification.container.CORBAObjectComponentAdapter;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * @author Alphonse Bendt
 */
public class CORBAObjectComponentAdapterTest
{
    private MutablePicoContainer container;

    private MockControl controlObject;

    private org.omg.CORBA.Object mockObject;

    @Before
    public void setUp() throws Exception
    {
        container = new DefaultPicoContainer();

        controlObject = MockControl.createControl(org.omg.CORBA.Object.class);

        mockObject = (org.omg.CORBA.Object) controlObject.getMock();
    }

    @Test
    public void testAddReference()
    {
        mockObject._is_a("");
        controlObject.setReturnValue(true);

        controlObject.replay();

        container.registerComponent(new CORBAObjectComponentAdapter(org.omg.CORBA.Object.class,
                mockObject));

        assertEquals(mockObject, container.getComponentInstance(org.omg.CORBA.Object.class));

        controlObject.verify();
    }

    @Test
    public void testAddWrongReference()
    {
        mockObject._is_a(EventChannelHelper.id());
        controlObject.setReturnValue(false);

        controlObject.replay();
        try
        {
            new CORBAObjectComponentAdapter(EventChannel.class, mockObject);
            fail();
        } catch (AssignabilityRegistrationException e)
        {
            // expected
        }
        controlObject.verify();
    }
}