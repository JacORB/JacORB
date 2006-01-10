package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPullConsumer;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * @jmx.mbean  extends ="AbstractProxyMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPullSupplierImpl extends AbstractProxySupplier implements
        StructuredProxyPullSupplierOperations, StructuredProxyPullSupplierImplMBean
{
    /**
     * undefined StructuredEvent that is returned on unsuccessful pull operations.
     */
    protected static final StructuredEvent UNDEFINED_STRUCTURED_EVENT;

    // initialize undefinedStructuredEvent_
    static
    {
        ORB _orb = ORB.init();

        UNDEFINED_STRUCTURED_EVENT = new StructuredEvent();
        EventType _type = new EventType();
        FixedEventHeader _fixed = new FixedEventHeader(_type, "");
        Property[] _variable = new Property[0];
        UNDEFINED_STRUCTURED_EVENT.header = new EventHeader(_fixed, _variable);
        UNDEFINED_STRUCTURED_EVENT.filterable_data = new Property[0];
        UNDEFINED_STRUCTURED_EVENT.remainder_of_body = _orb.create_any();
    }

    /**
     * the associated Consumer.
     */
    private StructuredPullConsumer structuredPullConsumer_;

    // //////////////////////////////////////

    public StructuredProxyPullSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager,
                subscriptionManager, consumerAdmin);
    }

    public ProxyType MyType()
    {
        return ProxyType.PULL_STRUCTURED;
    }

    public void connect_structured_pull_consumer(StructuredPullConsumer consumer)
            throws AlreadyConnected
    {
        checkIsNotConnected();

        connectClient(consumer);

        structuredPullConsumer_ = consumer;

        logger_.info("connect structured_pull_consumer");
    }

    public StructuredEvent pull_structured_event() throws Disconnected
    {
        checkStillConnected();

        try
        {
            Message _message = getMessageBlocking();

            try
            {
                return _message.toStructuredEvent();
            } finally
            {
                _message.dispose();
            }
        } catch (InterruptedException e)
        {
            return UNDEFINED_STRUCTURED_EVENT;
        }
    }

    public StructuredEvent try_pull_structured_event(BooleanHolder hasEvent) throws Disconnected
    {
        checkStillConnected();

        Message _message = getMessageNoBlock();

        if (_message != null)
        {
            try
            {
                hasEvent.value = true;

                return _message.toStructuredEvent();
            } finally
            {
                _message.dispose();
            }
        }

        hasEvent.value = false;

        return UNDEFINED_STRUCTURED_EVENT;
    }

    public void disconnect_structured_pull_supplier()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        logger_.info("disconnect structured_pull_consumer");

        structuredPullConsumer_.disconnect_structured_pull_consumer();

        structuredPullConsumer_ = null;
    }

    public void disableDelivery()
    {
        // as no active deliveries are made this can be ignored
    }

    public void enableDelivery()
    {
        // as no active deliveries are made this can be ignored
    }

    public void deliverPendingData()
    {
        // as no active deliveries are made this can be ignored
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new StructuredProxyPullSupplierPOATie(this);
        }

        return thisServant_;
    }

    protected long getCost()
    {
        return 0;
    }
}