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
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPushConsumerImpl extends AbstractProxyConsumer implements
        StructuredProxyPushConsumerOperations
{
    private StructuredPushSupplier pushSupplier_;

    ////////////////////////////////////////

    public StructuredProxyPushConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory mf, SupplierAdmin supplierAdmin,
            OfferManager offerManager, SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, mf, supplierAdmin, offerManager, subscriptionManager);
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_STRUCTURED;
    }

    public void push_structured_event(StructuredEvent structuredEvent) throws Disconnected
    {
        checkStillConnected();
        Message _mesg = getMessageFactory().newMessage(structuredEvent, this);

        checkMessageProperties(_mesg);
        getTaskProcessor().processMessage(_mesg);
    }

    public void disconnect_structured_push_consumer()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        logger_.info("disconnect structured_push_supplier");

        pushSupplier_.disconnect_structured_push_supplier();

        pushSupplier_ = null;
    }

    public void connect_structured_push_supplier(StructuredPushSupplier supplier)
            throws AlreadyConnected
    {
        checkIsNotConnected();

        connectClient(supplier);

        pushSupplier_ = supplier;

        logger_.info("connect structured_push_supplier");
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new StructuredProxyPushConsumerPOATie(this);
        }
        return thisServant_;
    }

    public org.omg.CORBA.Object activate()
    {
        return ProxyConsumerHelper.narrow(getServant()._this_object(getORB()));
    }
}