package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PullConsumer;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullSupplierImpl extends AbstractProxySupplier implements
        ProxyPullSupplierOperations
{
    private static final Any sUndefinedAny;

    static
    {
        ORB _orb = ORB.init();

        sUndefinedAny = _orb.create_any();
    }

    ////////////////////////////////////////

    private PullConsumer pullConsumer_ = null;

    ////////////////////////////////////////

    public ProxyPullSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration config, TaskProcessor taskProcessor, OfferManager offerManager, SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, config, taskProcessor, offerManager, subscriptionManager, consumerAdmin);
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_ANY;
    }

    public void disconnect_pull_supplier()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        if (pullConsumer_ != null)
        {
            logger_.info("disconnect any_pull_consumer");

            pullConsumer_.disconnect_pull_consumer();
            pullConsumer_ = null;
        }
    }

    public Any pull() throws Disconnected
    {
        checkStillConnected();

        try
        {
            Message _event = getMessageBlocking();
            try
            {
                return _event.toAny();
            } finally
            {
                _event.dispose();
            }
        } catch (InterruptedException e)
        {
            logger_.fatalError("interrupted", e);

            throw new UNKNOWN();
        }
    }

    public Any try_pull(BooleanHolder hasEvent) throws Disconnected
    {
        checkStillConnected();

        hasEvent.value = false;

        Message _message = getMessageNoBlock();

        if (_message != null)
        {
            try
            {
                hasEvent.value = true;

                return _message.toAny();
            } finally
            {
                _message.dispose();
            }
        }

        return sUndefinedAny;
    }

    public void connect_any_pull_consumer(PullConsumer consumer) throws AlreadyConnected
    {
        logger_.info("connect any_pull_consumer");

        checkIsNotConnected();

        pullConsumer_ = consumer;

        connectClient(consumer);
    }

    public void enableDelivery()
    {
        // as delivery to this PullSupplier causes no remote calls
        // we can ignore this
    }

    public void disableDelivery()
    {
        // as delivery to this PullSupplier causes no remote calls
        // we can ignore this
    }

    public void deliverPendingData()
    {
        // as we do not actively deliver events we can ignore this
    }

    public Servant newServant()
    {
        return new ProxyPullSupplierPOATie(this);
    }

    protected long getCost()
    {
        return 0;
    }
}