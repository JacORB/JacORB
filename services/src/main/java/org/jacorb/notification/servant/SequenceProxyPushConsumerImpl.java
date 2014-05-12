package org.jacorb.notification.servant;

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


import org.jacorb.config.*;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.SequencePushSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyConsumerMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 */

public class SequenceProxyPushConsumerImpl extends AbstractProxyConsumer implements
        SequenceProxyPushConsumerOperations, SequenceProxyPushConsumerImplMBean, IProxyConsumer
{
    private SequencePushSupplier sequencePushSupplier_;

    ////////////////////////////////////////

    public SequenceProxyPushConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory mf, SupplierAdmin supplierAdmin,
            OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, mf, supplierAdmin, offerManager,
                subscriptionManager);
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_SEQUENCE;
    }

    protected void disconnectClient()
    {
        logger_.info("disconnect sequence_push_supplier");

        sequencePushSupplier_.disconnect_sequence_push_supplier();

        sequencePushSupplier_ = null;
    }

    public void connect_sequence_push_supplier(SequencePushSupplier supplier)
            throws AlreadyConnected
    {
        checkIsNotConnected();

        connectClient(supplier);

        sequencePushSupplier_ = supplier;

        logger_.info("connect sequence_push_supplier");
    }

    public void push_structured_events(StructuredEvent[] events) throws Disconnected
    {
        checkStillConnected();

        Message[] _messages = newMessages(events);
        
        for (int x = 0; x < _messages.length; ++x)
        {
            processMessage(_messages[x]);
        }
    }
    
    public void disconnect_sequence_push_consumer()
    {
        destroy();
    }

    public synchronized Servant newServant()
    {
        return new SequenceProxyPushConsumerPOATie(this);
    }
}