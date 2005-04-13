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

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.container.CORBAObjectComponentAdapter;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.servant.ITypedEventChannel;
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
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.CachingComponentAdapter;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedEventChannelFactoryImpl extends AbstractChannelFactory implements
        TypedEventChannelFactoryOperations
{
    public TypedEventChannelFactoryImpl(PicoContainer container, ORB orb) throws UserException
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
            logger_.fatalError("error creating typed channel", e);

            throw new org.omg.CORBA.INTERNAL();
        }
    }

    protected AbstractEventChannel newEventChannel()
    {
        final MutablePicoContainer _container = PicoContainerFactory
                .createChildContainer(container_);

        ComponentAdapter typedChannelComponentAdapter = componentAdapterFactory_
                .createComponentAdapter(ITypedEventChannel.class, TypedEventChannelImpl.class, null);

        _container.registerComponent(new CachingComponentAdapter(typedChannelComponentAdapter));

        final int _channelID = createChannelIdentifier();

        IFactory _factory = new IFactory()
        {
            public MutablePicoContainer getContainer()
            {
                return _container;
            }

            public int getChannelID()
            {
                return _channelID;
            }

            public void destroy()
            {
                container_.removeChildContainer(_container);
            }
        };

        _container.registerComponentInstance(_factory);

        return (AbstractEventChannel) _container.getComponentInstance(ITypedEventChannel.class);
    }

    public int[] get_all_typed_channels()
    {
        return getAllChannels();
    }

    public TypedEventChannel get_typed_event_channel(int id) throws ChannelNotFound
    {
        return TypedEventChannelHelper.narrow(get_event_channel_servant(id));
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