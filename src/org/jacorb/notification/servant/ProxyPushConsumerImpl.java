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
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends = "AbstractProxyConsumerMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushConsumerImpl extends AbstractProxyConsumer implements
        ProxyPushConsumerOperations, ProxyPushConsumerImplMBean
{
    private PushSupplier pushSupplier_;

    ////////////////////////////////////////

    public ProxyPushConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory messageFactory,
            SupplierAdmin supplierAdmin, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, messageFactory, supplierAdmin, offerManager,
                subscriptionManager);
    }

    public ProxyType MyType()
    {
        return ProxyType.PUSH_ANY;
    }

    public void disconnect_push_consumer()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        if (pushSupplier_ != null)
        {
            pushSupplier_.disconnect_push_supplier();
            pushSupplier_ = null;
        }
    }

    /**
     * Supplier sends data to the consumer (this object) using this call.
     */
    public void push(Any event) throws Disconnected
    {
        checkStillConnected();

        logger_.debug("push Any into the Channel");

        Message _mesg = getMessageFactory().newMessage(event, this);

        checkMessageProperties(_mesg);

        processMessage(_mesg);
    }

    public void connect_any_push_supplier(org.omg.CosEventComm.PushSupplier pushSupplier)
            throws AlreadyConnected
    {
        logger_.info("connect any_push_supplier");

        checkIsNotConnected();

        pushSupplier_ = pushSupplier;

        connectClient(pushSupplier);
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new ProxyPushConsumerPOATie(this);
        }

        return thisServant_;
    }

}