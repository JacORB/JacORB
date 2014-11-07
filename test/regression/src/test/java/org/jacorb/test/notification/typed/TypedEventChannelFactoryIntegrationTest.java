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

package org.jacorb.test.notification.typed;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.jacorb.test.notification.common.TypedServerTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;

public class TypedEventChannelFactoryIntegrationTest extends TypedServerTestCase
{
    private TypedEventChannelFactory factory_;

    @Before
    public void setUp() throws Exception
    {
        factory_ = getChannelFactory();
    }

    @Test
    public void testGetFactory()
    {
        assertNotNull(factory_);
    }

    @Test
    public void testCreateChannel() throws Exception
    {
        final IntHolder channelID = new IntHolder();
        final TypedEventChannel channel = factory_.create_typed_channel(new Property[0], new Property[0], channelID);
        assertNotNull(channel);

        final TypedEventChannel lookup = factory_.get_typed_event_channel(channelID.value);
        assertTrue(channel._is_equivalent(lookup));

        int[] all_typed_channels = factory_.get_all_typed_channels();
        assertTrue(Arrays.binarySearch(all_typed_channels, channelID.value) >= 0);
    }
}
