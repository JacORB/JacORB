package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelHelper;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.omg.PortableServer.Servant;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedEventChannelFactoryImpl
    extends EventChannelFactoryImpl
    implements TypedEventChannelFactoryOperations {

    private ChannelManager typedChannels_ = new ChannelManager();

    // Implementation of org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryOperations

    public TypedEventChannel create_typed_channel(Property[] qosProps,
                                                  Property[] adminProps,
                                                  IntHolder intHolder)
        throws UnsupportedAdmin, UnsupportedQoS {

        try {
            AbstractEventChannel _channel = create_channel_servant(intHolder,
                                                               qosProps,
                                                               adminProps);

            typedChannels_.addToChannels(intHolder.value, _channel);

            return TypedEventChannelHelper.narrow(_channel.activate());
        } catch (ConfigurationException e) {
            logger_.fatalError("error creating typed channel", e);

            throw new org.omg.CORBA.INTERNAL();
        }
    }

    protected AbstractEventChannel newEventChannelImpl() {
        return new TypedEventChannelImpl();
    }


    public int[] get_all_typed_channels() {
        return typedChannels_.get_all_channels();
    }


    public TypedEventChannel get_typed_event_channel(int n) throws ChannelNotFound {
        return TypedEventChannelHelper.narrow(typedChannels_.get_event_channel_servant(n));
    }


    public Servant getServant() {
        return new TypedEventChannelFactoryPOATie(this);
    }
}
