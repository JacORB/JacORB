package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
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

import org.jacorb.config.*;
import org.jacorb.notification.container.CORBAObjectComponentAdapter;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryOperations;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelHelper;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedEventChannelFactoryImpl extends AbstractChannelFactory implements
        TypedEventChannelFactoryOperations
{
    public TypedEventChannelFactoryImpl(MutablePicoContainer container, ORB orb) throws UserException
    {
        super(container, orb);

        container_.registerComponent(new CORBAObjectComponentAdapter(
                TypedEventChannelFactory.class, TypedEventChannelFactoryHelper.narrow(thisRef_)));
    }

    public TypedEventChannel create_typed_channel(Property[] qosProps, Property[] adminProps,
            IntHolder intHolder) throws UnsupportedAdmin, UnsupportedQoS
    {
        try
        {
            AbstractEventChannel _channel = create_channel_servant(intHolder, qosProps, adminProps);

            addToChannels(intHolder.value, _channel);

            return TypedEventChannelHelper.narrow(_channel.activate());
        } catch (ConfigurationException e)
        {
            logger_.error("error creating typed channel", e);

            throw new org.omg.CORBA.INTERNAL();
        }
    }

    protected AbstractEventChannel newEventChannel()
    {
        final MutablePicoContainer _container = newContainerForChannel();

        _container.registerComponentImplementation(AbstractEventChannel.class, TypedEventChannelImpl.class);

        return (AbstractEventChannel) _container.getComponentInstance(AbstractEventChannel.class);
    }

    public int[] get_all_typed_channels()
    {
        return getAllChannels();
    }

    public TypedEventChannel get_typed_event_channel(int id) throws ChannelNotFound
    {
        return TypedEventChannelHelper.narrow(get_event_channel_servant(id).activate());
    }

    public Servant getServant()
    {
        return new TypedEventChannelFactoryPOATie(this);
    }

    public String getObjectName()
    {
        return "_ECFactory";
    }

    protected String getShortcut()
    {
        return "NotificationService";
    }

    protected org.omg.CORBA.Object create_abstract_channel(Property[] admin, Property[] qos,
            IntHolder id) throws UnsupportedQoS, UnsupportedAdmin
    {
        return create_typed_channel(admin, qos, id);
    }
}