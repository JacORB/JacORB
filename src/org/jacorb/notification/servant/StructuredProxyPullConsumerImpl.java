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
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.StructuredPullSupplier;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @jmx.mbean extends ="AbstractProxyConsumerMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPullConsumerImpl extends AbstractProxyConsumer implements
        StructuredProxyPullConsumerOperations, MessageSupplier, MessageSupplierDelegate,
        StructuredProxyPullConsumerImplMBean
{
    private StructuredPullSupplier pullSupplier_;

    private final long pollInterval_;

    private final PullMessagesUtility pollUtil_;

    private final PullMessagesOperation pullMessagesOperation_;

    // //////////////////////////////////////

    public StructuredProxyPullConsumerImpl(IAdmin admin, ORB orb, POA poa, Configuration config,
            TaskProcessor taskProcessor, MessageFactory mf, OfferManager offerManager,
            SubscriptionManager subscriptionManager, SupplierAdmin supplierAdmin)
    {
        super(admin, orb, poa, config, taskProcessor, mf, supplierAdmin, offerManager,
                subscriptionManager);

        pollInterval_ = config.getAttributeAsLong(Attributes.PULL_CONSUMER_POLL_INTERVAL,
                Default.DEFAULT_PULL_CONSUMER_POLL_INTERVAL);

        pollUtil_ = new PullMessagesUtility(taskProcessor, this);

        pullMessagesOperation_ = new PullMessagesOperation(this);
    }

    // //////////////////////////////////////

    public ProxyType MyType()
    {
        return ProxyType.PULL_STRUCTURED;
    }

    public void disconnect_structured_pull_consumer()
    {
        destroy();
    }

    public synchronized void connect_structured_pull_supplier(StructuredPullSupplier pullSupplier)
            throws AlreadyConnected
    {
        checkIsNotConnected();
        pullSupplier_ = pullSupplier;
        connectClient(pullSupplier);
        startTask();
    }

    protected void connectionSuspended()
    {
        stopTask();
    }

    public void connectionResumed()
    {
        startTask();
    }

    protected void disconnectClient()
    {
        stopTask();
        pullSupplier_.disconnect_structured_pull_supplier();

        pullSupplier_ = null;
    }

    protected void startTask()
    {
        pollUtil_.startTask(pollInterval_);
    }

    protected void stopTask()
    {
        pollUtil_.stopTask();
    }

    public synchronized Servant getServant()
    {
        if (thisServant_ == null)
        {
            thisServant_ = new StructuredProxyPullConsumerPOATie(this);
        }

        return thisServant_;
    }

    public MessageSupplierDelegate.PullResult pullMessages() throws Disconnected
    {
        BooleanHolder _hasEvent = new BooleanHolder();
        _hasEvent.value = false;
        StructuredEvent _event = pullSupplier_.try_pull_structured_event(_hasEvent);

        return new MessageSupplierDelegate.PullResult(_event, _hasEvent.value);
    }

    public void queueMessages(PullResult data)
    {
        Message _mesg = getMessageFactory().newMessage((StructuredEvent) data.data_, this);

        checkMessageProperties(_mesg);

        processMessage(_mesg);
    }

    public void runPullMessage() throws Disconnected
    {
        pullMessagesOperation_.runPull();
    }
}
